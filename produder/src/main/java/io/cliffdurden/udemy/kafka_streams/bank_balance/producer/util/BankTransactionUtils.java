package io.cliffdurden.udemy.kafka_streams.bank_balance.producer.util;

import io.cliffdurden.udemy.kafka_streams.bank_balance.api.BankTransaction;

import java.util.Random;

import static java.time.LocalDateTime.now;

public class BankTransactionUtils {

    private BankTransactionUtils() {
        throw new UnsupportedOperationException("Utils class");
    }

    public static BankTransaction randomBankAccount() {
        return BankTransaction.builder()
                .name(randomName())
                .amount(randomAmount())
                .time(now())
                .build();
    }

    private static long randomAmount() {
        return new Random().nextInt(1000) - 500L;
    }

    private static String randomName() {
        return Names.values()[(new Random().nextInt(Names.values().length))].name();
    }

}
