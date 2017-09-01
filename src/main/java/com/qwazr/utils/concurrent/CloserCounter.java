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

public class CloserCounter<T extends Closeable> {

	public final T item;
	private int counter;

	public CloserCounter(T item) {
		this.item = item;
		this.counter = 0;
	}

	public T get() {
		return item;
	}

	public synchronized void acquire() {
		++counter;
	}

	public synchronized void release() throws IOException {
		if (--counter <= 0)
			item.close();
	}

	public void release(Consumer<IOException> exceptionConsumer) {
		try {
			release();
		} catch (IOException e) {
			exceptionConsumer.accept(e);
		}
	}

}
