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
package io.quicksign.kafka.crypto.keyrepository;

import io.quicksign.kafka.crypto.pairing.keyextractor.KeyReferenceExtractor;


/**
 * KeyReference used when the key to use is stored on a repository, and the key to use can be computed from the record key
 *
 * @see KeyNameExtractor
 * @see KeyNameObfuscator
 */
// TODO: change the name, it is repo agnostic.
public class RepositoryBasedKeyReferenceExtractor implements KeyReferenceExtractor {

    private final KeyNameExtractor keyNameExtractor;
    private final KeyNameObfuscator keyNameObfuscator;

    /**
     *
     * @param keyNameExtractor used to compute the keyName
     * @param keyNameObfuscator used to obfuscate the keyName
     */
    public RepositoryBasedKeyReferenceExtractor(KeyNameExtractor keyNameExtractor, KeyNameObfuscator keyNameObfuscator) {

        this.keyNameExtractor = keyNameExtractor;
        this.keyNameObfuscator = keyNameObfuscator;
    }


    @Override
    public byte[] extractKeyReference(String topic, Object key) {
        String keyName = keyNameExtractor.extractKeyName(topic, key);
        return keyName == null ? null : keyNameObfuscator.obfuscate(keyName);
    }
}
