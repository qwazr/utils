package com.qwazr.utils.test;

import com.qwazr.utils.http.HttpUtils;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by ekeller on 15/05/16.
 */
public class HttpTest {

	private String[] badSslURLs =
			{ "https://expired.badssl.com/", "https://self-signed.badssl.com/", "https://wrong.host.badssl.com/",
					"https://mixed-script.badssl.com/", "https://very.badssl.com/" };

	@Test
	public void sslTest() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
		System.setProperty("jsse.enableSNIExtension", "false");
		try (final CloseableHttpClient client = HttpUtils.createHttpClient_AcceptsUntrustedCerts()) {
			final Executor executor = Executor.newInstance(client);
			for (String url : badSslURLs)
				try {
					Assert.assertEquals(url, 200,
							executor.execute(Request.Get(url)).returnResponse().getStatusLine().getStatusCode());
				} catch (Exception e) {
					e.printStackTrace();
					Assert.fail("Fail on url: " + url);
				}
		}
	}
}
