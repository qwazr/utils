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

import com.qwazr.utils.AnnotationsUtils;
import io.undertow.server.session.SessionListener;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.api.SessionPersistenceManager;

import javax.ws.rs.Path;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerBuilder<T extends ServerConfiguration> {

	final ExecutorService executorService;
	final T serverConfiguration;
	final Collection<Class<? extends ServiceInterface>> webServices;
	final Collection<String> webServicePaths;
	final Collection<String> webServiceNames;
	final Collection<UdpServerThread.PacketListener> packetListeners;
	final Collection<ServletInfo> servletInfos;
	SessionPersistenceManager sessionPersistenceManager;
	SessionListener sessionListener;
	GenericServer.IdentityManagerProvider identityManagerProvider;
	final Collection<GenericServer.Listener> startedListeners;
	final Collection<GenericServer.Listener> shutdownListeners;

	public ServerBuilder(final T serverConfiguration, final ExecutorService executorService) {
		this.executorService = executorService == null ? Executors.newCachedThreadPool() : executorService;
		this.serverConfiguration = serverConfiguration;
		webServices = new LinkedHashSet<>();
		webServicePaths = new LinkedHashSet<>();
		webServiceNames = new LinkedHashSet<>();
		packetListeners = new LinkedHashSet<>();
		servletInfos = new LinkedHashSet<>();
		sessionPersistenceManager = null;
		identityManagerProvider = null;
		sessionListener = null;
		startedListeners = new LinkedHashSet<>();
		shutdownListeners = new LinkedHashSet<>();
	}

	public ServerBuilder(final T serverConfiguration) {
		this(serverConfiguration, Executors.newCachedThreadPool());
	}

	public ServerBuilder() {
		this((T) new ServerConfiguration(), Executors.newCachedThreadPool());
	}

	public void registerWebService(final Class<? extends ServiceInterface> webService) {
		final ServiceName serviceName = AnnotationsUtils.getFirstAnnotation(webService, ServiceName.class);
		Objects.requireNonNull(serviceName, "The ServiceName annotation is missing for " + webService);
		webServices.add(webService);
		webServiceNames.add(serviceName.value());
		final Path path = AnnotationsUtils.getFirstAnnotation(webService, Path.class);
		if (path != null && path.value() != null)
			webServicePaths.add(path.value());
	}

	public void registerPacketListener(final UdpServerThread.PacketListener packetListener) {
		this.packetListeners.add(packetListener);
	}

	public void registerServlet(final ServletInfo servlet) {
		this.servletInfos.add(servlet);
	}

	public void registerStartedListener(final GenericServer.Listener listener) {
		this.startedListeners.add(listener);
	}

	public void registerShutdownListener(final GenericServer.Listener listener) {
		this.shutdownListeners.add(listener);
	}

	public void setSessionPersistenceManager(final SessionPersistenceManager manager) {
		this.sessionPersistenceManager = manager;
	}

	public void setIdentityManagerProvider(final GenericServer.IdentityManagerProvider provider) {
		this.identityManagerProvider = provider;
	}

	public void setSessionListenere(final SessionListener sessionListener) {
		this.sessionListener = sessionListener;
	}

	public T getServerConfiguration() {
		return serverConfiguration;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}

	public synchronized GenericServer build() {
		if (GenericServer.INSTANCE != null)
			throw new RuntimeException("The server has already been created (only one server per runtime)");
		GenericServer.INSTANCE = new GenericServer(this);
		return GenericServer.INSTANCE;
	}
}
