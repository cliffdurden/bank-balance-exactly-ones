# Solution for udemy example of course Apache Kafka Series - Kafka Streams for Data Processing
Section 8

# Docker
docker image: https://github.com/confluentinc/cp-docker-images

# Need to add this property to kafka env in docker-compose.yml
```
KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1 
KAFKA_TRANSACTION_STATE_LOG.MIN.ISR: 1
```

# Start kafka
```
docker-compose up
```

# Show topics
```
docker container exec -it kafka-single-node_kafka_1 kafka-topics \
--bootstrap-server localhost:9092 \
--list
```
# Create a new topics
## Create input topic
```
docker container exec -it kafka-single-node_kafka_1 kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--replication-factor 1 \
--config min.insync.replicas=1 \
--partitions 1 \
--topic bank-transactions
```

## Create output topic
```
docker container exec -it kafka-single-node_kafka_1 kafka-topics \
--bootstrap-server localhost:9092 \
--create \
--replication-factor 1 \
--partitions 1 \
--topic bank-accounts
```

# Read values from input topic
```
docker container exec -it kafka-single-node_kafka_1 kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic bank-transactions \
--from-beginning \
--property print.key=true \
--value-deserializer org.apache.kafka.common.serialization.StringDeserializer
```

# Read values from out topic
```
docker container exec -it kafka-single-node_kafka_1 kafka-console-consumer \
--bootstrap-server localhost:9092 \
--topic bank-accounts \
--from-beginning \
--property print.key=true \
--value-deserializer org.apache.kafka.common.serialization.StringDeserializer
```
