/*
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
package org.eclipse.fennec.event.atlas.model.deployment;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentFactory
 * @model kind="package"
 * @generated
 */
@ProviderType
@EPackage(uri = DeploymentPackage.eNS_URI, fingerprint = "fp1:5ab9b092deb99fdfbcdf8355576e7b2202df0b56795bb27b52c73c3d7da9cb44", genModel = "/model/event-atlas-deployment.genmodel", genModelSourceLocations = {"model/event-atlas-deployment.genmodel","org.eclipse.fennec.event.atlas.deployment/model/event-atlas-deployment.genmodel"}, ecore = "/model/event-atlas-deployment.ecore", ecoreSourceLocations = "/model/event-atlas-deployment.ecore")
public interface DeploymentPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "deployment";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "https://fennec.eclipse.org/event.atlas/deployment/1.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "deployment";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DeploymentPackage eINSTANCE = org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl <em>Event Atlas Deployment</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getEventAtlasDeployment()
	 * @generated
	 */
	int EVENT_ATLAS_DEPLOYMENT = 0;

	/**
	 * The feature id for the '<em><b>Deployment Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_ATLAS_DEPLOYMENT__DEPLOYMENT_ID = 0;

	/**
	 * The feature id for the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_ATLAS_DEPLOYMENT__DESCRIPTION = 1;

	/**
	 * The feature id for the '<em><b>Http</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_ATLAS_DEPLOYMENT__HTTP = 2;

	/**
	 * The feature id for the '<em><b>Atlas</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_ATLAS_DEPLOYMENT__ATLAS = 3;

	/**
	 * The feature id for the '<em><b>Brokers</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_ATLAS_DEPLOYMENT__BROKERS = 4;

	/**
	 * The feature id for the '<em><b>Channels</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_ATLAS_DEPLOYMENT__CHANNELS = 5;

	/**
	 * The feature id for the '<em><b>History</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_ATLAS_DEPLOYMENT__HISTORY = 6;

	/**
	 * The feature id for the '<em><b>Inference</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_ATLAS_DEPLOYMENT__INFERENCE = 7;

	/**
	 * The number of structural features of the '<em>Event Atlas Deployment</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_ATLAS_DEPLOYMENT_FEATURE_COUNT = 8;

	/**
	 * The number of operations of the '<em>Event Atlas Deployment</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int EVENT_ATLAS_DEPLOYMENT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HttpEndpointImpl <em>Http Endpoint</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.HttpEndpointImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getHttpEndpoint()
	 * @generated
	 */
	int HTTP_ENDPOINT = 1;

	/**
	 * The feature id for the '<em><b>Port</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HTTP_ENDPOINT__PORT = 0;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HTTP_ENDPOINT__HOST = 1;

	/**
	 * The feature id for the '<em><b>Context Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HTTP_ENDPOINT__CONTEXT_PATH = 2;

	/**
	 * The feature id for the '<em><b>Rest Path</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HTTP_ENDPOINT__REST_PATH = 3;

	/**
	 * The feature id for the '<em><b>Whiteboard Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HTTP_ENDPOINT__WHITEBOARD_NAME = 4;

	/**
	 * The feature id for the '<em><b>Allow Anonymous</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HTTP_ENDPOINT__ALLOW_ANONYMOUS = 5;

	/**
	 * The number of structural features of the '<em>Http Endpoint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HTTP_ENDPOINT_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Http Endpoint</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HTTP_ENDPOINT_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl <em>Atlas Binding</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getAtlasBinding()
	 * @generated
	 */
	int ATLAS_BINDING = 2;

	/**
	 * The feature id for the '<em><b>Base Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATLAS_BINDING__BASE_URI = 0;

	/**
	 * The feature id for the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATLAS_BINDING__SCOPE = 1;

	/**
	 * The feature id for the '<em><b>Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATLAS_BINDING__STAGE = 2;

	/**
	 * The feature id for the '<em><b>Mapping Registries</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATLAS_BINDING__MAPPING_REGISTRIES = 3;

	/**
	 * The feature id for the '<em><b>Prefetch Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATLAS_BINDING__PREFETCH_MODE = 4;

	/**
	 * The feature id for the '<em><b>Required Ns Uris</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATLAS_BINDING__REQUIRED_NS_URIS = 5;

	/**
	 * The feature id for the '<em><b>Drift Check Interval Ms</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATLAS_BINDING__DRIFT_CHECK_INTERVAL_MS = 6;

	/**
	 * The feature id for the '<em><b>Refresh Interval Ms</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATLAS_BINDING__REFRESH_INTERVAL_MS = 7;

	/**
	 * The number of structural features of the '<em>Atlas Binding</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATLAS_BINDING_FEATURE_COUNT = 8;

	/**
	 * The number of operations of the '<em>Atlas Binding</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int ATLAS_BINDING_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.MqttBrokerImpl <em>Mqtt Broker</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.MqttBrokerImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getMqttBroker()
	 * @generated
	 */
	int MQTT_BROKER = 3;

	/**
	 * The feature id for the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_BROKER__ID = 0;

	/**
	 * The feature id for the '<em><b>Protocol</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_BROKER__PROTOCOL = 1;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_BROKER__HOST = 2;

	/**
	 * The feature id for the '<em><b>Port</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_BROKER__PORT = 3;

	/**
	 * The feature id for the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_BROKER__USER = 4;

	/**
	 * The feature id for the '<em><b>Password</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_BROKER__PASSWORD = 5;

	/**
	 * The feature id for the '<em><b>Topics</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_BROKER__TOPICS = 6;

	/**
	 * The number of structural features of the '<em>Mqtt Broker</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_BROKER_FEATURE_COUNT = 7;

	/**
	 * The number of operations of the '<em>Mqtt Broker</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int MQTT_BROKER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.IngestChannelImpl <em>Ingest Channel</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.IngestChannelImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getIngestChannel()
	 * @generated
	 */
	int INGEST_CHANNEL = 4;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INGEST_CHANNEL__NAME = 0;

	/**
	 * The feature id for the '<em><b>Transport</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INGEST_CHANNEL__TRANSPORT = 1;

	/**
	 * The feature id for the '<em><b>Format</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INGEST_CHANNEL__FORMAT = 2;

	/**
	 * The feature id for the '<em><b>Broker Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INGEST_CHANNEL__BROKER_ID = 3;

	/**
	 * The feature id for the '<em><b>Topics</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INGEST_CHANNEL__TOPICS = 4;

	/**
	 * The feature id for the '<em><b>Codec Type Map Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INGEST_CHANNEL__CODEC_TYPE_MAP_ID = 5;

	/**
	 * The number of structural features of the '<em>Ingest Channel</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INGEST_CHANNEL_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Ingest Channel</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INGEST_CHANNEL_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryConfigImpl <em>History Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryConfigImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getHistoryConfig()
	 * @generated
	 */
	int HISTORY_CONFIG = 5;

	/**
	 * The feature id for the '<em><b>Provider Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORY_CONFIG__PROVIDER_NAME = 0;

	/**
	 * The feature id for the '<em><b>Storage</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORY_CONFIG__STORAGE = 1;

	/**
	 * The feature id for the '<em><b>Filters</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORY_CONFIG__FILTERS = 2;

	/**
	 * The feature id for the '<em><b>Housekeeping</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORY_CONFIG__HOUSEKEEPING = 3;

	/**
	 * The number of structural features of the '<em>History Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORY_CONFIG_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>History Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORY_CONFIG_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryStorageImpl <em>History Storage</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryStorageImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getHistoryStorage()
	 * @generated
	 */
	int HISTORY_STORAGE = 6;

	/**
	 * The feature id for the '<em><b>Max Page Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORY_STORAGE__MAX_PAGE_SIZE = 0;

	/**
	 * The feature id for the '<em><b>Include Resources</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORY_STORAGE__INCLUDE_RESOURCES = 1;

	/**
	 * The feature id for the '<em><b>Exclude Resources</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORY_STORAGE__EXCLUDE_RESOURCES = 2;

	/**
	 * The number of structural features of the '<em>History Storage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORY_STORAGE_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>History Storage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORY_STORAGE_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.TimescaleStorageImpl <em>Timescale Storage</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.TimescaleStorageImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getTimescaleStorage()
	 * @generated
	 */
	int TIMESCALE_STORAGE = 7;

	/**
	 * The feature id for the '<em><b>Max Page Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESCALE_STORAGE__MAX_PAGE_SIZE = HISTORY_STORAGE__MAX_PAGE_SIZE;

	/**
	 * The feature id for the '<em><b>Include Resources</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESCALE_STORAGE__INCLUDE_RESOURCES = HISTORY_STORAGE__INCLUDE_RESOURCES;

	/**
	 * The feature id for the '<em><b>Exclude Resources</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESCALE_STORAGE__EXCLUDE_RESOURCES = HISTORY_STORAGE__EXCLUDE_RESOURCES;

	/**
	 * The feature id for the '<em><b>Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESCALE_STORAGE__URL = HISTORY_STORAGE_FEATURE_COUNT + 0;

	/**
	 * The feature id for the '<em><b>Host</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESCALE_STORAGE__HOST = HISTORY_STORAGE_FEATURE_COUNT + 1;

	/**
	 * The feature id for the '<em><b>Port</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESCALE_STORAGE__PORT = HISTORY_STORAGE_FEATURE_COUNT + 2;

	/**
	 * The feature id for the '<em><b>Database</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESCALE_STORAGE__DATABASE = HISTORY_STORAGE_FEATURE_COUNT + 3;

	/**
	 * The feature id for the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESCALE_STORAGE__USER = HISTORY_STORAGE_FEATURE_COUNT + 4;

	/**
	 * The feature id for the '<em><b>Password</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESCALE_STORAGE__PASSWORD = HISTORY_STORAGE_FEATURE_COUNT + 5;

	/**
	 * The number of structural features of the '<em>Timescale Storage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESCALE_STORAGE_FEATURE_COUNT = HISTORY_STORAGE_FEATURE_COUNT + 6;

	/**
	 * The number of operations of the '<em>Timescale Storage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TIMESCALE_STORAGE_OPERATION_COUNT = HISTORY_STORAGE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.InMemoryStorageImpl <em>In Memory Storage</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.InMemoryStorageImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getInMemoryStorage()
	 * @generated
	 */
	int IN_MEMORY_STORAGE = 8;

	/**
	 * The feature id for the '<em><b>Max Page Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IN_MEMORY_STORAGE__MAX_PAGE_SIZE = HISTORY_STORAGE__MAX_PAGE_SIZE;

	/**
	 * The feature id for the '<em><b>Include Resources</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IN_MEMORY_STORAGE__INCLUDE_RESOURCES = HISTORY_STORAGE__INCLUDE_RESOURCES;

	/**
	 * The feature id for the '<em><b>Exclude Resources</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IN_MEMORY_STORAGE__EXCLUDE_RESOURCES = HISTORY_STORAGE__EXCLUDE_RESOURCES;

	/**
	 * The feature id for the '<em><b>Max Values Per Resource</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IN_MEMORY_STORAGE__MAX_VALUES_PER_RESOURCE = HISTORY_STORAGE_FEATURE_COUNT + 0;

	/**
	 * The number of structural features of the '<em>In Memory Storage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IN_MEMORY_STORAGE_FEATURE_COUNT = HISTORY_STORAGE_FEATURE_COUNT + 1;

	/**
	 * The number of operations of the '<em>In Memory Storage</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int IN_MEMORY_STORAGE_OPERATION_COUNT = HISTORY_STORAGE_OPERATION_COUNT + 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl <em>Historization Filter</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getHistorizationFilter()
	 * @generated
	 */
	int HISTORIZATION_FILTER = 9;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORIZATION_FILTER__NAME = 0;

	/**
	 * The feature id for the '<em><b>Targets</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORIZATION_FILTER__TARGETS = 1;

	/**
	 * The feature id for the '<em><b>Include Resources</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORIZATION_FILTER__INCLUDE_RESOURCES = 2;

	/**
	 * The feature id for the '<em><b>Exclude Resources</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORIZATION_FILTER__EXCLUDE_RESOURCES = 3;

	/**
	 * The feature id for the '<em><b>Change Mode</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORIZATION_FILTER__CHANGE_MODE = 4;

	/**
	 * The feature id for the '<em><b>Change Threshold</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORIZATION_FILTER__CHANGE_THRESHOLD = 5;

	/**
	 * The feature id for the '<em><b>Change Threshold Percent</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORIZATION_FILTER__CHANGE_THRESHOLD_PERCENT = 6;

	/**
	 * The feature id for the '<em><b>Change Max Interval</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORIZATION_FILTER__CHANGE_MAX_INTERVAL = 7;

	/**
	 * The number of structural features of the '<em>Historization Filter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORIZATION_FILTER_FEATURE_COUNT = 8;

	/**
	 * The number of operations of the '<em>Historization Filter</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HISTORIZATION_FILTER_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HousekeepingPolicyImpl <em>Housekeeping Policy</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.HousekeepingPolicyImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getHousekeepingPolicy()
	 * @generated
	 */
	int HOUSEKEEPING_POLICY = 10;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HOUSEKEEPING_POLICY__NAME = 0;

	/**
	 * The feature id for the '<em><b>Targets</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HOUSEKEEPING_POLICY__TARGETS = 1;

	/**
	 * The feature id for the '<em><b>Retention Period</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HOUSEKEEPING_POLICY__RETENTION_PERIOD = 2;

	/**
	 * The feature id for the '<em><b>Keep Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HOUSEKEEPING_POLICY__KEEP_COUNT = 3;

	/**
	 * The feature id for the '<em><b>Max Delete</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HOUSEKEEPING_POLICY__MAX_DELETE = 4;

	/**
	 * The feature id for the '<em><b>Schedule Period</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HOUSEKEEPING_POLICY__SCHEDULE_PERIOD = 5;

	/**
	 * The number of structural features of the '<em>Housekeeping Policy</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HOUSEKEEPING_POLICY_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Housekeeping Policy</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int HOUSEKEEPING_POLICY_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.InferenceConfigImpl <em>Inference Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.InferenceConfigImpl
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getInferenceConfig()
	 * @generated
	 */
	int INFERENCE_CONFIG = 11;

	/**
	 * The feature id for the '<em><b>Sampling Enabled</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFERENCE_CONFIG__SAMPLING_ENABLED = 0;

	/**
	 * The feature id for the '<em><b>Namespace</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFERENCE_CONFIG__NAMESPACE = 1;

	/**
	 * The feature id for the '<em><b>Max Runs Per Interval</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFERENCE_CONFIG__MAX_RUNS_PER_INTERVAL = 2;

	/**
	 * The number of structural features of the '<em>Inference Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFERENCE_CONFIG_FEATURE_COUNT = 3;

	/**
	 * The number of operations of the '<em>Inference Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int INFERENCE_CONFIG_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode <em>Prefetch Mode</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getPrefetchMode()
	 * @generated
	 */
	int PREFETCH_MODE = 12;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport <em>Channel Transport</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getChannelTransport()
	 * @generated
	 */
	int CHANNEL_TRANSPORT = 13;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat <em>Payload Format</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getPayloadFormat()
	 * @generated
	 */
	int PAYLOAD_FORMAT = 14;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.event.atlas.model.deployment.ChangeMode <em>Change Mode</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.event.atlas.model.deployment.ChangeMode
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getChangeMode()
	 * @generated
	 */
	int CHANGE_MODE = 15;

	/**
	 * The meta object id for the '<em>EDuration</em>' data type.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see java.time.Duration
	 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getEDuration()
	 * @generated
	 */
	int EDURATION = 16;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment <em>Event Atlas Deployment</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Event Atlas Deployment</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment
	 * @generated
	 */
	EClass getEventAtlasDeployment();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getDeploymentId <em>Deployment Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Deployment Id</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getDeploymentId()
	 * @see #getEventAtlasDeployment()
	 * @generated
	 */
	EAttribute getEventAtlasDeployment_DeploymentId();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getDescription <em>Description</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Description</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getDescription()
	 * @see #getEventAtlasDeployment()
	 * @generated
	 */
	EAttribute getEventAtlasDeployment_Description();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getHttp <em>Http</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Http</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getHttp()
	 * @see #getEventAtlasDeployment()
	 * @generated
	 */
	EReference getEventAtlasDeployment_Http();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getAtlas <em>Atlas</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Atlas</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getAtlas()
	 * @see #getEventAtlasDeployment()
	 * @generated
	 */
	EReference getEventAtlasDeployment_Atlas();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getBrokers <em>Brokers</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Brokers</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getBrokers()
	 * @see #getEventAtlasDeployment()
	 * @generated
	 */
	EReference getEventAtlasDeployment_Brokers();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getChannels <em>Channels</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Channels</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getChannels()
	 * @see #getEventAtlasDeployment()
	 * @generated
	 */
	EReference getEventAtlasDeployment_Channels();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getHistory <em>History</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>History</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getHistory()
	 * @see #getEventAtlasDeployment()
	 * @generated
	 */
	EReference getEventAtlasDeployment_History();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getInference <em>Inference</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Inference</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.EventAtlasDeployment#getInference()
	 * @see #getEventAtlasDeployment()
	 * @generated
	 */
	EReference getEventAtlasDeployment_Inference();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint <em>Http Endpoint</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Http Endpoint</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint
	 * @generated
	 */
	EClass getHttpEndpoint();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getPort <em>Port</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Port</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getPort()
	 * @see #getHttpEndpoint()
	 * @generated
	 */
	EAttribute getHttpEndpoint_Port();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getHost <em>Host</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Host</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getHost()
	 * @see #getHttpEndpoint()
	 * @generated
	 */
	EAttribute getHttpEndpoint_Host();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getContextPath <em>Context Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Context Path</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getContextPath()
	 * @see #getHttpEndpoint()
	 * @generated
	 */
	EAttribute getHttpEndpoint_ContextPath();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getRestPath <em>Rest Path</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Rest Path</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getRestPath()
	 * @see #getHttpEndpoint()
	 * @generated
	 */
	EAttribute getHttpEndpoint_RestPath();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getWhiteboardName <em>Whiteboard Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Whiteboard Name</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#getWhiteboardName()
	 * @see #getHttpEndpoint()
	 * @generated
	 */
	EAttribute getHttpEndpoint_WhiteboardName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#isAllowAnonymous <em>Allow Anonymous</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Allow Anonymous</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HttpEndpoint#isAllowAnonymous()
	 * @see #getHttpEndpoint()
	 * @generated
	 */
	EAttribute getHttpEndpoint_AllowAnonymous();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding <em>Atlas Binding</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Atlas Binding</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding
	 * @generated
	 */
	EClass getAtlasBinding();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getBaseUri <em>Base Uri</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Base Uri</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getBaseUri()
	 * @see #getAtlasBinding()
	 * @generated
	 */
	EAttribute getAtlasBinding_BaseUri();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getScope <em>Scope</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Scope</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getScope()
	 * @see #getAtlasBinding()
	 * @generated
	 */
	EAttribute getAtlasBinding_Scope();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getStage <em>Stage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Stage</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getStage()
	 * @see #getAtlasBinding()
	 * @generated
	 */
	EAttribute getAtlasBinding_Stage();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getMappingRegistries <em>Mapping Registries</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Mapping Registries</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getMappingRegistries()
	 * @see #getAtlasBinding()
	 * @generated
	 */
	EAttribute getAtlasBinding_MappingRegistries();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getPrefetchMode <em>Prefetch Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Prefetch Mode</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getPrefetchMode()
	 * @see #getAtlasBinding()
	 * @generated
	 */
	EAttribute getAtlasBinding_PrefetchMode();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getRequiredNsUris <em>Required Ns Uris</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Required Ns Uris</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getRequiredNsUris()
	 * @see #getAtlasBinding()
	 * @generated
	 */
	EAttribute getAtlasBinding_RequiredNsUris();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getDriftCheckIntervalMs <em>Drift Check Interval Ms</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Drift Check Interval Ms</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getDriftCheckIntervalMs()
	 * @see #getAtlasBinding()
	 * @generated
	 */
	EAttribute getAtlasBinding_DriftCheckIntervalMs();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getRefreshIntervalMs <em>Refresh Interval Ms</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Refresh Interval Ms</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.AtlasBinding#getRefreshIntervalMs()
	 * @see #getAtlasBinding()
	 * @generated
	 */
	EAttribute getAtlasBinding_RefreshIntervalMs();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker <em>Mqtt Broker</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Mqtt Broker</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.MqttBroker
	 * @generated
	 */
	EClass getMqttBroker();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getId <em>Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Id</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getId()
	 * @see #getMqttBroker()
	 * @generated
	 */
	EAttribute getMqttBroker_Id();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getProtocol <em>Protocol</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Protocol</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getProtocol()
	 * @see #getMqttBroker()
	 * @generated
	 */
	EAttribute getMqttBroker_Protocol();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getHost <em>Host</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Host</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getHost()
	 * @see #getMqttBroker()
	 * @generated
	 */
	EAttribute getMqttBroker_Host();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getPort <em>Port</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Port</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getPort()
	 * @see #getMqttBroker()
	 * @generated
	 */
	EAttribute getMqttBroker_Port();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getUser <em>User</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>User</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getUser()
	 * @see #getMqttBroker()
	 * @generated
	 */
	EAttribute getMqttBroker_User();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getPassword <em>Password</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Password</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getPassword()
	 * @see #getMqttBroker()
	 * @generated
	 */
	EAttribute getMqttBroker_Password();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getTopics <em>Topics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Topics</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.MqttBroker#getTopics()
	 * @see #getMqttBroker()
	 * @generated
	 */
	EAttribute getMqttBroker_Topics();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel <em>Ingest Channel</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Ingest Channel</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.IngestChannel
	 * @generated
	 */
	EClass getIngestChannel();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getName()
	 * @see #getIngestChannel()
	 * @generated
	 */
	EAttribute getIngestChannel_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getTransport <em>Transport</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Transport</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getTransport()
	 * @see #getIngestChannel()
	 * @generated
	 */
	EAttribute getIngestChannel_Transport();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getFormat <em>Format</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Format</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getFormat()
	 * @see #getIngestChannel()
	 * @generated
	 */
	EAttribute getIngestChannel_Format();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getBrokerId <em>Broker Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Broker Id</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getBrokerId()
	 * @see #getIngestChannel()
	 * @generated
	 */
	EAttribute getIngestChannel_BrokerId();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getTopics <em>Topics</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Topics</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getTopics()
	 * @see #getIngestChannel()
	 * @generated
	 */
	EAttribute getIngestChannel_Topics();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getCodecTypeMapId <em>Codec Type Map Id</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Codec Type Map Id</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.IngestChannel#getCodecTypeMapId()
	 * @see #getIngestChannel()
	 * @generated
	 */
	EAttribute getIngestChannel_CodecTypeMapId();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig <em>History Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>History Config</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig
	 * @generated
	 */
	EClass getHistoryConfig();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getProviderName <em>Provider Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Provider Name</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getProviderName()
	 * @see #getHistoryConfig()
	 * @generated
	 */
	EAttribute getHistoryConfig_ProviderName();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getStorage <em>Storage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Storage</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getStorage()
	 * @see #getHistoryConfig()
	 * @generated
	 */
	EReference getHistoryConfig_Storage();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getFilters <em>Filters</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Filters</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getFilters()
	 * @see #getHistoryConfig()
	 * @generated
	 */
	EReference getHistoryConfig_Filters();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getHousekeeping <em>Housekeeping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Housekeeping</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistoryConfig#getHousekeeping()
	 * @see #getHistoryConfig()
	 * @generated
	 */
	EReference getHistoryConfig_Housekeeping();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage <em>History Storage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>History Storage</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage
	 * @generated
	 */
	EClass getHistoryStorage();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage#getMaxPageSize <em>Max Page Size</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Page Size</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage#getMaxPageSize()
	 * @see #getHistoryStorage()
	 * @generated
	 */
	EAttribute getHistoryStorage_MaxPageSize();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage#getIncludeResources <em>Include Resources</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Include Resources</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage#getIncludeResources()
	 * @see #getHistoryStorage()
	 * @generated
	 */
	EAttribute getHistoryStorage_IncludeResources();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage#getExcludeResources <em>Exclude Resources</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Exclude Resources</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistoryStorage#getExcludeResources()
	 * @see #getHistoryStorage()
	 * @generated
	 */
	EAttribute getHistoryStorage_ExcludeResources();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage <em>Timescale Storage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Timescale Storage</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage
	 * @generated
	 */
	EClass getTimescaleStorage();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getUrl <em>Url</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Url</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getUrl()
	 * @see #getTimescaleStorage()
	 * @generated
	 */
	EAttribute getTimescaleStorage_Url();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getHost <em>Host</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Host</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getHost()
	 * @see #getTimescaleStorage()
	 * @generated
	 */
	EAttribute getTimescaleStorage_Host();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getPort <em>Port</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Port</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getPort()
	 * @see #getTimescaleStorage()
	 * @generated
	 */
	EAttribute getTimescaleStorage_Port();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getDatabase <em>Database</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Database</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getDatabase()
	 * @see #getTimescaleStorage()
	 * @generated
	 */
	EAttribute getTimescaleStorage_Database();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getUser <em>User</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>User</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getUser()
	 * @see #getTimescaleStorage()
	 * @generated
	 */
	EAttribute getTimescaleStorage_User();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getPassword <em>Password</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Password</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getPassword()
	 * @see #getTimescaleStorage()
	 * @generated
	 */
	EAttribute getTimescaleStorage_Password();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage <em>In Memory Storage</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>In Memory Storage</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage
	 * @generated
	 */
	EClass getInMemoryStorage();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage#getMaxValuesPerResource <em>Max Values Per Resource</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Values Per Resource</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.InMemoryStorage#getMaxValuesPerResource()
	 * @see #getInMemoryStorage()
	 * @generated
	 */
	EAttribute getInMemoryStorage_MaxValuesPerResource();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter <em>Historization Filter</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Historization Filter</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter
	 * @generated
	 */
	EClass getHistorizationFilter();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getName()
	 * @see #getHistorizationFilter()
	 * @generated
	 */
	EAttribute getHistorizationFilter_Name();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getTargets <em>Targets</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Targets</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getTargets()
	 * @see #getHistorizationFilter()
	 * @generated
	 */
	EAttribute getHistorizationFilter_Targets();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getIncludeResources <em>Include Resources</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Include Resources</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getIncludeResources()
	 * @see #getHistorizationFilter()
	 * @generated
	 */
	EAttribute getHistorizationFilter_IncludeResources();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getExcludeResources <em>Exclude Resources</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Exclude Resources</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getExcludeResources()
	 * @see #getHistorizationFilter()
	 * @generated
	 */
	EAttribute getHistorizationFilter_ExcludeResources();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeMode <em>Change Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Change Mode</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeMode()
	 * @see #getHistorizationFilter()
	 * @generated
	 */
	EAttribute getHistorizationFilter_ChangeMode();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeThreshold <em>Change Threshold</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Change Threshold</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeThreshold()
	 * @see #getHistorizationFilter()
	 * @generated
	 */
	EAttribute getHistorizationFilter_ChangeThreshold();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeThresholdPercent <em>Change Threshold Percent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Change Threshold Percent</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeThresholdPercent()
	 * @see #getHistorizationFilter()
	 * @generated
	 */
	EAttribute getHistorizationFilter_ChangeThresholdPercent();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeMaxInterval <em>Change Max Interval</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Change Max Interval</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HistorizationFilter#getChangeMaxInterval()
	 * @see #getHistorizationFilter()
	 * @generated
	 */
	EAttribute getHistorizationFilter_ChangeMaxInterval();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy <em>Housekeeping Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Housekeeping Policy</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy
	 * @generated
	 */
	EClass getHousekeepingPolicy();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getName()
	 * @see #getHousekeepingPolicy()
	 * @generated
	 */
	EAttribute getHousekeepingPolicy_Name();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getTargets <em>Targets</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Targets</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getTargets()
	 * @see #getHousekeepingPolicy()
	 * @generated
	 */
	EAttribute getHousekeepingPolicy_Targets();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getRetentionPeriod <em>Retention Period</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Retention Period</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getRetentionPeriod()
	 * @see #getHousekeepingPolicy()
	 * @generated
	 */
	EAttribute getHousekeepingPolicy_RetentionPeriod();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getKeepCount <em>Keep Count</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Keep Count</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getKeepCount()
	 * @see #getHousekeepingPolicy()
	 * @generated
	 */
	EAttribute getHousekeepingPolicy_KeepCount();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getMaxDelete <em>Max Delete</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Delete</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getMaxDelete()
	 * @see #getHousekeepingPolicy()
	 * @generated
	 */
	EAttribute getHousekeepingPolicy_MaxDelete();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getSchedulePeriod <em>Schedule Period</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Schedule Period</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.HousekeepingPolicy#getSchedulePeriod()
	 * @see #getHousekeepingPolicy()
	 * @generated
	 */
	EAttribute getHousekeepingPolicy_SchedulePeriod();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig <em>Inference Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Inference Config</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig
	 * @generated
	 */
	EClass getInferenceConfig();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#isSamplingEnabled <em>Sampling Enabled</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Sampling Enabled</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#isSamplingEnabled()
	 * @see #getInferenceConfig()
	 * @generated
	 */
	EAttribute getInferenceConfig_SamplingEnabled();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#getNamespace <em>Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Namespace</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#getNamespace()
	 * @see #getInferenceConfig()
	 * @generated
	 */
	EAttribute getInferenceConfig_Namespace();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#getMaxRunsPerInterval <em>Max Runs Per Interval</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Max Runs Per Interval</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.InferenceConfig#getMaxRunsPerInterval()
	 * @see #getInferenceConfig()
	 * @generated
	 */
	EAttribute getInferenceConfig_MaxRunsPerInterval();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode <em>Prefetch Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Prefetch Mode</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode
	 * @generated
	 */
	EEnum getPrefetchMode();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport <em>Channel Transport</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Channel Transport</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport
	 * @generated
	 */
	EEnum getChannelTransport();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat <em>Payload Format</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Payload Format</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat
	 * @generated
	 */
	EEnum getPayloadFormat();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.event.atlas.model.deployment.ChangeMode <em>Change Mode</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Change Mode</em>'.
	 * @see org.eclipse.fennec.event.atlas.model.deployment.ChangeMode
	 * @generated
	 */
	EEnum getChangeMode();

	/**
	 * Returns the meta object for data type '{@link java.time.Duration <em>EDuration</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
     * <!-- begin-model-doc -->
     * An ISO-8601 duration, e.g. P30D or PT15M.
     * 
     * The create/convert bodies below are not decoration: EMF's default reflective conversion cannot build a java.time.Duration from a literal (it has no valueOf(String)), so without them every deployment XMI carrying a duration fails to load with "The value 'P30D' is invalid".
     * <!-- end-model-doc -->
	 * @return the meta object for data type '<em>EDuration</em>'.
	 * @see java.time.Duration
	 * @model instanceClass="java.time.Duration"
	 * @generated
	 */
	EDataType getEDuration();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	DeploymentFactory getDeploymentFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl <em>Event Atlas Deployment</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.EventAtlasDeploymentImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getEventAtlasDeployment()
		 * @generated
		 */
		EClass EVENT_ATLAS_DEPLOYMENT = eINSTANCE.getEventAtlasDeployment();

		/**
		 * The meta object literal for the '<em><b>Deployment Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EVENT_ATLAS_DEPLOYMENT__DEPLOYMENT_ID = eINSTANCE.getEventAtlasDeployment_DeploymentId();

		/**
		 * The meta object literal for the '<em><b>Description</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute EVENT_ATLAS_DEPLOYMENT__DESCRIPTION = eINSTANCE.getEventAtlasDeployment_Description();

		/**
		 * The meta object literal for the '<em><b>Http</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EVENT_ATLAS_DEPLOYMENT__HTTP = eINSTANCE.getEventAtlasDeployment_Http();

		/**
		 * The meta object literal for the '<em><b>Atlas</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EVENT_ATLAS_DEPLOYMENT__ATLAS = eINSTANCE.getEventAtlasDeployment_Atlas();

		/**
		 * The meta object literal for the '<em><b>Brokers</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EVENT_ATLAS_DEPLOYMENT__BROKERS = eINSTANCE.getEventAtlasDeployment_Brokers();

		/**
		 * The meta object literal for the '<em><b>Channels</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EVENT_ATLAS_DEPLOYMENT__CHANNELS = eINSTANCE.getEventAtlasDeployment_Channels();

		/**
		 * The meta object literal for the '<em><b>History</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EVENT_ATLAS_DEPLOYMENT__HISTORY = eINSTANCE.getEventAtlasDeployment_History();

		/**
		 * The meta object literal for the '<em><b>Inference</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference EVENT_ATLAS_DEPLOYMENT__INFERENCE = eINSTANCE.getEventAtlasDeployment_Inference();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HttpEndpointImpl <em>Http Endpoint</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.HttpEndpointImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getHttpEndpoint()
		 * @generated
		 */
		EClass HTTP_ENDPOINT = eINSTANCE.getHttpEndpoint();

		/**
		 * The meta object literal for the '<em><b>Port</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HTTP_ENDPOINT__PORT = eINSTANCE.getHttpEndpoint_Port();

		/**
		 * The meta object literal for the '<em><b>Host</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HTTP_ENDPOINT__HOST = eINSTANCE.getHttpEndpoint_Host();

		/**
		 * The meta object literal for the '<em><b>Context Path</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HTTP_ENDPOINT__CONTEXT_PATH = eINSTANCE.getHttpEndpoint_ContextPath();

		/**
		 * The meta object literal for the '<em><b>Rest Path</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HTTP_ENDPOINT__REST_PATH = eINSTANCE.getHttpEndpoint_RestPath();

		/**
		 * The meta object literal for the '<em><b>Whiteboard Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HTTP_ENDPOINT__WHITEBOARD_NAME = eINSTANCE.getHttpEndpoint_WhiteboardName();

		/**
		 * The meta object literal for the '<em><b>Allow Anonymous</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HTTP_ENDPOINT__ALLOW_ANONYMOUS = eINSTANCE.getHttpEndpoint_AllowAnonymous();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl <em>Atlas Binding</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.AtlasBindingImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getAtlasBinding()
		 * @generated
		 */
		EClass ATLAS_BINDING = eINSTANCE.getAtlasBinding();

		/**
		 * The meta object literal for the '<em><b>Base Uri</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATLAS_BINDING__BASE_URI = eINSTANCE.getAtlasBinding_BaseUri();

		/**
		 * The meta object literal for the '<em><b>Scope</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATLAS_BINDING__SCOPE = eINSTANCE.getAtlasBinding_Scope();

		/**
		 * The meta object literal for the '<em><b>Stage</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATLAS_BINDING__STAGE = eINSTANCE.getAtlasBinding_Stage();

		/**
		 * The meta object literal for the '<em><b>Mapping Registries</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATLAS_BINDING__MAPPING_REGISTRIES = eINSTANCE.getAtlasBinding_MappingRegistries();

		/**
		 * The meta object literal for the '<em><b>Prefetch Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATLAS_BINDING__PREFETCH_MODE = eINSTANCE.getAtlasBinding_PrefetchMode();

		/**
		 * The meta object literal for the '<em><b>Required Ns Uris</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATLAS_BINDING__REQUIRED_NS_URIS = eINSTANCE.getAtlasBinding_RequiredNsUris();

		/**
		 * The meta object literal for the '<em><b>Drift Check Interval Ms</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATLAS_BINDING__DRIFT_CHECK_INTERVAL_MS = eINSTANCE.getAtlasBinding_DriftCheckIntervalMs();

		/**
		 * The meta object literal for the '<em><b>Refresh Interval Ms</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute ATLAS_BINDING__REFRESH_INTERVAL_MS = eINSTANCE.getAtlasBinding_RefreshIntervalMs();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.MqttBrokerImpl <em>Mqtt Broker</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.MqttBrokerImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getMqttBroker()
		 * @generated
		 */
		EClass MQTT_BROKER = eINSTANCE.getMqttBroker();

		/**
		 * The meta object literal for the '<em><b>Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MQTT_BROKER__ID = eINSTANCE.getMqttBroker_Id();

		/**
		 * The meta object literal for the '<em><b>Protocol</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MQTT_BROKER__PROTOCOL = eINSTANCE.getMqttBroker_Protocol();

		/**
		 * The meta object literal for the '<em><b>Host</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MQTT_BROKER__HOST = eINSTANCE.getMqttBroker_Host();

		/**
		 * The meta object literal for the '<em><b>Port</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MQTT_BROKER__PORT = eINSTANCE.getMqttBroker_Port();

		/**
		 * The meta object literal for the '<em><b>User</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MQTT_BROKER__USER = eINSTANCE.getMqttBroker_User();

		/**
		 * The meta object literal for the '<em><b>Password</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MQTT_BROKER__PASSWORD = eINSTANCE.getMqttBroker_Password();

		/**
		 * The meta object literal for the '<em><b>Topics</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute MQTT_BROKER__TOPICS = eINSTANCE.getMqttBroker_Topics();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.IngestChannelImpl <em>Ingest Channel</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.IngestChannelImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getIngestChannel()
		 * @generated
		 */
		EClass INGEST_CHANNEL = eINSTANCE.getIngestChannel();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INGEST_CHANNEL__NAME = eINSTANCE.getIngestChannel_Name();

		/**
		 * The meta object literal for the '<em><b>Transport</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INGEST_CHANNEL__TRANSPORT = eINSTANCE.getIngestChannel_Transport();

		/**
		 * The meta object literal for the '<em><b>Format</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INGEST_CHANNEL__FORMAT = eINSTANCE.getIngestChannel_Format();

		/**
		 * The meta object literal for the '<em><b>Broker Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INGEST_CHANNEL__BROKER_ID = eINSTANCE.getIngestChannel_BrokerId();

		/**
		 * The meta object literal for the '<em><b>Topics</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INGEST_CHANNEL__TOPICS = eINSTANCE.getIngestChannel_Topics();

		/**
		 * The meta object literal for the '<em><b>Codec Type Map Id</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INGEST_CHANNEL__CODEC_TYPE_MAP_ID = eINSTANCE.getIngestChannel_CodecTypeMapId();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryConfigImpl <em>History Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryConfigImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getHistoryConfig()
		 * @generated
		 */
		EClass HISTORY_CONFIG = eINSTANCE.getHistoryConfig();

		/**
		 * The meta object literal for the '<em><b>Provider Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORY_CONFIG__PROVIDER_NAME = eINSTANCE.getHistoryConfig_ProviderName();

		/**
		 * The meta object literal for the '<em><b>Storage</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HISTORY_CONFIG__STORAGE = eINSTANCE.getHistoryConfig_Storage();

		/**
		 * The meta object literal for the '<em><b>Filters</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HISTORY_CONFIG__FILTERS = eINSTANCE.getHistoryConfig_Filters();

		/**
		 * The meta object literal for the '<em><b>Housekeeping</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference HISTORY_CONFIG__HOUSEKEEPING = eINSTANCE.getHistoryConfig_Housekeeping();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryStorageImpl <em>History Storage</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.HistoryStorageImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getHistoryStorage()
		 * @generated
		 */
		EClass HISTORY_STORAGE = eINSTANCE.getHistoryStorage();

		/**
		 * The meta object literal for the '<em><b>Max Page Size</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORY_STORAGE__MAX_PAGE_SIZE = eINSTANCE.getHistoryStorage_MaxPageSize();

		/**
		 * The meta object literal for the '<em><b>Include Resources</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORY_STORAGE__INCLUDE_RESOURCES = eINSTANCE.getHistoryStorage_IncludeResources();

		/**
		 * The meta object literal for the '<em><b>Exclude Resources</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORY_STORAGE__EXCLUDE_RESOURCES = eINSTANCE.getHistoryStorage_ExcludeResources();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.TimescaleStorageImpl <em>Timescale Storage</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.TimescaleStorageImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getTimescaleStorage()
		 * @generated
		 */
		EClass TIMESCALE_STORAGE = eINSTANCE.getTimescaleStorage();

		/**
		 * The meta object literal for the '<em><b>Url</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIMESCALE_STORAGE__URL = eINSTANCE.getTimescaleStorage_Url();

		/**
		 * The meta object literal for the '<em><b>Host</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIMESCALE_STORAGE__HOST = eINSTANCE.getTimescaleStorage_Host();

		/**
		 * The meta object literal for the '<em><b>Port</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIMESCALE_STORAGE__PORT = eINSTANCE.getTimescaleStorage_Port();

		/**
		 * The meta object literal for the '<em><b>Database</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIMESCALE_STORAGE__DATABASE = eINSTANCE.getTimescaleStorage_Database();

		/**
		 * The meta object literal for the '<em><b>User</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIMESCALE_STORAGE__USER = eINSTANCE.getTimescaleStorage_User();

		/**
		 * The meta object literal for the '<em><b>Password</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TIMESCALE_STORAGE__PASSWORD = eINSTANCE.getTimescaleStorage_Password();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.InMemoryStorageImpl <em>In Memory Storage</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.InMemoryStorageImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getInMemoryStorage()
		 * @generated
		 */
		EClass IN_MEMORY_STORAGE = eINSTANCE.getInMemoryStorage();

		/**
		 * The meta object literal for the '<em><b>Max Values Per Resource</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute IN_MEMORY_STORAGE__MAX_VALUES_PER_RESOURCE = eINSTANCE.getInMemoryStorage_MaxValuesPerResource();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl <em>Historization Filter</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.HistorizationFilterImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getHistorizationFilter()
		 * @generated
		 */
		EClass HISTORIZATION_FILTER = eINSTANCE.getHistorizationFilter();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORIZATION_FILTER__NAME = eINSTANCE.getHistorizationFilter_Name();

		/**
		 * The meta object literal for the '<em><b>Targets</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORIZATION_FILTER__TARGETS = eINSTANCE.getHistorizationFilter_Targets();

		/**
		 * The meta object literal for the '<em><b>Include Resources</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORIZATION_FILTER__INCLUDE_RESOURCES = eINSTANCE.getHistorizationFilter_IncludeResources();

		/**
		 * The meta object literal for the '<em><b>Exclude Resources</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORIZATION_FILTER__EXCLUDE_RESOURCES = eINSTANCE.getHistorizationFilter_ExcludeResources();

		/**
		 * The meta object literal for the '<em><b>Change Mode</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORIZATION_FILTER__CHANGE_MODE = eINSTANCE.getHistorizationFilter_ChangeMode();

		/**
		 * The meta object literal for the '<em><b>Change Threshold</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORIZATION_FILTER__CHANGE_THRESHOLD = eINSTANCE.getHistorizationFilter_ChangeThreshold();

		/**
		 * The meta object literal for the '<em><b>Change Threshold Percent</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORIZATION_FILTER__CHANGE_THRESHOLD_PERCENT = eINSTANCE.getHistorizationFilter_ChangeThresholdPercent();

		/**
		 * The meta object literal for the '<em><b>Change Max Interval</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HISTORIZATION_FILTER__CHANGE_MAX_INTERVAL = eINSTANCE.getHistorizationFilter_ChangeMaxInterval();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.HousekeepingPolicyImpl <em>Housekeeping Policy</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.HousekeepingPolicyImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getHousekeepingPolicy()
		 * @generated
		 */
		EClass HOUSEKEEPING_POLICY = eINSTANCE.getHousekeepingPolicy();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HOUSEKEEPING_POLICY__NAME = eINSTANCE.getHousekeepingPolicy_Name();

		/**
		 * The meta object literal for the '<em><b>Targets</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HOUSEKEEPING_POLICY__TARGETS = eINSTANCE.getHousekeepingPolicy_Targets();

		/**
		 * The meta object literal for the '<em><b>Retention Period</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HOUSEKEEPING_POLICY__RETENTION_PERIOD = eINSTANCE.getHousekeepingPolicy_RetentionPeriod();

		/**
		 * The meta object literal for the '<em><b>Keep Count</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HOUSEKEEPING_POLICY__KEEP_COUNT = eINSTANCE.getHousekeepingPolicy_KeepCount();

		/**
		 * The meta object literal for the '<em><b>Max Delete</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HOUSEKEEPING_POLICY__MAX_DELETE = eINSTANCE.getHousekeepingPolicy_MaxDelete();

		/**
		 * The meta object literal for the '<em><b>Schedule Period</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute HOUSEKEEPING_POLICY__SCHEDULE_PERIOD = eINSTANCE.getHousekeepingPolicy_SchedulePeriod();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.impl.InferenceConfigImpl <em>Inference Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.InferenceConfigImpl
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getInferenceConfig()
		 * @generated
		 */
		EClass INFERENCE_CONFIG = eINSTANCE.getInferenceConfig();

		/**
		 * The meta object literal for the '<em><b>Sampling Enabled</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INFERENCE_CONFIG__SAMPLING_ENABLED = eINSTANCE.getInferenceConfig_SamplingEnabled();

		/**
		 * The meta object literal for the '<em><b>Namespace</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INFERENCE_CONFIG__NAMESPACE = eINSTANCE.getInferenceConfig_Namespace();

		/**
		 * The meta object literal for the '<em><b>Max Runs Per Interval</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute INFERENCE_CONFIG__MAX_RUNS_PER_INTERVAL = eINSTANCE.getInferenceConfig_MaxRunsPerInterval();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode <em>Prefetch Mode</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.PrefetchMode
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getPrefetchMode()
		 * @generated
		 */
		EEnum PREFETCH_MODE = eINSTANCE.getPrefetchMode();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport <em>Channel Transport</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.ChannelTransport
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getChannelTransport()
		 * @generated
		 */
		EEnum CHANNEL_TRANSPORT = eINSTANCE.getChannelTransport();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat <em>Payload Format</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.PayloadFormat
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getPayloadFormat()
		 * @generated
		 */
		EEnum PAYLOAD_FORMAT = eINSTANCE.getPayloadFormat();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.event.atlas.model.deployment.ChangeMode <em>Change Mode</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.event.atlas.model.deployment.ChangeMode
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getChangeMode()
		 * @generated
		 */
		EEnum CHANGE_MODE = eINSTANCE.getChangeMode();

		/**
		 * The meta object literal for the '<em>EDuration</em>' data type.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see java.time.Duration
		 * @see org.eclipse.fennec.event.atlas.model.deployment.impl.DeploymentPackageImpl#getEDuration()
		 * @generated
		 */
		EDataType EDURATION = eINSTANCE.getEDuration();

	}

} //DeploymentPackage
