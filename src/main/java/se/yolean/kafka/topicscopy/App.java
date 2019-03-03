package se.yolean.kafka.topicscopy;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {

  private final Logger logger = LogManager.getLogger(App.class);

  public App(TopicsCopyOptions options) {
    logger.info("App started with options {}", options);

    TopicsCopyProcessor processor = new TopicsCopyProcessor(options.getSource().get(0), options.getTarget());
    logger.info("Processor created");

    Topology topology = processor.getTopology();
    logger.info("Topology created, starting Streams using {} custom props",
        options.getStreamsProperties().size());

    KafkaStreams streams = new KafkaStreams(topology, options.getStreamsProperties());
    logger.info("Streams application configured", streams);
  }

}
