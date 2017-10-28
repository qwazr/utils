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
package com.qwazr.utils.concurrent;

import com.qwazr.utils.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class BlockingPoolLambdaTest {

	void test(final int poolSize, final int valueNumber, boolean withResult) throws Exception {
		final int[] values = new int[valueNumber];
		for (int i = 0; i < valueNumber; i++)
			values[i] = RandomUtils.nextInt(1, 100);

		final ConsumerEx<Collection<Integer>, Exception> results;
		final Set<Integer> resultValues;
		if (withResult) {
			resultValues = ConcurrentHashMap.newKeySet();
			results = resultValues::addAll;
		} else {
			resultValues = null;
			results = null;
		}

		final Set<Integer> returned = ConcurrentHashMap.newKeySet();
		try (BlockingPoolLambda<Integer> pool = new BlockingPoolLambda<>(poolSize, results)) {
			for (int value : values)
				pool.submit(() -> {
					returned.add(value);
					return value;
				});
			pool.close();
			Assert.assertEquals(0, pool.size());
		}

		for (int value : values)
			Assert.assertTrue(returned.contains(value));
		if (resultValues != null)
			for (int value : values)
				Assert.assertTrue(resultValues.contains(value));
	}

	@Test
	public void oneThreadOneValueNoResults() throws Exception {
		test(1, 1, false);
	}

	@Test
	public void oneThreadMultipleValuesNoResults() throws Exception {
		test(1, 5, false);
	}

	@Test
	public void multipleThreadOneValueNoResults() throws Exception {
		test(2, 1, false);
	}

	@Test
	public void multipleThreadMutipleValuesNoResults() throws Exception {
		test(2, 5, false);
	}

	@Test
	public void oneThreadOneValueWithResults() throws Exception {
		test(1, 1, true);
	}

	@Test
	public void oneThreadMultipleValuesWithResults() throws Exception {
		test(1, 5, true);
	}

	@Test
	public void multipleThreadOneValueWithResults() throws Exception {
		test(2, 1, true);
	}

	@Test
	public void multipleThreadMutipleValuesWithResults() throws Exception {
		test(2, 5, true);
	}

}
