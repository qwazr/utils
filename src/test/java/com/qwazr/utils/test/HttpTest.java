/**
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
package com.qwazr.utils.test;

import com.qwazr.utils.http.HttpClients;
import com.qwazr.utils.http.HttpRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

public class HttpTest {

	private String[] badSslURLs = { "https://expired.badssl.com/",
			"https://self-signed.badssl.com/",
			"https://wrong.host.badssl.com/",
			"https://mixed-script.badssl.com/",
			"https://very.badssl.com/" };

	@BeforeClass
	public static void beforeClass() {
		Assert.assertNotNull(HttpClients.startMonitorThread(null, 100, 30000));
	}

	@Test
	public void sslTest() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
		System.setProperty("jsse.enableSNIExtension", "false");
		for (String url : badSslURLs)
			try {
				Assert.assertEquals(url, 200, HttpClients.UNSECURE_HTTP_CLIENT.execute(HttpRequest.Get(url).request)
						.getStatusLine()
						.getStatusCode());
			} catch (Exception e) {
				e.printStackTrace();
				Assert.fail("Fail on url: " + url);
			}
	}

	@AfterClass
	public static void afterClass() {
		HttpClients.stopMonitorThread();
	}
}
