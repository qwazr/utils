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

abstract class ArrayExternalizer<E, O> extends FieldExternalizer.AbstractObjectExternalizer<E, O> {

	protected ArrayExternalizer(final Field field) {
		super(field);
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

	static class IntegerExternalizer<T> extends ArrayExternalizer<T, int[]> {

		IntegerExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final int[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (int item : array)
				out.writeInt(item);
		}

		@Override
		public int[] readObject(ObjectInput in) throws IOException {
			if (!in.readBoolean())
				return null;
			final int[] array = new int[in.readInt()];
			for (int i = 0; i < array.length; i++)
				array[i] = in.readInt();
			return array;
		}
	}

	static class LongExternalizer<T> extends ArrayExternalizer<T, long[]> {

		LongExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final long[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (long item : array)
				out.writeLong(item);
		}

		@Override
		final public long[] readObject(final ObjectInput in) throws IOException {
			if (!in.readBoolean())
				return null;
			final long[] array = new long[in.readInt()];
			for (int i = 0; i < array.length; i++)
				array[i] = in.readLong();
			return array;
		}
	}

	static class ShortExternalizer<T> extends ArrayExternalizer<T, short[]> {

		ShortExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final short[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (short item : array)
				out.writeShort(item);
		}

		@Override
		final public short[] readObject(final ObjectInput in) throws IOException {
			if (!in.readBoolean())
				return null;
			final short[] array = new short[in.readInt()];
			for (int i = 0; i < array.length; i++)
				array[i] = in.readShort();
			return array;
		}
	}

	static class DoubleExternalizer<T> extends ArrayExternalizer<T, double[]> {

		DoubleExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final double[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (double item : array)
				out.writeDouble(item);
		}

		@Override
		final public double[] readObject(final ObjectInput in) throws IOException {
			if (!in.readBoolean())
				return null;
			final double[] array = new double[in.readInt()];
			for (int i = 0; i < array.length; i++)
				array[i] = in.readDouble();
			return array;
		}
	}

	static class FloatExternalizer<T> extends ArrayExternalizer<T, float[]> {

		FloatExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final float[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (float item : array)
				out.writeFloat(item);
		}

		@Override
		final public float[] readObject(final ObjectInput in) throws IOException {
			if (!in.readBoolean())
				return null;
			final float[] array = new float[in.readInt()];
			for (int i = 0; i < array.length; i++)
				array[i] = in.readFloat();
			return array;
		}
	}

	static class BooleanExternalizer<T> extends ArrayExternalizer<T, boolean[]> {

		BooleanExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final boolean[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (boolean item : array)
				out.writeBoolean(item);
		}

		@Override
		final public boolean[] readObject(final ObjectInput in) throws IOException {
			if (!in.readBoolean())
				return null;
			final boolean[] array = new boolean[in.readInt()];
			for (int i = 0; i < array.length; i++)
				array[i] = in.readBoolean();
			return array;
		}

	}

	static class ByteExternalizer<T> extends ArrayExternalizer<T, byte[]> {

		ByteExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final byte[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (byte item : array)
				out.writeByte(item);
		}

		@Override
		final public byte[] readObject(final ObjectInput in) throws IOException {
			if (!in.readBoolean())
				return null;
			final byte[] array = new byte[in.readInt()];
			for (int i = 0; i < array.length; i++)
				array[i] = in.readByte();
			return array;
		}
	}

	static class CharExternalizer<T> extends ArrayExternalizer<T, char[]> {

		CharExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
			final byte[] array = checkNullAndGet(object, out);
			if (array == null)
				return;
			out.writeInt(array.length);
			for (byte item : array)
				out.writeChar(item);
		}

		@Override
		final public char[] readObject(final ObjectInput in) throws IOException {
			if (!in.readBoolean())
				return null;
			final char[] array = new char[in.readInt()];
			for (int i = 0; i < array.length; i++)
				array[i] = in.readChar();
			return array;
		}
	}

	static class StringExternalizer<T> extends ArrayExternalizer<T, String[]> {

		StringExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
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
		final public String[] readObject(final ObjectInput in) throws IOException {
			if (!in.readBoolean())
				return null;
			final String[] array = new String[in.readInt()];
			for (int i = 0; i < array.length; i++)
				array[i] = in.readBoolean() ? in.readUTF() : null;
			return array;
		}
	}

	static class ObjectExternalizer<T> extends ArrayExternalizer<T, Object[]> {

		ObjectExternalizer(final Field field) {
			super(field);
		}

		@Override
		final public void write(final T object, final ObjectOutput out) throws IOException, IllegalAccessException {
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
		final public Object[] readObject(final ObjectInput in) throws IOException, ClassNotFoundException {
			if (!in.readBoolean())
				return null;
			final Object[] array = new Object[in.readInt()];
			for (int i = 0; i < array.length; i++)
				array[i] = in.readBoolean() ? in.readObject() : null;
			return array;
		}
	}
}
