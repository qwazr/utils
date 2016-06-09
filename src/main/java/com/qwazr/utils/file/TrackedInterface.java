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
import java.util.Collection;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface TrackedInterface {

	void register(final FileChangeConsumer consumer);

	void unregister(final FileChangeConsumer consumer);

	void check();

	enum ChangeReason {
		DELETED, UPDATED
	}

	interface FileChangeConsumer extends BiConsumer<ChangeReason, File> {
	}

	static TrackedInterface build(final Collection<File> directories, final FileFilter fileFilter) {
		if (directories == null || directories.isEmpty())
			return null;
		if (directories.size() == 1)
			return new TrackedDirectory(directories.iterator().next(), fileFilter);
		return new TrackedDirectories(directories, fileFilter);
	}

	static TrackedInterface build(final File directory, final FileFilter fileFilter) {
		return new TrackedDirectory(directory, fileFilter);
	}
}
