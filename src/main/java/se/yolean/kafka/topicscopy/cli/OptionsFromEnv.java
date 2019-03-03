package se.yolean.kafka.topicscopy.cli;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.StreamsConfig;

import se.yolean.kafka.topicscopy.TopicsCopyOptions;

public class OptionsFromEnv implements TopicsCopyOptions {

  public static final String ENV_NAME_SOURCE_TOPICS = "SOURCE_TOPICS";
  public static final String ENV_NAME_TARGET_TOPIC = "TARGET_TOPIC";

  private final Properties streamsProperties = new Properties();

  private List<String> source;
  private String target;
  private int exitAfterIdleSeconds;

  public OptionsFromEnv() {
    Map<String, String> env = System.getenv();
    String singleSourcePattern = env.get(ENV_NAME_SOURCE_TOPICS);
    if (singleSourcePattern == null || singleSourcePattern.length() == 0) {
      throw new IllegalStateException("Missing source topics env " + ENV_NAME_SOURCE_TOPICS);
    }
    this.source = Collections.unmodifiableList(Arrays.asList(singleSourcePattern));
    this.target = env.get(ENV_NAME_TARGET_TOPIC);
    if (target == null || target.length() == 0) {
      throw new IllegalStateException("Missing target topic env " + ENV_NAME_TARGET_TOPIC);
    }

    this.streamsProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "topicscopy__" + source + "__" + target +
        "__1");
    this.streamsProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "kafka:9092");

    this.streamsProperties.put(StreamsConfig.PRODUCER_PREFIX + ProducerConfig.ACKS_CONFIG, "all");

    this.exitAfterIdleSeconds = 10;
  }

  @Override
  public List<String> getSource() {
    return source;
  }

  @Override
  public String getTarget() {
    return target;
  }

  @Override
  public Properties getStreamsProperties() {
    return streamsProperties;
  }

  @Override
  public int getExitAfterIdleSeconds() {
    return exitAfterIdleSeconds;
  }

}
