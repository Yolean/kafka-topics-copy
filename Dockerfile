FROM maven:3.6.0-jdk-8-slim@sha256:c3e480a0180ff76cfd2c4d51672ca9c050009b98eba8f9d6b9e2752c8ef2956b as maven

FROM oracle/graalvm-ce:1.0.0-rc14@sha256:ea22ec502d371af47524ceedbe6573caaa59d5143c2c122a46c8eedf40c961f0 \
  as maven-build

COPY --from=maven /usr/share/maven /usr/share/maven
RUN ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME=/usr/share/maven
ENV MAVEN_CONFIG=/root/.m2

WORKDIR /workspace
RUN mvn io.quarkus:quarkus-maven-plugin:0.12.0:create \
    -DprojectGroupId=org.acme \
    -DprojectArtifactId=getting-started \
    -DclassName="org.acme.quickstart.GreetingResource" \
    -Dpath="/hello"
COPY pom.xml .
RUN mvn package
RUN rm -r src target mvnw* && ls -l
COPY . .
RUN mvn -o package

FROM alpine:3.9@sha256:644fcb1a676b5165371437feaa922943aaf7afcfa8bfee4472f6860aad1ef2a0 as snappy-mod

RUN apk add --no-cache zip

COPY --from=maven-build /workspace/target/lib/io.quarkus.quarkus-kafka-client-runtime-*.jar /workspace/target/lib/

# https://github.com/Yolean/kafka-topics-copy/issues/4
#RUN \
#  zip -d /workspace/target/lib/io.quarkus.quarkus-kafka-client-runtime-*.jar io/quarkus/kafka/client/runtime/graal/SubstituteSnappy.class && \
#  zip -d /workspace/target/lib/io.quarkus.quarkus-kafka-client-runtime-*.jar io/quarkus/kafka/client/runtime/graal/FixEnumAccess.class

FROM oracle/graalvm-ce:1.0.0-rc14@sha256:ea22ec502d371af47524ceedbe6573caaa59d5143c2c122a46c8eedf40c961f0 \
  as native-build

WORKDIR /project
COPY --from=maven-build /workspace/target/lib ./lib
COPY --from=snappy-mod  /workspace/target/lib/* ./lib/
COPY --from=maven-build /workspace/target/*.jar ./

# from Quarkus' maven plugin mvn package -Pnative -Dnative-image.docker-build=true
# but CollectionPolicy commented out due to "Error: policy com.oracle.svm.core.genscavenge.CollectionPolicy cannot be instantiated."
RUN native-image \
  -J-Djava.util.logging.manager=org.jboss.logmanager.LogManager \
  #-H:InitialCollectionPolicy=com.oracle.svm.core.genscavenge.CollectionPolicy$BySpaceAndTime \
  -jar quarkus-kafka-1.0-SNAPSHOT-runner.jar \
  -J-Djava.util.concurrent.ForkJoinPool.common.parallelism=1 \
  -H:+PrintAnalysisCallTree \
  -H:EnableURLProtocols=http \
  -H:-SpawnIsolates \
  -H:-JNI \
  --no-server \
  -H:-UseServiceLoaderFeature \
  -H:+StackTrace

RUN ls -l ./lib/io.quarkus.quarkus-kafka-client-runtime-*.jar && sha256sum ./lib/io.quarkus.quarkus-kafka-client-runtime-*.jar

# The rest should be identical to src/main/docker/Dockerfile which is the recommended quarkus build
FROM cescoffier/native-base@sha256:407e2412c7d15ee951bfc31dcdcbbba806924350734e9b0929a95dd16c7c1b2b
WORKDIR /work/
COPY --from=native-build /project/*-runner /work/application
#RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
