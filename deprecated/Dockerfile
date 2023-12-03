FROM ubuntu

ARG VERSION
ARG MASTER_PASSWORD
ARG JAVA_HOME=/opt/java/jdk-17.0.1

ENV CONFIG_DIR=/opt/homeworks/config
ENV JASYPT_ENCRYPTOR_PASSWORD=${MASTER_PASSWORD}

RUN export JAVA_HOME=${JAVA_HOME}
RUN export PATH=${JAVA_HOME}/bin:${PATH}
RUN export JASYPT_ENCRYPTOR_PASSWORD

RUN apt update && apt upgrade -y
ADD /files/openjdk-17.0.1_linux-x64_bin.tar.gz /opt/java

COPY /deprecated/config/circuit_zones.csv /opt/homeworks/circuit_zones.csv
COPY /deprecated/config/keypads.csv /opt/homeworks/keypads.csv
COPY /deprecated/config/users.csv /opt/homeworks/users.csv

COPY /target/homeworks-service-${VERSION}.jar /opt/homeworks/app.jar

ENTRYPOINT ["/opt/java/jdk-17.0.1/bin/java","-jar","/opt/homeworks/app.jar", "--spring.config.location=file:config/homeworks.yaml"]
