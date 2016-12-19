/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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

import com.qwazr.utils.FileUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

public class FileUtilsTest {

	@Test
	public void createDirectory() throws IOException {
		File parentDir = Files.createTempDirectory("FileUtilsTest").toFile();
		File newDir1 = new File(parentDir, RandomStringUtils.randomAlphanumeric(5));
		File newDir2 = FileUtils.createDirectory(newDir1);
		Assert.assertNotNull(newDir2);
		Assert.assertTrue(newDir2.exists());
		Assert.assertTrue(newDir2.isDirectory());
		Assert.assertEquals(newDir1, newDir2);
	}
}


