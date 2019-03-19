package se.yolean.kafka.topicscopy.tasks;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import se.yolean.kafka.topicscopy.TopicsCopyOptions;

public class Create implements Runnable {

  KafkaConsumer<byte[], byte[]> consumer;

  KafkaProducer<byte[], byte[]> producer;

  TopicsCopyOptions options;

  Shutdown shutdown;

  public Create(TopicsCopyOptions options, Shutdown shutdown) {
    this.options = options;
    this.shutdown = shutdown;
  }

  @Override
  public void run() {
    Properties consumerProps = new Properties();
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, options.getGroupId());
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getSourceBootstrap());
    consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, options.getAutoOffsetReset());

    createConsumer(consumerProps);

    Properties producerProps = new Properties();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getTargetBootstrap());
    producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    // https://www.baeldung.com/kafka-exactly-once
    // "All that we need to do is make sure the transaction id is distinct for each
    // producer, though consistent across restarts."
    producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, options.getGroupId() + "-tx1");
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
    producerProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, options.getTargetCompression());

    createProducer(producerProps);
  }

  void createProducer(Properties producerProps) {
    producer = new KafkaProducer<>(producerProps);
    shutdown.setProducer(producer);
  }

  void createConsumer(Properties consumerProps) {
    consumer = new KafkaConsumer<>(consumerProps);
    shutdown.setConsumer(consumer);
  }

  public KafkaConsumer<byte[], byte[]> getConsumer() {
    return consumer;
  }

  public KafkaProducer<byte[], byte[]> getProducer() {
    return producer;
  }

}
