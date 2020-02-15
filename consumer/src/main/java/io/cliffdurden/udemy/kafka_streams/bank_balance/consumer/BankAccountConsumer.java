package io.cliffdurden.udemy.kafka_streams.bank_balance.consumer;

import io.cliffdurden.udemy.kafka_streams.bank_balance.api.BankAccount;
import io.cliffdurden.udemy.kafka_streams.bank_balance.serialization.*;
import lombok.extern.slf4j.Slf4j;
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

        kafkaProperties.put(StreamsConfig.APPLICATION_ID_CONFIG, "bank-account-consumer");
        kafkaProperties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        StreamsBuilder kafkaStreamBuilder = new StreamsBuilder();

        final Serde<BankAccount> bankAccountSerde = Serdes.serdeFrom(new BankAccountSerializer(), new BankAccountDeserializer());


        KStream<String, BankAccount> kStreamBankTransactions = kafkaStreamBuilder
                .stream(IN_MESSAGES_TOPIC_NAME, Consumed.with(Serdes.String(), bankAccountSerde));

        BankAccount initialVal = BankAccount.builder()
                .amount(0L)
                .name(null)
                .time(null)
                .build();

        kStreamBankTransactions
                .groupByKey()
                .aggregate(
                        () -> initialVal,
                        (key, transaction, balance) -> newBalance(transaction, balance),
                        Materialized.with(Serdes.String(), bankAccountSerde)

                )
                .toStream()
                .peek((key, value) -> log.info("{}'s bank balance changed: {}", key, value))
                .to(OUT_MESSAGES_TOPIC_NAME, Produced.with(Serdes.String(), bankAccountSerde));

        KafkaStreams kafkaStreams = new KafkaStreams(kafkaStreamBuilder.build(), kafkaProperties);

        kafkaStreams.cleanUp(); // DO NOT DO THIS IN PROD
        kafkaStreams.start();

        Runtime.getRuntime().addShutdownHook(new Thread(kafkaStreams::close));
    }

    private static BankAccount newBalance(BankAccount transaction, BankAccount balance) {
        return BankAccount.builder()
                .time(transaction.getTime())
                .amount(balance.getAmount() + transaction.getAmount())
                .name(transaction.getName())
                .build();
    }

}
