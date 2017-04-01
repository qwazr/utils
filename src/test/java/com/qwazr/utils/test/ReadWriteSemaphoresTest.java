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
import java.util.concurrent.atomic.AtomicInteger;

public class ReadWriteSemaphoresTest {

	void doTest(final ReadWriteSemaphores semaphores, final Integer maxRead, final Integer maxWrite)
			throws InterruptedException {
		final int threads = maxRead != null && maxWrite != null ? maxRead + maxWrite : 10;
		final ExecutorService executorService = Executors.newFixedThreadPool(threads * 5);
		final AtomicInteger executionCount = new AtomicInteger(0);
		final AtomicInteger maxReadCount = new AtomicInteger(0);
		final AtomicInteger maxWriteCount = new AtomicInteger(0);
		final AtomicInteger concurrentReadCount = new AtomicInteger(0);
		final AtomicInteger concurrentWritecount = new AtomicInteger(0);
		final int count = RandomUtils.nextInt(threads * 10, threads * 20);
		for (int i = 0; i < count; i++) {
			try {
				final Action action;
				if (RandomUtils.nextBoolean())
					action = new Action(semaphores.acquireReadSemaphore(), concurrentReadCount, executionCount,
							maxReadCount);
				else
					action = new Action(semaphores.acquireWriteSemaphore(), concurrentWritecount, executionCount,
							maxWriteCount);
				executorService.submit(action);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
		executorService.shutdown();
		executorService.awaitTermination(3, TimeUnit.MINUTES);
		Assert.assertEquals(count, executionCount.get());
		if (maxRead != null)
			Assert.assertEquals(maxRead.intValue(), maxReadCount.get());
		if (maxWrite != null)
			Assert.assertEquals(maxWrite.intValue(), maxWriteCount.get());
	}

	@Test
	public void test() throws InterruptedException {
		Integer maxRead = null;
		Integer maxWrite = null;
		final ReadWriteSemaphores semaphores = new ReadWriteSemaphores(maxRead, maxWrite);
		for (int i = 0; i < 3; i++) {
			doTest(semaphores, maxRead, maxWrite);
			maxRead = RandomUtils.nextInt(1, 10);
			maxWrite = RandomUtils.nextInt(1, 4);
			semaphores.setReadSize(maxRead);
			semaphores.setWriteSize(maxWrite);
		}
	}

	class Action implements Runnable {

		private final ReadWriteSemaphores.Lock semaphoreLock;
		private final AtomicInteger concurrentCount;
		private final AtomicInteger executionCount;

		Action(final ReadWriteSemaphores.Lock semaphoreLock, final AtomicInteger concurrentCount,
				final AtomicInteger executionCount, final AtomicInteger maxConcurrentCount) {
			this.semaphoreLock = semaphoreLock;
			this.concurrentCount = concurrentCount;
			this.executionCount = executionCount;
			final int cc = concurrentCount.incrementAndGet();
			if (cc > maxConcurrentCount.get())
				maxConcurrentCount.set(cc);
		}

		@Override
		public void run() {
			try {
				executionCount.incrementAndGet();
				Thread.sleep(RandomUtils.nextInt(250, 500));
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			} finally {
				concurrentCount.decrementAndGet();
				semaphoreLock.close();
			}
		}

	}
}
