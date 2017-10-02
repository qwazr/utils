/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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

import org.junit.Assert;
import org.junit.Test;

import java.util.Objects;

public class EqualizerTest {

	@Test
	public void testEquals() {
		Assert.assertEquals(new Same(2), new Same(2));
	}

	@Test
	public void testNonEquals() {
		Assert.assertNotEquals(new Same(2), new Same(3));
	}

	public static class Same extends Equalizer<Same> {

		private final Integer value;

		Same(Integer value) {
			super(Same.class);
			this.value = value;
		}

		@Override
		protected boolean isEqual(Same query) {
			return Objects.equals(value, query.value);
		}
	}
}
