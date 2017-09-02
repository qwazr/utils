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
import java.util.function.Consumer;

public class ReferenceCounter<E extends Exception> {

	private int counter = 0;

	public synchronized int acquire() {
		return ++counter;
	}

	public synchronized int release() throws E {
		return --counter;
	}

	public abstract static class Closer extends ReferenceCounter<IOException> implements Closeable {

		@Override
		public int release() throws IOException {
			final int c = super.release();
			if (c <= 0)
				close();
			return c;
		}

		public void release(Consumer<IOException> exceptionConsumer) {
			try {
				release();
			} catch (IOException e) {
				if (exceptionConsumer != null)
					exceptionConsumer.accept(e);
			}
		}

	}

}
