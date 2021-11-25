FROM ubuntu
ADD /files/openjdk-17.0.1_linux-x64_bin.tar.gz /opt/java
ENV JAVA_HOME=/opt/java/jdk-17.0.1
ENV PATH=$JAVA_HOME/bin:$PATH
RUN export JAVA_HOME
RUN export PATH
RUN java -version
ARG VERSION=1.0-SNAPSHOT
ARG JAR_FILE=target/homeworks-service-${VERSION}.jar
COPY ${JAR_FILE} app.jar
ENV JASYPT_ENCRYPTOR_PASSWORD=ajFFsDfj93209ajd0ad9a239da
ENTRYPOINT ["java","-jar","/app.jar"]