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

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding;
import org.eclipse.fennec.event.atlas.model.deployment.ChangeMode;
import org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport;
import org.eclipse.fennec.event.atlas.model.deployment.DeploymentFactory;
import org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage;
import org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment;
import org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter;
import org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig;
import org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage;
import org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy;
import org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint;
import org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage;
import org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig;
import org.eclipse.fennec.event.atlas.model.deployment.IngestChannel;
import org.eclipse.fennec.event.atlas.model.deployment.MqttBroker;
import org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat;
import org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode;
import org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class DeploymentPackageImpl extends EPackageImpl implements DeploymentPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass eventAtlasDeploymentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass httpEndpointEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass atlasBindingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass mqttBrokerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass ingestChannelEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass historyConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass historyStorageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass timescaleStorageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass inMemoryStorageEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass historizationFilterEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass housekeepingPolicyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass inferenceConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum prefetchModeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum channelTransportEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum payloadFormatEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum changeModeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EDataType eDurationEDataType = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private DeploymentPackageImpl() {
		super(eNS_URI, DeploymentFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link DeploymentPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static DeploymentPackage init() {
		if (isInited) return (DeploymentPackage)EPackage.Registry.INSTANCE.getEPackage(DeploymentPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredDeploymentPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		DeploymentPackageImpl theDeploymentPackage = registeredDeploymentPackage instanceof DeploymentPackageImpl ? (DeploymentPackageImpl)registeredDeploymentPackage : new DeploymentPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theDeploymentPackage.createPackageContents();

		// Initialize created meta-data
		theDeploymentPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theDeploymentPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(DeploymentPackage.eNS_URI, theDeploymentPackage);
		return theDeploymentPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getEventAtlasDeployment() {
		return eventAtlasDeploymentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEventAtlasDeployment_DeploymentId() {
		return (EAttribute)eventAtlasDeploymentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getEventAtlasDeployment_Description() {
		return (EAttribute)eventAtlasDeploymentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEventAtlasDeployment_Http() {
		return (EReference)eventAtlasDeploymentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEventAtlasDeployment_Atlas() {
		return (EReference)eventAtlasDeploymentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEventAtlasDeployment_Brokers() {
		return (EReference)eventAtlasDeploymentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEventAtlasDeployment_Channels() {
		return (EReference)eventAtlasDeploymentEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEventAtlasDeployment_History() {
		return (EReference)eventAtlasDeploymentEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getEventAtlasDeployment_Inference() {
		return (EReference)eventAtlasDeploymentEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getHttpEndpoint() {
		return httpEndpointEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHttpEndpoint_Port() {
		return (EAttribute)httpEndpointEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHttpEndpoint_Host() {
		return (EAttribute)httpEndpointEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHttpEndpoint_ContextPath() {
		return (EAttribute)httpEndpointEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHttpEndpoint_RestPath() {
		return (EAttribute)httpEndpointEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHttpEndpoint_WhiteboardName() {
		return (EAttribute)httpEndpointEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHttpEndpoint_AllowAnonymous() {
		return (EAttribute)httpEndpointEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAtlasBinding() {
		return atlasBindingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAtlasBinding_BaseUri() {
		return (EAttribute)atlasBindingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAtlasBinding_Scope() {
		return (EAttribute)atlasBindingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAtlasBinding_Stage() {
		return (EAttribute)atlasBindingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAtlasBinding_MappingRegistries() {
		return (EAttribute)atlasBindingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAtlasBinding_PrefetchMode() {
		return (EAttribute)atlasBindingEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAtlasBinding_RequiredNsUris() {
		return (EAttribute)atlasBindingEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAtlasBinding_DriftCheckIntervalMs() {
		return (EAttribute)atlasBindingEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAtlasBinding_RefreshIntervalMs() {
		return (EAttribute)atlasBindingEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getMqttBroker() {
		return mqttBrokerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMqttBroker_Id() {
		return (EAttribute)mqttBrokerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMqttBroker_Protocol() {
		return (EAttribute)mqttBrokerEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMqttBroker_Host() {
		return (EAttribute)mqttBrokerEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMqttBroker_Port() {
		return (EAttribute)mqttBrokerEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMqttBroker_User() {
		return (EAttribute)mqttBrokerEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMqttBroker_Password() {
		return (EAttribute)mqttBrokerEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getMqttBroker_Topics() {
		return (EAttribute)mqttBrokerEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getIngestChannel() {
		return ingestChannelEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIngestChannel_Name() {
		return (EAttribute)ingestChannelEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIngestChannel_Transport() {
		return (EAttribute)ingestChannelEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIngestChannel_Format() {
		return (EAttribute)ingestChannelEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIngestChannel_BrokerId() {
		return (EAttribute)ingestChannelEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIngestChannel_Topics() {
		return (EAttribute)ingestChannelEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getIngestChannel_CodecTypeMapId() {
		return (EAttribute)ingestChannelEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getHistoryConfig() {
		return historyConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistoryConfig_ProviderName() {
		return (EAttribute)historyConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getHistoryConfig_Storage() {
		return (EReference)historyConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getHistoryConfig_Filters() {
		return (EReference)historyConfigEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getHistoryConfig_Housekeeping() {
		return (EReference)historyConfigEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getHistoryStorage() {
		return historyStorageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistoryStorage_MaxPageSize() {
		return (EAttribute)historyStorageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistoryStorage_IncludeResources() {
		return (EAttribute)historyStorageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistoryStorage_ExcludeResources() {
		return (EAttribute)historyStorageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTimescaleStorage() {
		return timescaleStorageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTimescaleStorage_Url() {
		return (EAttribute)timescaleStorageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTimescaleStorage_Host() {
		return (EAttribute)timescaleStorageEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTimescaleStorage_Port() {
		return (EAttribute)timescaleStorageEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTimescaleStorage_Database() {
		return (EAttribute)timescaleStorageEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTimescaleStorage_User() {
		return (EAttribute)timescaleStorageEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTimescaleStorage_Password() {
		return (EAttribute)timescaleStorageEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getInMemoryStorage() {
		return inMemoryStorageEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getInMemoryStorage_MaxValuesPerResource() {
		return (EAttribute)inMemoryStorageEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getHistorizationFilter() {
		return historizationFilterEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistorizationFilter_Name() {
		return (EAttribute)historizationFilterEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistorizationFilter_Targets() {
		return (EAttribute)historizationFilterEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistorizationFilter_IncludeResources() {
		return (EAttribute)historizationFilterEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistorizationFilter_ExcludeResources() {
		return (EAttribute)historizationFilterEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistorizationFilter_ChangeMode() {
		return (EAttribute)historizationFilterEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistorizationFilter_ChangeThreshold() {
		return (EAttribute)historizationFilterEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistorizationFilter_ChangeThresholdPercent() {
		return (EAttribute)historizationFilterEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHistorizationFilter_ChangeMaxInterval() {
		return (EAttribute)historizationFilterEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getHousekeepingPolicy() {
		return housekeepingPolicyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHousekeepingPolicy_Name() {
		return (EAttribute)housekeepingPolicyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHousekeepingPolicy_Targets() {
		return (EAttribute)housekeepingPolicyEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHousekeepingPolicy_RetentionPeriod() {
		return (EAttribute)housekeepingPolicyEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHousekeepingPolicy_KeepCount() {
		return (EAttribute)housekeepingPolicyEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHousekeepingPolicy_MaxDelete() {
		return (EAttribute)housekeepingPolicyEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getHousekeepingPolicy_SchedulePeriod() {
		return (EAttribute)housekeepingPolicyEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getInferenceConfig() {
		return inferenceConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getInferenceConfig_SamplingEnabled() {
		return (EAttribute)inferenceConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getInferenceConfig_Namespace() {
		return (EAttribute)inferenceConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getInferenceConfig_MaxRunsPerInterval() {
		return (EAttribute)inferenceConfigEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getPrefetchMode() {
		return prefetchModeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getChannelTransport() {
		return channelTransportEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getPayloadFormat() {
		return payloadFormatEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getChangeMode() {
		return changeModeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EDataType getEDuration() {
		return eDurationEDataType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DeploymentFactory getDeploymentFactory() {
		return (DeploymentFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		eventAtlasDeploymentEClass = createEClass(EVENT_ATLAS_DEPLOYMENT);
		createEAttribute(eventAtlasDeploymentEClass, EVENT_ATLAS_DEPLOYMENT__DEPLOYMENT_ID);
		createEAttribute(eventAtlasDeploymentEClass, EVENT_ATLAS_DEPLOYMENT__DESCRIPTION);
		createEReference(eventAtlasDeploymentEClass, EVENT_ATLAS_DEPLOYMENT__HTTP);
		createEReference(eventAtlasDeploymentEClass, EVENT_ATLAS_DEPLOYMENT__ATLAS);
		createEReference(eventAtlasDeploymentEClass, EVENT_ATLAS_DEPLOYMENT__BROKERS);
		createEReference(eventAtlasDeploymentEClass, EVENT_ATLAS_DEPLOYMENT__CHANNELS);
		createEReference(eventAtlasDeploymentEClass, EVENT_ATLAS_DEPLOYMENT__HISTORY);
		createEReference(eventAtlasDeploymentEClass, EVENT_ATLAS_DEPLOYMENT__INFERENCE);

		httpEndpointEClass = createEClass(HTTP_ENDPOINT);
		createEAttribute(httpEndpointEClass, HTTP_ENDPOINT__PORT);
		createEAttribute(httpEndpointEClass, HTTP_ENDPOINT__HOST);
		createEAttribute(httpEndpointEClass, HTTP_ENDPOINT__CONTEXT_PATH);
		createEAttribute(httpEndpointEClass, HTTP_ENDPOINT__REST_PATH);
		createEAttribute(httpEndpointEClass, HTTP_ENDPOINT__WHITEBOARD_NAME);
		createEAttribute(httpEndpointEClass, HTTP_ENDPOINT__ALLOW_ANONYMOUS);

		atlasBindingEClass = createEClass(ATLAS_BINDING);
		createEAttribute(atlasBindingEClass, ATLAS_BINDING__BASE_URI);
		createEAttribute(atlasBindingEClass, ATLAS_BINDING__SCOPE);
		createEAttribute(atlasBindingEClass, ATLAS_BINDING__STAGE);
		createEAttribute(atlasBindingEClass, ATLAS_BINDING__MAPPING_REGISTRIES);
		createEAttribute(atlasBindingEClass, ATLAS_BINDING__PREFETCH_MODE);
		createEAttribute(atlasBindingEClass, ATLAS_BINDING__REQUIRED_NS_URIS);
		createEAttribute(atlasBindingEClass, ATLAS_BINDING__DRIFT_CHECK_INTERVAL_MS);
		createEAttribute(atlasBindingEClass, ATLAS_BINDING__REFRESH_INTERVAL_MS);

		mqttBrokerEClass = createEClass(MQTT_BROKER);
		createEAttribute(mqttBrokerEClass, MQTT_BROKER__ID);
		createEAttribute(mqttBrokerEClass, MQTT_BROKER__PROTOCOL);
		createEAttribute(mqttBrokerEClass, MQTT_BROKER__HOST);
		createEAttribute(mqttBrokerEClass, MQTT_BROKER__PORT);
		createEAttribute(mqttBrokerEClass, MQTT_BROKER__USER);
		createEAttribute(mqttBrokerEClass, MQTT_BROKER__PASSWORD);
		createEAttribute(mqttBrokerEClass, MQTT_BROKER__TOPICS);

		ingestChannelEClass = createEClass(INGEST_CHANNEL);
		createEAttribute(ingestChannelEClass, INGEST_CHANNEL__NAME);
		createEAttribute(ingestChannelEClass, INGEST_CHANNEL__TRANSPORT);
		createEAttribute(ingestChannelEClass, INGEST_CHANNEL__FORMAT);
		createEAttribute(ingestChannelEClass, INGEST_CHANNEL__BROKER_ID);
		createEAttribute(ingestChannelEClass, INGEST_CHANNEL__TOPICS);
		createEAttribute(ingestChannelEClass, INGEST_CHANNEL__CODEC_TYPE_MAP_ID);

		historyConfigEClass = createEClass(HISTORY_CONFIG);
		createEAttribute(historyConfigEClass, HISTORY_CONFIG__PROVIDER_NAME);
		createEReference(historyConfigEClass, HISTORY_CONFIG__STORAGE);
		createEReference(historyConfigEClass, HISTORY_CONFIG__FILTERS);
		createEReference(historyConfigEClass, HISTORY_CONFIG__HOUSEKEEPING);

		historyStorageEClass = createEClass(HISTORY_STORAGE);
		createEAttribute(historyStorageEClass, HISTORY_STORAGE__MAX_PAGE_SIZE);
		createEAttribute(historyStorageEClass, HISTORY_STORAGE__INCLUDE_RESOURCES);
		createEAttribute(historyStorageEClass, HISTORY_STORAGE__EXCLUDE_RESOURCES);

		timescaleStorageEClass = createEClass(TIMESCALE_STORAGE);
		createEAttribute(timescaleStorageEClass, TIMESCALE_STORAGE__URL);
		createEAttribute(timescaleStorageEClass, TIMESCALE_STORAGE__HOST);
		createEAttribute(timescaleStorageEClass, TIMESCALE_STORAGE__PORT);
		createEAttribute(timescaleStorageEClass, TIMESCALE_STORAGE__DATABASE);
		createEAttribute(timescaleStorageEClass, TIMESCALE_STORAGE__USER);
		createEAttribute(timescaleStorageEClass, TIMESCALE_STORAGE__PASSWORD);

		inMemoryStorageEClass = createEClass(IN_MEMORY_STORAGE);
		createEAttribute(inMemoryStorageEClass, IN_MEMORY_STORAGE__MAX_VALUES_PER_RESOURCE);

		historizationFilterEClass = createEClass(HISTORIZATION_FILTER);
		createEAttribute(historizationFilterEClass, HISTORIZATION_FILTER__NAME);
		createEAttribute(historizationFilterEClass, HISTORIZATION_FILTER__TARGETS);
		createEAttribute(historizationFilterEClass, HISTORIZATION_FILTER__INCLUDE_RESOURCES);
		createEAttribute(historizationFilterEClass, HISTORIZATION_FILTER__EXCLUDE_RESOURCES);
		createEAttribute(historizationFilterEClass, HISTORIZATION_FILTER__CHANGE_MODE);
		createEAttribute(historizationFilterEClass, HISTORIZATION_FILTER__CHANGE_THRESHOLD);
		createEAttribute(historizationFilterEClass, HISTORIZATION_FILTER__CHANGE_THRESHOLD_PERCENT);
		createEAttribute(historizationFilterEClass, HISTORIZATION_FILTER__CHANGE_MAX_INTERVAL);

		housekeepingPolicyEClass = createEClass(HOUSEKEEPING_POLICY);
		createEAttribute(housekeepingPolicyEClass, HOUSEKEEPING_POLICY__NAME);
		createEAttribute(housekeepingPolicyEClass, HOUSEKEEPING_POLICY__TARGETS);
		createEAttribute(housekeepingPolicyEClass, HOUSEKEEPING_POLICY__RETENTION_PERIOD);
		createEAttribute(housekeepingPolicyEClass, HOUSEKEEPING_POLICY__KEEP_COUNT);
		createEAttribute(housekeepingPolicyEClass, HOUSEKEEPING_POLICY__MAX_DELETE);
		createEAttribute(housekeepingPolicyEClass, HOUSEKEEPING_POLICY__SCHEDULE_PERIOD);

		inferenceConfigEClass = createEClass(INFERENCE_CONFIG);
		createEAttribute(inferenceConfigEClass, INFERENCE_CONFIG__SAMPLING_ENABLED);
		createEAttribute(inferenceConfigEClass, INFERENCE_CONFIG__NAMESPACE);
		createEAttribute(inferenceConfigEClass, INFERENCE_CONFIG__MAX_RUNS_PER_INTERVAL);

		// Create enums
		prefetchModeEEnum = createEEnum(PREFETCH_MODE);
		channelTransportEEnum = createEEnum(CHANNEL_TRANSPORT);
		payloadFormatEEnum = createEEnum(PAYLOAD_FORMAT);
		changeModeEEnum = createEEnum(CHANGE_MODE);

		// Create data types
		eDurationEDataType = createEDataType(EDURATION);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		timescaleStorageEClass.getESuperTypes().add(this.getHistoryStorage());
		inMemoryStorageEClass.getESuperTypes().add(this.getHistoryStorage());

		// Initialize classes, features, and operations; add parameters
		initEClass(eventAtlasDeploymentEClass, EventAtlasDeployment.class, "EventAtlasDeployment", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getEventAtlasDeployment_DeploymentId(), ecorePackage.getEString(), "deploymentId", null, 1, 1, EventAtlasDeployment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getEventAtlasDeployment_Description(), ecorePackage.getEString(), "description", null, 0, 1, EventAtlasDeployment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEventAtlasDeployment_Http(), this.getHttpEndpoint(), null, "http", null, 0, 1, EventAtlasDeployment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEventAtlasDeployment_Atlas(), this.getAtlasBinding(), null, "atlas", null, 0, 1, EventAtlasDeployment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEventAtlasDeployment_Brokers(), this.getMqttBroker(), null, "brokers", null, 0, -1, EventAtlasDeployment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEventAtlasDeployment_Channels(), this.getIngestChannel(), null, "channels", null, 0, -1, EventAtlasDeployment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEventAtlasDeployment_History(), this.getHistoryConfig(), null, "history", null, 0, 1, EventAtlasDeployment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getEventAtlasDeployment_Inference(), this.getInferenceConfig(), null, "inference", null, 0, 1, EventAtlasDeployment.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(httpEndpointEClass, HttpEndpoint.class, "HttpEndpoint", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getHttpEndpoint_Port(), ecorePackage.getEInt(), "port", "8080", 0, 1, HttpEndpoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHttpEndpoint_Host(), ecorePackage.getEString(), "host", "0.0.0.0", 0, 1, HttpEndpoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHttpEndpoint_ContextPath(), ecorePackage.getEString(), "contextPath", "event/", 0, 1, HttpEndpoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHttpEndpoint_RestPath(), ecorePackage.getEString(), "restPath", "rest", 0, 1, HttpEndpoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHttpEndpoint_WhiteboardName(), ecorePackage.getEString(), "whiteboardName", "eventrest", 0, 1, HttpEndpoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHttpEndpoint_AllowAnonymous(), ecorePackage.getEBoolean(), "allowAnonymous", "true", 0, 1, HttpEndpoint.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(atlasBindingEClass, AtlasBinding.class, "AtlasBinding", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAtlasBinding_BaseUri(), ecorePackage.getEString(), "baseUri", null, 1, 1, AtlasBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAtlasBinding_Scope(), ecorePackage.getEString(), "scope", null, 1, 1, AtlasBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAtlasBinding_Stage(), ecorePackage.getEString(), "stage", "release", 0, 1, AtlasBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAtlasBinding_MappingRegistries(), ecorePackage.getEString(), "mappingRegistries", null, 0, -1, AtlasBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAtlasBinding_PrefetchMode(), this.getPrefetchMode(), "prefetchMode", "EAGER", 0, 1, AtlasBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAtlasBinding_RequiredNsUris(), ecorePackage.getEString(), "requiredNsUris", null, 0, -1, AtlasBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAtlasBinding_DriftCheckIntervalMs(), ecorePackage.getELong(), "driftCheckIntervalMs", "10000", 0, 1, AtlasBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAtlasBinding_RefreshIntervalMs(), ecorePackage.getELong(), "refreshIntervalMs", "60000", 0, 1, AtlasBinding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(mqttBrokerEClass, MqttBroker.class, "MqttBroker", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getMqttBroker_Id(), ecorePackage.getEString(), "id", null, 1, 1, MqttBroker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMqttBroker_Protocol(), ecorePackage.getEString(), "protocol", "tcp", 0, 1, MqttBroker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMqttBroker_Host(), ecorePackage.getEString(), "host", "localhost", 0, 1, MqttBroker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMqttBroker_Port(), ecorePackage.getEInt(), "port", "1883", 0, 1, MqttBroker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMqttBroker_User(), ecorePackage.getEString(), "user", null, 0, 1, MqttBroker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMqttBroker_Password(), ecorePackage.getEString(), "password", null, 0, 1, MqttBroker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getMqttBroker_Topics(), ecorePackage.getEString(), "topics", null, 0, -1, MqttBroker.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(ingestChannelEClass, IngestChannel.class, "IngestChannel", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getIngestChannel_Name(), ecorePackage.getEString(), "name", null, 1, 1, IngestChannel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIngestChannel_Transport(), this.getChannelTransport(), "transport", "MQTT", 0, 1, IngestChannel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIngestChannel_Format(), this.getPayloadFormat(), "format", "XMI", 0, 1, IngestChannel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIngestChannel_BrokerId(), ecorePackage.getEString(), "brokerId", null, 0, 1, IngestChannel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIngestChannel_Topics(), ecorePackage.getEString(), "topics", null, 0, -1, IngestChannel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getIngestChannel_CodecTypeMapId(), ecorePackage.getEString(), "codecTypeMapId", null, 0, 1, IngestChannel.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(historyConfigEClass, HistoryConfig.class, "HistoryConfig", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getHistoryConfig_ProviderName(), ecorePackage.getEString(), "providerName", "brokerHistory", 0, 1, HistoryConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getHistoryConfig_Storage(), this.getHistoryStorage(), null, "storage", null, 0, 1, HistoryConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getHistoryConfig_Filters(), this.getHistorizationFilter(), null, "filters", null, 0, -1, HistoryConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getHistoryConfig_Housekeeping(), this.getHousekeepingPolicy(), null, "housekeeping", null, 0, -1, HistoryConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(historyStorageEClass, HistoryStorage.class, "HistoryStorage", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getHistoryStorage_MaxPageSize(), ecorePackage.getEInt(), "maxPageSize", "10000", 0, 1, HistoryStorage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHistoryStorage_IncludeResources(), ecorePackage.getEString(), "includeResources", null, 0, -1, HistoryStorage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHistoryStorage_ExcludeResources(), ecorePackage.getEString(), "excludeResources", null, 0, -1, HistoryStorage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(timescaleStorageEClass, TimescaleStorage.class, "TimescaleStorage", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTimescaleStorage_Url(), ecorePackage.getEString(), "url", null, 0, 1, TimescaleStorage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTimescaleStorage_Host(), ecorePackage.getEString(), "host", "localhost", 0, 1, TimescaleStorage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTimescaleStorage_Port(), ecorePackage.getEInt(), "port", "5432", 0, 1, TimescaleStorage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTimescaleStorage_Database(), ecorePackage.getEString(), "database", "sensinactHistory", 0, 1, TimescaleStorage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTimescaleStorage_User(), ecorePackage.getEString(), "user", null, 0, 1, TimescaleStorage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTimescaleStorage_Password(), ecorePackage.getEString(), "password", null, 0, 1, TimescaleStorage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(inMemoryStorageEClass, InMemoryStorage.class, "InMemoryStorage", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getInMemoryStorage_MaxValuesPerResource(), ecorePackage.getEInt(), "maxValuesPerResource", null, 0, 1, InMemoryStorage.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(historizationFilterEClass, HistorizationFilter.class, "HistorizationFilter", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getHistorizationFilter_Name(), ecorePackage.getEString(), "name", null, 1, 1, HistorizationFilter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHistorizationFilter_Targets(), ecorePackage.getEString(), "targets", null, 0, -1, HistorizationFilter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHistorizationFilter_IncludeResources(), ecorePackage.getEString(), "includeResources", null, 0, -1, HistorizationFilter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHistorizationFilter_ExcludeResources(), ecorePackage.getEString(), "excludeResources", null, 0, -1, HistorizationFilter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHistorizationFilter_ChangeMode(), this.getChangeMode(), "changeMode", "ALL", 0, 1, HistorizationFilter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHistorizationFilter_ChangeThreshold(), ecorePackage.getEDouble(), "changeThreshold", null, 0, 1, HistorizationFilter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHistorizationFilter_ChangeThresholdPercent(), ecorePackage.getEDouble(), "changeThresholdPercent", null, 0, 1, HistorizationFilter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHistorizationFilter_ChangeMaxInterval(), this.getEDuration(), "changeMaxInterval", null, 0, 1, HistorizationFilter.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(housekeepingPolicyEClass, HousekeepingPolicy.class, "HousekeepingPolicy", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getHousekeepingPolicy_Name(), ecorePackage.getEString(), "name", null, 1, 1, HousekeepingPolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHousekeepingPolicy_Targets(), ecorePackage.getEString(), "targets", null, 0, -1, HousekeepingPolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHousekeepingPolicy_RetentionPeriod(), this.getEDuration(), "retentionPeriod", null, 0, 1, HousekeepingPolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHousekeepingPolicy_KeepCount(), ecorePackage.getEInt(), "keepCount", null, 0, 1, HousekeepingPolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHousekeepingPolicy_MaxDelete(), ecorePackage.getEInt(), "maxDelete", null, 0, 1, HousekeepingPolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getHousekeepingPolicy_SchedulePeriod(), this.getEDuration(), "schedulePeriod", null, 0, 1, HousekeepingPolicy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(inferenceConfigEClass, InferenceConfig.class, "InferenceConfig", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getInferenceConfig_SamplingEnabled(), ecorePackage.getEBoolean(), "samplingEnabled", "false", 0, 1, InferenceConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInferenceConfig_Namespace(), ecorePackage.getEString(), "namespace", null, 0, 1, InferenceConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getInferenceConfig_MaxRunsPerInterval(), ecorePackage.getEInt(), "maxRunsPerInterval", "5", 0, 1, InferenceConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(prefetchModeEEnum, PrefetchMode.class, "PrefetchMode");
		addEEnumLiteral(prefetchModeEEnum, PrefetchMode.EAGER);
		addEEnumLiteral(prefetchModeEEnum, PrefetchMode.LAZY);

		initEEnum(channelTransportEEnum, ChannelTransport.class, "ChannelTransport");
		addEEnumLiteral(channelTransportEEnum, ChannelTransport.MQTT);
		addEEnumLiteral(channelTransportEEnum, ChannelTransport.REST);

		initEEnum(payloadFormatEEnum, PayloadFormat.class, "PayloadFormat");
		addEEnumLiteral(payloadFormatEEnum, PayloadFormat.XMI);
		addEEnumLiteral(payloadFormatEEnum, PayloadFormat.JSON);

		initEEnum(changeModeEEnum, ChangeMode.class, "ChangeMode");
		addEEnumLiteral(changeModeEEnum, ChangeMode.ALL);
		addEEnumLiteral(changeModeEEnum, ChangeMode.ON_CHANGE);
		addEEnumLiteral(changeModeEEnum, ChangeMode.DEADBAND);

		// Initialize data types
		initEDataType(eDurationEDataType, Duration.class, "EDuration", IS_SERIALIZABLE, !IS_GENERATED_INSTANCE_CLASS);

		// Create resource
		createResource(eNS_URI);
	}

} //DeploymentPackageImpl
