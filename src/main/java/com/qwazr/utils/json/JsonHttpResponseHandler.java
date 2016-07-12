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
package com.qwazr.utils.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.qwazr.utils.http.HttpResponseHandler;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.entity.ContentType;

import java.io.IOException;

public class JsonHttpResponseHandler {

	public static class JsonTreeResponse extends HttpResponseHandler<JsonNode> {

		public JsonTreeResponse(final ContentType expectedContentType, final int... expectedCodes) {
			super(expectedContentType, expectedCodes);
		}

		@Override
		final public JsonNode handleResponse(final HttpResponse response) throws IOException {
			super.handleResponse(response);
			return JsonMapper.MAPPER.readTree(httpEntity.getContent());
		}
	}

	public static class JsonValueResponse<T> extends HttpResponseHandler<T> {

		private final Class<T> jsonClass;

		public JsonValueResponse(final ContentType expectedContentType, final Class<T> jsonClass,
				final int... expectedCodes) {
			super(expectedContentType, expectedCodes);
			this.jsonClass = jsonClass;
		}

		@Override
		final public T handleResponse(final HttpResponse response) throws IOException {
			super.handleResponse(response);
			return JsonMapper.MAPPER.readValue(httpEntity.getContent(), jsonClass);
		}
	}

	public static class JsonValueTypeRefResponse<T> extends HttpResponseHandler<T> {

		private final TypeReference<T> typeReference;

		public JsonValueTypeRefResponse(final ContentType expectedContentType, final TypeReference<T> typeReference,
				final int... expectedCodes) {
			super(expectedContentType, expectedCodes);
			this.typeReference = typeReference;
		}

		@Override
		final public T handleResponse(final HttpResponse response) throws IOException {
			super.handleResponse(response);
			return JsonMapper.MAPPER.readValue(httpEntity.getContent(), typeReference);
		}
	}
}
