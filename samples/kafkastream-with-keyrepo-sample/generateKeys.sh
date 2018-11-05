#!/bin/bash

#  Copyright 2018 Quicksign
#
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#    http://www.apache.org/licenses/LICENSE-2.0
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License. See accompanying LICENSE file.

rm /tmp/samplestream.pkcs12
rm /tmp/samplestream1.pkcs12
rm /tmp/samplestream2.pkcs12
for i in `seq 0 9`; do
    keytool -genseckey -keystore /tmp/samplestream.pkcs12 -storetype pkcs12 -storepass sample -keyalg AES -keysize 256 -alias cpt$i -keypass sample
done

for i in `seq 0 4`; do
    keytool -importkeystore -srckeystore /tmp/samplestream.pkcs12 -srcstorepass sample \
    -destkeystore /tmp/samplestream1.pkcs12 -deststoretype pkcs12 -deststorepass sample \
    -srcalias cpt$i
done

for i in `seq 5 9`; do
    keytool -importkeystore -srckeystore /tmp/samplestream.pkcs12 -srcstorepass sample \
    -destkeystore /tmp/samplestream2.pkcs12 -deststoretype pkcs12 -deststorepass sample \
    -srcalias cpt$i
done
