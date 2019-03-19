package se.yolean.kafka.topicscopy;

import java.io.IOException;

import org.apache.kafka.common.record.CompressionType;

public abstract class TricksForNative {

  /**
   * https://github.com/Yolean/kafka-topics-copy/issues/4
   * https://github.com/apache/kafka/blob/2.1.1/clients/src/main/java/org/apache/kafka/common/record/CompressionType.java#L210
   */
  static void nativeSupportCompressionTypesSnappy() {
    CompressionType.forId(2);
    try {
      new org.xerial.snappy.SnappyInputStream(new java.io.ByteArrayInputStream(new byte[0])).close();
    } catch (IOException e) {
    }
    try {
      new org.xerial.snappy.SnappyOutputStream(new java.io.ByteArrayOutputStream()).close();
    } catch (IOException e) {
    }
  }

}
