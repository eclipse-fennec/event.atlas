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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;

import org.eclipse.fennec.event.atlas.southbound.common.IngestResult.Outcome;
import org.eclipse.fennec.event.atlas.southbound.common.PayloadIngest;
import org.eclipse.fennec.event.atlas.southbound.common.UnknownPayload;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ShapeFingerprint} - the admission key of a collection window, and
 * therefore what decides whether a payload counts as new evidence or as another copy of one
 * the window already has.
 * @author Ilenia Salvadori
 */
public class ShapeFingerprintTest {

	@Test
	@DisplayName("A JSON payload yields one path:type entry per leaf")
	void json_yieldsPathAndTypePerLeaf() {
		List<String> shape = shapeOf(PayloadIngest.FORMAT_JSON,
				"{\"deviceId\":\"a1\",\"object\":{\"temperature\":21.5,\"humidity\":40}}");

		assertEquals(List.of("deviceId:string", "object.humidity:int", "object.temperature:float"), shape);
	}

	@Test
	@DisplayName("Two payloads with the same paths and types have the same shape")
	// This is the case the collector exists to collapse: three real captures from one device in
	// the repository's fixtures have identical shapes, and keyed on bytes they would take three
	// of the ring's slots and starve it of the rarer shapes.
	void json_sameStructureDifferentValues_isOneShape() {
		List<String> first = shapeOf(PayloadIngest.FORMAT_JSON, "{\"temp\":21.5,\"id\":\"a\"}");
		List<String> second = shapeOf(PayloadIngest.FORMAT_JSON, "{\"temp\":19.25,\"id\":\"b\"}");

		assertEquals(first, second);
	}

	@Test
	@DisplayName("An integral and a fractional value of the same field are different shapes")
	// The type is part of the shape on purpose. "temp": 0 alone types the field as an integer;
	// it takes a fractional sample to widen it to a double, so the two must not collapse into
	// one slot or that evidence never reaches the inference.
	void json_intAndFloat_areDifferentShapes() {
		List<String> integral = shapeOf(PayloadIngest.FORMAT_JSON, "{\"temp\":0}");
		List<String> fractional = shapeOf(PayloadIngest.FORMAT_JSON, "{\"temp\":21.5}");

		assertEquals(List.of("temp:int"), integral);
		assertEquals(List.of("temp:float"), fractional);
		assertNotEquals(integral, fractional);
	}

	@Test
	@DisplayName("A missing field makes a different shape, which is what optionality is read from")
	void json_absentField_isADifferentShape() {
		assertNotEquals(shapeOf(PayloadIngest.FORMAT_JSON, "{\"temp\":1,\"rssi\":-70}"),
				shapeOf(PayloadIngest.FORMAT_JSON, "{\"temp\":1}"));
	}

	@Test
	@DisplayName("Array indices are collapsed, but the element types are all kept")
	void json_arrays_collapseIndicesAndKeepElementTypes() {
		assertEquals(List.of("readings[]:int"), shapeOf(PayloadIngest.FORMAT_JSON, "{\"readings\":[1,2,3]}"));
		// two elements typing the same field differently is evidence, not noise
		assertEquals(List.of("readings[].v:float", "readings[].v:int"),
				shapeOf(PayloadIngest.FORMAT_JSON, "{\"readings\":[{\"v\":1},{\"v\":2.5}]}"));
	}

	@Test
	@DisplayName("Empty containers and nulls are recorded rather than dropped")
	void json_emptyContainersAndNulls_areRecorded() {
		assertEquals(List.of("meta:{}", "tags[]:[]", "rssi:null").stream().sorted().toList(),
				shapeOf(PayloadIngest.FORMAT_JSON, "{\"tags\":[],\"meta\":{},\"rssi\":null}"));
	}

	@Test
	@DisplayName("A scalar or an array at the document root still has a shape")
	void json_rootLevelValues_areNamed() {
		assertEquals(List.of("#root:int"), shapeOf(PayloadIngest.FORMAT_JSON, "42"));
		assertEquals(List.of("[].a:int"), shapeOf(PayloadIngest.FORMAT_JSON, "[{\"a\":1},{\"a\":2}]"));
	}

	@Test
	@DisplayName("An XMI payload yields element paths and attributes, without types")
	// An XML document does not state its types; for an XMI payload they live in the very model
	// this runtime is missing.
	void xmi_yieldsElementPathsAndAttributes() {
		List<String> shape = shapeOf(PayloadIngest.FORMAT_XMI, "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<t:Station xmlns:t=\"http://example.org/nowhere/1.0\" name=\"roof\">"
				+ "<reading value=\"21.5\"/></t:Station>");

		assertEquals(List.of("Station", "Station/reading", "Station/reading@value", "Station@name"), shape);
	}

	@Test
	@DisplayName("A payload no parser can read collapses into a single shape")
	// Otherwise a channel emitting garbage would fill the ring with it, one slot per message.
	void unreadablePayload_isOneShapeForAllOfThem() {
		assertEquals(ShapeFingerprint.UNPARSEABLE, shapeOf(PayloadIngest.FORMAT_JSON, "this is not json"));
		assertEquals(ShapeFingerprint.UNPARSEABLE, shapeOf(PayloadIngest.FORMAT_XMI, "}{ neither is this"));
		assertEquals(shapeOf(PayloadIngest.FORMAT_JSON, "garbage"), shapeOf(PayloadIngest.FORMAT_JSON, "other rubbish"),
				"Every unreadable payload of a channel must occupy the same slot");
	}

	@Test
	@DisplayName("A shape is sorted, so two payloads that differ only in key order agree")
	void shape_isOrderIndependent() {
		List<String> shape = shapeOf(PayloadIngest.FORMAT_JSON, "{\"b\":1,\"a\":2}");

		assertEquals(shapeOf(PayloadIngest.FORMAT_JSON, "{\"a\":2,\"b\":1}"), shape);
		assertTrue(shape.equals(shape.stream().sorted().toList()), "The entries must be sorted");
	}

	private static List<String> shapeOf(String format, String body) {
		return ShapeFingerprint.of(new UnknownPayload(body.getBytes(StandardCharsets.UTF_8), format, "sensors/test",
				null, Outcome.EMPTY, Instant.now()));
	}

}
