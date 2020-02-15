package io.cliffdurden.udemy.kafka_streams.bank_balance.producer;

import io.cliffdurden.udemy.kafka_streams.bank_balance.api.BankAccount;
import io.cliffdurden.udemy.kafka_streams.bank_balance.serialization.BankAccountSerializer;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.*;

import static io.cliffdurden.udemy.kafka_streams.bank_balance.producer.util.BankAccountUtils.randomBankAccount;

@Slf4j
public class ProducerApp {

    private static final String TOPIC_NAME = "bank-transactions";
    private static final int MAX_MESSAGES_COUNT = 1_000_000;

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, BankAccountSerializer.class.getName());
        props.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, "true");

        try (Producer<String, BankAccount> producer = new KafkaProducer<>(props);) {
            testData().forEach(
                    bankAccount -> {
                        log.info(bankAccount.toString());
                        ProducerRecord<String, BankAccount> bankAccountProducerRecord = new ProducerRecord<>(TOPIC_NAME, bankAccount.getName(), bankAccount);
                        producer.send(bankAccountProducerRecord);
                        try {
                            TimeUnit.SECONDS.sleep(1);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt();
                        }
                    });
            Runtime.getRuntime().addShutdownHook(new Thread(producer::close));
        }
    }

    private static List<BankAccount> testData() {
        return IntStream.range(1, MAX_MESSAGES_COUNT)
                .mapToObj(i -> randomBankAccount())
                .collect(Collectors.toList());
    }

}
