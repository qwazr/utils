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

package com.qwazr.utils.server;

import io.undertow.security.api.SecurityContext;
import io.undertow.security.idm.Account;
import io.undertow.server.ExchangeCompletionListener;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.HeaderMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.MDC;

import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.Calendar;

public class LogHandler implements HttpHandler {

	private final HttpHandler next;
	private final Logger logger;

	public LogHandler(final HttpHandler next, final Logger logger) {
		this.next = next;
		this.logger = logger;
	}

	@Override
	final public void handleRequest(final HttpServerExchange exchange) throws Exception {
		exchange.addExchangeCompleteListener(new CompletionListener());
		next.handleRequest(exchange);
	}

	private class CompletionListener implements ExchangeCompletionListener {

		private final long startTime = System.currentTimeMillis();

		@Override
		final public void exchangeEvent(final HttpServerExchange exchange, final NextListener nextListener) {
			try {
				final HeaderMap requestHeaders = exchange.getRequestHeaders();
				final Calendar calendar = Calendar.getInstance();

				final InetSocketAddress destinationAddress = exchange.getDestinationAddress();
				final InetSocketAddress sourceAddress = exchange.getSourceAddress();

				final long endTime = System.currentTimeMillis();

				MDC.put("c-ip", sourceAddress.getAddress().getHostAddress());
				MDC.put("cs-host", sourceAddress.getHostName());
				MDC.put("cs-method", exchange.getRequestMethod().toString());
				MDC.put("cs-uri-query", exchange.getQueryString());
				MDC.put("cs-uri-stem", exchange.getRequestPath());
				MDC.put("cs-user-agent", requestHeaders.getFirst("User-Agent"));
				MDC.put("cs-username", getUsername(exchange.getSecurityContext()));
				MDC.put("cs-x-forwarded-for", requestHeaders.getFirst("X-Forwarded-For"));
				MDC.put("date", getDate(calendar));
				MDC.put("cs-referer", requestHeaders.getFirst("Referer"));
				MDC.put("sc-status", Integer.toString(exchange.getStatusCode()));
				MDC.put("s-ip", destinationAddress.getAddress().getHostAddress());
				MDC.put("s-port", Integer.toString(destinationAddress.getPort()));
				MDC.put("time", getTime(calendar));
				MDC.put("time-taken", Long.toString(endTime - startTime));
				MDC.put("cs-bytes", Long.toString(exchange.getRequestContentLength()));
				MDC.put("sc-bytes", Long.toString(exchange.getResponseBytesSent()));

				logger.info(StringUtils.EMPTY);
				MDC.clear();
			} finally {
				nextListener.proceed();
			}
		}
	}

	private static String getUsername(final SecurityContext securityContext) {
		if (!securityContext.isAuthenticated())
			return null;
		final Account account = securityContext.getAuthenticatedAccount();
		if (account == null)
			return null;
		final Principal principal = account.getPrincipal();
		if (principal == null)
			return null;
		return principal.getName();
	}

	private static void span2(final StringBuilder sb, final int value) {
		if (value < 10)
			sb.append('0');
		sb.append(value);
	}

	private static void span3(final StringBuilder sb, final int value) {
		if (value < 10)
			sb.append("00");
		else if (value < 100)
			sb.append('0');
		sb.append(value);
	}

	private static String getDate(final Calendar calendar) {
		final StringBuilder sb = new StringBuilder();
		sb.append(calendar.get(Calendar.YEAR));
		sb.append('-');
		span2(sb, calendar.get(Calendar.MONTH) + 1);
		sb.append('-');
		span2(sb, calendar.get(Calendar.DAY_OF_MONTH));
		return sb.toString();
	}

	private static String getTime(final Calendar calendar) {
		final StringBuilder sb = new StringBuilder();
		span2(sb, calendar.get(Calendar.HOUR_OF_DAY));
		sb.append(':');
		span2(sb, calendar.get(Calendar.MINUTE));
		sb.append(':');
		span2(sb, calendar.get(Calendar.SECOND));
		sb.append('.');
		span3(sb, calendar.get(Calendar.MILLISECOND));
		return sb.toString();
	}
}
