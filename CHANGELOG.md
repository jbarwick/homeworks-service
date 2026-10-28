
# Change Log

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

## 1.3.0 - 2026-10-28

### Added

- `GET /health` reports that the process is up and does not open telnet. The image uses it as the Docker health check.
- `lutron_scrape_duration_seconds`, `lutron_scrape_success`, and `lutron_last_success_timestamp_seconds`.
- `lutron_zone_rated_watts` holds the rated wattage for a zone.

### Changed

- A finished scrape replaces zone and keypad series, so zones removed from the design expire. A failed scrape leaves the previous series in place.
- `lutron_zone_watts` uses the same labels as `lutron_zone_level`. Its value is the current wattage. Rated watts are no longer a label.
- Telnet uses a small client in this package. The image does not depend on the standard-library `telnetlib` module.
- The image still runs one Uvicorn worker, and each scrape still opens and closes its own telnet session.

## 1.2.4 - 2026-10-28

Image tag is the `version` field in `pyproject.toml`. This tag is not published to PyPI.

### Added

- `compose.yaml` deploys the image and sets `LUTRON_ADDRESS`, `LUTRON_PORT`, `LUTRON_USERNAME`, `LUTRON_PASSWORD`, and `LOG_LEVEL`.
- Prometheus and Grafana are optional through the Compose `monitoring` profile, with sample dashboards in `deploy/grafana/dashboards/`.

### Changed

- The image installs the package with `pip install .`. Poetry is no longer part of the build.
- Processor settings come from the environment. `config.json` is not read.
- `GET /config` no longer returns the password.
- Each `/metrics` call opens a telnet session and closes it. A failed login, a dropped command, or a missing design keeps the last finished gauges and waits for the next scrape.

### Removed

- The Java service under `deprecated/`.

## 1.1.12 - Python Release - 2023-12-04

This is the first Pytho release version.

## 1.0.0-alpha - 2021-11-30

An Initial version of this program.  Please be nice, I don't know how all commands
on my Lutron Homeworks system function.  I am reverse engineering a lot.  The PDF I found
about Lutron/Homeworks is still incomprehensible.  I think I'll figure it out one day.

In the meantime, if you know how to process commands, then please contribute!

### Added Features
- Initial Version
- Telnet Console
- REDIS in-memory store
- Postgres backing store
- Circuit definition in CSV file.  Copied to Postgres database
- Keypad definition in CSV file.  Copied to Postgres database
- Asynchronous monitoring jobs refresh all dimmers.  Stored in REDIS
- Statistic and other background monitors update REDIS values
- Constant NetStat queries keep telnet session alive
- Prometheus Metrics for dimmer levels and wattage
- Total Wattage written every 60 seconds to postgres DB
- Several API calls to content stored in REDIS
- Asynchronous API.  API reads data from redis
- Asynchronous Homeworks Processor Command Queue.  All commands are queued and background updated
- Option to "wait()" for processor commands to complete (found a bug though)

### Changed

- none

### Fixed

- none
