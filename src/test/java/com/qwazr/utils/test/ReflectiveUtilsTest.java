/**
 * Copyright 2017 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.test;

import com.qwazr.utils.ReflectiveUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ReflectiveUtilsTest {

	@Test
	public void parameterMapTest() {
		final Collection<?> parameters = Arrays.asList("Test", Integer.valueOf(1));
		final Map<Class<?>, Object> givenMap = new HashMap<>();
		final Map<Class<?>, ?> returnedMap = ReflectiveUtils.fillParameterMap(givenMap, parameters);
		Assert.assertNotNull(returnedMap);
		Assert.assertEquals(returnedMap, givenMap);
		Assert.assertEquals(parameters.size(), returnedMap.size());
		parameters.forEach(parameter -> Assert.assertEquals(parameter, returnedMap.get(parameter.getClass())));
	}

	@Test
	public void emptyParameterMapTest() {
		Assert.assertTrue(ReflectiveUtils.newParameterMap().isEmpty());
		Assert.assertTrue(ReflectiveUtils.newParameterMap(new ArrayList<>()).isEmpty());
	}

	public static class NoPublicConstructor {

		private NoPublicConstructor() {
		}
	}

	@Test
	public void checkNoPublicConstructor() throws ReflectiveOperationException {
		final Map<Class<?>, ?> parameterMap = ReflectiveUtils.newParameterMap();
		final ReflectiveUtils.InstanceFactory<NoPublicConstructor> result =
				ReflectiveUtils.findBestMatchingConstructor(parameterMap, NoPublicConstructor.class);
		Assert.assertNotNull(result);
		try {
			result.newInstance();
			Assert.fail("IllegalAccessException should be thrown");
		} catch (IllegalAccessException e) {
			Assert.assertNull(result.parameters);
		}
	}

	public static class EmptyConstructor {

		public EmptyConstructor() {
		}
	}

	@Test
	public void checkPublicConstructor() throws ReflectiveOperationException {
		final Map<Class<?>, ?> parameterMap = ReflectiveUtils.newParameterMap();
		final ReflectiveUtils.InstanceFactory<EmptyConstructor> result =
				ReflectiveUtils.findBestMatchingConstructor(parameterMap, EmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(EmptyConstructor.class, result.newInstance().getClass());
	}

	@Test
	public void checkPublicConstructorWithParameters() throws ReflectiveOperationException {
		final Map<Class<?>, ?> parameterMap = ReflectiveUtils.newParameterMap("Test", Integer.valueOf(1));
		final ReflectiveUtils.InstanceFactory<EmptyConstructor> result =
				ReflectiveUtils.findBestMatchingConstructor(parameterMap, EmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertEquals(EmptyConstructor.class, result.newInstance().getClass());
	}

	public static class ManyNoEmptyConstructor {

		public final String string;
		public final Integer integer;
		public final EmptyConstructor empty;

		public ManyNoEmptyConstructor(String string) {
			this.string = string;
			this.integer = null;
			this.empty = null;
		}

		public ManyNoEmptyConstructor(Integer integer) {
			this.string = null;
			this.integer = integer;
			this.empty = null;
		}

		public ManyNoEmptyConstructor(String string, Integer integer) {
			this.string = string;
			this.integer = integer;
			this.empty = null;
		}

		public ManyNoEmptyConstructor(String string, Integer integer, EmptyConstructor empty) {
			this.string = string;
			this.integer = integer;
			this.empty = empty;
		}

		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof ManyNoEmptyConstructor))
				return false;
			final ManyNoEmptyConstructor e = (ManyNoEmptyConstructor) o;
			return Objects.equals(string, e.string) && Objects.equals(integer, e.integer) && Objects.equals(empty,
					e.empty);
		}
	}

	@Test
	public void checkManyNoEmptyConstructor() throws ReflectiveOperationException {
		final ManyNoEmptyConstructor exFull =
				new ManyNoEmptyConstructor(RandomStringUtils.randomAlphanumeric(5), RandomUtils.nextInt());
		final Map<Class<?>, ?> parameterMap = ReflectiveUtils.newParameterMap(exFull.integer, exFull.string);
		final ReflectiveUtils.InstanceFactory<ManyNoEmptyConstructor> result =
				ReflectiveUtils.findBestMatchingConstructor(parameterMap, ManyNoEmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertFalse(exFull == result.newInstance());
		Assert.assertEquals(exFull, result.newInstance());
	}

	public static class ManyAndEmptyConstructor extends ManyNoEmptyConstructor {

		public ManyAndEmptyConstructor() {
			super((String) null);
		}

		public ManyAndEmptyConstructor(String string, EmptyConstructor empty) {
			super(string, null, empty);
		}

		public ManyAndEmptyConstructor(String string, Integer integer, EmptyConstructor empty) {
			super(string, integer, empty);
		}
	}

	@Test
	public void checkManyAndEmptyConstructorThreeParameters() throws ReflectiveOperationException {
		final ManyAndEmptyConstructor exFull =
				new ManyAndEmptyConstructor(RandomStringUtils.randomAlphanumeric(5), RandomUtils.nextInt(),
						new EmptyConstructor());
		final Map<Class<?>, ?> parameterMap =
				ReflectiveUtils.newParameterMap(exFull.integer, exFull.empty, exFull.string);
		final ReflectiveUtils.InstanceFactory<ManyNoEmptyConstructor> result =
				ReflectiveUtils.findBestMatchingConstructor(parameterMap, ManyNoEmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertFalse(exFull == result.newInstance());
		Assert.assertEquals(exFull, result.newInstance());
	}

	@Test
	public void checkManyAndEmptyConstructorTwoParameters() throws ReflectiveOperationException {
		final ManyAndEmptyConstructor exFull =
				new ManyAndEmptyConstructor(RandomStringUtils.randomAlphanumeric(5), new EmptyConstructor());
		final Map<Class<?>, ?> parameterMap = ReflectiveUtils.newParameterMap(exFull.empty, exFull.string);
		final ReflectiveUtils.InstanceFactory<ManyAndEmptyConstructor> result =
				ReflectiveUtils.findBestMatchingConstructor(parameterMap, ManyAndEmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertFalse(exFull == result.newInstance());
		Assert.assertEquals(exFull, result.newInstance());
	}

	@Test
	public void checkManyAndEmptyConstructorNoParameters() throws ReflectiveOperationException {
		final Map<Class<?>, ?> parameterMap = ReflectiveUtils.newParameterMap();
		final ReflectiveUtils.InstanceFactory<ManyAndEmptyConstructor> result =
				ReflectiveUtils.findBestMatchingConstructor(parameterMap, ManyAndEmptyConstructor.class);
		Assert.assertNotNull(result);
		Assert.assertNotNull(result.newInstance());
	}

	@Test
	public void testParameterMap() {

		ReflectiveUtils.ParameterHashMap map = new ReflectiveUtils.ParameterHashMap();

		map.registerConstructorParameter(Integer.valueOf(1));
		map.registerConstructorParameter(Long.class, Long.valueOf(2));

		Assert.assertEquals(1, map.get(Integer.class));
		Assert.assertEquals(2L, map.get(Long.class));

		map.unregisterConstructorParameter(Integer.valueOf(1));
		Assert.assertNull(map.get(Integer.class));

		map.unregisterConstructorParameter(Long.class);
		Assert.assertNull(map.get(Long.class));

	}

}
