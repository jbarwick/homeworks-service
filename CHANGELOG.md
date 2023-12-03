
# Change Log
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/)
and this project adheres to [Semantic Versioning](http://semver.org/).

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