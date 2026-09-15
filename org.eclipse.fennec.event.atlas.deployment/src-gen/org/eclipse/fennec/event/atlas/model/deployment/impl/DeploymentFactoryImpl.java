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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.event.atlas.model.deployment.impl;

import java.time.Duration;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.event.atlas.model.deployment.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class DeploymentFactoryImpl extends EFactoryImpl implements DeploymentFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static DeploymentFactory init() {
		try {
			DeploymentFactory theDeploymentFactory = (DeploymentFactory)EPackage.Registry.INSTANCE.getEFactory(DeploymentPackage.eNS_URI);
			if (theDeploymentFactory != null) {
				return theDeploymentFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new DeploymentFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public DeploymentFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case DeploymentPackage.EVENT_ATLAS_DEPLOYMENT: return createEventAtlasDeployment();
			case DeploymentPackage.HTTP_ENDPOINT: return createHttpEndpoint();
			case DeploymentPackage.ATLAS_BINDING: return createAtlasBinding();
			case DeploymentPackage.MQTT_BROKER: return createMqttBroker();
			case DeploymentPackage.INGEST_CHANNEL: return createIngestChannel();
			case DeploymentPackage.HISTORY_CONFIG: return createHistoryConfig();
			case DeploymentPackage.TIMESCALE_STORAGE: return createTimescaleStorage();
			case DeploymentPackage.IN_MEMORY_STORAGE: return createInMemoryStorage();
			case DeploymentPackage.HISTORIZATION_FILTER: return createHistorizationFilter();
			case DeploymentPackage.HOUSEKEEPING_POLICY: return createHousekeepingPolicy();
			case DeploymentPackage.INFERENCE_CONFIG: return createInferenceConfig();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case DeploymentPackage.PREFETCH_MODE:
				return createPrefetchModeFromString(eDataType, initialValue);
			case DeploymentPackage.CHANNEL_TRANSPORT:
				return createChannelTransportFromString(eDataType, initialValue);
			case DeploymentPackage.PAYLOAD_FORMAT:
				return createPayloadFormatFromString(eDataType, initialValue);
			case DeploymentPackage.CHANGE_MODE:
				return createChangeModeFromString(eDataType, initialValue);
			case DeploymentPackage.EDURATION:
				return createEDurationFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case DeploymentPackage.PREFETCH_MODE:
				return convertPrefetchModeToString(eDataType, instanceValue);
			case DeploymentPackage.CHANNEL_TRANSPORT:
				return convertChannelTransportToString(eDataType, instanceValue);
			case DeploymentPackage.PAYLOAD_FORMAT:
				return convertPayloadFormatToString(eDataType, instanceValue);
			case DeploymentPackage.CHANGE_MODE:
				return convertChangeModeToString(eDataType, instanceValue);
			case DeploymentPackage.EDURATION:
				return convertEDurationToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EventAtlasDeployment createEventAtlasDeployment() {
		EventAtlasDeploymentImpl eventAtlasDeployment = new EventAtlasDeploymentImpl();
		return eventAtlasDeployment;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HttpEndpoint createHttpEndpoint() {
		HttpEndpointImpl httpEndpoint = new HttpEndpointImpl();
		return httpEndpoint;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AtlasBinding createAtlasBinding() {
		AtlasBindingImpl atlasBinding = new AtlasBindingImpl();
		return atlasBinding;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public MqttBroker createMqttBroker() {
		MqttBrokerImpl mqttBroker = new MqttBrokerImpl();
		return mqttBroker;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public IngestChannel createIngestChannel() {
		IngestChannelImpl ingestChannel = new IngestChannelImpl();
		return ingestChannel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HistoryConfig createHistoryConfig() {
		HistoryConfigImpl historyConfig = new HistoryConfigImpl();
		return historyConfig;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TimescaleStorage createTimescaleStorage() {
		TimescaleStorageImpl timescaleStorage = new TimescaleStorageImpl();
		return timescaleStorage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public InMemoryStorage createInMemoryStorage() {
		InMemoryStorageImpl inMemoryStorage = new InMemoryStorageImpl();
		return inMemoryStorage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HistorizationFilter createHistorizationFilter() {
		HistorizationFilterImpl historizationFilter = new HistorizationFilterImpl();
		return historizationFilter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HousekeepingPolicy createHousekeepingPolicy() {
		HousekeepingPolicyImpl housekeepingPolicy = new HousekeepingPolicyImpl();
		return housekeepingPolicy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public InferenceConfig createInferenceConfig() {
		InferenceConfigImpl inferenceConfig = new InferenceConfigImpl();
		return inferenceConfig;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PrefetchMode createPrefetchModeFromString(EDataType eDataType, String initialValue) {
		PrefetchMode result = PrefetchMode.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPrefetchModeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ChannelTransport createChannelTransportFromString(EDataType eDataType, String initialValue) {
		ChannelTransport result = ChannelTransport.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertChannelTransportToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PayloadFormat createPayloadFormatFromString(EDataType eDataType, String initialValue) {
		PayloadFormat result = PayloadFormat.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPayloadFormatToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ChangeMode createChangeModeFromString(EDataType eDataType, String initialValue) {
		ChangeMode result = ChangeMode.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertChangeModeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Duration createEDuration(final String it) {
		return it == null || it.isBlank() ? null : java.time.Duration.parse(it);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Duration createEDurationFromString(EDataType eDataType, String initialValue) {
		return createEDuration(initialValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEDuration(final Duration it) {
		return it == null ? null : it.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertEDurationToString(EDataType eDataType, Object instanceValue) {
		return convertEDuration((Duration)instanceValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DeploymentPackage getDeploymentPackage() {
		return (DeploymentPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static DeploymentPackage getPackage() {
		return DeploymentPackage.eINSTANCE;
	}

} //DeploymentFactoryImpl
