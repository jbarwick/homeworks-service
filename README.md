Homeworks Service
---------------------------------

I am writing this GPLv3.  Let's all keep it in the community and share the code.  DO NOT steal this code.  Let's share.

As such, it's on GitHub here:  https://github.com/jbarwick/homeworks-service

Do note that inside the Docker image is "seed data" that is the Circuit names/addresses for my home system.  I have not written a generic method to have you define our own network and seed data.  This module does NOT FTP to the Lutron system to download configuration files or a map of your system.

In the image are two files:

* circuit_zones.csv
* keypads.csv

Although keypads are not used in the program yet, the seed data is loaded.

Also, the configuration file is setup to connect to MY PostgreSQL server on my Synology NAS.

If you download this image, you will need to boot up the container and change ALL the configurations manually in application.properties

Share your ideas for the next features to add.

Oh.  By the way... this program provides a /metrics URL for Prometheus to read and I've a sample Graphana dashboard if you like.

And, I've a React web application that I'm writing to consume the /api in this program.

Please enjoy and make recommendations...

This application

* polls the Homeworks system every 60 seconds for network status
* uses an internal REDIS database for storage of dimmer values
* uses a postgres database to store circuits, keypads, sort ranking for User Interfaces or web apps (in dev...there are no users yet), and
* calculates system total wattage and saves that information to PostgreSQL every 60 seconds (yes, your DB will grow...there is no 'purge' logic)
* provides a json API for a Web App to consume
* provides a /metrics endpoint for Prometheus.  On Prometheous project, I said I'll put the metrics-exporter on 9007, but, I haven't created a listener here.   Just use port 8080.. web port can't be changed at the moment.  Or, this is just SpringBoot, you can modify application.properties.
* polls DIMMER values every HOUR for re-sync
* LISTENS for DIMMER value changes and updates REDIS as you change light levels
* DOES NOT interact in real-time with the Homeworks system.  API is ALWAYS read from cache. Command processor actions are queued and run asynchronously.  (option to 'wait' and make it synchronous...however, I do have a logic error/bug in the wait...can you guess what it is...I know what I did wrong...haven't had time to re-work it)
