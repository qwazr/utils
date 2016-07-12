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
import com.qwazr.utils.http.HttpUtils;
import com.qwazr.utils.json.JsonHttpResponseHandler;
import com.qwazr.utils.json.JsonMapper;
import com.qwazr.utils.server.RemoteService;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response.Status;
import java.io.IOException;
import java.io.InputStream;

public abstract class JsonClientAbstract implements JsonClientInterface {

	private static final Logger logger = LoggerFactory.getLogger(JsonClientAbstract.class);

	private final static int DEFAULT_TIMEOUT;

	static {
		String s = System.getProperty("com.qwazr.utils.json.client.default_timeout");
		DEFAULT_TIMEOUT = s == null ? 60000 : Integer.parseInt(s);
	}

	protected final RemoteService remote;

	private final Executor executor;

	final private AuthCache authCache;
	final private BasicCredentialsProvider credentialsProvider;
	final private BasicCookieStore cookieStore;

	protected JsonClientAbstract(final RemoteService remote) {
		this.remote = remote;

		final Credentials credentials = remote.getCredentials();
		this.executor = credentials == null ? Executor.newInstance(HttpUtils.HTTP_CLIENT) :
				Executor.newInstance(HttpUtils.HTTP_CLIENT).auth(credentials);

		authCache = new BasicAuthCache();
		credentialsProvider = new BasicCredentialsProvider();
		if (credentials != null)
			credentialsProvider.setCredentials(AuthScope.ANY, credentials);
		cookieStore = new BasicCookieStore();
	}

	private void setBodyString(final Request request, final Object bodyObject) throws JsonProcessingException {
		if (bodyObject == null)
			return;
		if (bodyObject instanceof String)
			request.bodyString(bodyObject.toString(), ContentType.TEXT_PLAIN);
		else if (bodyObject instanceof InputStream)
			request.bodyStream((InputStream) bodyObject, ContentType.APPLICATION_OCTET_STREAM);
		else
			request.bodyString(JsonMapper.MAPPER.writeValueAsString(bodyObject), ContentType.APPLICATION_JSON);
	}

	private void setTimeOut(final Request request, final Integer msTimeOut) {
		final int timeout = msTimeOut != null ? msTimeOut : remote.timeout != null ? remote.timeout : DEFAULT_TIMEOUT;
		request.connectTimeout(timeout).socketTimeout(timeout);
	}

	private void commonSet(final Request request, final Object bodyObject, final Integer msTimeOut)
			throws JsonProcessingException {
		if (logger.isDebugEnabled())
			logger.debug(request.toString());
		setBodyString(request, bodyObject);
		setTimeOut(request, msTimeOut);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final public <T> T execute(final Request request, final Object bodyObject, final Integer msTimeOut,
			final Class<T> jsonResultClass, final int... expectedCodes) throws IOException {
		commonSet(request, bodyObject, msTimeOut);
		JsonHttpResponseHandler.JsonValueResponse<T> responseHandler =
				new JsonHttpResponseHandler.JsonValueResponse<T>(ContentType.APPLICATION_JSON, jsonResultClass,
						expectedCodes);
		return executor.execute(request.addHeader("Accept", ContentType.APPLICATION_JSON.toString()))
				.handleResponse(responseHandler);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final public <T> T execute(final Request request, final Object bodyObject, final Integer msTimeOut,
			final TypeReference<T> typeRef, final int... expectedCodes) throws IOException {
		commonSet(request, bodyObject, msTimeOut);
		return executor.execute(request.addHeader("accept", ContentType.APPLICATION_JSON.toString())).handleResponse(
				new JsonHttpResponseHandler.JsonValueTypeRefResponse<T>(ContentType.APPLICATION_JSON, typeRef,
						expectedCodes));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final public JsonNode execute(final Request request, final Object bodyObject, final Integer msTimeOut,
			final int... expectedCodes) throws IOException {
		commonSet(request, bodyObject, msTimeOut);
		return executor.execute(request.addHeader("accept", ContentType.APPLICATION_JSON.toString())).handleResponse(
				new JsonHttpResponseHandler.JsonTreeResponse(ContentType.APPLICATION_JSON, expectedCodes));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final public HttpResponse execute(final Request request, final Object bodyObject, final Integer msTimeOut)
			throws IOException {
		commonSet(request, bodyObject, msTimeOut);
		return executor.execute(request).returnResponse();
	}

	final private HttpClientContext getContext(final Integer msTimeOut) {
		final HttpClientContext context = HttpClientContext.create();
		context.setAttribute(HttpClientContext.CREDS_PROVIDER, credentialsProvider);
		context.setAttribute(HttpClientContext.AUTH_CACHE, authCache);
		context.setAttribute(HttpClientContext.COOKIE_STORE, cookieStore);
		final RequestConfig.Builder requestConfig = RequestConfig.custom();
		final int timeout = msTimeOut != null ? msTimeOut : remote.timeout != null ? remote.timeout : DEFAULT_TIMEOUT;
		requestConfig.setSocketTimeout(timeout).setConnectTimeout(timeout);
		context.setAttribute(HttpClientContext.REQUEST_CONFIG, requestConfig.build());
		return context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	final public HttpResponse execute(final HttpUriRequest request, final Integer msTimeOut) throws IOException {
		if (logger.isDebugEnabled())
			logger.debug(request.toString());
		return HttpUtils.HTTP_CLIENT.execute(request, getContext(msTimeOut));
	}


	final public <T> T commonServiceRequest(final Request request, final Object body, final Integer msTimeOut,
			final Class<T> objectClass, final int... expectedCodes) {
		try {
			return execute(request, body, msTimeOut, objectClass, expectedCodes);
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	final public <T> T commonServiceRequest(final Request request, final Object body, final Integer msTimeOut,
			final TypeReference<T> typeRef, final int... expectedCodes) {
		try {
			return execute(request, body, msTimeOut, typeRef, expectedCodes);
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

	final public JsonNode commonServiceRequest(final Request request, final Object body, final Integer msTimeOut,
			final int... expectedCodes) {
		try {
			return execute(request, body, msTimeOut, expectedCodes);
		} catch (HttpResponseEntityException e) {
			throw e.getWebApplicationException();
		} catch (IOException e) {
			throw new WebApplicationException(e.getMessage(), e, Status.INTERNAL_SERVER_ERROR);
		}
	}

}
