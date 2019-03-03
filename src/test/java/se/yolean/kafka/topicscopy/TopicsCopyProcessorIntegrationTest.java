package se.yolean.kafka.topicscopy;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.ByteArraySerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.TopologyTestDriver;
import org.apache.kafka.streams.test.ConsumerRecordFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TopicsCopyProcessorIntegrationTest {

  private TopologyTestDriver testDriver;
  private ConsumerRecordFactory<byte[], byte[]> recordFactory = new ConsumerRecordFactory<>(new ByteArraySerializer(),
      new ByteArraySerializer());

  private TopicsCopyProcessor copy;

  @BeforeEach
  public void setup() {
    copy = new TopicsCopyProcessor("source1", "target1");

    Properties config = new Properties();
    config.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "test-kafka-keyvalue");
    config.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

    Topology topology = copy.getTopology();

    testDriver = new TopologyTestDriver(topology, config);
  }

  @AfterEach
  public void tearDown() {
    testDriver.close();
  }

  @Test
  void test() {
    testDriver.pipeInput(recordFactory.create("source1", "k1".getBytes(), "v1".getBytes()));
    ByteArrayDeserializer b = new ByteArrayDeserializer();

    ProducerRecord<byte[], byte[]> copy1 = testDriver.readOutput("target1", b, b);
    assertNotNull(copy1);
    assertEquals("k1", new String(copy1.key()));
    assertEquals("v1", new String(copy1.value()));
  }

}
