/*
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class StringUtilsTest {

	@Test
	public void joinWithSeparatorTests() {
		final String result = "dir1/dir2/file.txt";
		Assert.assertEquals(result, StringUtils.joinWithSeparator('/', "/dir1/", "/dir2/", "/file.txt"));
		Assert.assertEquals(result, StringUtils.joinWithSeparator('/', "dir1", "dir2", "file.txt"));
	}

	byte[] testCompression(String text, Charset charset) throws IOException {
		final byte[] compressed = StringUtils.compressGzip(text, charset);
		Assert.assertEquals(text, StringUtils.decompressGzip(compressed, charset));
		return compressed;
	}

	@Test
	public void gzipCompression() throws IOException {
		final String test = RandomUtils.alphanumeric(1000);
		final byte[] compressed = testCompression(test, null);
		Assert.assertTrue(compressed.length < test.length());
	}

	@Test
	public void gzipCompressionUtf8() throws IOException {
		final String test = RandomUtils.alphanumeric(1000);
		final byte[] compressed = testCompression(test, StandardCharsets.UTF_8);
		Assert.assertTrue(compressed.length < test.length());
	}

	@Test
	public void gzipCompressionEmpty() throws IOException {
		testCompression(StringUtils.EMPTY, null);
	}
}
