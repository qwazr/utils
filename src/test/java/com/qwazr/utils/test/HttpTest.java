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
package com.qwazr.utils.test;

import com.qwazr.utils.concurrent.ThreadUtils;
import com.qwazr.utils.http.HttpClients;
import com.qwazr.utils.http.HttpRequest;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpTest {

	private String[] badSslURLs = { "https://expired.badssl.com/",
			"https://self-signed.badssl.com/",
			"https://wrong.host.badssl.com/",
			"https://mixed-script.badssl.com/",
			"https://very.badssl.com/" };

	private static ExecutorService executorService;
	private static HttpClients.IdleConnectionMonitorThread monitorThread;

	@BeforeClass
	public static void beforeClass() {
		executorService = Executors.newCachedThreadPool(new ThreadUtils.ExtendedThreadFactory());
		monitorThread = HttpClients.getNewMonitorThread(100, 30000);
		executorService.submit(monitorThread);
	}

	@Test
	public void sslTest() throws Exception {
		System.setProperty("jsse.enableSNIExtension", "false");
		for (String url : badSslURLs) {
			try {
				Assert.assertEquals(url, 200, HttpClients.UNSECURE_HTTP_CLIENT.execute(HttpRequest.Get(url).request)
						.getStatusLine()
						.getStatusCode());
			} catch (Exception e) {
				throw new Exception("Fail on url: " + url, e);
			}
		}
	}

	@AfterClass
	public static void afterClass() {
		monitorThread.shutdown();
		executorService.shutdown();
	}
}
