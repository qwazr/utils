/**
 * Copyright 2015-2016 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.inputstream;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class count the number of bytes read.
 */
public class CounterInputStream extends InputStream {

	private final InputStream input;
	protected final AtomicLong count;

	public CounterInputStream(final InputStream input) {
		this.input = input;
		this.count = new AtomicLong(0);
	}

	@Override
	public int read() throws IOException {
		final int c = input.read();
		if (c == -1)
			return -1;
		count.incrementAndGet();
		return c;
	}

	@Override
	public int read(final byte b[], final int off, final int len) throws IOException {
		final int c = input.read(b, off, len);
		switch (c) {
		case -1:
			return -1;
		case 0:
			return 0;
		}
		count.addAndGet(c);
		return c;
	}

	@Override
	public synchronized void reset() throws IOException {
		input.reset();
		count.set(0);
	}

	@Override
	public long skip(final long n) throws IOException {
		final long c = input.skip(n);
		count.addAndGet(c);
		return c;
	}

	@Override
	public void close() throws IOException {
		input.close();
	}

	/**
	 * @return the number of bytes read
	 */
	final public long getCount() {
		return count.get();
	}
}
