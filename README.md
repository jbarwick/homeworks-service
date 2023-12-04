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

### Dockerfile

```dockerfile
# Use an official Python runtime as a parent image
FROM python:3.9-slim

# Set the working directory in the container to /app
WORKDIR /app

# Add the current directory contents into the container at /app
ADD . /app

# Install any needed packages specified in requirements.txt
RUN pip install -q --no-cache-dir -r requirements.txt

# Make port 80 available to the world outside this container
EXPOSE 9992

# Define environment variable
ENV NAME FastAPIApp

# Run main.py when the container launches
CMD ["hypercorn", "asgi:app", "--bind", "0.0.0.0:9992"]
```

#### Docker Build and Run

Scripts are available in the root folder demonstrating how the docker file is built.

### Configration

Create a file called 'config.json' and mount it when you start the docker container

```json
{
  "address": "192.168.x.x",
  "port": 23,
  "username": "default",
  "password": "default",
  "log_level": "INFO"
}
```

### Docker Compose

Here's my compose file describing the build and run of the docker image

```yaml
version: '3'

services:
  lutron-qs-exporter:
    image: lutron_qs_exporter:1.1.12
    container_name: lutron-qs-exporter
    restart: unless-stopped
    ports:
      - 9992:9992
    volumes:
      - /volume1/docker/lutron/config.json:/app/config.json
```

### Prometheus

This program provides a /metrics URL for Prometheus to read and I've a sample Graphana dashboard if you like.

I've added this application to target port 9992 as registered here: https://github.com/prometheus/prometheus/wiki/Default-port-allocations

##### Example of my Prometheus config

```yaml
global:
  scrape_interval:     15s # Set the scrape interval to every 15 seconds. Default is every 1 minute.
  evaluation_interval: 15s # Evaluate rules every 15 seconds. The default is every 1 minute.
  # scrape_timeout is set to the global default (10s).

# Alertmanager configuration
alerting:
  alertmanagers:
  - static_configs:
    - targets:
      # - alertmanager:9093

rule_files:
  # - "first_rules.yml"
  # - "second_rules.yml"

scrape_configs:
  - job_name: 'prometheus'
    static_configs:
    - targets: ['localhost:9090']
  - job_name: 'lutron'
    static_configs:
    - targets: ['localhost:9992']
```

### Comments

Please enjoy and make recommendations...

This application

* polls the Homeworks system every 60 seconds for network status
* calculates system total wattage and saves that information to PostgreSQL every 60 seconds (yes, your DB will grow...there is no 'purge' logic)
* provides a json API for a Web App to consume
* provides a /metrics endpoint for Prometheus.  
* Telnets to the lutron system over time /metrics endpoint is called to retrieve values.
* Cashes and automatically updates Lutron lighting design by examining the design revision numbr.
