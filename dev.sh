#!/bin/bash

set -e

export GRAALVM_HOME=/opt/graalvm-ce-1.0.0-rc13/Contents/Home/
export PATH=$GRAALVM_HOME/bin:$PATH:/opt/apache-maven-3.6.0/bin

#mvn eclipse:eclipse

function compose {
  docker-compose -f ./build-contracts/docker-compose.yml $@
}

compose up -d kafka destination-kafka topic1-create destination-topic2-create

export GROUP_ID=copy1dev
export SOURCE_BOOTSTRAP=localhost:19092
export SOURCE_TOPICS=topic1
export TARGET_BOOTSTRAP=localhost:19192
export TARGET_TOPIC=topic2
export AUTO_OFFSET_RESET=latest

cat <<EOF

# Once the service is up and running a test scenario is for example
curl http://localhost:8080/healthz
echo k1=v1 | kafkacat -b localhost:19092 -P -t topic1 -K '='
echo k2=v1 | kafkacat -b localhost:19092 -P -t topic1 -K '='
echo k3=v1 | kafkacat -b localhost:19092 -P -t topic1 -K '='
curl http://localhost:8080/metrics
sleep 1
curl http://localhost:8080/metrics
kafkacat -b localhost:19192 -C -t topic2 -K '=' -e

EOF

mvn compile quarkus:dev
