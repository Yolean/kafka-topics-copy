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
docker build -f src/main/docker/Dockerfile -t yolean/quarkus-kafka:dev .

compose='docker-compose -f ./build-contracts/docker-compose.yml'

$compose up -d kafka
sleep 5
$compose up -d topic1-create
$compose up -d quarkus-kafka
curl http://localhost:8080/client?n=[1-3]
sleep 3
curl http://localhost:8080/client?n=[1-3]
$compose logs quarkus-kafka
$compose down
