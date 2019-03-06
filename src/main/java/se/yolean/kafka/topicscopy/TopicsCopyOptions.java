package se.yolean.kafka.topicscopy;

import java.util.List;
import java.util.Properties;

public interface TopicsCopyOptions {

  String getApplicationId();

  String getBootstrapServers();

  List<String> getInput();

  String getOutput();

  Properties getCustomProperties();

  int getExitAfterIdleSeconds();

  String getAutoOffsetReset();

}
