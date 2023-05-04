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
import io.kyligence.zenml.toolkit.model.sql.SqlMetricSpec;
import io.kyligence.zenml.toolkit.model.zenml.TimeDimension;
import io.kyligence.zenml.toolkit.utils.DateValidatorUtils;
import io.kyligence.zenml.toolkit.utils.sql.CalciteConfig;
import io.kyligence.zenml.toolkit.utils.sql.CalciteParser;
import io.kyligence.zenml.toolkit.utils.sql.DwSqlDialect;
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

        List<SqlMetricSpec> sqlMetricSpecs = new ArrayList<>();
        var sqlNodeList = CalciteParser.parse(sqls, CalciteConfig.DEFAULT_PARSER_CONFIG);
        var list = sqlNodeList.getList();

        // for each sql from the sql file
        for (SqlNode node : list) {
            if(SqlUtils.isASqlOrderBy(node)){
                var sqlOrderBy = (SqlOrderBy) node;
                node = sqlOrderBy.query;
            }

            if (!SqlUtils.isASqlSelect(node)) {
                // only support from sql select sql
                log.error(ErrorCode.SQL_SELECT_STATEMENT_SUPPORT_ONLY.getReportMessage() + " : {}", node);
                continue;
            }

            var sqlMetric = analyzeSql2MetricSpec(node);

            if (StringUtils.isEmpty(sqlMetric.getMeasure())) {
                // this sql is a detailed query, no measure found
                log.error(ErrorCode.MEASURE_NOT_FOUND_IN_SQL.getReportMessage() + " : {}", node);
            } else if (StringUtils.isEmpty(sqlMetric.getDatasource())) {
                // this sql has no data source, ignore it
                log.error(ErrorCode.MEASURE_NOT_FOUND_IN_SQL.getReportMessage() + " : {}", node);
            } else {
                sqlMetricSpecs.add(sqlMetric);
            }
        }


        return enrichSqlMetricSpecs(sqlMetricSpecs);
    }

    private List<SqlMetricSpec> enrichSqlMetricSpecs(List<SqlMetricSpec> metricSpecs) {
        for (SqlMetricSpec metric : metricSpecs) {
            var datasource = metric.getDatasource();

            List<String> dimensions = metric.getDimensions();
            List<String> newDims = enrichDimensionIdentifiers(datasource, dimensions);
            metric.setDimensions(newDims);
            List<String> timeDimensionsStrs = metric.getTimeDimensionStrs();
            List<String> newTimeDimStrs = enrichDimensionIdentifiers(datasource, timeDimensionsStrs);

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

    private SqlMetricSpec analyzeSql2MetricSpec(SqlNode node) {
        var sqlMetric = new SqlMetricSpec();
        Set<String> dimensions = new HashSet<>();
        var sqlSelect = (SqlSelect) node;
        var selectList = sqlSelect.getSelectList();
        for (SqlNode selectNode : selectList) {
            // get measure in sql select list
            if (SqlUtils.isASqlBasicCall(selectNode)) {
                var measureNode = (SqlBasicCall) selectNode;
                var op = measureNode.getOperator();
                if (SqlUtils.isAAsOperator(op)) {
                    // measure with alias: sum(a) as A
                    var measureNodes = measureNode.getOperandList();
                    var measureExpr = measureNodes.get(0);
                    var measureAlias = measureNodes.get(1);
                    sqlMetric.setMeasureAlias(toDwDialectString(measureAlias).toUpperCase());
                    sqlMetric.setMeasure(toDwDialectString(measureExpr));
                } else {
                    // measure with no alias: sum(a)
                    sqlMetric.setMeasure(toDwDialectString(measureNode));
                    var measureAlias = mockMeasureAlias(measureNode);
                    sqlMetric.setMeasureAlias(measureAlias);
                }

            }

            // get dimensions in sql select list
            // dimensions in group by clause generally will be existed in select list
            if (SqlUtils.isASqlIdentifier(selectNode)) {
                dimensions.add(toDwDialectString(selectNode));
            }
        }

        // get dimensions & time dimension from where clause
        var whereNode = (SqlBasicCall) sqlSelect.getWhere();
        if (whereNode != null) {
            List<String> dimsInWhere = SqlUtils.getFullNameIdentifiers(whereNode);
            dimensions.addAll(dimsInWhere);
            Set<String> timeDimensions = parseTimeDimensions(whereNode);
            sqlMetric.setTimeDimensionStrs(timeDimensions.stream().toList());
        }


        // get datasource name
        var fromNode = sqlSelect.getFrom();
        if (fromNode != null) {
            var datasource = extractDataSourceName(fromNode);
            sqlMetric.setDatasource(datasource);
        }
        sqlMetric.setDimensions(dimensions.stream().toList());
        sqlMetric.setOriginalSql(toDwDialectString(node));
        return sqlMetric;
    }

    private String extractDataSourceName(SqlNode fromNode) {
        String datasource;
        if (SqlUtils.isASqlJoin(fromNode)) {
            // for joint table, give a new name as data model name in zen
            // Need manually create a view with the same name created here
            var joinNode = (SqlJoin) fromNode;
            var leftNode = joinNode.getLeft();
            var joinType = joinNode.getJoinType();
            var rightNode = joinNode.getRight();
            if (SqlUtils.isASqlIdentifier(leftNode)) {
                datasource = toDwDialectString(leftNode)
                        + "_" + joinType.toString()
                        + "_" + toDwDialectString(rightNode);
            } else {
                var left = extractDataSourceName(leftNode);
                datasource = left
                        + "_" + joinType.toString()
                        + "_" + toDwDialectString(rightNode);
            }
        } else {
            // it's a single table, use as table name
            datasource = toDwDialectString(fromNode);
        }
        return datasource.toLowerCase();
    }

    private static Set<String> parseTimeDimensions(SqlBasicCall whereNode) {
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
                        if (SqlUtils.isASqlLiteral(opNode) &&
                                DateValidatorUtils.isValid(opNode.toString())) {
                            isDateColumn = true;
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
        return toDwDialectString(measureNode).replaceAll(" ", "").toUpperCase();
    }

    private String toDwDialectString(SqlNode node) {
        return node.toSqlString(DwSqlDialect.DEFAULT).toString();
    }


}
