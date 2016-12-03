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

import com.qwazr.utils.file.TrackedInterface;
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
import java.util.function.Consumer;
import java.util.logging.Level;

public class GenericServer {

	// One instance per JVM
	private static volatile GenericServer INSTANCE = null;

	final private ServerBuilder<?> builder;
	final private TrackedInterface etcTracker;

	final private Collection<Class<? extends ServiceInterface>> webServices;
	final private Collection<String> webServiceNames;
	final private Collection<String> webServicePaths;
	final private IdentityManagerProvider identityManagerProvider;
	final private Collection<ConnectorStatisticsMXBean> connectorsStatistics;

	final private Collection<Listener> startedListeners;
	final private Collection<Listener> shutdownListeners;

	final private Collection<Undertow> undertows;
	final private Collection<DeploymentManager> deploymentManagers;

	final private ServerConfiguration serverConfiguration;

	final private Collection<SecurableServletInfo> servletInfos;
	final private Map<String, FilterInfo> filterInfos;
	final private Collection<ListenerInfo> listenerInfos;
	final private SessionPersistenceManager sessionPersistenceManager;
	final private SessionListener sessionListener;
	final private Logger servletAccessLogger;
	final private Logger restAccessLogger;

	final protected UdpServerThread udpServer;

	static final private Logger LOGGER = LoggerFactory.getLogger(GenericServer.class);

	protected GenericServer(final ServerConfiguration serverConfiguration) throws IOException {
		synchronized (GenericServer.class) {
			if (INSTANCE != null)
				throw new RuntimeException("The server " + getClass().getName() + " is already running");

			this.builder = new ServerBuilder<>(serverConfiguration);
			this.etcTracker =
					TrackedInterface.build(serverConfiguration.etcDirectories, serverConfiguration.etcFileFilter);

			this.serverConfiguration = builder.serverConfiguration;
			this.webServices = builder.webServices.isEmpty() ? null : new ArrayList<>(builder.webServices);
			this.webServiceNames = builder.webServiceNames.isEmpty() ? null : new ArrayList<>(builder.webServiceNames);
			this.webServicePaths = builder.webServicePaths.isEmpty() ? null : new ArrayList<>(builder.webServicePaths);
			this.undertows = new ArrayList<>();
			this.deploymentManagers = new ArrayList<>();
			this.identityManagerProvider = builder.identityManagerProvider;
			this.servletInfos = builder.servletInfos.isEmpty() ? null : new ArrayList<>(builder.servletInfos);
			this.filterInfos = builder.filterInfos.isEmpty() ? null : new LinkedHashMap<>(builder.filterInfos);
			this.listenerInfos = builder.listenerInfos.isEmpty() ? null : new ArrayList<>(builder.listenerInfos);
			this.sessionPersistenceManager = builder.sessionPersistenceManager;
			this.sessionListener = builder.sessionListener;
			this.servletAccessLogger = builder.servletAccessLogger;
			this.restAccessLogger = builder.restAccessLogger;
			this.udpServer = buildUdpServer(builder);
			this.startedListeners =
					builder.startedListeners.isEmpty() ? null : new ArrayList<>(builder.startedListeners);
			this.shutdownListeners =
					builder.shutdownListeners.isEmpty() ? null : new ArrayList<>(builder.shutdownListeners);
			this.connectorsStatistics = new ArrayList<>();

			if (this.etcTracker != null)
				builder.etcConsumers.forEach(this.etcTracker::register);

			INSTANCE = this;
		}
	}

	public static GenericServer getInstance() {
		return INSTANCE;
	}

	public void forEachWebServices(final Consumer<Class<? extends ServiceInterface>> consumer) {
		if (webServices != null)
			webServices.forEach(consumer::accept);
	}

	public void forEachServicePath(final Consumer<String> consumer) {
		if (webServicePaths != null)
			webServicePaths.forEach(consumer::accept);
	}

	public Collection<String> getWebServiceNames() {
		return webServiceNames;
	}

	protected ServerBuilder getBuilder() {
		return builder;
	}

	protected TrackedInterface getEtcTracker() {
		return etcTracker;
	}

	private static UdpServerThread buildUdpServer(final ServerBuilder builder) throws IOException {
		if (builder.packetListeners == null || builder.packetListeners.isEmpty())
			return null;

		if (builder.serverConfiguration.multicastConnector.address != null
				&& builder.serverConfiguration.multicastConnector.port != -1)
			return new UdpServerThread(builder.serverConfiguration.multicastConnector.address,
					builder.serverConfiguration.multicastConnector.port, null, builder.packetListeners);
		else
			return new UdpServerThread(new InetSocketAddress(builder.serverConfiguration.listenAddress,
					builder.serverConfiguration.webServiceConnector.port), null, builder.packetListeners);
	}

	private synchronized void start(final Undertow undertow) {
		etcTracker.check();
		// start the server
		undertow.start();
		undertows.add(undertow);
	}

	public synchronized void stopAll() {

		executeListener(shutdownListeners);

		if (udpServer != null)
			udpServer.shutdown();

		for (DeploymentManager manager : deploymentManagers) {
			try {
				if (manager.getState() == DeploymentManager.State.STARTED)
					manager.stop();
				if (manager.getState() == DeploymentManager.State.DEPLOYED)
					manager.undeploy();
			} catch (ServletException e) {
				if (LOGGER.isWarnEnabled())
					LOGGER.warn("Cannot stop the manager: " + e.getMessage(), e);
			}
		}
		undertows.forEach(Undertow::stop);
	}

	private final IdentityManager getIdentityManager(final ServerConfiguration.WebConnector connector)
			throws IOException {
		if (identityManagerProvider == null || connector == null || connector.realm == null)
			return null;
		return identityManagerProvider.getIdentityManager(connector.realm);
	}

	private void startHttpServer(final ServerConfiguration.WebConnector connector, final DeploymentInfo deploymentInfo,
			final Logger accessLogger, final String jmxName)
			throws IOException, ServletException, OperationsException, MBeanException {

		if (deploymentInfo.getIdentityManager() != null)
			deploymentInfo.setLoginConfig(Servlets.loginConfig("BASIC", connector.realm));

		final DeploymentManager manager = Servlets.defaultContainer().addDeployment(deploymentInfo);
		manager.deploy();

		LOGGER.info("Start the connector " + serverConfiguration.listenAddress + ":" + connector.port);

		HttpHandler httpHandler = manager.start();
		final LogMetricsHandler logMetricsHandler =
				new LogMetricsHandler(httpHandler, accessLogger, serverConfiguration.listenAddress, connector.port,
						jmxName);
		deploymentManagers.add(manager);
		httpHandler = logMetricsHandler;

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
	final public void start(boolean shutdownHook)
			throws IOException, ServletException, ReflectiveOperationException, OperationsException, MBeanException {

		java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);

		if (!serverConfiguration.dataDirectory.exists())
			throw new IOException("The data directory does not exists: " + serverConfiguration.dataDirectory);
		if (!serverConfiguration.dataDirectory.isDirectory())
			throw new IOException("The data directory path is not a directory: " + serverConfiguration.dataDirectory);
		LOGGER.info("Data directory sets to: " + serverConfiguration.dataDirectory);

		if (udpServer != null)
			udpServer.checkStarted();

		// Launch the servlet application if any
		if (servletInfos != null && !servletInfos.isEmpty()) {
			final IdentityManager identityManager = getIdentityManager(serverConfiguration.webAppConnector);
			startHttpServer(serverConfiguration.webAppConnector,
					ServletApplication.getDeploymentInfo(servletInfos, identityManager, filterInfos, listenerInfos,
							sessionPersistenceManager, sessionListener), servletAccessLogger, "WEBAPP");
		}

		// Launch the jaxrs application if any
		if (webServices != null && !webServices.isEmpty()) {
			final IdentityManager identityManager = getIdentityManager(serverConfiguration.webServiceConnector);
			startHttpServer(serverConfiguration.webServiceConnector, RestApplication.getDeploymentInfo(identityManager),
					restAccessLogger, "WEBSERVICE");
		}

		if (shutdownHook) {
			Runtime.getRuntime().addShutdownHook(new Thread() {
				@Override
				public void run() {
					stopAll();
				}
			});
		}

		executeListener(startedListeners);
	}

	public Collection<ConnectorStatisticsMXBean> getConnectorsStatistics() {
		return connectorsStatistics;
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
				LOGGER.error(e.getMessage(), e);
			}
		});
	}

	public interface IdentityManagerProvider {

		IdentityManager getIdentityManager(String realm) throws IOException;

	}
}
