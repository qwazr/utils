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

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.util.Collection;
import java.util.Map;

abstract class FieldExternalizer<T, O> implements Externalizer<T, O> {

	protected final Field field;

	protected FieldExternalizer(final Field field) {
		this.field = field;
	}

	/**
	 * If the value is null, write false (boolean). If not null, it writes true (boolean).
	 *
	 * @param object
	 * @param out
	 * @param <E>
	 * @return null or the value
	 * @throws IllegalAccessException
	 * @throws IOException
	 */
	final protected <E> E checkNullAndGet(final T object, final ObjectOutput out)
			throws IllegalAccessException, IOException {
		final E value = (E) field.get(object);
		if (value == null) {
			out.writeBoolean(false);
			return null;
		}
		out.writeBoolean(true);
		return value;
	}

	protected abstract void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException;

	protected abstract void read(final T object, final ObjectInput in)
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
		if (Long.class.isAssignableFrom(clazz))
			return new LongExternalizer<>(field);
		if (Integer.class.isAssignableFrom(clazz))
			return new IntegerExternalizer<>(field);
		if (Short.class.isAssignableFrom(clazz))
			return new ShortExternalizer<>(field);
		if (Double.class.isAssignableFrom(clazz))
			return new DoubleExternalizer<>(field);
		if (Float.class.isAssignableFrom(clazz))
			return new FloatExternalizer<>(field);
		if (Character.class.isAssignableFrom(clazz))
			return new CharExternalizer<>(field);
		if (Byte.class.isAssignableFrom(clazz))
			return new ByteExternalizer<>(field);
		if (Collection.class.isAssignableFrom(clazz))
			return new CollectionExternalizer(field, clazz);
		if (Map.class.isAssignableFrom(clazz))
			return new MapExternalizer(field, clazz);
		if (Serializable.class.isAssignableFrom(clazz))
			return new SerializedExternalizer(field, clazz);
		throw new IllegalArgumentException("Unsupported class: " + clazz);
	}

	static abstract class AbstractObjectExternalizer<T, O> extends FieldExternalizer<T, O> {

		protected AbstractObjectExternalizer(Field field) {
			super(field);
		}

		@Override
		final protected void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			field.set(object, readObject(in));
		}
	}

	static class StringExternalizer<T> extends AbstractObjectExternalizer<T, String> {

		protected StringExternalizer(final Field field) {
			super(field);
		}

		@Override
		final protected void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final String value = checkNullAndGet(object, out);
			if (value == null)
				return;
			out.writeUTF(value);
		}

		@Override
		final public String readObject(final ObjectInput in) throws IOException {
			return in.readBoolean() ? in.readUTF() : null;
		}
	}

	static class LongExternalizer<T> extends AbstractObjectExternalizer<T, Long> {

		protected LongExternalizer(final Field field) {
			super(field);
		}

		@Override
		final protected void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final Long value = checkNullAndGet(object, out);
			if (value == null)
				return;
			out.writeLong(value);
		}

		@Override
		final public Long readObject(final ObjectInput in) throws IOException {
			return in.readBoolean() ? in.readLong() : null;
		}
	}

	static class IntegerExternalizer<T> extends AbstractObjectExternalizer<T, Integer> {

		protected IntegerExternalizer(final Field field) {
			super(field);
		}

		@Override
		final protected void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final Integer value = checkNullAndGet(object, out);
			if (value == null)
				return;
			out.writeInt(value);
		}

		@Override
		final public Integer readObject(final ObjectInput in) throws IOException {
			return in.readBoolean() ? in.readInt() : null;
		}
	}

	static class ShortExternalizer<T> extends AbstractObjectExternalizer<T, Short> {

		protected ShortExternalizer(final Field field) {
			super(field);
		}

		@Override
		final protected void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final Short value = checkNullAndGet(object, out);
			if (value == null)
				return;
			out.writeShort(value);
		}

		@Override
		final public Short readObject(final ObjectInput in) throws IOException {
			return in.readBoolean() ? in.readShort() : null;
		}
	}

	static class DoubleExternalizer<T> extends AbstractObjectExternalizer<T, Double> {

		protected DoubleExternalizer(final Field field) {
			super(field);
		}

		@Override
		final protected void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final Double value = checkNullAndGet(object, out);
			if (value == null)
				return;
			out.writeDouble(value);
		}

		@Override
		final public Double readObject(final ObjectInput in) throws IOException {
			return in.readBoolean() ? in.readDouble() : null;
		}
	}

	static class FloatExternalizer<T> extends AbstractObjectExternalizer<T, Float> {

		protected FloatExternalizer(final Field field) {
			super(field);
		}

		@Override
		final protected void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final Float value = checkNullAndGet(object, out);
			if (value == null)
				return;
			out.writeFloat(value);
		}

		@Override
		final public Float readObject(final ObjectInput in) throws IOException {
			return in.readBoolean() ? in.readFloat() : null;
		}
	}

	static class CharExternalizer<T> extends AbstractObjectExternalizer<T, Character> {

		protected CharExternalizer(final Field field) {
			super(field);
		}

		@Override
		final protected void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final Character value = checkNullAndGet(object, out);
			if (value == null)
				return;
			out.writeChar(value);
		}

		@Override
		final public Character readObject(final ObjectInput in) throws IOException {
			return in.readBoolean() ? in.readChar() : null;
		}
	}

	static class ByteExternalizer<T> extends AbstractObjectExternalizer<T, Byte> {

		protected ByteExternalizer(final Field field) {
			super(field);
		}

		@Override
		final protected void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final Byte value = checkNullAndGet(object, out);
			if (value == null)
				return;
			out.writeByte(value);
		}

		@Override
		final public Byte readObject(final ObjectInput in) throws IOException {
			return in.readBoolean() ? in.readByte() : null;
		}
	}

	static abstract class ConstructorExternalizer<T, C> extends AbstractObjectExternalizer<T, C> {

		protected final Constructor<? extends C> constructor;

		protected ConstructorExternalizer(final Field field, final Class<? extends C> clazz) {
			super(field);
			try {
				constructor = clazz.getConstructor();
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException("Not empty public constructor for the type " + clazz);
			}
		}

		protected Externalizer<Object, ?> getGeneric(final int pos) {
			final ParameterizedType paramTypes = (ParameterizedType) field.getGenericType();
			return Externalizer.of((Class<?>) paramTypes.getActualTypeArguments()[pos]);
		}
	}

	static class CollectionExternalizer<T> extends ConstructorExternalizer<T, Collection> {

		protected final Externalizer<Object, ?> componentExternalizer;

		protected CollectionExternalizer(final Field field, Class<? extends Collection> clazz) {
			super(field, clazz);
			componentExternalizer = getGeneric(0);
		}

		@Override
		final protected void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final Collection<Object> collection = checkNullAndGet(object, out);
			if (collection == null)
				return;
			out.writeInt(collection.size());
			for (Object o : collection)
				componentExternalizer.writeExternal(o, out);
		}

		@Override
		final public Collection readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			try {
				if (!in.readBoolean())
					return null;
				final Collection collection = constructor.newInstance();
				int size = in.readInt();
				while (size-- > 0)
					collection.add(componentExternalizer.readObject(in));
				return collection;
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	static class MapExternalizer<T> extends ConstructorExternalizer<T, Map> {

		protected final Externalizer<Object, ?> keyExternalizer;
		protected final Externalizer<Object, ?> valueExternalizer;

		protected MapExternalizer(final Field field, Class<? extends Map> clazz) {
			super(field, clazz);
			keyExternalizer = getGeneric(0);
			valueExternalizer = getGeneric(1);
		}

		@Override
		final protected void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final Map<?, ?> map = checkNullAndGet(object, out);
			if (map == null)
				return;
			out.writeInt(map.size());
			for (Map.Entry entry : map.entrySet()) {
				keyExternalizer.writeExternal(entry.getKey(), out);
				valueExternalizer.writeExternal(entry.getValue(), out);
			}
		}

		@Override
		final public Map readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			try {
				if (!in.readBoolean())
					return null;
				final Map map = constructor.newInstance();
				int size = in.readInt();
				while (size-- > 0)
					map.put(keyExternalizer.readObject(in), valueExternalizer.readObject(in));
				return map;
			} catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
				throw new IllegalArgumentException(e);
			}
		}
	}

	static class SerializedExternalizer<T> extends ConstructorExternalizer<T, T> {

		protected SerializedExternalizer(final Field field, final Class<? extends T> clazz) {
			super(field, clazz);
		}

		@Override
		final protected void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final T value = checkNullAndGet(object, out);
			if (value == null)
				return;
			out.writeObject(value);
		}

		@Override
		final public T readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			if (!in.readBoolean())
				return null;
			return (T) in.readObject();
		}
	}
}
