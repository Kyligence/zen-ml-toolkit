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

import io.kyligence.zenml.toolkit.config.ToolkitConfig;
import org.apache.commons.lang3.StringUtils;

public class TableauDialectUtils {
    public static final String OP_EQUALS = "=";
    public static final String OP_AND = "AND";

    public static final String LOWER_CASE = "lower";
    public static final String UPPER_CASE = "upper";

    private static ToolkitConfig config = ToolkitConfig.getInstance();

    public static String formatIdentifierCase(String identifier) {
        if (StringUtils.isEmpty(identifier))
            return identifier;

        var caseStyle = config.getIdentifierCaseFormatStyle();
        if (StringUtils.equalsIgnoreCase(LOWER_CASE, caseStyle)) {
            return identifier.toLowerCase();
        } else if (StringUtils.equalsIgnoreCase(UPPER_CASE, caseStyle)) {
            return identifier.toUpperCase();
        } else {
            return identifier;
        }
    }

    public static String formatIdentifier(String identifier) {
        if (StringUtils.isEmpty(identifier))
            return identifier;
        identifier = removeBracket(identifier);
        identifier = identifier.replaceAll("-", "_");
        identifier = identifier.replaceAll(" ", "");
        identifier = identifier.replaceAll("/", "_");

        return formatIdentifierCase(identifier);
    }


    public static String removeBracket(String identifier) {
        if (StringUtils.isEmpty(identifier))
            return identifier;
        identifier = identifier.replaceAll("\\[", "");
        identifier = identifier.replaceAll("\\]", "");
        return identifier;
    }

    public static String addBracket(String identifier) {
        return "[" + identifier + "]";
    }

    public static String getSchema(String schemaTable) {
        schemaTable = removeBracket(schemaTable);
        var cut = schemaTable.indexOf('.');
        return schemaTable.substring(0, cut);
    }

    public static String getTable(String schemaTable) {
        schemaTable = removeBracket(schemaTable);
        var cut = schemaTable.indexOf('.');
        return schemaTable.substring(cut + 1, schemaTable.length());
    }

    public static boolean containSpecialChar(String name) {
        return !name.matches("[a-zA-Z0-9_]+");
    }
}
