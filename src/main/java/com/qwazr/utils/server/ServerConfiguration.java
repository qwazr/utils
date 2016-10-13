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
import org.aeonbits.owner.ConfigCache;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.*;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.Map;

public class ServerConfiguration {

	final static Logger LOGGER = LoggerFactory.getLogger(ServerConfiguration.class);

	public final File dataDirectory;
	public final LinkedHashSet<File> etcDirectories;
	public final String publicAddress;
	public final String listenAddress;
	public final WebConnector webAppConnector;
	public final WebConnector webServiceConnector;
	public final WebConnector multicastConnector;

	public ServerConfiguration(final Map... properties) {
		final ConfigurationProperties configProperties =
				ConfigCache.getOrCreate(ConfigurationProperties.class, properties);

		//Set the data directory
		final String dataDir = configProperties.dataDirectory();
		dataDirectory = StringUtils.isEmpty(dataDir) ? new File(System.getProperty("user.dir")) : new File(dataDir);

		//Set the configuration directories
		final String paths = configProperties.configDirectories();
		etcDirectories = new LinkedHashSet<>();
		final String[] parts = StringUtils.split(paths, File.pathSeparatorChar);
		int i = 0;
		for (String part : parts) { // By design relative path are relative to the working directory
			final File etcFile = new File(part);
			etcDirectories.add(etcFile);
			LOGGER.info("Configuration (ETC) directory #" + (++i) + " : " + etcFile.getAbsolutePath());
		}

		//Set the listen address
		this.listenAddress = findListenAddress(configProperties.listenAddress());

		//Set the public address
		this.publicAddress = findPublicAddress(configProperties.publicAddress(), this.listenAddress);

		//Set the connectors
		webAppConnector =
				new WebConnector(publicAddress, configProperties.webAppPort(), configProperties.webAppRealm());
		webServiceConnector =
				new WebConnector(publicAddress, configProperties.webServicePort(), configProperties.webServiceRealm());
		multicastConnector =
				new WebConnector(configProperties.multicastAddress(), configProperties.multicastPort(), null);
	}

	public static class WebConnector {

		public final String address;
		public final String realm;
		public final int port;
		public final String addressPort;

		private WebConnector(final String address, final Integer port, final String realm) {
			this.address = address;
			this.realm = realm;
			this.port = port == null ? -1 : port;
			this.addressPort = address + ":" + port;
		}

	}

	private static String findMatchingAddress(final String addressPattern, final String defaultAddress)
			throws SocketException {
		final String[] patterns = StringUtils.split(addressPattern, ',');
		if (patterns == null)
			return defaultAddress;
		for (String pattern : patterns) {
			final SubnetUtils.SubnetInfo subnet =
					new SubnetUtils(pattern.contains("/") ? pattern : pattern + "/32").getInfo();
			final Enumeration<NetworkInterface> enumInterfaces = NetworkInterface.getNetworkInterfaces();
			while (enumInterfaces != null && enumInterfaces.hasMoreElements()) {
				final NetworkInterface ifc = enumInterfaces.nextElement();
				if (!ifc.isUp())
					continue;
				final Enumeration<InetAddress> enumAddresses = ifc.getInetAddresses();
				while (enumAddresses != null && enumAddresses.hasMoreElements()) {
					final InetAddress inetAddress = enumAddresses.nextElement();
					if (!(inetAddress instanceof Inet4Address))
						continue;
					final String addr = inetAddress.getHostAddress();
					if (subnet.isInRange(addr))
						return addr;
				}
			}
		}
		return defaultAddress;
	}

	private final static String DEFAULT_LISTEN_ADDRESS = "0.0.0.0";

	private static String findListenAddress(final String addressPattern) {
		if (StringUtils.isEmpty(addressPattern))
			return DEFAULT_LISTEN_ADDRESS;
		try {
			return findMatchingAddress(addressPattern, DEFAULT_LISTEN_ADDRESS);
		} catch (SocketException e) {
			LOGGER.warn("Failed in extracting IP informations. Listen address set to default (" + DEFAULT_LISTEN_ADDRESS
					+ ")", e);
			return DEFAULT_LISTEN_ADDRESS;
		}
	}

	private final static String DEFAULT_PUBLIC_ADDRESS = "localhost";

	private static String getLocalHostAddress() {
		try {
			return InetAddress.getLocalHost().getHostAddress();
		} catch (UnknownHostException e) {
			LOGGER.warn("Cannot extract the address of the localhost.", e);
			return DEFAULT_PUBLIC_ADDRESS;
		}
	}

	private static String findPublicAddress(final String addressPattern, final String listenAddress) {
		if (StringUtils.isEmpty(addressPattern))
			return StringUtils.isEmpty(listenAddress) || DEFAULT_LISTEN_ADDRESS.equals(listenAddress) ?
					getLocalHostAddress() :
					listenAddress;
		try {
			return findMatchingAddress(addressPattern, DEFAULT_PUBLIC_ADDRESS);
		} catch (SocketException e) {
			final String addr = StringUtils.isEmpty(listenAddress) || DEFAULT_LISTEN_ADDRESS.equals(listenAddress) ?
					DEFAULT_PUBLIC_ADDRESS :
					listenAddress;
			LOGGER.warn("Failed in extracting IP informations. Public address set to default (" + addr + ")", e);
			return addr;
		}
	}

}


