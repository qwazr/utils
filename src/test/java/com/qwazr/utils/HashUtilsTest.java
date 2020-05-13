/*
 * Copyright 2016-2018 Emmanuel Keller / QWAZR
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

public class HashUtilsTest {

    @Test
    public void longToBase64Test() {
        Assert.assertEquals("AAAAAkywFuo=", HashUtils.longToBase64(9876543210L));
    }

    @Test
    public void longGetMurmur3Hash32() {
        Assert.assertEquals(0, HashUtils.getMurmur3Hash32("a", 10));
        Assert.assertEquals(1, HashUtils.getMurmur3Hash32("b", 10));
        Assert.assertEquals(7, HashUtils.getMurmur3Hash32("c", 10));
        Assert.assertEquals(9, HashUtils.getMurmur3Hash32("d", 10));
        Assert.assertEquals(9, HashUtils.getMurmur3Hash32("e", 10));
        Assert.assertEquals(3, HashUtils.getMurmur3Hash32("f", 10));
    }

    @Test
    public void longGetMurmur3Hash128Hex() {
        Assert.assertEquals("e47d86bfaca3bf55b07109993321845c", HashUtils.getMurmur3Hash128Hex("abcdef"));
        Assert.assertEquals("a6cd2f9fc09ee4991c3aa23ab155bbb6", HashUtils.getMurmur3Hash128Hex("abcdefg"));
    }

    @Test
    public void longGetMurmur3Hash32Hex() {
        Assert.assertEquals("6181c085", HashUtils.getMurmur3Hash32Hex("abcdef"));
        Assert.assertEquals("883c9b06", HashUtils.getMurmur3Hash32Hex("abcdefg"));
    }
}
