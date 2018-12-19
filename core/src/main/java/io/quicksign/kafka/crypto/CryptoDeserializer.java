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

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quicksign.kafka.crypto.utils.ArrayUtils;

/**
 * <p>Deserializer for encrypted data</p>
 *
 * <p>If the data to deserialize starts with the magic bytes {@link KafkaCryptoConstants#ENCRYPTED_PREFIX},
 * then the data is decrypted using the {@link Decryptor}.The result is then deserialized using the underlying Deserializer.
 * If the data where not successfully decrypted, the result of the deserialization will be {@code null}
 * </p>
 *
 * <p>If the data to deserialize does not starts with the magic bytes, it is directly deserialized using the underlying Deserializer
 * </p>
 *
 * <p>Please note that this Deserializer has no default constructor, so it can not be configured via properties.
 * So if you want to use it into a Consumer, you will have to use {@link org.apache.kafka.clients.consumer.KafkaConsumer#KafkaConsumer(Properties, Deserializer, Deserializer)}
 * or {@link org.apache.kafka.clients.consumer.KafkaConsumer#KafkaConsumer(Map, Deserializer, Deserializer)} with your
 * instance of CryptoDeserializer
 * </p>
 *
 * @param <T>
 * @see CryptoSerializer
 * @see Decryptor
 */
public class CryptoDeserializer<T> implements Deserializer<T> {

    private static final Logger log = LoggerFactory.getLogger(CryptoDeserializer.class);

    private final Deserializer<? extends T> rawDeserializer;
    private final Decryptor decryptor;

    /**
     * @param rawDeserializer deserializer to deserialize clear data
     * @param decryptor       Decryptor used to decrypt the data
     */
    public CryptoDeserializer(Deserializer<? extends T> rawDeserializer, Decryptor decryptor) {

        this.rawDeserializer = rawDeserializer;
        this.decryptor = decryptor;
    }


    /**
     * deserialize the data (with decryption if needed)
     * The keyref used to deserialize the data will be added to the header {@link KafkaCryptoConstants#KEY_REF_HEADER} (may be {@code null})
     *
     * @param topic
     * @param headers they will be enriched with header {@link KafkaCryptoConstants#KEY_REF_HEADER}
     * @param data
     * @return the deserialized data
     */
    @Override
    public T deserialize(String topic, Headers headers, byte[] data) {
        if (data == null) {
            return null;
        }

        DecryptedDataWithKeyRef decryptedDataWithKeyRef = decrypt(data);


        T deserializedValue = rawDeserializer.deserialize(topic, headers, decryptedDataWithKeyRef.decryptedData);

        headers.add(KEY_REF_HEADER, decryptedDataWithKeyRef.keyRef);

        return deserializedValue;
    }

    @Override
    public void configure(Map<String, ?> configs, boolean isKey) {
        this.rawDeserializer.configure(configs, isKey);
    }

    /**
     * deserialize the data (with decryption if needed)
     * It is equivalent to:
     * <pre>{@code
     *  deserialize(topic, new RecordHeaders(), data);
     * }</pre>
     *
     * @param topic
     * @param data
     * @return
     * @see #deserialize(String, Headers, byte[])
     */
    @Override
    public T deserialize(String topic, byte[] data) {
        return deserialize(topic, new RecordHeaders(), data);
    }


    private DecryptedDataWithKeyRef decrypt(byte[] data) {
        if (ArrayUtils.startWith(data, ENCRYPTED_PREFIX)) {
            ByteBuffer byteBuffer = ByteBuffer.wrap(data,
                    ENCRYPTED_PREFIX.length, data.length - ENCRYPTED_PREFIX.length);
            int keyRefLength = byteBuffer.getInt();
            byte[] decryptedData;
            byte[] keyRef = null;
            if (keyRefLength == 0) {
                log.debug("not key ref, data are not encrypted");
                decryptedData = new byte[byteBuffer.remaining()];
                byteBuffer.get(decryptedData);
            }
            else {
                keyRef = new byte[keyRefLength];
                byteBuffer.get(keyRef);
                byte[] encryptedData = new byte[byteBuffer.remaining()];
                byteBuffer.get(encryptedData);
                decryptedData = decryptor.decrypt(encryptedData, keyRef);

            }
            return new DecryptedDataWithKeyRef(keyRef, decryptedData);
        }
        else {
            return new DecryptedDataWithKeyRef(null, data);
        }

    }


    @Override
    public void close() {
        this.rawDeserializer.close();
    }


    private static class DecryptedDataWithKeyRef {

        private final byte[] keyRef;
        private final byte[] decryptedData;

        DecryptedDataWithKeyRef(byte[] keyRef, byte[] decryptedData) {

            this.keyRef = keyRef;
            this.decryptedData = decryptedData;
        }

    }
}
