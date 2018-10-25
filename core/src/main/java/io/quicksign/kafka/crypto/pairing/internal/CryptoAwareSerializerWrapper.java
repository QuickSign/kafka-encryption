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
package io.quicksign.kafka.crypto.pairing.internal;

import java.util.Map;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.ExtendedSerializer;
import org.apache.kafka.common.serialization.Serializer;

import io.quicksign.kafka.crypto.KafkaCryptoConstants;
import io.quicksign.kafka.crypto.pairing.keyextractor.KeyReferenceExtractor;

/**
 * A wrapper for {@link Serializer}.
 * It will call the {@link KeyReferenceExtractor} and then serialize the data using the underlying serializer
 *
 * @param <T>
 */
public class CryptoAwareSerializerWrapper<T> implements ExtendedSerializer<T> {

    private final ExtendedSerializer<T> rawSerializer;
    private final KeyReferenceExtractor keyReferenceExtractor;
    private final ThreadLocal<byte[]> keyRefHolder;

    /**
     * @param rawSerializer         the Serializer to use
     * @param keyReferenceExtractor the KeyReferenceExtractor to use
     * @param keyRefHolder          the ThreadLocal to share the keyref (only used in the context of a Kafka Stream)
     */
    public CryptoAwareSerializerWrapper(Serializer<T> rawSerializer, KeyReferenceExtractor keyReferenceExtractor, ThreadLocal<byte[]> keyRefHolder) {
        this.rawSerializer = ExtendedSerializer.Wrapper.ensureExtended(rawSerializer);
        this.keyReferenceExtractor = keyReferenceExtractor;
        this.keyRefHolder = keyRefHolder;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.rawSerializer.configure(configs, isKey);
    }

    /**
     * Call the KeyReferenceExtractor with the topic and the data and set the computed value into the ThreadLocal reference holder.
     * <p>
     * This method is called in the context of a kafka stream and not in the Kafka Producer
     *
     * @param topic
     * @param data
     * @return the result of the underlying serializer
     */
    @Override
    public byte[] serialize(String topic, T data) {
        this.keyRefHolder.set(keyReferenceExtractor.extractKeyReference(topic, data));

        return this.rawSerializer.serialize(topic, data);
    }

    @Override
    public void close() {
        this.rawSerializer.close();
    }

    /**
     * Call the KeyReferenceExtractor with the topic and the data and set the result in the kafka header {@link KafkaCryptoConstants#KEY_REF_HEADER}
     * <p>
     * This method is called by the Kafka Producer
     *
     * @param topic
     * @param headers
     * @param data
     * @return the result of the underlying serializer
     */
    @Override
    public byte[] serialize(String topic, Headers headers, T data) {
        headers.add(KafkaCryptoConstants.KEY_REF_HEADER, keyReferenceExtractor.extractKeyReference(topic, data));
        return this.rawSerializer.serialize(topic, headers, data);
    }
}
