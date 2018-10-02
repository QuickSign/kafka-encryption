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
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.processor.StateStore;

public class SerdesPair<K, V> {

    private final Serde<K> keySerde;
    private final Serde<V> valueSerde;

    public SerdesPair(Serde<K> keySerde, Serde<V> valueSerde) {

        this.keySerde = keySerde;
        this.valueSerde = valueSerde;
    }

    public Serde<K> getKeySerde() {
        return keySerde;
    }

    public Serde<V> getValueSerde() {
        return valueSerde;
    }

    public Serialized<K, V> toSerialized() {
        return Serialized.with(keySerde, valueSerde);
    }

    public Produced<K, V> toProduced() {
        return Produced.with(keySerde, valueSerde);
    }

    public Consumed<K, V> toConsumed() {
        return Consumed.with(keySerde, valueSerde);
    }


    public <S extends StateStore> Materialized<K, V, S> applyTo(Materialized<K, V, S> materialized) {
        return materialized.withKeySerde(keySerde).withValueSerde(valueSerde);
    }
}
