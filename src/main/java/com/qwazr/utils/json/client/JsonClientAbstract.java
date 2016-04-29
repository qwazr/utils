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
package com.qwazr.utils.json.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.qwazr.utils.http.HttpResponseEntityException;
import com.qwazr.utils.json.JsonHttpResponseHandler;
import com.qwazr.utils.json.JsonMapper;
import org.apache.http.HttpResponse;
import org.apache.http.auth.Credentials;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

public abstract class JsonClientAbstract implements JsonClientInterface {

	private static final Logger logger = LoggerFactory.getLogger(JsonClientAbstract.class);

	private final static int DEFAULT_TIMEOUT;

	static {
		String s = System.getProperty("com.qwazr.utils.json.client.default_timeout");
		DEFAULT_TIMEOUT = s == null ? 60000 : Integer.parseInt(s);
	}

	public final String url;
	protected final String scheme;
	protected final String host;
	protected final String fragment;
	protected final String path;
	protected final int port;
	private final int timeout;

	private final Executor executor;

	protected JsonClientAbstract(String url, Integer msTimeOut, Credentials credentials) throws URISyntaxException {
		this.url = url;
		URI u = new URI(url);
		String path = u.getPath();
		if (path != null && path.endsWith("/"))
			u = new URI(u.getScheme(), null, u.getHost(), u.getPort(), path.substring(0, path.length() - 1),
					u.getQuery(), u.getFragment());
		this.scheme = u.getScheme() == null ? "http" : u.getScheme();
		this.host = u.getHost();
		this.fragment = u.getFragment();
		this.path = u.getPath();
		this.port = u.getPort() == -1 ? 80 : u.getPort();
		this.timeout = msTimeOut == null ? DEFAULT_TIMEOUT : msTimeOut;
		this.executor = credentials == null ? Executor.newInstance() : Executor.newInstance().auth(credentials);

	}

	protected JsonClientAbstract(String url, Integer msTimeOut) throws URISyntaxException {
		this(url, msTimeOut, null);
	}

	private Request setBodyString(Request request, Object bodyObject) throws JsonProcessingException {
		if (bodyObject == null)
			return request;
		if (bodyObject instanceof String)
			return request.bodyString(bodyObject.toString(), ContentType.TEXT_PLAIN);
		else if (bodyObject instanceof InputStream)
			return request.bodyStream((InputStream) bodyObject, ContentType.APPLICATION_OCTET_STREAM);
		else
			return request.bodyString(JsonMapper.MAPPER.writeValueAsString(bodyObject), ContentType.APPLICATION_JSON);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final public <T> T execute(Request request, Object bodyObject, Integer msTimeOut, Class<T> jsonResultClass,
			int... expectedCodes) throws IOException {
		if (logger.isDebugEnabled())
			logger.debug(request.toString());
		if (msTimeOut == null)
			msTimeOut = this.timeout;
		request = setBodyString(request, bodyObject);
		JsonHttpResponseHandler.JsonValueResponse<T> responseHandler = new JsonHttpResponseHandler.JsonValueResponse<T>(
				ContentType.APPLICATION_JSON, jsonResultClass, expectedCodes);
		return executor.execute(request.connectTimeout(msTimeOut).socketTimeout(msTimeOut)
				.addHeader("Accept", ContentType.APPLICATION_JSON.toString())).handleResponse(responseHandler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final public <T> T execute(Request request, Object bodyObject, Integer msTimeOut, TypeReference<T> typeRef,
			int... expectedCodes) throws IOException {
		if (logger.isDebugEnabled())
			logger.debug(request.toString());
		if (msTimeOut == null)
			msTimeOut = this.timeout;
		request = setBodyString(request, bodyObject);
		return executor.execute(request.connectTimeout(msTimeOut).socketTimeout(msTimeOut)
				.addHeader("accept", ContentType.APPLICATION_JSON.toString())).handleResponse(
				new JsonHttpResponseHandler.JsonValueTypeRefResponse<T>(ContentType.APPLICATION_JSON, typeRef,
						expectedCodes));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final public JsonNode execute(Request request, Object bodyObject, Integer msTimeOut, int... expectedCodes)
			throws IOException {
		if (logger.isDebugEnabled())
			logger.debug(request.toString());
		if (msTimeOut == null)
			msTimeOut = this.timeout;
		request = setBodyString(request, bodyObject);
		return executor.execute(request.connectTimeout(msTimeOut).socketTimeout(msTimeOut)
				.addHeader("accept", ContentType.APPLICATION_JSON.toString())).handleResponse(
				new JsonHttpResponseHandler.JsonTreeResponse(ContentType.APPLICATION_JSON, expectedCodes));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final public HttpResponse execute(Request request, Object bodyObject, Integer msTimeOut) throws IOException {
		if (logger.isDebugEnabled())
			logger.debug(request.toString());
		if (msTimeOut == null)
			msTimeOut = this.timeout;
		request = setBodyString(request, bodyObject);
		return executor.execute(request.connectTimeout(msTimeOut).socketTimeout(msTimeOut)).returnResponse();
	}

	final public <T> T commonServiceRequest(Request request, Object body, Integer msTimeOut, Class<T> objectClass,
			int... expectedCodes) {
		try {
			return execute(request, body, msTimeOut, objectClass, expectedCodes);
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	final public <T> T commonServiceRequest(Request request, Object body, Integer msTimeOut, TypeReference<T> typeRef,
			int... expectedCodes) {
		try {
			return execute(request, body, msTimeOut, typeRef, expectedCodes);
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	final public JsonNode commonServiceRequest(Request request, Object body, Integer msTimeOut, int... expectedCodes) {
		try {
			return execute(request, body, msTimeOut, expectedCodes);
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	public class UBuilder extends URIBuilder {

		/**
		 * Helper for URL building. The URL is built by concatening the url
		 * parameters given in the constructor and an array of pathes.
		 *
		 * @param paths An array of path
		 */
		public UBuilder(String... paths) {
			StringBuilder sb = new StringBuilder();
			if (path != null)
				sb.append(path);
			if (paths != null)
				for (String path : paths)
					if (path != null)
						sb.append(path);
			setScheme(scheme).setHost(host).setPort(port).setFragment(fragment);
			if (sb.length() > 0)
				setPath(sb.toString());
		}

		/**
		 * Add the query parameters if the object parameter is not null
		 *
		 * @param param  the name of the parameter
		 * @param object an optional value
		 * @return the current UBuilder
		 */
		public UBuilder setParameterObject(String param, Object object) {
			if (object == null)
				return this;
			super.setParameter(param, object.toString());
			return this;
		}

		/**
		 * Add the query parameter. If the value is null nothing is added.
		 *
		 * @param param the name of the parameter
		 * @param value an optional value
		 * @return the current UBuilder
		 */
		@Override
		public UBuilder setParameter(String param, String value) {
			if (value == null)
				return this;
			super.setParameter(param, value);
			return this;
		}

		public UBuilder setParameter(String param, Enum value) {
			if (value == null)
				return this;
			super.setParameter(param, value.name());
			return this;
		}

		public UBuilder setParameter(String param, Number value) {
			if (value == null)
				return this;
			super.setParameter(param, value.toString());
			return this;
		}

		/**
		 * Set common parameters for QWAZR services
		 *
		 * @param local     an optional local parameter (can be null)
		 * @param group     an optional group parameter (can be null)
		 * @param msTimeout an optional timeout parameter in milliseconds (can be null)
		 * @return the current UBuilder
		 */
		public UBuilder setParameters(Boolean local, String group, Integer msTimeout) {
			setParameterObject("local", local);
			setParameterObject("group", group);
			setParameterObject("timeout", msTimeout);
			return this;
		}

		public URI build() {
			try {
				return super.build();
			} catch (URISyntaxException e) {
				throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
			}
		}

	}
}
