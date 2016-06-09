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
import java.io.FileFilter;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class TrackedDirectory implements TrackedInterface {

	private final Set<FileChangeConsumer> consumerSet;
	private final Map<File, TrackedFile> trackedFiles;
	private final FileFilter fileFilter;
	private final File trackedDirectory;

	TrackedDirectory(final File directory, final FileFilter fileFilter) {
		this.consumerSet = new LinkedHashSet<>();
		this.fileFilter = fileFilter;
		this.trackedFiles = new LinkedHashMap<>();
		this.trackedDirectory = directory;
	}

	@Override
	final public void register(final FileChangeConsumer consumer) {
		synchronized (consumerSet) {
			if (consumerSet.contains(consumer))
				return;
			consumerSet.add(consumer);
			trackedFiles.forEach((file, trackedFile) -> trackedFile.register(consumer));
		}
	}

	final public void unregister(final FileChangeConsumer consumer) {
		synchronized (consumerSet) {
			if (!consumerSet.contains(consumer))
				return;
			trackedFiles.forEach((file, trackedFile) -> trackedFile.unregister(consumer));
			consumerSet.remove(consumer);
		}
	}

	@Override
	final public void check() {
		synchronized (consumerSet) {
			final File[] files;
			if (trackedDirectory.exists() && trackedDirectory.isDirectory())
				files = trackedDirectory.listFiles(fileFilter);
			else
				files = null;
			if (files == null || files.length == 0)
				doEmptyDirectory();
			else
				doDirectory(files);
		}
	}

	private void doEmptyDirectory() {
		trackedFiles.forEach((file, trackedFile) -> trackedFile.check());
		trackedFiles.clear();
	}

	private void doDirectory(File[] files) {
		final Map<File, TrackedFile> toDelete = new LinkedHashMap<>(trackedFiles);
		for (File file : files) {
			TrackedFile trackedFile = trackedFiles.get(file);
			if (trackedFile == null) {
				final TrackedFile finalTrackedFile;
				finalTrackedFile = trackedFile = new TrackedFile(file);
				trackedFiles.put(file, trackedFile);
				consumerSet.forEach(consumer -> finalTrackedFile.register(consumer));
			}
			trackedFile.check();
			toDelete.remove(file);
		}
		toDelete.forEach((file, trackedFile) -> {
			trackedFile.check();
			trackedFiles.remove(file);
		});
	}

}
