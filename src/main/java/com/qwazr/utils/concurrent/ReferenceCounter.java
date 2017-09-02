/*
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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
import java.io.IOException;

public interface ReferenceCounter {

	void acquire();

	void release();

	abstract class Impl implements ReferenceCounter {

		private int counter = 0;

		public synchronized void acquire() {
			++counter;
		}

		public synchronized void release() {
			try {
				release(--counter);
			} catch (Exception e) {
				throw new ReleaseException(e);
			}
		}

		abstract protected void release(int counter) throws Exception;

	}

	class ReleaseException extends RuntimeException {

		protected ReleaseException(Exception e) {
			super(e);
		}
	}

	abstract class Closer extends Impl implements Closeable {

		@Override
		protected void release(int counter) throws IOException {
			if (counter <= 0)
				close();
		}

	}

}
