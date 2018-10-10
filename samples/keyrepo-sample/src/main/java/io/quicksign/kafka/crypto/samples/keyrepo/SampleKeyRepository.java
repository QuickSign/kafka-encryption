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

import io.quicksign.kafka.crypto.generatedkey.CryptoKeyGenerator;
import io.quicksign.kafka.crypto.generatedkey.MasterKeyEncryption;
import io.quicksign.kafka.crypto.keyrepository.KeyRepository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In a real life scenario your encryption keys are stored encrypted in a persistent storage accessible by
 * the producer and the consumer, you would of course cache them locally in memory to avoid introducing too much
 * latency. In our example, the repository is a simple map.
 */
public class SampleKeyRepository implements KeyRepository {
    private MasterKeyEncryption masterKeyEncryption;
    CryptoKeyGenerator cryptoKeyGenerator;

    private Map<String, byte[]> keyStore = new ConcurrentHashMap<>();


    public SampleKeyRepository(MasterKeyEncryption masterKeyEncryption, CryptoKeyGenerator cryptoKeyGenerator) {
        this.masterKeyEncryption = masterKeyEncryption;
        this.cryptoKeyGenerator = cryptoKeyGenerator;
    }

    @Override
    public Optional<byte[]> getKey(String keyName) {
        byte[] encryptedEncryptionKey = keyStore.get(keyName);

        if (encryptedEncryptionKey != null) {
            return Optional.of(masterKeyEncryption.decryptKey(encryptedEncryptionKey));
        }

        byte[] encryptionKey = cryptoKeyGenerator.generateKey();

        keyStore.put(keyName, masterKeyEncryption.encryptKey(encryptionKey));

        return Optional.of(encryptionKey);
    }
}
