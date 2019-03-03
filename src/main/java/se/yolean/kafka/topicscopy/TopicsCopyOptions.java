package se.yolean.kafka.topicscopy;

import java.util.List;
import java.util.Properties;

public interface TopicsCopyOptions {

  List<String> getSource();

  String getTarget();

  Properties getStreamsProperties();

}
