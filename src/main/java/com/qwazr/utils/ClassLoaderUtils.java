/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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
 **/
package com.qwazr.utils;

import java.io.InputStream;

public class ClassLoaderUtils {

	public static <T> Class<T> findClass(final ClassLoader classLoader, final String className)
			throws ClassNotFoundException {
		if (classLoader != null)
			return (Class<T>) classLoader.loadClass(className);
		else
			return (Class<T>) Class.forName(className);
	}

	public static InputStream getResourceAsStream(final ClassLoader classLoader, final String name) {
		if (classLoader != null)
			return classLoader.getResourceAsStream(name);
		else
			return ClassLoaderUtils.class.getResourceAsStream(name);
	}

	public static <T> Class<T> findClass(final ClassLoader classLoader, final String classSuffix,
			final String... classPrefixes) throws ClassNotFoundException {
		if (classPrefixes == null || classPrefixes.length == 0)
			return findClass(classLoader, classSuffix);
		ClassNotFoundException firstClassException = null;
		for (String prefix : classPrefixes) {
			try {
				return (Class<T>) findClass(classLoader, prefix + classSuffix);
			} catch (ClassNotFoundException e) {
				if (firstClassException == null)
					firstClassException = e;
			}
		}
		throw firstClassException;
	}

	public interface ClassFactory {

		DefaultFactory DEFAULT = new DefaultFactory();

		<T> T newInstance(Class<T> clazz) throws ReflectiveOperationException;

	}

	final public static class DefaultFactory implements ClassFactory {

		@Override
		final public <T> T newInstance(final Class<T> clazz) throws ReflectiveOperationException {
			return clazz.newInstance();
		}
	}

}
