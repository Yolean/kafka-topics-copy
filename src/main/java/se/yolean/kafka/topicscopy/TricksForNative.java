package se.yolean.kafka.topicscopy;

import java.io.IOException;

import org.apache.kafka.common.record.CompressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class TricksForNative {

  static final Logger logger = LoggerFactory.getLogger(TricksForNative.class);

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
    logger.info("Tried to enable snappy support");
  }

}
