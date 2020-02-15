package io.cliffdurden.udemy.kafka_streams.bank_balance.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cliffdurden.udemy.kafka_streams.bank_balance.api.BankAccount;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class BankAccountDeserializer implements Deserializer<BankAccount> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BankAccount deserialize(String topic, byte[] data) {
        BankAccount retVal = null;
        try {
            retVal = objectMapper.readValue(data, BankAccount.class);
        } catch (IOException e) {
            log.error("Can not deserialize BankAccount", e);
        }
        return retVal;
    }
}
