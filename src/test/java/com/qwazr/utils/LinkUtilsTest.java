/*
 * Copyright 2016-2017 Emmanuel Keller / QWAZR
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
package com.qwazr.utils;

import org.junit.Assert;
import org.junit.Test;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

public class LinkUtilsTest {

	@Test
	public void lastPart() throws MalformedURLException, UnsupportedEncodingException {
		Assert.assertEquals("my+folder", LinkUtils.lastPart("/my+folder/"));
		Assert.assertEquals("my folder", LinkUtils.lastPart("my folder/"));
		Assert.assertEquals("my+sub-folder", LinkUtils.lastPart("my folder/my+sub-folder/"));
		Assert.assertEquals("my+file.png", LinkUtils.lastPart("/my+file.png"));
		Assert.assertEquals("my+file.png", LinkUtils.lastPart("my+file.png"));
		Assert.assertEquals("my+file.png", LinkUtils.lastPart("my+folder/my+sub-folder/my+file.png"));
		Assert.assertEquals("outlook%3AINBOX%2F%7ETEST%20TEST", LinkUtils.UTF8_URL_Encode("outlook:INBOX/~TEST TEST"));
	}

	@Test
	public void testUrlHostPathWrapReduce() {
		final String url =
				"file://Users/ekeller/Moteur/infotoday_enterprisesearchsourcebook08/Open_on_Windows.exe?test=2";
		Assert.assertEquals("Users/ekeller/…/infotoday_enterprisesearchsourcebook08/Open_on_Windows.exe",
				LinkUtils.urlHostPathWrapReduce(url, 80));
		Assert.assertEquals("www.qwazr.com", LinkUtils.urlHostPathWrapReduce("http://www.qwazr.com/", 80));
	}
}


