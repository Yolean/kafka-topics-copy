package se.yolean.kafka.topicscopy;

import java.util.List;

public interface TopicsCopyOptions {

  String getGroupId();

  /**
   * @return bootstrap servers string
   */
  String getSourceBootstrap();

  /**
   * @return topic names and/or patterns
   */
  List<String> getSourceTopics();

  /**
   * @return bootstrap servers string
   */
  String getTargetBootstrap();

  /**
   * @return topic name
   */
  String getTargetTopic();

  int getExitAfterIdleSeconds();

  String getAutoOffsetReset();

  /**
   * @return true to copy to the same partition number that the source message came from
   */
  boolean getPartitionPreserve();

}
