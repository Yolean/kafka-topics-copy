#!/bin/bash
# Until we've dockerized build

set -e

export GRAALVM_HOME=/opt/graalvm-ce-1.0.0-rc13/Contents/Home/
export PATH=$GRAALVM_HOME/bin:$PATH:/opt/apache-maven-3.6.0/bin
export JAVA_HOME=$GRAALVM_HOME

# Verify dev environment with graalvm
#java -version
#mvn test

# Local use
#mvn package -Pnative

# x64
mvn dependency:tree
mvn test
mvn package -Pnative -Dnative-image.docker-build=true -Dmaven.test.skip=true
docker build -f src/main/docker/Dockerfile -t yolean/quarkus-kafka .

docker run --rm -p 8080:8080 --name quarkus-kafka-test -d yolean/quarkus-kafka
curl http://localhost:8080/client?n=[1-10]
docker logs quarkus-kafka-test
docker kill quarkus-kafka-test
