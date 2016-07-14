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
import org.apache.commons.io.input.ReaderInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;

import javax.ws.rs.core.StreamingOutput;
import java.io.*;
import java.nio.charset.Charset;

public abstract class AbstractStreamingOutput implements StreamingOutput {

	final public static HttpResponseStreamingOutput with(final CloseableHttpResponse response) {
		return new HttpResponseStreamingOutput(response);
	}

	final public static InputStreamingOutput with(final InputStream input) {
		return new InputStreamingOutput(input);
	}

	final public static AbstractStreamingOutput with(final Reader reader, final Charset charset) {
		return new ReaderStreamingOutput(reader, charset);
	}

	public abstract InputStream getInputStream() throws IOException;

	final public static class HttpResponseStreamingOutput extends AbstractStreamingOutput {

		private final CloseableHttpResponse response;

		private HttpResponseStreamingOutput(final CloseableHttpResponse response) {
			this.response = response;
		}

		final public InputStream getInputStream() throws IOException {
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

	public static class InputStreamingOutput extends AbstractStreamingOutput {

		protected final InputStream inputStream;

		private InputStreamingOutput(final InputStream inputStream) {
			this.inputStream = inputStream;
		}

		final public InputStream getInputStream() throws IOException {
			if (inputStream == null)
				throw new IOException("The inputStream is null");
			return inputStream;
		}

		@Override
		final public void write(final OutputStream output) throws IOException {
			IOUtils.copy(getInputStream(), output);
			IOUtils.closeQuietly(inputStream);
		}
	}

	public static class ReaderStreamingOutput extends InputStreamingOutput {

		private ReaderStreamingOutput(final Reader reader, final Charset charset) {
			super(new ReaderInputStream(reader, charset));
		}

	}
}