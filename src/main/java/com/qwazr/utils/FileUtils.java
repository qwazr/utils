/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class FileUtils extends org.apache.commons.io.FileUtils {

	public static File createDirectory(final File directoryFile) throws IOException {
		Objects.requireNonNull(directoryFile, "directoryFile is null");
		if (directoryFile.exists()) {
			if (!directoryFile.isDirectory())
				throw new IOException("Not a directory: " + directoryFile);
			return directoryFile;
		}
		if (!directoryFile.mkdir())
			throw new IOException("Can't create the directory");
		return directoryFile;
	}

	/**
	 * Delete a directory and its content
	 *
	 * @param directoryPath the path of the directory
	 * @return the number of item deleted
	 * @throws IOException if any I/O error occurs
	 */
	public static int deleteDirectory(final Path directoryPath) throws IOException {

		AtomicInteger counter = new AtomicInteger();

		Files.walkFileTree(Objects.requireNonNull(directoryPath), new SimpleFileVisitor<Path>() {

			@Override
			public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
				Files.delete(file);
				counter.incrementAndGet();
				return FileVisitResult.CONTINUE;
			}

			@Override
			public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
				Files.delete(dir);
				counter.incrementAndGet();
				return FileVisitResult.CONTINUE;
			}
		});

		return counter.get();
	}

	/**
	 * Quietly delete a directory and its content without throwing any IOException
	 *
	 * @param directoryPath the path of the directory
	 * @return the first IOException which occured (if any)
	 */
	public static IOException deleteDirectoryQuietly(final Path directoryPath) {

		AtomicReference<IOException> firstException = new AtomicReference<>();

		try {
			Files.walkFileTree(Objects.requireNonNull(directoryPath), new SimpleFileVisitor<Path>() {

				@Override
				public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
					try {
						Files.delete(file);
					} catch (IOException e) {
						firstException.compareAndSet(null, e);
					}
					return FileVisitResult.CONTINUE;
				}

				@Override
				public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
					try {
						Files.delete(dir);
					} catch (IOException e) {
						firstException.compareAndSet(null, e);
					}
					return FileVisitResult.CONTINUE;
				}
			});
		} catch (IOException e) {
			firstException.compareAndSet(null, e);
		}

		return firstException.get();
	}
}
