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
package com.qwazr.utils.test;

import com.qwazr.utils.CollectionsUtils;
import com.qwazr.utils.externalizer.Externalizer;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Objects;

public class ExternalizerTest {


	static class Serial implements Externalizable {

		final static Externalizer<Serial> EXTERNALIZER = Externalizer.of(Serial.class);

		final private int integerValue;
		public long longValue;
		final public String string;
		public String emptyObject;
		public double[] primitiveArray;
		String[] objectArray;
		final public ArrayList<String> objectList;
		transient String transientValue;

		public Serial() {
			integerValue = RandomUtils.nextInt(0, Integer.MAX_VALUE);
			longValue = RandomUtils.nextLong(0, Long.MAX_VALUE);
			string = RandomStringUtils.randomAscii(64);
			emptyObject = null;
			primitiveArray = new double[]{RandomUtils.nextDouble(0, Double.MAX_VALUE),
					RandomUtils.nextDouble(0, Double.MAX_VALUE), RandomUtils.nextDouble(0, Double.MAX_VALUE)};
			objectArray =
					new String[]{RandomStringUtils.randomAscii(8), RandomStringUtils.randomAscii(8),
							RandomStringUtils.randomAscii(8)};
			objectList = new ArrayList<>();
			for (int i = 0; i < RandomUtils.nextInt(5, 10); i++)
				objectList.add(RandomStringUtils.randomAscii(8));
			transientValue = RandomStringUtils.randomAscii(12);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Serial))
				return false;
			final Serial s = (Serial) o;
			if (!Objects.equals(string, s.string))
				return false;
			if (!Objects.equals(emptyObject, s.emptyObject))
				return false;
			if (!Objects.deepEquals(primitiveArray, s.primitiveArray))
				return false;
			if (!Objects.deepEquals(objectArray, s.objectArray))
				return false;
			if (!CollectionsUtils.equals(objectList, s.objectList))
				return false;
			return true;
		}

		@Override
		public void writeExternal(final ObjectOutput out) throws IOException {
			EXTERNALIZER.writeExternal(this, out);
		}

		@Override
		public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
			EXTERNALIZER.readExternal(this, in);
		}
	}

	@Test
	public void createExternalizer() {
		try {
			Externalizer.of(Serial.class);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}

	@Test
	public void readWrite() throws IOException, ClassNotFoundException {

		//Write
		final Serial write = new Serial();
		final byte[] byteArray;
		try (ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
				oos.writeObject(write);
			}
			byteArray = bos.toByteArray();
		}

		//Read
		final Serial read;
		try (ByteArrayInputStream bis = new ByteArrayInputStream(byteArray)) {
			try (ObjectInputStream ois = new ObjectInputStream(bis)) {
				read = (Serial) ois.readObject();
			}
		}
		Assert.assertNotNull(read);

		// Check equals
		Assert.assertEquals(write, read);
		Assert.assertNotEquals(write.transientValue, read.transientValue);
	}
}
