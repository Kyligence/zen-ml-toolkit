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

package io.kyligence.zenml.toolkit.utils.tableau;

import io.kyligence.zenml.toolkit.converter.tableau.tds.TableauColumn;
import io.kyligence.zenml.toolkit.converter.tableau.tds.TableauSourceTable;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class TableauFuncUtils {

    public static Pattern colPattern = Pattern.compile("\\[[\\s\\S]+?\\]");

    public static Pattern ifPattern = Pattern.compile(
            "(?i)\\bIF\\b[\\s\\S]+\\bTHEN\\b[\\s\\S]+(\\bELSEIF\\b[\\s\\S]+\\bTHEN\\b[\\s\\S]+)*(\\bELSE\\b[\\s\\S]+)?\\bEND\\b");

    public static Pattern yearPattern = Pattern.compile("(?i)\\byear\\(([^(]+?)\\)");

    public static Pattern quarterPattern = Pattern.compile("(?i)\\bquarter\\(([^(]+?)\\)");

    public static Pattern monthPattern = Pattern.compile("(?i)\\bmonth\\(([^(]+?)\\)");

    public static Pattern dayPattern = Pattern.compile("(?i)\\bday\\(([^(]+?)\\)");

    public static String convertTableauCalculation(String calculation, Map<String, TableauColumn> colMap,
                                                   Map<TableauSourceTable, String> tableAliasMap,
                                                   Map<String, String> ccAliasMap, String factTable) {
        // step 0 qualify cc
        if (calculation.contains("(?i)today()")) {
            log.info("该 CC 包含当前时间函数，不适合在模型中创建");
            return null;
        }
        calculation = calculation.replaceAll("\"", "'");
        // step 1 replace tableau column [TRANS_ID] → KYLIN_SALES.TRANS_ID
        log.info("开始转换可计算列：");
        log.info(calculation);
        Matcher matcher = colPattern.matcher(calculation);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String tableauCol = matcher.group(0);
            String kylinCol = getKylinCol(tableauCol, colMap, tableAliasMap, ccAliasMap, factTable);
            if (kylinCol == null) {
                return null;
            }
            matcher.appendReplacement(sb, kylinCol.toUpperCase());
        }
        matcher.appendTail(sb);
        String replacedCalculation = sb.toString();

        // step 2 replace tableau function, like IF ELSE
        Matcher ifMatcher = ifPattern.matcher(replacedCalculation);
        if (ifMatcher.find()) {
            replacedCalculation = replacedCalculation.replaceAll("(?i)\\bIF\\b", "CASE WHEN");
            replacedCalculation = replacedCalculation.replaceAll("(?i)\\bELSEIF\\b", "WHEN");
        }
        log.info("转换后的结果为：");
        log.info(replacedCalculation);

        // step 3 replace tableau date functions, like day month, quarter, year, but not today
        replacedCalculation = replaceDateFun(replacedCalculation, "YEAR");
        replacedCalculation = replaceDateFun(replacedCalculation, "QUARTER");
        replacedCalculation = replaceDateFun(replacedCalculation, "MONTH");
        replacedCalculation = replaceDateFun(replacedCalculation, "DAY");

        return replacedCalculation;
    }

    public static String replaceDateFun(String calculation, String type) {
        Pattern datePattern = null;
        switch (type) {
            case "YEAR":
                datePattern = yearPattern;
                break;
            case "QUARTER":
                datePattern = quarterPattern;
                break;
            case "MONTH":
                datePattern = monthPattern;
                break;
            case "DAY":
                datePattern = dayPattern;
                break;
        }
        Matcher yearMatcher = datePattern.matcher(calculation);
        StringBuffer buffer = new StringBuffer();
        while (yearMatcher.find()) {
            String arg = yearMatcher.group(1);
            String replaceFun = "";
            if (arg.matches("#.+#")) {
                replaceFun = "EXTRACT(" + type + " FROM CAST('" + arg.replaceAll("#", "") + "' AS DATE))";
            } else {
                replaceFun = "EXTRACT(" + type + " FROM " + arg + ")";
            }
            yearMatcher.appendReplacement(buffer, replaceFun);
        }
        yearMatcher.appendTail(buffer);
        return buffer.toString();
    }

    public static String getKylinCol(String tableauCol, Map<String, TableauColumn> colMap,
                                      Map<TableauSourceTable, String> tableAliasMap, Map<String, String> ccAliasMap, String factTable) {
        TableauColumn tableauColumn = colMap.get(tableauCol);
        if (tableauColumn != null) {
            if (tableauColumn.isCM() || tableauColumn.isCC()) {
                return factTable.toUpperCase() + "." + TableauDialectUtils.removeBracket(ccAliasMap.get(tableauCol));
            } else {
                String tableAlias = tableAliasMap.get(tableauColumn.getSourceColumn().getSourceTable());
                return tableAlias + "." + TableauDialectUtils.removeBracket(tableauColumn.getSourceColumn().getColName());
            }
        } else {
            throw new RuntimeException("can not digest tableau " + tableauCol);
        }
    }
}
