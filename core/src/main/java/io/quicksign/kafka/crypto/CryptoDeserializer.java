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

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.ExtendedDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quicksign.kafka.crypto.utils.ArrayUtils;

public class CryptoDeserializer<T> implements ExtendedDeserializer<T> {

    private static final Logger log = LoggerFactory.getLogger(CryptoDeserializer.class);

    private final ExtendedDeserializer<? extends T> rawDeserializer;
    private final Decryptor decryptor;

    public CryptoDeserializer(ExtendedDeserializer<? extends T> rawDeserializer, Decryptor decryptor) {

        this.rawDeserializer = rawDeserializer;
        this.decryptor = decryptor;
    }

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
