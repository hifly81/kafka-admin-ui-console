package io.hifly.kafka.console;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import io.debezium.kafka.KafkaCluster;
import io.debezium.util.Testing;
import io.vertx.ext.unit.TestContext;

/**
 * Base class for tests providing a Kafka cluster
 */
public class KafkaClusterTestBase {

    protected static final int ZOOKEEPER_PORT = 2181;
    protected static final int KAFKA_PORT = 9092;
    protected static final String DATA_DIR = "cluster";

    private static File dataDir;
    protected static KafkaCluster kafkaCluster;

    protected static KafkaCluster kafkaCluster() {

        if (kafkaCluster != null) {
            throw new IllegalStateException();
        }
        dataDir = Testing.Files.createTestingDirectory(DATA_DIR);

        Properties props = new Properties();
        props.put("auto.create.topics.enable", "false");

        kafkaCluster =
                new KafkaCluster()
                        .usingDirectory(dataDir)
                        .withPorts(ZOOKEEPER_PORT, KAFKA_PORT)
                        .withKafkaConfiguration(props);
        return kafkaCluster;
    }

    @BeforeClass
    public static void setUp(TestContext context) throws IOException {
        kafkaCluster = kafkaCluster().deleteDataPriorToStartup(true).addBrokers(1).startup();
    }


    @AfterClass
    public static void tearDown(TestContext context) {

        if (kafkaCluster != null) {
            kafkaCluster.shutdown();
            kafkaCluster = null;
            boolean delete = dataDir.delete();
            // If files are still locked and a test fails: delete on exit to allow subsequent test execution
            if (!delete) {
                dataDir.deleteOnExit();
            }
        }
    }
}