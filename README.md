# UPDATE As Of June 2023

Ok, the Homeworks processor is called Lutron Homeworks Processor 4.  And mine died.

So, for this prometheus exporter, I'm going to dumb down this and simplify.

Since I've upgraded from HW4 to HomeWorks QS HQP6-2 processor, the first thing I want to do is change this from Java SpringBoot to Python and simply use uvicorn to publish a FastAPI endpoint.  And, I'll document the Guages and Metrics that I'll export.

SUGGESTIONS WELCOME

## COMING SOON

```dockerfile
# Use an official Python runtime as a parent image
FROM python:3.9-slim

# Set the working directory in the container to /app
WORKDIR /app

# Add the current directory contents into the container at /app
ADD . /app

# Install any needed packages specified in requirements.txt
RUN pip install --no-cache-dir -r requirements.txt

# Make port 80 available to the world outside this container
EXPOSE 80

# Define environment variable
ENV NAME FastAPIApp

# Run main.py when the container launches
CMD ["uvicorn", "asgi:app", "--host", "0.0.0.0", "--port", "80"]
```

This works very well on my bench and I get my metrics from the processor.

TODO:  figure out how to get the database. (Areas/Zones configuration).  Right now, on my test program, they are hardcoded in a json file:

```json
[
  {
    "area_id": 8,
    "area": "Area Name",
    "zones": [
      {
        "zone_id": 28,
        "name": "Zone Name",
        "watts": 200.0
      }
    ]
  }
]
```

I ran this like this:

```powershell
# Define image and container name
$ImageName = "lutron_qs_exporter"
$ContainerName = "lutron_qs_exporter_container"

Write-Output "Building Docker image: $ImageName"

# Build the Docker image
docker build -t $ImageName .

Write-Output "Image $ImageName built successfully."

# Run the Docker container using HTTP port of the defined Lutron Homeworks/QS Exporter defined on GitHub
docker run -d -p 9992:80 -v data.json:app/static/data.json --name $ContainerName $ImageName

Write-Output "Container $ContainerName running successfully."
```

And, simply get the metrics with
```
curl http://localhost:9992/metrics
```

I'll figure out credentials injection in a moment....

COME BACK SOON!!!!

# Homeworks Service

---

I am writing this GPLv3.  Let's all keep it in the community and share the code.  DO NOT steal this code.  Let's share.

As such, it's on GitHub here:  https://github.com/jbarwick/homeworks-service

Do note that inside the Docker image is "seed data" that is the Circuit names/addresses for my home system.  I have not written a generic method to have you define our own network and seed data.  This module does NOT FTP to the Lutron system to download configuration files or a map of your system.

### Notes

In the image are three files:

* circuit_zones.csv - all the circuits on your system.  name, address, room, total_watts, dimmer_type
* keypads.csv - all the keypads on your system.  name, address (stored to postgresql, but not used  yet)
* users.csv - all the user accounts that can access the api.  uuid, name, firstname, password (stored to postgresql, but not used yet)

Although keypads are not used in the program yet, the seed data is loaded.

Also, the configuration file is setup to connect to MY PostgreSQL server on my Synology NAS.

If you download this image, you will need to boot up the container and change ALL the configurations manually in application.properties

Share your ideas for the next features to add.

### Help Wanted!

I need YOUR help.  I need advise on how I would make this container more configurable.  It is up to the user of this container to set a bunch of properties, Have a REDIS and Postgresql server up and running (not included in this container).

To make this more generic and adoptable by users, should I do what other's do and build in EVERYTHING in the Container?  Let me know your recommendations.  Create the issue on GitHub https://github.com/jbarwick/homeworks-service/issues

### Prometheus

This program provides a /metrics URL for Prometheus to read and I've a sample Graphana dashboard if you like.

I've added this application to target port 9992 as registered here: https://github.com/prometheus/prometheus/wiki/Default-port-allocations

Currently, we're on 8080.  Will change soon!

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
    - targets: ['192.168.1.248:8080']

```

### Client UI's

And, I've a React web application that I'm writing to consume the /api in this program.  Not included in this docker image

### OpenAPI (Swagger)

Specifications and Documenation

    http://localhost:9992/api-docs    
    http://localhost:9992/api-docs.yaml

Working with Swagger

    http://localhost:9992/swagger-ui.html

### Comments

Please enjoy and make recommendations...

This application

* polls the Homeworks system every 60 seconds for network status
* uses an REDIS database for storage of dimmer values
* uses a PostgreSQL database to store circuits, keypads, sort ranking for User Interfaces or web apps (in dev...there are no users yet), and
* calculates system total wattage and saves that information to PostgreSQL every 60 seconds (yes, your DB will grow...there is no 'purge' logic)
* provides a json API for a Web App to consume
* provides a /metrics endpoint for Prometheus.  On Prometheous project, I said I'll put the metrics-exporter on 9992, but, I haven't created a listener here.   Just use port 8080.. web port can't be changed at the moment.  Or, this is just SpringBoot, you can modify application.properties.
* polls DIMMER values every HOUR for re-sync
* LISTENS for DIMMER value changes and updates REDIS as you change light levels
* DOES NOT interact in real-time with the Homeworks system.  API is ALWAYS read from cache. Command processor actions are queued and run asynchronously.  (option to 'wait' and make it synchronous...however, I do have a logic error/bug in the wait...can you guess what it is...I know what I did wrong...haven't had time to re-work it)

