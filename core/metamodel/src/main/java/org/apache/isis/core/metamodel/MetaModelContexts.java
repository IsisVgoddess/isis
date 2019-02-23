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
package org.apache.isis.core.metamodel;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.applib.services.repository.RepositoryService;
import org.apache.isis.applib.services.title.TitleService;
import org.apache.isis.applib.services.xactn.TransactionService;
import org.apache.isis.applib.services.xactn.TransactionState;
import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.cdi._CDI;
import org.apache.isis.config.IsisConfiguration;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.commons.ensure.Assert;
import org.apache.isis.core.metamodel.adapter.ObjectAdapter;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.services.ServiceUtil;
import org.apache.isis.core.metamodel.services.persistsession.ObjectAdapterService;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.security.authentication.manager.AuthenticationManager;
import org.apache.isis.core.security.authorization.manager.AuthorizationManager;

import lombok.Getter;
import lombok.val;

/**
 * 
 * @since 2.0.0-M2
 *
 */
public final class MetaModelContexts {

    static class MetaModelContextUsingCDI implements MetaModelContext {

        @Getter(lazy=true) 
        private final IsisConfiguration configuration = 
        _Config.getConfiguration();

        @Getter(lazy=true) 
        private final ObjectAdapterProvider objectAdapterProvider =
        _CDI.getSingleton(ObjectAdapterProvider.class);

        @Getter(lazy=true) 
        private final ServiceInjector serviceInjector =
        _CDI.getSingleton(ServiceInjector.class);

        @Getter(lazy=true) 
        private final ServiceRegistry serviceRegistry =
        _CDI.getSingleton(ServiceRegistry.class);

        @Getter(lazy=true) 
        private final SpecificationLoader specificationLoader = 
        _CDI.getSingleton(SpecificationLoader.class);

        @Getter(lazy=true) 
        private final AuthenticationSessionProvider authenticationSessionProvider =
        _CDI.getSingleton(AuthenticationSessionProvider.class);

        @Getter(lazy=true) 
        private final TranslationService translationService =
        _CDI.getSingleton(TranslationService.class);

        @Getter(lazy=true) 
        private final AuthorizationManager authorizationManager =
        _CDI.getSingleton(AuthorizationManager.class); 

        @Getter(lazy=true) 
        private final AuthenticationManager authenticationManager =
        _CDI.getSingleton(AuthenticationManager.class);

        @Getter(lazy=true) 
        private final TitleService titleService =
        _CDI.getSingleton(TitleService.class);

        @Getter(lazy=true) 
        private final ObjectAdapterService objectAdapterService =
        _CDI.getSingleton(ObjectAdapterService.class);

        @Getter(lazy=true) 
        private final RepositoryService repositoryService =
        _CDI.getSingleton(RepositoryService.class);
        
        @Getter(lazy=true) 
        private final TransactionService transactionService =
        _CDI.getSingleton(TransactionService.class);
        

        @Override
        public final AuthenticationSession getAuthenticationSession() {
            return getAuthenticationSessionProvider().getAuthenticationSession();
        }

        @Override
        public final ObjectSpecification getSpecification(final Class<?> type) {
            return type != null ? getSpecificationLoader().loadSpecification(type) : null;
        }

        @Override
        public final TransactionState getTransactionState() {
            return getTransactionService().getTransactionState();
        }
        
        // -- SERVICE SUPPORT
        
        @Override
        public Stream<ObjectAdapter> streamServiceAdapters() {
            return serviceAdapters.get().values().stream();
        }
        
        @Override
        public ObjectAdapter lookupServiceAdapterById(final String serviceId) {
            return serviceAdapters.get().get(serviceId);
        }
        
        
        // -- HELPER
        
        private final _Lazy<Map<String, ObjectAdapter>> serviceAdapters = _Lazy.of(this::initServiceAdapters);
        
        private Map<String, ObjectAdapter> initServiceAdapters() {
        	
        	val objectAdapterProvider = getObjectAdapterProvider();
        	
            return getServiceRegistry().streamServices()
            .map(objectAdapterProvider::adapterFor) 
            .peek(serviceAdapter->{
                Assert.assertFalse("expected to not be 'transient'", serviceAdapter.getOid().isTransient());
            })
            .collect(Collectors.toMap(ServiceUtil::idOfAdapter, v->v, (o,n)->n, LinkedHashMap::new));
        }

        // -------------------------------------------------------------------------------


    }

    final static MetaModelContext usingCDI() {
        return new MetaModelContextUsingCDI();
    }

}
