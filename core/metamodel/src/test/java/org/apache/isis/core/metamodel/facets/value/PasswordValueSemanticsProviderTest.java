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
package org.apache.isis.core.metamodel.facets.value;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import org.apache.isis.applib.value.Password;
import org.apache.isis.applib.value.semantics.ValueDecomposition;
import org.apache.isis.core.metamodel.valuesemantics.PasswordValueSemantics;

public class PasswordValueSemanticsProviderTest
extends ValueSemanticsProviderAbstractTestCase<Password> {

    private PasswordValueSemantics valueSemantics;
    private Password password;

    @Before
    public void setUpObjects() throws Exception {
        setSemantics(valueSemantics = new PasswordValueSemantics());
        password = new Password("secret");
    }

    @Test
    public void testEncode() throws Exception {
        assertEquals("secret", valueSemantics.decompose(password).toJson());
    }

    @Test
    public void testDecode() throws Exception {
        final Object restore = valueSemantics.compose(
                ValueDecomposition.fromJson(valueSemantics.getSchemaValueType(), "secret"));
        assertEquals(password, restore);
    }

}
