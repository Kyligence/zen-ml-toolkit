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

package io.kyligence.zenml.toolkit.sql;

import io.kyligence.zenml.toolkit.utils.SqlUtils;
import org.apache.calcite.plan.RelOptCluster;
import org.apache.calcite.plan.RelOptTable;
import org.apache.calcite.prepare.CalciteCatalogReader;
import org.apache.calcite.rel.RelRoot;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rex.RexInputRef;
import org.apache.calcite.rex.RexNode;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.calcite.sql.parser.SqlParser;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.calcite.sql.util.SqlVisitor;
import org.apache.calcite.sql.validate.SqlValidator;
import org.apache.calcite.sql2rel.SqlToRelConverter;
import org.apache.calcite.sql2rel.StandardConvertletTable;
import org.apache.calcite.util.Pair;
import org.apache.calcite.util.SourceStringReader;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;
import static org.apache.calcite.linq4j.Nullness.castNonNull;

public class CalciteParser implements RelOptTable.ViewExpander {

    /**
     * Parses a SQL statement.
     *
     * @param qry       Query string.
     * @param parserCfg Parser config.
     * @return Parsed query.
     */
    static SqlNodeList parse(String qry, SqlParser.Config parserCfg) {
        try {
            return parse(new SourceStringReader(qry), parserCfg);
        } catch (SqlParseException e) {
            throw SQLErrorBuilder.parseError().of("Failed to parse query:\n" + qry, e);
        }
    }

    /**
     * Parses a SQL statement.
     *
     * @param reader    Source string reader.
     * @param parserCfg Parser config.
     * @return Parsed query.
     * @throws SqlParseException on parse error.
     */
    static SqlNodeList parse(Reader reader, SqlParser.Config parserCfg) throws SqlParseException {
        SqlParser parser = SqlParser.create(reader, parserCfg);
        return parser.parseStmtList();
    }

    public static SqlNode parseExpression(String sqlExpression) {
        return parseExpression(sqlExpression, CalciteConfig.DEFAULT_PARSER_CONFIG);
    }

    /**
     * Parses a SQL expression into a {@link SqlNode}. The {@link SqlNode} is not yet validated.
     *
     * @param sqlExpression a SQL expression string to parse
     * @return a parsed SQL node
     */
    public static SqlNode parseExpression(String sqlExpression, SqlParser.Config config) {
        try {
            final SqlParser parser = SqlParser.create(SqlUtils.quoteKeyword(sqlExpression), config);
            return parser.parseExpression();
        } catch (SqlParseException e) {
            throw SQLErrorBuilder.parseError().of("SQL parse failed: " + e.getMessage(), e);
        }
    }

    @Override
    public RelRoot expandView(
            RelDataType rowType,
            String queryString,
            List<String> schemaPath,
            @Nullable List<String> viewPath) {
        SqlNode node = parse(queryString);
        return rel(node);
    }

    /**
     * Visitor that throws exceptions for unsupported SQL features.
     * <p>
     * <p/>
     * Implement it in the future.
     */
    private static class UnsupportedOperationVisitor extends SqlBasicVisitor<Void> {
    }

    private final SqlValidator validator;
    private final RelOptCluster cluster;

    public CalciteParser(SqlValidator validator,
                         @NonNull RelOptCluster cluster) {
        this.validator = validator;
        this.cluster = requireNonNull(cluster, "cluster");
    }

    public SqlNode parse(String sql) {
        SqlNodeList statements = parse(sql, CalciteConfig.DEFAULT_PARSER_CONFIG);
        if (statements.size() != 1) {
            throw SQLErrorBuilder.parseError().of("The command must contain a single statement");
        }
        SqlNode topNode = statements.get(0);
        SqlNode node = validator.validate(topNode);
        SqlVisitor<Void> visitor = new UnsupportedOperationVisitor();
        node.accept(visitor);
        return node;
    }


    public RelRoot rel(SqlNode node) {
        SqlToRelConverter sqlToRelConverter = createSqlToRelConverter();
        return sqlToRelConverter.convertQuery(node, false, true);
    }

    private static SqlNode validateExpression(
            SqlNode sqlNode,
            SqlValidator sqlValidator,
            RelDataType inputRowType) {
        Map<String, RelDataType> nameToTypeMap = new HashMap<>();
        inputRowType.getFieldList().forEach(f -> nameToTypeMap.put(f.getName(), f.getType()));
        return sqlValidator.validateParameterizedExpression(sqlNode, nameToTypeMap);
    }


    public RexNode rex(SqlNode node, RelDataType inputRowType) {
        SqlNode validatedSqlNode = validateExpression(node, validator, inputRowType);
        SqlToRelConverter converter = createSqlToRelConverter();
        Map<String, RexNode> nameToNodeMap = inputRowType.getFieldList().stream()
                .map(relDataTypeField ->
                        Pair.of(relDataTypeField.getName(), RexInputRef.of(relDataTypeField.getIndex(), inputRowType)))
                .collect(Collectors.toMap(Pair::getKey, pair -> castNonNull(pair.getValue())));
        return converter.convertExpression(validatedSqlNode, nameToNodeMap);
    }

    public SqlToRelConverter createSqlToRelConverter() {
        // TODO: custom StandardConvertletTable
        return new SqlToRelConverter(
                this,
                validator,
                validator.getCatalogReader().unwrap(CalciteCatalogReader.class),
                cluster,
                StandardConvertletTable.INSTANCE,
                CalciteConfig.DEFAULT_TO_REL_CONVERTER_CONFIG);
    }

}

