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
package com.qwazr.utils;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

public class LinkUtils {

	public static String urlHostPathWrapReduce(final String url, final int maxSize) {
		final URL u;
		try {
			u = new URL(url);
		} catch (MalformedURLException e) {
			return url;
		}
		final String path = StringUtils.joinWithSeparator('/', u.getHost(), u.getPath());
		final String[] frags = StringUtils.split(path, '/');
		if (frags.length < 2)
			return frags[0];
		int startPos = 1;
		int endPos = frags.length - 2;
		final StringBuilder sbStart = new StringBuilder(frags[0]);
		final StringBuilder sbEnd = new StringBuilder(frags[frags.length - 1]);
		final int length = sbStart.length() + sbEnd.length();
		for (; ; ) {
			boolean bHandled = false;
			if (startPos != -1 && startPos < endPos) {
				if (frags[startPos].length() + length < maxSize) {
					sbStart.append('/');
					sbStart.append(frags[startPos++]);
					bHandled = true;
				}
			}
			if (endPos != -1 && endPos > startPos) {
				if (frags[endPos].length() + length < maxSize) {
					sbEnd.insert(0, '/');
					sbEnd.insert(0, frags[endPos--]);
					bHandled = true;
				}
			}
			if (!bHandled)
				break;
		}
		return StringUtils.joinWithSeparator('/', sbStart, "…", sbEnd);
	}

	public static String lastPart(String path) {
		if (path == null)
			return null;
		final String[] parts = StringUtils.split(path, '/');
		if (parts == null)
			return path;
		if (parts.length == 0)
			return path;
		return parts[parts.length - 1];
	}

	public static String UTF8_URL_Encode(String s) throws UnsupportedEncodingException {
		return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
	}

}
