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
package io.quicksign.kafka.crypto.samples.generatedkey;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import io.quicksign.kafka.crypto.encryption.CryptoAlgorithm;

public class AesGcmNoPaddingCryptoAlgorithm implements CryptoAlgorithm {

    private static final String KEY_SPEC = "AES";
    private static final String ALGO_TRANSFORMATION_STRING = "AES/GCM/NoPadding";
    private static int IV_SIZE = 96;
    private static int TAG_BIT_LENGTH = 128;
    private static String TAG = "sample";


    private final SecureRandom secureRandom;

    public AesGcmNoPaddingCryptoAlgorithm() {
        try {
            this.secureRandom = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("unable to init secureRandom", e);
        }
    }

    @Override
    public byte[] encrypt(byte[] data, byte[] key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, KEY_SPEC);
        byte[] iv = new byte[IV_SIZE];
        secureRandom.nextBytes(iv);
        GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
        Cipher cipher = Cipher.getInstance(ALGO_TRANSFORMATION_STRING);
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParamSpec, secureRandom);
        cipher.updateAAD(TAG.getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        baos.write(iv);

        try (CipherOutputStream cipherOutputStream = new CipherOutputStream(baos, cipher)) {
            cipherOutputStream.write(data);
        }

        return baos.toByteArray();
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, byte[] key) throws Exception {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, KEY_SPEC);
        ByteBuffer byteBuffer = ByteBuffer.wrap(encryptedData);
        byte[] iv = new byte[IV_SIZE];
        byteBuffer.get(iv);
        GCMParameterSpec gcmParamSpec = new GCMParameterSpec(TAG_BIT_LENGTH, iv);
        Cipher cipher = Cipher.getInstance(ALGO_TRANSFORMATION_STRING);
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParamSpec, secureRandom);
        cipher.updateAAD(TAG.getBytes(StandardCharsets.UTF_8));

        byte[] encryptedPayload = new byte[byteBuffer.remaining()];
        byteBuffer.get(encryptedPayload);

        return cipher.doFinal(encryptedPayload);
    }
}
