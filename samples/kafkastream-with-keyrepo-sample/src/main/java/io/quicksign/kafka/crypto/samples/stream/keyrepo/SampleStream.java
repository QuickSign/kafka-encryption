/*-
 * #%L
 * Kafka Encryption
 * %%
 * Copyright (C) 2018 Quicksign
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.quicksign.kafka.crypto.samples.stream.keyrepo;

import java.util.HashMap;
import java.util.Map;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.state.KeyValueBytesStoreSupplier;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.Stores;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.quicksign.kafka.crypto.Decryptor;
import io.quicksign.kafka.crypto.Encryptor;
import io.quicksign.kafka.crypto.encryption.DefaultDecryptor;
import io.quicksign.kafka.crypto.encryption.DefaultEncryptor;
import io.quicksign.kafka.crypto.encryption.KeyProvider;
import io.quicksign.kafka.crypto.pairing.keyextractor.KeyReferenceExtractor;
import io.quicksign.kafka.crypto.pairing.serdes.CryptoSerdeFactory;
import io.quicksign.kafka.crypto.pairing.serdes.SerdesPair;

public class SampleStream implements Runnable {

    private final String name;
    private final KeyProvider keyProvider;
    private final KeyReferenceExtractor keyReferenceExtractor;

    public SampleStream(String name, KeyProvider keyProvider, KeyReferenceExtractor keyReferenceExtractor) {

        this.name = name;
        this.keyProvider = keyProvider;
        this.keyReferenceExtractor = keyReferenceExtractor;
    }


    @Override
    public void run() {
        try {
            // tag::stream[]
            AesGcmNoPaddingCryptoAlgorithm cryptoAlgorithm = new AesGcmNoPaddingCryptoAlgorithm();
            Decryptor decryptor = new DefaultDecryptor(keyProvider, cryptoAlgorithm);
            Encryptor encryptor = new DefaultEncryptor(keyProvider, cryptoAlgorithm);
            CryptoSerdeFactory cryptoSerdeFactory = new CryptoSerdeFactory(encryptor, decryptor, keyReferenceExtractor);

            KeyValueBytesStoreSupplier storeSupplier = Stores.inMemoryKeyValueStore(name + "_balance");


            Map<String, Object> props = new HashMap<>();
            props.put(StreamsConfig.APPLICATION_ID_CONFIG, name);
            props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
            props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
            StreamsConfig config = new StreamsConfig(props);


            StreamsBuilder streamsBuilder = new StreamsBuilder();

            SerdesPair<Integer, String> serdesPair = cryptoSerdeFactory.buildSerdesPair(Serdes.Integer(), Serdes.String());

            streamsBuilder.stream("operations", serdesPair.toConsumed())
                    .filter((i, s) -> s != null) // messages that were not decrypted (because key not in repository) are null
                    .groupByKey()
                    .reduce((s1, s2) -> "" + (Integer.valueOf(s1) + Integer.valueOf(s2)),
                            serdesPair.applyTo(Materialized.as(storeSupplier)));

            KafkaStreams kafkaStreams = new KafkaStreams(streamsBuilder.build(), config);
            kafkaStreams.start();

            // end::stream[]

            ObjectMapper mapper = new ObjectMapper();

            for (; true; ) {
                try {
                    Thread.sleep(10000L);
                }
                catch (InterruptedException e) {
                    return;
                }
                try {

                    // tag::display[]
                    ReadOnlyKeyValueStore<Integer, String> store = kafkaStreams.store(
                            name + "_balance", QueryableStoreTypes.<Integer, String>keyValueStore()
                    );

                    ArrayNode arrayNode = JsonNodeFactory.instance.arrayNode();

                    KeyValueIterator<Integer, String> iterator = store.all();
                    iterator.forEachRemaining(kv -> {
                        ObjectNode node = JsonNodeFactory.instance.objectNode();
                        node.put("account", kv.key);
                        node.put("balance", kv.value);
                        arrayNode.add(node);
                    });

                    ObjectNode node = JsonNodeFactory.instance.objectNode();
                    node.set(name, arrayNode);

                    System.out.println(mapper.writerWithDefaultPrettyPrinter().writeValueAsString(node));
                    // end::display[]
                }
                catch (Exception e) {

                }
            }
        }
        catch (Throwable e) {
            System.out.println("error on " + name);
            e.printStackTrace();
        }

    }
}
