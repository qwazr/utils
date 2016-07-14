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
 */
package com.qwazr.utils.http;

import com.qwazr.utils.CharsetUtils;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpResponseException;
import org.apache.http.util.EntityUtils;

import java.io.IOException;

public class HttpResponseEntityException extends HttpResponseException {

	private static final long serialVersionUID = 1958648159987063347L;

	public HttpResponseEntityException(final StatusLine statusLine, final HttpEntity entity, final String message) {
		super(getStatusCode(statusLine), getMessage(entity, message));
	}

	private static int getStatusCode(StatusLine statusLine) {
		if (statusLine == null)
			return 0;
		return statusLine.getStatusCode();
	}

	private static String getMessage(final HttpEntity entity, final String message) {
		try {
			if (entity != null)
				return EntityUtils.toString(entity, CharsetUtils.CharsetUTF8);
			return message;

		} catch (IOException e) {
			return message;
		}
	}

	public static HttpResponseEntityException findFirstCause(Throwable e) {
		if (e == null)
			return null;
		if (e instanceof HttpResponseEntityException)
			return (HttpResponseEntityException) e;
		return findFirstCause(e.getCause());
	}

}
