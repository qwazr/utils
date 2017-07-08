/*
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
package com.qwazr.utils.http;

import com.qwazr.utils.concurrent.PeriodicThread;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.LayeredConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLInitializationException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContextBuilder;

import javax.net.ssl.SSLContext;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HttpClients {

	public static final CloseableHttpClient HTTP_CLIENT;
	public static final PoolingHttpClientConnectionManager CNX_MANAGER;

	static {

		LayeredConnectionSocketFactory ssl = null;
		try {
			ssl = SSLConnectionSocketFactory.getSystemSocketFactory();
		} catch (final SSLInitializationException ex) {
			final SSLContext sslcontext;
			try {
				sslcontext = SSLContext.getInstance(SSLConnectionSocketFactory.TLS);
				sslcontext.init(null, null, null);
				ssl = new SSLConnectionSocketFactory(sslcontext);
			} catch (final SecurityException | KeyManagementException | NoSuchAlgorithmException ignore) {
			}
		}

		final Registry<ConnectionSocketFactory> sfr = RegistryBuilder.<ConnectionSocketFactory>create().register("http",
				PlainConnectionSocketFactory.getSocketFactory()).register("https",
				ssl != null ? ssl : SSLConnectionSocketFactory.getSocketFactory()).build();

		CNX_MANAGER = new PoolingHttpClientConnectionManager(sfr);
		CNX_MANAGER.setDefaultMaxPerRoute(100);
		CNX_MANAGER.setMaxTotal(200);
		CNX_MANAGER.setValidateAfterInactivity(1000);

		HTTP_CLIENT = HttpClientBuilder.create().setConnectionManager(CNX_MANAGER).build();
	}

	public static final CloseableHttpClient UNSECURE_HTTP_CLIENT;
	public static final PoolingHttpClientConnectionManager UNSECURE_CNX_MANAGER;

	/**
	 * Create a new HttpClient which accept untrusted SSL certificates
	 *
	 * @return a new HttpClient
	 * @throws KeyStoreException
	 * @throws NoSuchAlgorithmException
	 * @throws KeyManagementException
	 */
	static {

		try {
			SSLContext sslContext = SSLContextBuilder.create().loadTrustMaterial(null, (arg0, arg1) -> true).build();

			SSLConnectionSocketFactory sslSocketFactory = new SSLConnectionSocketFactory(sslContext,
					NoopHostnameVerifier.INSTANCE);

			Registry<ConnectionSocketFactory> sfr = RegistryBuilder.<ConnectionSocketFactory>create().register("http",
					PlainConnectionSocketFactory.getSocketFactory()).register("https", sslSocketFactory).build();

			UNSECURE_CNX_MANAGER = new PoolingHttpClientConnectionManager(sfr);
			UNSECURE_CNX_MANAGER.setDefaultMaxPerRoute(100);
			UNSECURE_CNX_MANAGER.setMaxTotal(200);
			UNSECURE_CNX_MANAGER.setValidateAfterInactivity(1000);

			UNSECURE_HTTP_CLIENT = HttpClientBuilder.create().setSSLContext(sslContext).setConnectionManager(
					UNSECURE_CNX_MANAGER).build();

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static IdleConnectionMonitorThread getNewMonitorThread(final int msPeriod, final int msIdleTime) {
		final IdleConnectionMonitorThread monitorThread = new IdleConnectionMonitorThread(msPeriod);
		monitorThread.add(UNSECURE_CNX_MANAGER, msIdleTime);
		monitorThread.add(CNX_MANAGER, msIdleTime);
		return monitorThread;
	}

	public static class IdleConnectionMonitorThread extends PeriodicThread {

		private final ConcurrentHashMap<HttpClientConnectionManager, Integer> connectionManagers;

		public IdleConnectionMonitorThread(int msPeriod) {
			super(msPeriod);
			this.connectionManagers = new ConcurrentHashMap<>();
		}

		public void add(final HttpClientConnectionManager connectionManager, final int idleTime) {
			connectionManagers.put(connectionManager, idleTime);
		}

		@Override
		public void runner() {
			connectionManagers.forEach((connectionManager, idleTime) -> {
				connectionManager.closeExpiredConnections();
				connectionManager.closeIdleConnections(idleTime, TimeUnit.SECONDS);
			});
		}

	}
}
