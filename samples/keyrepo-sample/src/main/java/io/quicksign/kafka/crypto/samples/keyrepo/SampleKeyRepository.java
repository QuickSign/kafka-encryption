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
package io.quicksign.kafka.crypto.samples.keyrepo;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import io.quicksign.kafka.crypto.generatedkey.AES256CryptoKeyGenerator;
import io.quicksign.kafka.crypto.generatedkey.CryptoKeyGenerator;
import io.quicksign.kafka.crypto.keyrepository.KeyRepository;

/**
 * In a real life scenario your encryption keys could be stored encrypted in a persistent storage accessible by
 * the producer and the consumer, you would of course cache them locally in memory to avoid introducing too much
 * latency. In our example, the repository is a simple map.
 */
public class SampleKeyRepository implements KeyRepository {

    // Use an AES256 key generator
    private CryptoKeyGenerator cryptoKeyGenerator = new AES256CryptoKeyGenerator();
    private Map<String, byte[]> keyStore = new ConcurrentHashMap<>();

    @Override
    public Optional<byte[]> getKey(String keyName) {
        return Optional.of(keyStore.computeIfAbsent(keyName, k -> cryptoKeyGenerator.generateKey()));
    }
}
