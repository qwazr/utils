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
 */
package com.qwazr.utils;

import org.apache.http.NameValuePair;
import org.apache.http.client.utils.URIBuilder;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;

public class UBuilder extends URIBuilder {

	public UBuilder() {
		super();
	}

	public UBuilder(final String string) throws URISyntaxException {
		super(string);
	}

	public UBuilder(final URI uri) {
		super(uri);
	}

	/**
	 * Add the query parameters if the object parameter is not null
	 *
	 * @param param  the name of the parameter
	 * @param object an optional value
	 * @return the current UBuilder
	 */
	final public UBuilder setParameterObject(final String param, final Object object) {
		if (object == null)
			return this;
		super.setParameter(param, object.toString());
		return this;
	}

	/**
	 * Add the query parameter. If the value is null nothing is added.
	 *
	 * @param param the name of the parameter
	 * @param value an optional value
	 * @return the current UBuilder
	 */
	@Override
	final public UBuilder setParameter(final String param, final String value) {
		if (value == null)
			return this;
		super.setParameter(param, value);
		return this;
	}

	final public UBuilder setParameter(final String param, final Enum value) {
		if (value == null)
			return this;
		super.setParameter(param, value.name());
		return this;
	}

	final public UBuilder setParameter(final String param, final Number value) {
		if (value == null)
			return this;
		super.setParameter(param, value.toString());
		return this;
	}

	/**
	 * Set common parameters for QWAZR services
	 *
	 * @param local     an optional local parameter (can be null)
	 * @param group     an optional group parameter (can be null)
	 * @param msTimeout an optional timeout parameter in milliseconds (can be null)
	 * @return the current UBuilder
	 */
	final public UBuilder setParameters(final Boolean local, final String group, final Integer msTimeout) {
		setParameterObject("local", local);
		setParameterObject("group", group);
		setParameterObject("timeout", msTimeout);
		return this;
	}

	/**
	 * Build the URI. This build masks the URISyntaxException (replaced by an IllegalArgumentException)
	 *
	 * @return a new URI
	 */
	final public URI buildNoEx() {
		try {
			return super.build();
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		}
	}

	final public void removeMatchingParameters(final Collection<Matcher> matcherList) {
		if (matcherList == null || matcherList.isEmpty())
			return;
		final List<NameValuePair> oldParams = getQueryParams();
		if (oldParams == null || oldParams.isEmpty())
			return;
		clearParameters();
		for (NameValuePair param : oldParams)
			if (!RegExpUtils.anyMatch(param.getName() + "=" + param.getValue(), matcherList))
				addParameter(param.getName(), param.getValue());
	}

	final public void cleanPath(final Collection<Matcher> matcherList) {
		if (matcherList == null || matcherList.isEmpty())
			return;
		String path = getPath();
		if (path == null || path.isEmpty())
			return;
		setPath(RegExpUtils.removeAllMatches(path, matcherList));
	}

}