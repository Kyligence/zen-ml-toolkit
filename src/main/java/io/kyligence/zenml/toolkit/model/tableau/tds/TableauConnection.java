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
import io.kyligence.zenml.toolkit.model.tableau.tds.connection.Cols;
import io.kyligence.zenml.toolkit.model.tableau.tds.connection.NamedConnectionList;
import io.kyligence.zenml.toolkit.model.tableau.tds.connection.Calculation;
import io.kyligence.zenml.toolkit.model.tableau.tds.connection.metadata.MetadataRecordList;
import io.kyligence.zenml.toolkit.model.tableau.tds.connection.relation.Relation;
import lombok.*;

import java.util.List;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TableauConnection {

    @JacksonXmlProperty(localName = "class", isAttribute = true)
    private String className;

    @JacksonXmlProperty(localName = "named-connections")
    private NamedConnectionList namedConnectionList;

    @JacksonXmlProperty(localName = "relation")
    private Relation relation;

    @JacksonXmlProperty(localName = "calculation")
    @JacksonXmlElementWrapper(localName = "calculations")
    private List<Calculation> calculations;

    @JacksonXmlProperty(localName = "cols")
    private Cols cols;

    @JacksonXmlProperty(localName = "metadata-records")
    private MetadataRecordList metadataRecords;

}
