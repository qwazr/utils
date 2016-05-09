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
package com.qwazr.utils;

import java.util.concurrent.Callable;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

final public class LockUtils {

	final static public class ReadWriteLock {

		final private ReentrantReadWriteLock rwl = new ReentrantReadWriteLock(true);
		final private Lock r = rwl.readLock();
		final private Lock w = rwl.writeLock();

		final public <T> T read(final Callable<T> call) {
			r.lock();
			try {
				return call.call();
			} catch (Exception e) {
				throw new InsideLockException(e);
			} finally {
				r.unlock();
			}
		}

		final public <V, E extends Exception> V readEx(final ExceptionCallable<V, E> call) throws E {
			r.lock();
			try {
				return call.call();
			} finally {
				r.unlock();
			}
		}

		final public void read(final Runnable run) {
			r.lock();
			try {
				run.run();
			} catch (Exception e) {
				throw new InsideLockException(e);
			} finally {
				r.unlock();
			}
		}

		final public <E extends Exception> void readEx(final ExceptionRunnable<E> run) throws E {
			r.lock();
			try {
				run.run();
			} finally {
				r.unlock();
			}
		}

		final public <T> T write(final Callable<T> call) {
			w.lock();
			try {
				return call.call();
			} catch (Exception e) {
				throw new InsideLockException(e);
			} finally {
				w.unlock();
			}
		}

		final public <V, E extends Exception> V writeEx(final ExceptionCallable<V, E> call) throws E {
			w.lock();
			try {
				return call.call();
			} catch (Exception e) {
				throw new InsideLockException(e);
			} finally {
				w.unlock();
			}
		}

		final public <E extends Exception> void writeEx(final ExceptionRunnable<E> run) throws E {
			w.lock();
			try {
				run.run();
			} finally {
				w.unlock();
			}
		}

		final public void write(final Runnable run) {
			w.lock();
			try {
				run.run();
			} finally {
				w.unlock();
			}
		}

		final public <V> V readOrWrite(final Callable<V> read, final Callable<V> write) {
			V result = read(read);
			if (result != null)
				return result;
			w.lock();
			try {
				result = read.call();
				if (result != null)
					return result;
				return write.call();
			} catch (Exception e) {
				throw new InsideLockException(e);
			} finally {
				w.unlock();
			}
		}

		final public <V, E extends Exception> V readOrWriteEx(final ExceptionCallable<V, E> read,
				final ExceptionCallable<V, E> write) throws Exception {
			V result = readEx(read);
			if (result != null)
				return result;
			w.lock();
			try {
				result = read.call();
				if (result != null)
					return result;
				return write.call();
			} finally {
				w.unlock();
			}
		}
	}

	public interface ExceptionRunnable<E extends Exception> {
		void run() throws E;
	}

	public interface ExceptionCallable<V, E extends Exception> {
		V call() throws E;
	}

	public static class InsideLockException extends RuntimeException {
		InsideLockException(Exception cause) {
			super(cause);
		}
	}
}
