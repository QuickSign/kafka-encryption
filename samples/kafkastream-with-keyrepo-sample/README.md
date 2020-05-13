### Key repository sample (Streams API)

This is a dummy sample of a bank account. Each account has an associated
key to encrypt all operations associated to the account.

For this account, we manage the bank view and the agency view.

* The bank has access to the full key repository (account 0 to 9).
* Agency 1 has only access to a subset of key repository (account 0 to 4)
* Agency 2 has only access to a subset of key repository (account 5 to 9)

All operations are written encrypted into a common topic `operations`

#### Running the sample
  
    cd samples/kafkastream-with-keyrepo-sample
    mvn clean package
    docker-compose up # on windows and OSX, you need to adjust
    sh generateKeys.sh
    sh runSamples.sh
