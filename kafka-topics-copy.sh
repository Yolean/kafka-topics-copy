#!/bin/bash
set -e

echo "Describing source topic"
./kafka-topics.sh --describe --topic $SOURCE_TOPIC --zookeeper $ZOOKEEPER

echo "Creating target topic if not exists"
./kafka-topics.sh --create --topic $SOURCE_TOPIC --zookeeper $ZOOKEEPER \
             --partitions $TARGET_PARTITIONS --replication-factor $TARGET_REPLICATION_FACTOR \
             --if-not-exists

echo "Creating target topic (should bail if it exists)"
./kafka-topics.sh --create --topic $TARGET_TOPIC --zookeeper $ZOOKEEPER \
             --partitions $TARGET_PARTITIONS --replication-factor $TARGET_REPLICATION_FACTOR \

echo "Describing target topic"
./kafka-topics.sh --describe --topic $TARGET_TOPIC --zookeeper $ZOOKEEPER

# GROUP_ID_DEFAULT=topic-copy-$SOURCE_TOPIC-$TARGET_TOPIC
# GROUP_ID=${GROUP_ID:-$GROUP_ID_DEFAULT}
# echo "Using group.id: $GROUP_ID"

KEY_DELIMITER=${KEY_DELIMITER:-\~}
echo "Using key delimiter: $KEY_DELIMITER"
echo "Copying topic $SOURCE_TOPIC to topic $TARGET_TOPIC ..."
kafkacat -C -b $BOOTSTRAP_SERVERS -o beginning -e -t $SOURCE_TOPIC -K "$KEY_DELIMITER" | kafkacat -P -b $BOOTSTRAP_SERVERS -K "$KEY_DELIMITER" -t $TARGET_TOPIC
# group id will be a future improvement when https://github.com/edenhill/kafkacat/issues/88 is resolved
# kafkacat -C -b $BOOTSTRAP_SERVERS -o beginning -e -G $GROUP_ID $SOURCE_TOPIC | kafkacat -P -b $BOOTSTRAP_SERVERS -t $TARGET_TOPIC
