/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ReflectiveUtils {

	public static <T> Collection<T> getCollection(final Object record, final Field field, final Class<?> fieldClass)
			throws ReflectiveOperationException {
		Collection<T> collection = (Collection<T>) field.get(record);
		if (collection != null)
			return collection;
		collection = (Collection<T>) fieldClass.newInstance();
		field.set(record, collection);
		return collection;
	}

	/**
	 * Fill a parameter map from the given parameters collection
	 *
	 * @param map        the map to populate
	 * @param parameters the parameters to check
	 * @return return the map
	 */
	public static Map<Class<?>, ?> fillParameterMap(final Map<Class<?>, Object> map, final Collection<?> parameters) {
		if (parameters == null)
			return map;
		Objects.requireNonNull(map, "The map parameter is null");
		parameters.forEach(parameter -> map.put(parameter.getClass(), parameter));
		return map;
	}

	/**
	 * Fill a parameter map from the given parameters
	 *
	 * @param map        the map to populate
	 * @param parameters the parameters to check
	 * @return return the map
	 */
	public static Map<Class<?>, ?> fillParameterMap(final Map<Class<?>, Object> map, final Object... parameters) {
		if (parameters == null)
			return map;
		Objects.requireNonNull(map, "The map parameter is null");
		for (final Object parameter : parameters)
			map.put(parameter.getClass(), parameter);
		return map;
	}

	/**
	 * Create  and fill new parameter map (HashMap) with the give parameters
	 *
	 * @param parameters the parameters to check
	 * @return a new HashMap
	 */
	public static Map<Class<?>, ?> newParameterMap(final Collection<?> parameters) {
		return fillParameterMap(new HashMap<>(), parameters);
	}

	/**
	 * Create  and fill new parameter map (HashMap) with the give parameters
	 *
	 * @param parameters the parameters to check
	 * @return a new HashMap
	 */
	public static Map<Class<?>, ?> newParameterMap(final Object... parameters) {
		return fillParameterMap(new HashMap<>(), parameters);
	}

	private final static Object[] NO_PARAMS = new Object[0];

	/**
	 * Build a list of parameters matching the constructor parameter.
	 *
	 * @param constructor the constructor to check
	 * @param parameters  the parameter map
	 * @param <T>         the type of the introspected class
	 * @return the ordered parameter list or null
	 */
	public static <T> Object[] findMatchingParameterSet(final Constructor<T> constructor,
			final Map<Class<?>, ?> parameters) {
		final List<Object> parameterList = new ArrayList<>();
		final Class<?>[] parameterClasses = constructor.getParameterTypes();
		if (parameterClasses == null || parameterClasses.length == 0)
			return NO_PARAMS;
		for (final Class<?> parameterClass : parameterClasses) {
			final Object parameter = parameters.get(parameterClass);
			if (parameter == null)
				return null;
			parameterList.add(parameter);
		}
		return parameterList.toArray(new Object[parameterList.size()]);
	}

	/**
	 * Find the first constructor who match the largest set of parameters present
	 *
	 * @param objectClass  the class to introspect
	 * @param parameterMap the available parameters (may be null or empty)
	 * @param <T>          the type of the class
	 * @return the best matching constructor
	 * @throws NoSuchMethodException
	 */
	public static <T> InstanceFactory<T> findBestMatchingConstructor(final Map<Class<?>, ?> parameterMap,
			final Class<T> objectClass) throws NoSuchMethodException {
		final Constructor<T>[] constructors = (Constructor<T>[]) objectClass.getDeclaredConstructors();
		if (constructors == null)
			return null;
		if (parameterMap == null || parameterMap.size() == 0) {
			final Constructor<T> constructor = objectClass.getDeclaredConstructor();
			return constructor == null ? null : new InstanceFactory(constructor, null);
		}
		int max = -1;
		Constructor<T> bestMatchConstructor = null;
		Object[] bestParameterArray = null;
		for (final Constructor<T> constructor : constructors) {
			final Object[] parameters = findMatchingParameterSet(constructor, parameterMap);
			if (parameters != null && parameters.length > max) {
				bestMatchConstructor = constructor;
				bestParameterArray = parameters;
				if (parameters.length == parameterMap.size())
					break;
				max = parameters.length;
			}
		}
		return new InstanceFactory(bestMatchConstructor, bestParameterArray);
	}

	public static class InstanceFactory<T> {

		final public Constructor<T> constructor;
		final public Object[] parameters;

		public InstanceFactory(final Constructor<T> constructor, final Object[] parameters) {
			this.constructor = constructor;
			this.parameters = parameters;
		}

		public T newInstance() throws IllegalAccessException, InvocationTargetException, InstantiationException {
			return constructor.newInstance(parameters);
		}

	}
}
