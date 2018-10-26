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

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import io.quicksign.kafka.crypto.encryption.KeyProvider;
import io.quicksign.kafka.crypto.keyrepository.KeyRepository;
import io.quicksign.kafka.crypto.keyrepository.RepositoryBasedKeyProvider;
import io.quicksign.kafka.crypto.keyrepository.RepositoryBasedKeyReferenceExtractor;
import io.quicksign.kafka.crypto.pairing.keyextractor.KeyReferenceExtractor;

public class SamplesMain {

    public static void main(String... args) {

        // tag::main[]
        KeyRepository fullKeyRepository = new KeyStoreBasedKeyRepository(
                new File("/tmp/samplestream.pkcs12"),
                "sample"
        );

        KeyRepository agency1KeyRepository = new KeyStoreBasedKeyRepository(
                new File("/tmp/samplestream1.pkcs12"),
                "sample"
        );

        KeyRepository agency2KeyRepository = new KeyStoreBasedKeyRepository(
                new File("/tmp/samplestream2.pkcs12"),
                "sample"
        );

        KeyProvider fullKeyProvider = new RepositoryBasedKeyProvider(fullKeyRepository, new SampleKeyNameObfuscator());
        KeyProvider agency1KeyProvider = new RepositoryBasedKeyProvider(agency1KeyRepository, new SampleKeyNameObfuscator());
        KeyProvider agency2KeyProvider = new RepositoryBasedKeyProvider(agency2KeyRepository, new SampleKeyNameObfuscator());

        // We create an encryption keyref for each record's key.
        // Two records with the same record key have the same encryption key ref.
        KeyReferenceExtractor keyReferenceExtractor = new RepositoryBasedKeyReferenceExtractor(new SampleKeyNameExtractor(), new SampleKeyNameObfuscator());


        ExecutorService executorService = Executors.newFixedThreadPool(4);


        SampleProducer sampleProducer = new SampleProducer(fullKeyProvider, keyReferenceExtractor);

        executorService.submit(sampleProducer);

        try {
            Thread.sleep(10000l);
        }
        catch (InterruptedException e) {
            System.exit(0);
        }

        SampleStream fullView = new SampleStream("full", fullKeyProvider, keyReferenceExtractor);
        SampleStream agency1View = new SampleStream("agency1", agency1KeyProvider, keyReferenceExtractor);
        SampleStream agency2View = new SampleStream("agency2", agency2KeyProvider, keyReferenceExtractor);

        executorService.submit(fullView);
        executorService.submit(agency1View);
        executorService.submit(agency2View);

        // end::main[]

    }


}
