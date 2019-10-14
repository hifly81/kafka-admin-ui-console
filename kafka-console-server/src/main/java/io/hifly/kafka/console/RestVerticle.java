package io.hifly.kafka.console;

import io.hifly.kafka.console.model.ErrorMessage;
import io.hifly.kafka.console.service.KafkaAdminService;
import io.vertx.config.ConfigRetriever;
import io.vertx.config.ConfigRetrieverOptions;
import io.vertx.config.ConfigStoreOptions;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.kafka.admin.NewTopic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RestVerticle extends AbstractVerticle {

    private Router router;
    private KafkaAdminService kafkaAdminService;
    private JsonObject jsonObject;

    public RestVerticle() {
    }

    public RestVerticle(KafkaAdminService kafkaAdminService) {
        this.kafkaAdminService = kafkaAdminService;
    }

    @Override
    public void start(Future<Void> future) {

        if (kafkaAdminService == null) {
            ConfigStoreOptions fileStore = new ConfigStoreOptions()
                    .setType("file")
                    .setOptional(true)
                    .setConfig(new JsonObject().put("path", "conf/config.json"));

            ConfigRetrieverOptions options = new ConfigRetrieverOptions().addStore(fileStore);
            ConfigRetriever retriever = ConfigRetriever.create(vertx, options);
            retriever.getConfig(ar -> {
                if (ar.failed()) {
                    // Failed to retrieve the configuration
                } else {
                    jsonObject = ar.result();
                    String bootstrapServers = jsonObject.getString("kafka.bootstrap.servers");
                    kafkaAdminService = new io.hifly.kafka.console.service.KafkaAdminServiceImpl(vertx, bootstrapServers);
                }
            });
        }

        router = Router.router(vertx);

        router.get("/api/cluster").handler(this::cluster);

        router.get("/api/topics").handler(this::topics);
        router.route("/api/topics").handler(BodyHandler.create());
        router.delete("/api/topics").handler(this::deleteTopics);
        router.put("/api/topics").handler(this::createTopics);

        router.route("/api/topics/describe").handler(BodyHandler.create());
        router.post("/api/topics/describe").handler(this::topicsDescribe);

        router.get("/api/consumergroups").handler(this::consumergroups);
        router.route("/api/consumergroups").handler(BodyHandler.create());
        router.delete("/api/consumergroups").handler(this::deleteConsumerGroups);
        router.route("/api/consumergroups/describe").handler(BodyHandler.create());
        router.post("/api/consumergroups/describe").handler(this::consumerGroupsDescribe);
        router.get("/api/consumergroups/:groupId/offset").handler(this::consumergroupOffset);

        router.route("/api/logs/describe").handler(BodyHandler.create());
        router.post("/api/logs/describe").handler(this::logsDescribe);


        vertx.createHttpServer()
                .requestHandler(this.router)
                .listen(config().getInteger("http.port", 8080), result -> {
                    if (result.succeeded()) {
                        future.complete();
                    } else {
                        future.fail(result.cause());
                    }
                });
    }

    private void cluster(RoutingContext routingContext) {
        kafkaAdminService.describeCluster​(ar -> {
            if (ar.succeeded()) {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(ar.result()));

            } else {
                handleErrorMessage(routingContext, ar);
            }
        });
    }

    private void topics(RoutingContext routingContext) {
        kafkaAdminService.listTopics(ar -> {
            if (ar.succeeded()) {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(ar.result()));

            } else {
                handleErrorMessage(routingContext, ar);
            }
        });

    }

    private void topicsDescribe(RoutingContext routingContext) {
        JsonObject json = routingContext.getBodyAsJson();
        String topics = (String) json.getValue("topics");
        List<String> input = Arrays.asList(topics.split(","));
        kafkaAdminService.describeTopics​(input, ar -> {
            if (ar.succeeded()) {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(ar.result()));

            } else {
                handleErrorMessage(routingContext, ar);
            }
        });

    }

    private void deleteTopics(RoutingContext routingContext) {
        JsonObject json = routingContext.getBodyAsJson();
        String topics = (String) json.getValue("topics");
        List<String> input = Arrays.asList(topics.split(","));
        kafkaAdminService.deleteTopics​(input, ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(202).end();
            } else {
                handleErrorMessage(routingContext, ar);
            }
        });
    }

    private void createTopics(RoutingContext routingContext) {
        JsonObject json = routingContext.getBodyAsJson();
        JsonArray topics = json.getJsonArray("topics");
        List<io.vertx.kafka.admin.NewTopic> newTopics = new ArrayList<>(topics.size());
        for (int i = 0; i < topics.size(); i++) {
            io.vertx.kafka.admin.NewTopic newTopic = new NewTopic();
            JsonObject temp = topics.getJsonObject(i);
            newTopic.setName(temp.getString("name"));
            newTopic.setNumPartitions(temp.getInteger("numPartitions"));
            Integer intReplica = temp.getInteger("replicationFactor");
            newTopic.setReplicationFactor(intReplica.shortValue());
            newTopics.add(newTopic);
        }

        kafkaAdminService.createTopics​(newTopics, ar -> {
            if (ar.succeeded()) {
                routingContext.response().setStatusCode(201).end();
            } else {
                handleErrorMessage(routingContext, ar);
            }
        });
    }

    private void consumergroups(RoutingContext routingContext) {
        kafkaAdminService.listConsumerGroups(ar -> {
            if (ar.succeeded()) {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(ar.result()));

            } else {
                handleErrorMessage(routingContext, ar);
            }
        });

    }

    private void consumerGroupsDescribe(RoutingContext routingContext) {
        JsonObject json = routingContext.getBodyAsJson();
        String groups = (String) json.getValue("groups");
        List<String> input = Arrays.asList(groups.split(","));
        kafkaAdminService.describeConsumerGroups​(input, ar -> {
            if (ar.succeeded()) {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(ar.result()));

            } else {
                handleErrorMessage(routingContext, ar);
            }
        });

    }

    private void consumergroupOffset(RoutingContext routingContext) {
        String groupId = routingContext.request().getParam("groupId");
        kafkaAdminService.listConsumerGroupOffsets​(groupId, ar -> {
            if (ar.succeeded()) {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(ar.result()));

            } else {
                handleErrorMessage(routingContext, ar);
            }
        });

    }

    private void deleteConsumerGroups(RoutingContext routingContext) {
        JsonObject json = routingContext.getBodyAsJson();
        String groups = (String) json.getValue("groups");
        List<String> input = Arrays.asList(groups.split(","));
        kafkaAdminService.deleteConsumerGroups​(input, ar -> {
            if (ar.succeeded()) {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(ar.result()));

            } else {
                handleErrorMessage(routingContext, ar);
            }
        });

    }

    private void logsDescribe(RoutingContext routingContext) {
        JsonObject json = routingContext.getBodyAsJson();
        String topics = (String) json.getValue("brokers");
        List<String> input = Arrays.asList(topics.split(","));
        kafkaAdminService.describeLogDirs​(input.stream().map(Integer::parseInt).collect(Collectors.toList()),ar -> {
            if (ar.succeeded()) {
                routingContext.response()
                        .putHeader("content-type", "application/json; charset=utf-8")
                        .end(Json.encodePrettily(ar.result()));

            } else {
                handleErrorMessage(routingContext, ar);
            }
        });

    }

    private void handleErrorMessage(RoutingContext routingContext, AsyncResult ar) {
        ErrorMessage errorMessage = new ErrorMessage();
        errorMessage.setMessage(ar.cause().toString());
        routingContext.response().setStatusCode(500)
                .putHeader("content-type", "application/json; charset=utf-8")
                .end(Json.encodePrettily(errorMessage));
    }


}