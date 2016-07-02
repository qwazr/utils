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
import io.undertow.security.idm.IdentityManager;
import io.undertow.server.HttpHandler;
import io.undertow.server.session.SessionListener;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanException;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.OperationsException;
import javax.servlet.ServletException;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.logging.Level;

public class GenericServer {

	static volatile GenericServer INSTANCE = null;

	final Collection<Class<? extends ServiceInterface>> webServices;
	final Collection<String> webServiceNames;
	final Collection<String> webServicePaths;
	final private IdentityManagerProvider identityManagerProvider;
	final private Collection<ConnectorStatisticsMXBean> connectorsStatistics;

	final private Collection<Listener> startedListeners;
	final private Collection<Listener> shutdownListeners;

	final protected Collection<Undertow> undertows;
	final protected Collection<DeploymentManager> deploymentManagers;

	final protected ExecutorService executorService;

	final protected ServerConfiguration serverConfiguration;

	final private Collection<SecurableServletInfo> servletInfos;
	final private Map<String, FilterInfo> filterInfos;
	final private Collection<ListenerInfo> listenerInfos;
	final private SessionPersistenceManager sessionPersistenceManager;
	final private SessionListener sessionListener;
	final private Logger servletAccessLogger;
	final private Logger restAccessLogger;

	final protected UdpServerThread udpServer;

	static final private Logger logger = LoggerFactory.getLogger(GenericServer.class);

	GenericServer(final ServerBuilder builder) {
		this.executorService = builder.executorService;
		this.serverConfiguration = builder.serverConfiguration;
		this.webServices = builder.webServices.isEmpty() ? null : new ArrayList<>(builder.webServices);
		this.webServiceNames = builder.webServiceNames.isEmpty() ? null : new ArrayList<>(builder.webServiceNames);
		this.webServicePaths = builder.webServicePaths.isEmpty() ? null : new ArrayList<>(builder.webServicePaths);
		this.undertows = new ArrayList<>();
		this.deploymentManagers = new ArrayList<>();
		this.identityManagerProvider = builder.identityManagerProvider;
		this.servletInfos = builder.servletInfos.isEmpty() ? null : new ArrayList<>(builder.servletInfos);
		this.filterInfos = builder.filterInfos.isEmpty() ? null : new LinkedHashMap(builder.filterInfos);
		this.listenerInfos = builder.listenerInfos.isEmpty() ? null : new ArrayList<>(builder.listenerInfos);
		this.sessionPersistenceManager = builder.sessionPersistenceManager;
		this.sessionListener = builder.sessionListener;
		this.servletAccessLogger = builder.servletAccessLogger;
		this.restAccessLogger = builder.restAccessLogger;
		this.udpServer = buildUdpServer(builder);
		this.startedListeners = builder.startedListeners.isEmpty() ? null : new ArrayList<>(builder.startedListeners);
		this.shutdownListeners =
				builder.shutdownListeners.isEmpty() ? null : new ArrayList<>(builder.shutdownListeners);
		this.connectorsStatistics = new ArrayList<>();
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
		undertows.forEach(Undertow::stop);
	}

	private final IdentityManager getIdentityManager(final ServerConfiguration.HttpConnector connector)
			throws IOException {
		if (identityManagerProvider == null || connector == null || connector.realm == null)
			return null;
		return identityManagerProvider.getIdentityManager(connector.realm);
	}

	private void startHttpServer(final ServerConfiguration.HttpConnector connector, final DeploymentInfo deploymentInfo,
			final Logger accessLogger, final String jmxName)
			throws IOException, ServletException, OperationsException, MBeanException {

		if (deploymentInfo.getIdentityManager() != null)
			deploymentInfo.setLoginConfig(Servlets.loginConfig("BASIC", connector.realm));

		final DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
		manager.deploy();

		HttpHandler httpHandler = manager.start();
		final LogMetricsHandler logMetricsHandler =
				new LogMetricsHandler(httpHandler, accessLogger, serverConfiguration.listenAddress, connector.port,
						jmxName);
		deploymentManagers.add(manager);
		httpHandler = logMetricsHandler;

		logger.info("Start the connector " + serverConfiguration.listenAddress + ":" + connector.port);

		Builder servletBuilder = Undertow.builder()
				.addHttpListener(connector.port, serverConfiguration.listenAddress)
				.setServerOption(UndertowOptions.NO_REQUEST_TIMEOUT, 10000)
				.setHandler(httpHandler);
		start(servletBuilder.build());

		// Register MBeans
		final MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
		final Hashtable<String, String> props = new Hashtable<>();
		props.put("type", "connector");
		props.put("name", jmxName);
		final ObjectName name = new ObjectName("com.qwazr.server", props);
		mbs.registerMBean(logMetricsHandler, name);
		connectorsStatistics.add(logMetricsHandler);
	}

	/**
	 * Call this method to start the server
	 *
	 * @throws IOException      if any IO error occur
	 * @throws ServletException if the servlet configuration failed
	 */
	final public GenericServer start(boolean shutdownHook)
			throws IOException, ServletException, ReflectiveOperationException, OperationsException, MBeanException {

		java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);

		if (!serverConfiguration.dataDirectory.exists())
			throw new IOException("The data directory does not exists: " + serverConfiguration.dataDirectory);
		if (!serverConfiguration.dataDirectory.isDirectory())
			throw new IOException("The data directory path is not a directory: " + serverConfiguration.dataDirectory);
		logger.info("Data directory sets to: " + serverConfiguration.dataDirectory);

		if (udpServer != null)
			udpServer.checkStarted();

		// Launch the servlet application if any
		if (servletInfos != null && !servletInfos.isEmpty()) {
			final IdentityManager identityManager = getIdentityManager(serverConfiguration.webAppConnector);
			startHttpServer(serverConfiguration.webAppConnector,
					ServletApplication.getDeploymentInfo(servletInfos, identityManager, filterInfos, listenerInfos,
							sessionPersistenceManager, sessionListener), servletAccessLogger,
					ServerConfiguration.PrefixEnum.WEBAPP.name());
		}

		// Launch the jaxrs application if any
		if (webServices != null && !webServices.isEmpty()) {
			final IdentityManager identityManager = getIdentityManager(serverConfiguration.webServiceConnector);
			startHttpServer(serverConfiguration.webServiceConnector, RestApplication.getDeploymentInfo(identityManager),
					restAccessLogger, ServerConfiguration.PrefixEnum.WEBSERVICE.name());
		}

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

	public Collection<ConnectorStatisticsMXBean> getConnectorsStatistics() {
		return connectorsStatistics;
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
