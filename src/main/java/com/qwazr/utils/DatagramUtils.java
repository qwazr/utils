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
package com.qwazr.utils;

import java.io.Externalizable;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Objects;

public class DatagramUtils {

	/**
	 * Send a byte array to the given address using UDP (datagram) transport.
	 *
	 * @param data      the byte array to send
	 * @param offset
	 * @param length
	 * @param addresses
	 * @throws IOException
	 */
	public static void send(final byte[] data, final int offset, final int length, final InetSocketAddress... addresses)
			throws IOException {
		Objects.requireNonNull(data, "The data is null");
		Objects.requireNonNull(addresses, "No recipients: the addresses is null");
		try (final DatagramSocket clientSocket = new DatagramSocket()) {
			for (InetSocketAddress address : addresses)
				clientSocket.send(new DatagramPacket(data, offset, length, address));
		}
	}

	/**
	 * Send a byte array to the given address using UDP (datagram) transport.
	 *
	 * @param data      the byte array to send
	 * @param offset
	 * @param length
	 * @param addresses
	 * @throws IOException
	 */
	public static void send(final byte[] data, final int offset, final int length,
			final Collection<InetSocketAddress> addresses, final InetSocketAddress... exclusions)
			throws IOException {
		Objects.requireNonNull(data, "Nothing to send: the data is null");
		Objects.requireNonNull(addresses, "No recipients: the addresses is null");
		try (final DatagramSocket clientSocket = new DatagramSocket()) {
			for (InetSocketAddress address : addresses) {
				if (exclusions != null && ArrayUtils.contains(exclusions, address))
					continue;
				clientSocket.send(new DatagramPacket(data, offset, length, address));
			}
		}
	}

	/**
	 * Send a byte array to the given address using UDP (datagram) transport.
	 *
	 * @param data      the byte array to send
	 * @param addresses the recipients
	 * @throws IOException
	 */
	public static void send(final byte[] data, final InetSocketAddress... addresses)
			throws IOException {
		Objects.requireNonNull(data, "Nothing to send: the data is null");
		send(data, 0, data.length, addresses);
	}

	/**
	 * Send a byte array to the given address using UDP (datagram) transport.
	 *
	 * @param data      the byte array to send
	 * @param addresses the recipients
	 * @throws IOException
	 */
	public static void send(final byte[] data, final Collection<InetSocketAddress> addresses,
			final InetSocketAddress... exclusions)
			throws IOException {
		Objects.requireNonNull(data, "Nothing to send: the data is null");
		send(data, 0, data.length, addresses, exclusions);
	}

	/**
	 * Send a serializable object to the given address using UDP (datagram) transport
	 *
	 * @param object     the object to send
	 * @param bufferSize the initial size of the buffer (for byte array serialisation)
	 * @param addresses  the recipients
	 * @throws IOException
	 */
	public static void send(final Externalizable object, final int bufferSize,
			final Collection<InetSocketAddress> addresses, InetSocketAddress... exclusions)
			throws IOException {
		Objects.requireNonNull(object, "Nothing to send: the object is null.");
		send(SerializationUtils.getBytes(object, bufferSize), addresses, exclusions);
	}

	/**
	 * Send a serializable object to the given address using UDP (datagram) transport
	 *
	 * @param object     the object to send
	 * @param bufferSize the initial size of the buffer (for byte array serialisation)
	 * @param addresses  the recipients
	 * @throws IOException
	 */
	public static void send(final Externalizable object, final int bufferSize,
			final InetSocketAddress... addresses)
			throws IOException {
		Objects.requireNonNull(object, "Nothing to send: the object is null.");
		send(SerializationUtils.getBytes(object, bufferSize), addresses);
	}

	/**
	 * Build an InetSocketAddress from the form {host}:{port}.
	 * If only the {host} is given, the provided defaultPort is used
	 *
	 * @param address     a formatted address: {host}:{port}
	 * @param defaultPort the default port to use
	 * @return
	 */
	public static InetSocketAddress buildInetSocketAddress(final String address, final int defaultPort) {
		Objects.requireNonNull(address, "The address is null");
		String[] parts = StringUtils.split(address, ':');
		final int port = parts.length > 1 ? Integer.parseInt(parts[1]) : defaultPort;
		return new InetSocketAddress(parts[0], port);
	}
}
