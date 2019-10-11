package io.hifly.kafka.console.service;

import io.hifly.kafka.console.model.ConsoleTopic;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import kafka.tools.TopicPartitionReplica;
import org.apache.kafka.clients.admin.*;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.config.ConfigResource;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface KafkaAdminService {

    void listTopics(Handler<AsyncResult<Set<ConsoleTopic>>> resulthandler);
    void listConsumerGroups(Handler<AsyncResult<List<io.vertx.kafka.admin.ConsumerGroupListing>>> resulthandler);
    void listConsumerGroupOffsets​(java.lang.String groupId, Handler<AsyncResult<ListConsumerGroupOffsetsResult>> resulthandler);

    void describeTopics​(List<String> topicNames, Handler<AsyncResult<Map<String, io.vertx.kafka.admin.TopicDescription>>> resulthandler);
    void describeLogDirs​(java.util.Collection<java.lang.Integer> brokers, Handler<AsyncResult<DescribeLogDirsResult>> resulthandler);
    void describeConsumerGroups​(List<String> groupIds, Handler<AsyncResult<Map<String, io.vertx.kafka.admin.ConsumerGroupDescription>>> resulthandler);
    void describeCluster​(Handler<AsyncResult<Map<String,List>>> resulthandler);

    void deleteTopics​(List<String> topics, Handler<AsyncResult<Void>> resulthandler);
    void deleteConsumerGroups​(java.util.Collection<java.lang.String> groupIds, Handler<AsyncResult<Void>> resulthandler);

    void createTopics​(List<io.vertx.kafka.admin.NewTopic> newTopics, Handler<AsyncResult<Void>> resulthandler);
    void createPartitions​(java.util.Map<java.lang.String,NewPartitions> newPartitions, Handler<AsyncResult<CreatePartitionsResult>> resulthandler);

    void alterReplicaLogDirs​(java.util.Map<TopicPartitionReplica,java.lang.String> replicaAssignment, Handler<AsyncResult<AlterReplicaLogDirsResult>> resulthandler);
    void alterConfigs​(java.util.Map<ConfigResource,Config> configs, Handler<AsyncResult<AlterConfigsResult>> resulthandler);


}