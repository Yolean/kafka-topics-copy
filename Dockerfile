FROM confluentinc/cp-kafka:3.2.0

# Source: ryane/kafkacat

ENV BUILD_PACKAGES "build-essential git curl zlib1g-dev python"
RUN apt-get update -y && \
    apt-get install $BUILD_PACKAGES -y && \
    git clone https://github.com/edenhill/kafkacat.git && \
    cd kafkacat && \
    ./bootstrap.sh && \
    make install && \
    cd .. && rm -rf kafkacat

COPY kafka-topics-copy.sh /scripts/kafka-topics-copy.sh

CMD ["/scripts/kafka-topics-copy.sh"]
