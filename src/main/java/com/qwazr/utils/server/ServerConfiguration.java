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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
		String address = configProperties.publicAddress();
		if (StringUtils.isEmpty(address)) {
			try {
				address = InetAddress.getLocalHost().getHostAddress();
			} catch (UnknownHostException e) {
				LOGGER.warn("Cannot extract the address of the localhost.", e);
				address = "localhost";
			}
		}
		this.publicAddress = address;

		//Set the public address
		this.listenAddress = configProperties.listenAddress();

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

}


