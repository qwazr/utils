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
import io.undertow.security.idm.IdentityManager;
import io.undertow.servlet.api.ServletInfo;
import io.undertow.servlet.api.SessionPersistenceManager;

import java.net.DatagramPacket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class ServerBuilder<T extends ServerConfiguration> {

	final ExecutorService executorService;
	final T serverConfiguration;
	final Map<String, Class<? extends ServiceInterface>> webServices;
	final Collection<Consumer<DatagramPacket>> datagramConsumers;
	ServletApplication servletApplication;
	final Collection<ServletInfo> servletInfos;
	SessionPersistenceManager sessionPersistenceManager;
	final Map<String, IdentityManager> identityManagers;
	final Collection<GenericServer.Listener> startedListeners;
	final Collection<GenericServer.Listener> shutdownListeners;

	public ServerBuilder(final T serverConfiguration, final ExecutorService executorService) {
		this.executorService = executorService == null ? Executors.newCachedThreadPool() : executorService;
		this.serverConfiguration = serverConfiguration;
		webServices = new LinkedHashMap<>();
		datagramConsumers = new LinkedHashSet<>();
		servletApplication = null;
		servletInfos = new LinkedHashSet<>();
		sessionPersistenceManager = null;
		identityManagers = new LinkedHashMap<>();
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
		ServiceName serviceName = AnnotationsUtils.getFirstAnnotation(webService, ServiceName.class);
		Objects.requireNonNull(serviceName, "The ServiceName annotation is missing for " + webService);
		webServices.put(serviceName.value(), webService);
	}

	public void registerDatagramConsumer(final Consumer<DatagramPacket> datagramConsumer) {
		this.datagramConsumers.add(datagramConsumer);
	}

	public void setServletApplication(final ServletApplication servletApplication) {
		this.servletApplication = servletApplication;
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

	public void registerIdentityManager(final String realm, final IdentityManager manager) {
		this.identityManagers.put(realm, manager);
	}

	public T getServerConfiguration() {
		return serverConfiguration;
	}

	public ExecutorService getExecutorService() {
		return executorService;
	}
}
