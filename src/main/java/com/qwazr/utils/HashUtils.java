/*
 * Copyright 2015-2020 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.google.common.primitives.Longs;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.codec.digest.MurmurHash3;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.UUID;

public class HashUtils {

    public static int getMurmur3Hash32(final String stringToHash, final int mod) {
        return (Math.abs(MurmurHash3.hash32x86(stringToHash.getBytes())) % mod);
    }

    public static String getMurmur3Hash128Hex(final String stringToHash) {
        final long[] hash = MurmurHash3.hash128x64(stringToHash.getBytes());
        return Long.toHexString(hash[0]) + Long.toHexString(hash[1]);
    }

    public static String getMurmur3Hash32Hex(final String stringToHash) {
        return Integer.toHexString(MurmurHash3.hash32x86(stringToHash.getBytes()));
    }

    /**
     * Compute the MD5 hash from a file and return the hexa representation
     *
     * @param filePath path to a regular file
     * @return an hexa representation of the md5
     * @throws IOException if any I/O exception occurs
     */
    public static String md5Hex(final Path filePath) throws IOException {
        try (final InputStream in = Files.newInputStream(filePath);
             final BufferedInputStream bIn = new BufferedInputStream(in)) {
            return DigestUtils.md5Hex(bIn);
        }
    }

    public static String md5Hex(String text) {
        return DigestUtils.md5Hex(text);
    }

    private static TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(EthernetAddress.fromInterface());

    public static UUID newTimeBasedUUID() {
        return uuidGenerator.generate();
    }

    private static final long NUM_100NS_INTERVALS_SINCE_UUID_EPOCH = 0x01b21dd213814000L;

    // This method comes from Hector's TimeUUIDUtils class:
    // https://github.com/rantav/hector/blob/master/core/src/main/java/me/prettyprint/cassandra/utils/TimeUUIDUtils.java
    public static long getTimeFromUUID(UUID uuid) {
        return (uuid.timestamp() - NUM_100NS_INTERVALS_SINCE_UUID_EPOCH) / 10000;
    }

    public static B64 b64() {
        return new B64(Base64.getEncoder(), Base64.getDecoder());
    }

    public static B64 b64url() {
        return new B64(Base64.getUrlEncoder(), Base64.getUrlDecoder());
    }

    public static class B64 {

        private final Base64.Encoder encoder;
        private final Base64.Decoder decoder;

        public B64(final Base64.Encoder encoder, final Base64.Decoder decoder) {
            this.encoder = encoder;
            this.decoder = decoder;
        }

        public String longToBase64(final long value) {
            return encoder.encodeToString(Longs.toByteArray(value));
        }

        public long base64toLong(final String base64) {
            return Longs.fromByteArray(decoder.decode(base64));
        }

        /**
         * Encode an UUID into a base64 string
         *
         * @param uuids the UUIDS to encode
         * @return the encoded string
         */
        public String toBase64(final UUID... uuids) {
            final byte[] bytes = new byte[uuids.length * 16];
            int i = 0;
            for (final UUID uuid : uuids) {
                System.arraycopy(Longs.toByteArray(uuid.getMostSignificantBits()), 0, bytes, i, 8);
                i += 8;
                System.arraycopy(Longs.toByteArray(uuid.getLeastSignificantBits()), 0, bytes, i, 8);
                i += 8;
            }
            return encoder.encodeToString(bytes);
        }

        /**
         * Decode an array of UUIDS from a Base64 string
         *
         * @param encodedString the encoded string
         * @return the decoded UUID array
         */
        public UUID[] fromBase64(final String encodedString) {
            final byte[] bytes = decoder.decode(encodedString);
            final UUID[] uuids = new UUID[bytes.length / 16];
            int i = 0;
            final byte[] leastBytes = new byte[8];
            final byte[] mostBytes = new byte[8];
            for (int j = 0; j < uuids.length; j++) {
                System.arraycopy(bytes, i, mostBytes, 0, 8);
                i += 8;
                System.arraycopy(bytes, i, leastBytes, 0, 8);
                i += 8;
                uuids[j] = new UUID(Longs.fromByteArray(mostBytes), Longs.fromByteArray(leastBytes));
            }
            return uuids;
        }

    }
}
