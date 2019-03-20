/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.isis.core.plugins.ioc.weld;

import static org.apache.isis.commons.internal.base._NullSafe.stream;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.collections._Arrays;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.debug._Probe;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;

/**
 * @since 2.0.0-M2
 */
public class WeldFactory {
    
	private final static _Probe probe = _Probe.unlimited().label("WeldFactory");
	
    public static int invokeCount = 0;

	public static WeldContainer newContainer(@Nullable Stream<Class<?>> discover) {
		return newContainer(null, discover);
	}
	
	public static WeldContainer newContainer(@Nullable String containerId, @Nullable Stream<Class<?>> discover) {
	    
	    final Class<?>[] classes = stream(discover)
	            .collect(_Arrays.toArray(Class.class));
	    
	    //final Class<?> beanScanInterceptor = classForName("org.apache.isis.core.webapp.jee.IsisCDIBeanScanInterceptor");
	    final Class<?>[] additionalPackages = 
	            classesForClassNames(
	            		
	                    //FIXME [2033] this is just for PoC, don't hardcode this here !? ...
	                    
	            		"org.apache.isis.core.plugins.ioc.weld.WeldFactory",
	            		"org.jboss.seam.conversation.spi.SeamConversationContext",
	            		
	            		"org.jboss.weld.module.web.WeldWebModule",
	                    
	                    "org.apache.isis.core.wrapper.WrapperFactoryDefault",
	                    "org.apache.isis.viewer.wicket.viewer.IsisWicketModule",
//	                    "org.apache.isis.applib.services.jdosupport.IsisJdoSupportDN5",
//	                    "org.apache.isis.jdo.datanucleus.persistence.IsisLegacyJdoContextHandler",
	                    
	                    "org.apache.wicket.cdi.AutoConversation",
	                    "org.apache.isis.viewer.restfulobjects.rendering.RendererContext"
	                    
	                    
	                    );
	    
	    stream(classes)
	    .forEach(c->probe.println("scanning class %s", c));
	    stream(additionalPackages)
	    .forEach(p->probe.println("scanning additionalPackage %s", p));
	    
	    System.setProperty("seam.conversation.disable.noop", Boolean.TRUE.toString());
	    
        boolean scanRecursively = true;
        
        Weld builder = new Weld()
	            .disableDiscovery()
	            .addPackages(scanRecursively, classes)
	            .addPackages(scanRecursively, additionalPackages)
	            //.interceptors(beanScanInterceptor)
//	            .property("org.jboss.weld.construction.relaxed", true)
//	            .property("org.jboss.weld.development", true)
//	            .property("org.jboss.weld.probe.jmxSupport", true)
//	            .property("org.jboss.weld.probe.exportDataAfterDeployment", "d:/tmp/")
	            ;
        
        
        if(!_Strings.isNullOrEmpty(containerId)) {
            builder = builder.containerId(containerId);
        }
        
        WeldContainer container = builder.initialize();
        
        return container;
        
	}
	
//	private static Package[] packagesForClassNames(String ... names) {
//	    final Package[] packages = stream(names)
//	            .map(WeldFactory::classForName)
//	            .map(Class::getPackage)
//                .collect(_Arrays.toArray(Package.class));
//	    return packages;
//	}
	
	private static Class<?>[] classesForClassNames(String ... names) {
        final Class<?>[] classes = stream(names)
                .map(WeldFactory::classForName)
                .collect(_Arrays.toArray(Class.class));
        return classes;
    }
	
	private static Class<?> classForName(String name) {
        final Class<?> cls;
        try {
            cls = _Context.loadClassAndInitialize(name);
        } catch (ClassNotFoundException e) {
            throw _Exceptions.unrecoverable(e);
        }
        return cls;
    }
	
}
