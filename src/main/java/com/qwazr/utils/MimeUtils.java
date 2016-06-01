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
package com.qwazr.utils;

import org.apache.commons.fileupload.*;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;

import java.io.*;
import java.nio.charset.Charset;
import java.util.List;

public class MimeUtils {

	public static class StreamFileUpload extends FileUpload {

		public StreamFileUpload(final File repository) {
			super(new DiskFileItemFactory(0, repository));
		}

		public List<FileItem> parse(final String contentType, final long contentLength, final String encoding,
				final InputStream inputStream) throws FileUploadException {
			return parseRequest(new StreamRequestContext(contentType, contentLength, encoding, inputStream));
		}

		public List<FileItem> parse(final HttpEntity httpEntity) throws IOException, FileUploadException {
			return parseRequest(new StreamRequestContext(httpEntity));
		}
	}

	public static class StreamRequestContext implements RequestContext {

		private final String contentType;
		private final int contentLength;
		private final String encoding;
		private final InputStream inputStream;

		public StreamRequestContext(final String contentType, final long contentLength, final String encoding,
				final InputStream inputStream) {
			this.contentType = contentType;
			this.contentLength = (int) contentLength;
			this.encoding = encoding;
			this.inputStream = inputStream;
		}

		public StreamRequestContext(final HttpEntity entity) throws IOException {
			final ContentType ct = ContentType.getLenient(entity);
			this.contentType = ct.toString();
			this.contentLength = (int) entity.getContentLength();
			final Charset charset = ct.getCharset();
			this.encoding = charset == null ? null : charset.name();
			this.inputStream = entity.getContent();
		}

		@Override
		public String getCharacterEncoding() {
			return encoding;
		}

		@Override
		public String getContentType() {
			return contentType;
		}

		@Override
		@Deprecated
		public int getContentLength() {
			return contentLength;
		}

		@Override
		public InputStream getInputStream() throws IOException {
			return inputStream;
		}
	}
}
