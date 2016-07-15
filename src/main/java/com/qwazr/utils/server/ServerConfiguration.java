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

	public enum VariablesPortPrefix {
		WEBAPP, WEBSERVICE, UDP
	}

	public enum VariablesPortSuffix {
		REALM, AUTHTYPE, PORT, ADDRESS
	}

	public enum VariablesEnum {
		QWAZR_DATA,
		QWAZR_ETC_DIR,
		LISTEN_ADDR,
		PUBLIC_ADDR
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

	final class HttpConnector {

		/**
		 * The port TCP port used by the listening socket
		 */
		final int port;

		final String realm;

		final String authType;


		private HttpConnector(VariablesPortPrefix prefix, int defaultPort) {
			port = getPropertyOrEnvInt(prefix, VariablesPortSuffix.PORT, defaultPort);
			realm = getPropertyOrEnv(prefix, VariablesPortSuffix.REALM);
			authType = getPropertyOrEnv(prefix, VariablesPortSuffix.AUTHTYPE);
		}
	}

	final class UdpConnector {

		final int port;

		final String address;

		private UdpConnector(int defaultPort) {
			address = getPropertyOrEnv(VariablesPortPrefix.UDP, VariablesPortSuffix.ADDRESS);
			port = getPropertyOrEnvInt(VariablesPortPrefix.UDP, VariablesPortSuffix.PORT, defaultPort);
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

	final HttpConnector webServiceConnector;

	final HttpConnector webAppConnector;

	final UdpConnector udpConnector;

	public ServerConfiguration() {

		dataDirectory = buildDataDir(getPropertyOrEnv(null, VariablesEnum.QWAZR_DATA));
		etcDirectories = buildEtcDirectories(dataDirectory, getPropertyOrEnv(null, VariablesEnum.QWAZR_ETC_DIR));

		webAppConnector = new HttpConnector(VariablesPortPrefix.WEBAPP, 9090);
		webServiceConnector = new HttpConnector(VariablesPortPrefix.WEBSERVICE, 9091);
		udpConnector = new UdpConnector(9091);

		String defaultAddress;
		try {
			defaultAddress = InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			logger.warn("Cannot extract the address of the localhost.", e);
			defaultAddress = "localhost";
		}
		listenAddress = getPropertyOrEnv(null, VariablesEnum.LISTEN_ADDR, defaultAddress);
		publicAddress = getPropertyOrEnv(null, VariablesEnum.PUBLIC_ADDR, defaultAddress);

		webApplicationPublicAddress = publicAddress + ':' + webAppConnector.port;
		webServicePublicAddress = publicAddress + ':' + webServiceConnector.port;
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
			etcDirectories.add(new File(dataDirectory, "etc"));
		return etcDirectories;
	}

	final protected Integer getPropertyOrEnvInt(Enum<?> prefix, Enum<?> key, Integer defaultValue) {
		String value = getPropertyOrEnv(prefix, key);
		return value == null ? defaultValue : Integer.parseInt(value.trim());
	}

	final protected String getPropertyOrEnv(Enum<?> prefix, Enum<?> key) {
		return getPropertyOrEnv(prefix, key, null);
	}

	final protected String getPropertyOrEnv(Enum<?> prefix, Enum<?> key, String defaultValue) {
		String value = getProperty(prefix, key, null);
		if (value != null)
			return value;
		return getEnv(prefix, key, defaultValue);
	}

	final public static String getKey(Enum<?> prefix, Enum<?> key) {
		return prefix == null ? key.name() : prefix.name() + '_' + key.name();
	}

	final protected String getEnv(Enum<?> prefix, Enum<?> key, String defaultValue) {
		return defaultValue(System.getenv(getKey(prefix, key)), defaultValue);
	}

	final protected String getProperty(Enum<?> prefix, Enum<?> key, String defaultValue) {
		return defaultValue(System.getProperty(getKey(prefix, key)), defaultValue);
	}

	final protected String defaultValue(String value, String defaultValue) {
		if (value == null)
			return defaultValue;
		value = value.trim();
		return StringUtils.isEmpty(value) ? defaultValue : value;
	}

}


