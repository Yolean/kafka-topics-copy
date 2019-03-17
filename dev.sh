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

# In real scenarios you'll probably reset to --to-earliest
echo "# Try rerunning this script if the following fails as kafka might not have started yet on first run ..."
compose exec kafka ./bin/kafka-consumer-groups.sh --bootstrap-server=localhost:9092 --group $GROUP_ID --reset-offsets --topic topic1 --to-latest --execute

cat <<EOF

# Once the service is up and running a test scenario is for example
echo k1=v1 | kafkacat -b localhost:19092 -P -t topic1 -K '='
echo k2=v1 | kafkacat -b localhost:19092 -P -t topic1 -K '='
echo k3=v1 | kafkacat -b localhost:19092 -P -t topic1 -K '='
curl http://localhost:8080/client
kafkacat -b localhost:19192 -C -t topic2 -K '='

EOF

# colors make text invisible in OSX terminal, so do a dummy pipe
mvn compile quarkus:dev | sed 's/qwertyuio//'
