package se.yolean.kafka.topicscopy;

import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.prometheus.client.Counter;
import io.quarkus.runtime.ShutdownEvent;
import io.quarkus.runtime.StartupEvent;
import se.yolean.kafka.topicscopy.tasks.CopyByPoll;
import se.yolean.kafka.topicscopy.tasks.CopyStatusHandler;
import se.yolean.kafka.topicscopy.tasks.Create;
import se.yolean.kafka.topicscopy.tasks.RecordCopy;
import se.yolean.kafka.topicscopy.tasks.Shutdown;
import se.yolean.kafka.topicscopy.tasks.Subscribe;
import se.yolean.kafka.topicscopy.tasks.TopicCheck;

@ApplicationScoped
public class TopicsCopyKafkaClient {

  static final Counter recordsCopied = Counter.build()
      .name("ktc_records_copied").help("Total records copied from source to target").register();

  static final Counter consumerPolls = Counter.build()
      .name("ktc_consumer_polls").help("Total records copied from source to target").register();

  final Logger logger = LoggerFactory.getLogger(TopicsCopyKafkaClient.class);

  public static final Duration POLL_DURATION = Duration.ofMillis(1000);

  public static final long SEND_RECORD_TIMEOUT_MILLIS = 5000;

  /**
   * For stuff like {@link Shutdown}.
   */
  public static final long MAINTENANCE_TIMEOUT_MILLIS = 20000;

  /**
   * https://quarkus.io/guides/cdi-reference "Quarkus users are encouraged not to
   * use private members"
   */
  @Inject
  TopicsCopyOptions options;

  @Inject
  Readiness readiness;

  final ExecutorService thread = Executors.newSingleThreadExecutor();

  Shutdown shutdown = null;

  PollScheduler pollScheduler;

  public TopicsCopyKafkaClient() {
  }

  void onStart(@Observes StartupEvent ev) {
    logger.info("Kafka topics copy starting with options {}", options);

    shutdown = new Shutdown();

    Create create = new Create(options, shutdown);
    schedule(create);
    awaitKafka();
    logger.info("Created consumer and producer {}", create);

    while (true) {
      Duration listTopicsTimeout = Duration.ofSeconds(3);
      TopicCheck topicCheck = new TopicCheck(create, options.getSourceTopics(), listTopicsTimeout);
      schedule(topicCheck);
      awaitKafka();
      if (topicCheck.sourceTopicsExist()) {
        logger.info("Found source topics {}", options.getSourceTopics());
        break;
      };
      // Commented out until we've figured out how to trigger app termination
      //if (i == retries) {
      //  throw new RuntimeException("Failed to find source topics " + options.getSourceTopics() + " after " + i + " retries");
      //}
      logger.debug("Retrying topic check for: " + options.getSourceTopics());
    }

    final Subscribe subscribe = new Subscribe(create, options.getSourceTopics());
    schedule(subscribe);

    if (options.getPartitionPreserve()) {
      logger.info("Note that there's no validation of source to target partition setup with {}", TopicsCopyOptionsEnv.ENV_NAME_PARTITION_PRESERVE);
    }
    RecordCopy recordCopy = new RecordCopy(options.getTargetTopic(), options.getPartitionPreserve());

    pollScheduler = new PollScheduler(subscribe, recordCopy);
    if (options.getExitAfterIdleSeconds() > 0) {
      pollScheduler.setMaxConsecutiveEmpty(options.getExitAfterIdleSeconds() / (int) POLL_DURATION.getSeconds());
    }
    pollScheduler.again();
  }

  void schedule(Runnable task) {
    thread.execute(task);
  }

  boolean awaitKafka() {
    try {
      return thread.awaitTermination(MAINTENANCE_TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
    } catch (InterruptedException e) {
      throw new RuntimeException("Got interrupted while waiting for kafka tasks", e);
    }
  }

  void onStop(@Observes ShutdownEvent ev) {
    logger.info("Got shutdown event");

    if (pollScheduler != null) {
      pollScheduler.stop();
    }
    schedule(shutdown);
    awaitKafka();
    thread.shutdown();
  }

  class PollScheduler implements CopyStatusHandler {

    boolean keepPolling = true;
    int consecutiveEmpty = 0;

    Subscribe subscribe;
    RecordCopy recordCopy;
    private int maxConsecutiveEmpty = 0;

    public PollScheduler(Subscribe subscribe, RecordCopy recordCopy) {
      this.subscribe = subscribe;
      this.recordCopy = recordCopy;
    }

    PollScheduler setMaxConsecutiveEmpty(int numberOfPolls) {
      this.maxConsecutiveEmpty  = numberOfPolls;
      return this;
    }

    void stop() {
      logger.info("Polling stop requested");
      keepPolling = false;
    }

    void again() {
      if (maxConsecutiveEmpty > 0 && consecutiveEmpty >= maxConsecutiveEmpty) {
        logger.info("Had {} empty polls in a row. Exceeds idle setting. Aborting poll.", consecutiveEmpty);
        keepPolling = false;
      }
      if (keepPolling) {
        schedule(new CopyByPoll(subscribe, recordCopy)
            .setStatusHandler(this)
            .setStatusHandler(readiness));
      } else {
        logger.info("Re-scheduling aborted");
      }
    }

    @Override
    public void polledEmpty() {
      consumerPolls.inc();
      consecutiveEmpty++;
      again();
    }

    @Override
    public void copied(int count) {
      consumerPolls.inc();
      recordsCopied.inc(count);
      consecutiveEmpty = 0;
      again();
    }

  };

}
