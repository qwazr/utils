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

import com.qwazr.utils.RegExpUtils;
import com.qwazr.utils.UBuilder;
import org.junit.Assert;
import org.junit.Test;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

public class UBuilderTest {

	private final static String URL = "http://www.qwazr.com/test;parm=1234?qry=5678";

	@Test
	public void cleanPath() throws URISyntaxException {
		UBuilder uBuilder = new UBuilder(URL);
		List<Matcher> list = RegExpUtils.getMatcherList(Arrays.asList(";parm=[0-9]{4}"));
		uBuilder.cleanPath(list);
		Assert.assertEquals("http://www.qwazr.com/test?qry=5678", uBuilder.build().toString());
	}

	@Test
	public void removeParameter() throws URISyntaxException {
		UBuilder uBuilder = new UBuilder(URL);
		List<Matcher> list = RegExpUtils.getMatcherList(Arrays.asList("qry=[0-9]{4}"));
		uBuilder.removeMatchingParameters(list);
		Assert.assertEquals("http://www.qwazr.com/test;parm=1234", uBuilder.build().toString());
	}

	@Test
	public void cleanPathAndParameter() throws URISyntaxException {
		UBuilder uBuilder = new UBuilder(URL);
		List<Matcher> pathList = RegExpUtils.getMatcherList(Arrays.asList(";parm=[0-9]{4}"));
		List<Matcher> queryList = RegExpUtils.getMatcherList(Arrays.asList("qry=[0-9]{4}"));
		uBuilder.cleanPath(pathList);
		uBuilder.removeMatchingParameters(queryList);
		Assert.assertEquals("http://www.qwazr.com/test", uBuilder.build().toString());

	}
}


