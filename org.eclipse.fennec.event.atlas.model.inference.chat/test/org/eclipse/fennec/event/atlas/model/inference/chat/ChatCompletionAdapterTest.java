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
package org.eclipse.fennec.event.atlas.model.inference.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.ai.chat.completion.api.ChatCompletionService;
import org.eclipse.emf.ecore.EAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ChatCompletionAdapter} and {@link AnswerText}.
 * <p>
 * The response is built as a dynamic EMF object with the shape of a Messages API answer - a
 * response with content blocks, some of which carry text - rather than with a provider's
 * generated classes, which is exactly the shape the adapter claims to read reflectively.
 * @author Ilenia Salvadori
 */
public class ChatCompletionAdapterTest {

	private EClass responseEClass;
	private EClass textBlockEClass;
	private EClass toolUseBlockEClass;
	private EClass usageEClass;

	@BeforeEach
	void setUp() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("responses");
		ePackage.setNsPrefix("r");
		ePackage.setNsURI("http://example.org/responses/1.0");

		textBlockEClass = eClass(ePackage, "TextBlock");
		textBlockEClass.getEStructuralFeatures().add(stringAttribute("text"));
		// a block with no text feature at all, like a tool-use or thinking block
		toolUseBlockEClass = eClass(ePackage, "ToolUseBlock");
		toolUseBlockEClass.getEStructuralFeatures().add(stringAttribute("name"));

		usageEClass = eClass(ePackage, "Usage");
		usageEClass.getEStructuralFeatures().add(intAttribute("inputTokens"));
		usageEClass.getEStructuralFeatures().add(intAttribute("outputTokens"));

		responseEClass = eClass(ePackage, "Response");
		EReference content = EcoreFactory.eINSTANCE.createEReference();
		content.setName("content");
		content.setEType(EcorePackage.Literals.EOBJECT);
		content.setUpperBound(-1);
		content.setContainment(true);
		responseEClass.getEStructuralFeatures().add(content);
		EReference usage = EcoreFactory.eINSTANCE.createEReference();
		usage.setName("usage");
		usage.setEType(usageEClass);
		usage.setContainment(true);
		responseEClass.getEStructuralFeatures().add(usage);
	}

	@Test
	@DisplayName("The agent's answer is read out of the response's text blocks")
	void answer_isReadFromTheContentBlocks() {
		EObject response = response(textBlock("I published the package."),
				textBlock("RECEIPT: created https://example.org/inferred/dragino/1.0"));

		assertEquals("""
				I published the package.
				RECEIPT: created https://example.org/inferred/dragino/1.0""", AnswerText.of(response));
	}

	@Test
	@DisplayName("Blocks that are not text - a tool use, a thought - are skipped")
	// A hundred-turn run ends with tool-use blocks all over its history; only what the agent
	// said carries the receipt.
	void answer_skipsNonTextBlocks() {
		EObject toolUse = EcoreUtil.create(toolUseBlockEClass);
		toolUse.eSet(toolUseBlockEClass.getEStructuralFeature("name"), "register_package");
		EObject response = response(toolUse, textBlock("RECEIPT: conflict urn:x"));

		assertEquals("RECEIPT: conflict urn:x", AnswerText.of(response));
	}

	@Test
	@DisplayName("A response with no text at all reads as no answer")
	// Which model inference records as an unreadable receipt - the run happened, its outcome is
	// unknown - rather than as a failure.
	void answer_withoutText_isNull() {
		assertNull(AnswerText.of(response()));
		assertNull(AnswerText.of(null));
	}

	@Test
	@DisplayName("Token usage is reported when the response carries it")
	// The input-token count is what tells an operator whether the tool allow-list is working:
	// the MCP tool definitions are re-sent on every one of a hundred turns.
	void usage_isReportedForTheLog() {
		EObject response = response(textBlock("done"));
		EObject usage = EcoreUtil.create(usageEClass);
		usage.eSet(usageEClass.getEStructuralFeature("inputTokens"), 40344);
		usage.eSet(usageEClass.getEStructuralFeature("outputTokens"), 512);
		response.eSet(responseEClass.getEStructuralFeature("usage"), usage);

		String reported = AnswerText.usageOf(response);

		assertTrue(reported.contains("inputTokens=40344"), reported);
		assertTrue(reported.contains("outputTokens=512"), reported);
	}

	@Test
	@DisplayName("A response without usage reports none rather than failing")
	void usage_isOptional() {
		assertNull(AnswerText.usageOf(response(textBlock("done"))));
		assertNull(AnswerText.usageOf(null));
	}

	@Test
	@DisplayName("The adapter hands the two messages to the service and returns its answer")
	void adapter_delegatesAndReturnsTheAnswer() throws Exception {
		ChatCompletionService service = mock(ChatCompletionService.class);
		when(service.complete(anyString(), anyString())).thenReturn(null);

		// a service that answers nothing must read as "no answer" rather than throw
		assertNull(adapter(service).complete("system", "user"));
		verify(service).complete("system", "user");
	}

	@Test
	@DisplayName("A transport failure becomes an unchecked throw, which inference records as unavailable")
	// The port's contract: a checked IOException must not reach the caller, and a swallowed
	// failure would be recorded as a run that produced nothing.
	void adapter_wrapsTransportFailures() throws Exception {
		ChatCompletionService service = mock(ChatCompletionService.class);
		when(service.complete(anyString(), anyString())).thenThrow(new IOException("connection refused"));
		ChatCompletionAdapter adapter = adapter(service);

		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> adapter.complete("system", "user"));

		assertTrue(failure.getMessage().contains("connection refused"));
	}

	private static ChatCompletionAdapter adapter(ChatCompletionService service) {
		ChatCompletionAdapter adapter = new ChatCompletionAdapter();
		try {
			// the field is DS-injected at runtime; set it directly here
			Field field = ChatCompletionAdapter.class.getDeclaredField("chatCompletionService");
			field.setAccessible(true);
			field.set(adapter, service);
		} catch (ReflectiveOperationException e) {
			throw new IllegalStateException("Could not inject the chat completion service", e);
		}
		return adapter;
	}

	private EObject response(EObject... blocks) {
		EObject response = EcoreUtil.create(responseEClass);
		@SuppressWarnings("unchecked")
		List<EObject> content = (List<EObject>) response.eGet(responseEClass.getEStructuralFeature("content"));
		content.addAll(List.of(blocks));
		return response;
	}

	private EObject textBlock(String text) {
		EObject block = EcoreUtil.create(textBlockEClass);
		block.eSet(textBlockEClass.getEStructuralFeature("text"), text);
		return block;
	}

	private static EClass eClass(EPackage ePackage, String name) {
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(name);
		ePackage.getEClassifiers().add(eClass);
		return eClass;
	}

	private static EAttribute stringAttribute(String name) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(EcorePackage.Literals.ESTRING);
		return attribute;
	}

	private static EAttribute intAttribute(String name) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(EcorePackage.Literals.EINT);
		return attribute;
	}

}
