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
package io.quicksign.kafka.crypto.samples.keyrepo;

import io.quicksign.kafka.crypto.keyrepository.KeyNameObfuscator;

import java.nio.charset.Charset;
import java.util.Arrays;


public class SampleKeyNameObfuscator implements KeyNameObfuscator {
    @Override
    public byte[] obfuscate(String keyName) {
        return swapFirstAndLast(keyName.getBytes(Charset.forName("UTF-8")));
    }

    @Override
    public String unObfuscate(byte[] keyref) {
        byte[] copy = Arrays.copyOf(keyref, keyref.length);
        return new String(swapFirstAndLast(copy), Charset.forName("UTF-8"));
    }

    // basic, just for the sample
    private byte[] swapFirstAndLast(byte[] keyRef) {
        byte[] copy = Arrays.copyOf(keyRef, keyRef.length);
        byte first = copy[0];
        copy[0] = copy[copy.length - 1];
        copy[copy.length - 1] = first;
        return copy;
    }
}
