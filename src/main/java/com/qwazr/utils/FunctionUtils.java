/**
 * Copyright 2015-2017 Emmanuel Keller / QWAZR
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.qwazr.utils;

import java.util.Map;

public class FunctionUtils {

	@FunctionalInterface
	public interface FunctionEx<T, R, E extends Exception> {

		R apply(T t) throws E;

	}

	@FunctionalInterface
	public interface FunctionEx2<T, R, E1 extends Exception, E2 extends Exception> {

		R apply(T t) throws E1, E2;

	}

	@FunctionalInterface
	public interface IntConsumerEx<E extends Exception> {

		void accept(int value) throws E;

	}

	@FunctionalInterface
	public interface ConsumerEx<T, E extends Exception> {

		void accept(T t) throws E;

	}

	@FunctionalInterface
	public interface BiConsumerEx<K, V, E extends Exception> {

		void accept(K k, V v) throws E;

	}

	@FunctionalInterface
	public interface BiConsumerEx2<K, V, E1 extends Exception, E2 extends Exception> {

		void accept(K k, V v) throws E1, E2;

	}

	@FunctionalInterface
	public interface CallableEx<V, E extends Exception> {

		V call() throws E;
	}

	@FunctionalInterface
	public interface RunnableEx<E extends Exception> {

		void run() throws E;
	}

	public static <K, V, E extends Exception> void forEachEx(Map<K, V> map, BiConsumerEx<K, V, E> consumer) throws E {
		for (Map.Entry<K, V> entry : map.entrySet())
			consumer.accept(entry.getKey(), entry.getValue());
	}

	public static <K, V, E1 extends Exception, E2 extends Exception> void forEachEx2(Map<K, V> map,
			BiConsumerEx2<K, V, E1, E2> consumer) throws E1, E2 {
		for (Map.Entry<K, V> entry : map.entrySet())
			consumer.accept(entry.getKey(), entry.getValue());
	}

}
