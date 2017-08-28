FROM solsson/kafka-persistent:0.10.1.1

# Source: ryane/kafkacat

ENV BUILD_PACKAGES "build-essential git curl zlib1g-dev python"
RUN apt-get update -y && \
    apt-get install $BUILD_PACKAGES -y && \
    git clone https://github.com/edenhill/kafkacat.git && \
    cd kafkacat && \
    ./bootstrap.sh && \
    make install && \
    cd .. && rm -rf kafkacat

WORKDIR /opt/kafka/bin/

COPY kafka-topics-copy.sh kafka-topics-copy.sh

ENTRYPOINT ["./kafka-topics-copy.sh"]
