package se.yolean.kafka.quarkus;

import io.quarkus.test.junit.QuarkusTest;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class QuarkusKafkaClientTest {

    @Disabled // initialization needed with some config before this test can work again
    @Test
    public void testHelloEndpoint() {
        given()
          .when().get("/client")
          .then()
             .statusCode(200)
             .body(is("init failed\n"));
    }

}