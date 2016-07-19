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
 **/
package com.qwazr.utils.server;

import com.qwazr.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;

public class ServerConfiguration {

	static final Logger logger = LoggerFactory.getLogger(ServerConfiguration.class);

	public enum VariablesEnum {
		QWAZR_DATA,
		QWAZR_ETC_DIR,
		LISTEN_ADDR,
		PUBLIC_ADDR,
		WEBAPP_REALM,
		WEBAPP_PORT,
		WEBSERVICE_REALM,
		WEBSERVICE_PORT,
		MULTICAST_ADDR,
		MULTICAST_PORT
	}

	/**
	 * The hostname or address uses for the listening socket
	 */
	final String listenAddress;

	/**
	 * The public hostname or address and port for external access (node
	 * communication)
	 */
	final String publicAddress;

	/**
	 * the hostname and port on which the web application can be contacted
	 */
	public final String webApplicationPublicAddress;

	/**
	 * the hostname and port on which the web service can be contacted
	 */
	public final String webServicePublicAddress;

	public final String multicastAddress;

	public final Integer multicastPort;

	final class WebConnector {

		/**
		 * The port used by the listening socket
		 */
		final int port;

		final String realm;

		final String authType;

		private WebConnector(final Enum<?> portKey, final Enum<?> realmKey, final Enum<?> authTypeKey,
				final int defaultPort) {
			port = portKey != null ? getPropertyOrEnvInt(portKey, defaultPort) : defaultPort;
			realm = realmKey != null ? getPropertyOrEnv(realmKey) : null;
			authType = authTypeKey != null ? getPropertyOrEnv(authTypeKey) : null;
		}
	}

	/**
	 * The data directory
	 */
	public final File dataDirectory;

	/**
	 * The configuration directory
	 */
	public final Collection<File> etcDirectories;

	final WebConnector webServiceConnector;

	final WebConnector webAppConnector;

	public ServerConfiguration() {

		dataDirectory = buildDataDir(getPropertyOrEnv(VariablesEnum.QWAZR_DATA));
		etcDirectories = buildEtcDirectories(dataDirectory, getPropertyOrEnv(VariablesEnum.QWAZR_ETC_DIR));

		webAppConnector = new WebConnector(VariablesEnum.WEBAPP_PORT, VariablesEnum.WEBAPP_REALM, null, 9090);
		webServiceConnector =
				new WebConnector(VariablesEnum.WEBSERVICE_PORT, VariablesEnum.WEBSERVICE_REALM, null, 9091);

		String defaultAddress;
		try {
			defaultAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			logger.warn("Cannot extract the address of the localhost.", e);
			defaultAddress = "localhost";
		}
		listenAddress = getPropertyOrEnv(VariablesEnum.LISTEN_ADDR, defaultAddress);
		publicAddress = getPropertyOrEnv(VariablesEnum.PUBLIC_ADDR, defaultAddress);

		webApplicationPublicAddress = publicAddress + ':' + webAppConnector.port;
		webServicePublicAddress = publicAddress + ':' + webServiceConnector.port;
		multicastAddress = getPropertyOrEnv(VariablesEnum.MULTICAST_ADDR);
		multicastPort = getPropertyOrEnvInt(VariablesEnum.MULTICAST_PORT, null);
	}

	private static File buildDataDir(String path) {
		if (!StringUtils.isEmpty(path))
			return new File(path);
		return new File(System.getProperty("user.dir"));
	}

	private static ArrayList<File> buildEtcDirectories(final File dataDirectory, final String paths) {
		final ArrayList<File> etcDirectories = new ArrayList<>();
		if (paths != null && !paths.isEmpty()) {
			final String[] parts = StringUtils.split(paths, File.pathSeparatorChar);
			for (String path : parts)
				etcDirectories.add(new File(path));
		}
		if (etcDirectories.isEmpty())
			etcDirectories.add(new File(dataDirectory, "conf"));
		return etcDirectories;
	}

	final protected Integer getPropertyOrEnvInt(final Enum<?> key, final Integer defaultValue) {
		final String value = getPropertyOrEnv(key);
		if (value == null)
			return defaultValue;
		return Integer.parseInt(value.trim());
	}

	final protected String getPropertyOrEnv(final Enum<?> key) {
		return getPropertyOrEnv(key, null);
	}

	final protected String getPropertyOrEnv(final Enum<?> key, final String defaultValue) {
		final String value = getProperty(key, null);
		if (value != null)
			return value;
		return getEnv(key, defaultValue);
	}

	final protected String getEnv(final Enum<?> key, final String defaultValue) {
		return defaultValue(System.getenv(key.name()), defaultValue);
	}

	final protected String getProperty(final Enum<?> key, final String defaultValue) {
		return defaultValue(System.getProperty(key.name()), defaultValue);
	}

	final protected String defaultValue(String value, final String defaultValue) {
		if (value == null)
			return defaultValue;
		value = value.trim();
		return StringUtils.isEmpty(value) ? defaultValue : value;
	}

}


