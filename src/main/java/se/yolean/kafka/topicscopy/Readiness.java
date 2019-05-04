package se.yolean.kafka.topicscopy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;

import se.yolean.kafka.topicscopy.tasks.CopyStatusHandler;

@ApplicationScoped
@Singleton
public class Readiness implements CopyStatusHandler {

  private boolean lastPollOk = false;

  public boolean isApplicationReady() {
    return lastPollOk;
  }

  @Override
  public void polledEmpty() {
    this.lastPollOk = true;
  }

  @Override
  public void copied(int count) {
    this.lastPollOk = true;
  }

}
