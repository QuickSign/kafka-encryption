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
package io.quicksign.kafka.crypto.samples.stream.keyrepo;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.quicksign.kafka.crypto.keyrepository.KeyRepository;

public class KeyStoreBasedKeyRepository implements KeyRepository {

    private final KeyStore keyStore;
    private final ConcurrentMap<String, Optional<Key>> keyCache = new ConcurrentHashMap<>();
    private final String keyPass;

    public KeyStoreBasedKeyRepository(File masterKeyFile, String keyPass) {
        this.keyPass = keyPass;
        try (InputStream keystoreStream = new FileInputStream(masterKeyFile)) {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(keystoreStream, keyPass.toCharArray());

            this.keyStore = keystore;
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public Optional<byte[]> getKey(String keyName) {
        Optional<Key> maybeKey = keyCache.computeIfAbsent(keyName, this::loadKey);

        return maybeKey.map(Key::getEncoded);
    }

    private synchronized Optional<Key> loadKey(String keyName) {
        try {
            if (keyStore.containsAlias(keyName)) {
                return Optional.ofNullable(keyStore.getKey(keyName, keyPass.toCharArray()));
            }
            else {
                return Optional.empty();
            }
        }
        catch (Exception e) {
            throw new RuntimeException("error while loading key", e);
        }
    }
}
