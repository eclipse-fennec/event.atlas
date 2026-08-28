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

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.BatchResponse;
import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.BatchResult;
import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.BatchStatusType;
import org.eclipse.fennec.ai.apis.meta.model.aiapismeta.MessageBatch;
import org.eclipse.fennec.ai.chat.completion.api.BatchChatCompletionService;
import org.eclipse.fennec.event.atlas.model.inference.ChatCompletion;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * Runs the inference completion through the <em>batch</em> API instead of a synchronous request,
 * and blocks until the batch has a result.
 * <p>
 * <b>Why this exists.</b> A synchronous request cannot finish this task. The Messages API runs
 * server-side tools in a sampling loop with a documented <em>default limit of 10 iterations per
 * request</em>; when it is reached the response comes back with
 * {@code stop_reason: pause_turn}, carrying whatever the agent had said so far and no final
 * answer - so model inference reports an answer with no receipt. Measured on 2026-08-28 against
 * this exact prompt: 11 iterations then {@code pause_turn}, identically with and without
 * streaming, while the same request submitted as a batch ran 21 iterations to
 * {@code stop_reason: end_turn} and published its draft. Authoring a package takes more than ten
 * iterations, so the batch path is the one that completes. See
 * {@code docs/model-inference-test-log.md}.
 * <p>
 * <b>Deliberately the simplest thing that works.</b> It submits one batch, polls it, and returns
 * the text. That is enough to prove the chain end to end, and it is honest about two costs:
 * <ul>
 * <li><b>It blocks for a long time.</b> The measured run took about twenty minutes including
 * queueing, and a batch's window is 24h. {@link ChatCompletion#complete(String, String)} is
 * allowed to block for minutes and is called on model inference's own runner thread, but
 * {@code timeoutSeconds} on the inference configuration has to be raised well past its 900s
 * default or a run that was going to succeed is cancelled near the end.</li>
 * <li><b>An abandoned batch keeps running, and keeps costing.</b> If this thread is interrupted -
 * a timeout, a framework stop - the batch is not cancelled, and nothing here remembers its id
 * across a restart. The id is therefore logged at INFO on submission, so a run can be recovered
 * by hand. Persisting it against the sample-set fingerprint, and reattaching instead of
 * resubmitting, is the obvious next step and is not done here.</li>
 * </ul>
 * <p>
 * <b>Which adapter wins.</b> {@link ChatCompletionAdapter} and this one both publish
 * {@link ChatCompletion}, and model inference binds that reference {@code GREEDY}, so the
 * higher {@code service.ranking} is used. This component ranks above the synchronous adapter and
 * is {@link ConfigurationPolicy#REQUIRE}, so the sync path stays in charge until a
 * {@code event.atlas.model.inference.chat.batch} configuration exists - deploying this bundle
 * changes nothing on its own.
 * @author Ilenia Salvadori
 * @since 28.08.2026
 */
@Component(name = BatchChatCompletionAdapter.PID, configurationPid = BatchChatCompletionAdapter.PID, //
		configurationPolicy = ConfigurationPolicy.REQUIRE, property = "service.ranking:Integer=100")
@Designate(ocd = BatchChatCompletionAdapter.Config.class)
public class BatchChatCompletionAdapter implements ChatCompletion {

	/** The configuration pid - its presence is what switches the batch path on. */
	static final String PID = "event.atlas.model.inference.chat.batch";

	private static final Logger logger = Logger.getLogger(BatchChatCompletionAdapter.class.getName());

	@ObjectClassDefinition
	public @interface Config {

		/**
		 * How long to wait between polls of the batch's status. The batch API is not fast and
		 * polling it hard buys nothing: a run is minutes, not seconds.
		 */
		long pollSeconds() default 20;
	}

	@Reference
	private BatchChatCompletionService<?> batchService;

	private volatile Duration poll = Duration.ofSeconds(20);

	@Activate
	@Modified
	void activate(Config config) {
		poll = Duration.ofSeconds(Math.max(1, config.pollSeconds()));
		logger.info(() -> String.format(
				"Model inference will use the batch API, polling every %ss. Note that a batch keeps running "
						+ "(and costs) if this runtime stops while waiting.",
				poll.toSeconds()));
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.event.atlas.model.inference.ChatCompletion#complete(java.lang.String, java.lang.String)
	 */
	@Override
	public String complete(String systemMessage, String userMessage) {
		Instant started = Instant.now();
		String customId = "event-atlas-inference-" + UUID.randomUUID();
		String batchId;
		try {
			MessageBatch batch = batchService.createMessageBatch(customId, systemMessage, userMessage);
			BatchResponse submitted = batchService.sendBatch(batch);
			batchId = submitted.getBatchId();
		} catch (IOException e) {
			throw new IllegalStateException("The completion batch could not be submitted: " + describe(e), e);
		} catch (RuntimeException e) {
			// As in the synchronous adapter: the client reports a non-2xx by failing to
			// deserialize the body, so a rejected key or a wrong endpoint arrives here wearing
			// a message about types. Name the suspects rather than leave an operator guessing.
			throw new IllegalStateException(String.format(
					"The completion batch was rejected and the answer could not be read (%s). An HTTP error from "
							+ "the provider looks exactly like this - check that api.key is set in the environment "
							+ "and that base.url points at the provider's messages endpoint.",
					describe(e)), e);
		}
		// INFO, not FINE: this id is the only handle on a run that outlives the runtime.
		logger.log(Level.INFO, () -> String.format(
				"Completion batch '%s' submitted (custom id '%s'). This takes minutes; the id is what to use if "
						+ "this runtime stops before it finishes.",
				batchId, customId));
		return awaitResult(batchId, started);
	}

	/**
	 * Polls until the batch leaves {@link BatchStatusType#IN_PROGRESS}, then reads its result.
	 * An interruption is propagated as an interrupt and reported as unavailable - model
	 * inference cancels the call on its own timeout, and that is the path it takes.
	 */
	private String awaitResult(String batchId, Instant started) {
		try {
			BatchStatusType status;
			while ((status = batchService.getBatch(batchId).getBatchStatus()) == BatchStatusType.IN_PROGRESS) {
				Thread.sleep(poll.toMillis());
			}
			if (status != BatchStatusType.COMPLETED) {
				throw new IllegalStateException(
						String.format("Completion batch '%s' ended as %s", batchId, status));
			}
			BatchResult result = batchService.getBatchResult(batchId);
			if (!result.isSuccessful()) {
				throw new IllegalStateException(String.format("Completion batch '%s' failed: %s", batchId,
						result.getErrorMessage()));
			}
			logger.log(Level.INFO, () -> String.format("Completion batch '%s' answered in %ss", batchId,
					Duration.between(started, Instant.now()).toSeconds()));
			return result.getResultText();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			// The batch is deliberately not cancelled: it may well be nearly done, and the id
			// is in the log. Cancelling here would throw away paid work on every timeout.
			throw new IllegalStateException(String.format(
					"Stopped waiting for completion batch '%s' - it is still running and can be read back by id",
					batchId), e);
		} catch (IOException e) {
			throw new IllegalStateException(
					String.format("Completion batch '%s' could not be read: %s", batchId, describe(e)), e);
		}
	}

	private static String describe(Throwable e) {
		return e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
	}

}
