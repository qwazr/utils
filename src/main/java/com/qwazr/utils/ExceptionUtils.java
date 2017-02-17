/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import org.slf4j.Logger;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ExceptionUtils extends org.apache.commons.lang3.exception.ExceptionUtils {

	public static String getLocation(StackTraceElement[] stackTrace, String prefix) {
		for (StackTraceElement element : stackTrace)
			if (element.getClassName().startsWith(prefix))
				return element.toString();
		return null;
	}

	public static String getFirstLocation(StackTraceElement[] stackTrace) {
		for (StackTraceElement element : stackTrace) {
			String ele = element.toString();
			if (ele != null && ele.length() > 0)
				return ele;
		}
		return null;
	}

	public static String getFullStackTrace(StackTraceElement[] stackTrace) {
		StringWriter sw = null;
		PrintWriter pw = null;
		try {
			sw = new StringWriter();
			pw = new PrintWriter(sw);
			for (StackTraceElement element : stackTrace)
				pw.println(element);
			return sw.toString();
		} finally {
			IOUtils.close(pw, sw);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T extends Exception> T throwException(final Exception exception, final Class<T> exceptionClass)
			throws T {
		if (exception == null)
			return null;
		if (exceptionClass.isInstance(exception))
			throw (T) exception;
		try {
			return exceptionClass.getConstructor(Exception.class).newInstance(exception);
		} catch (ReflectiveOperationException e) {
			throw new RuntimeException(e);
		}
	}

	public static Exception getCauseIfException(Exception e) {
		if (e == null)
			return null;
		final Throwable cause = e.getCause();
		return cause instanceof Exception ? (Exception) cause : e;
	}

	public static List<String> getStackTraces(final Throwable throwable) {
		final StackTraceElement[] stElements = throwable.getStackTrace();
		if (stElements == null)
			return null;
		final List<String> stList = new ArrayList<>(stElements.length);
		for (StackTraceElement stElement : stElements)
			stList.add(stElement.toString());
		return stList;
	}

	public static class Holder {

		private final Logger logger;
		private volatile Exception holdException;

		public Holder(final Logger logger) {
			this.logger = logger;
			this.holdException = null;
		}

		/**
		 * Hold the new exception. If there was a previous exception, a log warn
		 * is emited
		 *
		 * @param newException the new exception to old
		 */
		public void switchAndWarn(final Exception newException) {
			if (holdException != null && logger != null && logger.isWarnEnabled())
				logger.warn(holdException.getMessage(), holdException);
			holdException = newException;
		}

		/**
		 * @param <E> the generic type of the hold exception
		 * @return the hold exception if any
		 */
		public <E extends Exception> E getException() {
			return (E) holdException;
		}

		/**
		 * @param <E> the generic type of the hold exception
		 * @throws E the exception if any
		 */
		public <E extends Exception> void thrownIfAny() throws E {
			if (holdException != null)
				throw (E) holdException;
		}

	}
}
