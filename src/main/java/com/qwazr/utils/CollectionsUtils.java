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

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class CollectionsUtils {

	public static <T> boolean equals(final Collection<T> coll1, final Collection<T> coll2) {
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

	public static <K, V> boolean equals(final Map<K, V> map1, final Map<K, V> map2) {
		if (map1 == null)
			return map2 == null;
		else if (map2 == null)
			return false;
		if (map1.size() != map2.size())
			return false;
		for (Map.Entry<K, V> entry : map1.entrySet()) {
			V value = map2.get(entry.getKey());
			if (value == null)
				return false;
			if (!Objects.equals(value, entry.getValue()))
				return false;
		}
		return true;
	}

	public static <T> String multiline(final Collection<T> collection, final Function<T, String> toStringFunc) {
		if (collection == null)
			return null;
		try (final StringWriter sw = new StringWriter()) {
			try (final PrintWriter pw = new PrintWriter(sw)) {
				boolean first = true;
				for (T object : collection) {
					if (first)
						first = false;
					else
						pw.println();
					if (object != null)
						pw.print(toStringFunc.apply(object));
				}
			}
			return sw.toString();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public static <T> String multiline(final Collection<T> collection) {
		return multiline(collection, Object::toString);
	}
}
