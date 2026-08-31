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

import org.eclipse.fennec.codec.jsonschema.v2.value.EClassValueWriter;
import org.eclipse.fennec.codec.value.CodecValueWriter;
import org.osgi.service.component.annotations.Component;

/**
 * Publishes the codec's {@code eClassToJsonSchema} writer as a service, so that a structured
 * completion request carries an inlined JSON Schema instead of a reference to one.
 * <p>
 * <b>Why this is here and not in the codec.</b> {@code OutputFormat.schema} in
 * {@code claude-chat-completion.ecore} is an {@code EReference} to an {@code EClass} annotated
 * {@code valueWriterName="eClassToJsonSchema"}, and the Claude client sets the matching
 * {@code CodecJsonSchemaOptions} on the request - so both ends expect that writer to be
 * available. It is not: {@code org.eclipse.fennec.codec.jsonschema} registers its handlers into
 * a <em>copy</em> of the shared registry ({@code JsonSchemaResourceFactoryComponent} does
 * {@code registry.copy()}, deliberately, so plain-object handlers cannot outlive the bundle), so
 * they only ever reach resources that factory creates. A request serialized as ordinary JSON
 * never sees them, and the EClass is written as a reference:
 *
 * <pre>
 * "schema":{"_type":"…Ecore#//EClass","$ref":"//fennec.eclipse.org/…#//InferenceResult"}
 * </pre>
 *
 * which Anthropic rejects outright - <em>"External schema references are not supported"</em>,
 * measured 2026-08-31.
 * <p>
 * The shared registry's own documented extension point is the whiteboard this uses:
 * {@code CodecValueRegistryComponent} binds every {@code CodecValueWriter} service and
 * unregisters it when its bundle goes, which is exactly the lifecycle the jsonschema bundle was
 * avoiding by copying. So this is the intended wiring, supplied by the bundle that needs it.
 * Deleting it is right the moment the codec publishes the writer itself.
 * @author Ilenia Salvadori
 */
@Component(service = CodecValueWriter.class)
public class SchemaValueWriter extends EClassValueWriter {
}
