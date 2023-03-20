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

package io.kyligence.zenml.toolkit.source.tableau;

import lombok.*;
import org.apache.commons.lang3.StringUtils;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class TableauColumn {
    public static final String DIMENSION = "dimension";
    public static final String AGGREGATION = "aggregation";

    public static final String MEASURE = "measure";

    private String caption;

    private String datatype;

    private String name;

    private String role;

    private String type;

    private String hidden;

    private String semanticRole;

    private String aggregation;

    private TableauCalculation calculation;

    public boolean isDimension() {
        if (this.calculation != null) {
            return false;
        }
        return StringUtils.equalsAnyIgnoreCase(this.role, DIMENSION);
    }

    public boolean isTimeDimension() {
        if (!isDimension()) {
            return false;
        }

        return StringUtils.equalsAnyIgnoreCase(this.datatype, TableauDataType.DATE) ||
                StringUtils.equalsAnyIgnoreCase(this.datatype, TableauDataType.TIMESTAMP);
    }

    public boolean isMeasure() {
        return StringUtils.equalsAnyIgnoreCase(this.role, MEASURE) &&
                this.calculation != null &&
                StringUtils.equalsAnyIgnoreCase(this.calculation.getClazz(), TableauCalculation.CLASS_TABLEAU);
    }

    public String getDimension() {
        return this.name;
    }

    public String getTimeDimensionName() {
        if (StringUtils.contains(this.name, ".")) {
            String[] parts = this.name.split("\\.");
            return parts[1];
        }

        return this.name;
    }

    public String getDimensionColumn() {
        return this.name.split("\\.")[1];
    }

    public String getDimensionTable() {
        return this.name.split("\\.")[0];
    }

    public String getMeasureName() {
        return this.name;
    }

    public String getMeasureDisplayName() {
        return this.caption;
    }

    public String getMeasureExpression() {
        if (StringUtils.equalsAnyIgnoreCase(this.calculation.getClazz(), TableauCalculation.CLASS_TABLEAU)) {
            return this.calculation.getFormula();
        }
        return null;
    }

    public boolean isHidden() {
        if (StringUtils.isNotEmpty(this.hidden)) {
            return StringUtils.equalsAnyIgnoreCase(this.hidden, "true");
        } else {
            return false;
        }
    }

}
