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
package com.qwazr.utils.server;

import io.undertow.Undertow;
import io.undertow.Undertow.Builder;
import io.undertow.UndertowOptions;
import io.undertow.security.api.AuthenticationMechanism;
import io.undertow.security.api.AuthenticationMode;
import io.undertow.security.handlers.AuthenticationCallHandler;
import io.undertow.security.handlers.AuthenticationConstraintHandler;
import io.undertow.security.handlers.AuthenticationMechanismsHandler;
import io.undertow.security.handlers.SecurityInitialHandler;
import io.undertow.security.idm.IdentityManager;
import io.undertow.security.impl.BasicAuthenticationMechanism;
import io.undertow.server.HttpHandler;
import io.undertow.server.session.SessionListener;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

public class GenericServer {

	static volatile GenericServer INSTANCE = null;

	final Collection<Class<? extends ServiceInterface>> webServices;
	final Collection<String> webServiceNames;
	final Collection<String> webServicePaths;
	final private IdentityManagerProvider identityManagerProvider;

	final private Collection<Listener> startedListeners;
	final private Collection<Listener> shutdownListeners;

	final protected Collection<Undertow> undertows;
	final protected Collection<DeploymentManager> deploymentManagers;

	final protected ExecutorService executorService;

	final protected ServerConfiguration serverConfiguration;

	final private Collection<ServletInfo> servletInfos;
	final private SessionPersistenceManager sessionPersistenceManager;
	final private SessionListener sessionListener;
	final private Logger servletAccessLogger;
	final private Logger restAccessLogger;

	final protected UdpServerThread udpServer;

	static final private Logger logger = LoggerFactory.getLogger(GenericServer.class);

	GenericServer(ServerBuilder builder) {
		this.executorService = builder.executorService;
		this.serverConfiguration = builder.serverConfiguration;
		this.webServices = builder.webServices.isEmpty() ? null : new ArrayList<>(builder.webServices);
		this.webServiceNames = builder.webServiceNames.isEmpty() ? null : new ArrayList<>(builder.webServiceNames);
		this.webServicePaths = builder.webServicePaths.isEmpty() ? null : new ArrayList<>(builder.webServicePaths);
		this.undertows = new ArrayList<>();
		this.deploymentManagers = new ArrayList<>();
		this.identityManagerProvider = builder.identityManagerProvider;
		this.servletInfos = builder.servletInfos.isEmpty() ? null : new ArrayList<>(builder.servletInfos);
		this.sessionPersistenceManager = builder.sessionPersistenceManager;
		this.sessionListener = builder.sessionListener;
		this.servletAccessLogger = builder.servletAccessLogger;
		this.restAccessLogger = builder.restAccessLogger;
		this.udpServer = buildUdpServer(builder);
		this.startedListeners = builder.startedListeners.isEmpty() ? null : new ArrayList<>(builder.startedListeners);
		this.shutdownListeners =
				builder.shutdownListeners.isEmpty() ? null : new ArrayList<>(builder.shutdownListeners);
	}

	private static UdpServerThread buildUdpServer(final ServerBuilder builder) {
		if (builder.packetListeners == null || builder.packetListeners.isEmpty())
			return null;
		final InetSocketAddress socketAddress = new InetSocketAddress(builder.serverConfiguration.listenAddress,
				builder.serverConfiguration.webServiceConnector.port);
		return new UdpServerThread(socketAddress, null, null, builder.packetListeners);
	}

	private synchronized void start(final Undertow undertow) {
		undertow.start();
		undertows.add(undertow);
	}

	private synchronized HttpHandler start(final DeploymentManager manager, final Logger accessLogger)
			throws ServletException {
		HttpHandler handler = manager.start();
		if (accessLogger != null)
			handler = new LogHandler(handler, accessLogger);
		deploymentManagers.add(manager);
		return handler;
	}

	public synchronized void stopAll() {

		executeListener(shutdownListeners);

		if (udpServer != null)
			udpServer.shutdown();
		for (DeploymentManager manager : deploymentManagers)
			try {
				manager.stop();
			} catch (ServletException e) {
				if (logger.isWarnEnabled())
					logger.warn("Cannot stop the manager: " + e.getMessage(), e);
			}
		for (Undertow undertow : undertows)
			undertow.stop();
	}

	private final IdentityManager getIdentityManager(ServerConfiguration.HttpConnector connector,
			DeploymentInfo deploymentInfo) throws IOException {
		if (identityManagerProvider == null)
			return null;
		if (connector == null)
			return null;
		if (connector.realm == null)
			return null;
		IdentityManager identityManager = identityManagerProvider.getIdentityManager(connector.realm);
		if (identityManager == null)
			return null;
		deploymentInfo.setIdentityManager(identityManager).setLoginConfig(
				new LoginConfig(connector.authType == null ? "BASIC" : connector.authType, connector.realm));
		return identityManager;
	}

	private final void startHttpServer(ServerConfiguration.HttpConnector connector, DeploymentInfo deploymentInfo,
			Logger accessLogger) throws IOException, ServletException {
		IdentityManager identityManager = getIdentityManager(connector, deploymentInfo);

		DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
		manager.deploy();

		HttpHandler httpHandler = start(manager, accessLogger);

		if (identityManager != null)
			httpHandler = addSecurity(httpHandler, identityManager, serverConfiguration.webAppConnector.realm);

		logger.info("Start the connector " + serverConfiguration.listenAddress + ":" + connector.port);

		Builder servletBuilder = Undertow.builder().addHttpListener(connector.port, serverConfiguration.listenAddress)
				.setServerOption(UndertowOptions.NO_REQUEST_TIMEOUT, 10000).setHandler(httpHandler);
		start(servletBuilder.build());
	}

	/**
	 * Call this method to start the server
	 *
	 * @throws IOException      if any IO error occur
	 * @throws ServletException if the servlet configuration failed
	 */
	final public GenericServer start(boolean shutdownHook)
			throws IOException, ServletException, IllegalAccessException, InstantiationException {

		java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);

		if (!serverConfiguration.dataDirectory.exists())
			throw new IOException("The data directory does not exists: " + serverConfiguration.dataDirectory);
		if (!serverConfiguration.dataDirectory.isDirectory())
			throw new IOException("The data directory path is not a directory: " + serverConfiguration.dataDirectory);
		logger.info("Data directory sets to: " + serverConfiguration.dataDirectory);

		if (udpServer != null)
			udpServer.checkStarted();

		// Launch the servlet application if any
		if (servletInfos != null && !servletInfos.isEmpty())
			startHttpServer(serverConfiguration.webAppConnector,
					ServletApplication.getDeploymentInfo(servletInfos, sessionPersistenceManager, sessionListener),
					servletAccessLogger);

		// Launch the jaxrs application if any
		if (webServices != null && !webServices.isEmpty())
			startHttpServer(serverConfiguration.webServiceConnector, RestApplication.getDeploymentInfo(),
					restAccessLogger);

		if (shutdownHook) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				public void run() {
					stopAll();
				}
			});
		}

		executeListener(startedListeners);

		return this;
	}

	private static HttpHandler addSecurity(HttpHandler handler, final IdentityManager identityManager, String realm) {
		handler = new AuthenticationCallHandler(handler);
		handler = new AuthenticationConstraintHandler(handler);
		final List<AuthenticationMechanism> mechanisms =
				Collections.<AuthenticationMechanism>singletonList(new BasicAuthenticationMechanism(realm));
		handler = new AuthenticationMechanismsHandler(handler, mechanisms);
		handler = new SecurityInitialHandler(AuthenticationMode.PRO_ACTIVE, identityManager, handler);
		return handler;
	}

	public Collection<String> getServiceNames() {
		return webServiceNames;
	}

	public Collection<String> getServicePaths() {
		return webServicePaths;
	}

	public interface Listener {

		void accept(GenericServer server);
	}

	private void executeListener(final Collection<Listener> listeners) {
		if (listeners == null)
			return;
		listeners.forEach(listener -> {
			try {
				listener.accept(this);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		});
	}

	public interface IdentityManagerProvider {

		IdentityManager getIdentityManager(String realm) throws IOException;

	}
}
