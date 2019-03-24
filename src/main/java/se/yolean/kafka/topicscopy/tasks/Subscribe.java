package se.yolean.kafka.topicscopy.tasks;

import java.util.Collection;

public class Subscribe implements Runnable {

  private Create created;
  private Collection<String> sourceTopics;

  public Subscribe(Create created, Collection<String> sourceTopics) {
    this.created = created;
    this.sourceTopics = sourceTopics;
  }

  public Create getCreated() {
    return this.created;
  }

  @Override
  public void run() {
    //this.created.getProducer().initTransactions();

    this.created.getConsumer().subscribe(sourceTopics);
  }

}
