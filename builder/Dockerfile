FROM gradle:5.2.1-jdk8-slim as gradle

FROM oracle/graalvm-ce:1.0.0-rc13@sha256:9531f8ea96ecdf6ed51fc34932fb2134d37a3ef5f9ff3db3c1ffc0ef4ca94e43

COPY --from=gradle /opt/gradle /opt/gradle

RUN ln -s /opt/gradle/bin/gradle /usr/bin/gradle \
  && gradle -v

VOLUME /workspace
WORKDIR /workspace
ENTRYPOINT [ "gradle" ]
CMD [ "--no-daemon", "build" ]
