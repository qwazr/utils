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
 */
package com.qwazr.utils;

import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.namespace.QName;
import javax.xml.xpath.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

public class XPathParser {

	private final static XPathFactory xPathfactory = XPathFactory.newInstance();

	private final XPath xPath;
	private final ConcurrentHashMap<String, XPathExpression> xPathExpressions;

	public XPathParser() {
		this.xPath = getXPath();
		this.xPathExpressions = new ConcurrentHashMap<>();
	}

	final public static XPath getXPath() {
		synchronized (xPathfactory) {
			return xPathfactory.newXPath();
		}
	}

	private XPathExpression getExpression(final String query) {
		return xPathExpressions.computeIfAbsent(query, s -> {
			try {
				return xPath.compile(query);
			} catch (XPathExpressionException e) {
				throw new IllegalArgumentException(e);
			}
		});
	}

	final public String evaluateString(final Node rootNode, final String query) throws XPathExpressionException {
		return getExpression(query).evaluate(rootNode);
	}

	final public Number evaluateNumber(final Node rootNode, final String query) throws XPathExpressionException {
		return (Number) getExpression(query).evaluate(rootNode, XPathConstants.NUMBER);
	}

	final public Boolean evaluateBoolean(final Node rootNode, final String query) throws XPathExpressionException {
		return (Boolean) getExpression(query).evaluate(rootNode, XPathConstants.BOOLEAN);
	}

	final public Node evaluateNode(final Node rootNode, final String query) throws XPathExpressionException {
		return (Node) getExpression(query).evaluate(rootNode, XPathConstants.NODE);
	}

	final public NodeIterator evaluateNodes(final Node rootNode, final String query) throws XPathExpressionException {
		return new NodeIterator((NodeList) getExpression(query).evaluate(rootNode, XPathConstants.NODESET));
	}

	private static final List<Pair<QName, BiConsumer<Object, Consumer>>> consumers = new ArrayList<>();

	static {
		consumers.add(Pair.of(XPathConstants.NODESET, (object, consumer) -> {
			if (object != null && consumer != null) {
				final NodeIterator nodeIterator = new NodeIterator((NodeList) object);
				while (nodeIterator.hasNext())
					consumer.accept(nodeIterator.next());
			}
		}));
		consumers.add(Pair.of(XPathConstants.NODE, (object, consumer) -> {
			if (object != null && consumer != null)
				consumer.accept((Node) object);
		}));
		consumers.add(Pair.of(XPathConstants.STRING, (object, consumer) -> {
			if (object != null && consumer != null)
				consumer.accept((String) object);
		}));
		consumers.add(Pair.of(XPathConstants.NUMBER, (object, consumer) -> {
			if (object != null && consumer != null)
				consumer.accept((Number) object);
		}));
		consumers.add(Pair.of(XPathConstants.BOOLEAN, (object, consumer) -> {
			if (object != null && consumer != null)
				consumer.accept((Boolean) object);
		}));
	}

	final public void evaluate(final Node rootNode, final String query, final Consumer consumer)
			throws XPathExpressionException {
		XPathExpressionException lastError = null;
		final XPathExpression expression = getExpression(query);
		for (Pair<QName, BiConsumer<Object, Consumer>> pair : consumers) {
			try {
				final Object result = expression.evaluate(rootNode, pair.getLeft());
				if (result != null) {
					pair.getRight().accept(result, consumer);
					return;
				}
			} catch (XPathExpressionException e) {
				lastError = e;
			}
		}
		if (lastError != null)
			throw lastError;
	}

	public interface Consumer {

		default void accept(Node object) {
		}

		default void accept(Boolean object) {
		}

		default void accept(String object) {
		}

		default void accept(Number object) {
		}
	}

	public static class NodeIterator implements Iterator<Node> {

		private final NodeList nodeList;
		private final int length;
		private int pos;

		public NodeIterator(final NodeList nodeList) {
			this.nodeList = nodeList;
			length = nodeList == null ? 0 : nodeList.getLength();
			pos = 0;
		}

		@Override
		public boolean hasNext() {
			return pos < length;
		}

		@Override
		public Node next() {
			return nodeList.item(pos++);
		}
	}

	public static Node getAttribute(final Node node, final String attributeName) {
		final NamedNodeMap attributes = node.getAttributes();
		return attributes == null ? null : attributes.getNamedItem(attributeName);
	}

	public static String getAttributeString(final Node node, final String attributeName) {
		final Node attrNode = getAttribute(node, attributeName);
		return attrNode == null ? null : attrNode.getTextContent();
	}
}
