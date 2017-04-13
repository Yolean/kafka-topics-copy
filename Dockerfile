FROM debian:latest

# Source: ryane/kafkacat
WORKDIR /build

ENV BUILD_PACKAGES "build-essential git curl zlib1g-dev python"
RUN apt-get update -y && \
    apt-get install $BUILD_PACKAGES -y && \
    git clone https://github.com/edenhill/kafkacat.git && \
    cd kafkacat && \
    ./bootstrap.sh && \
    make install && \
    cd .. && rm -rf kafkacat && \
    AUTO_ADDED_PACKAGES=`apt-mark showauto` && \
    apt-get remove --purge -y $BUILD_PACKAGES $AUTO_ADDED_PACKAGES && \
    rm -rf /var/lib/apt/lists/* /tmp/* /var/tmp/*

COPY kafka-topics-copy.sh /scripts/kafka-topics-copy.sh

CMD ["/scripts/kafka-topics-copy.sh"]
