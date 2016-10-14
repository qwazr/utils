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
import java.lang.reflect.Field;

class PrimitiveExternalizer {

	static FieldExternalizer newPrimitive(final Field field, final Class<?> clazz) {
		if (Integer.TYPE.equals(clazz))
			return new IntegerExternalizer(field);
		if (Short.TYPE.equals(clazz))
			return new ShortExternalizer(field);
		if (Long.TYPE.equals(clazz))
			return new LongExternalizer(field);
		if (Float.TYPE.equals(clazz))
			return new FloatExternalizer(field);
		if (Double.TYPE.equals(clazz))
			return new DoubleExternalizer(field);
		if (Boolean.TYPE.equals(clazz))
			return new BooleanExternalizer(field);
		if (Byte.TYPE.equals(clazz))
			return new ByteExternalizer(field);
		if (Character.TYPE.equals(clazz))
			return new CharExternalizer(field);
		throw new IllegalArgumentException("Unsupported primitive type: " + clazz);
	}

	static class IntegerExternalizer<T> extends FieldExternalizer<T> {

		IntegerExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			out.writeInt(field.getInt(object));
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			field.setInt(object, in.readInt());
		}
	}

	static class LongExternalizer<T> extends FieldExternalizer<T> {

		LongExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			out.writeLong(field.getLong(object));
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			field.setLong(object, in.readLong());
		}
	}

	static class FloatExternalizer<T> extends FieldExternalizer<T> {

		FloatExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			out.writeFloat(field.getFloat(object));
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			field.setFloat(object, in.readFloat());
		}
	}

	static class DoubleExternalizer<T> extends FieldExternalizer<T> {

		DoubleExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			out.writeDouble(field.getDouble(object));
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			field.setDouble(object, in.readDouble());
		}
	}

	static class ShortExternalizer<T> extends FieldExternalizer<T> {

		ShortExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			out.writeShort(field.getShort(object));
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			field.setShort(object, in.readShort());
		}
	}

	static class BooleanExternalizer<T> extends FieldExternalizer<T> {

		BooleanExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			out.writeBoolean(field.getBoolean(object));
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			field.setBoolean(object, in.readBoolean());
		}
	}

	static class ByteExternalizer<T> extends FieldExternalizer<T> {

		ByteExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			out.writeByte(field.getByte(object));
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			field.setByte(object, in.readByte());
		}
	}

	static class CharExternalizer<T> extends FieldExternalizer<T> {

		CharExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			out.writeChar(field.getChar(object));
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			field.setChar(object, in.readChar());
		}
	}
}
