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

abstract class ArrayExternalizer<E> extends FieldExternalizer<E> {

	protected ArrayExternalizer(final Field field) {
		super(field);
	}

	/**
	 * Check if the array should be null. -1 is returned if the array is null.
	 *
	 * @param object
	 * @param in
	 * @return
	 * @throws IOException
	 * @throws IllegalAccessException
	 */
	final protected int setCheckArray(final Object object, final ObjectInput in)
			throws IOException, IllegalAccessException {
		if (checkIsNotNull(object, in))
			return in.readInt();
		else
			return -1;
	}

	static FieldExternalizer newArray(final Field field, final Class<?> clazz) {
		final Class<?> componentType = clazz.getComponentType();
		if (componentType.isPrimitive()) {
			if (Integer.TYPE.equals(componentType))
				return new IntegerExternalizer(field);
			if (Short.TYPE.equals(componentType))
				return new ShortExternalizer(field);
			if (Long.TYPE.equals(componentType))
				return new LongExternalizer(field);
			if (Float.TYPE.equals(componentType))
				return new FloatExternalizer(field);
			if (Double.TYPE.equals(componentType))
				return new DoubleExternalizer(field);
			if (Boolean.TYPE.equals(componentType))
				return new BooleanExternalizer(field);
			if (Byte.TYPE.equals(componentType))
				return new ByteExternalizer(field);
			if (Character.TYPE.equals(componentType))
				return new CharExternalizer(field);
		}
		if (String.class.isAssignableFrom(componentType))
			return new StringExternalizer(field);
		return new ObjectExternalizer(field);
	}

	static class IntegerExternalizer<T> extends ArrayExternalizer<T> {

		IntegerExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			final int[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (int item : array)
				out.writeInt(item);
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			int size = setCheckArray(object, in);
			if (size == -1)
				return;
			final int[] array = new int[size];
			for (int i = 0; i < size; i++)
				array[i] = in.readInt();
			field.set(object, array);
		}
	}

	static class LongExternalizer<T> extends ArrayExternalizer<T> {

		LongExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			final long[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (long item : array)
				out.writeLong(item);
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			int size = setCheckArray(object, in);
			if (size == -1)
				return;
			final long[] array = new long[size];
			for (int i = 0; i < size; i++)
				array[i] = in.readLong();
			field.set(object, array);
		}
	}

	static class ShortExternalizer<T> extends ArrayExternalizer<T> {

		ShortExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			final short[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (short item : array)
				out.writeShort(item);
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			int size = setCheckArray(object, in);
			if (size == -1)
				return;
			final short[] array = new short[size];
			for (int i = 0; i < size; i++)
				array[i] = in.readShort();
			field.set(object, array);
		}
	}

	static class DoubleExternalizer<T> extends ArrayExternalizer<T> {

		DoubleExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			final double[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (double item : array)
				out.writeDouble(item);
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			int size = setCheckArray(object, in);
			if (size == -1)
				return;
			final double[] array = new double[size];
			for (int i = 0; i < size; i++)
				array[i] = in.readDouble();
			field.set(object, array);
		}
	}

	static class FloatExternalizer<T> extends ArrayExternalizer<T> {

		FloatExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			final float[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (float item : array)
				out.writeFloat(item);
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			int size = setCheckArray(object, in);
			if (size == -1)
				return;
			final float[] array = new float[size];
			for (int i = 0; i < size; i++)
				array[i] = in.readFloat();
			field.set(object, array);
		}
	}

	static class BooleanExternalizer<T> extends ArrayExternalizer<T> {

		BooleanExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			final boolean[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (boolean item : array)
				out.writeBoolean(item);
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			int size = setCheckArray(object, in);
			if (size == -1)
				return;
			final boolean[] array = new boolean[size];
			for (int i = 0; i < size; i++)
				array[i] = in.readBoolean();
			field.set(object, array);
		}
	}

	static class ByteExternalizer<T> extends ArrayExternalizer<T> {

		ByteExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			final byte[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (byte item : array)
				out.writeByte(item);
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			int size = setCheckArray(object, in);
			if (size == -1)
				return;
			final byte[] array = new byte[size];
			for (int i = 0; i < size; i++)
				array[i] = in.readByte();
			field.set(object, array);
		}
	}

	static class CharExternalizer<T> extends ArrayExternalizer<T> {

		CharExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			final byte[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (byte item : array)
				out.writeChar(item);
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			int size = setCheckArray(object, in);
			if (size == -1)
				return;
			final char[] array = new char[size];
			for (int i = 0; i < size; i++)
				array[i] = in.readChar();
			field.set(object, array);
		}
	}

	static class StringExternalizer<T> extends ArrayExternalizer<T> {

		StringExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			final String[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (String item : array) {
				if (item == null) {
					out.writeBoolean(false);
					continue;
				}
				out.writeBoolean(true);
				out.writeUTF(item);
			}
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			int size = setCheckArray(object, in);
			if (size == -1)
				return;
			final String[] array = new String[size];
			for (int i = 0; i < size; i++)
				if (in.readBoolean())
					array[i] = in.readUTF();
			field.set(object, array);
		}
	}

	static class ObjectExternalizer<T> extends ArrayExternalizer<T> {

		ObjectExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out)
				throws IOException, IllegalAccessException {
			final Object[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (Object item : array) {
				if (item == null) {
					out.writeBoolean(false);
					continue;
				}
				out.writeBoolean(true);
				out.writeObject(item);
			}
		}

		@Override
		final public void read(final T object, final ObjectInput in)
				throws IOException, ClassNotFoundException, IllegalAccessException {
			int size = setCheckArray(object, in);
			if (size == -1)
				return;
			final Object[] array = new Object[size];
			for (int i = 0; i < size; i++)
				if (in.readBoolean())
					array[i] = in.readObject();
			field.set(object, array);
		}
	}
}
