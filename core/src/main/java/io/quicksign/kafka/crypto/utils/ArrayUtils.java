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
package io.quicksign.kafka.crypto.utils;

public class ArrayUtils {

    private ArrayUtils(){}

    public static boolean startWith(byte[] array, byte[] prefix){
        if(array.length < prefix.length) {
            return false;
        }
        boolean isPrefix = true;
        for (int i = 0; i < prefix.length && isPrefix; i ++){
            isPrefix = isPrefix && (array[i] == prefix[i]);
        }
        return isPrefix;
    }


}
