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

package io.kyligence.zenml.toolkit.converter.tableau;

import io.kyligence.zenml.toolkit.converter.tableau.tds.TableauColumn;
import io.kyligence.zenml.toolkit.converter.tableau.tds.TableauSourceTable;
import io.kyligence.zenml.toolkit.converter.tableau.tds.TdsSpec;
import io.kyligence.zenml.toolkit.utils.tableau.TableauDialectUtils;
import io.kyligence.zenml.toolkit.utils.tableau.TableauExpressionUtils;

public class TableauFormatter {

    public TdsSpec formatTdsIdentifierAndExpr(TdsSpec tdsSpec) {
        var dsName = tdsSpec.getTableauDsName();
        tdsSpec.setTableauDsName(TableauDialectUtils.formatIdentifier(dsName));

        var sourceTable = tdsSpec.getFactSourceTable();
        if (sourceTable != null) {
            formatSourceTable(sourceTable);
        }

        // format column identifier
        var measures = tdsSpec.getMeasures();
        for (TableauColumn measure : measures) {
            formatMeasureName(measure);
            formatSourceColumn(measure);
            formatCalculationExpression(measure);
        }

        // format dimension identifier
        var dimensions = tdsSpec.getDimensions();
        for (TableauColumn dimension : dimensions) {
            formatSourceColumn(dimension);
        }

        return tdsSpec;
    }

    private void formatMeasureName(TableauColumn measure){
        var caption = measure.getCaption();
        var name = measure.getTableauIdentifier();
        measure.setTableauIdentifier(TableauDialectUtils.removeBracket(name));
        measure.setCaption(TableauDialectUtils.removeBracket(caption));
    }

    private void formatSourceColumn(TableauColumn column) {
        var sourceColumn = column.getSourceColumn();
        if (sourceColumn == null)
            return;

        // source table
        var sourceTable = sourceColumn.getSourceTable();
        if (sourceTable != null) {
            formatSourceTable(sourceTable);
        }

        var colName = sourceColumn.getColName();
        var tableauColName = sourceColumn.getTableauColName();
        sourceColumn.setColName(TableauDialectUtils.formatIdentifier(colName));
        sourceColumn.setTableauColName(TableauDialectUtils.formatIdentifier(tableauColName));

    }

    private static void formatSourceTable(TableauSourceTable sourceTable) {
        var tableName = sourceTable.getTableName();
        var schema = sourceTable.getSchema();
        var catalog = sourceTable.getCatalog();
        var tableauTableName = sourceTable.getTableauTableName();

        sourceTable.setTableName(TableauDialectUtils.formatIdentifier(tableName));
        sourceTable.setSchema(TableauDialectUtils.formatIdentifier(schema));
        sourceTable.setCatalog(TableauDialectUtils.formatIdentifier(catalog));
        sourceTable.setTableauTableName(TableauDialectUtils.formatIdentifier(tableauTableName));
    }

    private void formatCalculationExpression(TableauColumn measure) {
        var calculation = measure.getCalculation();
        if(calculation != null){
            var colName = calculation.getColumn();
            var expr = calculation.getFormula();
            calculation.setColumn(TableauDialectUtils.formatIdentifier(colName));
            var formattedExpr = TableauExpressionUtils.convertTableauCalculation(expr);

            calculation.setFormula(formattedExpr);
        }
    }


}
