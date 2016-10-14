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
package com.qwazr.utils;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class CollectionsUtils {

	final public static <T> boolean equals(final Collection<T> coll1, final Collection<T> coll2) {
		if (coll1 == null)
			return coll2 == null;
		else if (coll2 == null)
			return false;
		if (coll1.size() != coll2.size())
			return false;
		Iterator<T> i = coll2.iterator();
		for (T o : coll1)
			if (!Objects.equals(o, i.next()))
				return false;
		return true;
	}
}
