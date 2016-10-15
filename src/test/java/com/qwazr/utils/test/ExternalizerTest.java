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
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.util.*;

public class ExternalizerTest {

	static class SerialInner {

		private final String key;
		private final String value;

		public SerialInner() {
			key = RandomStringUtils.randomAscii(8);
			value = RandomStringUtils.randomAscii(8);
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Serial))
				return false;
			final SerialInner s = (SerialInner) o;
			return Objects.equals(key, s.key) && Objects.equals(value, s.value);
		}

	}

	static class Serial extends SerialInner {

		final private int integerValue;
		public long longValue;
		final public String string;
		public String emptyObject;
		public double[] primitiveArray;
		String[] objectArray;
		final public ArrayList<String> stringList;
		final public LinkedHashSet<Integer> integerList;
		final public TreeSet<Long> longList;
		final public ArrayList<Float> floatList;
		final public ArrayList<Double> doubleList;
		final protected Vector<Byte> byteList;
		final public HashSet<Short> shortList;
		final public HashSet<Character> charList;
		final private HashMap<String, Integer> map;
		public SerialInner inner = new SerialInner();
		transient String transientValue;

		public Serial() {
			integerValue = RandomUtils.nextInt(0, Integer.MAX_VALUE);
			longValue = RandomUtils.nextLong(0, Long.MAX_VALUE);
			string = RandomStringUtils.randomAscii(64);
			emptyObject = null;
			primitiveArray = new double[] { RandomUtils.nextDouble(0, Double.MAX_VALUE),
					RandomUtils.nextDouble(0, Double.MAX_VALUE),
					RandomUtils.nextDouble(0, Double.MAX_VALUE) };
			objectArray = new String[] { RandomStringUtils.randomAscii(8),
					RandomStringUtils.randomAscii(8),
					RandomStringUtils.randomAscii(8) };
			stringList = new ArrayList<>();
			for (int i = 0; i < RandomUtils.nextInt(5, 10); i++)
				stringList.add(RandomStringUtils.randomAscii(8));
			integerList = new LinkedHashSet<>();
			for (int i = 0; i < RandomUtils.nextInt(5, 10); i++)
				integerList.add(RandomUtils.nextInt(0, Integer.MAX_VALUE));
			longList = new TreeSet<>();
			for (int i = 0; i < RandomUtils.nextInt(5, 10); i++)
				longList.add(RandomUtils.nextLong(0, Long.MAX_VALUE));
			floatList = new ArrayList<>();
			for (int i = 0; i < RandomUtils.nextInt(5, 10); i++)
				floatList.add(RandomUtils.nextFloat(0, Float.MAX_VALUE));
			doubleList = new ArrayList<>();
			for (int i = 0; i < RandomUtils.nextInt(5, 10); i++)
				doubleList.add(RandomUtils.nextDouble(0, Double.MAX_VALUE));
			byteList = new Vector<>();
			for (int i = 0; i < RandomUtils.nextInt(5, 10); i++)
				byteList.add((byte) RandomUtils.nextInt(0, 128));
			shortList = new HashSet<>();
			for (int i = 0; i < RandomUtils.nextInt(5, 10); i++)
				shortList.add((short) RandomUtils.nextInt(0, Short.MAX_VALUE));
			charList = new HashSet<>();
			for (int i = 0; i < RandomUtils.nextInt(5, 10); i++)
				charList.add((char) RandomUtils.nextInt(0, Short.MAX_VALUE));
			map = new HashMap<>();
			for (int i = 0; i < RandomUtils.nextInt(5, 10); i++)
				map.put(RandomStringUtils.randomAscii(5), RandomUtils.nextInt(0, Short.MAX_VALUE));
			transientValue = RandomStringUtils.randomAscii(12);
		}

		@Override
		public boolean equals(Object o) {
			if (!super.equals(o))
				return false;
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
			if (!CollectionsUtils.equals(stringList, s.stringList))
				return false;
			if (!CollectionsUtils.equals(integerList, s.integerList))
				return false;
			if (!CollectionsUtils.equals(longList, s.longList))
				return false;
			if (!CollectionsUtils.equals(floatList, s.floatList))
				return false;
			if (!CollectionsUtils.equals(doubleList, s.doubleList))
				return false;
			if (!CollectionsUtils.equals(byteList, s.byteList))
				return false;
			if (!CollectionsUtils.equals(charList, s.charList))
				return false;
			if (!CollectionsUtils.equals(shortList, s.shortList))
				return false;
			if (!CollectionsUtils.equals(map, s.map))
				return false;
			if (!Objects.equals(inner, s.inner))
				return false;
			return true;
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
