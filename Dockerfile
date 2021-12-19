FROM ubuntu

ARG VERSION
ARG MASTER_PASSWORD

ENV JASYPT_ENCRYPTOR_PASSWORD=${MASTER_PASSWORD}

RUN export JAVA_HOME
RUN export PATH
RUN export JASYPT_ENCRYPTOR_PASSWORD

RUN apt update && apt upgrade -y
RUN apt install openjdk-17-jre

COPY /circuit_zones.csv /var/lib/homeworks/circuit_zones.csv
COPY /keypads.csv /var/lib/homeworks/keypads.csv
COPY /users.csv /var/lib/homeworks/users.csv

COPY /target/homeworks-service-${VERSION}.jar /opt/homeworks/app.jar

ENTRYPOINT ["java","-jar","/opt/homeworks/app.jar"]
