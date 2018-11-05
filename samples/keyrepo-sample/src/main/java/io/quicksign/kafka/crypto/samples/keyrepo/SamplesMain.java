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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.quicksign.kafka.crypto.encryption.CryptoAlgorithm;
import io.quicksign.kafka.crypto.encryption.KeyProvider;
import io.quicksign.kafka.crypto.keyrepository.RepositoryBasedKeyProvider;
import io.quicksign.kafka.crypto.keyrepository.RepositoryBasedKeyReferenceExtractor;
import io.quicksign.kafka.crypto.pairing.keyextractor.KeyReferenceExtractor;

public class SamplesMain {


    public static void main(String... args) {

        // tag::main[]

        // 1- SETUP

        // key provider backed by a simple in memory key repository
        KeyProvider keyProvider = new RepositoryBasedKeyProvider(new SampleKeyRepository(), new SampleKeyNameObfuscator());

        // We create an encryption keyref for each record's key.
        // Two records with the same record key have the same encryption key ref.
        KeyReferenceExtractor keyReferenceExtractor = new RepositoryBasedKeyReferenceExtractor(new SampleKeyNameExtractor(), new SampleKeyNameObfuscator());

        // The payload is encrypted using AES
        CryptoAlgorithm cryptoAlgorithm = new AesGcmNoPaddingCryptoAlgorithm();


        // 2- RUN

        ExecutorService executorService = Executors.newFixedThreadPool(3);

        // the producer encrypts the message
        executorService.submit(new SampleProducer(keyProvider, keyReferenceExtractor, cryptoAlgorithm));

        // this consumer reads them but don't decrypt them... so you can see that it can't be read by default
        executorService.submit(new SampleRawConsumer());

        // this consumer reads and decrypts... and dump in clear the payload...
        executorService.submit(new SampleDecryptingConsumer(keyProvider, cryptoAlgorithm));

        // end::main[]
    }
}
