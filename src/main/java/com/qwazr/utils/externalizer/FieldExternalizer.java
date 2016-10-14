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
package com.qwazr.utils.externalizer;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

abstract class FieldExternalizer<T> implements Externalizer<T> {

	protected final Field field;

	protected FieldExternalizer(final Field field) {
		this.field = field;
	}

	/**
	 * If the value is null, write false (boolean). If not null, it writes true (boolean).
	 *
	 * @param object
	 * @param out
	 * @param <T>
	 * @return null or the value
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	final protected <T> T checkNullAndGet(final Object object, final ObjectOutput out)
			throws IllegalAccessException, IOException {
		final T value = (T) field.get(object);
		if (value == null) {
			out.writeBoolean(false);
			return null;
		}
		out.writeBoolean(true);
		return value;
	}

	final protected boolean checkIsNotNull(final Object object, final ObjectInput in)
			throws IOException, IllegalAccessException {
		if (in.readBoolean())
			return true;
		field.set(object, null);
		return false;
	}

	protected abstract void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException;

	protected abstract void read(final T object, final ObjectInput out)
			throws IOException, ClassNotFoundException, IllegalAccessException;

	@Override
	final public void writeExternal(final T object, final ObjectOutput out) throws IOException {
		try {
			write(object, out);
		} catch (IllegalAccessException e) {
			throw new IOException(e);
		}
	}

	@Override
	final public void readExternal(final T object, final ObjectInput in) throws IOException, ClassNotFoundException {
		try {
			read(object, in);
		} catch (IllegalAccessException e) {
			throw new IOException(e);
		}
	}

	static Externalizer of(final Field field, final Class<?> clazz) {
		if (clazz.isPrimitive())
			return PrimitiveExternalizer.newPrimitive(field, clazz);
		if (clazz.isArray())
			return ArrayExternalizer.newArray(field, clazz);
		if (String.class.isAssignableFrom(clazz))
			return new StringExternalizer<>(field);
		if (Collection.class.isAssignableFrom(clazz))
			return new CollectionExternalizer<>(field);
		return new ClassExternalizer<>(clazz);
	}

	static class StringExternalizer<T> extends FieldExternalizer<T> {

		protected StringExternalizer(final Field field) {
			super(field);
		}

		@Override
		protected void write(T object, ObjectOutput out) throws IOException, IllegalAccessException {
			final String value = checkNullAndGet(object, out);
			if (value == null)
				return;
			out.writeUTF(value);
		}

		@Override
		protected void read(T object, ObjectInput out)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			if (checkIsNotNull(object, out))
				field.set(object, out.readUTF());
		}

	}

	static class CollectionExternalizer<T> extends FieldExternalizer<T> {

		private final Constructor<? extends Collection> collectionConstructor;
		private final Externalizer<Object> componentExternalizer;

		protected CollectionExternalizer(final Field field, Class<? extends Collection> clazz) {
			super(field);
			try {
				collectionConstructor = clazz.getConstructor();
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException("Not empty public constructor for the type " + clazz);
			}
			componentExternalizer = Externalizer.of(field.getGenericType().getClass());
		}

		@Override
		protected void write(T object, ObjectOutput out) throws IOException, IllegalAccessException {
			final Collection<Object> collection = checkNullAndGet(object, out);
			if (collection == null)
				return;
			out.writeInt(collection.size());
			for (Object o : collection)
				componentExternalizer.writeExternal(o, out);
		}

		@Override
		protected void read(T object, ObjectInput out)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			if (!checkIsNotNull(object, out))
				return;
			try {
				final Collection collection = collectionConstructor.newInstance();
				int size = out.readInt();
				while (size-- > 0)
					componentExternalizer.
					field.set(object, out.readUTF());
			} catch (InstantiationException | InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}
}
