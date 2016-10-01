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

import java.io.IOException;

public class ClassLoaderUtils {

	public final static <T> Class<T> findClass(final ClassLoader classLoader, final String className)
			throws ClassNotFoundException {
		if (classLoader != null)
			return (Class<T>) classLoader.loadClass(className);
		else
			return (Class<T>) Class.forName(className);
	}

	public final static <T> Class<T> findClass(final ClassLoader classLoader, final String[] classPrefixes,
			final String suffix) throws ClassNotFoundException {
		ClassNotFoundException firstClassException = null;
		for (String prefix : classPrefixes) {
			try {
				return (Class<T>) findClass(classLoader, prefix + suffix);
			} catch (ClassNotFoundException e) {
				if (firstClassException == null)
					firstClassException = e;
			}
		}
		throw firstClassException;
	}

	public final static <T> Class<T> findClass(final ClassLoader classLoader, final String classDef,
			final String[] classPrefixes) throws ReflectiveOperationException, IOException {
		if (classDef == null)
			return null;
		if (classPrefixes == null)
			return findClass(classLoader, classDef);
		else
			return (Class<T>) findClass(classLoader, classPrefixes, classDef);
	}

	interface ClassFactory {

		<T> T newInstance(Class<T> clazz) throws ReflectiveOperationException;

	}

	static class DefaultFactory implements ClassFactory {

		final static DefaultFactory INSTANCE = new DefaultFactory();

		@Override
		final public <T> T newInstance(final Class<T> clazz) throws ReflectiveOperationException {
			return clazz.newInstance();
		}
	}

	private static volatile ClassFactory currentClassFactory = DefaultFactory.INSTANCE;

	public final static void register(final ClassFactory classFactory) {
		currentClassFactory = classFactory == null ? DefaultFactory.INSTANCE : classFactory;
	}

	public final static <T> T newInstance(final Class<T> clazz) throws ReflectiveOperationException {
		return currentClassFactory.newInstance(clazz);
	}

}
