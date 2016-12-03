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
import org.junit.Test;

import java.io.File;
import java.io.IOException;

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

	@Test
	public void empty() throws IOException {
		ServerConfiguration config = new ServerConfiguration();
		Assert.assertEquals(new File(System.getProperty("user.dir")), config.dataDirectory);
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
		Assert.assertTrue(config.etcFileFilter.accept(new File("etc/common-test.json")));
		Assert.assertTrue(config.etcFileFilter.accept(new File("etc/prod-test.json")));
		Assert.assertFalse(config.etcFileFilter.accept(new File("etc/dev-test.json")));
	}

	@Test
	public void dataDirectory() throws IOException {
		// Absolute path
		System.setProperty("QWAZR_DATA", "/var/lib/qwazr");
		ServerConfiguration config = new ServerConfiguration();
		Assert.assertEquals(new File("/var/lib/qwazr"), config.dataDirectory);

		// Relative path
		System.setProperty("QWAZR_DATA", "qwazr_data");
		config = new ServerConfiguration();
		Assert.assertEquals(new File("qwazr_data"), config.dataDirectory);
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
}
