/**
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

import com.qwazr.utils.CollectionsUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class CollectionsUtilsTest {

	@Test
	public void notSameSize() {
		Assert.assertFalse(CollectionsUtils.equals(Arrays.asList(1, 2, 3), Arrays.asList(1, 2)));
	}

	@Test
	public void firstIsNull() {
		Assert.assertFalse(CollectionsUtils.equals(Arrays.asList(1, 2, 3), null));
	}

	@Test
	public void secondIsNull() {
		Assert.assertFalse(CollectionsUtils.equals(null, Arrays.asList(1, 2, 3)));
	}

	@Test
	public void notSameContent() {
		Assert.assertFalse(CollectionsUtils.equals(Arrays.asList(1, 2, 3), Arrays.asList(3, 2, 1)));
	}

	@Test
	public void sameContent() {
		Assert.assertTrue(CollectionsUtils.equals(Arrays.asList(1, 2, 3), Arrays.asList(1, 2, 3)));
	}

	@Test
	public void sameMap() {
		Map map1 = new HashMap<Integer, Integer>();
		Map map2 = new TreeMap<Integer, Integer>();
		for (int i = 0; i < 5; i++) {
			int value = RandomUtils.nextInt();
			map1.put(value, value);
			map2.put(value, value);
		}
		Assert.assertTrue(CollectionsUtils.equals(map1, map2));
	}

	@Test
	public void multilinePrint() throws IOException {
		Assert.assertEquals(String.format("one%ntwo%n%nthree"),
				CollectionsUtils.multiline(Arrays.asList("one", "two", null, "three")));
	}
}


