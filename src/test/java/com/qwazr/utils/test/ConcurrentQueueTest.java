/**
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
package com.qwazr.utils.test;

import com.qwazr.utils.concurrent.ConcurrentQueue;
import org.apache.commons.lang3.RandomUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class ConcurrentQueueTest extends ConcurrentQueue<Integer> {

	private static ExecutorService executor;

	private final AtomicInteger counter;

	public ConcurrentQueueTest() {
		super(executor, Math.min(4, Runtime.getRuntime().availableProcessors()));
		counter = new AtomicInteger();
	}

	@BeforeClass
	public static void before() {
		executor = Executors.newCachedThreadPool();
	}

	@AfterClass
	public static void after() {
		executor.shutdown();
	}

	@Test
	public void test() {
		final int l = RandomUtils.nextInt(1000, 5000);
		for (int i = 0; i < l; i++)
			accept(RandomUtils.nextInt());
		close();
		Assert.assertEquals(l, counter.get());
	}

	@Override
	protected Consumer<Integer> getNewConsumer() {
		return integer -> counter.incrementAndGet();
	}

}
