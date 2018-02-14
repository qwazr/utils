/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.primitives.Longs;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class HashUtils {

	public static int getMurmur3Mod(final String hashString, final Charset charset, final int mod) {
		final HashFunction m3 = Hashing.murmur3_128();
		return (Math.abs(m3.hashString(hashString, charset == null ? Charset.defaultCharset() : charset).asInt()) %
				mod);
	}

	/**
	 * Compute the MD5 hash from a file and return the hexa representation
	 *
	 * @param filePath path to a regular file
	 * @return an hexa representation of the md5
	 * @throws IOException
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

	public static String longToBase64(final long value) {
		return Base64.encodeBase64String(Longs.toByteArray(value));
	}

	public static long base64toLong(final String base64) {
		return Longs.fromByteArray(Base64.decodeBase64(base64));
	}

	/**
	 * Encode an UUID into a base64 string
	 *
	 * @param uuid the UUID to encode
	 * @return the encoded string
	 */
	public static String toBase64(UUID uuid) {
		return longToBase64(uuid.getMostSignificantBits()) + ' ' + longToBase64(uuid.getLeastSignificantBits());
	}

	/**
	 * Decode an UUID from a Base64 string
	 *
	 * @param shortString the encoded string
	 * @return the decoded UUID
	 */
	public static UUID fromBase64(String shortString) {
		final String[] parts = StringUtils.split(shortString, ' ');
		return new UUID(base64toLong(parts[0]), base64toLong(parts[1]));
	}
}
