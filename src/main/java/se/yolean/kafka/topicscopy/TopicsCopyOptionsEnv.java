package se.yolean.kafka.topicscopy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class TopicsCopyOptionsEnv implements TopicsCopyOptions {

  public static final String ENV_NAME_GROUP_ID = "GROUP_ID";
  public static final String ENV_NAME_SOURCE_BOOTSTRAP = "SOURCE_BOOTSTRAP";
  public static final String ENV_NAME_SOURCE_TOPICS = "SOURCE_TOPICS";
  public static final String ENV_NAME_TARGET_BOOTSTRAP = "TARGET_BOOTSTRAP";
  public static final String ENV_NAME_TARGET_TOPIC = "TARGET_TOPIC";
  private String groupId;
  private String sourceBootstrap;
  private String targetBootstrap;
  private String targetTopic;
  private List<String> sourceTopics;
  private String autoOffsetReset;
  private int exitAfterIdleSeconds;

  public TopicsCopyOptionsEnv() {
    TopicsCopyOptionsDefaults defaults = new TopicsCopyOptionsDefaults();
    Map<String, String> env = System.getenv();
    this.groupId = env.containsKey(ENV_NAME_GROUP_ID) ? env.get(ENV_NAME_GROUP_ID) : defaults.getGroupId();
    this.sourceBootstrap = env.containsKey(ENV_NAME_SOURCE_BOOTSTRAP) ? env.get(ENV_NAME_SOURCE_BOOTSTRAP) : defaults.getSourceBootstrap();
    this.targetBootstrap = env.containsKey(ENV_NAME_TARGET_BOOTSTRAP) ? env.get(ENV_NAME_TARGET_BOOTSTRAP) : defaults.getTargetBootstrap();
    this.targetTopic = env.containsKey(ENV_NAME_TARGET_TOPIC) ? env.get(ENV_NAME_TARGET_TOPIC) : defaults.getTargetTopic();
    this.sourceTopics = env.containsKey(ENV_NAME_SOURCE_TOPICS) ? Arrays.asList(env.get(ENV_NAME_SOURCE_TOPICS)) : defaults.getSourceTopics();
    this.autoOffsetReset = defaults.getAutoOffsetReset();
    this.exitAfterIdleSeconds = defaults.getExitAfterIdleSeconds();
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
  public int getExitAfterIdleSeconds() {
    return this.exitAfterIdleSeconds;
  }

  @Override
  public String getAutoOffsetReset() {
    return this.autoOffsetReset;
  }

}
