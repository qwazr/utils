/**
 * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import org.apache.http.HttpEntity;
import org.apache.http.impl.client.AbstractResponseHandler;

import java.io.IOException;
import java.io.InputStream;

public class InputStreamResponseHandler extends AbstractResponseHandler<InputStream> {

	@Override
	public InputStream handleEntity(final HttpEntity httpEntity) throws IOException {
		return httpEntity.getContent();
	}
}
