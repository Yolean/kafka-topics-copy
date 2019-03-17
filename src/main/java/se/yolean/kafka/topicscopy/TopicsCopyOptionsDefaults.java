package se.yolean.kafka.topicscopy;

import java.util.List;

public class TopicsCopyOptionsDefaults implements TopicsCopyOptions {

  private static final String DEFAULT_AUTO_OFFSET_RESET = "none";

  @Override
  public String getGroupId() {
    throw new RuntimeException("Required: " + TopicsCopyOptionsEnv.ENV_NAME_GROUP_ID);
  }

  @Override
  public String getSourceBootstrap() {
    throw new RuntimeException("Required: " + TopicsCopyOptionsEnv.ENV_NAME_SOURCE_BOOTSTRAP);
  }

  @Override
  public List<String> getSourceTopics() {
    throw new RuntimeException("Required: " + TopicsCopyOptionsEnv.ENV_NAME_SOURCE_TOPICS);
  }

  @Override
  public String getTargetBootstrap() {
    throw new RuntimeException("Required: " + TopicsCopyOptionsEnv.ENV_NAME_TARGET_BOOTSTRAP);
  }

  @Override
  public String getTargetTopic() {
    throw new RuntimeException("Required: " + TopicsCopyOptionsEnv.ENV_NAME_TARGET_TOPIC);
  }

  @Override
  public int getExitAfterIdleSeconds() {
    return 0;
  }

  @Override
  public String getAutoOffsetReset() {
    return DEFAULT_AUTO_OFFSET_RESET;
  }

}
