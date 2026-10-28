# Homeworks Service

Python package `lutron_qs_exporter`: a FastAPI Prometheus exporter for a Lutron HomeWorks QS processor. GPL-3.0. It ships as a Docker image only. Do not publish it to PyPI. Do not use Poetry.

The old Java app was removed from `deprecated/`. Do not restore it. Git history still has it.

The work list is `IMPROVEMENTS_PLAN.md`. Follow that phase order.

## Layout

- `asgi.py` re-exports the FastAPI app as `app`.
- `lutron_qs_exporter/app.py` owns `/`, `/metrics`, `/health`, `/data`, and `/config`. HTTP port is **9992**. `/health` does not open telnet. The image tag is **1.3.0** until the next version bump.
- `metrics.py` logs into the processor with `telnet_client.py` and walks the design. Do not import `telnetlib`.
- `database.py` fetches `http://<address>/DbXmlInfo.xml`.
- `config.py` reads environment variables: `LUTRON_ADDRESS`, `LUTRON_PORT` (default 23), `LUTRON_USERNAME`, `LUTRON_PASSWORD`, `LOG_LEVEL` (default INFO). Do not add a config file.
- Image tag is the `version` field in `pyproject.toml`. `compose.yaml` pulls `monster-jj.jvj28.com:9092/lutron_qs_exporter:<version>`. It does not build the image. Do not point compose at Docker Hub unless the user asks. `docker-publish.ps1` defaults to Nexus. `-Target DockerHub` or `-Target Both` also pushes `docker.io/<DOCKERHUB_USERNAME>/lutron_qs_exporter:<version>` using `DOCKERHUB_TOKEN`.
- Prometheus and Grafana are in `compose.yaml` under the `monitoring` profile. They are off unless the user passes `--profile monitoring`. Sample dashboards live in `deploy/grafana/dashboards/`. Do not start that profile when the user already runs Prometheus and Grafana.
- Do not print, log, or return the processor password. `/config` omits it.

## Runtime

- Local dev: `pip install -e ".[dev]"` then `uvicorn --reload asgi:app --port 9992` with the `LUTRON_*` variables set. The reported version comes from the installed package metadata. Build the image with `docker-build.ps1`, which reads `version` from `pyproject.toml`.
- `/metrics` opens a telnet session and walks the whole design on every scrape, then closes it. Do not keep a session across requests. If login or a command fails, log it, leave the last good gauges in place, and let the next scrape try again.
- Style: snake_case, 120-column lines (`.flake8`, `.pylintrc`). Use the logger in `logger.py`.
- New tests must mock telnet and XML. Do not hit `192.168.1.40` from tests.
- Do not commit `venv/`, `dist/`, `.secret`, or a non-default processor password.

## Docs

- Phases and checklist: `IMPROVEMENTS_PLAN.md`
- Protocol guide: Lutron Integration Protocol revision AB (22 August 2019), file on `H:\Documents\Lutron\` named `C__ProgramData_Lutron_Lutron Designer_Lutron Designer 15.13.1_Lutron Designer__IntegrationInfo.pdf`. Do not copy that PDF or a transcript of it into the repo. Project notes are `docs/lutron-integration.md`. HomeWorks QS starts at printed page 46.
