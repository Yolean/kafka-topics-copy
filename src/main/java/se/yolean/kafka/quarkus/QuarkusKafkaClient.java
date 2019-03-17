package se.yolean.kafka.quarkus;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;

import io.prometheus.client.Counter;
import se.yolean.kafka.topicscopy.TopicsCopyOptions;
import se.yolean.kafka.topicscopy.TopicsCopyOptionsEnv;

@Path("/client")
public class QuarkusKafkaClient {

  static final Counter recordsCopied = Counter.build()
      .name("records_copied").help("Total records copied from source to target").register();

  public static final Duration POLL_DURATION = Duration.ofMillis(1000);
  public static final long SEND_RECORD_TIMEOUT_MILLIS = 100;

  private String status = "hello";

  private Properties consumerProps;
  private Properties producerProps;
  private Collection<String> sourceTopics;
  private String targetTopic;

  public QuarkusKafkaClient() {
    TopicsCopyOptionsEnv options = new TopicsCopyOptionsEnv();
    try {
      init(options);
    } catch (Exception e) {
      e.printStackTrace();
      status = "init failed";
    }
  }

  public void init(TopicsCopyOptions options) {

    this.consumerProps = new Properties();
    consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, options.getGroupId());
    consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getSourceBootstrap());
    consumerProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, "false");
    consumerProps.put(ConsumerConfig.ISOLATION_LEVEL_CONFIG, "read_committed");
    consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
    consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, options.getAutoOffsetReset());

    this.producerProps = new Properties();
    producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, options.getTargetBootstrap());
    producerProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");
    // https://www.baeldung.com/kafka-exactly-once
    // "All that we need to do is make sure the transaction id is distinct for each
    // producer, though consistent across restarts."
    producerProps.put(ProducerConfig.TRANSACTIONAL_ID_CONFIG, options.getGroupId() + "-tx1");
    producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);
    producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArraySerializer.class);

    status = "configured";

    sourceTopics = options.getSourceTopics();
    targetTopic = options.getTargetTopic();
  }

  /**
   * Intentionally uncached and synchronous.
   */
  private void batchFromScratch() {
    KafkaConsumer<byte[], byte[]> consumer = new KafkaConsumer<>(consumerProps);
    status = "consumer created";

    Map<String, List<PartitionInfo>> topics = consumer.listTopics(Duration.ofSeconds(5));
    status = "Topics found: " + topics.size();

    KafkaProducer<byte[], byte[]> producer = new KafkaProducer<>(producerProps);
    producer.initTransactions();

    consumer.subscribe(sourceTopics);

    ConsumerRecords<byte[], byte[]> polled = consumer.poll(POLL_DURATION);
    if (polled.count() == 0) {
      status = "Records polled: 0";
      consumer.close();
      producer.close();
      return;
    }

    try {
      producer.beginTransaction();
      List<Future<RecordMetadata>> sent = new ArrayList<>(polled.count());
      Iterator<ConsumerRecord<byte[], byte[]>> records = polled.iterator();
      while (records.hasNext()) {
        ConsumerRecord<byte[], byte[]> consumed = records.next();
        ProducerRecord<byte[], byte[]> produce = getProduce(consumed);
        sent.add(producer.send(produce));
      }
      status = polled.count() + " messages sent to " + targetTopic + ": ";
      List<RecordMetadata> metadata = new ArrayList<>(polled.count());
      for (int i = 0; i < sent.size(); i++) {
        RecordMetadata m = sent.get(i).get(SEND_RECORD_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        metadata.add(i, m);
        status += ("" + m.partition() + '-' + m.offset() + ' ');
      }

      // https://hevodata.com/blog/kafka-exactly-once/, but what do we send for the cross-cluster mirror case?
      // producer.sendOffsetsToTransaction(offsets, consumerGroupId);
      producer.commitTransaction();

      // https://www.baeldung.com/kafka-exactly-once says:
      // Conversely, applications that must read and write to different Kafka clusters
      // must use the older commitSync and commitAsync API. Typically, applications
      // will store consumer offsets into their external state storage to maintain
      // transactionality.
      consumer.commitSync();

      recordsCopied.inc(polled.count());

    } catch (ProducerFencedException e) {
      // https://hevodata.com/blog/kafka-exactly-once/ doesn't abortTransaction here
      throw new RuntimeException("Unhandled", e);
    } catch (KafkaException e) {
      producer.abortTransaction();
      status += " " + e;
      throw new RuntimeException("Unhandled", e);
    } catch (InterruptedException e) {
      producer.abortTransaction();
      status += " " + e;
      throw new RuntimeException("Unhandled", e);
    } catch (ExecutionException e) {
      producer.abortTransaction();
      status += " " + e;
      throw new RuntimeException("Unhandled", e);
    } catch (TimeoutException e) {
      producer.abortTransaction();
      status += " " + e;
      throw new RuntimeException("Unhandled", e);
    } finally {
      consumer.close();
      producer.close();
    }
  }

  private ProducerRecord<byte[], byte[]> getProduce(ConsumerRecord<byte[], byte[]> consumed) {
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

  @GET
  @Produces(MediaType.TEXT_PLAIN)
  public String hello() {
    batchFromScratch();
    return status + "\n";
  }
}
