FROM ubuntu
ADD /files/openjdk-17.0.1_linux-x64_bin.tar.gz /opt/java
ADD circuit_zones.csv /
ADD keypads.csv /
ENV JAVA_HOME=/opt/java/jdk-17.0.1
ENV PATH=$JAVA_HOME/bin:$PATH
RUN export JAVA_HOME
RUN export PATH
ARG VERSION
ENV APP_VERSION=${VERSION:-1.0}
ARG JAR_FILE=target/homeworks-service-${APP_VERSION}.jar
COPY ${JAR_FILE} app.jar
ENV JASYPT_ENCRYPTOR_PASSWORD=ajFFsDfj93209ajd0ad9a239da
ENTRYPOINT ["java","-jar","/app.jar"]