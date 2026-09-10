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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Timescale Storage</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * The PostgreSQL/TimescaleDB backend. The TimescaleDB extension is optional - with it the history table becomes a hypertable, on plain PostgreSQL 14+ the provider falls back to date_bin. PostGIS is not required.
 * 
 * Its component requires a configuration, so declaring this section is what switches history on.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getUrl <em>Url</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getHost <em>Host</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getPort <em>Port</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getDatabase <em>Database</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getUser <em>User</em>}</li>
 *   <li>{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getPassword <em>Password</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getTimescaleStorage()
 * @model
 * @generated
 */
@ProviderType
public interface TimescaleStorage extends HistoryStorage {
	/**
	 * Returns the value of the '<em><b>Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Full JDBC URL. Set it to override host/port/database, which are otherwise composed into one.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Url</em>' attribute.
	 * @see #setUrl(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getTimescaleStorage_Url()
	 * @model
	 * @generated
	 */
	String getUrl();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getUrl <em>Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Url</em>' attribute.
	 * @see #getUrl()
	 * @generated
	 */
	void setUrl(String value);

	/**
	 * Returns the value of the '<em><b>Host</b></em>' attribute.
	 * The default value is <code>"localhost"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Host</em>' attribute.
	 * @see #setHost(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getTimescaleStorage_Host()
	 * @model default="localhost"
	 * @generated
	 */
	String getHost();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getHost <em>Host</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Host</em>' attribute.
	 * @see #getHost()
	 * @generated
	 */
	void setHost(String value);

	/**
	 * Returns the value of the '<em><b>Port</b></em>' attribute.
	 * The default value is <code>"5432"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Port</em>' attribute.
	 * @see #setPort(int)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getTimescaleStorage_Port()
	 * @model default="5432"
	 * @generated
	 */
	int getPort();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getPort <em>Port</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Port</em>' attribute.
	 * @see #getPort()
	 * @generated
	 */
	void setPort(int value);

	/**
	 * Returns the value of the '<em><b>Database</b></em>' attribute.
	 * The default value is <code>"sensinactHistory"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Database</em>' attribute.
	 * @see #setDatabase(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getTimescaleStorage_Database()
	 * @model default="sensinactHistory"
	 * @generated
	 */
	String getDatabase();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getDatabase <em>Database</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Database</em>' attribute.
	 * @see #getDatabase()
	 * @generated
	 */
	void setDatabase(String value);

	/**
	 * Returns the value of the '<em><b>User</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>User</em>' attribute.
	 * @see #setUser(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getTimescaleStorage_User()
	 * @model
	 * @generated
	 */
	String getUser();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getUser <em>User</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>User</em>' attribute.
	 * @see #getUser()
	 * @generated
	 */
	void setUser(String value);

	/**
	 * Returns the value of the '<em><b>Password</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Written to the private ConfigAdmin property '.password'. Prefer supplying it through the environment.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Password</em>' attribute.
	 * @see #setPassword(String)
	 * @see org.eclipse.fennec.event.atlas.model.deployment.DeploymentPackage#getTimescaleStorage_Password()
	 * @model
	 * @generated
	 */
	String getPassword();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.event.atlas.model.deployment.TimescaleStorage#getPassword <em>Password</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Password</em>' attribute.
	 * @see #getPassword()
	 * @generated
	 */
	void setPassword(String value);

} // TimescaleStorage
