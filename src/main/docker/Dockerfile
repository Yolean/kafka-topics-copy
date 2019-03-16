####
# Before building the docker image run:
#
# mvn package -Pnative -Dnative-image.docker-build=true
#
# Then, build the image with:
#
# docker build -f src/main/docker/Dockerfile -t quarkus/quarkus-kafka .
#
# Then run the container using:
#
# docker run -i --rm -p 8080:8080 quarkus/quarkus-kafka
#
###
FROM registry.fedoraproject.org/fedora-minimal@sha256:28dcdc19fd1d55598dc308a44c40287f4b00d0bf5a53cd01c39368c16cf85d57
WORKDIR /work/
COPY target/*-runner /work/application
RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]