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

import com.qwazr.utils.FileUtils;
import com.qwazr.utils.IOUtils;
import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileUtilsTest {

	@Test
	public void createDirectory() throws IOException {
		File parentDir = Files.createTempDirectory("FileUtilsTest").toFile();
		File newDir1 = new File(parentDir, RandomUtils.alphanumeric(5));
		File newDir2 = FileUtils.createDirectory(newDir1);
		Assert.assertNotNull(newDir2);
		Assert.assertTrue(newDir2.exists());
		Assert.assertTrue(newDir2.isDirectory());
		Assert.assertEquals(newDir1, newDir2);
	}

	Path prepareDirectory() throws IOException {
		Path parentDir = Files.createTempDirectory("FileUtilsTest");
		Path subdir1 = parentDir.resolve("sub1");
		Path subdir2 = subdir1.resolve("sub2");
		File file1 = subdir1.resolve("file1").toFile();
		File file2 = subdir2.resolve("file1").toFile();
		Files.createDirectories(subdir2);
		IOUtils.writeStringToFile("Test", file1);
		IOUtils.writeStringToFile("Test", file2);
		Assert.assertTrue(subdir2.toFile().setReadOnly());
		Assert.assertTrue(file2.setReadOnly());
		return parentDir;
	}

	@Test
	public void testDeleteDirectory() throws IOException {
		Path parentDir = prepareDirectory();
		Assert.assertEquals(5L, Files.walk(parentDir).count(), 0);
		Assert.assertEquals(5, FileUtils.deleteDirectory(parentDir));
		Assert.assertFalse(Files.exists(parentDir));
	}

	@Test
	public void testDeleteDirectoryQuietly() throws IOException {
		Path parentDir = prepareDirectory();
		Assert.assertEquals(5L, Files.walk(parentDir).count(), 0);
		Assert.assertNull(FileUtils.deleteDirectoryQuietly(parentDir));
		Assert.assertFalse(Files.exists(parentDir));
	}

	@Test
	public void testIsParent() throws IOException {
		Path parentDir = Files.createTempDirectory("FileUtilsTest");
		Path child1Dir = parentDir.resolve("child1");
		Files.createDirectories(child1Dir);
		Assert.assertTrue(FileUtils.isParent(parentDir, child1Dir));
		Assert.assertFalse(FileUtils.isParent(child1Dir, parentDir));
		Path child2Dir = parentDir.resolve("child2");
		Assert.assertTrue(FileUtils.isParent(parentDir, child2Dir));
		Assert.assertFalse(FileUtils.isParent(child2Dir, parentDir));
	}
}

