package se.yolean.kafka.topicscopy;

import javax.enterprise.context.ApplicationScoped;

import org.eclipse.microprofile.config.inject.ConfigProperty;

@ApplicationScoped
public class TopicsCopyOptionsMicroProfileConfig {

  @ConfigProperty(name = "greeting.message", defaultValue = "Hej!")
  String message;

  //@ConfigProperty(name = "ktc_transactional")
  //Boolean transactional;

}
