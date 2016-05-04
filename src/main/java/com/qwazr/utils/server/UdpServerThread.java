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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * Created by ekeller on 04/05/2016.
 */
public class UdpServerThread extends Thread {

	private static final Logger logger = LoggerFactory.getLogger(UdpServerThread.class);

	private final static int DEFAULT_BUFFER_SIZE = 64;
	private final static String DEFAULT_MULTICAST = "224.0.0.10";
	private final static int DEFAULT_PORT = 9092;

	private final String multicastAddress;
	private final int port;
	private final int dataBufferSize;
	private final Collection<Consumer<DatagramPacket>> datagramConsumers;

	private volatile Collection<Consumer<DatagramPacket>> datagramConsumersCache;
	private volatile InetAddress address;


	public UdpServerThread(Integer port, String multicastAddress, Integer dataBufferSize) {
		super();
		setName("UDP Server");
		setDaemon(true);
		this.datagramConsumers = new HashSet<>();
		this.dataBufferSize = dataBufferSize == null ? DEFAULT_BUFFER_SIZE : dataBufferSize;
		this.port = port == null ? DEFAULT_PORT : port;
		this.multicastAddress = multicastAddress == null ? DEFAULT_MULTICAST : multicastAddress;
		this.address = null;
		register(null); // To create an initial empty array
	}

	public void register(Consumer<DatagramPacket> datagramConsumer) {
		synchronized (datagramConsumers) {
			if (datagramConsumer != null)
				datagramConsumers.add(datagramConsumer);
			datagramConsumersCache = new ArrayList<>(datagramConsumers);
		}
	}

	public void send(final byte[] data, final int bufferSize) throws IOException {
		Objects.requireNonNull(data, "The data is null");
		if (bufferSize == 0 || bufferSize > data.length)
			throw new IOException("Buffer overflow: " + bufferSize + "/" + data.length);
		try (final DatagramSocket clientSocket = new DatagramSocket()) {
			final DatagramPacket datagramPacket =
					new DatagramPacket(data, bufferSize, address, port);
			clientSocket.send(datagramPacket);
		}
	}

	public void send(final byte[] data) throws IOException {
		send(data, data.length);
	}

	@Override
	public void run() {
		try (final MulticastSocket socket = new MulticastSocket(this.port)) {
			socket.joinGroup(address);
			for (; ; ) {
				final byte[] dataBuffer = new byte[dataBufferSize];
				final DatagramPacket datagramPacket = new DatagramPacket(dataBuffer, dataBuffer.length);
				socket.receive(datagramPacket);
				datagramConsumersCache.forEach(datagramConsumer -> {
					try {
						datagramConsumer.accept(datagramPacket);
					} catch (Exception e) {
						logger.warn(e.getMessage(), e);
					}
				});
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Start or restart the thread if it is stopped
	 */
	public synchronized void checkStarted() throws UnknownHostException {
		if (isAlive())
			return;
		this.address = InetAddress.getByName(multicastAddress);
		this.start();
	}
}
