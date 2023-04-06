/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.kyligence.zenml.toolkit.model.tableau.tds.column;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class Column {

    @JacksonXmlProperty(localName = "caption", isAttribute = true)
    private String caption;

    @JacksonXmlProperty(localName = "datatype", isAttribute = true)
    private String datatype;

    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;

    @JacksonXmlProperty(localName = "role", isAttribute = true)
    private String role;

    @JacksonXmlProperty(localName = "default-role", isAttribute = true)
    private String defaultRole;

    @JacksonXmlProperty(localName = "default-type", isAttribute = true)
    private String defaultType;

    @JacksonXmlProperty(localName = "default-format", isAttribute = true)
    private String defaultFormat;

    @JacksonXmlProperty(localName = "type", isAttribute = true)
    private String type;

    @JacksonXmlProperty(localName = "hidden", isAttribute = true)
    private String hidden;

    @JacksonXmlProperty(localName = "aggregation", isAttribute = true)
    private String aggregation;

    @JacksonXmlProperty(localName = "semantic-role", isAttribute = true)
    private String semanticRole;

    @JacksonXmlProperty(localName = "user:auto-column", isAttribute = true)
    private String autoColumn;

    @JacksonXmlProperty(localName = "calculation")
    private Calculation calculation;

    @JacksonXmlProperty(localName = "alias")
    @JacksonXmlElementWrapper(localName = "aliases")
    private List<Alias> aliases;
}
