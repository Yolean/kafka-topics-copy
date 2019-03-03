package se.yolean.kafka.topicscopy;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {

  private static final Logger logger = LogManager.getLogger(App.class);

  private ShutdownHook shutdown;

  public App(TopicsCopyOptions options) {
    logger.info("App started with options {}", options);

    TopicsCopyProcessor processor = new TopicsCopyProcessor(options.getSource().get(0), options.getTarget());
    logger.info("Processor created");

    Topology topology = processor.getTopology();
    logger.info("Topology created, starting Streams using {} custom props",
        options.getStreamsProperties().size());

    final KafkaStreams streams = new KafkaStreams(topology, options.getStreamsProperties());
    logger.info("Streams application configured", streams);

    streams.start();
    logger.info("Streams application started");

    this.shutdown = new ShutdownHook(streams);

    Runtime.getRuntime().addShutdownHook(new Thread(shutdown));

    if (options.getExitAfterIdleSeconds() > 0) {
      while (true) {
        try {
          Thread.sleep(1000L * options.getExitAfterIdleSeconds());
        } catch (InterruptedException e) {
          throw new RuntimeException("Interrupted while polling for idle", e);
        }
        long last = processor.getTimeOfLastProcessedMessage();
        if (last == 0) {
          logger.info("No messages processed yet");
        } else if (System.currentTimeMillis() - last > 1000L * options.getExitAfterIdleSeconds()) {
          logger.info("Idle for more than {} seconds. Exiting.", options.getExitAfterIdleSeconds());
          // with only the shutdown hook and System.exit we'd see logs until PENDING_SHUTDOWN but with this wait we see NOT_RUNNING and "Streams client stopped completely"
          streams.close();
          try {
            Thread.sleep(1);
          } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while waiting for streams close", e);
          }
          System.exit(0);
        }
      }
    }
  }

  private class ShutdownHook implements Runnable {

    private KafkaStreams streams;

    ShutdownHook(KafkaStreams streams) {
      this.streams = streams;
    }

    @Override
    public void run() {
      streams.close();
    }

  }

}
