/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import java.io.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class SerializationUtils extends
		org.apache.commons.lang3.SerializationUtils {

	/**
	 * Read an object from a file using a buffered stream and GZIP compression
	 *
	 * @param file the destination file
	 * @param <T>  the type of the object
	 * @return the deserialized object
	 * @throws IOException
	 */
	public static <T> T deserialize(final File file) throws IOException {
		try (final FileInputStream is = new FileInputStream(file)) {
			try (final BufferedInputStream bis = new BufferedInputStream(is)) {
				try (final GZIPInputStream zis = new GZIPInputStream(bis)) {
					return SerializationUtils.deserialize(zis);
				}
			}
		}
	}

	/**
	 * Write an object to a file using a buffered stream and GZIP compression.
	 *
	 * @param obj  the object to write
	 * @param file the destination file
	 * @throws IOException
	 */
	public static void serialize(final Serializable obj, final File file)
			throws IOException {
		try (final FileOutputStream os = new FileOutputStream(file)) {
			try (final BufferedOutputStream bos = new BufferedOutputStream(os)) {
				try (final GZIPOutputStream zos = new GZIPOutputStream(bos)) {
					SerializationUtils.serialize(obj, zos);
				}
			}
		}
	}

	/**
	 * Build a byte array from an externlizable object
	 *
	 * @param object     the object to serialize
	 * @param bufferSize the initial sizeof the buffer
	 * @return a byte array
	 * @throws IOException
	 */
	public static byte[] getBytes(final Externalizable object, final int bufferSize) throws IOException {
		try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(bufferSize)) {
			try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
				object.writeExternal(oos);
				oos.flush();
				return bos.toByteArray();
			}
		}
	}

	final public static class NullEmptyObject implements Serializable {

		public final static NullEmptyObject INSTANCE = new NullEmptyObject();
	}

}
