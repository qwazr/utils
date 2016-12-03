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
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.net.util.SubnetUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.net.*;
import java.util.*;
import java.util.function.Consumer;

public class ServerConfiguration implements ConfigurationProperties {

	private final static Logger LOGGER = LoggerFactory.getLogger(ServerConfiguration.class);

	private final Map<Object, Object> properties;

	public final File dataDirectory;

	public final Set<File> etcDirectories;
	public final FileFilter etcFileFilter;

	public final String publicAddress;
	public final String listenAddress;

	public final WebConnector webAppConnector;
	public final WebConnector webServiceConnector;
	public final WebConnector multicastConnector;

	public final Set<String> masters;
	public final Set<String> groups;

	public ServerConfiguration(final String... args) throws IOException {
		this(System.getenv(), System.getProperties(), argsToMap(args));
	}

	protected ServerConfiguration(final Map<?, ?>... propertiesMaps) throws IOException {

		// Merge the maps.
		properties = new HashMap<>();
		if (propertiesMaps != null) {
			for (Map<?, ?> props : propertiesMaps)
				if (props != null)
					props.forEach((key, value) -> properties.put(key.toString(), value));
		}

		//Set the data directory
		dataDirectory = getDataDirectory(getStringProperty(QWAZR_DATA));
		if (dataDirectory == null)
			throw new IOException("The data directory has not been set.");
		if (!dataDirectory.exists())
			throw new IOException("The data directory does not exists: " + dataDirectory.getAbsolutePath());
		if (!dataDirectory.isDirectory())
			throw new IOException("The data directory is not a directory: " + dataDirectory.getAbsolutePath());

		//Set the configuration directories
		etcDirectories = getEtcDirectories(getStringProperty(QWAZR_ETC_DIR));
		etcFileFilter = buildEtcFileFilter(getStringProperty(QWAZR_ETC));

		//Set the listen address
		listenAddress = findListenAddress(getStringProperty(LISTEN_ADDR));

		//Set the public address
		publicAddress = findPublicAddress(getStringProperty(PUBLIC_ADDR), this.listenAddress);

		//Set the connectors
		webAppConnector =
				new WebConnector(publicAddress, getIntegerProperty(WEBAPP_PORT), 9090, getStringProperty(WEBAPP_REALM));
		webServiceConnector = new WebConnector(publicAddress, getIntegerProperty(WEBSERVICE_PORT), 9091,
				getStringProperty(WEBSERVICE_REALM));
		multicastConnector =
				new WebConnector(getStringProperty(MULTICAST_ADDR), getIntegerProperty(MULTICAST_PORT), 9091, null);

		// Collect the master address.
		final LinkedHashSet<String> set = new LinkedHashSet<>();
		try {
			findMatchingAddress(getStringProperty(QWAZR_MASTERS), set);
		} catch (SocketException e) {
			LOGGER.warn("Failed in extracting IP information. No master server is configured.");
		}
		this.masters = set.isEmpty() ? null : Collections.unmodifiableSet(set);

		this.groups = buildSet(getStringProperty(QWAZR_GROUPS), ",; \t", true);
	}

	protected String getStringProperty(final String propName) {
		final Object o = properties.get(propName);
		return o == null ? null : o.toString();
	}

	protected Integer getIntegerProperty(final String propName) {
		final Object o = properties.get(propName);
		if (o == null)
			return null;
		if (o instanceof Number)
			return ((Number) o).intValue();
		return Integer.parseInt(o.toString());
	}

	protected static void fillStringListProperty(final String value, final String separatorChars, final boolean trim,
			final Consumer<String> consumer) {
		if (value == null)
			return;
		final String[] parts = StringUtils.split(value, separatorChars);
		for (String part : parts)
			if (part != null)
				consumer.accept(trim ? part.trim() : part);
	}

	protected static Set<String> buildSet(final String value, final String separatorChars, final boolean trim) {
		if (value == null || value.isEmpty())
			return null;
		final HashSet<String> set = new HashSet<>();
		fillStringListProperty(value, separatorChars, trim, set::add);
		return Collections.unmodifiableSet(set);
	}

	private static File getDataDirectory(final String dataDir) {
		//Set the data directory
		return StringUtils.isEmpty(dataDir) ? new File(System.getProperty("user.dir")) : new File(dataDir);
	}

	private static Set<File> getEtcDirectories(final String value) {
		final Set<File> set = new LinkedHashSet<>();
		fillStringListProperty(value == null ? "etc" : value, File.pathSeparator, true, part -> {
			// By design relative path are relative to the working directory
			final File etcFile = new File(part);
			set.add(etcFile);
			LOGGER.info("Configuration (ETC) directory: " + etcFile.getAbsolutePath());
		});
		return Collections.unmodifiableSet(set);
	}

	private static FileFilter buildEtcFileFilter(final String etcFilter) {
		if (StringUtils.isEmpty(etcFilter))
			return FileFileFilter.FILE;
		final String[] array = StringUtils.split(etcFilter, ',');
		if (array == null || array.length == 0)
			return FileFileFilter.FILE;
		return new AndFileFilter(FileFileFilter.FILE, new ConfigurationFileFilter(array));
	}

	public static class WebConnector {

		public final String address;
		public final String realm;
		public final int port;
		public final String addressPort;

		private WebConnector(final String address, final Integer port, final int defaulPort, final String realm) {
			this.address = address;
			this.realm = realm;
			this.port = port == null ? defaulPort : port;
			this.addressPort = this.address == null ? null : this.address + ":" + this.port;
		}

	}

	/**
	 * Manage that kind of pattern:
	 * 192.168.0.0/16,172.168.0.0/16
	 * 192.168.0.0/16
	 * 10.3.12.12
	 *
	 * @param addressPattern
	 * @return
	 * @throws SocketException
	 */
	private static void findMatchingAddress(final String addressPattern, final Collection<String> collect)
			throws SocketException {
		final String[] patterns = StringUtils.split(addressPattern, ",; ");
		if (patterns == null)
			return;
		for (String pattern : patterns) {
			if (pattern == null)
				continue;
			pattern = pattern.trim();
			if (!pattern.contains("/")) {
				collect.add(pattern);
				continue;
			}
			final SubnetUtils.SubnetInfo subnet = pattern.contains("/") ? new SubnetUtils(pattern).getInfo() : null;
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
					final String address = inetAddress.getHostAddress();
					if (subnet != null && subnet.isInRange(address) || address.equals(pattern))
						collect.add(address);
				}
			}
		}
	}

	private final static String DEFAULT_LISTEN_ADDRESS = "0.0.0.0";

	private static String findListenAddress(final String addressPattern) {
		if (StringUtils.isEmpty(addressPattern))
			return DEFAULT_LISTEN_ADDRESS;
		try {
			final ArrayList<String> list = new ArrayList<>();
			findMatchingAddress(addressPattern, list);
			return list.isEmpty() ? DEFAULT_LISTEN_ADDRESS : list.get(0);
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
			final ArrayList<String> list = new ArrayList<>();
			findMatchingAddress(addressPattern, list);
			return list.isEmpty() ? DEFAULT_PUBLIC_ADDRESS : list.get(0);
		} catch (SocketException e) {
			final String addr = StringUtils.isEmpty(listenAddress) || DEFAULT_LISTEN_ADDRESS.equals(listenAddress) ?
					DEFAULT_PUBLIC_ADDRESS :
					listenAddress;
			LOGGER.warn("Failed in extracting IP informations. Public address set to default (" + addr + ")", e);
			return addr;
		}
	}

	private static Map<String, String> argsToMapPrefix(final String prefix, final String... args) {
		final HashMap<String, String> props = new HashMap<>();
		if (args == null || args.length == 0)
			return props;
		final Integer prefixLength = prefix == null || prefix.isEmpty() ? null : prefix.length();
		for (String arg : args) {
			if (arg == null || arg.isEmpty())
				continue;
			if (prefixLength != null && !arg.startsWith(prefix))
				continue;
			final String[] split = StringUtils.split(arg, "=");
			final int l = split.length - 1;
			if (l < 1)
				continue;
			final String value = split[l];
			for (int i = 0; i < l; i++) {
				final String key = prefixLength == null || i > 0 ? split[i] : split[i].substring(prefixLength);
				props.put(key, value);
			}
		}
		return props;
	}

	private static Map<String, String> argsToMap(final String... args) {
		return argsToMapPrefix("--", args);
	}

	public static Builder of() {
		return new Builder();
	}

	public static class Builder {

		private final Map<String, Object> map;
		private final Set<String> masters;
		private final Set<String> groups;
		private final Set<String> etcFilters;
		private final Set<String> etcDirectories;

		private Builder() {
			this.map = new HashMap<>();
			this.masters = new LinkedHashSet<>();
			this.groups = new LinkedHashSet<>();
			this.etcFilters = new LinkedHashSet<>();
			this.etcDirectories = new LinkedHashSet<>();
		}

		public Builder data(final File file) {
			map.put(QWAZR_DATA, file.getPath());
			return this;
		}

		public Builder publicAddress(final String address) {
			map.put(PUBLIC_ADDR, address);
			return this;
		}

		public Builder listenAddress(final String address) {
			map.put(LISTEN_ADDR, address);
			return this;
		}

		public Builder master(final String master) {
			masters.add(master);
			return this;
		}

		public Builder group(final String group) {
			groups.add(group);
			return this;
		}

		public Builder etcFilter(final String etcFilter) {
			etcFilters.add(etcFilter);
			return this;
		}

		public Builder etcDirectory(final String etcDirectory) {
			etcDirectories.add(etcDirectory);
			return this;
		}

		public ServerConfiguration build() throws IOException {
			if (!masters.isEmpty())
				map.put(QWAZR_MASTERS, StringUtils.join(masters, ','));
			if (!groups.isEmpty())
				map.put(QWAZR_GROUPS, StringUtils.join(groups, ','));
			if (!etcFilters.isEmpty())
				map.put(QWAZR_ETC, StringUtils.join(etcFilters, ','));
			if (!etcDirectories.isEmpty())
				map.put(QWAZR_ETC_DIR, StringUtils.join(etcDirectories, File.pathSeparatorChar));
			return new ServerConfiguration(map);
		}
	}
}


