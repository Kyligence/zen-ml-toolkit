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
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

@Slf4j
public class TableauExpressionUtils {

    public static Pattern colPattern = Pattern.compile("\\[[\\s\\S]+?\\]");

    public static Pattern ifPattern = Pattern.compile(
            "(?i)\\bIF\\b[\\s\\S]+\\bTHEN\\b[\\s\\S]+(\\bELSEIF\\b[\\s\\S]+\\bTHEN\\b[\\s\\S]+)*(\\bELSE\\b[\\s\\S]+)?\\bEND\\b");

    public static Pattern yearPattern = Pattern.compile("(?i)\\byear\\(([^(]+?)\\)");

    public static Pattern quarterPattern = Pattern.compile("(?i)\\bquarter\\(([^(]+?)\\)");

    public static Pattern monthPattern = Pattern.compile("(?i)\\bmonth\\(([^(]+?)\\)");

    public static Pattern dayPattern = Pattern.compile("(?i)\\bday\\(([^(]+?)\\)");

    private static final List<String> tableauAggFunList = Arrays.asList("SUM", "MIN", "MAX", "AVG", "AGG", "COUNTD",
            "COUNT");

    private static final String simpleAgg = "(SUM|MIN|MAX|AVG|COUNTD|COUNT)\\((.+?\\))";

    private static Pattern simpleAggPattern = Pattern.compile(simpleAgg);


    public static String convertTableauCalculation(String calculation) {
        // step 0
        calculation = calculation.replaceAll("\"", "'");
        calculation = calculation.replaceAll("\n", " ");

        // step 1 format column identifier
        var matcher = colPattern.matcher(calculation);
        var sb = new StringBuffer();
        while (matcher.find()) {
            var tableauCol = matcher.group(0);
            var formattedCol = TableauDialectUtils.formatIdentifier(tableauCol);
            matcher.appendReplacement(sb, formattedCol.toUpperCase());
        }
        matcher.appendTail(sb);
        var replacedCalculation = sb.toString();

        // step 2 replace tableau function, like IF ELSE
        var ifMatcher = ifPattern.matcher(replacedCalculation);
        if (ifMatcher.find()) {
            replacedCalculation = replacedCalculation.replaceAll("(?i)\\bIF\\b", "CASE WHEN");
            replacedCalculation = replacedCalculation.replaceAll("(?i)\\bELSEIF\\b", "WHEN");
        }


        // step 3 replace tableau date functions, like day month, quarter, year, but not today
        replacedCalculation = replaceDateFun(replacedCalculation, "YEAR");
        replacedCalculation = replaceDateFun(replacedCalculation, "QUARTER");
        replacedCalculation = replaceDateFun(replacedCalculation, "MONTH");
        replacedCalculation = replaceDateFun(replacedCalculation, "DAY");

        // step4  format case

        replacedCalculation = TableauDialectUtils.formatIdentifierCase(replacedCalculation);

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
        var yearMatcher = datePattern.matcher(calculation);
        var buffer = new StringBuffer();
        while (yearMatcher.find()) {
            var arg = yearMatcher.group(1);
            var replaceFun = "";
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
}
