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
/**
 * <p>A common use case is to choose the encryption key accordingly to the record key.
 * For example, in an event log, you may want to encrypt all records relative to the
 * same transaction with the same encryption key and the transaction id can be deducted
 * from the record key.</p>
 *
 * <p>To acheive this, the key serializer and the value serializer will be paired.
 * The key serializer will be wrapped to call the a
 * {@link io.quicksign.kafka.crypto.pairing.keyextractor.KeyReferenceExtractor KeyReferenceExtractor}
 * and put in the context the cryptographic key reference, that will be used to encrypt the record value.</p>
 *
 * <p>This based on the fact that {@link org.apache.kafka.clients.producer.KafkaProducer#doSend(org.apache.kafka.clients.producer.ProducerRecord, org.apache.kafka.clients.producer.Callback) KafkaProducer}
 * and {@link org.apache.kafka.streams.processor.internals.RecordCollectorImpl#send(java.lang.String, java.lang.Object, java.lang.Object, java.lang.Integer, java.lang.Long, org.apache.kafka.common.serialization.Serializer, org.apache.kafka.common.serialization.Serializer) RecordCollector}
 * (for streams)
 * call key serialization before value serialization.
 * </p>
 *
 * @see io.quicksign.kafka.crypto.pairing.serializer.CryptoSerializerPairFactory
 * @see io.quicksign.kafka.crypto.pairing.serdes.CryptoSerdeFactory
 *
 */
package io.quicksign.kafka.crypto.pairing;

