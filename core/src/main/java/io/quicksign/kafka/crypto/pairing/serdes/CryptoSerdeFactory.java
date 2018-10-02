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

import org.apache.kafka.common.serialization.ExtendedDeserializer;
import org.apache.kafka.common.serialization.ExtendedSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

import io.quicksign.kafka.crypto.CryptoDeserializer;
import io.quicksign.kafka.crypto.CryptoSerializer;
import io.quicksign.kafka.crypto.Decryptor;
import io.quicksign.kafka.crypto.Encryptor;
import io.quicksign.kafka.crypto.pairing.internal.CryptoAwareSerializerWrapper;
import io.quicksign.kafka.crypto.pairing.keyextractor.KeyReferenceExtractor;

public class CryptoSerdeFactory implements SerdeFactory {

    private final Encryptor encryptor;
    private final Decryptor decryptor;
    private final KeyReferenceExtractor keyReferenceExtractor;

    public CryptoSerdeFactory(Encryptor encryptor, Decryptor decryptor, KeyReferenceExtractor keyReferenceExtractor) {

        this.encryptor = encryptor;
        this.decryptor = decryptor;
        this.keyReferenceExtractor = keyReferenceExtractor;
    }

    public <T> Serde<T> buildFrom(Serde<T> rawSerde) {
        return buildFrom(rawSerde, null);
    }

    private <T> Serde<T> buildFrom(Serde<T> rawSerde, ThreadLocal<byte[]> keyRefHolder) {
        ExtendedDeserializer<T> rawExtendedDeserializer = ExtendedDeserializer.Wrapper.ensureExtended(rawSerde.deserializer());
        ExtendedSerializer<T> rawExtendedSerializer = ExtendedSerializer.Wrapper.ensureExtended(rawSerde.serializer());

        return Serdes.serdeFrom(new CryptoSerializer<>(rawExtendedSerializer, encryptor, keyRefHolder),
                new CryptoDeserializer<>(rawExtendedDeserializer, decryptor));
    }

    /**
     * warning: the pair has to be used together
     *
     * @param keySerde
     * @param valueSerde
     * @param <K>
     * @param <V>
     * @return
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
