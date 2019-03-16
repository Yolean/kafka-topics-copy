package se.yolean.kafka.quarkus;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.PartitionInfo;

@Path("/client")
public class QuarkusKafkaClient {

	private String status = "hello";

	private ExecutorService executor = Executors.newSingleThreadExecutor();

	public QuarkusKafkaClient() {
		try {
			init();
		} catch (Exception e) {
			status = "init failed";
		}
	}

	public void init() {
		status = "initializing";
		Properties props = new Properties();
		props.put("bootstrap.servers", "kafka:19092");
		props.put("group.id", "test");
		props.put("enable.auto.commit", "true");
		props.put("auto.commit.interval.ms", "1000");
		props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
		KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
		status = "consumer created";
		executor.execute(new Runnable() {
			@Override
			public void run() {
				Map<String, List<PartitionInfo>> topics = consumer.listTopics(Duration.ofSeconds(5));
				if (topics == null) {
					status = "topic listing failed";
				}
				status = "topics: " + topics.keySet();
			}
		});
	}

	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
		return status + "\n";
	}
}
