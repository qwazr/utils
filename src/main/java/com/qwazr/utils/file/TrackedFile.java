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
package com.qwazr.utils.file;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

public class TrackedFile implements TrackedInterface {

	private final Map<FileChangeConsumer, AtomicLong> consumerMap;
	private final File trackedFile;

	public TrackedFile(File file) {
		this.consumerMap = new LinkedHashMap<>();
		this.trackedFile = file;
	}

	@Override
	final public void register(final FileChangeConsumer consumer) {
		synchronized (consumerMap) {
			if (consumerMap.containsKey(consumer))
				return;
			consumerMap.put(consumer, new AtomicLong(0));
		}
	}

	@Override
	final public void unregister(final FileChangeConsumer consumer) {
		synchronized (consumerMap) {
			consumerMap.remove(consumer);
		}
	}

	@Override
	final public void check() {
		synchronized (consumerMap) {
			if (trackedFile.exists())
				doExists();
			else
				doNotExists();
		}
	}

	private void doNotExists() {
		consumerMap.forEach((consumer, lastModified) -> {
			if (lastModified.get() == 0)
				return;
			lastModified.set(0);
			consumer.accept(ChangeReason.DELETED, trackedFile);
		});
	}

	private void doExists() {
		final long newLastModified = trackedFile.lastModified();
		consumerMap.forEach((consumer, lastModified) -> {
			if (lastModified.get() == newLastModified)
				return;
			lastModified.set(newLastModified);
			consumer.accept(ChangeReason.UPDATED, trackedFile);
		});
	}

}
