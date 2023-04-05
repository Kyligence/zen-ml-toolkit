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

public class TableauDialectUtils {
    public static final String OP_EQUALS = "=";
    public static final String OP_AND = "AND";



    public static String removeBracket(String identifier) {
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

    //非特殊字符包括数字字母下划线
    public static boolean containSpecialChar(String name) {
        return !name.matches("[a-zA-Z0-9_]+");
    }
}
