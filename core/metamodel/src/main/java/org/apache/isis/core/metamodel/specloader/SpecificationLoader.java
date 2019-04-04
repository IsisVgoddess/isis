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
package org.apache.isis.core.metamodel.specloader;

import java.util.List;

import javax.annotation.Nullable;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.config.property.ConfigPropertyBoolean;
import org.apache.isis.config.property.ConfigPropertyEnum;
import org.apache.isis.core.commons.exceptions.IsisException;
import org.apache.isis.core.commons.lang.ClassUtil;
import org.apache.isis.core.metamodel.spec.ObjectSpecId;
import org.apache.isis.core.metamodel.spec.ObjectSpecification;
import org.apache.isis.core.metamodel.specloader.specimpl.IntrospectionState;

import lombok.val;

/**
 * Builds the meta-model.
 * TODO [2033] add missing java-doc
 *
 */
public interface SpecificationLoader {

    static final ConfigPropertyBoolean CONFIG_PROPERTY_PARALLELIZE =
            new ConfigPropertyBoolean("isis.reflector.introspector.parallelize", true);

    static final ConfigPropertyEnum<IntrospectionMode> CONFIG_PROPERTY_MODE =
            new ConfigPropertyEnum<>("isis.reflector.introspector.mode", IntrospectionMode.LAZY_UNLESS_PRODUCTION);

    // -- LIVE CYCLE 
    
    void init();
    
    void shutdown();
    
    // -- LOOKUP
    
	/**
     * Returns (a new list holding a copy of) all the loaded specifications.
     *
     * <p>
     *     A new list is returned to avoid concurrent modification exceptions for if the caller then
     *     iterates over all the specifications and performs an activity that might give rise to new
     *     ObjectSpec's being discovered, eg performing metamodel validation.
     * </p>
     */
	List<ObjectSpecification> allSpecifications();

	ObjectSpecification lookupBySpecId(ObjectSpecId objectSpecId);
	
    // -- LOADING

	void reloadSpecification(Class<?> domainType);

	/**
	 * Return the specification for the specified class of object.
	 *
	 * <p>
	 * It is possible for this method to return <tt>null</tt>, for example if
	 * the configured {@link org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor}
	 * has filtered out the class.
	 * 
	 * @return {@code null} if {@code domainType==null}
	 */
	ObjectSpecification loadSpecification(@Nullable Class<?> domainType, IntrospectionState upTo);
	
	/**
	 * Return the specification for the specified ObjectSpecId.
	 *
	 * <p>
	 * It is possible for this method to return <tt>null</tt>, for example if
	 * the configured {@link org.apache.isis.core.metamodel.specloader.classsubstitutor.ClassSubstitutor}
	 * has filtered out the class.
	 * 
	 * @return {@code null} if {@code objectSpecId==null} 
	 */
	default ObjectSpecification loadSpecification(@Nullable ObjectSpecId objectSpecId, IntrospectionState upTo) {
		if(objectSpecId==null) {
			return null;
		}
		
		val className = objectSpecId.asString();
		
		if(_Strings.isNullOrEmpty(className)) {
			return null;
		}
		
		final Class<?> type = ClassUtil.forNameElseFail(className);
		return loadSpecification(type, upTo);
	}
	
	// -- SHORTCUTS
	
	default ObjectSpecification loadSpecification(@Nullable final Class<?> domainType) {
        return loadSpecification(domainType, IntrospectionState.TYPE_INTROSPECTED);
    }
 
	default ObjectSpecification loadSpecification(@Nullable ObjectSpecId objectSpecId) {
		return loadSpecification(objectSpecId, IntrospectionState.TYPE_INTROSPECTED);
    }

	

}
