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

import com.qwazr.utils.server.ServerException;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.*;

public class FieldMapWrapper<T> {

	protected final Map<String, Field> fieldMap;
	protected final Class<T> objectClass;

	public FieldMapWrapper(Map<String, Field> fieldMap, Class<T> objectClass) {
		this.fieldMap = fieldMap;
		this.objectClass = objectClass;
	}

	/**
	 * Build a new Map by reading the IndexField annotations
	 *
	 * @param row the record
	 * @return a new Map
	 */
	protected Map<String, Object> newMap(final T row) {
		final Map<String, Object> map = new HashMap<>();
		fieldMap.forEach((name, field) -> {
			try {
				Object value = field.get(row);
				if (value == null)
					return;
				if (value instanceof Number || value instanceof String) {
					map.put(name, value);
					return;
				}
				if (value instanceof Collection) {
					map.put(name, value);
					return;
				}
				if (value instanceof Map) {
					map.put(name, value);
					return;
				}
				if (value.getClass().isArray()) {
					map.put(name, value);
					return;
				}
				if (value instanceof Serializable) {
					map.put(name, SerializationUtils.toExternalizorBytes((Serializable) value));
					return;
				}
			} catch (IOException | ReflectiveOperationException e) {
				throw new ServerException("Cannot convert the field " + name, e);
			}
		});
		return map.isEmpty() ? null : map;
	}

	/**
	 * Buid a collection of Map by reading the IndexFields of the annotated documents
	 *
	 * @param rows a collection of records
	 * @return a new array of map objects
	 */
	protected List<Map<String, Object>> newMapCollection(final Collection<T> rows) {
		if (rows == null || rows.isEmpty())
			return null;
		final List<Map<String, Object>> list = new ArrayList<>(rows.size());
		rows.forEach(row -> list.add(newMap(row)));
		return list;
	}

	protected T toRecord(final Map<String, Object> fields) throws ReflectiveOperationException {
		if (fields == null)
			return null;
		final T record = objectClass.newInstance();
		fields.forEach((fieldName, fieldValue) -> {
			final Field field = fieldMap.get(fieldName);
			if (field == null || fieldValue == null)
				return;
			final Class<?> fieldType = field.getType();
			final Class<?> fieldValueType = fieldValue.getClass();
			try {
				if (fieldType.isAssignableFrom(fieldValueType)) {
					field.set(record, fieldValue);
					return;
				}
				if (fieldValue instanceof Number) {
					final Number finalValueNumber = (Number) fieldValue;
					if (fieldType.isAssignableFrom(Long.class))
						field.set(record, finalValueNumber.longValue());
					else if (fieldType.isAssignableFrom(Integer.class))
						field.set(record, finalValueNumber.intValue());
					else if (fieldType.isAssignableFrom(Float.class))
						field.set(record, finalValueNumber.floatValue());
					else if (fieldType.isAssignableFrom(Double.class))
						field.set(record, finalValueNumber.doubleValue());
					return;
				}
				if (fieldValue instanceof Collection) {
					final Collection<?> fieldValues = (Collection<?>) fieldValue;
					if (fieldValues.isEmpty())
						return;
					field.set(record, fieldValues.iterator().next());
					return;
				}
				if (fieldValueType.isArray()) {
					final int length = Array.getLength(fieldValue);
					if (length == 0)
						return;
					if (Collection.class.isAssignableFrom(fieldType)) {
						final Collection fieldValues = (Collection) fieldType.newInstance();
						for (int i = 0; i < length; i++)
							fieldValues.add(Array.get(fieldValue, i));
						field.set(record, fieldValues);
					} else
						field.set(record, Array.get(fieldValue, 0));
					return;
				}
				if (Serializable.class.isAssignableFrom(fieldType)) {
					field.set(record,
							SerializationUtils.fromExternalizorBytes(Base64.getDecoder().decode((String) fieldValue),
									(Class<? extends Serializable>) fieldType));
					return;
				}
				throw new UnsupportedOperationException(
						"Field " + fieldName + " not assignable: " + fieldType + " -> " + fieldValueType);
			} catch (ReflectiveOperationException | IOException e) {
				throw new ServerException(e);
			}
		});
		return record;
	}

	protected List<T> toRecords(final List<LinkedHashMap<String, Object>> docs) {
		if (docs == null)
			return null;
		final List<T> records = new ArrayList<>();
		docs.forEach(doc -> {
			try {
				records.add(toRecord(doc));
			} catch (ReflectiveOperationException e) {
				throw new RuntimeException(e);
			}
		});
		return records;
	}
}
