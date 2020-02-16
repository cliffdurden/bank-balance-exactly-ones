package io.cliffdurden.udemy.kafka_streams.bank_balance.consumer;

import io.cliffdurden.udemy.kafka_streams.bank_balance.api.*;
import io.cliffdurden.udemy.kafka_streams.bank_balance.serialization.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;

/**
 * The main purpose of this lesson is to try
 * StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE kafka feature
 */
@Slf4j
public class BankAccountConsumer {

    private static final String IN_MESSAGES_TOPIC_NAME = "bank-transactions";
    private static final String OUT_MESSAGES_TOPIC_NAME = "bank-accounts";


    public static void main(String[] args) {
        Properties kafkaProperties = new Properties();
        // the goal of the exercise
        //  ERROR [KafkaApi-1] Number of alive brokers '1' does not meet the required replication factor '3' for the transactions state topic (configured via 'transaction.state.log.replication.factor'). This error can be ignored if the cluster is starting up and not all brokers are up yet. (kafka.server.KafkaApis)
        //  Note that "exactly_once" processing requires a cluster of at least three brokers by default, which is the recommended setting for production.
        kafkaProperties.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);
        kafkaProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "latest");
        kafkaProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "bank-account-consumer");
        kafkaProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        BankAccountConsumer consumer = new BankAccountConsumer();
        try (KafkaStreams kafkaStreams = new KafkaStreams(consumer.createTopology(), kafkaProperties)) {
            //Do a clean up of the local StateStore directory (StreamsConfig.STATE_DIR_CONFIG) by deleting all data with regard to the application ID
            kafkaStreams.cleanUp(); // DO NOT DO THIS IN PROD
            kafkaStreams.start();

            Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
        }
    }

    private static BankTransaction aggregate(BankTransaction transaction, BankTransaction balance) {
        return BankTransaction.builder()
                .time(transaction.getTime())
                .amount(balance.getAmount() + transaction.getAmount())
                .name(transaction.getName())
                .build();
    }

    private static BankAccount mapAccount(BankTransaction aggregatedTransaction) {
        return BankAccount.builder()
                .updateTime(aggregatedTransaction.getTime())
                .amount(aggregatedTransaction.getAmount())
                .name(aggregatedTransaction.getName())
                .build();
    }

    public Topology createTopology() {
        StreamsBuilder kafkaStreamBuilder = new StreamsBuilder();

        final Serde<BankTransaction> bankTransactionSerde = Serdes.serdeFrom(new BankTransactionSerializer(), new BankTransactionDeserializer());
        final Serde<BankAccount> bankAccountSerde = Serdes.serdeFrom(new BankAccountSerializer(), new BankAccountDeserializer());

        KStream<String, BankTransaction> kStreamBankTransactions = kafkaStreamBuilder
                .stream(IN_MESSAGES_TOPIC_NAME, Consumed.with(Serdes.String(), bankTransactionSerde));

        BankTransaction initialVal = BankTransaction.builder()
                .amount(0L)
                .name(null)
                .time(null)
                .build();

        kStreamBankTransactions
                .groupByKey()
                .aggregate(
                        () -> initialVal,
                        (key, transaction, aggregatedTransaction) -> aggregate(transaction, aggregatedTransaction),
                        Materialized.with(Serdes.String(), bankTransactionSerde)

                )
                .mapValues(BankAccountConsumer::mapAccount)
                .toStream()
                .peek((key, value) -> log.info("{}'s bank balance changed: {}", key, value))
                .to(OUT_MESSAGES_TOPIC_NAME, Produced.with(Serdes.String(), bankAccountSerde));
        return kafkaStreamBuilder.build();
    }

}
