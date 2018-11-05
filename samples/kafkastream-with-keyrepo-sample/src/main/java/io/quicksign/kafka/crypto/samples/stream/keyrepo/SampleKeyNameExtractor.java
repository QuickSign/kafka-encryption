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

import io.quicksign.kafka.crypto.keyrepository.KeyNameExtractor;


/**
 * Simple key name extractor that use the recordKey as the key name.
 * More complex implementation could leverage the key hidden structure
 * or take into account the current time.
 */
public class SampleKeyNameExtractor implements KeyNameExtractor {

    @Override
    public String extractKeyName(String topic, Object recordKey) {
        return "cpt" + recordKey;
    }
}
