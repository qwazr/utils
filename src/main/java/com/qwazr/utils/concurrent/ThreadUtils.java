/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import java.util.Collection;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicReference;

public class ThreadUtils {

	public static class ThreadGroupFactory implements ThreadFactory {

		private final ThreadGroup group;

		public ThreadGroupFactory(ThreadGroup group) {
			this.group = group;
		}

		@Override
		public Thread newThread(Runnable target) {
			return new Thread(group, target);
		}

	}

	public static Thread[] getThreadArray(ThreadGroup group) {
		Thread[] threads = new Thread[group.activeCount()];
		for (; ; ) {
			int l = group.enumerate(threads);
			if (l == threads.length)
				break;
			threads = new Thread[l];
		}
		return threads;
	}

	public interface WaitInterface {

		boolean done();

		boolean abort();
	}

	public static boolean waitUntil(long secTimeOut, WaitInterface waiter) throws InterruptedException {
		long finalTime = System.currentTimeMillis() + secTimeOut * 1000;
		while (!waiter.done()) {
			if (waiter.abort())
				return false;
			if (secTimeOut != 0)
				if (System.currentTimeMillis() > finalTime)
					return false;
			Thread.sleep(200);
		}
		return true;
	}

	public static <T> void parallel(Collection<T> collection, ParallelConsumer<T> consumer) throws Exception {
		AtomicReference<Exception> exceptionRef = new AtomicReference<>();
		collection.parallelStream().forEach(item -> {
			try {
				consumer.accept(item);
			} catch (Exception e) {
				exceptionRef.compareAndSet(null, e);
			}
		});
		Exception exception = exceptionRef.get();
		if (exception == null)
			return;
		throw exceptionRef.get();
	}

	public interface ParallelConsumer<T> {

		void accept(T t) throws Exception;

	}

	public static void parallel(Collection<? extends ParallelRunnable> runnables) throws Exception {
		AtomicReference<Exception> exceptionRef = new AtomicReference<>();
		runnables.parallelStream().forEach(runnable -> {
			try {
				runnable.run();
			} catch (Exception e) {
				exceptionRef.compareAndSet(null, e);
			}
		});
		Exception exception = exceptionRef.get();
		if (exception == null)
			return;
		throw exceptionRef.get();
	}

	public interface ParallelRunnable {

		void run() throws Exception;

	}

}
