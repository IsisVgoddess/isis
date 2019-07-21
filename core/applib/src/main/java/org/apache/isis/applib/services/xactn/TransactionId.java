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
package org.apache.isis.applib.services.xactn;

import java.util.UUID;

import org.apache.isis.applib.services.HasUniqueId;

import lombok.Data;

@Data(staticConstructor = "of")
public final class TransactionId implements HasUniqueId {

	private final UUID uniqueId;
	
	/**
     * The {@link HasUniqueId#getUniqueId()} is (as of 1.13.0) actually an identifier for the request/
     * interaction, and there can actually be multiple transactions within such a request/interaction.  
     * The sequence (0-based) is used to distinguish such.
     */
	private final int sequence;

	private final static TransactionId EMPTY = TransactionId.of(UUID.fromString("0000-00-00-00-000000"), 0); 
	
	public static TransactionId empty() {
		return EMPTY;
	}

}
