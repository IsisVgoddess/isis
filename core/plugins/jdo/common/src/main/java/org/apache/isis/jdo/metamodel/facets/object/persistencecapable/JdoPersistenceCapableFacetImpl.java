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
package org.apache.isis.jdo.metamodel.facets.object.persistencecapable;

import java.util.UUID;

import javax.jdo.annotations.IdentityType;

import org.apache.isis.commons.internal.base._Lazy;
import org.apache.isis.commons.internal.context._Context;
import org.apache.isis.metamodel.adapter.oid.Oid;
import org.apache.isis.metamodel.facetapi.FacetHolder;
import org.apache.isis.metamodel.spec.ObjectSpecification;

import lombok.val;

public class JdoPersistenceCapableFacetImpl extends JdoPersistenceCapableFacetAbstract {

    public JdoPersistenceCapableFacetImpl(
            final String schemaName,
            final String tableOrTypeName,
            final IdentityType identityType,
            final FacetHolder holder) {
        
        super(schemaName, tableOrTypeName, identityType, holder);
    }

    @Override
    public Object fetchByIdentifier(ObjectSpecification spec, String identifier) {
        
        //TODO simplify, spec is already loaded
        
        val persistenceSession = super.getPersistenceSessionJdo();
        val adapter = persistenceSession.getObjectAdapterByIdProvider()
                .adapterFor(Oid.Factory.persistentOf(spec.getSpecId(), identifier)); 
        
        return adapter.getPojo();
    }
    
    @Override
    public String identifierFor(ObjectSpecification spec, Object pojo) {

        //TODO simplify, spec is already loaded
        
        if(pojo==null || !isPersistableType(pojo.getClass())) {
            return "?";
        }
        
        val persistenceSession = super.getPersistenceSessionJdo();
        val isRecognized = persistenceSession.isRecognized(pojo);
        if(isRecognized) {
            final String identifier = persistenceSession.identifierFor(pojo);
            return identifier;
        } else {
            final String identifier = UUID.randomUUID().toString();
            return identifier;    
        }
    }

    @Override
    public void persist(ObjectSpecification spec, Object pojo) {

        if(pojo==null || !isPersistableType(pojo.getClass())) {
            return; //noop
        }
        
        val persistenceSession = super.getPersistenceSessionJdo();
        //TODO don't bypass domain logic !? 
        persistenceSession.getJdoPersistenceManager().makePersistent(pojo);
    }

    // -- HELPER
    
    private final static _Lazy<Class<?>> persistable_type = _Lazy.threadSafe(()->{
        try {
            return _Context.loadClass("org.datanucleus.enhancement.Persistable");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    });

    
    private static boolean isPersistableType(Class<?> type) {
        return persistable_type.get().isAssignableFrom(type);
    }




}
