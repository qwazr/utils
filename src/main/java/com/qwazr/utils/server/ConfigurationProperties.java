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

import org.aeonbits.owner.Config;

@Config.LoadPolicy(Config.LoadType.MERGE)
public interface ConfigurationProperties extends Config {

	/**
	 * The data directory
	 */
	@Key("QWAZR_DATA")
	String dataDirectory();

	/**
	 * The configuration directory
	 */
	@Key("QWAZR_ETC_DIR")
	@DefaultValue("etc")
	String configDirectories();

	/**
	 * The hostname or address uses for the listening socket
	 */
	@Key("LISTEN_ADDR")
	@DefaultValue("0.0.0.0")
	String listenAddress();

	/**
	 * The public hostname or address and port for external access (node
	 * communication)
	 */
	@Key("PUBLIC_ADDR")
	String publicAddress();

	@Key("WEBAPP_REALM")
	String webAppRealm();

	@Key("WEBAPP_PORT")
	@DefaultValue("9090")
	int webAppPort();

	@Key("WEBSERVICE_REALM")
	String webServiceRealm();

	@Key("WEBSERVICE_PORT")
	@DefaultValue("9091")
	int webServicePort();

	@Key("MULTICAST_ADDR")
	String multicastAddress();

	@Key("MULTICAST_PORT")
	Integer multicastPort();

}


