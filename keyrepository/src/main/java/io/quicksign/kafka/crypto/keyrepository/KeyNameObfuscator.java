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

/**
 * <p>Obfuscate the keyName to obtain the keyRef. Obfuscation has to be reversible.</p>
 *
 * <p>Calling several time {@link #obfuscate(String)}</p> with the same keyName may give
 * different results. It is recommend to not let appear in the Kafka the same key reference
 * in several records</p>
 */
public interface KeyNameObfuscator {

    byte[] obfuscate(String keyName);

    String unObfuscate(byte[] keyref);
}
