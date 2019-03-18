package se.yolean.kafka.topicscopy;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Singleton;

@ApplicationScoped
@Singleton
public class Readiness {

  public boolean isApplicationReady() {
    return false;
  }

}
