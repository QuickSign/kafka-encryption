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

public class KafkaCryptoConstants {

    /**
     * header name to handle the key reference
     */
    public static final String KEY_REF_HEADER = "keKeyReference";

    /**
     * "magic" prefix added to all encrypted messages
     */
    public static final byte[] ENCRYPTED_PREFIX = {0x2B, 0x45, 0x2B, 0x1B, 0x2B, 0x46};

    private KafkaCryptoConstants() {
    }
}
