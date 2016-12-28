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

import com.qwazr.utils.AnnotationsUtils;
import com.qwazr.utils.FieldMapWrapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class FieldMapWrapperTest {

	private static Wrapper wrapper;

	@Test
	public void test100createWrapper() {
		Map<String, Field> fieldMap = new HashMap<>();
		AnnotationsUtils.browseFieldsRecursive(Record.class, field -> {
			field.setAccessible(true);
			fieldMap.put(field.getName(), field);
		});
		wrapper = new Wrapper(fieldMap);
		Assert.assertNotNull(wrapper);
	}

	@Test
	public void test200newMap() {
		Record record = new Record(RandomUtils.nextLong(), RandomStringUtils.randomAscii(10));
		Map<String, Object> map = wrapper.newMap(record);
		Assert.assertNotNull(map);
		Assert.assertEquals(record, map);
	}

	@Test
	public void test300newMapCollection() {
		Record record1 = new Record(RandomUtils.nextLong(), RandomStringUtils.randomAscii(10));
		Record record2 = new Record(RandomUtils.nextLong(), RandomStringUtils.randomAscii(10));
		List<Map<String, Object>> mapCollection = wrapper.newMapCollection(Arrays.asList(record1, record2));
		Assert.assertNotNull(mapCollection);
		Assert.assertEquals(2, mapCollection.size());
		Assert.assertEquals(record1, mapCollection.get(0));
		Assert.assertEquals(record2, mapCollection.get(1));
	}

	@Test
	public void test400newMapArray() {
		Record record = new Record(RandomUtils.nextLong(), RandomStringUtils.randomAscii(10));
		Map<String, Object> map = wrapper.newMap(record);
		Assert.assertNotNull(map);
		Assert.assertEquals(record.title, map.get("title"));
		Assert.assertEquals(record.id, map.get("id"));
	}

	private Map<String, Object> getRandom() {
		Map<String, Object> map = new HashMap<>();
		map.put("id", RandomUtils.nextLong());
		map.put("title", RandomStringUtils.randomAscii(10));
		return map;
	}

	@Test
	public void test500toRecord() throws ReflectiveOperationException {
		Map map = getRandom();
		Record record = wrapper.toRecord(map);
		Assert.assertNotNull(record);
		Assert.assertEquals(record, map);
	}

	@Test
	public void test600collectionToRecords() {
		Map<String, Object> map1 = getRandom();
		Map<String, Object> map2 = getRandom();
		List<Record> records = wrapper.toRecords(Arrays.asList(map1, map2));
		Assert.assertNotNull(records);
		Assert.assertEquals(2, records.size());
		Assert.assertEquals(records.get(0), map1);
		Assert.assertEquals(records.get(1), map2);
	}

	@Test
	public void test700arrayToRecords() {
		Map<String, Object> map1 = getRandom();
		Map<String, Object> map2 = getRandom();
		List<Record> records = wrapper.toRecords(map1, map2);
		Assert.assertNotNull(records);
		Assert.assertEquals(2, records.size());
		Assert.assertEquals(records.get(0), map1);
		Assert.assertEquals(records.get(1), map2);
	}

	public static class Record {

		final Long id;
		final String title;

		Record(Long id, String title) {
			this.id = id;
			this.title = title;
		}

		public Record() {
			this(null, null);
		}

		@Override
		public boolean equals(Object object) {
			if (object == null)
				return false;
			if (object instanceof Record) {
				Record r = (Record) object;
				return Objects.equals(id, r.id) && Objects.equals(title, r.title);
			}
			if (object instanceof Map) {
				Map m = (Map) object;
				return Objects.equals(id, m.get("id")) && Objects.equals(title, m.get("title"));
			}
			return false;
		}

	}

	class Wrapper extends FieldMapWrapper<Record> {

		Wrapper(Map<String, Field> fieldMap) {
			super(fieldMap, Record.class);
		}

		@Override
		protected Map<String, Object> newMap(final Record record) {
			return super.newMap(record);
		}

		@Override
		protected List<Map<String, Object>> newMapCollection(final Collection<Record> records) {
			return super.newMapCollection(records);
		}

		@Override
		protected List<Map<String, Object>> newMapArray(final Record... records) {
			return super.newMapArray(records);
		}

		@Override
		protected Record toRecord(Map map) throws ReflectiveOperationException {
			return super.toRecord(map);
		}

		@Override
		protected List<Record> toRecords(Collection<Map<String, Object>> mapCollection) {
			return super.toRecords(mapCollection);
		}

		@Override
		protected List<Record> toRecords(Map<String, Object>... mapArray) {
			return super.toRecords(mapArray);
		}

	}
}
