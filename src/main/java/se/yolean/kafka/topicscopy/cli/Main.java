package se.yolean.kafka.topicscopy.cli;

import se.yolean.kafka.topicscopy.TopicsCopyOptions;

public class Main {

  public static void main(String[] args) {
    TopicsCopyOptions options = new OptionsFromEnv();
  }

}
