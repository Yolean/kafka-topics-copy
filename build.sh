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

#mvn package -Pnative -Dnative-image.docker-build=true
#docker build -f src/main/docker/Dockerfile -t yolean/kafka-topics-copy:dev .
mvn package
docker build -f ./Dockerfile -t yolean/kafka-topics-copy:dev .

build-contract
docker tag yolean/kafka-topics-copy:dev yolean/kafka-topics-copy:latest
docker push yolean/kafka-topics-copy:latest
