/*
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package probeadmin.configuration;

import java.util.Hashtable;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.resource.Resource.Factory;

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

import probeadmin.ProbeadminFactory;
import probeadmin.ProbeadminPackage;

import probeadmin.impl.ProbeadminPackageImpl;

import probeadmin.util.ProbeadminResourceFactoryImpl;
/**
 * The <b>PackageConfiguration</b> for the model.
 * The package will be registered into a OSGi base model registry.
 * 
 * @generated
 */
@Component(name = "ProbeadminConfigurator")
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"probeadmin.util.ProbeadminResourceFactoryImpl, org.eclipse.emf.ecore.resource.Resource$Factory\"" , "uses:=\"org.eclipse.emf.ecore.resource,probeadmin.util\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"probeadmin.ProbeadminFactory, org.eclipse.emf.ecore.EFactory\"" , "uses:=\"org.eclipse.emf.ecore,probeadmin\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"probeadmin.ProbeadminPackage, org.eclipse.emf.ecore.EPackage\"" , "uses:=\"org.eclipse.emf.ecore,probeadmin\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator\"" , "uses:=\"org.eclipse.emf.ecore,probeadmin\"" })
@Capability( namespace = "osgi.service", attribute = { "objectClass:List<String>=\"org.osgi.service.condition.Condition\"" , "uses:=org.osgi.service.condition" })
public class ProbeadminConfigurationComponent {
	
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
		ProbeadminPackage ePackage = ProbeadminPackageImpl.eINSTANCE;
		
		if(!EPackage.Registry.INSTANCE.containsKey(ProbeadminPackage.eNS_URI)){
			EPackage.Registry.INSTANCE.put(ProbeadminPackage.eNS_URI, ePackage);
		}
		
		ProbeadminEPackageConfigurator packageConfigurator = registerEPackageConfiguratorService(ePackage, ctx);
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
	 * Registers the ProbeadminEPackageConfigurator as a service.
	 *
	 * @generated
	 */
	private ProbeadminEPackageConfigurator registerEPackageConfiguratorService(ProbeadminPackage ePackage, BundleContext ctx){
		ProbeadminEPackageConfigurator packageConfigurator = new ProbeadminEPackageConfigurator(ePackage);
		// register the EPackageConfigurator
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		ePackageConfiguratorRegistration = ctx.registerService(EPackageConfigurator.class, packageConfigurator, properties);

		return packageConfigurator;
	}

	/**
	 * Registers the ProbeadminResourceFactoryImpl as a service.
	 *
	 * @generated
	 */
	private void registerResourceFactoryService(BundleContext ctx){
		ProbeadminResourceFactoryImpl factory = new ProbeadminResourceFactoryImpl();
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(factory.getServiceProperties());
		String[] serviceClasses = new String[] {ProbeadminResourceFactoryImpl.class.getName(), Factory.class.getName()};
		resourceFactoryRegistration = ctx.registerService(serviceClasses, factory, properties);
	}

	/**
	 * Registers the ProbeadminPackage as a service.
	 *
	 * @generated
	 */
	private void registerEPackageService(ProbeadminPackage ePackage, ProbeadminEPackageConfigurator packageConfigurator, BundleContext ctx){
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		String[] serviceClasses = new String[] {ProbeadminPackage.class.getName(), EPackage.class.getName()};
		packageRegistration = ctx.registerService(serviceClasses, ePackage, properties);
	}

	/**
	 * Registers the ProbeadminFactory as a service.
	 *
	 * @generated
	 */
	private void registerEFactoryService(ProbeadminPackage ePackage, ProbeadminEPackageConfigurator packageConfigurator, BundleContext ctx){
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		String[] serviceClasses = new String[] {ProbeadminFactory.class.getName(), EFactory.class.getName()};
		eFactoryRegistration = ctx.registerService(serviceClasses, ePackage.getProbeadminFactory(), properties);
	}

	private void registerConditionService(ProbeadminEPackageConfigurator packageConfigurator, BundleContext ctx){
		// register the EPackage
		Hashtable<String, Object> properties = new Hashtable<String, Object>();
		properties.putAll(packageConfigurator.getServiceProperties());
		properties.put(Condition.CONDITION_ID, ProbeadminPackage.eNS_URI);
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
		EPackage.Registry.INSTANCE.remove(ProbeadminPackage.eNS_URI);
	}
}
