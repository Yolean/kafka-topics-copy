FROM maven:3.6.0-jdk-8-slim@sha256:c3e480a0180ff76cfd2c4d51672ca9c050009b98eba8f9d6b9e2752c8ef2956b as maven

FROM oracle/graalvm-ce:1.0.0-rc14@sha256:ea22ec502d371af47524ceedbe6573caaa59d5143c2c122a46c8eedf40c961f0 \
  as native-build

COPY --from=maven /usr/share/maven /usr/share/maven
RUN ln -s /usr/share/maven/bin/mvn /usr/bin/mvn
ENV MAVEN_HOME=/usr/share/maven
ENV MAVEN_CONFIG=/root/.m2

WORKDIR /workspace
COPY pom.xml .
RUN mvn package || echo "OK, just caching dependencies"
COPY . .
RUN mvn package

WORKDIR /project
COPY target .

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

# The rest should be identical to src/main/docker/Dockerfile which is the recommended quarkus build
FROM cescoffier/native-base@sha256:407e2412c7d15ee951bfc31dcdcbbba806924350734e9b0929a95dd16c7c1b2b
WORKDIR /work/
COPY --from=native-build /project/*-runner /work/application
#RUN chmod 775 /work
EXPOSE 8080
CMD ["./application", "-Dquarkus.http.host=0.0.0.0"]
