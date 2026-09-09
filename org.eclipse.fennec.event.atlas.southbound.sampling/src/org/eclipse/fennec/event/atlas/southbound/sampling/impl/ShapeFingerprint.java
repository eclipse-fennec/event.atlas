/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.event.atlas.southbound.sampling.impl;

import java.io.ByteArrayInputStream;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.XMLConstants;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.fennec.event.atlas.southbound.common.UnknownPayload;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

/**
 * Reduces a payload to its <em>shape</em>: the sorted set of {@code path:type} entries it
 * contains, with array indices collapsed.
 * <p>
 * This is the admission key of a collection window, and choosing it is the one decision in
 * the collector that changes what an inference can conclude:
 * <ul>
 * <li><b>Not the bytes.</b> A hundred structurally identical readings are one sample's worth
 * of evidence. Letting them fill the ring starves it of the rarer shapes that carry the
 * optional fields - the very thing the window exists to capture. Three real captures from one
 * device in the repository's fixtures have identical shapes; keyed on bytes they would occupy
 * three slots.</li>
 * <li><b>But the type is part of the shape.</b> {@code {"temp":0}} and {@code {"temp":21.5}}
 * have the same paths and must <em>not</em> collapse: an integral value alone types the field
 * as an integer, and it takes both samples to widen it to a double. Recording
 * {@code temp:int} against {@code temp:float} keeps both, while the identical captures above
 * still collapse - they agree on paths <em>and</em> types.</li>
 * </ul>
 * <p>
 * Syntax of an entry, by example: {@code object.humidity:float}, {@code readings[].id:int},
 * {@code tags[]:string}, {@code object.rssi:null}, {@code meta:{}} for an empty object and
 * {@code readings[]:[]} for an empty array. XML and XMI payloads yield element paths with
 * {@code @} for attributes and {@code #text} for character content -
 * {@code Sensor/Reading@value} - and carry no type, since an XML document does not state one.
 * <p>
 * A payload that cannot be parsed at all - the {@code PARSE_ERROR} case the hook also offers -
 * yields {@link #UNPARSEABLE}, so every such payload of a channel collapses into a single
 * slot instead of filling the ring with garbage.
 * @author Ilenia Salvadori
 * @since 27.08.2026
 */
final class ShapeFingerprint {

	/** The shape of a payload no parser could read. */
	static final List<String> UNPARSEABLE = List.of("<unparseable>");

	private static final Logger logger = Logger.getLogger(ShapeFingerprint.class.getName());

	/** Jackson factories are thread-safe and meant to be shared. */
	private static final JsonFactory JSON = new JsonFactory();

	/**
	 * Namespace-aware and hardened against entity expansion: the payload comes off a broker
	 * topic, so it is untrusted input being parsed only to be measured.
	 */
	private static final SAXParserFactory SAX = createSaxParserFactory();

	private ShapeFingerprint() {
		// static use only
	}

	/**
	 * @param payload the payload to measure. Parameter must not be <code>null</code>
	 * @return the payload's shape, sorted and de-duplicated, or {@link #UNPARSEABLE}. Never
	 * <code>null</code>, never empty
	 */
	static List<String> of(UnknownPayload payload) {
		try {
			if (PayloadIngest.FORMAT_JSON.equals(payload.format())) {
				return jsonShape(payload.payload());
			}
			return xmlShape(payload.payload());
		} catch (Exception e) {
			// Expected for the PARSE_ERROR outcome, and possible for the others: EMF may have
			// failed on a document a plain parser still reads, and the reverse.
			logger.log(Level.FINE, () -> String.format("Cannot determine the shape of the %s payload from '%s': %s",
					payload.format(), payload.source(), e.getMessage()));
			return UNPARSEABLE;
		}
	}

	/**
	 * Walks the JSON with the streaming parser rather than building a tree: nothing here needs
	 * the values, only which paths carry which kind of value.
	 */
	private static List<String> jsonShape(byte[] payload) throws Exception {
		TreeSet<String> paths = new TreeSet<>();
		Deque<Frame> open = new ArrayDeque<>();
		String pendingName = null;
		try (JsonParser parser = JSON.createParser(ObjectReadContext.empty(), payload)) {
			for (JsonToken token = parser.nextToken(); token != null; token = parser.nextToken()) {
				switch (token) {
				case PROPERTY_NAME -> pendingName = parser.currentName();
				case START_OBJECT, START_ARRAY -> {
					open.addLast(new Frame(segment(pendingName, token == JsonToken.START_ARRAY)));
					pendingName = null;
				}
				case END_OBJECT, END_ARRAY -> {
					Frame closing = open.pollLast();
					if (closing != null && !closing.contributed) {
						// an empty container is itself the only evidence that this path exists
						paths.add(path(open, closing.segment) + ":" + (token == JsonToken.END_ARRAY ? "[]" : "{}"));
					}
					contributed(open);
				}
				default -> {
					paths.add(path(open, segment(pendingName, false)) + ":" + type(token));
					contributed(open);
					pendingName = null;
				}
				}
			}
		}
		return paths.isEmpty() ? UNPARSEABLE : List.copyOf(paths);
	}

	/**
	 * One open object or array while the parser walks the document, and whether anything inside
	 * it has been recorded yet - which is how an empty container is told from a populated one.
	 */
	private static final class Frame {

		private final String segment;
		private boolean contributed;

		Frame(String segment) {
			this.segment = segment;
		}
	}

	/** Marks the innermost open container as non-empty. */
	private static void contributed(Deque<Frame> open) {
		Frame parent = open.peekLast();
		if (parent != null) {
			parent.contributed = true;
		}
	}

	/**
	 * One path segment. An array's elements are not distinguished from each other - a reading
	 * at index 0 and one at index 7 are the same field of the same model - so an array
	 * contributes {@code []} once and its elements hang below that.
	 */
	private static String segment(String name, boolean array) {
		String segment = name == null ? "" : name;
		return array ? segment + "[]" : segment;
	}

	/**
	 * Joins the open containers and the leaf into a dotted path, skipping the anonymous
	 * segments an array's elements and the document root contribute.
	 */
	private static String path(Deque<Frame> open, String leaf) {
		StringBuilder path = new StringBuilder();
		for (Frame frame : open) {
			append(path, frame.segment);
		}
		append(path, leaf);
		// a scalar document has neither a container nor a property name to be named after
		return path.isEmpty() ? "#root" : path.toString();
	}

	private static void append(StringBuilder path, String segment) {
		if (!segment.isEmpty()) {
			path.append(path.isEmpty() ? "" : ".").append(segment);
		}
	}

	/**
	 * The type tag of a scalar. {@code int} and {@code float} are kept apart on purpose - that
	 * distinction is the whole reason a set of samples can widen a field's type where one
	 * payload cannot.
	 */
	private static String type(JsonToken token) {
		return switch (token) {
		case VALUE_NUMBER_INT -> "int";
		case VALUE_NUMBER_FLOAT -> "float";
		case VALUE_STRING -> "string";
		case VALUE_TRUE, VALUE_FALSE -> "bool";
		case VALUE_NULL -> "null";
		default -> token.name().toLowerCase(Locale.ROOT);
		};
	}

	/**
	 * The XML equivalent: element paths, attributes as {@code @name}, character content as
	 * {@code #text}. No type tag - an XML document does not carry one, and for an XMI payload
	 * the types live in the model that this runtime is missing.
	 */
	private static List<String> xmlShape(byte[] payload) throws Exception {
		TreeSet<String> paths = new TreeSet<>();
		SAXParser parser;
		synchronized (SAX) {
			// SAXParserFactory is not documented to be thread-safe, the parsers it makes are
			// single-use anyway
			parser = SAX.newSAXParser();
		}
		parser.parse(new InputSource(new ByteArrayInputStream(payload)), new DefaultHandler() {

			private final Deque<String> elements = new ArrayDeque<>();

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) {
				elements.addLast(localName == null || localName.isBlank() ? qName : localName);
				String path = String.join("/", elements);
				paths.add(path);
				for (int i = 0; i < attributes.getLength(); i++) {
					String name = attributes.getLocalName(i);
					paths.add(path + "@" + (name == null || name.isBlank() ? attributes.getQName(i) : name));
				}
			}

			@Override
			public void characters(char[] ch, int start, int length) {
				if (!new String(ch, start, length).isBlank()) {
					paths.add(String.join("/", elements) + "#text");
				}
			}

			@Override
			public void endElement(String uri, String localName, String qName) {
				elements.pollLast();
			}
		});
		return paths.isEmpty() ? UNPARSEABLE : List.copyOf(paths);
	}

	private static SAXParserFactory createSaxParserFactory() {
		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setNamespaceAware(true);
		try {
			factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
		} catch (Exception e) {
			// a parser that does not know these features is still usable for measuring shapes
			logger.log(Level.FINE, "Could not harden the XML parser used for payload shapes", e);
		}
		return factory;
	}

}
