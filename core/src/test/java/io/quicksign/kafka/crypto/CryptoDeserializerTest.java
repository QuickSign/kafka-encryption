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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.Deserializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CryptoDeserializerTest {

    @Mock
    Decryptor decryptor;

    @Mock
    Deserializer<String> rawDeserializer;

    @InjectMocks
    CryptoDeserializer<String> cryptoDeserializer;

    @Test
    public void testDeserializeWhenKeyRefIsSet() {
        byte[] encoded = "encoded".getBytes(StandardCharsets.UTF_8);
        byte[] keyRef = "keyref1".getBytes(StandardCharsets.UTF_8);

        ByteBuffer encodedValue = ByteBuffer.allocate(KafkaCryptoConstants.ENCRYPTED_PREFIX.length + Integer.BYTES + keyRef.length + encoded.length);
        encodedValue.put(KafkaCryptoConstants.ENCRYPTED_PREFIX);
        encodedValue.putInt(keyRef.length);
        encodedValue.put(keyRef);
        encodedValue.put(encoded);


        given(decryptor.decrypt(encoded, keyRef)).willReturn("decoded".getBytes(StandardCharsets.UTF_8));
        given(rawDeserializer.deserialize("topic1", new RecordHeaders(), "decoded".getBytes(StandardCharsets.UTF_8))).willReturn("deserialized value");

        RecordHeaders recordHeaders = new RecordHeaders();
        String value = cryptoDeserializer.deserialize("topic1", recordHeaders, encodedValue.array());

        assertThat(value).isEqualTo("deserialized value");

        assertThat(recordHeaders.lastHeader(KafkaCryptoConstants.KEY_REF_HEADER).value()).isEqualTo(keyRef);
    }

    @Test
    public void testDeserializeWhenKeyRefIsNotSet() {
        Headers headers = new RecordHeaders();

        byte[] clearValue = "clearValue".getBytes(StandardCharsets.UTF_8);

        ByteBuffer rawValue = ByteBuffer.allocate(KafkaCryptoConstants.ENCRYPTED_PREFIX.length + Integer.BYTES + clearValue.length);
        rawValue.put(KafkaCryptoConstants.ENCRYPTED_PREFIX);
        rawValue.putInt(0);
        rawValue.put(clearValue);

        given(rawDeserializer.deserialize("topic1", headers, clearValue)).willReturn("deserialized value");

        String value = cryptoDeserializer.deserialize("topic1", headers, rawValue.array());

        assertThat(value).isEqualTo("deserialized value");
        assertThat(headers.lastHeader(KafkaCryptoConstants.KEY_REF_HEADER).value()).isNull();
        verifyZeroInteractions(decryptor);
    }

    @Test
    public void testDeserializeWhenNoEncryptionStructure() {
        Headers headers = new RecordHeaders();

        byte[] clearValue = "clearValue".getBytes(StandardCharsets.UTF_8);

        given(rawDeserializer.deserialize("topic1", headers, clearValue)).willReturn("deserialized value");

        String value = cryptoDeserializer.deserialize("topic1", headers, clearValue);

        assertThat(value).isEqualTo("deserialized value");
        assertThat(headers.lastHeader(KafkaCryptoConstants.KEY_REF_HEADER).value()).isNull();
        verifyZeroInteractions(decryptor);
    }


}
