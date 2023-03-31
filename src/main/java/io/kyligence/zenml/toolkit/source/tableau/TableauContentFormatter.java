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

import io.kyligence.zenml.toolkit.config.ToolkitConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TableauContentFormatter {

    public static final String IDENTIFIER_REGEX = "[\\[\\]]";
    public static final String WHITE_SPACE = " ";

    public static final String DOT = ".";
    public static final String DOT_REGEX = "\\.";
    public static final String MATH_OPERATOR_REGEX = "((?<=[+\\-*/])|(?=[+\\-*/]))";
    public static final String PARENTHESIS_REGEX = "((?<=[()])|(?=[()]))";

    public static final String FORMAT_SQL_STYLE = "sql";
    public static final String FORMAT_ORIGIN_STYLE = "origin";


    private boolean useColumnAlias = false;
    private ToolkitConfig config = ToolkitConfig.getInstance();


    private boolean usingOriginFormatStyle() {
        return StringUtils.equalsIgnoreCase(config.getTableauParseFormat(), FORMAT_ORIGIN_STYLE);
    }

    // replace [ ] to blank
    // replace whitespace and underscore to dash -
    // to lowercase
    public String formatIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }

        if (usingOriginFormatStyle())
            return identifier;


        return identifier.replaceAll(IDENTIFIER_REGEX, "").replaceAll(WHITE_SPACE, "_").replaceAll("[/-]", "_").trim()
                .toLowerCase();
    }


    public String formatName(String name) {
        if (name == null) {
            return null;
        }
        if (usingOriginFormatStyle())
            return name;

        return name.replaceAll(IDENTIFIER_REGEX, "").trim().toLowerCase();
    }

    public String formatFormula(String formula, Map<String, String> columnAlias) {
        if (formula == null) {
            return null;
        }

        if (useColumnAlias) {
            var sb = new StringBuilder();
            if (formula.contains("+") || formula.contains("-") || formula.contains("*") || formula.contains("/")) {
                String[] parts = formula.split(MATH_OPERATOR_REGEX);
                for (int i = 0; i < parts.length; i++) {
                    var part = parts[i];
                    if (part.contains("(") || part.contains(")")) {
                        String[] subParts = part.split(PARENTHESIS_REGEX);
                        for (String subpart : subParts) {
                            var identifier = getColumnNameFromAlias(columnAlias, subpart);
                            sb.append(removeTableIdentifier(identifier));
                        }
                    } else {
                        sb.append(part.toLowerCase());
                    }

                    if (i < parts.length - 1) {
                        sb.append(" ");
                    }
                }
            } else if (formula.contains("(") || formula.contains(")")) {
                String[] subParts = formula.split(PARENTHESIS_REGEX);
                for (String subpart : subParts) {
                    var identifier = getColumnNameFromAlias(columnAlias, subpart);
                    sb.append(removeTableIdentifier(identifier));
                }
            } else {
                var identifier = getColumnNameFromAlias(columnAlias, formula);
                sb.append(removeTableIdentifier(identifier));
            }

            return sb.toString();
        } else {
            return formula.toLowerCase();
        }

    }

    public String getColumnNameFromAlias(Map<String, String> columnAlias, String alias) {
        if (useColumnAlias) {
            return columnAlias.getOrDefault(alias, formatIdentifier(alias));
        } else {
            return formatIdentifier(alias);
        }
    }

    public String removeTableIdentifier(String column) {
        if (usingOriginFormatStyle())
            return column;

        if (column.contains(DOT)) {
            String[] parts = column.split(DOT_REGEX);
            return parts[1];
        }
        return column;
    }

}
