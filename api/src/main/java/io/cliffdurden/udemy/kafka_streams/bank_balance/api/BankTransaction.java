package io.cliffdurden.udemy.kafka_streams.bank_balance.api;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.*;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import lombok.*;

import java.time.LocalDateTime;

/**
 * {
 * "name": "John",
 * "amount": 123,
 * "time": "2019-12-15T05:24:30"
 * }
 */
@Value
@Builder
@JsonDeserialize(builder = BankTransaction.BankTransactionBuilder.class)
public class BankTransaction {

    private final String name;
    private final Long amount;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "YYYY-MM-dd'T'HH:mm:ss.SSSSSS")
    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    @JsonSerialize(using = LocalDateTimeSerializer.class)
    private final LocalDateTime time;

    @JsonPOJOBuilder(withPrefix = "")
    public static class BankTransactionBuilder {

        @JsonDeserialize(using = LocalDateTimeDeserializer.class)
        @JsonSerialize(using = LocalDateTimeSerializer.class)
        private LocalDateTime time;
    }
}
