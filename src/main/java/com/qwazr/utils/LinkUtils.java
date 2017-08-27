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

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;

import javax.ws.rs.core.MultivaluedHashMap;
import javax.ws.rs.core.MultivaluedMap;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.List;

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
		String[] parts = StringUtils.split(path, '/');
		if (parts == null)
			return path;
		if (parts.length == 0)
			return path;
		return parts[parts.length - 1];
	}

	public static String UTF8_URL_Encode(String s) throws UnsupportedEncodingException {
		return URLEncoder.encode(s, "UTF-8").replace("+", "%20");
	}

	public static String UTF8_URL_QuietDecode(String s) {
		try {
			return URLDecoder.decode(s, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return s;
		}
	}

	public static URI newEncodedURI(String u) throws MalformedURLException, URISyntaxException {
		URL tmpUrl = new URL(u);
		return new URI(tmpUrl.getProtocol(), tmpUrl.getUserInfo(), tmpUrl.getHost(), tmpUrl.getPort(), tmpUrl.getPath(),
				tmpUrl.getQuery(), tmpUrl.getRef());
	}

	public static URL newEncodedURL(String u) throws MalformedURLException, URISyntaxException {
		return newEncodedURI(u).toURL();
	}

	public static MultivaluedMap<String, String> getQueryParameters(final String queryString) {
		if (queryString == null || queryString.isEmpty())
			return null;
		final MultivaluedHashMap<String, String> map = new MultivaluedHashMap<>();
		final List<NameValuePair> parameters = URLEncodedUtils.parse(queryString, StandardCharsets.UTF_8);
		if (parameters != null)
			parameters.forEach(pair -> map.add(pair.getName(), pair.getValue()));
		return map;
	}

	public static String[] getPathSegments(final String path) {
		return path == null ? null : StringUtils.split(path, '/');
	}

	public static URI resolveQuietly(URI uri, String href) {
		if (uri == null || href == null)
			return null;
		try {
			return URIUtils.resolve(uri, href);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

}
