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
package io.quicksign.kafka.crypto.samples.generatedkey;

import java.util.Collections;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;

import io.quicksign.kafka.crypto.CryptoDeserializerFactory;
import io.quicksign.kafka.crypto.Decryptor;
import io.quicksign.kafka.crypto.encryption.DefaultDecryptor;
import io.quicksign.kafka.crypto.generatedkey.MasterKeyEncryption;
import io.quicksign.kafka.crypto.generatedkey.PerRecordKeyProvider;

public class SampleRawConsumer implements Runnable {

    public SampleRawConsumer() {
    }


    @Override
    public void run() {

        Properties consumerProperties = new Properties();
        consumerProperties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        consumerProperties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, "sampleraw");
        consumerProperties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        try (KafkaConsumer<Long, String> consumer = new KafkaConsumer<Long, String>(
                consumerProperties,
                new LongDeserializer(),
                new StringDeserializer())) {

            consumer.subscribe(Collections.singleton("sampletopic"));
            for(; true;){
                ConsumerRecords<Long, String> records = consumer.poll(1000L);
                records.forEach(
                        record -> System.out.println("raw record: key="+record.key()+", offset="+record.offset()+", value="+record.value())
                );
            }
        }
    }
}
