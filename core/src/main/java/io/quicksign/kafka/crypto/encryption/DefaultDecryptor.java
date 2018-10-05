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
package io.quicksign.kafka.crypto.encryption;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.quicksign.kafka.crypto.Decryptor;

/**
 * Default implementation of Decryptor.
 * It uses a {@link KeyProvider} to retrieve the key associated to keyr references.
 * It use a {@link CryptoAlgorithm} to decrypt the data
 *
 */
public class DefaultDecryptor implements Decryptor {

    private static final Logger log = LoggerFactory.getLogger(DefaultDecryptor.class);

    private final KeyProvider keyProvider;
    private final CryptoAlgorithm cryptoAlgorithm;

    public DefaultDecryptor(KeyProvider keyProvider, CryptoAlgorithm cryptoAlgorithm) {

        this.keyProvider = keyProvider;
        this.cryptoAlgorithm = cryptoAlgorithm;
    }


    /**
     *
     * {@inheritDoc}
     *
     */
    @Override
    public byte[] decrypt(byte[] value, byte[] keyRef) {
        //error on key retrieving must stop the world
        Optional<byte[]> maybeKey = keyProvider.getKey(keyRef);
        return maybeKey.map(key -> {
                    try {
                        return cryptoAlgorithm.decrypt(value, key);
                    }
                    catch (Exception e) {
                        log.error("error while decrypting data", e);
                        return null;
                    }
                }
        ).orElse(null);
    }
}
