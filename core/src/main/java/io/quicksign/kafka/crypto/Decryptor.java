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
 * Interface for decryption. It encapsulates the retriaval of the decryption key using the key reference
 * and the decryption of the data
 */
public interface Decryptor {

    /**
     * Decrypt the data.
     * It will return null if the key can not be retrieve or if decryption fails.
     *
     * @param data   value to be decrypted
     * @param keyRef reference to the decryption key
     * @return the decrypted value or {@code null} if the key cannot be retrieved or if decryption fails
     */
    byte[] decrypt(byte[] data, byte[] keyRef);
}
