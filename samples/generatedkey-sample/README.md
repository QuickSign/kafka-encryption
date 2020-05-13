### Embedded per message encryption key sample (Consumer API)

In this example, a different encryption key is generated for each message, encrypted using a master key and stored
embedded with the payload.

#### Running the sample

    cd samples/kafka-encryption-generatedkey-sample
    mvn package
    docker-compose up # on windows and OSX, you need to adjust
    sh generateMasterKey.sh
    sh runSamples.sh
