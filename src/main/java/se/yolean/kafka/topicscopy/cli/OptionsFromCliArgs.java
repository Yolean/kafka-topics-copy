package se.yolean.kafka.topicscopy.cli;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

import joptsimple.ArgumentAcceptingOptionSpec;
import joptsimple.OptionException;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import joptsimple.OptionSpecBuilder;
import se.yolean.kafka.topicscopy.TopicsCopyOptions;

public class OptionsFromCliArgs implements TopicsCopyOptions {

  private static String usage = "This tool will copy all messages from a list of topics (or pattens?) to a target topic\n\n";

  private ArgumentAcceptingOptionSpec<String> applicationIdOption;
  private ArgumentAcceptingOptionSpec<String> bootstrapServersOption;
  private ArgumentAcceptingOptionSpec<String> inputTopicsOption;
  private OptionSet options;
  //private OptionSpecBuilder executeOption;
  //private OptionSpecBuilder dryRunOption;
  private OptionSpecBuilder     helpOption;

  private ArgumentAcceptingOptionSpec<String> outputTopicOption;

  private ArgumentAcceptingOptionSpec<Integer> exitAfterIdleSecondsOption;

  private ArgumentAcceptingOptionSpec<String> autoOffsetReset;

  public OptionsFromCliArgs(String[] args) {
    try {
      parseArguments(args);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private void parseArguments(final String[] args) throws IOException {

    final OptionParser optionParser = new OptionParser(false);

    // Try to behave similar to https://github.com/apache/kafka/blob/trunk/core/src/main/scala/kafka/tools/StreamsResetter.java

    applicationIdOption = optionParser.accepts("application-id", "The Kafka Streams application ID (application.id).")
        .withRequiredArg()
        .ofType(String.class)
        .describedAs("id")
        .required();
    bootstrapServersOption = optionParser.accepts("bootstrap-servers", "Comma-separated list of broker urls with format: HOST1:PORT1,HOST2:PORT2")
        .withRequiredArg()
        .ofType(String.class)
        .defaultsTo("bootstrap.kafka:9092")
        .describedAs("urls");
    inputTopicsOption = optionParser.accepts("input-topics", "Comma-separated list of user input topics")
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs("list")
        .required();
    outputTopicOption = optionParser.accepts("output-topic", "Output topic name")
        .withRequiredArg()
        .ofType(String.class)
        .withValuesSeparatedBy(',')
        .describedAs("list")
        .required();
    autoOffsetReset = optionParser.accepts("auto-offset-reset", "What to do if the application ID lacks an offset for any source topic")
        .withRequiredArg()
        .ofType(String.class)
        .describedAs("type")
        .defaultsTo("none");
    exitAfterIdleSecondsOption = optionParser.accepts("exit-after-idle-seconds", "Exit the application if no message has been processed within this many seconds")
        .withRequiredArg()
        .ofType(Integer.class)
        .defaultsTo(0)
        .describedAs("seconds");

    //executeOption = optionParser.accepts("execute", "Execute the command.");
    //dryRunOption = optionParser.accepts("dry-run", "Display the actions that would be performed without executing the reset commands.");
    helpOption = optionParser.accepts("help", "Print usage information.");

    try {
      options = optionParser.parse(args);
      if (args.length == 0 || options.has(helpOption)) {
          printHelp(optionParser, usage);
      }
    } catch (final OptionException e) {
      printHelp(optionParser, usage);
      throw e;
    }

    //if (options.has(executeOption) && options.has(dryRunOption)) {
    //  printHelp(optionParser, "Only one of --dry-run and --execute can be specified");
    //}

  }

  private void printHelp(final OptionParser parser, String message) throws IOException {
    if (message != null) System.err.println(message);
    parser.printHelpOn(System.err);
  }

  @Override
  public List<String> getInput() {
    return options.valuesOf(this.inputTopicsOption);
  }

  @Override
  public String getOutput() {
    return options.valueOf(this.outputTopicOption);
  }

  @Override
  public int getExitAfterIdleSeconds() {
    return options.valueOf(this.exitAfterIdleSecondsOption);
  }

  @Override
  public String getApplicationId() {
    return options.valueOf(this.applicationIdOption);
  }

  @Override
  public String getBootstrapServers() {
    return options.valueOf(bootstrapServersOption);
  }

  @Override
  public Properties getCustomProperties() {
    return new Properties();
  }

  @Override
  public String getAutoOffsetReset() {
    return options.valueOf(autoOffsetReset);
  }

}
