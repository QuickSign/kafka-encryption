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
package io.quicksign.kafka.crypto.pairing.serializer;

import org.apache.kafka.common.serialization.ExtendedSerializer;
import org.apache.kafka.common.serialization.Serializer;

import io.quicksign.kafka.crypto.CryptoSerializer;
import io.quicksign.kafka.crypto.Encryptor;
import io.quicksign.kafka.crypto.pairing.internal.CryptoAwareSerializerWrapper;
import io.quicksign.kafka.crypto.pairing.keyextractor.KeyReferenceExtractor;

/**
 * A factory to pair 2 serializers
 * <ul>
 * <li>the keySerializer is wrapped to call the {@link KeyReferenceExtractor}</li>
 * <li> the valueSerializer is wrapped into a {@link CryptoSerializer} </li>
 * </ul>
 * The keyref extracted by the wrapped key serializer will be shared with the wrapped value serializer using Kafka headers
 */
public class CryptoSerializerPairFactory implements SerializerPairFactory {

    private final Encryptor encryptor;
    private final KeyReferenceExtractor keyReferenceExtractor;

    public CryptoSerializerPairFactory(Encryptor encryptor, KeyReferenceExtractor keyReferenceExtractor) {
        this.encryptor = encryptor;
        this.keyReferenceExtractor = keyReferenceExtractor;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> SerializerPair<K, V> build(Serializer<K> keySerializer, Serializer<V> valueSerializer) {
        Serializer<K> newKeySerializer = new CryptoAwareSerializerWrapper<K>(keySerializer, keyReferenceExtractor, null);
        Serializer<V> newvalueSerializer = new CryptoSerializer<>(ExtendedSerializer.Wrapper.ensureExtended(valueSerializer), encryptor, null);
        return new SerializerPair<>(newKeySerializer, newvalueSerializer);
    }
}
