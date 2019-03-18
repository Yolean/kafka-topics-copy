# KTC

Copy/mirror kafka topics, as a microservice.
Within a cluster or between clusters.
Preserves message timestamps and headers.
Optionally preserves partition.

Unlike [MirrorMaker](https://cwiki.apache.org/confluence/pages/viewpage.action?pageId=27846330)
and [MirrorMaker 2](https://cwiki.apache.org/confluence/display/KAFKA/KIP-382%3A+MirrorMaker+2.0)
this service is designed to be lightweight and runnable as a Kubernetes Job.

There's two major experiments here however:

### Experiment 1: Using Quarkus for native executables

We mean to mirror hundreds of topics using this service,
so keeping the resource footprint low is worth some effort.
Here we build a native executable from java source and use a distroless docker base for the image.

Also we were curious if [Quarkus](https://quarkus.io/) could provide a pattern for our backend services.

When the service has started and HTTP endpoints are up the service takes *around 3 MB memory*.
That grows with kafka use though.

### Experiment 2: Kafka exactly-once semantics

The framework should be stable, but the [client code](src/main/java/se/yolean/kafka/topicscopy/tasks/CopyByPoll.java) here needs review, in particular WRT error handling.

# Running

See the `copy1` service in [docker-compose.yml](./build-contracts/docker-compose.yml) for an example.

The only real documentation of config options is [where we read env](src/main/java/se/yolean/kafka/topicscopy/TopicsCopyOptionsEnv.java)
and in the [Options interface](./src/main/java/se/yolean/kafka/topicscopy/TopicsCopyOptions.java).

# Building

See [build.sh](./build.sh).

# Development

See [dev.sh](./dev.sh). Wanted: hot-reload during development.
