package io.hifly.kafka.console;

import io.hifly.kafka.console.service.KafkaAdminService;
import io.hifly.kafka.console.service.KafkaAdminServiceImpl;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.codec.BodyCodec;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

//TODO add consumer groups: list and describe test methods
@RunWith(VertxUnitRunner.class)
public class RestVerticleTest extends KafkaClusterTestBase {

    private static final Logger log = LoggerFactory.getLogger(RestVerticleTest.class);

    private static final String SERVER_HOST = "0.0.0.0";
    private static final int SERVER_PORT = 8080;
    private Vertx vertx;

    private KafkaAdminService kafkaAdminService;

    @Before
    public void setup(TestContext testContext) {
        vertx = Vertx.vertx();
        kafkaAdminService = new KafkaAdminServiceImpl(vertx, "localhost:9092");
        vertx.deployVerticle(new RestVerticle(kafkaAdminService), testContext.asyncAssertSuccess());
    }

    @After
    public void after(TestContext testContext) {
        vertx.close(testContext.asyncAssertSuccess());
    }

    @Test
    public void list_topics(TestContext context) {

        kafkaCluster.createTopic("topic1", 1, 1);

        WebClient client = WebClient.create(vertx);
        Async consumeAsync = context.async();
        client
                .get(SERVER_PORT, SERVER_HOST, "/api/topics")
                .putHeader("Accept", "application/json")
                .putHeader("Content-Type", "application/json")
                .as(BodyCodec.jsonArray())
                .send(ar -> {
                    if (ar.succeeded()) {
                        HttpResponse<JsonArray> response = ar.result();
                        JsonArray array = response.body();
                        List<String> names = new ArrayList<>();

                        for(int i = 0; i < array.size(); i++) {
                            JsonObject jsonResponse = array.getJsonObject(i);
                            String id = jsonResponse.getString("name");
                            names.add(id);
                        }
                        context.assertTrue(names.contains("topic1"));

                    } else {
                        log.error("Something went wrong " + ar.cause().getMessage());
                    }
                    consumeAsync.complete();
                });

        consumeAsync.await();

        Async consumeAsync2 = context.async();

        JsonObject json = new JsonObject().put("topics", "topic1");
        client.delete(SERVER_PORT, SERVER_HOST, "/api/topics")
                .putHeader("Content-length", String.valueOf(json.toBuffer().length()))
                .putHeader("Accept", "application/json")
                .putHeader("Content-Type", "application/json")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(json, ar -> {
                    context.assertTrue(ar.succeeded());
                    context.assertEquals(202, ar.result().statusCode());
                    consumeAsync2.complete();
                });

    }

    @Test
    public void delete_topics(TestContext context) {

        kafkaCluster.createTopic("topic1", 1, 1);
        kafkaCluster.createTopic("topic2", 1, 1);

        JsonObject json = new JsonObject().put("topics", "topic1,topic2");

        WebClient client = WebClient.create(vertx);
        Async consumeAsync = context.async();
        client.delete(SERVER_PORT, SERVER_HOST, "/api/topics")
                .putHeader("Content-length", String.valueOf(json.toBuffer().length()))
                .putHeader("Accept", "application/json")
                .putHeader("Content-Type", "application/json")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(json, ar -> {
                    context.assertTrue(ar.succeeded());
                    context.assertEquals(202, ar.result().statusCode());
                    consumeAsync.complete();
                });

    }

    @Test
    public void describe_topics(TestContext context) {

        kafkaCluster.createTopic("topic1", 1, 1);
        kafkaCluster.createTopic("topic2", 1, 1);

        JsonObject json = new JsonObject().put("topics", "topic1,topic2");

        WebClient client = WebClient.create(vertx);
        Async consumeAsync = context.async();
        client.post(SERVER_PORT, SERVER_HOST, "/api/topics/describe")
                .putHeader("Content-length", String.valueOf(json.toBuffer().length()))
                .putHeader("Accept", "application/json")
                .putHeader("Content-Type", "application/json")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(json, ar -> {
                    context.assertTrue(ar.succeeded());
                    context.assertEquals(200, ar.result().statusCode());
                    consumeAsync.complete();
                });

    }

    @Test
    public void create_topics(TestContext context) {

        JsonArray array = new JsonArray();
        JsonObject object = new JsonObject();
        object.put("name", "topic1");
        object.put("numPartitions", 1);
        object.put("replicationFactor", 1);
        array.add(object);
        JsonObject json = new JsonObject().put("topics", array);

        WebClient client = WebClient.create(vertx);
        Async consumeAsync = context.async();
        client.put(SERVER_PORT, SERVER_HOST, "/api/topics")
                .putHeader("Content-length", String.valueOf(json.toBuffer().length()))
                .putHeader("Accept", "application/json")
                .putHeader("Content-Type", "application/json")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(json, ar -> {
                    context.assertTrue(ar.succeeded());
                    context.assertEquals(201, ar.result().statusCode());
                    consumeAsync.complete();
                });

        consumeAsync.await();

        Async consumeAsync2 = context.async();

        json = new JsonObject().put("topics", "topic1");
        client.delete(SERVER_PORT, SERVER_HOST, "/api/topics")
                .putHeader("Content-length", String.valueOf(json.toBuffer().length()))
                .putHeader("Accept", "application/json")
                .putHeader("Content-Type", "application/json")
                .as(BodyCodec.jsonObject())
                .sendJsonObject(json, ar -> {
                    context.assertTrue(ar.succeeded());
                    context.assertEquals(202, ar.result().statusCode());
                    consumeAsync2.complete();
                });

    }

}