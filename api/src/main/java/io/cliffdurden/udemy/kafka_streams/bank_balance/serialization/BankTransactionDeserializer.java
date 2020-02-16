package io.cliffdurden.udemy.kafka_streams.bank_balance.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cliffdurden.udemy.kafka_streams.bank_balance.api.BankTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Deserializer;

import java.io.IOException;

@Slf4j
public class BankTransactionDeserializer implements Deserializer<BankTransaction> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public BankTransaction deserialize(String topic, byte[] data) {
        BankTransaction retVal = null;
        try {
            retVal = objectMapper.readValue(data, BankTransaction.class);
        } catch (IOException e) {
            log.error("Can not deserialize BankAccount", e);
        }
        return retVal;
    }
}
