# Homeworks Service

I am writing this GPLv3.  Let's all keep it in the community and share the code.  DO NOT steal this code.  Let's share.

As such, it's on GitHub here:  https://github.com/jbarwick/homeworks-service

### Lutron QS Prometheus Exporter

#### UPDATE As Of June 2023

Ok, the Homeworks processor is called Lutron Homeworks Processor 4.  And mine died.

So, for this prometheus exporter, I'm going to dumb down this and simplify.

Since I've upgraded from HW4 to HomeWorks QS HQP6-2 processor, the first thing I want to do is change this from Java SpringBoot to Python and simply use uvicorn to publish a FastAPI endpoint.  And, I'll document the Guages and Metrics that I'll export.

#### UPDATE December 2023

First release of the python FastAPI application.

The application will read your database from the QS processor directly.

The image tag is the `version` field in `pyproject.toml`. `compose.yaml` pulls that same tag. Change the version, build, push, and set the same tag on `services.lutron-qs-exporter.image` before you deploy.

### Build the image

The image runs `pip install .` from `pyproject.toml`. It does not use Poetry and it is not published to PyPI. The process reports that same version through `importlib.metadata`.

From this directory:

```powershell
.\docker-build.ps1
```

On a shell host:

```bash
./docker-build.sh
```

Both scripts read `version` from `pyproject.toml` and tag:

- `lutron_qs_exporter:<version>`
- `monster-jj.jvj28.com:9092/lutron_qs_exporter:<version>`
- `monster-jj.jvj28.com:9092/lutron_qs_exporter:latest`

`docker-publish.ps1` checks that `lutron_qs_exporter:<version>` exists locally, tags it, and pushes it. The password is passed to `docker login` on stdin. A failed login or push stops the script before `:latest` is moved.

The house deployment stays on the private registry. `compose.yaml` pulls `monster-jj.jvj28.com:9092/lutron_qs_exporter:<version>`.

```powershell
$env:NEXUS_USERNAME = "<nexus user>"
$env:NEXUS_PASSWORD = "<nexus password>"
.\docker-publish.ps1
```

Docker Hub is a second, optional destination. The image does not contain the processor password, so a public repository is safe. Use an access token. This does not change `compose.yaml`.

```powershell
$env:DOCKERHUB_USERNAME = "<docker hub user>"
$env:DOCKERHUB_TOKEN = "<access token>"
.\docker-publish.ps1 -Target DockerHub
.\docker-publish.ps1 -Target Both
```

`-SkipLatest` pushes only the version tag. On a shell host the same choices are `PUBLISH_TARGET=nexus|dockerhub|both` and `SKIP_LATEST=1` with `./docker-publish.sh`.

To roll back, set `services.lutron-qs-exporter.image` in `compose.yaml` to the previous version tag and run `docker compose up -d`. Do not roll back by pulling `:latest`.

The container listens on `0.0.0.0:9992` with one Uvicorn worker and without reload. `GET /health` returns immediately and does not open telnet. Docker uses that route as the container health check. `GET /` redirects to `/metrics`. `GET /metrics` telnets to the processor and walks the design, so a scrape can take a while. `GET /config` returns the connection settings and the image version. It does not return the password.

`lutron_zone_level` and `lutron_zone_watts` use the labels `area`, `area_id`, `zone`, and `zone_name`. `lutron_zone_watts` is the current wattage. `lutron_zone_rated_watts` is the rated wattage with the same labels. A finished scrape replaces those series. A failed scrape keeps the previous ones and sets `lutron_scrape_success` to 0.

### Deploy with compose.yaml

`compose.yaml` is the sample deployment. Edit the `environment` block for your processor before you start it:

| Variable | Required | Meaning |
| --- | --- | --- |
| `LUTRON_ADDRESS` | yes | Processor IP or hostname |
| `LUTRON_PORT` | no | Telnet port, default `23` |
| `LUTRON_USERNAME` | yes | Integration login |
| `LUTRON_PASSWORD` | yes | Integration password |
| `LOG_LEVEL` | no | Python log level, default `INFO` |

There is no `config.json`. A missing required variable makes the process start and log a critical error. It will not scrape until the variables are set and the container is recreated.

The exporter service is always defined. Prometheus and Grafana are in the `monitoring` profile, so they stay off when you already run them.

Exporter only, then point your existing Prometheus at `host:9992`:

```powershell
docker compose pull
docker compose up -d
```

Exporter plus the sample Prometheus (port 9090) and Grafana (port 3001):

```powershell
docker compose --profile monitoring pull
docker compose --profile monitoring up -d
```

Turn the sample stack back off without removing the exporter:

```powershell
docker compose --profile monitoring stop prometheus grafana
```

Change `GF_SECURITY_ADMIN_USER` and `GF_SECURITY_ADMIN_PASSWORD` in `compose.yaml` before you expose Grafana. The sample values are `admin` / `admin`.

With the monitoring profile, Grafana loads two dashboards from `deploy/grafana/dashboards/`:

- Lutron QS overview (`lutron_zone_level`, `lutron_zone_watts`, processor info)
- Lutron QS keypads (`lutron_device_led`)

Prometheus is configured by `deploy/prometheus/prometheus.yml`. It scrapes `lutron-qs-exporter:9992` every 60 seconds with a 55 second timeout.

### Use your own Prometheus and Grafana

Leave the monitoring profile off. Add a scrape job on the Prometheus you already run:

```yaml
scrape_configs:
  - job_name: lutron
    metrics_path: /metrics
    scrape_interval: 60s
    scrape_timeout: 55s
    static_configs:
      - targets: ["<exporter-host>:9992"]
```

In Grafana, add that Prometheus as a datasource with uid `prometheus`, or edit the dashboard JSON datasource uid after import. Then import:

- `deploy/grafana/dashboards/lutron-overview.json`
- `deploy/grafana/dashboards/lutron-keypads.json`

Port 9992 is the port registered for this exporter: https://github.com/prometheus/prometheus/wiki/Default-port-allocations

### What a scrape does

Each `GET /metrics` logs into the QS processor over telnet, reads `http://<address>/DbXmlInfo.xml` when the design version changes, and exports zone level, estimated watts, and keypad LED state. It does not write to Postgres or Redis.
