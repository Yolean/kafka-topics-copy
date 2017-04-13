#!/bin/bash
echo "Describing source topic"
kafka-topics --describe --topic $SOURCE_TOPIC --zookeeper $ZOOKEEPER

echo "Creating target topic if it doesn't exist yet"
kafka-topics --create --topic $TARGET_TOPIC --zookeeper $ZOOKEEPER \
             --partitions $TARGET_PARTITIONS --replication-factor $TARGET_REPLICATION_FACTOR \
             --if-not-exists

echo "Describing target topic"
kafka-topics --describe --topic $TARGET_TOPIC --zookeeper $ZOOKEEPER

# GROUP_ID_DEFAULT=topic-copy-$SOURCE_TOPIC-$TARGET_TOPIC
# GROUP_ID=${GROUP_ID:-$GROUP_ID_DEFAULT}
# echo "Using group.id: $GROUP_ID"

echo "Please make sure no process is writing to topic: $SOURCE_TOPIC"
echo "Copying topic $SOURCE_TOPIC to topic $TARGET_TOPIC ..."
kafkacat -C -b $BOOTSTRAP_SERVERS -o beginning -e -t $SOURCE_TOPIC | kafkacat -P -b $BOOTSTRAP_SERVERS -t $TARGET_TOPIC
# group id will be a future improvement when https://github.com/edenhill/kafkacat/issues/88 is resolved
# kafkacat -C -b $BOOTSTRAP_SERVERS -o beginning -e -G $GROUP_ID $SOURCE_TOPIC | kafkacat -P -b $BOOTSTRAP_SERVERS -t $TARGET_TOPIC
