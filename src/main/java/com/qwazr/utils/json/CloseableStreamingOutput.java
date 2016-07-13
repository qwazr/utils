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
package com.qwazr.utils.json;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;

import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class CloseableStreamingOutput implements StreamingOutput {

	private final CloseableHttpResponse response;
	private final InputStream input;

	public CloseableStreamingOutput(final CloseableHttpResponse response) {
		this.response = response;
		this.input = null;
	}

	public CloseableStreamingOutput(final InputStream input) {
		this.response = null;
		this.input = input;
	}

	final public InputStream getInputStream() throws IOException {
		if (input != null)
			return input;
		try {
			if (response == null)
				throw new ClientProtocolException("The response is null");
			final HttpEntity entity = response.getEntity();
			if (entity == null)
				throw new ClientProtocolException("The entity is null");
			final InputStream input = entity.getContent();
			if (input == null)
				throw new ClientProtocolException("The entity content is empty");
			return input;
		} catch (ClientProtocolException e) {
			IOUtils.closeQuietly(response);
			throw e;
		}
	}

	@Override
	final public void write(final OutputStream output) throws IOException {
		IOUtils.copy(getInputStream(), output);
		IOUtils.closeQuietly(response);
	}
}
