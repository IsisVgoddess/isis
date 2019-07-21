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
package org.apache.isis.viewer.wicket.ui.components.actionmenu.serviceactions;

import org.apache.isis.metamodel.spec.feature.ObjectAction;
import org.apache.isis.viewer.wicket.model.models.EntityModel;

class ServiceAndAction {
    final String serviceName;
    final EntityModel serviceEntityModel;
    final ObjectAction objectAction;
    final ServiceActionLinkFactory linkAndLabelFactory;

    public boolean separator;

    ServiceAndAction(
            final String serviceName,
            final EntityModel serviceEntityModel,
            final ObjectAction objectAction) {
        this.serviceName = serviceName;
        this.serviceEntityModel = serviceEntityModel;
        this.objectAction = objectAction;
        this.linkAndLabelFactory = new ServiceActionLinkFactory(serviceEntityModel);
    }

    @Override
    public String toString() {
        return serviceName + " ~ " + objectAction.getIdentifier().toFullIdentityString();
    }

}
