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

package io.kyligence.zenml.toolkit.model.tableau.tds;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import io.kyligence.zenml.toolkit.model.tableau.tds.column.Column;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@JacksonXmlRootElement(localName = "datasource")
public class TableauDatasource {

    @JacksonXmlProperty(localName = "caption", isAttribute = true)
    private String caption;

    @JacksonXmlProperty(localName = "formatted-name", isAttribute = true)
    private String formattedName;

    @JacksonXmlProperty(localName = "name", isAttribute = true)
    private String name;

    @JacksonXmlProperty(localName = "xmlns:user", isAttribute = true)
    private String xmlns_user;

    @JacksonXmlProperty(localName = "inline", isAttribute = true)
    private String inline;

    @JacksonXmlProperty(localName = "source-platform", isAttribute = true)
    private String sourcePlatform;

    @JacksonXmlProperty(localName = "version", isAttribute = true)
    private String version;

    @JacksonXmlProperty(localName = "connection")
    private TableauConnection tableauConnection;

    @JacksonXmlProperty(localName = "aliases")
    private Aliases aliases;

    @JacksonXmlProperty(localName = "column")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Column> columns;

    @JacksonXmlProperty(localName = "drill-paths")
    private DrillPaths drillPaths;

    @JacksonXmlProperty(localName = "folder")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<Folder> folders;

    @JacksonXmlProperty(localName = "layout")
    private Layout layout;

    @JacksonXmlProperty(localName = "semantic-values")
    private SemanticValueList semanticValues;

}
