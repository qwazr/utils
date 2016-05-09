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
import java.util.LinkedHashSet;
import java.util.Map;

public class TrackedDirectories implements TrackedInterface {

	final Collection<TrackedDirectory> trackers = new LinkedHashSet<>();

	TrackedDirectories(final Collection<File> directories, final FileFilter fileFilter) {
		if (directories == null)
			return;
		directories.forEach(file -> trackers.add(new TrackedDirectory(file, fileFilter)));
	}

	@Override
	final public void register(final FileChangeConsumer consumer) {
		trackers.forEach(tracker -> tracker.register(consumer));
	}

	@Override
	final public void unregister(final FileChangeConsumer consumer) {
		trackers.forEach(tracker -> tracker.unregister(consumer));
	}

	@Override
	final public void check() {
		trackers.forEach(TrackedAbstract::check);
	}
}
