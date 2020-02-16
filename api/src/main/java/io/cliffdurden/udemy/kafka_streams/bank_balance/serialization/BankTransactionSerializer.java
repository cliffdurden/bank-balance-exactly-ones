package io.cliffdurden.udemy.kafka_streams.bank_balance.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.cliffdurden.udemy.kafka_streams.bank_balance.api.BankTransaction;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serializer;

@Slf4j
public class BankTransactionSerializer implements Serializer<BankTransaction> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public byte[] serialize(String topic, BankTransaction data) {
        byte[] retVal = null;
        try {
            retVal = objectMapper.writeValueAsBytes(data);
        } catch (Exception e) {
            log.error("Error while serialize message {}", e.getMessage(), e);
        }
        return retVal;
    }


}
