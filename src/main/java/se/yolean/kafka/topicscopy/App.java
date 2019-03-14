package se.yolean.kafka.topicscopy;

import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class App {

  private static final Logger logger = LogManager.getLogger(App.class);

  private ShutdownHook shutdown;

  private void updateStreamsProperties(TopicsCopyOptions options, Properties props) {

    props.put(StreamsConfig.APPLICATION_ID_CONFIG, options.getApplicationId());

    //props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, options.getBootstrapServers());
    props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19192");
    props.put(StreamsConfig.PRODUCER_PREFIX + ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:19092");

    props.put(StreamsConfig.PRODUCER_PREFIX + ProducerConfig.ACKS_CONFIG, "all");

    props.put(StreamsConfig.CONSUMER_PREFIX + ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, options.getAutoOffsetReset());

  }

  public App(TopicsCopyOptions options) {
    logger.info("App started with options {}", options);

    Properties properties = options.getCustomProperties();
    updateStreamsProperties(options, properties);

    TopicsCopyProcessor processor = new TopicsCopyProcessor(options.getInput().get(0), options.getOutput());
    logger.info("Processor created");

    Topology topology = processor.getTopology();
    logger.info("Topology created, starting Streams using {} custom props",
        properties.size());

    final KafkaStreams streams = new KafkaStreams(topology, properties);
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
