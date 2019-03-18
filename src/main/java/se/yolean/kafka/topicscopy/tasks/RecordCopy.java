package se.yolean.kafka.topicscopy.tasks;

import java.util.Iterator;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;

public class RecordCopy {

  private String targetTopic;

  public RecordCopy(String targetTopic) {
    this.targetTopic = targetTopic;
  }

  ProducerRecord<byte[], byte[]> getProduce(ConsumerRecord<byte[], byte[]> consumed) {
    ProducerRecord<byte[], byte[]> record;
    if (consumed.key() == null) {
      record = new ProducerRecord<byte[], byte[]>(targetTopic, consumed.value());
    } else {
      record = new ProducerRecord<byte[], byte[]>(targetTopic, consumed.key(), consumed.value());
    }
    Iterator<Header> headers = consumed.headers().iterator();
    while (headers.hasNext()) {
      record.headers().add(headers.next());
    }
    return record;
  }

}
