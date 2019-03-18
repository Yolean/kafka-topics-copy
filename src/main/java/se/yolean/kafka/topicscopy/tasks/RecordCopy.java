package se.yolean.kafka.topicscopy.tasks;

import java.util.Iterator;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

public class RecordCopy {

  private String targetTopic;
  private boolean preservePartition;

  public RecordCopy(String targetTopic, boolean preservePartition) {
    this.targetTopic = targetTopic;
    this.preservePartition = preservePartition;
  }

  ProducerRecord<byte[], byte[]> getProduce(ConsumerRecord<byte[], byte[]> consumed) {
    Long timestamp = consumed.timestamp();
    Integer partition = preservePartition ? consumed.partition() : null;
    Headers headers = consumed.headers();
    return new ProducerRecord<byte[], byte[]>(targetTopic, partition, timestamp, consumed.key(), consumed.value(), headers);
  }

}
