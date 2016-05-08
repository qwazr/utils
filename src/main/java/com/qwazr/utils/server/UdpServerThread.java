/**
 * s * Copyright 2016 Emmanuel Keller / QWAZR
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.*;
import java.util.Collection;
import java.util.function.Consumer;

/**
 * Created by ekeller on 04/05/2016.
 */
public class UdpServerThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(UdpServerThread.class);

	public final static int DEFAULT_BUFFER_SIZE = 65536;
	public final static String DEFAULT_MULTICAST = "239.255.90.91";

	private final int dataBufferSize;
	private final PacketListener[] packetListeners;

	private volatile Collection<Consumer<DatagramPacket>> datagramConsumersCache;
	private final InetSocketAddress socketAddress;
	private final InetAddress multicastAddress;

	public UdpServerThread(final InetSocketAddress socketAddress, final InetAddress multicastAddress,
			Integer dataBufferSize, Collection<PacketListener> packetListeners) {
		super();
		setName("UDP Server");
		setDaemon(true);
		this.dataBufferSize = dataBufferSize == null ? DEFAULT_BUFFER_SIZE : dataBufferSize;
		this.socketAddress = socketAddress;
		this.multicastAddress = multicastAddress;
		this.packetListeners = packetListeners.toArray(new PacketListener[packetListeners.size()]);
	}

	@Override
	public void run() {
		try (final DatagramSocket socket = multicastAddress != null ?
				new MulticastSocket(socketAddress) :
				new DatagramSocket(socketAddress)) {
			if (socket instanceof MulticastSocket)
				((MulticastSocket) socket).joinGroup(multicastAddress);
			if (logger.isInfoEnabled())
				logger.info("UDP Server started: " + socketAddress);
			for (; ; ) {
				final byte[] dataBuffer = new byte[dataBufferSize];
				final DatagramPacket datagramPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
				socket.receive(datagramPacket);
				for (PacketListener packetListener : packetListeners) {
					try {
						packetListener.acceptPacket(datagramPacket);
					} catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				}
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			if (logger.isInfoEnabled())
				logger.info("UDP Server exit: " + socketAddress);
		}
	}

	/**
	 * Start or restart the thread if it is stopped
	 */
	public synchronized void checkStarted() throws UnknownHostException {
		if (isAlive())
			return;
		this.start();
	}

	public void shutdown() {
		if (isInterrupted())
			return;
		this.interrupt();
	}

	public interface PacketListener {

		void acceptPacket(final DatagramPacket packet) throws Exception;
	}
}
