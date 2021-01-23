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

package org.apache.isis.core.metamodel.spec.feature;

import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import org.apache.isis.applib.Identifier;
import org.apache.isis.commons.collections.ImmutableEnumSet;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.core.metamodel.spec.ActionType;

public interface ObjectActionContainer {

    // -- ACTION LOOKUP (INHERITENCE CONSIDERED)
    
    /**
     * Same as {@link #getObjectAction(String, ActionType)}, but also considering any inherited object members. 
     * @param id
     * @param type
     * 
     * @implSpec If not found on the current 'type' search for the 'nearest' match in super-types, 
     * and if nothing found there, search the interfaces. Special care needs to be taken, as the
     * {@link ActionType} might be redeclared when inheriting from a super-type or interface.  
     */
    Optional<ObjectAction> findObjectAction(String id, @Nullable ActionType type);
    
    default ObjectAction findObjectActionElseFail(String id, @Nullable ActionType type) {
        return findObjectAction(id, type)
                .orElseThrow(()->_Exceptions.noSuchElement("id=%s type=%s", 
                        id, 
                        type==null ? "any" : type.name()));
    }

     default ObjectAction findObjectActionElseFail(String id) {
        return findObjectActionElseFail(id, null);
    }
    
    // -- ACTION LOOKUP, DECLARED ACTIONS (NO INHERITENCE CONSIDERED)
    
    /**
     * Get the action object represented by the specified identity string.
     * <p>
     * The identity string can be either fully specified with parameters (as per
     * {@link Identifier#toNameParmsIdentityString()} or in abbreviated form (
     * {@link Identifier#toNameIdentityString()}).
     *
     * @see #getObjectAction(String)
     */
    Optional<ObjectAction> getObjectAction(String id, @Nullable ActionType type);

    /**
     * Shortcut to {@link #getObjectAction(String, null)}, meaning where action type is <i>any</i>.
     * @see #getObjectAction(String, ActionType)
     */
    default Optional<ObjectAction> getObjectAction(String id) {
        return getObjectAction(id, null);
    }

    // -- ACTION STREAM

    /**
     * Returns an array of actions of the specified type, including or excluding
     * contributed actions as required.
     */
    Stream<ObjectAction> streamObjectActions(ImmutableEnumSet<ActionType> types, MixedIn contributed);

    default Stream<ObjectAction> streamObjectActions(ActionType type, MixedIn contributed) {
        return streamObjectActions(ImmutableEnumSet.of(type), contributed);
    }
    
    default Stream<ObjectAction> streamObjectActions(MixedIn contributed) {
        return streamObjectActions(ActionType.ANY, contributed);
    }
}
