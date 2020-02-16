package io.cliffdurden.udemy.kafka_streams.bank_balance.consumer;

import io.cliffdurden.udemy.kafka_streams.bank_balance.api.*;
import io.cliffdurden.udemy.kafka_streams.bank_balance.serialization.*;
import org.apache.kafka.common.serialization.*;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.junit.*;

import java.time.LocalDateTime;
import java.util.Properties;

import static java.time.LocalDateTime.now;
import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.assertThat;

public class BankTransactionConsumerAppTest {

    private TopologyTestDriver testDriver;
    private TestInputTopic<String, BankTransaction> inputTopic;
    private TestOutputTopic<String, BankAccount> outputTopic;

    private Topology topology;
    private KeyValueStore<String, BankTransaction> store;

    private static BankTransaction dummyJohnTransaction(long amount, LocalDateTime time) {
        return BankTransaction.builder()
                .amount(amount)
                .name("John")
                .time(time)
                .build();
    }

    private static BankAccount dummyJohnAccount(long amount, LocalDateTime time) {
        return BankAccount.builder()
                .amount(amount)
                .name("John")
                .updateTime(time)
                .build();
    }

    @Before
    public void setUp() {
        BankTransactionConsumerApp consumerApp = new BankTransactionConsumerApp();
        topology = consumerApp.createTopology();

        // setup test driver
        final Properties props = new Properties();
        props.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "bankTransactionsAggregator");
        props.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "dummy:1234");

        testDriver = new TopologyTestDriver(topology, props);

        // setup test topics
        inputTopic = testDriver.createInputTopic("bank-transactions", new StringSerializer(), new BankTransactionSerializer());
        outputTopic = testDriver.createOutputTopic("bank-accounts", new StringDeserializer(), new BankAccountDeserializer());

        // TODO: manually set storeName
        store = testDriver.getKeyValueStore("KSTREAM-AGGREGATE-STATE-STORE-0000000001");
    }

    @After
    public void tearDown() {
        testDriver.close();
    }

    @Test
    public void shouldFlushStoreForFirstInput() {
        LocalDateTime now = now();
        inputTopic.pipeInput("John", dummyJohnTransaction(10L, now));

        assertThat(outputTopic.readKeyValue(), equalTo(new KeyValue<>("John", dummyJohnAccount(10L, now))));
        assertThat(outputTopic.isEmpty(), is(true));
    }

    @Test
    public void should_change_change_BankAccount() {
        inputTopic.pipeInput("John", BankTransaction.builder().amount(10L).name("John").time(now()).build());
        inputTopic.pipeInput("John", BankTransaction.builder().amount(10L).name("John").time(now()).build());

        assertThat("Wrong aggregated amount", outputTopic.readKeyValue().value.getAmount(), equalTo(10L));
        assertThat("Wrong aggregated amount", outputTopic.readKeyValue().value.getAmount(), equalTo(20L));
    }

    @Test
    public void should_update_store_with_new_Balance() {
        LocalDateTime now = now();
        BankTransaction initialBankTransaction = dummyJohnTransaction(10, now());
        store.put("John", initialBankTransaction);

        inputTopic.pipeInput("John", dummyJohnTransaction(20L, now));

        assertThat(store.get("John"), equalTo(dummyJohnTransaction(30, now)));
    }

    @Test
    public void should_change_change_BankAccount_with_previous_stored_value() {
        BankTransaction initialBankTransaction = dummyJohnTransaction(10, now());
        store.put("John", initialBankTransaction);

        inputTopic.pipeInput("John", BankTransaction.builder().amount(10L).name("John").time(now()).build());

        assertThat("Wrong aggregated amount", outputTopic.readKeyValue().value.getAmount(), equalTo(20L));
        assertThat(outputTopic.isEmpty(), is(true));
    }
}
