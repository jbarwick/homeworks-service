FROM ubuntu

ARG VERSION
ARG MASTER_PASSWORD

ENV JAVA_HOME=/opt/java/jdk-17.0.1
ENV PATH=$JAVA_HOME/bin:$PATH
ENV JASYPT_ENCRYPTOR_PASSWORD=${MASTER_PASSWORD}

RUN export JAVA_HOME
RUN export PATH
RUN export JASYPT_ENCRYPTOR_PASSWORD

ADD /files/openjdk-17.0.1_linux-x64_bin.tar.gz /opt/java

COPY /circuit_zones.csv /
COPY /keypads.csv /
COPY /users.csv /
COPY /target/homeworks-service-${VERSION}.jar app.jar

ENTRYPOINT ["java","-jar","/app.jar"]