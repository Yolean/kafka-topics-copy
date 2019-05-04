package se.yolean.kafka.topicscopy;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class TopicsCopyOptionsDefaultsTest {

  @Test
  void testGetTargetCompression() {
    TopicsCopyOptions defaults = new TopicsCopyOptionsDefaults();
    assertEquals("none", defaults.getTargetCompression().name);
    // gotcha
    assertEquals("NONE", defaults.getTargetCompression().name());
  }

}
