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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DefaultDecryptorTest {

    @Mock
    KeyProvider keyProvider;

    @Mock
    CryptoAlgorithm encryptionAlgorithm;

    @InjectMocks
    DefaultDecryptor defaultDecryptor;

    @Test
    public void testDecrypt() throws Exception {
        byte[] keyRef = "keyref".getBytes(StandardCharsets.UTF_8);
        byte[] key = "key".getBytes(StandardCharsets.UTF_8);
        byte[] clearData = "clear data".getBytes(StandardCharsets.UTF_8);
        byte[] encodedData = "encoded data".getBytes(StandardCharsets.UTF_8);

        given(keyProvider.getKey(keyRef)).willReturn(Optional.of(key));
        given(encryptionAlgorithm.decrypt(encodedData, key)).willReturn(clearData);

        byte[] res = defaultDecryptor.decrypt(encodedData, keyRef);
        assertThat(res).isEqualTo(clearData);

    }
}
