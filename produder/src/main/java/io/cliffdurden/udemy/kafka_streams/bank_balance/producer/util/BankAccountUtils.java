package io.cliffdurden.udemy.kafka_streams.bank_balance.producer.util;

import io.cliffdurden.udemy.kafka_streams.bank_balance.api.BankAccount;

import java.util.Random;

import static java.time.LocalDateTime.now;

public class BankAccountUtils {

    private BankAccountUtils() {
        throw new UnsupportedOperationException("Utils class");
    }

    public static BankAccount randomBankAccount() {
        return BankAccount.builder()
                .name(randomName())
                .amount(randomAmount())
                .time(now())
                .build();
    }

    private static long randomAmount() {
        return new Random().nextInt(1000);
    }

    private static String randomName() {
        return Names.values()[(new Random().nextInt(Names.values().length))].name();
    }

}
