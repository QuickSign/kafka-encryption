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

/**
 * Interface to handle cryptographic algorithm
 *
 * @see DefaultDecryptor
 * @see DefaultEncryptor
 */
public interface CryptoAlgorithm {

    /**
     * Encrypt the data using the provided key
     *
     * @param data message to be encrypted
     * @param key encryption key
     * @return encrypted message
     * @throws Exception
     */
    byte[] encrypt(byte[] data, byte[] key) throws Exception;

    /**
     * Decrypt the data using the provided key
     *
     * @param encryptedData message to be decrypted
     * @param key encryption key
     * @return decrypted message
     * @throws Exception
     */
    byte[] decrypt(byte[] encryptedData, byte[] key) throws Exception;
}
