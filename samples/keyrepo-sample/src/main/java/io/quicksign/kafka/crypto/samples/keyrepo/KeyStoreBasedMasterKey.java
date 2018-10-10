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

import io.quicksign.kafka.crypto.generatedkey.MasterKeyEncryption;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyStore;

/**
 * A simple implementation that reads the master key (used to encrypt the symetric encryption keys) from a local keystore.
 */
public class KeyStoreBasedMasterKey implements MasterKeyEncryption {

    private final Key masterKey;
    private final AesGcmNoPaddingCryptoAlgorithm aesGcmNoPaddingCryptoAlgorithm;


    public KeyStoreBasedMasterKey(File masterKeyFile, String masterKeyPass, String alias, AesGcmNoPaddingCryptoAlgorithm aesGcmNoPaddingCryptoAlgorithm){
        this.aesGcmNoPaddingCryptoAlgorithm = aesGcmNoPaddingCryptoAlgorithm;
        try(InputStream keystoreStream = new FileInputStream(masterKeyFile)) {
            KeyStore keystore = KeyStore.getInstance("PKCS12");
            keystore.load(keystoreStream, masterKeyPass.toCharArray());
            if (!keystore.containsAlias(alias)) {
                throw new RuntimeException("Alias for key not found");
            }
            masterKey = keystore.getKey(alias, masterKeyPass.toCharArray());

        } catch (Exception e){
            throw new RuntimeException(e);
        }
    }



    @Override
    public byte[] encryptKey(byte[] key) {
        try {
            return aesGcmNoPaddingCryptoAlgorithm.encrypt(key, masterKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] decryptKey(byte[] encryptedKey) {
        try {
            return aesGcmNoPaddingCryptoAlgorithm.decrypt(encryptedKey, masterKey.getEncoded());
        } catch (Exception e) {
            return null;
        }
    }
}
