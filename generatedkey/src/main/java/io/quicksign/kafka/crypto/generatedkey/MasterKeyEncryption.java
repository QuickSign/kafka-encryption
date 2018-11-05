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
package io.quicksign.kafka.crypto.generatedkey;

/**
 * Encryption to encrypt/decrypt generated keys
 */
public interface MasterKeyEncryption {

    /**
     * encrypt the provided key with a master key
     *
     * @param key the key to be encrypted
     * @return
     */
    byte[] encryptKey(byte[] key);

    /**
     * decrypt the key with a master key
     *
     * @param encryptedKey
     * @return the decrypted key or {@code null} if the key can not be decrypted
     */
    byte[] decryptKey(byte[] encryptedKey);
}
