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
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

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

		public ManyNoEmptyConstructor(String string) {
		}

		public ManyNoEmptyConstructor(Integer integer) {
		}

		public ManyNoEmptyConstructor(String string, Integer integer) {
		}

		public ManyNoEmptyConstructor(String string, Integer integer, EmptyConstructor empty) {
		}
	}

	public static class ManyAndEmptyConstructor {

		public ManyAndEmptyConstructor() {
		}

		public ManyAndEmptyConstructor(String string) {
		}

		public ManyAndEmptyConstructor(String string, Integer integer) {
		}

		public ManyAndEmptyConstructor(String string, Integer integer, EmptyConstructor empty) {
		}
	}

}
