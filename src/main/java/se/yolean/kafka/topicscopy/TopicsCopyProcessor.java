package se.yolean.kafka.topicscopy;

import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

public class TopicsCopyProcessor implements Processor<byte[], byte[]> {

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
    try {
      Thread.sleep(1000); // DUMMY wait to test timestamp handling
    } catch (InterruptedException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    this.context.forward(key, value);
  }

  @Override
  public void close() {
    // nothing to do
  }

  public long getTimeOfLastProcessedMessage() {
    return timeOfLastProcessedMessage;
  }

}
