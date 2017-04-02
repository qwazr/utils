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
package com.qwazr.utils.concurrent;

import java.io.Closeable;
import java.util.Objects;
import java.util.concurrent.Semaphore;

public class ReadWriteSemaphores {

	private volatile Integer readPermits;
	private volatile Integer writePermits;
	private volatile Semaphore readSemaphore;
	private volatile Semaphore writeSemaphore;

	public ReadWriteSemaphores(final Integer maxConcurrentRead, final Integer maxConcurrentWrite) {
		setReadSize(maxConcurrentRead);
		setWriteSize(maxConcurrentWrite);
	}

	private static Semaphore checkNewSemaphore(final Semaphore oldSemaphore, final Integer oldSize,
			final Integer newSize) {
		if (oldSemaphore != null && Objects.equals(oldSize, newSize))
			return oldSemaphore;
		return newSize == null ? null : new Semaphore(newSize);
	}

	public synchronized void setReadSize(final Integer maxConcurrentRead) {
		readSemaphore = checkNewSemaphore(readSemaphore, readPermits, maxConcurrentRead);
		readPermits = maxConcurrentRead;
	}

	public synchronized void setWriteSize(final Integer maxConcurrentWrite) {
		writeSemaphore = checkNewSemaphore(writeSemaphore, writePermits, maxConcurrentWrite);
		writePermits = maxConcurrentWrite;
	}

	private static Lock atomicAquire(final Semaphore semaphore) throws AcquireException {
		try {
			return semaphore == null ? SemaphoreLock.EMPTY : new SemaphoreLock(semaphore);
		} catch (InterruptedException e) {
			throw new AcquireException(e);
		}
	}

	public Lock acquireReadSemaphore() throws AcquireException {
		return atomicAquire(readSemaphore);
	}

	public Lock acquireWriteSemaphore() throws AcquireException {
		return atomicAquire(writeSemaphore);
	}

	public interface Lock extends Closeable {

		default void close() {
		}

		Lock EMPTY = new Lock() {
		};
	}

	final static class SemaphoreLock implements Lock {

		private final Semaphore semaphore;

		SemaphoreLock(final Semaphore semaphore) throws InterruptedException {
			this.semaphore = semaphore;
			semaphore.acquire();
		}

		@Override
		public void close() {
			semaphore.release();
		}

	}

	public static class AcquireException extends RuntimeException {

		AcquireException(Exception cause) {
			super(cause);
		}
	}
}
