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
package io.quicksign.kafka.crypto;

import static io.quicksign.kafka.crypto.KafkaCryptoConstants.ENCRYPTED_PREFIX;
import static io.quicksign.kafka.crypto.KafkaCryptoConstants.KEY_REF_HEADER;

import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.ExtendedSerializer;
import org.apache.kafka.common.serialization.Serializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>Serializer for encrypted</p>
 *
 * <p>Data is first serialized with the underlying serializer</p>
 *
 * <p>If a key reference is found either in the Kafka Header {@link KafkaCryptoConstants#KEY_REF_HEADER},
 * or in the {@link ThreadLocal} keyRefHolder, then the serialized data is encrypted using the {@link Encryptor}.
 * If data has been encrypted, the result of the serialization is a byte array having the following structure:
 * <pre>magic_bytes({@link KafkaCryptoConstants#ENCRYPTED_PREFIX})(6 bytes)|keyref.length(4 bytes)|keyref|encrypted_data</pre>
 * </p>
 *
 * <p>If the result of encryption is null, then the result of serialization will be null.</p>
 *
 * <p>If no key reference was found, the result of serialization will be directly the output of the underlying Serializer.</p>
 *
 *
 * <p>Please note that this Serializer has no default constructor, so it can not be configured via properties.
 * So if you want to use it into a Producer, you will have to use {@link org.apache.kafka.clients.producer.KafkaProducer#KafkaProducer(Properties, Serializer, Serializer)}
 * or {@link org.apache.kafka.clients.producer.KafkaProducer#KafkaProducer(Map, Serializer, Serializer)} with your
 * instance of CryptoDeserializer
 * </p>
 *
 * @param <T>
 */
public class CryptoSerializer<T> implements ExtendedSerializer<T> {

    private static final Logger log = LoggerFactory.getLogger(CryptoSerializer.class);


    private final ExtendedSerializer<? super T> rawSerializer;
    private final Encryptor encryptor;
    private final ThreadLocal<byte[]> keyRefHolder;

    /**
     * @param rawSerializer Serializer to serialize data before encryption
     * @param encryptor     {@link Encryptor} to encrypt data
     * @param keyRefHolder  {@link ThreadLocal} used to communicate the key reference when using Kafka Stream (unused for regular Kafka Producer)
     */
    public CryptoSerializer(ExtendedSerializer<? super T> rawSerializer, Encryptor encryptor, ThreadLocal<byte[]> keyRefHolder) {
        this.rawSerializer = rawSerializer;
        this.encryptor = encryptor;
        this.keyRefHolder = keyRefHolder;
    }


    /**
     * serialize data with encryption (if needed). Key reference will be looked for exclusively in the Kafka Header
     * {@link KafkaCryptoConstants#KEY_REF_HEADER}
     *
     * @param topic
     * @param headers
     * @param data
     * @return
     */
    @Override
    public byte[] serialize(String topic, Headers headers, T data) {
        byte[] serializedData = rawSerializer.serialize(topic, headers, data);
        if (serializedData == null) {
            return null;
        }
        Header keyReferenceHeader = headers.lastHeader(KEY_REF_HEADER);
        return encrypt(serializedData, keyReferenceHeader == null ? null : keyReferenceHeader.value());
    }


    /**
     * serialize data with encryption (if needed). Key reference will be looked for into the {@link #keyRefHolder}
     *
     * @param topic
     * @param data
     * @return
     */
    @Override
    public byte[] serialize(String topic, T data) {
        byte[] serializedData = rawSerializer.serialize(topic, data);
        if (serializedData == null) {
            return null;
        }
        return encrypt(serializedData, keyRefHolder == null ? null : keyRefHolder.get());
    }

    private byte[] encrypt(byte[] serializedData, byte[] keyref) {
        if (keyref == null) {
            log.debug("keyref header not defined or null, we will send data unencrypted");
            return serializedData;
        }
        byte[] encryptedData = encryptor.encrypt(serializedData, keyref);
        if (encryptedData == null) {
            return null;
        }
        return ByteBuffer.allocate(ENCRYPTED_PREFIX.length + Integer.BYTES + keyref.length + encryptedData.length)
                .put(ENCRYPTED_PREFIX)
                .putInt(keyref.length)
                .put(keyref)
                .put(encryptedData)
                .array();
    }


    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {

        rawSerializer.configure(configs, isKey);
    }

    @Override
    public void close() {
        rawSerializer.close();
    }
}
