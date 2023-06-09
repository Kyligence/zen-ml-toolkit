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

package io.kyligence.zenml.toolkit.converter.tableau.tds;

import io.kyligence.zenml.toolkit.config.ToolkitConfig;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TableauSourceTable {
    // table rename in tableau
    private String tableauTableName;

    private String tableName;

    private String schema;

    private String catalog;

    public String getTableWithSchema() {
        ToolkitConfig config = ToolkitConfig.getInstance();
        if (config.isTableSourceNameIgnore() || StringUtils.isEmpty(this.schema))
            return this.tableName;
        else
            return this.schema + "." + this.tableName;
    }

    public void fillTable(TableauSourceTable sourceTable) {
        assert this.tableauTableName == null;
        this.tableauTableName = sourceTable.getTableauTableName();
        this.tableName = sourceTable.getTableName();
        this.catalog = sourceTable.getCatalog();
        this.schema = sourceTable.getSchema();
    }

    public void fillTable(String tableauTableName, String tableName, String catalog, String schema) {
        assert this.tableauTableName == null;
        this.tableauTableName = tableauTableName;
        this.tableName = tableName;
        this.catalog = catalog;
        this.schema = schema;
    }
}
