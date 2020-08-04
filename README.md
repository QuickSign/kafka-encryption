# kafka-encryption

[![Build Status](https://travis-ci.com/QuickSign/kafka-encryption.svg?branch=master)](https://travis-ci.com/QuickSign/kafka-encryption)

Kafka-encryption is a Java framework that eases the encryption/decryption of Kafka 
record's value at the serializer/deserializer level.

## Design goals

* Support or allow multiple encryption key management strategies (KMS, embedded, per message, rolling windows, per tenant, custom)
* Support for Kafka Streams intermediate topics
* Detect when a payload is not encrypted to skip decryption
* Support for Spring Boot
* Support for Camel

## Customization

This framework exposes some high level Interfaces to let you customize the crypto 
Serializer/Deserializer internals.

This framework is used on our platform. For obvious reason we do not reveal here our custom 
implementations of these interfaces. They would probably be useless to you anyway. 

However, and this is the good news, we provide in our examples some working implementations that 
you can definitely leverage.

## Terminology & basic explanation.

As you explore the code or the examples, you may get confused by the terminology used.

Do not confuse the Kafka `record's key` and the `encryption key` that is used to encrypt the record's value.

You may also get confused by what we call a `key name` and a `key reference`.

A `key name` is in general used to lookup an encryption key in a repository, but it could also be the `encryption key` itself. 

A `key reference` or `key ref` is derived from the `key name`. It can be for example an obfuscated or 
encrypted version of the `key name`. The `key ref` is stored in the record's value as a prefix of the encrypted value. .

## Examples

We provide 3 examples that work out of the box. Do not use their code as is in production (we don't).
Hopefully you can replace some of the implementations provided in the examples with your own.

    TIP: When studying the samples' code, to ease your pain start by studying 
    the SamplesMain and SampleProducer.

### Example 1 - samples/generatedkey-sample : one encryption key per record

This example uses the classic consumer API. It neither relies on the record's key nor on an 
encryption key repository. Instead the `encryption key` is encrypted and transmitted in the record's value. 

As a developer using the framework, in this example we provide 2 custom implementations to support our need.
These implementations are used to construct the CryptoSerializerPairFactory.

Here is roughly what this example demonstrates: 

__Serializer__

* Generates a new `encryption key` for each record
* Encrypts the record's value using the `encryption key` (see AesGcmNoPaddingCryptoAlgorithm).
* Uses the master encryption key (see KeyStoreBasedMasterKey) to encrypt the `encryption key`. The encrypted encryption key is the `key ref`. Note that the master encryption key is stored in a Java KeyStore which is itself protected by a password.

__Deserializer__

* It extracts the `key ref` from the record's value. 
* Uses the master encryption key (see KeyStoreBasedMasterKey) to decrypt the `encryption key` out of the `key ref`.
* Decrypts the record's value using the `encryption key` (see AesGcmNoPaddingCryptoAlgorithm).

### Example 2 - samples/kafkastreams-with-keyrepo-sample : one encryption key per record

This example uses the Kafka Streams API. It creates a KTable, its content is also encrypted.
We use one `encryption key` per record's key.
The `encryption key` is stored in Java KeyStore, it is not transmitted in the record's value. 

As a developer using the framework, in this example we provide 4 custom implementations to support our need.
These implementations are used to construct the CryptoSerializerPairFactory.

Here is roughly what this example demonstrates: 

__Serializer__

* Uses the record's key as the `key name` (see SampleKeyNameExtractor)
* Using the `key name`, looks up the `encryption key` from the KeyStoreBasedKeyRepository  
* Uses the SampleKeyNameObfuscator provided to create the `key ref` by simply swapping some bytes from the `key name`.
* It encrypts the record's value using `encryption key` (see AesGcmNoPaddingCryptoAlgorithm)

__Deserializer__

* Extracts the `key ref` from the record's value
* Uses the SampleKeyNameObfuscator to obtain the `key name` out of the `key ref`
* Looks up the `encryption key` from the KeyStoreBasedKeyRepository using the `key name`   
* Decrypts the record's value using the `encryption key` (see AesGcmNoPaddingCryptoAlgorithm)


### Example 3 - samples/keyrepo-sample : one encryption key per record's key.

This example uses the classic consumer API. There is one `encryption key` per record's key.
The `encryption key` is stored in an in memory encryption key repository, it is not transmitted in
the record's value. 

As a developer using the framework, in this example we provide 4 custom implementations to support our need.
These implementations are used to construct the CryptoSerializerPairFactory.

Here is roughly what this example demonstrates: 

__Serializer__

* Uses the record's key as the `key name` (see SampleKeyNameExtractor)
* Using the `key name`, looks up the `encryption key` from the SampleKeyRepository, a basic in memory encryption key repository.  
* Uses the SampleKeyNameObfuscator provided to create the `key ref` by simply swapping some bytes from the `key name`.
* It encrypts the record's value using `encryption key` (see AesGcmNoPaddingCryptoAlgorithm)

__Deserializer__

* Extracts the `key ref` from the record's value
* Uses the SampleKeyNameObfuscator to obtain the `key name` out of the `key ref`
* Looks up the `encryption key` from the SampleKeyRepository using the `key name`   
* Decrypts the record's value using the `encryption key` (see AesGcmNoPaddingCryptoAlgorithm)

### Troubleshooting

In case the docker compose provided in the examples to run Kafka does not work for you, you may use this command:

*On OSX and Windows*

    docker run --rm -p 2181:2181 -p 3030:3030 -p 8081-8083:8081-8083 -p 9581-9585:9581-9585 -p 9092:9092 -e ADV_HOST=192.168.99.100 landoop/fast-data-dev:2.0.1

*On linux*

    docker run --rm --net=host -e ADV_HOST=localhost landoop/fast-data-dev:2.0.1

