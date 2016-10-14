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

public interface Externalizer<T> {

	static <T> Externalizer<T> of(Class<? extends T> clazz) {
		return new ClassExternalizer<>(clazz);
	}

	void writeExternal(final T object, final ObjectOutput out) throws IOException;

	void readExternal(final T object, final ObjectInput in)
			throws IOException, ClassNotFoundException;

}
