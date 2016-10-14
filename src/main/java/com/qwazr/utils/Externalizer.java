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
package com.qwazr.utils;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;

public class Externalizer<T extends Externalizable> {

	private final Collection<Field> declaredFields;

	public Externalizer(Class<T> clazz) {
		declaredFields = new ArrayList<>();
		final Field[] fields = clazz.getDeclaredFields();
		for (Field field : fields) {
			if (Serializable.class.isAssignableFrom(field.getClass()))
				continue;
			final int modifier = field.getModifiers();
			if (Modifier.isStatic(modifier) || Modifier.isTransient(modifier))
				continue;
			field.setAccessible(true);
			declaredFields.add(field);
		}
	}

	public void writeExternal(final T object, final ObjectOutput out) throws IOException {
		try {
			for (Field field : declaredFields) {
				final Object value = field.get(object);
				if (value == null) {
					out.writeBoolean(false);
					continue;
				}
				out.writeBoolean(true);
				out.writeObject(value);
			}
		} catch (IllegalAccessException e) {
			throw new IOException(e);
		}
	}

	public void readExternal(final T object, final ObjectInput in) throws IOException, ClassNotFoundException {
		try {
			for (Field field : declaredFields) {
				if (!in.readBoolean()) {
					field.set(object, null);
					continue;
				}
				field.set(object, in.readObject());
			}
		} catch (IllegalAccessException e) {
			throw new IOException(e);
		}
	}
}
