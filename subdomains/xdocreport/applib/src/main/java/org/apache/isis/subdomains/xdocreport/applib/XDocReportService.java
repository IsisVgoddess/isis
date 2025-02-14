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
package org.apache.isis.subdomains.xdocreport.applib;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @since 2.0 {@index}
 */
public interface XDocReportService {

    byte[] render(byte[] templateBytes, XDocReportModel dataModel, OutputType outputType) throws IOException;

    enum OutputType {
        DOCX,
        PDF
    }

    /**
     * @since 2.0 {@index}
     */
    interface XDocReportModel {

        @lombok.Data
        class Data {
            private final Object obj;
            private final Class<?> cls;
            private final boolean list;

            public static <T> Data list(final List<T> objects, final Class<T> cls) {
                return new Data(objects, cls, true);
            }

            public static <T> Data object(final T object) {
                return new Data(object, object.getClass(), false);
            }
        }
        Map<String, Data> getContextData();

    }

}
