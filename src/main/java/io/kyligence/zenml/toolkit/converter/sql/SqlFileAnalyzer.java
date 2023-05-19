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

package io.kyligence.zenml.toolkit.converter.sql;

import io.kyligence.zenml.toolkit.exception.ErrorCode;
import io.kyligence.zenml.toolkit.exception.ToolkitException;
import io.kyligence.zenml.toolkit.model.sql.JoinRelation;
import io.kyligence.zenml.toolkit.model.sql.SqlMetricSpec;
import io.kyligence.zenml.toolkit.model.sql.SqlModel;
import io.kyligence.zenml.toolkit.model.zenml.TimeDimension;
import io.kyligence.zenml.toolkit.utils.DateValidatorUtils;
import io.kyligence.zenml.toolkit.utils.sql.CalciteConfig;
import io.kyligence.zenml.toolkit.utils.sql.CalciteParser;
import io.kyligence.zenml.toolkit.utils.sql.SqlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.*;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class SqlFileAnalyzer {

    public List<SqlMetricSpec> analyzeSqlMetricsSpec(String srcPath) {
        try {
            var sqls = FileUtils.readFileToString(new File(srcPath), StandardCharsets.UTF_8);
            return parseSqls(sqls);
        } catch (IOException e) {
            log.error(ErrorCode.FAILED_READ_SQL_FILE.getReportMessage());
            throw new ToolkitException(ErrorCode.FAILED_READ_SQL_FILE);
        }
    }

    public List<SqlMetricSpec> parseSqls(String sqls) {

        List<SqlMetricSpec> results = new ArrayList<>();
        var sqlNodeList = CalciteParser.parse(sqls, CalciteConfig.DEFAULT_PARSER_CONFIG);
        var list = sqlNodeList.getList();

        // for each sql from the sql file
        for (SqlNode node : list) {
            if (SqlUtils.isASqlOrderBy(node)) {
                var sqlOrderBy = (SqlOrderBy) node;
                node = sqlOrderBy.query;
            }

            if (!SqlUtils.isASqlSelect(node)) {
                // only support from sql select sql
                log.error(ErrorCode.SQL_SELECT_STATEMENT_SUPPORT_ONLY.getReportMessage() + " : {}", node);
                continue;
            }

            List<SqlMetricSpec> sqlMetrics = analyzeSql2MetricSpec(node);

            // validate the sql metrics
            for (SqlMetricSpec sqlMetric : sqlMetrics) {
                if (StringUtils.isEmpty(sqlMetric.getMeasure())) {
                    // this sql is a detailed query, no measure found
                    log.error(ErrorCode.MEASURE_NOT_FOUND_IN_SQL.getReportMessage() + " : {}", node);
                } else if (StringUtils.isEmpty(sqlMetric.getDatasource())) {
                    // this sql has no data source, ignore it
                    log.error(ErrorCode.MEASURE_NOT_FOUND_IN_SQL.getReportMessage() + " : {}", node);
                } else {
                    results.add(sqlMetric);
                }
            }
        }


        return enrichSqlMetricSpecs(results);
    }

    private List<SqlMetricSpec> enrichSqlMetricSpecs(List<SqlMetricSpec> metricSpecs) {
        for (SqlMetricSpec metric : metricSpecs) {
            var datasource = metric.getDatasource();

            List<String> dimensions = metric.getDimensions();
            List<String> newDims = enrichDimensionIdentifiers(datasource, dimensions);
            metric.setDimensions(newDims);
            List<String> timeDimensionsStrs = metric.getTimeDimensionStrs();
            List<String> newTimeDimStrs = enrichTimeDimensionIdentifiers(datasource, timeDimensionsStrs);

            List<TimeDimension> timeDimensions = new ArrayList<>();
            for (String timeDimStr : newTimeDimStrs) {
                var timeDim = new TimeDimension();
                timeDim.setName(timeDimStr);
                timeDimensions.add(timeDim);
            }
            metric.setTimeDimensions(timeDimensions);

        }
        return metricSpecs;
    }

    private List<String> enrichDimensionIdentifiers(String datasource, List<String> dimensions) {
        // enrich dimension name to full identifier
        List<String> newDims = new ArrayList<>();
        for (String dim : dimensions) {
            String newDim;
            if (dim.contains(".")) {
                newDim = datasource + "." + dim.split("\\.")[1];
            } else {
                newDim = datasource + "." + dim;
            }
            newDims.add(newDim);
        }
        return newDims;
    }
    private List<String> enrichTimeDimensionIdentifiers(String datasource, List<String> dimensions) {
        // enrich dimension name to full identifier
        List<String> newDims = new ArrayList<>();
        for (String dim : dimensions) {
            String newDim;
            if (dim.contains(".")) {
                newDim =  dim.split("\\.")[1];
            } else {
                newDim =  dim;
            }
            newDims.add(newDim);
        }
        return newDims;
    }

    private List<SqlMetricSpec> analyzeSql2MetricSpec(SqlNode node) {
        List<SqlMetricSpec> sqlMetricSpecs = new ArrayList<>();
        Set<String> dimensions = new HashSet<>();
        var sqlSelect = (SqlSelect) node;
        var selectList = sqlSelect.getSelectList();
        for (SqlNode selectNode : selectList) {
            // get measure in sql select list
            if (SqlUtils.isASqlBasicCall(selectNode)) {
                var measureNode = (SqlBasicCall) selectNode;
                var op = measureNode.getOperator();
                var sqlMetric = new SqlMetricSpec();
                if (SqlUtils.isAAsOperator(op)) {
                    // measure with alias: sum(a) as A
                    var measureNodes = measureNode.getOperandList();
                    var measureExpr = measureNodes.get(0);
                    var measureAlias = measureNodes.get(1);
                    sqlMetric.setMeasureAlias(SqlUtils.toDwDialectString(measureAlias).toUpperCase());
                    sqlMetric.setMeasure(SqlUtils.toDwDialectString(measureExpr));
                } else {
                    // measure with no alias: sum(a)
                    sqlMetric.setMeasure(SqlUtils.toDwDialectString(measureNode));
                    var measureAlias = mockMeasureAlias(measureNode);
                    sqlMetric.setMeasureAlias(measureAlias);
                }
                sqlMetricSpecs.add(sqlMetric);
            }

            // get dimensions in sql select list
            // dimensions in group by clause generally will be existed in select list
            if (SqlUtils.isASqlIdentifier(selectNode)) {
                dimensions.add(SqlUtils.toDwDialectString(selectNode));
            }
        }

        for (SqlMetricSpec spec : sqlMetricSpecs) {
            // get dimensions & time dimension from where clause
            var whereNode = (SqlBasicCall) sqlSelect.getWhere();
            if (whereNode != null) {
                List<String> dimsInWhere = SqlUtils.getFullNameIdentifiers(whereNode);
                dimensions.addAll(dimsInWhere);
                Set<String> timeDimensions = parseTimeDimensions(whereNode);
                spec.setTimeDimensionStrs(timeDimensions.stream().toList());
            }


            // get datasource name
            var fromNode = sqlSelect.getFrom();
            if (fromNode != null) {
                var jointTable = extractJointTable(fromNode);
                spec.setDatasource(jointTable.generateModelName());
                spec.setSqlModel(jointTable);
            }
            spec.setDimensions(dimensions.stream().toList());
            spec.setOriginalSql(SqlUtils.toDwDialectString(node));

        }
        return sqlMetricSpecs;
    }

    private SqlModel extractJointTable(SqlNode fromNode) {

        var jointTable = new SqlModel();
        jointTable.setJoinRelations(new ArrayList<>());

        if (SqlUtils.isASqlJoin(fromNode)) {
            // for joint table, give a new name as data model name in zen
            // Need manually create a view with the same name created here
            var joinNode = (SqlJoin) fromNode;
            visitJoinNodes(joinNode, jointTable);
        } else {
            // it's a single table, use as table name
            var factTable = SqlUtils.toDwDialectString(fromNode);
            jointTable.setFactTable(factTable);

        }
        return jointTable;
    }

    private void visitJoinNodes(SqlJoin joinNode, SqlModel sqlModel) {

        var leftNode = joinNode.getLeft();
        var joinType = joinNode.getJoinType();
        var rightNode = joinNode.getRight();

        var joinRelation = new JoinRelation();

        var rightTableName = SqlUtils.toDwDialectString(rightNode);
        joinRelation.setRightTable(rightTableName);
        joinRelation.setJoinType(joinType.toString());
        sqlModel.addJoinRelations(joinRelation);


        var conditionNode = (SqlBasicCall) joinNode.getCondition();
        if (conditionNode != null) {
            // table join on multi conditions
            conditionNode.accept(new SqlBasicVisitor<>() {
                @Override
                public Object visit(SqlCall conditionNode) {
                    if (!SqlUtils.isASqlBasicCall(conditionNode)) {
                        return false;
                    }

                    var operator = conditionNode.getOperator().getName();

                    if (StringUtils.equalsIgnoreCase(operator, "AND") ||
                            StringUtils.equalsIgnoreCase(operator, "OR")) {
                        List<SqlNode> operandList = conditionNode.getOperandList();
                        for (SqlNode operand : operandList) {
                            var operandCall = (SqlBasicCall) operand;
                            visit(operandCall);
                        }
                        return false;
                    }

                    var joinCondition = new JoinRelation.JoinCondition();
                    joinCondition.setOperator(operator);
                    var leftKey = SqlUtils.toDwDialectString(conditionNode.getOperandList().get(0));
                    var rightKey = SqlUtils.toDwDialectString(conditionNode.getOperandList().get(1));

                    if (rightKey.contains(".") && leftKey.contains(".")) {
                        // join condition : table_name.col_name = table2_name.col2_name
                        var tableName = rightKey.split("\\.")[0];
                        var colName = rightKey.split("\\.")[1];
                        if (StringUtils.equalsIgnoreCase(tableName, rightTableName)) {
                            // rightKey is a fk
                            joinCondition.setPk(leftKey.split("\\.")[1]);
                            joinCondition.setFk(colName);
                        } else {
                            // rightKey is a pk
                            joinCondition.setPk(colName);
                            joinCondition.setFk(leftKey.split("\\.")[1]);
                        }
                    } else {
                        // join condition : col_name = col2_name, use right key as fk
                        joinCondition.setFk(rightKey);
                        joinCondition.setPk(leftKey);
                    }

                    joinRelation.addJoinConditions(joinCondition);

                    return true;
                }
            });
        }

        if (SqlUtils.isASqlIdentifier(leftNode)) {
            sqlModel.setFactTable(SqlUtils.toDwDialectString(leftNode));
        } else if (SqlUtils.isASqlJoin(leftNode)) {
            var leftJoinNode = (SqlJoin) leftNode;
            visitJoinNodes(leftJoinNode, sqlModel);
        } else if (SqlUtils.isASqlBasicCall(leftNode)
                && SqlUtils.isAAsOperator(((SqlBasicCall) leftNode).getOperator())) {
            // tableName as aliasName
            sqlModel.setFactTable(SqlUtils.toDwDialectString(((SqlBasicCall) leftNode).getOperandList().get(0)));
        }

    }

    private Set<String> parseTimeDimensions(SqlBasicCall whereNode) {
        // time dimension exists in where clause
        // eg: where date > '2022-02-02'
        // date is a time dimension
        Set<String> timeDimensions = new HashSet<>();
        whereNode.accept(new SqlBasicVisitor<>() {
            @Override
            public Object visit(SqlCall call) {
                var opList = call.getOperandList();
                String identifier = null;
                for (SqlNode opNode : opList) {
                    boolean isDateColumn = false;

                    if (SqlUtils.isASqlBasicCall(opNode)) {
                        visit((SqlCall) opNode);
                    } else {
                        if (SqlUtils.isASqlIdentifier(opNode)) {
                            var identifierNode = (SqlIdentifier) opNode;
                            identifier = SqlUtils.getSqlIdentifierName(identifierNode);
                        }
                        if (SqlUtils.isASqlDateLiteral(opNode)) {
                            isDateColumn = true;
                        }
                        if (SqlUtils.isASqlLiteral(opNode)) {
                            var literalNode = (SqlLiteral) opNode;
                            if (DateValidatorUtils.isValid(literalNode.toValue())) {
                                isDateColumn = true;
                            }
                        }
                    }

                    if (StringUtils.isNotEmpty(identifier) && isDateColumn) {
                        timeDimensions.add(identifier);
                    }

                }
                return true;
            }
        });
        return timeDimensions;
    }

    private String mockMeasureAlias(SqlBasicCall measureNode) {
        return SqlUtils.toDwDialectString(measureNode).replaceAll(" ", "");
    }
}
