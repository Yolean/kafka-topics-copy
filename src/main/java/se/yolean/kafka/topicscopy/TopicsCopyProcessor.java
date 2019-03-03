package se.yolean.kafka.topicscopy;

import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;

public class TopicsCopyProcessor implements Processor<byte[], byte[]> {

  public TopicsCopyProcessor(String string, String target) {
    // TODO Auto-generated constructor stub
  }

  @Override
  public void init(ProcessorContext context) {
    // TODO Auto-generated method stub

  }

  @Override
  public void process(byte[] key, byte[] value) {
    // TODO Auto-generated method stub

  }

  @Override
  public void close() {
    // TODO Auto-generated method stub

  }

  public Topology getTopology() {
    // TODO Auto-generated method stub
    return null;
  }

}
