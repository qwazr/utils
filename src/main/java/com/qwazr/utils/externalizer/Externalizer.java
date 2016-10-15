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

public interface Externalizer<T, O> {

	static <E> Externalizer<E, E> of(Class<?> clazz) {
		if (String.class.isAssignableFrom(clazz))
			return (Externalizer<E, E>) new StringExternalizer();
		if (Long.class.isAssignableFrom(clazz))
			return (Externalizer<E, E>) new LongExternalizer();
		if (Integer.class.isAssignableFrom(clazz))
			return (Externalizer<E, E>) new IntegerExternalizer();
		if (Short.class.isAssignableFrom(clazz))
			return (Externalizer<E, E>) new ShortExternalizer();
		if (Double.class.isAssignableFrom(clazz))
			return (Externalizer<E, E>) new DoubleExternalizer();
		if (Float.class.isAssignableFrom(clazz))
			return (Externalizer<E, E>) new FloatExternalizer();
		if (Character.class.isAssignableFrom(clazz))
			return (Externalizer<E, E>) new CharExternalizer();
		if (Byte.class.isAssignableFrom(clazz))
			return (Externalizer<E, E>) new ByteExternalizer();
		return new ClassExternalizer(clazz);
	}

	void writeExternal(final T object, final ObjectOutput out) throws IOException;

	void readExternal(final T object, final ObjectInput in) throws IOException, ClassNotFoundException;

	O readObject(final ObjectInput in) throws IOException, ClassNotFoundException;

	abstract class SimpleExternalizer<T> implements Externalizer<T, T> {

		@Override
		final public void readExternal(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException {
			throw new IllegalArgumentException("Not available");
		}
	}

	class StringExternalizer extends SimpleExternalizer<String> {

		@Override
		final public void writeExternal(final String object, final ObjectOutput out) throws IOException {
			if (object != null) {
				out.writeBoolean(true);
				out.writeUTF(object);
			} else
				out.writeBoolean(false);
		}

		@Override
		final public String readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			return in.readBoolean() ? in.readUTF() : null;
		}
	}

	class LongExternalizer extends SimpleExternalizer<Long> {

		@Override
		final public void writeExternal(final Long object, final ObjectOutput out) throws IOException {
			if (object != null) {
				out.writeBoolean(true);
				out.writeLong(object);
			} else
				out.writeBoolean(false);
		}

		@Override
		final public Long readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			return in.readBoolean() ? in.readLong() : null;
		}
	}

	class IntegerExternalizer extends SimpleExternalizer<Integer> {

		@Override
		final public void writeExternal(final Integer object, final ObjectOutput out) throws IOException {
			if (object != null) {
				out.writeBoolean(true);
				out.writeInt(object);
			} else
				out.writeBoolean(false);
		}

		@Override
		final public Integer readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			return in.readBoolean() ? in.readInt() : null;
		}
	}

	class ShortExternalizer extends SimpleExternalizer<Short> {

		@Override
		final public void writeExternal(final Short object, final ObjectOutput out) throws IOException {
			if (object != null) {
				out.writeBoolean(true);
				out.writeShort(object);
			} else
				out.writeBoolean(false);
		}

		@Override
		final public Short readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			return in.readBoolean() ? in.readShort() : null;
		}
	}

	class DoubleExternalizer extends SimpleExternalizer<Double> {

		@Override
		final public void writeExternal(final Double object, final ObjectOutput out) throws IOException {
			if (object != null) {
				out.writeBoolean(true);
				out.writeDouble(object);
			} else
				out.writeBoolean(false);
		}

		@Override
		final public Double readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			return in.readBoolean() ? in.readDouble() : null;
		}
	}

	class FloatExternalizer extends SimpleExternalizer<Float> {

		@Override
		final public void writeExternal(final Float object, final ObjectOutput out) throws IOException {
			if (object != null) {
				out.writeBoolean(true);
				out.writeFloat(object);
			} else
				out.writeBoolean(false);
		}

		@Override
		final public Float readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			return in.readBoolean() ? in.readFloat() : null;
		}
	}

	class ByteExternalizer extends SimpleExternalizer<Byte> {

		@Override
		final public void writeExternal(final Byte object, final ObjectOutput out) throws IOException {
			if (object != null) {
				out.writeBoolean(true);
				out.writeByte(object);
			} else
				out.writeBoolean(false);
		}

		@Override
		final public Byte readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			return in.readBoolean() ? in.readByte() : null;
		}
	}

	class CharExternalizer extends SimpleExternalizer<Character> {

		@Override
		final public void writeExternal(final Character object, final ObjectOutput out) throws IOException {
			if (object != null) {
				out.writeBoolean(true);
				out.writeChar(object);
			} else
				out.writeBoolean(false);
		}

		@Override
		final public Character readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			return in.readBoolean() ? in.readChar() : null;
		}
	}
}
