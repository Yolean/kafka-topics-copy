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
export EXIT_AFTER_IDLE_SECONDS=5

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

# Test null keys + timestamp preservation
curl http://localhost:8080/metrics
echo nokey | kafkacat -b localhost:19092 -P -t topic1
echo k4=t1 | kafkacat -b localhost:19092 -P -t topic1 -K '='
kafkacat -b localhost:19092 -C -t topic1 -o -1 -e -f '%k=%s %T\n'
sleep 1
curl http://localhost:8080/metrics
kafkacat -b localhost:19192 -C -t topic2 -o -3 -e -f '%k=%s %T\n'

# Test compression
echo compression1=gzip | kafkacat -b localhost:19092 -P -t topic1 -K '=' -z gzip
echo compression2=snappy | kafkacat -b localhost:19092 -P -t topic1 -K '=' -z snappy
sleep 1
kafkacat -b localhost:19192 -C -t topic2 -o -3 -e -f '%k=%s %T\n'
EOF

mvn compile quarkus:dev
