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
 **/
package com.qwazr.utils.server;

public interface ConfigurationProperties {

	/**
	 * The data directory
	 */
	String QWAZR_DATA = "QWAZR_DATA";

	/**
	 * The configuration directory
	 * The default value is "etc" (relative to the working directory)
	 */
	String QWAZR_ETC_DIR = "QWAZR_ETC_DIR";

	/**
	 * A wildcard filter for configuration files
	 */
	String QWAZR_ETC = "QWAZR_ETC";

	/**
	 * The hostname or address uses for the listening socket.
	 * The default value is: 0.0.0.0
	 */
	String LISTEN_ADDR = "LISTEN_ADDR";

	/**
	 * The public hostname or address and port for external access (node
	 * communication). The default value is the LISTEN_ADDR.
	 */
	String PUBLIC_ADDR = "PUBLIC_ADDR";

	String WEBAPP_REALM = "WEBAPP_REALM";

	String WEBAPP_PORT = "WEBAPP_PORT";

	String WEBSERVICE_REALM = "WEBSERVICE_REALM";

	String WEBSERVICE_PORT = "WEBSERVICE_PORT";

	String MULTICAST_ADDR = "MULTICAST_ADDR";

	String MULTICAST_PORT = "MULTICAST_PORT";

	String QWAZR_MASTERS = "QWAZR_MASTERS";

	String QWAZR_GROUPS = "QWAZR_GROUPS";

}


