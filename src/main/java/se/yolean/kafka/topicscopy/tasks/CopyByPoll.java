package se.yolean.kafka.topicscopy.tasks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.ProducerFencedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CopyByPoll implements Runnable {

  public static final long DEFAULT_POLL_DURATION_MILLIS = 1000;
  public static final long DEFAULT_PRODUCE_WAIT_TIMEOUT_MILLIS = 5000;

  KafkaConsumer<byte[], byte[]> consumer;

  KafkaProducer<byte[], byte[]> producer;

  Collection<CopyStatusHandler> statusHandlers = new LinkedList<CopyStatusHandler>();

  RecordCopy recordCopy;

  Duration duration = Duration.ofMillis(DEFAULT_POLL_DURATION_MILLIS);

  long produceWaitTimeout = DEFAULT_PRODUCE_WAIT_TIMEOUT_MILLIS;

  public CopyByPoll(Subscribe subscribe, RecordCopy recordCopy) {
    this.consumer = subscribe.getCreated().getConsumer();
    this.producer = subscribe.getCreated().getProducer();
    this.recordCopy = recordCopy;
  }

  public CopyByPoll setStatusHandler(CopyStatusHandler handler) {
    statusHandlers.add(handler);
    return this;
  }

  public CopyByPoll setPollDuration(Duration duration) {
    this.duration = duration;
    return this;
  }

  public CopyByPoll setProduceWaitTimeout(long millis) {
    this.produceWaitTimeout = millis;
    return this;
  }

  @Override
  public void run() {
    ConsumerRecords<byte[], byte[]> polled = consumer.poll(duration);

    final int count = polled.count();

    if (count == 0) {
      statusHandlers.forEach(h -> h.polledEmpty());
      return;
    }

    try {
      // producer.beginTransaction();

      List<Future<RecordMetadata>> sent = new ArrayList<>(count);
      Iterator<ConsumerRecord<byte[], byte[]>> records = polled.iterator();
      while (records.hasNext()) {
        ConsumerRecord<byte[], byte[]> consumed = records.next();
        ProducerRecord<byte[], byte[]> produce = recordCopy.getProduce(consumed);
        sent.add(producer.send(produce));
      }

      List<RecordMetadata> metadata = new ArrayList<>(count);
      for (int i = 0; i < sent.size(); i++) {
        RecordMetadata m = sent.get(i).get(produceWaitTimeout, TimeUnit.MILLISECONDS);
        metadata.add(i, m);
      }

      // producer.commitTransaction();

      consumer.commitSync();

      statusHandlers.forEach(h -> h.copied(count));

    } catch (ProducerFencedException e) {
      throw new RuntimeException("Unhandled", e);
    } catch (KafkaException e) {
      //producer.abortTransaction();
      throw new RuntimeException("Unhandled", e);
    } catch (InterruptedException e) {
      //producer.abortTransaction();
      throw new RuntimeException("Unhandled", e);
    } catch (ExecutionException e) {
      //producer.abortTransaction();
      throw new RuntimeException("Unhandled", e);
    } catch (TimeoutException e) {
      //producer.abortTransaction();
      throw new RuntimeException("Unhandled", e);
    }
  }

}
