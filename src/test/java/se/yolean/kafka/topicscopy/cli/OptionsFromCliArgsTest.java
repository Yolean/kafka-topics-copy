package se.yolean.kafka.topicscopy.cli;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

import se.yolean.kafka.topicscopy.TopicsCopyOptions;

class OptionsFromCliArgsTest {

  @Test
  void test() {
    TopicsCopyOptions options = new OptionsFromCliArgs(
        "--bootstrap-servers kafka:9092 --application-id test --input-topics t1 --output-topic t2".split("\\s+"));
    assertEquals("kafka:9092", options.getBootstrapServers());
    assertEquals("test", options.getApplicationId());
    assertEquals(0, options.getExitAfterIdleSeconds());
    assertEquals("t1", options.getInput().get(0));
    assertEquals("t2", options.getOutput());
  }

  @Test
  void testMore() {
    TopicsCopyOptions options = new OptionsFromCliArgs(
        "--bootstrap-servers kafka:9092 --application-id test --exit-after-idle-seconds 5 --input-topics t1 --output-topic t2".split("\\s+"));
    assertEquals("kafka:9092", options.getBootstrapServers());
    assertEquals("test", options.getApplicationId());
    assertEquals(5, options.getExitAfterIdleSeconds());
  }

}
