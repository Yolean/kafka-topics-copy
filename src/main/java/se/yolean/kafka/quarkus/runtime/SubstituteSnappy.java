package se.yolean.kafka.quarkus.runtime;

import java.util.List;

import org.apache.kafka.common.metrics.JmxReporter;
import org.apache.kafka.common.metrics.KafkaMetric;
import org.apache.kafka.common.metrics.Metrics;
import org.apache.kafka.common.utils.AppInfoParser;

import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

/**
 * Remove JMX, from
 * https://github.com/quarkusio/quarkus/blob/0.12.0/extensions/kafka-client/runtime/src/main/java/io/quarkus/kafka/client/runtime/graal/SubstituteSnappy.java
 */

@TargetClass(value = AppInfoParser.class)
final class RemoveJMXAccess {

  @Substitute
  public static synchronized void registerAppInfo(String prefix, String id, Metrics metrics) {

  }

  @Substitute
  public static synchronized void unregisterAppInfo(String prefix, String id, Metrics metrics) {

  }

}

@TargetClass(value = JmxReporter.class)
final class JMXReporting {

  @Substitute
  public void init(List<KafkaMetric> metrics) {

  }

  @Substitute
  public void metricChange(KafkaMetric metric) {

  }

  @Substitute
  public void metricRemoval(KafkaMetric metric) {

  }

  @Substitute
  public void close() {
  }

}
