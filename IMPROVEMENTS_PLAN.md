# Improvements plan

Review date: 2026-10-28. Tree: `master` at `7ca42dc` (`v1.2.3-2-g7ca42dc`). The Java app under `deprecated/` is deleted in the working tree (202 files, not committed). History still contains it, including `deprecated/token.txt`. Rotate that credential if it was real.

This pass read the live modules, tests, Docker files, and docs. It did not run tests or Docker, and it did not talk to the processor.

## Decisions

These override the earlier Poetry-and-`config.json` release notes.

- This project ships a Docker image only. It does not publish to PyPI and it does not use Poetry.
- The image tag is the `version` field in `pyproject.toml`. Nothing else (git describe, `_version.py`, `version.txt`, a wheel filename, the changelog) picks the tag.
- `compose.yaml` is the deployment model. It pulls the image we pushed and starts the container. It does not build the image and it does not mount `config.json`.
- Processor settings live in the `environment:` block of `compose.yaml`. The process reads environment variables. `config.json` is not used.
- Registry and repository stay `monster-jj.jvj28.com:9092/lutron_qs_exporter`. Port stays **9992**.
- Prometheus and Grafana are services in the same `compose.yaml`, behind the Compose profile `monitoring`. A plain `docker compose up -d` starts only the exporter. `docker compose --profile monitoring up -d` also starts Prometheus on 9090 and Grafana on host port 3001.
- Sample dashboards are `deploy/grafana/dashboards/lutron-overview.json` and `deploy/grafana/dashboards/lutron-keypads.json`. Someone who already runs Grafana imports those files and scrapes `host:9992` from their own Prometheus. The sample Prometheus config is `deploy/prometheus/prometheus.yml`.

`pyproject.toml` `version` is **1.2.4**, and `compose.yaml` pulls that tag. The scrape-behavior work that is still open is a later bump, **1.3.0**, in the same field.

## What the app does today

`lutron_qs_exporter` is a FastAPI process. `GET /metrics` loads `config.json`, opens telnet to the QS processor, reads firmware and design version, downloads `http://<address>/DbXmlInfo.xml`, walks floors, areas, zones, and keypad LEDs, and fills Prometheus gauges. `/` redirects to `/metrics`. `/data` returns the cached design dict. `/config` returns the host config, including the password, plus a version string.

Postgres, Redis, background jobs, and the JSON control API in `README.md` and `CHANGELOG.md` are not in this code. They belonged to the removed Java service.

## Findings

### Release and packaging

| Issue | Where | Why it matters |
| --- | --- | --- |
| Several versions | `pyproject.toml` is `1.1.12`. `_version.py` is `1.2.3`. `git describe` is `v1.2.3-2-g7ca42dc`. `dist/` has `lutron_qs_exporter-1.2.4.dev2+7ca42dc`. Changelog stops at 1.1.12. | The image tag must come from one field. Poetry dynamic versioning (`bump = true`) rewrites `_version.py` at build time and fights that. |
| Image build depends on Poetry | `Dockerfile` copies `dist/*.whl`. `docker-build.ps1` runs `poetry build`. Poetry is not on PATH. | We are not publishing a Python package. The image should copy the source and `pip install` it. |
| Dockerfile apt line | `RUN apt-get update & apt-get upgrade -y` | `&` backgrounds `update`. `upgrade` can run before the package lists exist. Apt lists stay in the layer. |
| No `.dockerignore` | repo root | `docker build` sends `venv/` (thousands of files). |
| Publish scripts | `docker-publish.sh` reads a missing `version.txt`, hardcodes Nexus user `admin`, reads `.secret`, and uses `$()`, which Bash runs as a command. `docker-publish.ps1` asks Poetry for the version and prints "tagged successfully" before it tags. | Both scripts assume a Poetry or `version.txt` release. Replace them with one script that reads `pyproject.toml`. |
| Run scripts | `docker-run.ps1` mounts `config.json`. `docker-run.sh` also mounts `static/data.json`, which is not in the repo. | Deployment is `docker compose pull` and `docker compose up -d`. These scripts should go. |
| README image is stale | `python:3.9-slim`, `requirements.txt`, hypercorn | The Dockerfile is `python:3.11-slim` and `fastapi run`. |
| Listen address | `CMD ["fastapi", "run", "asgi.py", "--port", "9992"]` | Pin `--host 0.0.0.0`. |
| Dirty `dist/` | `pip install /app/*.whl` | Every wheel in the folder gets installed. The new Dockerfile must not do this. |

### Runtime config

| Issue | Where | Why it matters |
| --- | --- | --- |
| File config | `config.py` requires `config.json` in the working directory | Compose cannot describe the deployment without a mounted file. Move `address`, `port`, `username`, `password`, and `log_level` to environment variables. |
| Password in `/config` | `app.py` `get_config` | The route returns `get_host_data()` unchanged. |
| Password in logs | `config.py` `logger.debug(cls._instance)` | Debug logs the whole config dict. |
| Tracked house address | `config.json` is in git (`192.168.1.40`, user `default`) | After the switch, delete this file from the tree. A non-default password does not belong in git. The compose file in the repo can hold the LAN values you actually deploy. |

Environment names for the compose `environment:` block:

| Variable | Replaces | Required |
| --- | --- | --- |
| `LUTRON_ADDRESS` | `address` | yes |
| `LUTRON_PORT` | `port` | no, default `23` |
| `LUTRON_USERNAME` | `username` | yes |
| `LUTRON_PASSWORD` | `password` | yes |
| `LOG_LEVEL` | `log_level` | no, default `INFO` |

`LOG_LEVEL` already exists in `logger.py`. The other four are new. Missing required variables should log one error and skip the poll, not raise a file-not-found for `config.json`.

### Correctness

| Issue | Where | Why it matters |
| --- | --- | --- |
| Scrape does all the I/O | `refresh_metrics`, called from `GET /metrics` | The function is `async` and the body is blocking telnet plus `requests`. One scrape logs in and queries every zone and LED. That can exceed the Prometheus timeout and it blocks the event loop. |
| Failed design load crashes the scrape | `design['Areas']` | A failed XML fetch returns `{}` or `Areas: None`. The next line raises. Telnet is closed, then the request is a 500. |
| Missing XML keys abort the walk | `populate_area`, `populate_zone`, `populate_device` | One device without `Outputs`, `DeviceGroups`, `Components`, `Wattage`, or `IntegrationID` fails the whole response. |
| LED pairing | `populate_component` | `button_cache.pop(0)` raises `IndexError` when a LED is not preceded by a button. |
| Login is not checked | `login` | A bad password still returns a `Telnet` object. Later parsers return zeros. |
| Parse failure looks like off | `parse_output`, `parse_device` | A bad line returns `0.0`, which is a valid level and a valid LED state. |
| Time-of-day helper | `parse_time_of_day`, `populate_time_of_day` | Any non-empty field is truthy, including `"0"`. The function is never called. `Info.info()` replaces the whole info set, so a later call would wipe firmware labels. |
| Stale series | module-level gauges | Old label sets stay after a design change until restart. `total_watts` is a label on `lutron_zone_watts`, so a wattage edit creates a new series. |
| `telnetlib` | `metrics.py` | Removed in Python 3.13. The image is 3.11. Replace it before any 3.13 base image. |
| Tests | `tests/test_database.py` | Hits a live processor. The URL has a leading space. The second test imports `refresh_metrics` from the package (`__init__.py` only exports `app`) and calls `asyncio.run` on the module. |

### Operations and dead code

- No `/health`. A probe on `/metrics` would telnet the house on every check.
- No single-flight guard. Overlapping scrapes open overlapping telnet sessions.
- No scrape duration, success, or last-success metric.
- Container runs as root. No `HEALTHCHECK`.
- Routine events log at WARNING, so `LOG_LEVEL=WARN` still floods. `logger.warn` in `app.py` is the deprecated alias.
- `CustomFormatter` never receives `datefmt`, so log lines have no timestamp.
- `database.convert_text1` duplicates `convert_text` and has no callers.
- `prometheus-fastapi-instrumentator` is unused. `pydantic` is an unused dev dependency. `starlette` is pinned beside FastAPI, which already depends on it.
- `CONTRIBUTING.md` says to run the test command `command`.
- No CI.

## Target layout

`pyproject.toml` keeps the version and the runtime dependency list. Drop `[tool.poetry-dynamic-versioning]` and the Poetry CLI from every script. Use a PEP 621 `[project]` table so the image can `pip install .` with setuptools. That install stays inside the image. Do not upload the project.

`compose.yaml` pulls and runs:

```yaml
name: lutron-qs-exporter

services:
  lutron-qs-exporter:
    image: monster-jj.jvj28.com:9092/lutron_qs_exporter:1.2.4
    container_name: lutron-qs-exporter
    restart: unless-stopped
    ports:
      - "9992:9992"
    environment:
      LUTRON_ADDRESS: "192.168.1.40"
      LUTRON_PORT: "23"
      LUTRON_USERNAME: "default"
      LUTRON_PASSWORD: "default"
      LOG_LEVEL: "INFO"
```

The `image:` tag and `pyproject.toml` `version` are the same string. Deploy with:

```powershell
docker compose pull
docker compose up -d
```

Build and push stay outside Compose. Build from source, tag with the pyproject version, push that tag and `latest`.

```dockerfile
FROM python:3.11-slim

RUN apt-get update \
    && apt-get upgrade -y \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /app
COPY pyproject.toml README.md ./
COPY asgi.py ./
COPY lutron_qs_exporter ./lutron_qs_exporter
RUN pip install --no-cache-dir .

EXPOSE 9992
CMD ["uvicorn", "asgi:app", "--host", "0.0.0.0", "--port", "9992", "--workers", "1"]
```

`uvicorn` is the server command so the image does not depend on the `fastapi` CLI package. The app version shown in logs is `importlib.metadata.version("lutron_qs_exporter")`, which pip writes from `pyproject.toml` at image build.

Label names on `lutron_zone_level` stay `area`, `area_id`, `zone`, `zone_name` so current Grafana dashboards keep working.

## Phases

### Phase 1 — Version is the image tag

Make `pyproject.toml` the only version. Remove Poetry dynamic versioning, the wheel build, and the Poetry publish path. No image yet.

### Phase 2 — Compose deployment and environment config

`config.py` reads the five variables above. `compose.yaml` pulls `monster-jj.jvj28.com:9092/lutron_qs_exporter:<version>`. Delete `config.json` from the app and from the run scripts.

### Phase 3 — Image build and push

Dockerfile installs the source with pip. One build script reads the version from `pyproject.toml`, builds `lutron_qs_exporter:<version>`, and pushes that tag plus `latest` using `NEXUS_USERNAME` and `NEXUS_PASSWORD`.

### Phase 4 — Fixes in the 1.2.4 image

Redact the password from `/config` and from debug logs. A missing design or a bad device skips that node and records a failed scrape instead of failing the whole response. Login failure is an error, not a set of zeros.

### Phase 5 — 1.3.0 behavior

Bump `version` in `pyproject.toml` to `1.3.0` and ship a new tag. Keep one telnet session per `/metrics` call. Do not add a long-lived session or a background poller. Add `/health`, rebuild gauges after a finished scrape so removed zones expire, and replace `telnetlib` before a 3.13 image. Compose is updated to the new tag in the same change.

### Phase 6 — Tests and docs

Fixture tests with no live processor. README and changelog describe the image and Compose, not Poetry, hypercorn, Postgres, or Redis.

## Implementation checklist

### Phase 1

- [x] Replace the Poetry tables with a PEP 621 `[project]` table. `version` is the image tag. Dependencies stay fastapi, prometheus-client, requests, and uvicorn. Drop `prometheus-fastapi-instrumentator`, the extra `starlette` pin, and the Poetry dev group from the runtime image. Dev test tools are an optional extra.
- [x] Remove `poetry-dynamic-versioning` and `poetry.lock`. `[build-system]` uses setuptools.
- [x] Delete `_version.py`. `app.py` reads `importlib.metadata.version("lutron_qs_exporter")`.
- [x] `version` is `1.2.4` in `pyproject.toml` and in `compose.yaml`.
- [x] `docker-run.ps1` and `docker-run.sh` are removed. Build and publish scripts stay, and they read `pyproject.toml` instead of Poetry.
- [x] Do not add a PyPI publish step.

### Phase 2

- [x] `config.py` loads `LUTRON_ADDRESS`, `LUTRON_PORT` (default `23`), `LUTRON_USERNAME`, `LUTRON_PASSWORD`, and `LOG_LEVEL` (default `INFO`).
- [x] Required variables missing: log one critical line and return an empty config. Do not look for `config.json`.
- [x] Remove the debug log of the whole config dict.
- [x] Add `compose.yaml`. `image:` tag matches the current `pyproject.toml` version (`1.1.12`). `pull_policy: always`.
- [x] Prometheus and Grafana are in that file under profile `monitoring`, with provisioned dashboards in `deploy/grafana/dashboards/`.
- [x] Remove `config.json` from the repo. Update `AGENTS.md`.
- [x] `GET /config` returns address, port, username, log level, and version. It does not return `LUTRON_PASSWORD`.

### Phase 3

- [x] Add `.dockerignore` for `.git`, `venv`, `__pycache__`, `*.pyc`, `tests`, `dist`, and `.secret`.
- [x] Dockerfile runs `pip install .` from the PEP 621 project. Apt uses `&&` and deletes the lists. Command is `uvicorn` on `0.0.0.0:9992`.
- [x] `docker-build.ps1` and `docker-build.sh` read `version` from `pyproject.toml` and tag `monster-jj.jvj28.com:9092/lutron_qs_exporter:<version>`.
- [x] `docker-publish.ps1` and `docker-publish.sh` log in with `NEXUS_USERNAME` and `NEXUS_PASSWORD` and push that tag plus `:latest`. No `.secret` file and no `version.txt`.
- [x] Local smoke test of `lutron_qs_exporter:1.1.12` on 28 Oct 2026. Logs show `v1.1.12`. `GET /config` is 200 and has no password. `GET /` is 302 to `/metrics`. `GET /metrics` is 200 from the processor at `192.168.1.40` (design version 340014, about 14 seconds). Not pushed. The registry tag is still the previous image until `docker-publish.ps1`.
- [x] Rollback steps are in the README: pin `compose.yaml` to the previous version tag. Do not roll back with `:latest`. No rollback was required for this build.

### Phase 4

- [x] `/config` omits the password. Debug logs do not dump the config dict.
- [x] If telnet login does not reach `QNET>`, or a later command does not reach `QNET>`, log the error, close the session, and do not publish samples from that scrape. `/metrics` still returns the last finished scrape. The next call opens a new session.
- [x] If design XML fails or `Areas` is missing, keep the previous design and the last good gauges. Do not raise out of `/metrics`.
- [x] A missing zone field, an empty button list, or a reply that is not `~OUTPUT` / `~DEVICE` aborts the scrape. Samples from that walk are discarded. A parser miss is not stored as level `0`.

### Phase 5

- [x] `version` is `1.3.0` in `pyproject.toml` and in `compose.yaml`.
- [x] Telnet stays one session per `/metrics` call. A failed scrape does not retry in-process and does not hold the socket open.
- [x] `GET /health` does not open telnet. The image `HEALTHCHECK` calls it.
- [x] A finished scrape clears and republishes zone and keypad series. A failed scrape does not clear them.
- [x] `lutron_zone_watts` labels match zone level. Rated watts are `lutron_zone_rated_watts`.
- [x] Telnet is `lutron_qs_exporter/telnet_client.py`. The image does not import `telnetlib`.
- [x] `lutron_scrape_duration_seconds`, `lutron_scrape_success`, and `lutron_last_success_timestamp_seconds` are exported from `/metrics`.

### Phase 6

- [x] `tests/test_exporter.py` covers XML conversion, `~OUTPUT`, `~DEVICE`, missing environment variables, and login failure. It does not call a processor.
- [x] Removed unused `convert_text1`, `parse_time_of_day`, and `populate_time_of_day`.
- [x] README covers the image build and `compose.yaml`, including the `monitoring` profile and how to scrape from an existing Prometheus. The old hypercorn, `config.json`, Postgres, and Redis instructions are gone.
- [x] Changelog entry for 1.2.4.
- [x] The `deprecated/` deletion is included in the 1.3.0 commit. `venv/` and `dist/` stay untracked.

## Done already

- [x] Code review of the Python exporter recorded here.
- [x] `deprecated/` removed from the working tree. Not committed.
- [x] `AGENTS.md` added. Update it when Phase 2 lands so it stops telling sessions to mount `config.json` or run Poetry.
