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

import io.kyligence.zenml.toolkit.converter.tableau.tds.*;
import io.kyligence.zenml.toolkit.model.tableau.tds.TableauConnection;
import io.kyligence.zenml.toolkit.model.tableau.tds.TableauDatasource;
import io.kyligence.zenml.toolkit.model.tableau.tds.column.Column;
import io.kyligence.zenml.toolkit.model.tableau.tds.connection.Calculation;
import io.kyligence.zenml.toolkit.model.tableau.tds.connection.Col;
import io.kyligence.zenml.toolkit.model.tableau.tds.connection.metadata.MetadataRecord;
import io.kyligence.zenml.toolkit.model.tableau.tds.connection.relation.Relation;
import io.kyligence.zenml.toolkit.utils.tableau.TableauDialectUtils;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

@Slf4j
public class TdsAnalyzer {

    private static final String YES = "yes";
    public static final String DEFAULT_CATALOG = "defaultCatalog";
    private static final String TRUE = "true";
    private static final String MEASURE = "measure";
    private static final String DIMENSION = "dimension";

    public TdsSpec analyzeTdsSpec(TableauDatasource tds) {
        var spec = new TdsSpec();

        // get connection name
        var tableauDsName = tds.getFormattedName() == null ? tds.getCaption() : tds.getFormattedName();
        if (StringUtils.isEmpty(tableauDsName)) {
            log.error("tds file has no caption name or formatted-name in datasource, please add it");
            throw new IllegalArgumentException("tds file has no caption name or formatted-name in datasource, please add it");
        }
        spec.setTableauDsName(tableauDsName);

        // get connection
        var connection = tds.getTableauConnection().getNamedConnectionList().getNamedConnections().get(0)
                .getConnection();
        var catalog = connection.getDbName();
        var schema = connection.getSchema();
        var tableauConnection = tds.getTableauConnection();

        // analyze table and join relation
        var relation = tableauConnection.getRelation();
        // fact table must be existed
        var factSourceTable = new TableauSourceTable();
        LinkedList<TableauJoinTable> joinTables = new LinkedList<>();
        parseRelation(factSourceTable, joinTables, catalog, schema, relation);
        spec.setFactSourceTable(factSourceTable);
        spec.setJoinTables(joinTables);

        // get table alias to table identifier if alias is enabled
        Map<String, TableauSourceTable> tableAliases = getTableAliasMap(factSourceTable, joinTables);
        spec.setSourceTableMap(tableAliases);

        // get column alias to column identifier if alias is enabled
        Map<String, TableauSourceColumn> columnAliases = getColumnAliasMap(tableauConnection, tableAliases);
        spec.setSourceColumnMap(columnAliases);

        // get column 2 calculation map
        Map<String, TableauCalculation> column2Cal = getColumn2CalculationMap(tds);

        // get dimensions
        Map<String, TableauDimension> col2dimensions = getTableauDimensions(tds, columnAliases, column2Cal);
        spec.setDimensions(col2dimensions.values().stream().toList());

        // get measures
        Map<String, TableauMeasure> col2Measures = getTableauMeasures(tds, columnAliases, column2Cal);
        spec.setMeasures(col2Measures.values().stream().toList());

        return spec;
    }

    private void parseRelation(TableauSourceTable factSourceTable, LinkedList<TableauJoinTable> joinTables, String catalog,
                               String schema, Relation relation) {
        if (relation.getType().equals("table")) {
            factSourceTable.fillTable(createTable(relation, schema, catalog));
        } else {
            var rightJoinTable = relation.getRelationList().get(1);
            var joinTable = new TableauJoinTable();
            var lookupSourceTable = createTable(rightJoinTable, schema, catalog);
            joinTable.setJoinType(relation.getJoin());
            joinTable.setSourceTable(lookupSourceTable);

            var joinClause = relation.getClause();
            var expression = joinClause.getExpression();
            if (StringUtils.equalsIgnoreCase(expression.getOp(), TableauDialectUtils.OP_EQUALS)) {
                String[] fks = new String[1];
                String[] pks = new String[1];
                fks[0] = TableauDialectUtils.removeBracket(expression.getExpressionList().get(0).getOp());
                pks[0] = TableauDialectUtils.removeBracket(expression.getExpressionList().get(1).getOp());
                joinTable.setFks(fks);
                joinTable.setPks(pks);
            }
            if (StringUtils.equalsIgnoreCase(expression.getOp(), TableauDialectUtils.OP_AND)) {
                int length = expression.getExpressionList().size();
                String[] fks = new String[length];
                String[] pks = new String[length];
                for (int i = 0; i < length; i++) {
                    var operand = expression.getExpressionList().get(i);
                    fks[i] = TableauDialectUtils.removeBracket(operand.getExpressionList().get(0).getOp());
                    pks[i] = TableauDialectUtils.removeBracket(operand.getExpressionList().get(1).getOp());
                }
                joinTable.setFks(fks);
                joinTable.setPks(pks);
            }
            joinTables.addFirst(joinTable);
            parseRelation(factSourceTable, joinTables, catalog, schema, relation.getRelationList().get(0));
        }
    }

    private TableauSourceTable createTable(Relation relation, String schema, String catalog) {
        var schemaTable = relation.getTable();
        var tableauTableName = TableauDialectUtils.addBracket(relation.getName());
        String tableName = null;
        catalog = (StringUtils.isEmpty(catalog) ? DEFAULT_CATALOG : catalog);

        // [DEFAULT].[KYLIN_SALES] (has schema) or [KYLIN_SALES]
        if (schemaTable.contains(".")) {
            schema = TableauDialectUtils.getSchema(schemaTable);
            tableName = TableauDialectUtils.getTable(schemaTable);
        } else {
            schema = (StringUtils.isEmpty(schema) ? catalog : schema);
            tableName = TableauDialectUtils.removeBracket(schemaTable);
        }
        return new TableauSourceTable(tableauTableName, tableName, schema, catalog);
    }

    private boolean isAliasEnabled(TableauDatasource tds) {
        var alias = tds.getAliases();
        if (alias == null)
            return false;
        if (StringUtils.isEmpty(alias.getEnabled()))
            return false;
        return StringUtils.equalsIgnoreCase(alias.getEnabled(), YES);
    }

    private Map<String, TableauSourceTable> getTableAliasMap(TableauSourceTable factSourceTable, List<TableauJoinTable> joinTables) {
        Map<String, TableauSourceTable> tableAliases = new HashMap<>();

        tableAliases.put(factSourceTable.getTableauTableName(), factSourceTable);
        for (TableauJoinTable joinTable : joinTables) {
            var sourceTable = joinTable.getSourceTable();
            tableAliases.put(sourceTable.getTableauTableName(), sourceTable);
        }
        return tableAliases;
    }

    private Map<String, TableauSourceColumn> getColumnAliasMap(TableauConnection connection,
                                                               Map<String, TableauSourceTable> tableAliases) {
        Map<String, TableauSourceColumn> columnAliases = new HashMap<>();

        var metadataRecords = connection.getMetadataRecords();
        if (metadataRecords != null && metadataRecords.getMetadataRecords() != null) {
            for (MetadataRecord metadataRecord : metadataRecords.getMetadataRecords()) {
                var tableauIdentify = metadataRecord.getLocalName();
                var colTableName = metadataRecord.getParentName();
                var colName = metadataRecord.getRemoteName().toUpperCase();
                var colSourceTable = tableAliases.get(colTableName);
                var sourceColumn = new TableauSourceColumn(tableauIdentify, colName, colSourceTable);
                columnAliases.put(tableauIdentify, sourceColumn);
            }
        }

        var cols = connection.getCols();
        if (cols != null && cols.getCols() != null) {
            for (Col col : cols.getCols()) {
                var tableauIdentify = col.getKey();
                var colTableName = col.getValue().split("[.]")[0];
                var colName = TableauDialectUtils.removeBracket(col.getValue().split("[.]")[1]);
                var colSourceTable = tableAliases.get(colTableName);
                var sourceColumn = new TableauSourceColumn(tableauIdentify, colName, colSourceTable);
                columnAliases.put(tableauIdentify, sourceColumn);
            }
        }
        return columnAliases;
    }

    private Map<String, TableauCalculation> getColumn2CalculationMap(TableauDatasource tds) {
        Map<String, TableauCalculation> calculationMap = new HashMap<>(4);
        if (tds.getTableauConnection().getCalculations() != null) {
            for (Calculation cal : tds.getTableauConnection().getCalculations()) {
                var tableauCal = new TableauCalculation();
                tableauCal.setColumn(cal.getColumn());
                tableauCal.setFormula(cal.getFormula());
                calculationMap.put(cal.getColumn(), tableauCal);
            }
        }
        return calculationMap;
    }

    private Map<String, TableauDimension> getTableauDimensions(TableauDatasource tds,
                                                               Map<String, TableauSourceColumn> columnAliases,
                                                               Map<String, TableauCalculation> column2Cal) {
        Map<String, TableauDimension> col2DimMap = new HashMap<>();
        var columns = tds.getColumns();
        for (Column column : columns) {
            if (isDimensionColumn(column)) {
                var tableauIdentifier = column.getName();
                var sourceColumn = columnAliases.get(tableauIdentifier);
                if (sourceColumn != null) {
                    var dimension = new TableauDimension();
                    dimension.setCaption(column.getCaption());
                    dimension.setSourceColumn(sourceColumn);
                    dimension.setDataType(column.getDatatype());
                    dimension.setTableauIdentifier(tableauIdentifier);
                    col2DimMap.put(tableauIdentifier, dimension);
                } else {
                    var calculation = column2Cal.get(tableauIdentifier);
                    if (calculation == null) {
                        calculation = new TableauCalculation();
                        calculation.setColumn(tableauIdentifier);
                        if (column.getCalculation() == null || column.getCalculation().getFormula() == null) {
                            continue;
                        }
                        calculation.setFormula(column.getCalculation().getFormula());
                        column2Cal.put(tableauIdentifier, calculation);
                    }
                    var dimension = new TableauDimension();
                    dimension.setCaption(column.getCaption());
                    dimension.setDataType(column.getDatatype());
                    dimension.setCalculation(calculation);
                    dimension.setTableauIdentifier(tableauIdentifier);
                    col2DimMap.put(tableauIdentifier, dimension);
                }
            }
        }
        return col2DimMap;
    }

    private Map<String, TableauMeasure> getTableauMeasures(TableauDatasource tds,
                                                           Map<String, TableauSourceColumn> columnAliases,
                                                           Map<String, TableauCalculation> column2Cal) {
        Map<String, TableauMeasure> col2MeasureMap = new HashMap<>();
        var columns = tds.getColumns();
        for (Column column : columns) {
            if (isMeasureColumn(column)) {
                var tableauIdentifier = column.getName();
                var sourceColumn = columnAliases.get(tableauIdentifier);
                if (sourceColumn != null) {
                    var measure = new TableauMeasure();
                    measure.setCaption(column.getCaption());
                    measure.setDataType(column.getDatatype());
                    measure.setSourceColumn(sourceColumn);
                    measure.setTableauIdentifier(tableauIdentifier);
                    measure.setAggregation(column.getAggregation());
                    col2MeasureMap.put(tableauIdentifier, measure);
                } else {
                    var calculation = column2Cal.get(tableauIdentifier);
                    if (calculation == null) {
                        calculation = new TableauCalculation();
                        calculation.setColumn(tableauIdentifier);
                        if (column.getCalculation() == null || column.getCalculation().getFormula() == null) {
                            continue;
                        }
                        calculation.setFormula(column.getCalculation().getFormula());
                        column2Cal.put(tableauIdentifier, calculation);
                    }
                    var measure = new TableauMeasure();
                    measure.setCaption(column.getCaption());
                    measure.setDataType(column.getDatatype());
                    measure.setCalculation(calculation);
                    measure.setTableauIdentifier(tableauIdentifier);
                    measure.setAggregation(column.getAggregation());
                    col2MeasureMap.put(tableauIdentifier, measure);
                }
            }
        }
        return col2MeasureMap;
    }

    private boolean isMeasureColumn(Column column) {
        if (isHiddenColumn(column))
            return false;
        var role = column.getRole();
        return StringUtils.equalsIgnoreCase(role, MEASURE);
    }

    private boolean isDimensionColumn(Column column) {
        if (isHiddenColumn(column))
            return false;
        var role = column.getRole();
        return StringUtils.equalsIgnoreCase(role, DIMENSION);
    }

    private boolean isHiddenColumn(Column column) {
        var hidden = column.getHidden();
        if (StringUtils.isEmpty(hidden))
            return false;

        return StringUtils.equalsIgnoreCase(TRUE, hidden);
    }

}
