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

import com.qwazr.utils.IOUtils;
import com.qwazr.utils.SerializationUtils;
import io.undertow.servlet.api.SessionPersistenceManager;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class InFileSessionPersistenceManager implements SessionPersistenceManager {

	private static final Logger logger = LoggerFactory.getLogger(InFileSessionPersistenceManager.class);

	private final File sessionDir;

	public InFileSessionPersistenceManager(File sessionDir) {
		this.sessionDir = sessionDir;
	}

	@Override
	public void persistSessions(String deploymentName, Map<String, PersistentSession> sessionData) {
		if (sessionData == null)
			return;
		final File deploymentDir = new File(sessionDir, deploymentName);
		if (!deploymentDir.exists())
			deploymentDir.mkdir();
		if (!deploymentDir.exists() && !deploymentDir.isDirectory()) {
			if (logger.isErrorEnabled())
				logger.error("Cannot create the session directory " + deploymentDir + ": persistence aborted.");
			return;
		}
		sessionData.forEach(
						(sessionId, persistentSession) -> writeSession(deploymentDir, sessionId, persistentSession));
	}

	private void writeSession(File deploymentDir, String sessionId, PersistentSession persistentSession) {
		final Date expDate = persistentSession.getExpiration();
		if (expDate == null)
			return; // No expiry date? no serialization
		final Map<String, Object> sessionData = persistentSession.getSessionData();
		if (sessionData == null || sessionData.isEmpty())
			return; // No attribute? no serialization
		File sessionFile = new File(deploymentDir, sessionId);
		try {
			final FileOutputStream fileOutputStream = new FileOutputStream(sessionFile);
			try {
				final ObjectOutputStream out = new ObjectOutputStream(fileOutputStream);
				try {
					out.writeLong(expDate.getTime()); // The date is stored as long
					sessionData.forEach((attribute, object) -> writeSessionAttribute(out, attribute, object));
				} finally {
					IOUtils.close(out);
				}
			} finally {
				IOUtils.close(fileOutputStream);
			}
		} catch (IOException e) {
			if (logger.isWarnEnabled())
				logger.warn("Cannot save sessions in " + sessionFile + " " + e.getMessage(), e);
		}
	}

	private void writeSessionAttribute(ObjectOutputStream out, String attribute, Object object) {
		if (attribute == null || object == null)
			return;
		try {
			out.writeUTF(attribute); // Attribute name stored as string
		} catch (IOException e) {
			if (logger.isErrorEnabled())
				logger.error("Cannot write session attribute " + attribute + ": persistence aborted.");
			return; // The attribute cannot be written, we abort
		}
		try {
			out.writeObject(object);
			return; // The object was written, job done, we can exit
		} catch (IOException e) {
			if (logger.isWarnEnabled())
				logger.warn("Cannot write session object " + object);
			try {
				out.writeObject(SerializationUtils.NullEmptyObject.INSTANCE);
			} catch (IOException e1) {
				if (logger.isErrorEnabled())
					logger.error("Cannot write NULL session object for attribute " + attribute
									+ ": persistence aborted.");
			}
		}

	}

	@Override
	public Map<String, PersistentSession> loadSessionAttributes(String deploymentName, final ClassLoader classLoader) {
		final File deploymentDir = new File(sessionDir, deploymentName);
		if (!deploymentDir.exists() || !deploymentDir.isDirectory())
			return null;
		File[] sessionFiles = deploymentDir.listFiles((FileFilter) FileFileFilter.FILE);
		if (sessionFiles == null || sessionFiles.length == 0)
			return null;
		long time = System.currentTimeMillis();
		Map<String, PersistentSession> finalMap = new HashMap<String, PersistentSession>();
		for (File sessionFile : sessionFiles) {
			PersistentSession persistentSession = readSession(sessionFile);
			if (persistentSession != null && persistentSession.getExpiration().getTime() > time)
				finalMap.put(sessionFile.getName(), persistentSession);
			sessionFile.delete();
		}
		return finalMap.isEmpty() ? null : finalMap;
	}

	private PersistentSession readSession(File sessionFile) {
		try {
			final FileInputStream fileInputStream = new FileInputStream(sessionFile);
			try {
				final ObjectInputStream in = new ObjectInputStream(fileInputStream);
				try {
					final Date expDate = new Date(in.readLong());
					final HashMap<String, Object> sessionData = new HashMap<>();
					try {
						for (; ; )
							readSessionAttribute(in, sessionData);
					} catch (EOFException e) {
						;// Ok we reached the end of the file
					}
					return sessionData.isEmpty() ? null : new PersistentSession(expDate, sessionData);
				} finally {
					IOUtils.close(in);
				}
			} finally {
				IOUtils.close(fileInputStream);
			}
		} catch (IOException e) {
			if (logger.isWarnEnabled())
				logger.warn("Cannot load sessions from " + sessionFile + " " + e.getMessage(), e);
			return null;
		}
	}

	private void readSessionAttribute(ObjectInputStream in, Map<String, Object> sessionData) throws IOException {
		final String attribute = in.readUTF();
		try {
			Object object = in.readObject();
			if (!(object instanceof SerializationUtils.NullEmptyObject))
				sessionData.put(attribute, object);
		} catch (ClassNotFoundException e) {
			if (logger.isWarnEnabled())
				logger.warn("The attribute " + attribute + " cannot be deserialized: " + e.getMessage(), e);
		}
	}

	@Override
	public void clear(String deploymentName) {

	}
}
