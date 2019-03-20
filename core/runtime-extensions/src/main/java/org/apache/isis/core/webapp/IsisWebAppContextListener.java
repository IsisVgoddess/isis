/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.apache.isis.core.webapp;

import static org.apache.isis.commons.internal.base._With.acceptIfPresent;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebListener;

import org.apache.isis.commons.internal.base._Blackhole;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.commons.internal.resources._Resources;
import org.apache.isis.config.AppConfigLocator;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.core.runtime.system.context.IsisContext;
import org.apache.isis.core.webapp.modules.WebModule;
import org.apache.isis.core.webapp.modules.WebModuleContext;

import lombok.val;
import lombok.extern.slf4j.Slf4j;

/**
 * 
 * Introduced to render web.xml Filter/Listener/Servlet configurations obsolete.
 * <p> 
 * Acts as the single application entry-point for setting up the 
 * ServletContext programmatically.
 * </p><p> 
 * Installs {@link WebModule}s on the ServletContext. 
 * </p>   
 *  
 * @since 2.0.0-M2
 *
 */
@WebListener //[ahuber] to support Servlet 3.0 annotations @WebFilter, @WebListener or others 
//with skinny war deployment requires additional configuration, so for now we disable this annotation
// ... but enabled for now, because we are running into this issue
// https://stackoverflow.com/questions/44389716/spring-boot-embedded-tomcat-weblistener-scanned-by-servletcomponentscan
@Slf4j
public class IsisWebAppContextListener implements ServletContextListener {
    
    private final List<ServletContextListener> activeListeners = new ArrayList<>();

    // -- INTERFACE IMPLEMENTATION
    
    @Override
    public void contextInitialized(ServletContextEvent event) {

        val servletContext = event.getServletContext();
        
        // set the ServletContext initializing thread as preliminary default until overridden by
        // IsisWicketApplication#init() or others that better know what ClassLoader to use as application default.
        _Context.setDefaultClassLoader(Thread.currentThread().getContextClassLoader(), false);
        _Context.putSingleton(ServletContext.class, servletContext);
        _Resources.putContextPathIfPresent(servletContext.getContextPath());
        
        log.info("=== PHASE 1/3 === Setting up embedded CDI");
        
        // finalize the config (build and regard immutable)
        // as a side-effect bootstrap CDI, if the environment we are running on does not already have its own 
        _Blackhole.consume(AppConfigLocator.getAppConfig());
        
        // expected post-condition: a BeanManager should be available
        _Blackhole.consume(_CDI.getBeanManager());
        
        log.info("=== PHASE 2/3 === Preparing the ServletContext");
        
        val webModuleContext = new WebModuleContext(servletContext);
        
        final List<WebModule> webModules =
                 WebModule.discoverWebModules()
                 .peek(module->module.prepare(webModuleContext)) // prepare context
                 .collect(Collectors.toList());

        log.info("=== PHASE 3/3 === Initializing the ServletContext");
        
        webModules.stream()
        .filter(module->module.isApplicable(webModuleContext)) // filter those WebModules that are applicable
        .forEach(module->addListener(servletContext, module));
        
        activeListeners.forEach(listener->listener.contextInitialized(event));
        
        log.info("=== DONE === ServletContext initialized.");
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {
        log.info("=== SHUTDOWN === Shutting down ServletContext");
        activeListeners.forEach(listener->shutdownListener(event, listener));
        activeListeners.clear();
        IsisContext.clear();
        log.info("=== DESTROYED === ServletContext destroyed");
    }
    
    // -- HELPER
    
    private void addListener(ServletContext context, WebModule module) {
        log.info(String.format("Setup ServletContext, adding WebModule '%s'", module.getName()));
        try {
            acceptIfPresent(module.init(context), activeListeners::add);
        } catch (ServletException e) {
            log.error(String.format("Failed to add WebModule '%s' to the ServletContext.", module.getName()), e);
        }  
    }
    
    private void shutdownListener(ServletContextEvent event, ServletContextListener listener) {
        try {
            listener.contextDestroyed(event);
        } catch (Exception e) {
            log.error(String.format("Failed to shutdown WebListener '%s'.", listener.getClass().getName()), e);
        }
    }

}
