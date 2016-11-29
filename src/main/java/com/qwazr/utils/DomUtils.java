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

import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.Iterator;

public class DomUtils {

	public static Node getAttribute(final Node node, final String attributeName) {
		final NamedNodeMap attributes = node.getAttributes();
		return attributes == null ? null : attributes.getNamedItem(attributeName);
	}

	public static String getAttributeString(final Node node, final String attributeName) {
		final Node attrNode = getAttribute(node, attributeName);
		return attrNode == null ? null : attrNode.getTextContent();
	}

	final private static void extractText(final Node parent, final StringBuilder sb) {
		switch (parent.getNodeType()) {
		case Node.TEXT_NODE:
		case Node.CDATA_SECTION_NODE:
			sb.append(parent.getNodeValue());
			break;
		}
		final NodeList childrens = parent.getChildNodes();
		int l = childrens.getLength();
		for (int i = 0; i < l; i++)
			extractText(childrens.item(i), sb);
	}

	final public static String getText(final Node node) {
		final StringBuilder sb = new StringBuilder();
		extractText(node, sb);
		return sb.toString();
	}

	public static Looper iterator(final NodeList nodeList) {
		return new Looper(nodeList);
	}

	public static class Looper implements Iterator<Node>, Iterable<Node> {

		private final NodeList nodeList;
		private final int length;
		private int pos;

		public Looper(final NodeList nodeList) {
			this.nodeList = nodeList;
			length = nodeList == null ? 0 : nodeList.getLength();
			pos = 0;
		}

		private Looper(Looper it) {
			this.nodeList = it.nodeList;
			this.length = it.length;
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

		@Override
		public Iterator<Node> iterator() {
			return pos == 0 ? this : new Looper(this);
		}
	}

}
