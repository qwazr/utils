/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils.test;

import com.qwazr.utils.HashUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.UUID;

/**
 * Created by ekeller on 12/12/2016.
 */
public class HashUtilTest {

	@Test
	public void timeBasedUuid() {
		final HashSet<UUID> set = new HashSet<>();
		for (int i = 0; i < 100; i++) {
			final UUID uuid = HashUtils.newTimeBasedUUID();
			if (!set.add(uuid))
				Assert.fail("The UUID is not unique");
		}
	}

	@Test
	public void md5Test() throws IOException {
		final String content = RandomUtils.alphanumeric(1000);
		final String md5a = HashUtils.md5Hex(content);
		Assert.assertNotNull(md5a);

		final Path file = Files.createTempFile("hashUtils", ".txt");
		IOUtils.writeStringToPath(content, Charset.defaultCharset(), file);
		final String md5b = HashUtils.md5Hex(file);

		Assert.assertEquals(md5a, md5b);

	}

	@Test
	public void timeConversion() {
		UUID uuid = HashUtils.newTimeBasedUUID();
		long time = HashUtils.getTimeFromUUID(uuid);
		Assert.assertEquals(time, (uuid.timestamp() - 0x01b21dd213814000L) / 10000);
	}

	private void checkBase64Uuuid(UUID uuid) {
		final String encoded = HashUtils.toBase64(uuid);
		final UUID decoded = HashUtils.fromBase64(encoded);
		Assert.assertEquals(uuid, decoded);
	}

	@Test
	public void base64UuidTimeBasesEncodingTest() {
		checkBase64Uuuid(HashUtils.newTimeBasedUUID());
	}

	@Test
	public void base64UuidRandomEncodingTest() {
		checkBase64Uuuid(UUID.randomUUID());
	}
}
