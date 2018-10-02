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

import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.serialization.ExtendedSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CryptoSerializer<T> implements ExtendedSerializer<T> {

    private static final Logger log = LoggerFactory.getLogger(CryptoSerializer.class);


    private final ExtendedSerializer<? super T> rawSerializer;
    private final Encryptor encryptor;
    private final ThreadLocal<byte[]> keyRefHolder;

    public CryptoSerializer(ExtendedSerializer<? super T> rawSerializer, Encryptor encryptor, ThreadLocal<byte[]> keyRefHolder) {
        this.rawSerializer = rawSerializer;
        this.encryptor = encryptor;
        this.keyRefHolder = keyRefHolder;
    }


    @Override
    public byte[] serialize(String topic, Headers headers, T data) {
        byte[] serializedData = rawSerializer.serialize(topic, headers, data);
        if (serializedData == null) {
            return null;
        }
        Header keyReferenceHeader = headers.lastHeader(KEY_REF_HEADER);
        return encrypt(serializedData, keyReferenceHeader == null ? null : keyReferenceHeader.value());
    }


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
