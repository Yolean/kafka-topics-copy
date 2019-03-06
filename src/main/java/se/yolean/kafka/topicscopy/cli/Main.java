package se.yolean.kafka.topicscopy.cli;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import se.yolean.kafka.topicscopy.App;
import se.yolean.kafka.topicscopy.TopicsCopyOptions;

public class Main {

  private static final Logger logger = LogManager.getLogger(Main.class);

  public static void main(String[] args) {
    TopicsCopyOptions options = new OptionsFromCliArgs(args);

    App app = new App(options);

    logger.info("App running: {}", app);
  }

}
