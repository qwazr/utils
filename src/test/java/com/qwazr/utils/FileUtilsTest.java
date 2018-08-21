/*
 * Copyright 2015-2018 Emmanuel Keller / QWAZR
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
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public class FileUtilsTest {

    Path tmpDir;

    @Before
    public void setup() throws IOException {
        tmpDir = Files.createTempDirectory("test");
        Files.createFile(tmpDir.resolve("1.tmp"));
        Files.createFile(tmpDir.resolve("2.tmp"));
        Files.createFile(tmpDir.resolve("3.tmp"));
    }

    @Test
    public void testFileListCount() throws IOException {
        Assert.assertEquals(3, FileUtils.countFiles(tmpDir));
    }

    @Test
    public void testFileListWithConsumer() throws IOException {
        final Set<Path> pathSet = new LinkedHashSet<>();
        FileUtils.listFiles(tmpDir, pathSet::add);
        Assert.assertEquals(3, pathSet.size());
    }

}
