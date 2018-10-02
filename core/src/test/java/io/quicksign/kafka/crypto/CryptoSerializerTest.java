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

import static io.quicksign.kafka.crypto.KafkaCryptoConstants.ENCRYPTED_PREFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyZeroInteractions;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

import org.apache.kafka.common.header.Headers;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.apache.kafka.common.serialization.ExtendedSerializer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class CryptoSerializerTest {

    @Mock
    ExtendedSerializer<String> rawSerializer;

    @Mock
    Encryptor encryptor;

    @Mock
    ThreadLocal<byte[]> keyRefHolder;

    @InjectMocks
    CryptoSerializer<String> cryptoSerializer;

    @Test
    public void testSerializeWhenKeyRefHeaderIsSet() {
        final String keyRef = "org1";
        final int keyRefSize = toByteArray(keyRef).length;

        final String encoded = "encodedValue";
        final int encodedSize = toByteArray(encoded).length;

        Headers headers = new RecordHeaders().add(KafkaCryptoConstants.KEY_REF_HEADER, toByteArray(keyRef));
        given(rawSerializer.serialize("topic1", headers, "final value"))
                .willReturn("clear serialized value".getBytes(StandardCharsets.UTF_8));
        given(encryptor.encrypt("clear serialized value".getBytes(StandardCharsets.UTF_8), toByteArray(keyRef)))
                .willReturn(toByteArray(encoded));

        byte[] result = cryptoSerializer.serialize("topic1", headers, "final value");

        assertThat(result).hasSize(ENCRYPTED_PREFIX.length + Integer.BYTES + keyRefSize + encodedSize);
        ByteBuffer byteBuffer = ByteBuffer.wrap(result);
        byte[] prefix = new byte[ENCRYPTED_PREFIX.length];
        byteBuffer.get(prefix);
        assertThat(prefix).isEqualTo(ENCRYPTED_PREFIX);
        assertThat(byteBuffer.getInt()).isEqualTo(keyRefSize);
        byte[] resultKeyRef = new byte[keyRefSize];
        byteBuffer.get(resultKeyRef);
        assertThat(resultKeyRef).isEqualTo(toByteArray(keyRef));
        byte[] resultPayload = new byte[encodedSize];
        byteBuffer.get(resultPayload);
        assertThat(resultPayload).isEqualTo(toByteArray(encoded));

    }

    @Test
    public void testSerializeWhenKeyRefHeaderIsNotSet() {
        final String clearPayload = "clear serialized value";
        final int clearPayloadSize = toByteArray(clearPayload).length;

        Headers headers = new RecordHeaders();
        given(rawSerializer.serialize("topic1", headers, "final value"))
                .willReturn(toByteArray(clearPayload));

        byte[] result = cryptoSerializer.serialize("topic1", headers, "final value");

        assertThat(result).isEqualTo("clear serialized value".getBytes(StandardCharsets.UTF_8));

        verifyZeroInteractions(encryptor);
    }

    @Test
    public void testSerializeWhenKeyRefHeaderIsSetToNull() {
        final String clearPayload = "clear serialized value";
        final int clearPayloadSize = toByteArray(clearPayload).length;
        Headers headers = new RecordHeaders().add(KafkaCryptoConstants.KEY_REF_HEADER, null);
        given(rawSerializer.serialize("topic1", headers, "final value"))
                .willReturn(toByteArray(clearPayload));

        byte[] result = cryptoSerializer.serialize("topic1", headers, "final value");

        assertThat(result).isEqualTo("clear serialized value".getBytes(StandardCharsets.UTF_8));
        verifyZeroInteractions(encryptor);
    }

    @Test
    public void testSerializeWithThreadLocal() {
        final String keyRef = "org1";
        final int keyRefSize = toByteArray(keyRef).length;

        final String encoded = "encodedValue";
        final int encodedSize = toByteArray(encoded).length;

        given(keyRefHolder.get()).willReturn(toByteArray(keyRef));

        given(rawSerializer.serialize("topic1", "final value"))
                .willReturn("clear serialized value".getBytes(StandardCharsets.UTF_8));
        given(encryptor.encrypt("clear serialized value".getBytes(StandardCharsets.UTF_8), toByteArray(keyRef)))
                .willReturn(toByteArray(encoded));

        byte[] result = cryptoSerializer.serialize("topic1", "final value");

        assertThat(result).hasSize(ENCRYPTED_PREFIX.length + Integer.BYTES + keyRefSize + encodedSize);
        ByteBuffer byteBuffer = ByteBuffer.wrap(result);
        byte[] prefix = new byte[ENCRYPTED_PREFIX.length];
        byteBuffer.get(prefix);
        assertThat(prefix).isEqualTo(ENCRYPTED_PREFIX);
        assertThat(byteBuffer.getInt()).isEqualTo(keyRefSize);
        byte[] resultKeyRef = new byte[keyRefSize];
        byteBuffer.get(resultKeyRef);
        assertThat(resultKeyRef).isEqualTo(toByteArray(keyRef));
        byte[] resultPayload = new byte[encodedSize];
        byteBuffer.get(resultPayload);
        assertThat(resultPayload).isEqualTo(toByteArray(encoded));

    }

    private byte[] toByteArray(String s) {
        return s.getBytes(StandardCharsets.UTF_8);
    }
}
