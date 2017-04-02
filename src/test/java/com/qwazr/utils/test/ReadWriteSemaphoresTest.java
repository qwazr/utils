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

import com.qwazr.utils.concurrent.ReadWriteSemaphores;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class ReadWriteSemaphoresTest {

	final class TestContext {

		final AtomicInteger executionCount = new AtomicInteger(0);
		final ExecutorService executorService;
		final int threads;
		final AtomicBoolean done = new AtomicBoolean(false);

		TestContext(Integer maxRead, Integer maxWrite) {
			threads = maxRead != null && maxWrite != null ? maxRead + maxWrite : 10;
			executorService = Executors.newFixedThreadPool(threads * 5);
		}
	}

	final class TestCounter {

		final Integer maxExpected;
		final AtomicInteger maxConcurrentCount = new AtomicInteger(0);
		final AtomicInteger concurrentCount = new AtomicInteger(0);
		final AtomicInteger maxCountReached = new AtomicInteger(0);

		TestCounter(Integer maxExpected) {
			this.maxExpected = maxExpected == null ? 1 : maxExpected;
		}
	}

	void doTest(final ReadWriteSemaphores semaphores, final Integer maxRead, final Integer maxWrite)
			throws InterruptedException {

		semaphores.setReadSize(maxRead);
		semaphores.setWriteSize(maxWrite);

		final TestContext context = new TestContext(maxRead, maxWrite);
		final TestCounter readCounter = new TestCounter(maxRead);
		final TestCounter writeCounter = new TestCounter(maxWrite);

		int count = 0;
		long end = System.currentTimeMillis() + 1000 * 60 * 3;
		while (System.currentTimeMillis() < end) {
			final Action action;
			if (RandomUtils.nextBoolean())
				action = new Action(semaphores.acquireReadSemaphore(), context, readCounter);
			else
				action = new Action(semaphores.acquireWriteSemaphore(), context, writeCounter);
			context.executorService.submit(action);
			count++;
			if (readCounter.maxCountReached.get() >= 4 && writeCounter.maxCountReached.get() >= 4)
				break;
		}
		context.executorService.shutdown();
		context.executorService.awaitTermination(3, TimeUnit.MINUTES);
		Assert.assertEquals(count, context.executionCount.get());
		if (maxRead != null)
			Assert.assertEquals(maxRead.intValue(), readCounter.maxConcurrentCount.get());
		if (maxWrite != null)
			Assert.assertEquals(maxWrite.intValue(), writeCounter.maxConcurrentCount.get());
	}

	@Test
	public void test() throws InterruptedException {
		final ReadWriteSemaphores semaphores = new ReadWriteSemaphores(null, null);
		// Start with empty size
		doTest(semaphores, null, null);
		// Small values
		doTest(semaphores, 1, 1);
		// Keep same write size
		doTest(semaphores, 2, 1);
		// Largest size
		doTest(semaphores, 5, 2);
	}

	class Action implements Runnable {

		private final ReadWriteSemaphores.Lock semaphoreLock;
		private final TestContext context;
		private final TestCounter counter;

		Action(final ReadWriteSemaphores.Lock semaphoreLock, final TestContext context, final TestCounter counter) {
			this.semaphoreLock = semaphoreLock;
			this.context = context;
			this.counter = counter;
			final int cc = counter.concurrentCount.incrementAndGet();
			if (cc > counter.maxConcurrentCount.get())
				counter.maxConcurrentCount.set(cc);
			if (counter.maxExpected != null && cc >= counter.maxExpected)
				counter.maxCountReached.incrementAndGet();
		}

		@Override
		public void run() {
			try {
				context.executionCount.incrementAndGet();
				Thread.sleep(RandomUtils.nextInt(250, 500));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				counter.concurrentCount.decrementAndGet();
				semaphoreLock.close();
			}
		}

	}
}
