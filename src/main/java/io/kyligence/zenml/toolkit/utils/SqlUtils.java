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

package io.kyligence.zenml.toolkit.utils;

import com.google.common.collect.ImmutableList;
import io.kyligence.zenml.toolkit.tool.sql.CalciteParser;
import io.kyligence.zenml.toolkit.tool.sql.DwSqlDialect;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlBasicCall;
import org.apache.calcite.sql.SqlCharStringLiteral;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.util.SqlBasicVisitor;
import org.apache.calcite.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@UtilityClass
public class SqlUtils {
    private static final String DOT = ".";

    private static final String ESCAPE = "\\";

    private static final String DOUBLE_QUOTE = "\"";

    private static final String SINGLE_QUOTE = "'";

    private static final String LIKE_FLAG_CHART = "%";

    // TODO need fix chinese and special characters
    private static final Pattern NAIVE_IDENTIFIER_PTN = Pattern.compile("([A-Za-z_][A-Za-z_0-9]+)[.]([A-Za-z_][A-Za-z_0-9]+)");

    public static String doubleQuote(String identifier) {
        return DOUBLE_QUOTE + identifier + DOUBLE_QUOTE;
    }

    public static String singleQuote(String identifier) {
        if (identifier == null)
            return "NULL";

        return SINGLE_QUOTE + identifier.replace(SINGLE_QUOTE, SINGLE_QUOTE + SINGLE_QUOTE) + SINGLE_QUOTE;
    }

    public static String like(String identifier) {
        return like(identifier, true, true);
    }

    public static String like(String identifier, boolean left, boolean right) {
        String likeIdentifier = identifier;
        if (left) {
            likeIdentifier = LIKE_FLAG_CHART + likeIdentifier;
        }
        if (left) {
            likeIdentifier = likeIdentifier + LIKE_FLAG_CHART;
        }
        return singleQuote(likeIdentifier);
    }

    public static String in(List<String> identifiers, boolean needQuote) {
        List<String> finalIdentifiers;
        if (needQuote) {
            finalIdentifiers = identifiers.stream()
                    .map(SqlUtils::singleQuote)
                    .collect(Collectors.toList());
        } else {
            finalIdentifiers = identifiers;
        }
        return String.format("(%s)", String.join(",", finalIdentifiers));
    }

    public static String between(String lowerBound, String upperBound) {
        return singleQuote(lowerBound) + " AND " + singleQuote(upperBound);
    }

    public static List<String> getSingleNameIdentifiers(String expr) {
        List<String> ret = new ArrayList<>();
        try {
            expr = DwSqlDialect.startWithSqlKeyword(expr) ? "_" + expr : expr;
            SqlNode sqlNode = CalciteParser.parseExpression(expr);
            sqlNode.accept(new SqlBasicVisitor<>() {
                @Override
                public Object visit(SqlIdentifier identifier) {
                    if (identifier.names.size() == 1) {
                        if (!identifier.isStar())
                            ret.add(identifier.names.get(0));
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            log.warn("parse expression '{}' failed", expr, e);
        }
        return ret;
    }

    /**
     * count(order.sales) --> count("order"."sales")
     * count(sales) --> count(sales)
     * count(1) --> count(1)
     *
     * @param expr
     * @return keyword quoted expr
     */
    public static String quoteKeyword(String expr) {
        // polish a sql expression, to avoid unquoted keyword
        if (expr == null)
            return null;

        if (DwSqlDialect.isSqlKeyword(expr))
            return '"' + expr + '"';

        int lastIndex = 0;
        StringBuilder buf = new StringBuilder();
        Matcher matcher = NAIVE_IDENTIFIER_PTN.matcher(expr);
        while (matcher.find()) {
            int start = matcher.start();
            buf.append(expr, lastIndex, start);
            String g1 = matcher.group(1);
            String g2 = matcher.group(2);
            boolean containsKeyWord = DwSqlDialect.isSqlKeyword(g1) || DwSqlDialect.isSqlKeyword(g2);
            boolean alreadyQuote = start >= 1
                    && (expr.charAt(start - 1) == '\'' || expr.charAt(start - 1) == '"');
            boolean withQuote = containsKeyWord && !alreadyQuote;
            buf.append(withQuote ? '"' + g1 + '"' : g1);
            buf.append(".");
            buf.append(withQuote ? '"' + g2 + '"' : g2);
            lastIndex = matcher.end();
        }
        if (lastIndex < expr.length()) {
            buf.append(expr, lastIndex, expr.length());
        }
        return buf.toString();
    }

    public static Pair<String, List<String>> getExpCallAndArgs(String expr) {
        expr = DwSqlDialect.startWithSqlKeyword(expr) ? "_" + expr : expr;
        List<String> ret = new ArrayList<>();
        try {
            SqlNode sqlNode = CalciteParser.parseExpression(expr);
            if (sqlNode instanceof SqlBasicCall) {
                SqlBasicCall call = (SqlBasicCall) sqlNode;
                String callName = call.getOperator().getName();
                callName = callName.startsWith("_") ? callName.substring(1) : callName;
                return Pair.of(callName, call.getOperandList().stream().map(child -> {
                    if (child instanceof SqlIdentifier) {
                        return String.join(".", ((SqlIdentifier) child).names.stream().toArray(String[]::new));
                    } else if (child instanceof SqlCharStringLiteral) {
                        return ((SqlCharStringLiteral) child).toValue();
                    }
                    return child.toString();
                }).collect(Collectors.toList()));
            }
            return Pair.of(null, ImmutableList.of(sqlNode.toString()));
        } catch (Exception e) {
            log.warn("parse expression '{}' failed", expr, e);
        }
        return Pair.of(null, ImmutableList.of());
    }

    public static List<String> getFullNameIdentifiers(String expr) {
        List<String> ret = new ArrayList<>();
        try {
            SqlNode sqlNode = CalciteParser.parseExpression(expr);
            sqlNode.accept(new SqlBasicVisitor<>() {
                @Override
                public Object visit(SqlIdentifier identifier) {
                    if (identifier.names.size() == 1) {
                        if (!identifier.isStar())
                            ret.add(identifier.names.get(0));
                    } else {
                        ret.add(String.join(".", identifier.names));
                    }
                    return true;
                }
            });
        } catch (Exception e) {
            log.warn("parse expression '{}' failed", expr, e);
        }
        return ret;
    }
}
