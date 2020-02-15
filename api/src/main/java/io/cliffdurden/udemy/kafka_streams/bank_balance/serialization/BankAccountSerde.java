package io.cliffdurden.udemy.kafka_streams.bank_balance.serialization;

import io.cliffdurden.udemy.kafka_streams.bank_balance.api.BankAccount;
import org.apache.kafka.common.serialization.*;

public class BankAccountSerde extends Serdes.WrapperSerde<BankAccount> {

    public BankAccountSerde() {
        super(new BankAccountSerializer(), new BankAccountDeserializer());
    }
}
