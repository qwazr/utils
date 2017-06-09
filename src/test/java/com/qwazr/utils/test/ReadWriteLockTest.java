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
package com.qwazr.utils.test;

import com.qwazr.utils.LockUtils;
import com.qwazr.utils.concurrent.ReadWriteLock;
import com.qwazr.utils.concurrent.ThreadUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.TreeMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.StampedLock;

import static java.lang.System.currentTimeMillis;

public class ReadWriteLockTest {

	abstract class Read implements Runnable {

		final LockUtils.ReadWriteLock lock;
		long time;

		Read(LockUtils.ReadWriteLock lock) {
			this.lock = lock;
		}

		@Override
		public void run() {
			lock.read(() -> {
				time = currentTimeMillis();
			});
		}
	}

	private void test(ReadWriteLock rwl) throws InterruptedException {
		final AtomicLong writeTime = new AtomicLong();
		final AtomicLong readTime = new AtomicLong();
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		rwl.writeEx(() -> {
			executor.submit(() -> rwl.readEx(() -> {
				ThreadUtils.sleep(1, TimeUnit.MILLISECONDS);
				readTime.set(System.currentTimeMillis());
			}));
			Thread.sleep(100);
			writeTime.set(System.currentTimeMillis());
		});
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);
		Assert.assertTrue(writeTime.get() < readTime.get());
	}

	@Test
	public void testReadWriteLock() throws InterruptedException {
		test(ReadWriteLock.reentrant(false));
		test(ReadWriteLock.reentrant(true));
		test(ReadWriteLock.of(new StampedLock().asReadWriteLock()));
		test(ReadWriteLock.stamped());
	}

	private long benchmark(ReadWriteLock rwl, final int count) throws InterruptedException {
		final ExecutorService executor = Executors.newSingleThreadExecutor();
		final long startTime = System.currentTimeMillis();
		final AtomicLong writeCount = new AtomicLong();
		final AtomicLong readCount = new AtomicLong();
		executor.submit(() -> {
			for (int i = 0; i < count; i++)
				rwl.writeEx(writeCount::incrementAndGet);
		});
		executor.submit(() -> {
			for (int i = 0; i < count; i++)
				rwl.readEx(readCount::incrementAndGet);
		});
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);
		final long timeSpent = System.currentTimeMillis() - startTime;
		Assert.assertEquals(count, writeCount.get());
		Assert.assertEquals(count, readCount.get());
		return timeSpent;
	}

	@Test
	public void benchmarkTest() throws InterruptedException {
		final int count = 10000000;
		for (int i = 0; i < 10; i++) {
			TreeMap<Long, String> results = new TreeMap<>();
			results.put(benchmark(ReadWriteLock.reentrant(false), count), "Unfair");
			results.put(benchmark(ReadWriteLock.reentrant(true), count), "Fair");
			results.put(benchmark(ReadWriteLock.of(new StampedLock().asReadWriteLock()), count), "StampedAsRw");
			results.put(benchmark(ReadWriteLock.stamped(), count), "Stamped");
			results.forEach((key, value) -> System.out.print(
					" | " + value + " => " + key + " (" + (float) (key / count) + ")"));
			System.out.println();
		}
	}
}
