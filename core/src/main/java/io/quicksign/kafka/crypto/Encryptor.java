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
package io.quicksign.kafka.crypto;

/**
 * Interface for encryption. It encapsulates the retriaval of the encryption key using the key reference
 * and the encryption of the data
 */
public interface Encryptor {

    /**
     * Encrypt the data.
     * It will return null if encryption fails or if the key associated to the key reference
     * can not be found.
     *
     * @param value value to be encrypted
     * @param keyRef reference of the key
     * @return the encrypted value or {@code null} if the encryption fails
     */
    byte[] encrypt(byte[] value, byte[] keyRef);
}
