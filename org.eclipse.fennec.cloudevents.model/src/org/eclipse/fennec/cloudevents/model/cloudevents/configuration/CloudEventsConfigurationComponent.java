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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.cloudevents.model.cloudevents.configuration;

import java.util.Hashtable;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.resource.Resource.Factory;

import org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsFactory;
import org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage;

import org.eclipse.fennec.cloudevents.model.cloudevents.impl.CloudEventsPackageImpl;

import org.eclipse.fennec.cloudevents.model.cloudevents.util.CloudEventsResourceFactoryImpl;

import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;

import org.osgi.annotation.bundle.Capability;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;
import org.osgi.framework.ServiceRegistration;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;

import org.osgi.service.condition.Condition;
/**
 * The <b>PackageConfiguration</b> for the model.
 * The package will be registered into a OSGi base model registry.
 * 
 * @generated
 */
@Component(name = "CloudEventsConfigurator")
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"org.eclipse.fennec.cloudevents.model.cloudevents.util.CloudEventsResourceFactoryImpl, org.eclipse.emf.ecore.resource.Resource$Factory\"" , "uses:=\"org.eclipse.emf.ecore.resource,org.eclipse.fennec.cloudevents.model.cloudevents.util\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsFactory, org.eclipse.emf.ecore.EFactory\"" , "uses:=\"org.eclipse.emf.ecore,org.eclipse.fennec.cloudevents.model.cloudevents\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"org.eclipse.fennec.cloudevents.model.cloudevents.CloudEventsPackage, org.eclipse.emf.ecore.EPackage\"" , "uses:=\"org.eclipse.emf.ecore,org.eclipse.fennec.cloudevents.model.cloudevents\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator\"" , "uses:=\"org.eclipse.emf.ecore,org.eclipse.fennec.cloudevents.model.cloudevents\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"org.osgi.service.condition.Condition\"" , "uses:=org.osgi.service.condition" })
public class CloudEventsConfigurationComponent {
	
	private ServiceRegistration<?> packageRegistration = null;
	private ServiceRegistration<EPackageConfigurator> ePackageConfiguratorRegistration = null;
	private ServiceRegistration<?> eFactoryRegistration = null;
	private ServiceRegistration<?> conditionRegistration = null;
	private ServiceRegistration<?> resourceFactoryRegistration = null;

	/**
	 * Activates the Configuration Component.
	 *
	 * @generated
	 */
	@Activate
	public void activate(BundleContext ctx) {
	
		checkEMFEcore(ctx);
		CloudEventsPackage ePackage = CloudEventsPackageImpl.eINSTANCE;
		
		if(!EPackage.Registry.INSTANCE.containsKey(CloudEventsPackage.eNS_URI)){
			EPackage.Registry.INSTANCE.put(CloudEventsPackage.eNS_URI, ePackage);
		}
		
		CloudEventsEPackageConfigurator packageConfigurator = registerEPackageConfiguratorService(ePackage, ctx);
		registerResourceFactoryService(ctx);
		registerEPackageService(ePackage, packageConfigurator, ctx);
		registerEFactoryService(ePackage, packageConfigurator, ctx);
		registerConditionService(packageConfigurator, ctx);
	}
	
	/**
	 * We have to make sure that org.eclipse.emf.ecore is started, so we don't run 
	 * into start order issues due to the use of static access in EMF 
	 * @param ctx the {@link BundleContext} to use
	 */
	private void checkEMFEcore(BundleContext ctx) {
		Bundle[] bundles = ctx.getBundles();
		
		for(Bundle bundle : bundles) {
			if("org.eclipse.emf.ecore".equals(bundle.getSymbolicName())) {
				try {
					bundle.start();
				} catch (BundleException e) {
					System.getLogger(getClass().getName()).log(System.Logger.Level.ERROR, "Could not start Bundle org.eclipse.emf.ecore, something seems seriously wrong", e);
				}
				break;
			}
		}
	}
	
	/**
	 * Registers the CloudEventsEPackageConfigurator as a service.
	 *
	 * @generated
	 */
	private CloudEventsEPackageConfigurator registerEPackageConfiguratorService(CloudEventsPackage ePackage, BundleContext ctx){
		CloudEventsEPackageConfigurator packageConfigurator = new CloudEventsEPackageConfigurator(ePackage);
		// register the EPackageConfigurator
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		ePackageConfiguratorRegistration = ctx.registerService(EPackageConfigurator.class, packageConfigurator, properties);

		return packageConfigurator;
	}

	/**
	 * Registers the CloudEventsResourceFactoryImpl as a service.
	 *
	 * @generated
	 */
	private void registerResourceFactoryService(BundleContext ctx){
		CloudEventsResourceFactoryImpl factory = new CloudEventsResourceFactoryImpl();
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(factory.getServiceProperties());
		String[] serviceClasses = new String[] {CloudEventsResourceFactoryImpl.class.getName(), Factory.class.getName()};
		resourceFactoryRegistration = ctx.registerService(serviceClasses, factory, properties);
	}

	/**
	 * Registers the CloudEventsPackage as a service.
	 *
	 * @generated
	 */
	private void registerEPackageService(CloudEventsPackage ePackage, CloudEventsEPackageConfigurator packageConfigurator, BundleContext ctx){
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		String[] serviceClasses = new String[] {CloudEventsPackage.class.getName(), EPackage.class.getName()};
		packageRegistration = ctx.registerService(serviceClasses, ePackage, properties);
	}

	/**
	 * Registers the CloudEventsFactory as a service.
	 *
	 * @generated
	 */
	private void registerEFactoryService(CloudEventsPackage ePackage, CloudEventsEPackageConfigurator packageConfigurator, BundleContext ctx){
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		String[] serviceClasses = new String[] {CloudEventsFactory.class.getName(), EFactory.class.getName()};
		eFactoryRegistration = ctx.registerService(serviceClasses, ePackage.getCloudEventsFactory(), properties);
	}

	private void registerConditionService(CloudEventsEPackageConfigurator packageConfigurator, BundleContext ctx){
		// register the EPackage
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		properties.put(Condition.CONDITION_ID, CloudEventsPackage.eNS_URI);
		conditionRegistration = ctx.registerService(Condition.class, Condition.INSTANCE, properties);
	}

	/**
	 * Deactivates and unregisters everything.
	 *
	 * @generated
	 */
	@Deactivate
	public void deactivate() {
		conditionRegistration.unregister();
		eFactoryRegistration.unregister();
		packageRegistration.unregister();
		resourceFactoryRegistration.unregister();

		ePackageConfiguratorRegistration.unregister();
		EPackage.Registry.INSTANCE.remove(CloudEventsPackage.eNS_URI);
	}
}
