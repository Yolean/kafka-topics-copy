package se.yolean.kafka.topicscopy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

import org.apache.kafka.common.record.CompressionType;

@Singleton
public class TopicsCopyOptionsEnv implements TopicsCopyOptions {

  public static final String ENV_NAME_GROUP_ID = "GROUP_ID";
  public static final String ENV_NAME_SOURCE_BOOTSTRAP = "SOURCE_BOOTSTRAP";
  public static final String ENV_NAME_SOURCE_TOPICS = "SOURCE_TOPICS";
  public static final String ENV_NAME_TARGET_BOOTSTRAP = "TARGET_BOOTSTRAP";
  public static final String ENV_NAME_TARGET_TOPIC = "TARGET_TOPIC";
  public static final String ENV_NAME_TARGET_COMPRESSION = "TARGET_COMPRESSION";
  public static final String ENV_NAME_AUTO_OFFSET_RESET = "AUTO_OFFSET_RESET";
  public static final String ENV_NAME_PARTITION_PRESERVE = "PARTITION_PRESERVE";
  public static final String ENV_NAME_EXIT_AFTER_IDLE_SECONDS = "EXIT_AFTER_IDLE_SECONDS";
  final String groupId;
  final String sourceBootstrap;
  final String targetBootstrap;
  final String targetTopic;
  final CompressionType targetCompression;
  final List<String> sourceTopics;
  final String autoOffsetReset;
  final int exitAfterIdleSeconds;
  final boolean partitionPreserve;

  public TopicsCopyOptionsEnv() {
    TopicsCopyOptionsDefaults defaults = new TopicsCopyOptionsDefaults();
    Map<String, String> env = System.getenv();
    this.groupId = env.containsKey(ENV_NAME_GROUP_ID) ? env.get(ENV_NAME_GROUP_ID) : defaults.getGroupId();
    this.sourceBootstrap = env.containsKey(ENV_NAME_SOURCE_BOOTSTRAP) ? env.get(ENV_NAME_SOURCE_BOOTSTRAP) : defaults.getSourceBootstrap();
    this.targetBootstrap = env.containsKey(ENV_NAME_TARGET_BOOTSTRAP) ? env.get(ENV_NAME_TARGET_BOOTSTRAP) : defaults.getTargetBootstrap();
    this.targetTopic = env.containsKey(ENV_NAME_TARGET_TOPIC) ? env.get(ENV_NAME_TARGET_TOPIC) : defaults.getTargetTopic();
    this.targetCompression = env.containsKey(ENV_NAME_TARGET_COMPRESSION) ? CompressionType.forName(env.get(ENV_NAME_TARGET_COMPRESSION)) : defaults.getTargetCompression();
    this.sourceTopics = env.containsKey(ENV_NAME_SOURCE_TOPICS) ? Arrays.asList(env.get(ENV_NAME_SOURCE_TOPICS)) : defaults.getSourceTopics();
    this.autoOffsetReset = env.containsKey(ENV_NAME_AUTO_OFFSET_RESET) ? env.get(ENV_NAME_AUTO_OFFSET_RESET) : defaults.getAutoOffsetReset();
    this.exitAfterIdleSeconds = env.containsKey(ENV_NAME_EXIT_AFTER_IDLE_SECONDS) ?
        Integer.parseInt(env.get(ENV_NAME_EXIT_AFTER_IDLE_SECONDS)) : defaults.getExitAfterIdleSeconds();
    this.partitionPreserve = env.containsKey(ENV_NAME_PARTITION_PRESERVE) ? Boolean.parseBoolean(env.get(ENV_NAME_PARTITION_PRESERVE)) : defaults.getPartitionPreserve();
  }

  @Override
  public String getGroupId() {
    return this.groupId;
  }

  @Override
  public String getSourceBootstrap() {
    return this.sourceBootstrap;
  }

  @Override
  public List<String> getSourceTopics() {
    return this.sourceTopics;
  }

  @Override
  public String getTargetBootstrap() {
    return this.targetBootstrap;
  }

  @Override
  public String getTargetTopic() {
    return this.targetTopic;
  }

  @Override
  public CompressionType getTargetCompression() {
    return this.targetCompression;
  }

  @Override
  public int getExitAfterIdleSeconds() {
    return this.exitAfterIdleSeconds;
  }

  @Override
  public String getAutoOffsetReset() {
    return this.autoOffsetReset;
  }

  @Override
  public boolean getPartitionPreserve() {
    return this.partitionPreserve;
  }

}
