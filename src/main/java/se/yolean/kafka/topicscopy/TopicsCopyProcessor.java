package se.yolean.kafka.topicscopy;

import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TopicsCopyProcessor implements Processor<byte[], byte[]> {

  private static final Logger logger = LogManager.getLogger(TopicsCopyProcessor.class);

  private ProcessorContext context;
  private String source;
  private String target;

  private long timeOfLastProcessedMessage;

  public TopicsCopyProcessor(String source, String target) {
    this.source = source;
    this.target = target;
  }

  public Topology getTopology() {
    Topology builder = new Topology();

    builder
      .addSource("Source", source)
      .addProcessor("Process", () -> this, "Source")
      .addSink("Sink", target, "Process");

    return builder;
  }

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public void process(byte[] key, byte[] value) {
    this.timeOfLastProcessedMessage = System.currentTimeMillis();
    if (key == null) {
      logger.warn("Null key at {}-{}-{}", context.topic(), context.partition(), context.offset());
    }
    if (value == null) {
      logger.warn("Null value at {}-{}-{}[{}]", context.topic(), context.partition(), context.offset(), key);
    }
    context.forward(key, value);
  }

  @Override
  public void close() {
    // nothing to do
  }

  public long getTimeOfLastProcessedMessage() {
    return timeOfLastProcessedMessage;
  }

}
