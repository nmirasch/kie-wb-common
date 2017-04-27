/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.kie.workbench.common.forms.common.rendering.client.widgets.integerBox.converters;

import org.jboss.errai.databinding.client.api.Converter;

public class ByteToLongConverter implements Converter<Byte, Long> {

    @Override
    public Class<Byte> getModelType() {
        return Byte.class;
    }

    @Override
    public Class<Long> getComponentType() {
        return Long.class;
    }

    @Override
    public Byte toModelValue(Long widgetValue) {
        return widgetValue != null ? widgetValue.byteValue() : null;
    }

    @Override
    public Long toWidgetValue(Byte modelValue) {
        return modelValue != null ? modelValue.longValue() : null;
    }
}