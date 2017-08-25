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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class RunnablePool implements Closeable {

	private final int fixedThreads;

	private final ExecutorService executor;

	private final List<Future> futures;

	private RunnablePool(ExecutorService executor, int fixedThreads) {
		this.executor = executor;
		this.fixedThreads = fixedThreads;
		this.futures = new ArrayList<>(fixedThreads == 0 ? 4 : fixedThreads);
	}

	/**
	 * Build a SubmitPool with the given ExecutorService.
	 * It is the responsibility of the caller to shutdown the ExecutorService
	 *
	 * @param executor
	 */
	public RunnablePool(ExecutorService executor) {
		this(executor, 0);
	}

	/**
	 * Build a SubmitPool with its own managed FixedThreadPool
	 *
	 * @param fixedThreads the size of the pool
	 */
	public RunnablePool(int fixedThreads) {
		this(Executors.newFixedThreadPool(fixedThreads), fixedThreads);
	}

	/**
	 * Build a submit pool with the number of processors
	 */
	public RunnablePool() {
		this(Runtime.getRuntime().availableProcessors());
	}

	public void submit(Runnable runnable) {
		futures.add(executor.submit(runnable));
	}

	@Override
	public void close() throws IOException {
		if (fixedThreads != 0)
			executor.shutdown();
		List<Exception> exceptions = null;
		for (Future future : futures) {
			try {
				future.get();
			} catch (ExecutionException | CancellationException | InterruptedException e) {
				if (exceptions == null)
					exceptions = new ArrayList<>();
				exceptions.add(e);
			}
		}
		if (exceptions != null)
			throw new RunnablePoolException(exceptions);
	}

}
