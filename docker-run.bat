set VERSION=1.0

call mvn clean package
docker build -t jvj/homeworks-service:%VERSION% -m 2GB .
rem I can't afford a subscription
rem docker scan jvj/homeworks-service:%VERSION%
docker run --rm -ti -p 8080:8080 jvj/homeworks-service:%VERSION%
