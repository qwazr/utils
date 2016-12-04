/**
 * Copyright 2016 Emmanuel Keller / QWAZR
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
package com.qwazr.utils.test;

import com.qwazr.utils.server.ServerConfiguration;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Properties;

public class ServerConfigurationTest {

	private void checkConnector(ServerConfiguration.WebConnector connector, String address, Integer port) {
		Assert.assertEquals(address, connector.address);
		Assert.assertEquals(port == null ? null : address + ":" + port, connector.addressPort);
		if (port != null)
			Assert.assertEquals((int) port, connector.port);
	}

	private void checkConnectors(ServerConfiguration config, Integer webAppPort, Integer webServicePort,
			Integer multicastPort) {
		checkConnector(config.webAppConnector, webAppPort == null ? null : config.publicAddress, webAppPort);
		checkConnector(config.webServiceConnector, webServicePort == null ? null : config.publicAddress,
				webServicePort);
		checkConnector(config.multicastConnector, multicastPort == null ? null : config.publicAddress, multicastPort);
	}

	private static final File dataDir = new File("src/test/resources").getAbsoluteFile();
	private static final File tempDir = new File(dataDir, "temp").getAbsoluteFile();
	private static final File etcDir = new File(dataDir, "etc").getAbsoluteFile();
	private static final File confDir = new File(dataDir, "conf").getAbsoluteFile();

	@Before
	public void before() {
		System.setProperty("QWAZR_DATA", dataDir.getPath());
	}

	@Test
	public void empty() throws IOException {
		ServerConfiguration config = new ServerConfiguration();
		Assert.assertEquals(dataDir, config.dataDirectory);
		Assert.assertEquals(1, config.etcDirectories.size());
		Assert.assertEquals("0.0.0.0", config.listenAddress);
		Assert.assertNotNull(config.publicAddress);
		checkConnectors(config, 9090, 9091, null);
	}

	@Test
	public void ipArgs() throws IOException {
		ServerConfiguration config =
				new ServerConfiguration("--LISTEN_ADDR=PUBLIC_ADDR=MULTICAST_ADDR=192.168.0.1", "--WEBAPP_PORT=9190",
						"--WEBSERVICE_PORT=9191", "--MULTICAST_PORT=9192");
		Assert.assertEquals("192.168.0.1", config.listenAddress);
		Assert.assertEquals("192.168.0.1", config.publicAddress);
		checkConnectors(config, 9190, 9191, 9192);
	}

	@Test
	public void ipRange() throws IOException {
		ServerConfiguration config = new ServerConfiguration("--LISTEN_ADDR=PUBLIC_ADDR=127.0.0.0/24");
		Assert.assertEquals("127.0.0.1", config.listenAddress);
		Assert.assertEquals("127.0.0.1", config.publicAddress);
		checkConnectors(config, 9090, 9091, null);
	}

	@Test
	public void etcDirectories() throws IOException {
		ServerConfiguration config = new ServerConfiguration(
				"--QWAZR_ETC_DIR=etc1" + File.pathSeparatorChar + "etc2" + File.pathSeparatorChar + "etc3");
		Assert.assertEquals(3, config.etcDirectories.size());
		Assert.assertTrue(config.etcDirectories.contains(new File("etc1")));
		Assert.assertTrue(config.etcDirectories.contains(new File("etc2")));
		Assert.assertTrue(config.etcDirectories.contains(new File("etc3")));
	}

	@Test
	public void etcFilter() throws IOException {
		ServerConfiguration config = new ServerConfiguration("--QWAZR_ETC=common-*.json,prod-*json");
		Assert.assertNotNull(config.etcFileFilter);
		Assert.assertTrue(config.etcFileFilter.accept(new File(dataDir, "etc/common-test.json")));
		Assert.assertTrue(config.etcFileFilter.accept(new File(dataDir, "etc/prod-test.json")));
		Assert.assertFalse(config.etcFileFilter.accept(new File(dataDir, "etc/dev-test.json")));
	}

	@Test
	public void dataDirectory() throws IOException {
		ServerConfiguration config = new ServerConfiguration();
		Assert.assertEquals(dataDir, config.dataDirectory);
	}

	@Test
	public void groups() throws IOException {
		// First with properties
		System.setProperty("QWAZR_GROUPS", "groupProp");
		ServerConfiguration config = new ServerConfiguration();
		Assert.assertEquals(1, config.groups.size());
		Assert.assertTrue(config.groups.contains("groupProp"));

		// Then overrided by arguments
		config = new ServerConfiguration("--QWAZR_GROUPS=group1, group2, group3");
		Assert.assertEquals(3, config.groups.size());
		Assert.assertTrue(config.groups.contains("group1"));
		Assert.assertTrue(config.groups.contains("group2"));
		Assert.assertTrue(config.groups.contains("group3"));
		Assert.assertFalse(config.groups.contains("groupProp"));
	}

	@Test
	public void masters() throws IOException {
		// First with properties
		System.setProperty("QWAZR_MASTERS", "master5:9591");
		ServerConfiguration config = new ServerConfiguration();
		Assert.assertEquals(1, config.masters.size());
		Assert.assertTrue(config.masters.contains("master5:9591"));

		// Then overrided by arguments
		config = new ServerConfiguration("--QWAZR_MASTERS=master6:9691 , master7:9791 ; master8:9891");
		Assert.assertEquals(3, config.masters.size());
		Assert.assertTrue(config.masters.contains("master6:9691"));
		Assert.assertTrue(config.masters.contains("master7:9791"));
		Assert.assertTrue(config.masters.contains("master8:9891"));
		Assert.assertFalse(config.masters.contains("master5:9591"));
	}

	private static Properties getProperties() throws IOException {
		Properties properties = new Properties();
		properties.put("QWAZR_MASTERS", "localhost:9191,localhost:9291");
		properties.put("WEBAPP_PORT", Integer.toString(9190));
		properties.put("WEBSERVICE_PORT", Integer.toString(9191));
		properties.put("QWAZR_DATA", dataDir.getAbsolutePath());
		properties.put("QWAZR_ETC_DIR", confDir.getAbsolutePath() + File.pathSeparator + etcDir.getAbsolutePath());
		return properties;
	}

	public ServerConfiguration getConfig(Properties properties) throws IOException {
		File propFile = Files.createTempFile("qwazr-test", ".properties").toFile();
		try (final FileWriter writer = new FileWriter(propFile)) {
			properties.store(writer, null);
		}
		return new ServerConfiguration("--QWAZR_PROPERTIES=" + propFile.getAbsolutePath());
	}

	@Test
	public void webPort() throws IOException {
		final ServerConfiguration configuration = getConfig(getProperties());
		Assert.assertEquals(configuration.webAppConnector.port, 9190);
		Assert.assertEquals(configuration.webServiceConnector.port, 9191);
		Assert.assertTrue(configuration.masters.contains("localhost:9191"));
		Assert.assertTrue(configuration.masters.contains("localhost:9291"));
	}

	@Test
	public void etcFileFilter() throws IOException {
		final ServerConfiguration configuration = getConfig(getProperties());
		Assert.assertTrue(configuration.etcFileFilter.accept(new File(confDir, "conf_include.json")));
		Assert.assertTrue(configuration.etcFileFilter.accept(new File(etcDir, "conf_exclude.json")));
	}

	@Test
	public void etcExclude() throws IOException {
		final Properties properties = getProperties();
		properties.put("QWAZR_ETC", "!*_exclude.json");
		final ServerConfiguration configuration = getConfig(properties);
		Assert.assertEquals(dataDir, configuration.dataDirectory);
		Assert.assertEquals(2, configuration.etcDirectories.size());
		Assert.assertTrue(configuration.etcDirectories.contains(etcDir));
		Assert.assertTrue(configuration.etcDirectories.contains(confDir));
	}

	@Test
	public void etcInclude() throws IOException {
		final Properties properties = getProperties();
		properties.put("QWAZR_ETC", "*_include.json");
		final ServerConfiguration configuration = getConfig(properties);
		Assert.assertTrue(configuration.etcFileFilter.accept(new File(confDir, "conf_include.json")));
		Assert.assertFalse(configuration.etcFileFilter.accept(new File(etcDir, "conf_exclude.json")));
	}

	@Test
	public void etcExplicitExclusion() throws IOException {
		final Properties properties = getProperties();
		properties.put("QWAZR_ETC", "!*_exclude.json");
		final ServerConfiguration configuration = getConfig(properties);
		Assert.assertTrue(configuration.etcFileFilter.accept(new File(confDir, "conf_include.json")));
		Assert.assertFalse(configuration.etcFileFilter.accept(new File(etcDir, "conf_exclude.json")));
	}

	@Test
	public void bothInclusionExclusion() throws IOException {
		final Properties properties = getProperties();
		properties.put("QWAZR_ETC", "*_include.json,!*_exclude.json");
		final ServerConfiguration configuration = getConfig(properties);
		Assert.assertTrue(configuration.etcFileFilter.accept(new File(confDir, "conf_include.json")));
		Assert.assertFalse(configuration.etcFileFilter.accept(new File(etcDir, "conf_exclude.json")));
	}

	@Test
	public void builder() throws IOException {
		ServerConfiguration config = ServerConfiguration.of()
				.data(dataDir)
				.temp(tempDir)
				.etcDirectory(etcDir)
				.etcDirectory(confDir)
				.etcFilter("*.json")
				.publicAddress("localhost")
				.listenAddress("0.0.0.0")
				.webAppRealm("webapprealm")
				.webAppPort(9390)
				.webServicePort(9391)
				.webServiceRealm("webservicerealm")
				.multicastPort(9392)
				.multicastAddress("224.0.0.1")
				.group("group1")
				.master("localhost:9090")
				.build();
		Assert.assertEquals(dataDir, config.dataDirectory);
		Assert.assertEquals(tempDir, config.tempDirectory);
		Assert.assertTrue(config.etcDirectories.contains(etcDir));
		Assert.assertTrue(config.etcDirectories.contains(confDir));
		Assert.assertEquals("0.0.0.0", config.listenAddress);
		Assert.assertEquals("localhost", config.publicAddress);
		Assert.assertEquals("webapprealm", config.webAppConnector.realm);
		Assert.assertEquals("webservicerealm", config.webServiceConnector.realm);
		Assert.assertEquals(9390, config.webAppConnector.port);
		Assert.assertEquals(9391, config.webServiceConnector.port);
		Assert.assertEquals(9392, config.multicastConnector.port);
		Assert.assertEquals("224.0.0.1", config.multicastConnector.address);
		Assert.assertTrue(config.groups.contains("group1"));
		Assert.assertEquals(1, config.groups.size());
	}
}
