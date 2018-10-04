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

/**
 * A factory to pair 2 {@link org.apache.kafka.common.serialization.Serde Serde}
 *
 * Main usage is for Kafka Streams
 *
 */
public interface SerdeFactory {

    /**
     * Pair the keySerde with the valueSerde
     *
     * @param keySerde
     * @param valueSerde
     * @param <K>
     * @param <V>
     * @return
     */
    <K, V> SerdesPair<K, V> buildSerdesPair(Serde<K> keySerde, Serde<V> valueSerde);

    <V> Serde<V> buildSelfCryptoAwareSerde(Serde<V> valueSerde);
}
