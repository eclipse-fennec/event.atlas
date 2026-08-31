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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.BatchResponse;
import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.BatchResult;
import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.BatchStatusType;
import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.MessageBatch;
import org.eclipse.fennec.ai.chat.completion.api.BatchChatCompletionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The continuation loop of {@link BatchChatCompletionAdapter}.
 * <p>
 * A turn stopped at the provider's server-side iteration budget comes back
 * {@code stop_reason: pause_turn}, which is neither success nor failure: {@code isSuccessful()}
 * is false and {@code getErrorMessage()} is null. Reporting it as a failure - which is what this
 * adapter did before, as "failed: null" - both misnames it and throws away work that can be
 * resumed. Measured 2026-08-28: three batch runs went 21, 15 and 22 iterations, the last of them
 * paused, so this is a normal outcome of a long run rather than an edge case.
 * @author Ilenia Salvadori
 */
public class BatchChatCompletionAdapterTest {

	private static final String SYSTEM = "you author models";
	private static final String USER = "here are five payloads";

	private BatchChatCompletionService<?> batchService;
	private BatchChatCompletionAdapter adapter;
	private final Deque<BatchResult> paused = new ArrayDeque<>();

	@BeforeEach
	void setUp() throws Exception {
		batchService = mock(BatchChatCompletionService.class);
		// Built before they are stubbed: mocking inside a when(...) argument is nested stubbing.
		MessageBatch batch = mock(MessageBatch.class);
		BatchResponse completed = mock(BatchResponse.class);
		when(completed.getBatchStatus()).thenReturn(BatchStatusType.COMPLETED);
		when(batchService.createMessageBatch(anyString(), anyString(), anyString())).thenReturn(batch);
		when(batchService.getBatch(anyString())).thenReturn(completed);
		// Every result the service is asked about is paused if the test queued it as such.
		when(batchService.isPaused(any(), anyString())).thenAnswer(call -> paused.contains(call.getArgument(0)));
		adapter = new BatchChatCompletionAdapter();
		inject("batchService", batchService);
		adapter.activate(config(2));
	}

	@Test
	@DisplayName("A finished turn is returned as it stands")
	void finishedTurn_isReturned() throws Exception {
		answerWith(finished("RECEIPT: created https://example.org/inferred"));

		assertThat(adapter.complete(SYSTEM, USER)).isEqualTo("RECEIPT: created https://example.org/inferred");
		verify(batchService, never()).continueMessageBatch(any(), any(), anyString());
	}

	@Test
	@DisplayName("A paused turn is resumed, and what each turn said is joined")
	void pausedTurn_isResumedAndJoined() throws Exception {
		BatchResult first = pausedResult("I'll discover the existing models first");
		answerWith(first, finished("RECEIPT: created https://example.org/inferred"));

		String answer = adapter.complete(SYSTEM, USER);

		assertThat(answer).isEqualTo("""
				I'll discover the existing models first
				RECEIPT: created https://example.org/inferred""");
		// The result carries the assistant turn but not the prompt, so the continuation is built
		// from the same system and user message - not replayed from the paused batch.
		verify(batchService, times(2)).createMessageBatch(anyString(), eq(SYSTEM), eq(USER));
		verify(batchService).continueMessageBatch(any(), any(), anyString());
	}

	@Test
	@DisplayName("Continuation stops at the configured bound, and says so")
	void pausedForever_isGivenUpOnWithAnActionableMessage() throws Exception {
		answerWith(pausedResult("still working"), pausedResult("still working"), pausedResult("still working"));

		assertThatThrownBy(() -> adapter.complete(SYSTEM, USER))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("still paused after 2 continuation(s)")
				.hasMessageContaining("maxContinuations");
		verify(batchService, times(2)).continueMessageBatch(any(), any(), anyString());
	}

	@Test
	@DisplayName("maxContinuations=0 restores the submit-once behaviour")
	void continuationDisabled_doesNotResume() throws Exception {
		adapter.activate(config(0));
		answerWith(pausedResult("still working"));

		assertThatThrownBy(() -> adapter.complete(SYSTEM, USER))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("still paused after 0 continuation(s)");
		verify(batchService, never()).continueMessageBatch(any(), any(), anyString());
	}

	@Test
	@DisplayName("A genuine failure is still reported as one, not resumed")
	void failedTurn_isNotResumed() throws Exception {
		BatchResult failed = mock(BatchResult.class);
		when(failed.isSuccessful()).thenReturn(false);
		when(failed.getErrorMessage()).thenReturn("overloaded_error");
		answerWith(failed);

		assertThatThrownBy(() -> adapter.complete(SYSTEM, USER))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("overloaded_error");
		verify(batchService, never()).continueMessageBatch(any(), any(), anyString());
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

	/** What a turn stopped at the iteration budget looks like: not successful, no error. */
	private BatchResult pausedResult(String textSoFar) {
		BatchResult result = mock(BatchResult.class);
		when(result.isSuccessful()).thenReturn(false);
		when(result.getErrorMessage()).thenReturn(null);
		when(result.getResultText()).thenReturn(textSoFar);
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
