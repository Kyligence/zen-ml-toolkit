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

import io.kyligence.zenml.toolkit.utils.tableau.TableauDataTypeUtils;
import lombok.*;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class TableauColumn {
    public final static String DIMENSION_TYPE = "dimension";
    public final static String MEASURE_TYPE = "measure";

    private String columnType;

    protected String caption;

    protected TableauSourceColumn sourceColumn;

    protected String tableauIdentifier;

    protected String dataType;

    protected TableauCalculation calculation;

    private String aggregation;

    public boolean isCM() {
        return sourceColumn == null;
    }

    public boolean isCC() {
        return sourceColumn == null || sourceColumn.getSourceTable() == null
                || StringUtils.isEmpty(sourceColumn.getSourceTable().getTableName());

    }

    public boolean isDimension() {
        return StringUtils.equalsIgnoreCase(columnType, DIMENSION_TYPE);
    }

    public boolean isMeasure() {
        return StringUtils.equalsIgnoreCase(columnType, MEASURE_TYPE);
    }

    public boolean isTimeDimension() {
        if (!isDimension())
            return false;
//        if (isCC())
//            return false;
        return StringUtils.equalsIgnoreCase(this.dataType, TableauDataTypeUtils.DATE) ||
                StringUtils.equalsIgnoreCase(this.dataType, TableauDataTypeUtils.TIMESTAMP);
    }
}
