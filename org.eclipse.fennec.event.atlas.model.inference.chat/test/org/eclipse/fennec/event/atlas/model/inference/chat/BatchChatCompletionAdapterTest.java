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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.BatchResponse;
import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.BatchResult;
import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.BatchStatusType;
import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.MessageBatch;
import org.eclipse.fennec.ai.chat.completion.api.BatchChatCompletionService;
import org.eclipse.fennec.event.atlas.model.inference.InferenceOutcome;
import org.eclipse.fennec.event.atlas.model.inference.InferenceOutcome.Status;
import org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResult;
import org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceResultFactory;
import org.eclipse.fennec.event.atlas.model.inference.chat.result.InferenceStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

/**
 * The continuation loop of {@link BatchChatCompletionAdapter}.
 * <p>
 * A turn that stopped before finishing comes back neither successful nor failed:
 * {@code isSuccessful()} is false and {@code getErrorMessage()} is null. Reporting that as a
 * failure - which is what this adapter did before, as "failed: null" - both misnames it and throws
 * away work that can be resumed. Measured 2026-08-28: three batch runs went 21, 15 and 22
 * iterations, the last of them stopped at the provider's iteration budget, so this is a normal
 * outcome of a long run rather than an edge case.
 * <p>
 * Two things here are regression guards for issue #75, and both need the service mock to hand back
 * a <em>distinct</em> batch per call - an earlier version of this test returned one shared mock,
 * which is precisely what hid the first of them:
 * <ul>
 * <li>a continuation must carry the batch that produced the unfinished result as {@code previous},
 * or from the second continuation on the request opens with the result of a tool use it no longer
 * carries and the provider rejects it - after billing;</li>
 * <li>a turn cut at {@code max.tokens} is unfinished for a different reason but in the same state,
 * so the question asked is {@code isContinuable} and not {@code isPaused}.</li>
 * </ul>
 * @author Ilenia Salvadori
 */
public class BatchChatCompletionAdapterTest {

	private static final String SYSTEM = "you author models";
	private static final String USER = "here are five payloads";

	private BatchChatCompletionService<?> batchService;
	private BatchChatCompletionAdapter adapter;
	private Resource resource;
	/** Every batch the adapter asked for, in order - the first is the opening turn. */
	private final List<MessageBatch> created = new ArrayList<>();
	private final Deque<BatchResult> continuable = new ArrayDeque<>();
	private final Deque<BatchResult> paused = new ArrayDeque<>();

	@BeforeEach
	void setUp() throws Exception {
		batchService = mock(BatchChatCompletionService.class);
		// Built before they are stubbed: mocking inside a when(...) argument is nested stubbing.
		BatchResponse completed = mock(BatchResponse.class);
		when(completed.getBatchStatus()).thenReturn(BatchStatusType.COMPLETED);
		// A fresh batch per call, recorded: whether the adapter hands the RIGHT one over as
		// `previous` is the whole point of the continuation tests, and a shared mock cannot say.
		when(batchService.createMessageBatch(anyString(), anyString(), anyString(), any(EClass.class)))
				.thenAnswer(call -> {
					MessageBatch batch = mock(MessageBatch.class, "batch-" + (created.size() + 1));
					created.add(batch);
					return batch;
				});
		when(batchService.getBatch(anyString())).thenReturn(completed);
		// Every result the service is asked about reports what the test queued it as.
		when(batchService.isContinuable(any(), anyString()))
				.thenAnswer(call -> continuable.contains(call.getArgument(0)));
		when(batchService.isPaused(any(), anyString())).thenAnswer(call -> paused.contains(call.getArgument(0)));
		// The codec is not on a unit test's classpath, so the resource is stubbed: what is under
		// test here is the loop and the mapping, not the JSON parse - that the schema round-trips
		// is the live run's job to show.
		resource = mock(Resource.class);
		ResourceSet resourceSet = mock(ResourceSet.class);
		when(resourceSet.createResource(any())).thenReturn(resource);
		when(resource.getContents()).thenReturn(new BasicEList<>());

		adapter = new BatchChatCompletionAdapter();
		inject("batchService", batchService);
		inject("resourceSet", resourceSet);
		adapter.activate(config(2));
	}

	@Test
	@DisplayName("The agent's structured answer becomes the outcome, field for field")
	void structuredAnswer_becomesTheOutcome() throws Exception {
		answerWith(finished("{\"status\":\"PUBLISHED\"}"));
		agentAnswers(InferenceStatus.PUBLISHED, "https://example.org/inferred/dragino/lse01", "five samples, one model");

		InferenceOutcome outcome = adapter.complete(SYSTEM, USER);

		assertEquals(Status.PUBLISHED, outcome.status());
		assertEquals("https://example.org/inferred/dragino/lse01", outcome.nsUri(),
				"The namespace is the one field nothing else records");
		assertEquals("five samples, one model", outcome.message());
		verify(batchService, never()).continueMessageBatch(any(), any(), any(), anyString());
	}

	@Test
	@DisplayName("Every status the agent can report maps onto the port's")
	void everyAgentStatus_maps() throws Exception {
		for (InferenceStatus reported : InferenceStatus.values()) {
			answerWith(finished("{}"));
			agentAnswers(reported, "https://example.org/inferred/x", "because");

			assertEquals(reported.getName(), adapter.complete(SYSTEM, USER).status().name(),
					"The two enums have to stay in step, and nothing else checks that");
		}
	}

	@Test
	@DisplayName("An answer the schema cannot be read out of falls back to the agent's prose")
	// A turn that exhausts its continuations has text but never reached its structured answer.
	// Reporting that as prose is far better than reporting it as a failure.
	void unreadableStructuredAnswer_fallsBackToTheText() throws Exception {
		answerWith(finished("I published it. RECEIPT: created https://example.org/inferred/dragino/lse01"));
		doThrow(new IOException("not json")).when(resource).load(any(InputStream.class), any());

		InferenceOutcome outcome = adapter.complete(SYSTEM, USER);

		assertEquals(Status.PUBLISHED, outcome.status());
		assertEquals("https://example.org/inferred/dragino/lse01", outcome.nsUri());
	}

	@Test
	@DisplayName("A paused turn is resumed, and the resumed turn's answer is the outcome")
	void pausedTurn_isResumed() throws Exception {
		answerWith(pausedResult("I'll discover the existing models first"), finished("{}"));
		agentAnswers(InferenceStatus.PUBLISHED, "https://example.org/inferred/dragino/lse01", "done");

		InferenceOutcome outcome = adapter.complete(SYSTEM, USER);

		assertEquals(Status.PUBLISHED, outcome.status());
		// The prompt is rebuilt rather than replayed: it is a pure function of the sample set, so
		// it yields the identical opening turn, while model, tools and in particular the MCP
		// authorization token have to come from current configuration.
		verify(batchService, times(2)).createMessageBatch(anyString(), eq(SYSTEM), eq(USER), any(EClass.class));
		verify(batchService).continueMessageBatch(any(), any(), any(), anyString());
	}

	/**
	 * Issue #75, defect 2. A turn cut at the caller's own output-token cap reports exactly what a
	 * paused one does - not successful, no error - and is resumable in exactly the same way, so
	 * asking {@code isPaused} discarded a run that had only to carry on.
	 */
	@Test
	@DisplayName("A turn cut at max_tokens is resumed too, not reported as a failure")
	void turnCutAtMaxTokens_isResumed() throws Exception {
		answerWith(unfinishedResult("I have created the EPackage and am adding"), finished("{}"));
		agentAnswers(InferenceStatus.PUBLISHED, "https://example.org/inferred/dragino/lse01", "done");

		InferenceOutcome outcome = adapter.complete(SYSTEM, USER);

		assertEquals(Status.PUBLISHED, outcome.status(),
				"A turn that stopped at max_tokens is unfinished, not failed - isContinuable says so, isPaused does not");
		verify(batchService).continueMessageBatch(any(), any(), any(), anyString());
	}

	/**
	 * Issue #75, defect 1. The messages have to accumulate across continuations, because each one
	 * opens with the result of the tool use that ended the previous turn and the provider requires
	 * that tool use to be in the same request. Rebuilding the batch from the prompt and passing it
	 * as its own history is right exactly once; the second continuation then references a tool use
	 * nothing carries, and is rejected after the batch has been accepted, queued and billed.
	 */
	@Test
	@DisplayName("Each continuation takes its messages from the batch that produced the unfinished result")
	void continuation_carriesThePreviousBatch() throws Exception {
		BatchResult first = pausedResult("discovering");
		BatchResult second = pausedResult("authoring");
		answerWith(first, second, finished("{}"));
		agentAnswers(InferenceStatus.PUBLISHED, "https://example.org/inferred/dragino/lse01", "done");

		adapter.complete(SYSTEM, USER);

		ArgumentCaptor<MessageBatch> next = ArgumentCaptor.forClass(MessageBatch.class);
		ArgumentCaptor<MessageBatch> previous = ArgumentCaptor.forClass(MessageBatch.class);
		ArgumentCaptor<BatchResult> unfinished = ArgumentCaptor.forClass(BatchResult.class);
		verify(batchService, times(2)).continueMessageBatch(next.capture(), previous.capture(),
				unfinished.capture(), anyString());

		assertEquals(3, created.size(), "one batch per turn: the opening one and two continuations");
		assertSame(created.get(1), next.getAllValues().get(0));
		assertSame(created.get(0), previous.getAllValues().get(0),
				"the first continuation continues the opening batch");
		assertSame(created.get(2), next.getAllValues().get(1));
		assertSame(created.get(1), previous.getAllValues().get(1),
				"the second continuation has to continue the FIRST continuation, which is where the "
						+ "tool use it opens with lives - not the batch it was just built from");
		assertNotSame(next.getAllValues().get(1), previous.getAllValues().get(1),
				"a batch handed itself as its own history carries no conversation at all");
		assertSame(first, unfinished.getAllValues().get(0));
		assertSame(second, unfinished.getAllValues().get(1));
	}

	@Test
	@DisplayName("A continuation is submitted under its own custom id, and resumes the previous one's")
	// The custom id names the request inside the batch; handing back the new batch's id would ask
	// the service to continue a request that has not run yet.
	void continuation_namesThePreviousCustomId() throws Exception {
		answerWith(pausedResult("discovering"), finished("{}"));
		agentAnswers(InferenceStatus.PUBLISHED, "https://example.org/inferred/x", "done");
		ArgumentCaptor<String> submitted = ArgumentCaptor.forClass(String.class);

		adapter.complete(SYSTEM, USER);

		verify(batchService, times(2)).createMessageBatch(submitted.capture(), anyString(), anyString(),
				any(EClass.class));
		ArgumentCaptor<String> resumed = ArgumentCaptor.forClass(String.class);
		verify(batchService).continueMessageBatch(any(), any(), any(), resumed.capture());
		assertEquals(submitted.getAllValues().get(0), resumed.getValue());
		assertNotEquals(submitted.getAllValues().get(0), submitted.getAllValues().get(1),
				"each batch carries its own custom id");
	}

	@Test
	@DisplayName("Falling back to prose joins what every turn said")
	// The structured answer lives in the final turn only; the joined text is the fallback's
	// material, and a receipt that landed before a pause would otherwise be lost.
	void proseFallback_joinsTheTurns() throws Exception {
		answerWith(pausedResult("RECEIPT: created https://example.org/inferred/dragino/lse01"),
				finished("and that is all"));
		doThrow(new IOException("not json")).when(resource).load(any(InputStream.class), any());

		assertEquals("https://example.org/inferred/dragino/lse01", adapter.complete(SYSTEM, USER).nsUri());
	}

	@Test
	@DisplayName("Continuation stops at the configured bound, and says so")
	void unfinishedForever_isGivenUpOnWithAnActionableMessage() throws Exception {
		answerWith(pausedResult("still working"), pausedResult("still working"), pausedResult("still working"));

		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> adapter.complete(SYSTEM, USER));
		assertTrue(failure.getMessage().contains("still unfinished after 2 continuation(s)"), failure.getMessage());
		assertTrue(failure.getMessage().contains("maxContinuations"),
				"The message has to name the setting to raise: " + failure.getMessage());
		verify(batchService, times(2)).continueMessageBatch(any(), any(), any(), anyString());
	}

	@Test
	@DisplayName("maxContinuations=0 restores the submit-once behaviour")
	void continuationDisabled_doesNotResume() throws Exception {
		adapter.activate(config(0));
		answerWith(pausedResult("still working"));

		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> adapter.complete(SYSTEM, USER));
		assertTrue(failure.getMessage().contains("still unfinished after 0 continuation(s)"), failure.getMessage());
		verify(batchService, never()).continueMessageBatch(any(), any(), any(), anyString());
	}

	@Test
	@DisplayName("A genuine failure is still reported as one, not resumed")
	void failedTurn_isNotResumed() throws Exception {
		BatchResult failed = mock(BatchResult.class);
		when(failed.isSuccessful()).thenReturn(false);
		when(failed.getErrorMessage()).thenReturn("overloaded_error");
		answerWith(failed);

		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> adapter.complete(SYSTEM, USER));
		assertTrue(failure.getMessage().contains("overloaded_error"),
				"A real error must still be reported as one: " + failure.getMessage());
		verify(batchService, never()).continueMessageBatch(any(), any(), any(), anyString());
	}

	/**
	 * A batch the API rejected ends as FAILED, and the reason - which field the request was missing -
	 * is only in the results file. Reporting the status alone made a rejected continuation look like a
	 * provider outage for an hour.
	 */
	@Test
	@DisplayName("A batch that ends as FAILED reports the provider's own error")
	void failedBatch_reportsTheProviderError() throws Exception {
		BatchStatusType failedStatus = BatchStatusType.FAILED;
		BatchResponse failedBatch = mock(BatchResponse.class);
		when(failedBatch.getBatchStatus()).thenReturn(failedStatus);
		when(batchService.getBatch(anyString())).thenReturn(failedBatch);
		BatchResult errors = mock(BatchResult.class);
		when(errors.getErrorMessage())
				.thenReturn("messages.1.content.0.mcp_tool_use.input: Field required");
		when(batchService.getBatchError(anyString())).thenReturn(errors);
		answerWith(finished("{\"status\":\"PUBLISHED\"}"));

		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> adapter.complete(SYSTEM, USER));

		assertTrue(failure.getMessage().contains("ended as FAILED"), failure.getMessage());
		assertTrue(failure.getMessage().contains("mcp_tool_use.input: Field required"),
				"the field the provider named has to reach the log: " + failure.getMessage());
	}

	/** Enriching the message is a second request; when it fails the status must still be reported. */
	@Test
	@DisplayName("An unreadable provider error does not replace the status")
	void failedBatch_survivesAnUnreadableProviderError() throws Exception {
		BatchResponse failedBatch = mock(BatchResponse.class);
		when(failedBatch.getBatchStatus()).thenReturn(BatchStatusType.FAILED);
		when(batchService.getBatch(anyString())).thenReturn(failedBatch);
		when(batchService.getBatchError(anyString())).thenThrow(new IOException("results are gone"));
		answerWith(finished("{\"status\":\"PUBLISHED\"}"));

		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> adapter.complete(SYSTEM, USER));

		assertTrue(failure.getMessage().contains("ended as FAILED"), failure.getMessage());
		assertTrue(failure.getMessage().contains("could not be read"), failure.getMessage());
	}

	/** What the stubbed codec yields when the adapter reads the final turn's answer. */
	private void agentAnswers(InferenceStatus status, String nsUri, String message) {
		InferenceResult result = InferenceResultFactory.eINSTANCE.createInferenceResult();
		result.setStatus(status);
		result.setNsUri(nsUri);
		result.setMessage(message);
		BasicEList<org.eclipse.emf.ecore.EObject> contents = new BasicEList<>();
		contents.add(result);
		when(resource.getContents()).thenReturn(contents);
	}

	/** Queues the results the service hands back, one per batch, in order. */
	private void answerWith(BatchResult... results) throws IOException {
		Deque<BatchResult> queue = new ArrayDeque<>(List.of(results));
		BatchResponse submitted = mock(BatchResponse.class);
		when(submitted.getBatchId()).thenReturn("batch-1");
		when(batchService.sendBatch(any(MessageBatch.class))).thenReturn(submitted);
		when(batchService.getBatchResult(anyString())).thenAnswer(call -> queue.poll());
	}

	private static BatchResult finished(String text) {
		BatchResult result = mock(BatchResult.class);
		when(result.isSuccessful()).thenReturn(true);
		when(result.getResultText()).thenReturn(text);
		return result;
	}

	/**
	 * A turn that stopped before finishing: not successful, no error. {@code isContinuable} holds,
	 * {@code isPaused} does not - which is what a turn cut at {@code max.tokens} looks like.
	 */
	private BatchResult unfinishedResult(String textSoFar) {
		BatchResult result = mock(BatchResult.class);
		when(result.isSuccessful()).thenReturn(false);
		when(result.getErrorMessage()).thenReturn(null);
		when(result.getResultText()).thenReturn(textSoFar);
		continuable.add(result);
		return result;
	}

	/** As {@link #unfinishedResult(String)}, and paused as well: stopped at the iteration budget. */
	private BatchResult pausedResult(String textSoFar) {
		BatchResult result = unfinishedResult(textSoFar);
		paused.add(result);
		return result;
	}

	private static BatchChatCompletionAdapter.Config config(int maxContinuations) {
		BatchChatCompletionAdapter.Config config = mock(BatchChatCompletionAdapter.Config.class);
		// 1s is the adapter's floor; nothing here ever polls twice, the batch is COMPLETED at once
		when(config.pollSeconds()).thenReturn(1L);
		when(config.maxContinuations()).thenReturn(maxContinuations);
		return config;
	}

	private void inject(String fieldName, Object value) throws Exception {
		Field field = BatchChatCompletionAdapter.class.getDeclaredField(fieldName);
		field.setAccessible(true);
		field.set(adapter, value);
	}
}
