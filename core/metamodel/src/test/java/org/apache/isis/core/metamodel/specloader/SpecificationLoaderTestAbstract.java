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

package org.apache.isis.core.metamodel.specloader;

import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.weld.junit5.EnableWeld;
import org.jboss.weld.junit5.WeldInitiator;
import org.jboss.weld.junit5.WeldSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.apache.isis.applib.services.grid.GridService2;
import org.apache.isis.applib.services.i18n.TranslationService;
import org.apache.isis.applib.services.i18n.TranslationService.Mode;
import org.apache.isis.applib.services.message.MessageService;
import org.apache.isis.commons.internal.base._Timing;
import org.apache.isis.commons.internal.collections._Sets;
import org.apache.isis.config.registry.IsisBeanTypeRegistry;
import org.apache.isis.core.metamodel.BeansForTesting;
import org.apache.isis.core.metamodel.facetapi.Facet;
import org.apache.isis.core.metamodel.facets.actcoll.typeof.TypeOfFacet;
import org.apache.isis.core.metamodel.facets.all.describedas.DescribedAsFacet;
import org.apache.isis.core.metamodel.facets.all.named.NamedFacet;
import org.apache.isis.core.metamodel.facets.collections.modify.CollectionFacet;
import org.apache.isis.core.metamodel.facets.object.plural.PluralFacet;
import org.apache.isis.core.metamodel.metamodelvalidator.dflt.MetaModelValidatorDefault;
import org.apache.isis.core.metamodel.progmodel.ProgrammingModelAbstract.DeprecatedPolicy;
import org.apache.isis.core.metamodel.services.persistsession.ObjectAdapterService;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.security.authentication.AuthenticationSessionProvider;
import org.apache.isis.progmodels.dflt.ProgrammingModelFacetsJava5;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import lombok.val;

@EnableWeld
abstract class SpecificationLoaderTestAbstract {

    static class Factories {
        
        @Produces
        AuthenticationSessionProvider mockAuthenticationSessionProvider() {
            return Mockito.mock(AuthenticationSessionProvider.class);
        }
        
        @Produces
        GridService2 mockGridService() {
            return Mockito.mock(GridService2.class);
        }
        
        @Produces
        ObjectAdapterService mockPersistenceSessionServiceInternal() {
            return Mockito.mock(ObjectAdapterService.class);
        }
        
        @Produces
        MessageService mockMessageService() {
            return Mockito.mock(MessageService.class);
        }
        
        @Produces
        TranslationService mockTranslationService() {
            val mock = Mockito.mock(TranslationService.class);
            when(mock.getMode()).thenReturn(Mode.DISABLED);
            return mock;
        }
        
        @Produces
        SpecificationLoader getSpecificationLoader() {
            return new SpecificationLoader(
                    new ProgrammingModelFacetsJava5(DeprecatedPolicy.HONOUR),
                    new MetaModelValidatorDefault());
        }
        
    }

    @WeldSetup
    public WeldInitiator weld = WeldInitiator.from(
            
            BeansForTesting.builder()
            .injector()
            .addAll(
                    Factories.class
                    )
            .build()
            
            )
    .build();
    
    
    @Inject protected AuthenticationSessionProvider mockAuthenticationSessionProvider;
    @Inject protected GridService2 mockGridService;
    @Inject protected ObjectAdapterService mockPersistenceSessionServiceInternal;
    @Inject protected MessageService mockMessageService;
    @Inject protected SpecificationLoader specificationLoader;
    
    
    // is loaded by subclasses
    protected ObjectSpecification specification;


    @BeforeEach
    public void setUp() throws Exception {
        
        // PRODUCTION
        
//        BeanTypeRegistry.instance().setDomainServiceTypes(_Sets.newHashSet());
//        BeanTypeRegistry.instance().setFixtureScriptTypes(_Sets.newHashSet());
//        BeanTypeRegistry.instance().setDomainObjectTypes(_Sets.newHashSet());
//        BeanTypeRegistry.instance().setMixinTypes(_Sets.newHashSet());
//        BeanTypeRegistry.instance().setViewModelTypes(_Sets.newHashSet());
//        BeanTypeRegistry.instance().setPersistenceCapableTypes(_Sets.newHashSet());
//        BeanTypeRegistry.instance().setXmlElementTypes(_Sets.newHashSet());

        _Timing.runVerbose("specificationLoader.init()", specificationLoader::init);
        
        //specificationLoader.init();
        
        specification = loadSpecification(specificationLoader);
        
        
    }

    @AfterEach
    public void tearDown() throws Exception {
        specificationLoader.shutdown();
    }

    protected abstract ObjectSpecification loadSpecification(SpecificationLoader reflector);

    @Test
    public void testCollectionFacet() throws Exception {
        final Facet facet = specification.getFacet(CollectionFacet.class);
        assertNull(facet);
    }

    
    @Test
    public void testTypeOfFacet() throws Exception {
        final TypeOfFacet facet = specification.getFacet(TypeOfFacet.class);
        assertNull(facet);
    }


    @Test
    public void testNamedFaced() throws Exception {
        final Facet facet = specification.getFacet(NamedFacet.class);
        assertNotNull(facet);
    }

    @Test
    public void testPluralFaced() throws Exception {
        final Facet facet = specification.getFacet(PluralFacet.class);
        assertNotNull(facet);
    }

    @Test
    public void testDescriptionFacet() throws Exception {
        final Facet facet = specification.getFacet(DescribedAsFacet.class);
        assertNotNull(facet);
    }

}
