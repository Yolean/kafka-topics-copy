package se.yolean.kafka.topicscopy.tasks;

import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;

public class Shutdown implements Runnable {

  KafkaConsumer<? extends Object, ? extends Object> consumer = null;
  KafkaProducer<? extends Object, ? extends Object> producer = null;

  void setConsumer(KafkaConsumer<? extends Object, ? extends Object> consumer) {
    this.consumer = consumer;
  }

  void setProducer(KafkaProducer<? extends Object, ? extends Object> producer) {
    this.producer = producer;
  }

  @Override
  public void run() {
    if (consumer != null) {
      consumer.close();
    }
    if (producer != null) {
      producer.close();
    }
  }

}
