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
package io.quicksign.kafka.crypto.samples.keyrepo;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;

import io.quicksign.kafka.crypto.Encryptor;
import io.quicksign.kafka.crypto.encryption.CryptoAlgorithm;
import io.quicksign.kafka.crypto.encryption.DefaultEncryptor;
import io.quicksign.kafka.crypto.encryption.KeyProvider;
import io.quicksign.kafka.crypto.pairing.keyextractor.KeyReferenceExtractor;
import io.quicksign.kafka.crypto.pairing.serializer.CryptoSerializerPairFactory;
import io.quicksign.kafka.crypto.pairing.serializer.SerializerPair;

public class SampleProducer implements Runnable {

    private final KeyProvider keyProvider;
    private final KeyReferenceExtractor keyReferenceExtractor;
    private final CryptoAlgorithm cryptoAlgorithm;

    public SampleProducer(KeyProvider keyProvider, KeyReferenceExtractor keyReferenceExtractor, CryptoAlgorithm cryptoAlgorithm) {
        this.keyProvider = keyProvider;
        this.keyReferenceExtractor = keyReferenceExtractor;
        this.cryptoAlgorithm = cryptoAlgorithm;
    }

    @Override
    public void run() {

        // tag::produce[]

        Encryptor encryptor = new DefaultEncryptor(keyProvider, cryptoAlgorithm);

        // Wrap base LongSerializer and StringSerializer with encrypted wrappers
        CryptoSerializerPairFactory cryptoSerializerPairFactory = new CryptoSerializerPairFactory(encryptor, keyReferenceExtractor);
        SerializerPair<Long, String> serializerPair = cryptoSerializerPairFactory.build(new LongSerializer(), new StringSerializer());

        Properties producerProperties = new Properties();
        producerProperties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");

        try (KafkaProducer<Long, String> producer =
                     new KafkaProducer<>(producerProperties, serializerPair.getKeySerializer(), serializerPair.getValueSerializer())) {

            for (long i = 0L; i < Long.MAX_VALUE; i++) {
                producer.send(new ProducerRecord<>("sampletopic", i, "test number " + i));
                try {
                    Thread.sleep(1000L);
                }
                catch (InterruptedException e) {
                    return;
                }
            }
        }
        // end::produce[]
    }
}
