package io.hifly.kafka.console.service;

import io.hifly.kafka.console.model.ConsoleTopic;
import io.hifly.kafka.console.model.ControllerNode;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.kafka.admin.KafkaAdminClient;
import io.vertx.kafka.client.common.impl.Helper;
import kafka.tools.TopicPartitionReplica;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.requests.DescribeLogDirsResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

public class KafkaAdminServiceImpl implements KafkaAdminService {

    private static final Logger logger = LoggerFactory.getLogger(io.hifly.kafka.console.service.KafkaAdminServiceImpl.class);

    private final KafkaAdminClient kafkaAdminClient;
    private AdminClient kafkaInternalAdminClient;

    public KafkaAdminServiceImpl(Vertx vertx, String kafkaBootstrapServers) {
        Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaBootstrapServers);

        this.kafkaAdminClient = KafkaAdminClient.create(vertx, props);
        this.kafkaInternalAdminClient = AdminClient.create(props);
    }

    @Override
    public void listTopics(Handler<AsyncResult<Set<ConsoleTopic>>> resulthandler) {
        kafkaAdminClient.listTopics(ar -> {
            if(ar.succeeded()) {
                Set<ConsoleTopic> result = null;
                Set<String> topics = ar.result();
                if(topics != null && topics.size() != 0) {
                    result = new HashSet<>(topics.size());
                    for(String topic:topics) {
                        ConsoleTopic consoleTopic = new ConsoleTopic();
                        consoleTopic.setName(topic);
                        result.add(consoleTopic);
                    }
                }
                resulthandler.handle(Future.succeededFuture(result));
            } else {
                resulthandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void listConsumerGroups(Handler<AsyncResult<List<io.vertx.kafka.admin.ConsumerGroupListing>>> resulthandler) {
        kafkaAdminClient.listConsumerGroups(ar -> {
            if(ar.succeeded()) {
                resulthandler.handle(Future.succeededFuture(ar.result()));
            } else {
                resulthandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void listConsumerGroupOffsets​(String groupId, Handler<AsyncResult<Map<TopicPartition, OffsetAndMetadata>>> resulthandler) {
        ListConsumerGroupOffsetsResult listConsumerGroupOffsetsResult = kafkaInternalAdminClient.listConsumerGroupOffsets(groupId);
        listConsumerGroupOffsetsResult.partitionsToOffsetAndMetadata().whenComplete((partitions, ex)-> {
            if(ex == null) {
                resulthandler.handle(Future.succeededFuture(partitions));
            } else {
                resulthandler.handle(Future.failedFuture(ex));
            }
        });
    }

    @Override
    public void describeTopics​(List<String> topicNames, Handler<AsyncResult<Map<String, io.vertx.kafka.admin.TopicDescription>>> resulthandler) {
        kafkaAdminClient.describeTopics(topicNames, ar -> {
            if(ar.succeeded()) {
                resulthandler.handle(Future.succeededFuture(ar.result()));
            } else {
                resulthandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void describeLogDirs​(List<Integer> brokers, Handler<AsyncResult<Map<Integer, Map<String, DescribeLogDirsResponse.LogDirInfo>>>> resulthandler) {
        DescribeLogDirsResult describeLogDirsResult = this.kafkaInternalAdminClient.describeLogDirs(brokers);
        describeLogDirsResult.all().whenComplete((logs, ex) -> {
            if(ex == null) {
                resulthandler.handle(Future.succeededFuture(logs));
            } else {
                resulthandler.handle(Future.failedFuture(ex));
            }
        });
    }

    @Override
    public void describeConsumerGroups​(List<String> groupIds, Handler<AsyncResult<Map<String, io.vertx.kafka.admin.ConsumerGroupDescription>>> resulthandler) {
        kafkaAdminClient.describeConsumerGroups(groupIds, ar -> {
            if(ar.succeeded()) {
                resulthandler.handle(Future.succeededFuture(ar.result()));
            } else {
                resulthandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void describeCluster​(Handler<AsyncResult<Map<String,List>>> resulthandler) {
        DescribeClusterResult describeClusterResult = this.kafkaInternalAdminClient.describeCluster();
        Map<String,List> result = new HashMap<>();
        describeClusterResult.nodes().whenComplete((nodes, ex) -> {
            List nodesList;
            final ControllerNode controllerNode = new ControllerNode();
            if (ex == null) {
                nodesList = nodes.stream().map(Helper::from).collect(Collectors.toList());
                result.put("nodes", nodesList);
                describeClusterResult.controller().whenComplete((controller, ex2) -> {
                    if(ex2 == null) {
                        controllerNode.setHost(controller.host());
                        controllerNode.setId(controller.id());
                        controllerNode.setPort(controller.port());
                        controllerNode.setHasRack(controller.hasRack());
                        controllerNode.setRack(controller.rack());
                        result.put("controller", Arrays.asList(controllerNode));
                        describeClusterResult.clusterId().whenComplete((clusterId, ex3) -> {
                           if(ex3 == null) {
                               result.put("cluster", Arrays.asList(clusterId));
                               resulthandler.handle(Future.succeededFuture(result));
                           }
                           else {
                               resulthandler.handle(Future.failedFuture(ex3));
                           }
                        });

                    } else {
                        resulthandler.handle(Future.failedFuture(ex2));
                    }
                });
            } else {
                resulthandler.handle(Future.failedFuture(ex));
            }

        });
    }

    @Override
    public void deleteTopics​(List<String> topics, Handler<AsyncResult<Void>> resulthandler) {
        kafkaAdminClient.deleteTopics(topics, ar -> {
            if(ar.succeeded()) {
                resulthandler.handle(Future.succeededFuture(ar.result()));
            } else {
                resulthandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    public void deleteConsumerGroups​(java.util.Collection<java.lang.String> groupIds, Handler<AsyncResult<Void>> resulthandler) {
        DeleteConsumerGroupsResult deleteConsumerGroupsResult = this.kafkaInternalAdminClient.deleteConsumerGroups(groupIds);
        deleteConsumerGroupsResult.all().whenComplete((v, ex) -> {
            if (ex == null) {
                resulthandler.handle(Future.succeededFuture());
            } else {
                resulthandler.handle(Future.failedFuture(ex));
            }

        });
    }

    @Override
    public void createTopics​(List<io.vertx.kafka.admin.NewTopic> newTopics, Handler<AsyncResult<Void>> resulthandler) {
        kafkaAdminClient.createTopics(newTopics, ar -> {
            if(ar.succeeded()) {
                resulthandler.handle(Future.succeededFuture(ar.result()));
            } else {
                resulthandler.handle(Future.failedFuture(ar.cause()));
            }
        });
    }

    @Override
    public void createPartitions​(Map<String, NewPartitions> newPartitions, Handler<AsyncResult<CreatePartitionsResult>> resulthandler) {
        CreatePartitionsResult createPartitionsResult = this.kafkaInternalAdminClient.createPartitions(newPartitions);
        createPartitionsResult.all().whenComplete((v, ex) -> {
            if (ex == null) {
                resulthandler.handle(Future.succeededFuture());
            } else {
                resulthandler.handle(Future.failedFuture(ex));
            }

        });
    }

    @Override
    public void alterReplicaLogDirs​(Map<TopicPartitionReplica, String> replicaAssignment, Handler<AsyncResult<AlterReplicaLogDirsResult>> resulthandler) {

    }

    @Override
    public void alterConfigs​(Map<ConfigResource, Config> configs, Handler<AsyncResult<AlterConfigsResult>> resulthandler) {

    }
}