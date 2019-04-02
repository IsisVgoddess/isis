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

package org.apache.isis.core.metamodel.facets;

import java.lang.reflect.Method;
import java.util.List;

import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.junit.Rule;

import org.apache.isis.applib.Identifier;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.inject.ServiceInjector;
import org.apache.isis.applib.services.registry.ServiceRegistry;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.config.internal._Config;
import org.apache.isis.core.metamodel.MetaModelContext;
import org.apache.isis.core.metamodel.adapter.ObjectAdapterProvider;
import org.apache.isis.core.metamodel.facetapi.FacetHolder;
import org.apache.isis.core.metamodel.facetapi.FacetHolderImpl;
import org.apache.isis.core.metamodel.facetapi.FeatureType;
import org.apache.isis.core.metamodel.facetapi.IdentifiedHolder;
import org.apache.isis.core.metamodel.services.events.MetamodelEventService;
import org.apache.isis.core.metamodel.services.persistsession.ObjectAdapterService;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.SpecificationLoader;
import org.apache.isis.core.security.authentication.AuthenticationSession;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.core.unittestsupport.jmocking.JUnitRuleMockery2;

import junit.framework.TestCase;

public abstract class AbstractFacetFactoryTest extends TestCase {

    @Rule
    public JUnitRuleMockery2 context = JUnitRuleMockery2.createFor(JUnitRuleMockery2.Mode.INTERFACES_AND_CLASSES);

    public static class Customer {

        private String firstName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(final String firstName) {
            this.firstName = firstName;
        }
    }

    @Mock    protected MetamodelEventService mockMetamodelEventService;
    
    protected ObjectAdapterProvider mockObjectAdapterProvider;
    protected ServiceRegistry mockServiceRegistry;
    protected ServiceInjector mockServiceInjector;
    protected TranslationService mockTranslationService;
    protected AuthenticationSessionProvider mockAuthenticationSessionProvider;
    protected AuthenticationSession mockAuthenticationSession;

    protected ObjectAdapterService mockPersistenceSessionServiceInternal;

    protected SpecificationLoader mockSpecificationLoader;
    protected ProgrammableMethodRemover methodRemover;

    protected FacetHolder facetHolder;
    protected FacetedMethod facetedMethod;
    protected FacetedMethodParameter facetedMethodParameter;
    
    public static class IdentifiedHolderImpl extends FacetHolderImpl implements IdentifiedHolder {

        private Identifier identifier;

        public IdentifiedHolderImpl(final Identifier identifier) {
            this.identifier = identifier;
        }

        @Override
        public Identifier getIdentifier() {
            return identifier;
        }
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        _Context.clear();
        
        // PRODUCTION
        
        facetHolder = new IdentifiedHolderImpl(
                Identifier.propertyOrCollectionIdentifier(Customer.class, "firstName"));
        facetedMethod = FacetedMethod.createForProperty(Customer.class, "firstName");
        facetedMethodParameter = new FacetedMethodParameter(
                FeatureType.ACTION_PARAMETER_SCALAR, facetedMethod.getOwningType(), facetedMethod.getMethod(), String.class
        );

        methodRemover = new ProgrammableMethodRemover();

        mockAuthenticationSessionProvider = context.mock(AuthenticationSessionProvider.class);
        
        mockTranslationService = context.mock(TranslationService.class);
        mockAuthenticationSession = context.mock(AuthenticationSession.class);

        mockPersistenceSessionServiceInternal = context.mock(ObjectAdapterService.class);

        mockSpecificationLoader = context.mock(SpecificationLoader.class);
        
        mockServiceRegistry = context.mock(ServiceRegistry.class);
        mockServiceInjector = context.mock(ServiceInjector.class);
        mockObjectAdapterProvider = context.mock(ObjectAdapterProvider.class);
        
        context.checking(new Expectations() {{

            allowing(mockAuthenticationSessionProvider).getAuthenticationSession();
            will(returnValue(mockAuthenticationSession));
            
            allowing(mockServiceRegistry).lookupServiceElseFail(MetamodelEventService.class);
            will(returnValue(mockMetamodelEventService));
            
        }});
        
        MetaModelContext.preset(MetaModelContext.builder()
        		.configuration(_Config.getConfiguration())
        		.specificationLoader(mockSpecificationLoader)
        		.serviceRegistry(mockServiceRegistry)
        		.serviceInjector(mockServiceInjector)
        		.build());
    }



    protected void allowing_specificationLoader_loadSpecification_any_willReturn(final ObjectSpecification objectSpecification) {
        context.checking(new Expectations() {{
            allowing(mockSpecificationLoader).loadSpecification(with(any(Class.class)));
            will(returnValue(objectSpecification));
        }});
    }

    @Override
    protected void tearDown() throws Exception {
        mockSpecificationLoader = null;
        methodRemover = null;
        facetedMethod = null;
        super.tearDown();
    }

    protected static boolean contains(final Class<?>[] types, final Class<?> type) {
        return Utils.contains(types, type);
    }

    protected static boolean contains(final List<FeatureType> featureTypes, final FeatureType featureType) {
        return Utils.contains(featureTypes, featureType);
    }

    protected static Method findMethod(final Class<?> type, final String methodName, final Class<?>[] methodTypes) {
        return Utils.findMethod(type, methodName, methodTypes);
    }

    protected Method findMethod(final Class<?> type, final String methodName) {
        return Utils.findMethod(type, methodName);
    }

    protected void assertNoMethodsRemoved() {
        assertTrue(methodRemover.getRemovedMethodMethodCalls().isEmpty());
        assertTrue(methodRemover.getRemoveMethodArgsCalls().isEmpty());
    }

}
