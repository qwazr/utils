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

import com.qwazr.externalizor.Externalizor;

import java.io.*;

public class SerializationUtils {

	/**
	 * Build a compressed byte array from an serializable object
	 *
	 * @param object     the object to serialize
	 * @param bufferSize the initial sizeof the buffer
	 * @return
	 * @throws IOException
	 */
	public static byte[] toCompressedBytes(final Serializable object, final int bufferSize) throws IOException {
		try (final ByteArrayOutputStream output = new ByteArrayOutputStream(bufferSize)) {
			Externalizor.serialize(object, output);
			return output.toByteArray();
		}
	}

	/**
	 * Fill an object to restore its properties using deserialization (with compaction)
	 *
	 * @param bytes the serialized bytes
	 * @param <T>   the type of the container object
	 * @return the filled object
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static <T extends Serializable> T fromCompressedBytes(final byte[] bytes)
			throws IOException, ClassNotFoundException {
		try (final ByteArrayInputStream input = new ByteArrayInputStream(bytes)) {
			return Externalizor.deserialize(input);
		}
	}

	final public static class NullEmptyObject implements Serializable {

		public final static NullEmptyObject INSTANCE = new NullEmptyObject();
	}

}
