# Kafka Topics Copy

Copy a Kafka Topic within the same Kafka Cluster. Useful for reducing the number of partitions for a topic.
It keeps the keys of records, therefore ordering for a specific key is still guaranteed, even though the number of partitions is different.

# Running

```
docker run --net=host \
  -e SOURCE_TOPIC=source-topic \
  -e TARGET_TOPIC=target-topic \
  -e TARGET_PARTITIONS=3 \
  -e TARGET_REPLICATION_FACTOR=1 \
  -e ZOOKEEPER=localhost:2181 \
  -e BOOTSTRAP_SERVERS=localhost:9092 \
  -e KEY_DELIMITER="~" \
  simplesteph/kafka-topics-copy
```

# Roadmap / Improvements

 - Use group.id to track the replication (blocked by https://github.com/edenhill/kafkacat/issues/88 as -G option is not compatible with -e option)
 - Copy topic configuration as well.
 - Make it compatible with secured kafka cluster
