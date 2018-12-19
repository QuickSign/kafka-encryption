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
package io.quicksign.kafka.crypto.pairing.serdes;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import io.quicksign.kafka.crypto.CryptoDeserializer;
import io.quicksign.kafka.crypto.CryptoSerializer;
import io.quicksign.kafka.crypto.Decryptor;
import io.quicksign.kafka.crypto.Encryptor;
import io.quicksign.kafka.crypto.pairing.internal.CryptoAwareSerializerWrapper;
import io.quicksign.kafka.crypto.pairing.keyextractor.KeyReferenceExtractor;

/**
 * Factory for pairing 2 serde using encryption.
 * <ul>
 * <li>The serializer of the keySerde will be wrapped to call the {@link KeyReferenceExtractor}</li>
 * <li>The serializer of the valueSerde will be wrapped into a {@link CryptoSerializer}</li>
 * <li>The deserializer of the valueSerde will be wrapped into a {@link CryptoDeserializer}</li>
 * </ul>
 * The keyref extracted by the wrapped key serializer will be shared with the wrapped value serializer using a {@link ThreadLocal}
 *
 * @See io.quicksign.kafka.crypto.pairing.internal.CryptoAwareSerializerWrapper
 */
public class CryptoSerdeFactory implements SerdeFactory {

    private final Encryptor encryptor;
    private final Decryptor decryptor;
    private final KeyReferenceExtractor keyReferenceExtractor;

    /**
     * @param encryptor             used for value encryption
     * @param decryptor             used for value decryption
     * @param keyReferenceExtractor used to
     */
    public CryptoSerdeFactory(Encryptor encryptor, Decryptor decryptor, KeyReferenceExtractor keyReferenceExtractor) {

        this.encryptor = encryptor;
        this.decryptor = decryptor;
        this.keyReferenceExtractor = keyReferenceExtractor;
    }

    public <T> Serde<T> buildFrom(Serde<T> rawSerde) {
        return buildFrom(rawSerde, null);
    }

    private <T> Serde<T> buildFrom(Serde<T> rawSerde, ThreadLocal<byte[]> keyRefHolder) {
        return Serdes.serdeFrom(new CryptoSerializer<>(rawSerde.serializer(), encryptor, keyRefHolder),
                new CryptoDeserializer<>(rawSerde.deserializer(), decryptor));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <K, V> SerdesPair<K, V> buildSerdesPair(Serde<K> keySerde, Serde<V> valueSerde) {
        ThreadLocal<byte[]> keyRefHolder = new ThreadLocal<>();
        Serde<K> newKeySerde = Serdes.serdeFrom(
                new CryptoAwareSerializerWrapper<K>(keySerde.serializer(), keyReferenceExtractor, keyRefHolder),
                keySerde.deserializer());
        Serde<V> newValueSerde = buildFrom(valueSerde, keyRefHolder);

        return new SerdesPair<>(newKeySerde, newValueSerde);
    }

    /**
     * used when the keyref can be deducted directly from the value
     *
     * @param valueSerde
     * @param <V>
     * @return
     */
    @Override
    public <V> Serde<V> buildSelfCryptoAwareSerde(Serde<V> valueSerde) {
        ThreadLocal<byte[]> keyRefHolder = new ThreadLocal<>();
        Serde<V> cryptoSerde = buildFrom(valueSerde, keyRefHolder);
        Serde<V> selfAwareSerde = Serdes.serdeFrom(
                new CryptoAwareSerializerWrapper<V>(cryptoSerde.serializer(), keyReferenceExtractor, keyRefHolder),
                cryptoSerde.deserializer());
        return selfAwareSerde;
    }
}
