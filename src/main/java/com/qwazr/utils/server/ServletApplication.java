/**
 * s * Copyright 2014-2016 Emmanuel Keller / QWAZR
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

import io.undertow.server.session.SessionListener;
import io.undertow.servlet.Servlets;
import io.undertow.servlet.api.*;

import javax.servlet.DispatcherType;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

class ServletApplication {

	final static DeploymentInfo getDeploymentInfo(final Collection<ServletInfo> servletInfos,
			final Collection<String> securityContraints,
			final Map<String, FilterInfo> filterInfos, final Collection<ListenerInfo> listenersInfos,
			final SessionPersistenceManager sessionPersistenceManager, final SessionListener sessionListener) {

		final DeploymentInfo deploymentInfo =
				Servlets.deployment().setClassLoader(Thread.currentThread().getContextClassLoader()).setContextPath("/")
						.setDefaultEncoding("UTF-8").setDeploymentName(ServletApplication.class.getName());

		if (sessionPersistenceManager != null)
			deploymentInfo.setSessionPersistenceManager(sessionPersistenceManager);
		if (servletInfos != null)
			deploymentInfo.addServlets(servletInfos);
		if (filterInfos != null) {
			filterInfos.forEach((path, filterInfo) -> {
				deploymentInfo.addFilter(filterInfo);
				deploymentInfo.addFilterUrlMapping(filterInfo.getName(), path, DispatcherType.REQUEST);
			});
		}
		if (securityContraints != null && !securityContraints.isEmpty()) {
			final SecurityConstraint securityConstraint = Servlets.securityConstraint()
					.addWebResourceCollection(Servlets.webResourceCollection().addUrlPatterns(securityContraints));
			deploymentInfo.addSecurityConstraint(
					securityConstraint.setEmptyRoleSemantic(SecurityInfo.EmptyRoleSemantic.AUTHENTICATE));
		}
		if (listenersInfos != null)
			deploymentInfo.addListeners(listenersInfos);
		if (sessionListener != null)
			deploymentInfo.addSessionListener(sessionListener);
		return deploymentInfo;
	}

}
