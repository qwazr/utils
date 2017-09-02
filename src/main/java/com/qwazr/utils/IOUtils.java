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
package com.qwazr.utils;

import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IOUtils extends org.apache.commons.io.IOUtils {

	private final static Logger logger = LoggerUtils.getLogger(IOUtils.class);

	public static void close(final AutoCloseable autoCloseable) {
		if (autoCloseable == null)
			return;
		try {
			autoCloseable.close();
		} catch (Exception e) {
			logger.log(Level.WARNING, e, () -> "Close failure on " + autoCloseable);
		}
	}

	public static void close(final AutoCloseable... autoCloseables) {
		if (autoCloseables == null)
			return;
		for (AutoCloseable autoCloseable : autoCloseables)
			close(autoCloseable);
	}

	public static void close(final Collection<? extends AutoCloseable> autoCloseables) {
		if (autoCloseables == null)
			return;
		autoCloseables.forEach(IOUtils::close);
	}

	public static void closeQuietly(final Closeable closeable) {
		if (closeable == null)
			return;
		try {
			closeable.close();
		} catch (IOException e) {
			logger.log(Level.WARNING, e, () -> "Close failure on " + closeable);
		}
	}

	public static void closeQuietly(final Closeable... closeables) {
		if (closeables == null)
			return;
		for (Closeable closeable : closeables)
			closeQuietly(closeable);
	}

	public static void closeQuietly(final Collection<? extends Closeable> closeables) {
		if (closeables == null)
			return;
		closeables.forEach(IOUtils::closeQuietly);
	}

	public static void closeObjects(final Collection<?> objects) {
		if (objects == null)
			return;
		objects.forEach(object -> {
			if (object instanceof Closeable)
				close((Closeable) object);
		});
	}

	public static int copy(InputStream inputStream, File destFile) throws IOException {
		try (final FileOutputStream fos = new FileOutputStream(destFile)) {
			try (final BufferedOutputStream bos = new BufferedOutputStream(fos)) {
				return copy(inputStream, bos);
			}
		}
	}

	public static StringBuilder copy(InputStream inputStream, StringBuilder sb, String charsetName,
			boolean bCloseInputStream) throws IOException {
		if (inputStream == null)
			return sb;
		if (sb == null)
			sb = new StringBuilder();
		Charset charset = Charset.forName(charsetName);
		byte[] buffer = new byte[16384];
		int length;
		while ((length = inputStream.read(buffer)) != -1)
			sb.append(new String(buffer, 0, length, charset));
		if (bCloseInputStream)
			inputStream.close();
		return sb;
	}

	public static void appendLines(File file, String... lines) throws IOException {
		try (final FileWriter fw = new FileWriter(file, true)) {
			try (final PrintWriter pw = new PrintWriter(fw)) {
				for (String line : lines)
					pw.println(line);
			}
		}
	}

	public interface CloseableContext extends Closeable {

		<T extends AutoCloseable> T add(T autoCloseable);

		void close(AutoCloseable autoCloseable);
	}

	public static class CloseableList implements CloseableContext {

		private final LinkedHashSet<AutoCloseable> autoCloseables;

		public CloseableList() {
			autoCloseables = new LinkedHashSet<>();
		}

		@Override
		public <T extends AutoCloseable> T add(T autoCloseable) {
			synchronized (autoCloseables) {
				autoCloseables.add(autoCloseable);
				return autoCloseable;
			}
		}

		@Override
		public void close(AutoCloseable autoCloseable) {
			IOUtils.close(autoCloseable);
			synchronized (autoCloseables) {
				autoCloseables.remove(autoCloseable);
			}
		}

		@Override
		public void close() {
			synchronized (autoCloseables) {
				IOUtils.close(autoCloseables);
				autoCloseables.clear();
			}
		}

	}

	/**
	 * Extract the content of a file to a string
	 *
	 * @param file the file
	 * @return the content of the file as a string
	 * @throws IOException if any I/O error occured
	 */
	public static String readFileAsString(File file) throws IOException {
		final FileReader reader = new FileReader(file);
		try {
			return toString(reader);
		} finally {
			closeQuietly(reader);
		}
	}

	/**
	 * Write the string to a file
	 *
	 * @param content the text to write
	 * @param file    the destination file
	 * @throws IOException if any I/O error occured
	 */
	public static void writeStringAsFile(String content, File file) throws IOException {
		final FileWriter writer = new FileWriter(file);
		try {
			writer.write(content);
		} finally {
			closeQuietly(writer);
		}
	}

}
