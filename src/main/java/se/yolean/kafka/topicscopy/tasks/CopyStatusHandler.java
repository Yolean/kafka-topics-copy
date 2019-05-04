package se.yolean.kafka.topicscopy.tasks;

public interface CopyStatusHandler {

  void polledEmpty();

  void copied(int count);

}
