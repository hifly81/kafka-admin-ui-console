# Kafka Console - Apache Kafka UI admin console

Kafka Console is a UI for Apache Kafka admin operations.

## Run Kafka Console UI

WIP (help wanted!)

## Run Kafka Console Server

Compile the project:

```bash
cd kafka-console-server
mvn clean package
```

Run the server (listening on port 8080):
```bash
cd kafka-console-server/target
java -jar kafka-console-server-1.0-SNAPSHOT-app.jar
```

## Kafka Console API

```bash
#Create topics
curl -X PUT -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8080/api/topics -d '{"topics": [{"name":"topic1","numPartitions": 1,"replicationFactor": 1},{"name":"topic2","numPartitions": 1,"replicationFactor": 1}]}'

#Delete topics
curl -X DELETE -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8080/api/topics -d '{"topics": "topic1,topic2"}'

#Get topic list
curl -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8080/api/topics

#Describe topics
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8080/api/topics/describe -d '{"topics": "topic1,topic2"}'

#Get consumer groups list
curl -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8080/api/consumergroups

#Describe consumer groups
curl -X POST -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8080/api/consumergroups/describe -d '{"groups": "group1,group2"}'

#Delete consumer groups
curl -X DELETE -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8080/api/consumergroups -d '{"groups": "group1,group2"}'

#Describe cluster
curl -H "Accept:application/json" -H "Content-Type:application/json" http://localhost:8080/api/cluster
```
