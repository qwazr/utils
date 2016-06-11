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
package com.qwazr.utils.server;

import com.qwazr.utils.StringUtils;
import com.qwazr.utils.json.JsonExceptionReponse;
import org.slf4j.Logger;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

public class ServerException extends RuntimeException {

	/**
	 *
	 */
	private static final long serialVersionUID = -6102827990391082335L;

	private final int statusCode;
	private final String message;

	public ServerException(final Status status, String message, final Exception exception) {
		super(message, exception);
		if (status == null && exception != null && exception instanceof WebApplicationException) {
			WebApplicationException wae = (WebApplicationException) exception;
			this.statusCode = wae.getResponse().getStatus();
		} else
			this.statusCode = status != null ? status.getStatusCode() : Status.INTERNAL_SERVER_ERROR.getStatusCode();
		if (StringUtils.isEmpty(message)) {
			if (exception != null)
				message = exception.getMessage();
			if (StringUtils.isEmpty(message) && status != null)
				message = status.getReasonPhrase();
		}
		this.message = message;
	}

	public ServerException(final Status status) {
		this(status, status.getReasonPhrase(), null);
	}

	public ServerException(final Status status, final String message) {
		this(status, message, null);
	}

	public ServerException(final String message, final Exception exception) {
		this(null, message, exception);
	}

	public ServerException(final String message) {
		this(null, message, null);
	}

	public ServerException(final Exception exception) {
		this(null, null, exception);
	}

	public int getStatusCode() {
		return statusCode;
	}

	final public ServerException warnIfCause(final Logger logger) {
		final Throwable cause = getCause();
		if (cause == null)
			return this;
		if (logger.isWarnEnabled())
			logger.warn(getMessage(), cause);
		return this;
	}

	final public ServerException errorIfCause(final Logger logger) {
		final Throwable cause = getCause();
		if (cause == null)
			return this;
		if (logger.isErrorEnabled())
			logger.error(getMessage(), cause);
		return this;
	}

	@Override
	final public String getMessage() {
		if (message != null)
			return message;
		return super.getMessage();
	}

	private Response getTextResponse() {
		return Response.status(statusCode).type(MediaType.TEXT_PLAIN)
				.entity(message == null ? StringUtils.EMPTY : message).build();
	}

	private Response getJsonResponse() {
		return new JsonExceptionReponse(statusCode, message, this).toResponse();
	}

	public WebApplicationException getTextException() {
		return new WebApplicationException(this, getTextResponse());
	}

	public WebApplicationException getJsonException() {
		return new WebApplicationException(this, getJsonResponse());
	}

	public static ServerException getServerException(final Exception e) {
		if (e instanceof ServerException)
			return (ServerException) e;
		if (e instanceof WebApplicationException) {
			Throwable cause = e.getCause();
			if (cause != null && cause instanceof ServerException)
				return (ServerException) e;
		}
		return new ServerException(e);
	}

	private static WebApplicationException checkCompatible(final Exception e, final MediaType expectedType) {
		if (!(e instanceof WebApplicationException))
			return null;
		WebApplicationException wae = (WebApplicationException) e;
		Response response = wae.getResponse();
		if (response == null)
			return null;
		if (!response.hasEntity())
			return null;
		MediaType mediaType = response.getMediaType();
		if (mediaType == null)
			return null;
		if (!expectedType.isCompatible(mediaType))
			return null;
		return wae;
	}

	public static WebApplicationException getTextException(final Logger logger, final Exception e) {
		WebApplicationException wae = checkCompatible(e, MediaType.TEXT_PLAIN_TYPE);
		if (wae != null)
			return wae;
		return getServerException(e).warnIfCause(logger).getTextException();
	}

	public static WebApplicationException getJsonException(final Logger logger, final Exception e) {
		WebApplicationException wae = checkCompatible(e, MediaType.APPLICATION_JSON_TYPE);
		if (wae != null)
			return wae;
		return getServerException(e).warnIfCause(logger).getJsonException();
	}

}
