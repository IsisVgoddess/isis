/* Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License. */
package org.apache.isis.viewer.wicket.ui.util;

import java.time.Duration;

import org.apache.wicket.Component;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.springframework.lang.Nullable;

import org.apache.isis.commons.internal.base._Strings;
import org.apache.isis.commons.internal.exceptions._Exceptions;
import org.apache.isis.viewer.common.model.PlacementDirection;
import org.apache.isis.viewer.common.model.decorator.tooltip.TooltipUiModel;
import org.apache.isis.viewer.wicket.ui.util.ExtendedPopoverConfig.PopoverBoundary;

import lombok.NonNull;
import lombok.val;
import lombok.experimental.UtilityClass;

import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.PopoverConfig;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipBehavior;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.OpenTrigger;
import de.agilecoders.wicket.core.markup.html.bootstrap.components.TooltipConfig.Placement;
import de.agilecoders.wicket.jquery.Config;

@UtilityClass
public class WktTooltips {

    /**
     * Adds popover behavior to the {@code target}, if at least the body is not empty/blank.
     * @param target
     * @param tooltipUiModel
     */
    public <T extends Component> T addTooltip(
            final @Nullable T target,
            final @Nullable TooltipUiModel tooltipUiModel) {

        if(target==null
                || tooltipUiModel==null
                || tooltipUiModel.isEmpty()) {
            return target; // no body so don't render tooltip
        }

        val placementDirection = tooltipUiModel.getPlacementDirection();

        final IModel<String> bodyModel = Model.of(tooltipUiModel.getBody());

        val tooltipBehavior = tooltipUiModel
                .getTitle()
                .map(title->Model.of(title))
                .map(titleModel->createTooltipBehavior(placementDirection, titleModel, bodyModel))
                .orElseGet(()->createTooltipBehavior(placementDirection, bodyModel));
        target.add(tooltipBehavior);

        Wkt.cssAppend(target, "wkt-component-with-tooltip");
        return target;
    }

    public void clearTooltip(final @Nullable Component target) {
        if(target==null) {
            return;
        }
        target.getBehaviors(TooltipBehavior.class)
        .forEach(target::remove);
    }

    // -- SHORTCUTS

    public <T extends Component> T addTooltip(
            final @Nullable T target,
            final @Nullable String body) {
        return addTooltip(PlacementDirection.BOTTOM, target, body);
    }

    public <T extends Component> T addTooltip(
            final @Nullable T target,
            final @Nullable String title,
            final @Nullable String body) {
        return addTooltip(PlacementDirection.BOTTOM, target, title, body);
    }

    public <T extends Component> T addTooltip(
            final @NonNull PlacementDirection placementDirection,
            final @Nullable T target,
            final @Nullable String body) {
        return addTooltip(target, _Strings.isEmpty(body)
                ? null
                : TooltipUiModel.ofBody(placementDirection, body));
    }

    public <T extends Component> T addTooltip(
            final @NonNull PlacementDirection placementDirection,
            final @Nullable T target,
            final @Nullable String title,
            final @Nullable String body) {
        return addTooltip(target, TooltipUiModel.ofTitleAndBody(placementDirection, title, body));
    }

    // -- HELPER

    private TooltipBehavior createTooltipBehavior(
            final @NonNull PlacementDirection placementDirection,
            final @NonNull IModel<String> titleLabel,
            final @NonNull IModel<String> bodyLabel) {
        return createPopoverBehavior(titleLabel, bodyLabel, getTooltipConfig(placementDirection));
    }

    private TooltipBehavior createTooltipBehavior(
            final @NonNull PlacementDirection placementDirection,
            final @NonNull IModel<String> bodyLabel) {
        return createPopoverBehavior(Model.of(), bodyLabel, getTooltipConfig(placementDirection));
    }

    private PopoverBehavior createPopoverBehavior(
            final IModel<String> titleLabel,
            final IModel<String> bodyLabel,
            final PopoverConfig config) {

        return new PopoverBehavior(titleLabel, bodyLabel, config) {
            private static final long serialVersionUID = 1L;

            @Override
            protected CharSequence createInitializerScript(final Component component, final Config config) {
                // bootstrap.Popover(...) will fail when the popover trigger element is not found
                // so we wrap the call within a document search, that will only process elements,
                // that actually exist within the DOM
                val markupId = WktComponents.getMarkupId(component);
                return String.format("document.querySelectorAll('#%s').forEach((elem)=>{"
                        + "new bootstrap.Popover(elem, %s);"
                        + "})",
                        markupId,
                        config.toJsonString());
// alternative jQuery syntax ...
//                return String.format("$('#%s').each((i,elem)=>{"
//                        + "new bootstrap.Popover(elem, %s);"
//                        + "})",
//                        markupId,
//                        config.toJsonString());
            }
        };
    }

    private PopoverConfig getTooltipConfig(final PlacementDirection placementDirection) {
        switch(placementDirection) {
        case TOP:
            return createPopoverConfigDefault()
                .withPlacement(Placement.top);
        case RIGHT:
            return createPopoverConfigDefault()
                .withPlacement(Placement.right);
        case BOTTOM:
            return createPopoverConfigDefault()
                .withPlacement(Placement.bottom);
        case LEFT:
            return createPopoverConfigDefault()
                .withPlacement(Placement.left);
        default:
            throw _Exceptions.unmatchedCase(placementDirection);
        }
    }

//    @Getter(lazy=true)
//    private final PopoverConfig tooltipConfigTop =
//            createPopoverConfigDefault()
//            .withPlacement(Placement.top);
//
//    @Getter(lazy=true)
//    private final PopoverConfig tooltipConfigBottom =
//            createPopoverConfigDefault()
//                .withPlacement(Placement.bottom);

    private PopoverConfig createPopoverConfigDefault() {
        return new ExtendedPopoverConfig()
                .withBoundary(PopoverBoundary.viewport)
                .withTrigger(OpenTrigger.hover)
                .withDelay(Duration.ZERO)
                .withAnimation(true);
    }
}
