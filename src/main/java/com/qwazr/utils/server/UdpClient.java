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

import com.qwazr.utils.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class UdpClient {

	private final Map<InetAddress, Integer> serverList;
	private final int dataBufferSize;

	UdpClient(final int dataBufferSize, final InetAddress address, final int defaultPort, final String[] remotes)
			throws UnknownHostException {
		this.dataBufferSize = dataBufferSize;
		serverList = new HashMap<>();
		serverList.put(address, defaultPort);
		if (remotes != null) {
			for (String remote : remotes) {
				String[] split = StringUtils.split(remote, ':');
				switch (split.length) {
				case 1:
					serverList.put(InetAddress.getByName(split[0]), defaultPort);
					break;
				case 2:
					serverList.put(InetAddress.getByName(split[0]), Integer.parseInt(split[1]));
					break;
				}
			}
		}
	}

	private void send(final InetAddress address, int port, final byte[] data, final int bufferSize) throws IOException {
		Objects.requireNonNull(data, "The data is null");
		if (bufferSize == 0 || bufferSize > data.length)
			throw new IOException("Buffer overflow: " + bufferSize + "/" + data.length);
		try (final DatagramSocket clientSocket = new DatagramSocket()) {
			final DatagramPacket datagramPacket = new DatagramPacket(data, bufferSize, address, port);
			clientSocket.send(datagramPacket);
		}
	}

	private void send(final InetAddress address, final int port, final byte[] data) throws IOException {
		send(address, port, data, data.length);
	}

	public void send(final Externalizable object) throws IOException {
		try (final ByteArrayOutputStream bos = new ByteArrayOutputStream(dataBufferSize)) {
			try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
				object.writeExternal(oos);
				oos.flush();
				serverList.forEach((address, port) -> {
					try {
						send(address, port, bos.toByteArray());
					} catch (IOException e) {
						throw new RuntimeException(e);
					}
				});
			}
		}
	}

}
