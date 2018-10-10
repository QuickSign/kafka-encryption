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

import io.quicksign.kafka.crypto.encryption.KeyProvider;
import io.quicksign.kafka.crypto.generatedkey.AES256CryptoKeyGenerator;
import io.quicksign.kafka.crypto.generatedkey.MasterKeyEncryption;
import io.quicksign.kafka.crypto.keyrepository.KeyRepository;
import io.quicksign.kafka.crypto.keyrepository.RepositoryBasedKeyProvider;
import io.quicksign.kafka.crypto.keyrepository.RepositoryBasedKeyReferenceExtractor;
import io.quicksign.kafka.crypto.pairing.keyextractor.KeyReferenceExtractor;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SamplesMain {


    public static void main(String... args) {

        // 1- Prepare the MasterEncryption Service that encrypt the encryption keys.

        // tag::masterkey[]
        MasterKeyEncryption masterKeyEncryption = new KeyStoreBasedMasterKey(
            new File("/tmp/sample.pkcs12"),
            "sample",
            "sample",
            new AesGcmNoPaddingCryptoAlgorithm()
        );
        // end::masterkey[]


        // 2- Prepare the KeyProvider that provides the encryption keys.

        // Use an AES256 key generator
        AES256CryptoKeyGenerator cryptoKeyGenerator = new AES256CryptoKeyGenerator();

        // In our sample, we generate an encryption keyref for each record key
        // and 2 records with the same record key have the same encryption key ref
        // this is not optimal... up to you to leverage info present in your key, topic name or simply current timestamp.
        KeyReferenceExtractor keyReferenceExtractor = new RepositoryBasedKeyReferenceExtractor(new SampleKeyNameExtractor(), new SampleKeyNameObfuscator());

        // Our sample key repository is a basic in memory repo
        KeyRepository keyRepository = new SampleKeyRepository(masterKeyEncryption, cryptoKeyGenerator);

        // The key provider wraps the key repo as it first needs to unobfuscate the keyref.
        KeyProvider keyProvider = new RepositoryBasedKeyProvider(keyRepository, new SampleKeyNameObfuscator());

        // The payload is encrypted using AES
        AesGcmNoPaddingCryptoAlgorithm cryptoAlgorithm = new AesGcmNoPaddingCryptoAlgorithm();


        // 3- start a producer and 2 consumer...

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // the producer encrypt the message
        executorService.submit(new SampleProducer(keyProvider, keyReferenceExtractor, cryptoAlgorithm));

        // this consumer reads them but don't decrypt them... so you can see that it can't be read by default
        executorService.submit(new SampleRawConsumer());

        // this consumer reads and decrypts... and dump in clear the payload...
        executorService.submit(new SampleDecryptingConsumer(keyProvider, cryptoAlgorithm));
    }
}
