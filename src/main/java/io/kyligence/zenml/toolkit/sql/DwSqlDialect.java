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

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import org.apache.calcite.sql.dialect.SparkSqlDialect;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Set;

public class DwSqlDialect extends SparkSqlDialect {

    private static final List<String> KEYWORD_CONFLICT_NAMES = Lists.newArrayList("PERCENT", "RANK");

    public DwSqlDialect(Context context) {
        super(context);
    }

    public boolean identifierNeedsQuote(String val) {
        // Only quote upon SQL KEYWORD
        if (val.isEmpty())
            return true;

        char begin = val.charAt(0);
        if (!Character.isJavaIdentifierStart(begin))
            return true;

        for (int i = 1; i < val.length(); i++)
            if (!Character.isJavaIdentifierPart(val.charAt(i)))
                return true;

        // reach here, it is a normal SQL identifier
        // finally, check if is sql keyword
        return isSqlKeyword(val);
    }

    public static boolean isSqlKeyword(String s) {
        return SQL_KEYWORDS.contains(s.toUpperCase());
    }

    public static boolean startWithSqlKeyword(String s) {
        return !StringUtils.isBlank(s) && KEYWORD_CONFLICT_NAMES.stream()
                .anyMatch(k -> s.toUpperCase(Locale.ROOT).startsWith(k));
    }

    private static final Set<String> SQL_KEYWORDS = ImmutableSet.<String>builder()
            .add("ABS")
            .add("ADD")
            .add("ALL")
            .add("ALLOCATE")
            .add("ALTER")
            .add("AND")
            .add("ANY")
            .add("ARE")
            .add("ARRAY")
            .add("AS")
            .add("ASENSITIVE")
            .add("ASYMMETRIC")
            .add("AT")
            .add("ATOMIC")
            .add("AUTHORIZATION")
            .add("BEGIN")
            .add("BETWEEN")
            .add("BIGINT")
            .add("BLOB")
            .add("BINARY")
            .add("BOOLEAN")
            .add("BOTH")
            .add("BY")
            .add("CALL")
            .add("CALLED")
            .add("CASCADED")
            .add("CASE")
            .add("CAST")
            .add("CHAR")
            .add("CHARACTER")
            .add("CHECK")
            .add("CLOB")
            .add("CLOSE")
            .add("COLLATE")
            .add("COLUMN")
            .add("COMMIT")
            .add("CONDITION")
            .add("CONNECT")
            .add("CONSTRAINT")
            .add("CONTINUE")
            .add("CORRESPONDING")
            .add("CREATE")
            .add("CROSS")
            .add("CUBE")
            .add("CURRENT")
            .add("CURRENT_DATE")
            .add("CURRENT_PATH")
            .add("CURRENT_ROLE")
            .add("CURRENT_TIME")
            .add("CURRENT_TIMESTAMP")
            .add("CURRENT_USER")
            .add("CURSOR")
            .add("CYCLE")
            .add("DATE")
            .add("DAY")
            .add("DEALLOCATE")
            .add("DEC")
            .add("DECIMAL")
            .add("DECLARE")
            .add("DEFAULT")
            .add("DELETE")
            .add("DEREF")
            .add("DESCRIBE")
            .add("DETERMINISTIC")
            .add("DISCONNECT")
            .add("DISTINCT")
            .add("DO")
            .add("DOUBLE")
            .add("DROP")
            .add("DYNAMIC")
            .add("EACH")
            .add("ELEMENT")
            .add("ELSE")
            .add("ELSIF")
            .add("END")
            .add("ESCAPE")
            .add("EXCEPT")
            .add("EXEC")
            .add("EXECUTE")
            .add("EXISTS")
            .add("EXIT")
            .add("EXTERNAL")
            .add("FALSE")
            .add("FETCH")
            .add("FILTER")
            .add("FLOAT")
            .add("FOR")
            .add("FOREIGN")
            .add("FREE")
            .add("FROM")
            .add("FULL")
            .add("FUNCTION")
            .add("GET")
            .add("GLOBAL")
            .add("GRANT")
            .add("GROUP")
            .add("GROUPING")
            .add("HANDLER")
            .add("HAVING")
            .add("HOLD")
            .add("HOUR")
            .add("IDENTITY")
            .add("IF")
            .add("IMMEDIATE")
            .add("IN")
            .add("INDICATOR")
            .add("INNER")
            .add("INOUT")
            .add("INPUT")
            .add("INSENSITIVE")
            .add("INSERT")
            .add("INT")
            .add("INTEGER")
            .add("INTERSECT")
            .add("INTERVAL")
            .add("INTO")
            .add("IS")
            .add("ITERATE")
            .add("JOIN")
            .add("LANGUAGE")
            .add("LARGE")
            .add("LATERAL")
            .add("LEADING")
            .add("LEAVE")
            .add("LEFT")
            .add("LIKE")
            .add("LOCAL")
            .add("LOCALTIME")
            .add("LOCALTIMESTAMP")
            .add("LOOP")
            .add("MATCH")
            .add("MEMBER")
            .add("MERGE")
            .add("METHOD")
            .add("MINUTE")
            .add("MODIFIES")
            .add("MODULE")
            .add("MONTH")
            .add("WEEK")
            .add("MULTISET")
            .add("NATIONAL")
            .add("NATURAL")
            .add("NCHAR")
            .add("NCLOB")
            .add("NEW")
            .add("NO")
            .add("NONE")
            .add("NOT")
            .add("NULL")
            .add("NUMERIC")
            .add("OF")
            .add("OLD")
            .add("ON")
            .add("ONLY")
            .add("OPEN")
            .add("OR")
            .add("ORDER")
            .add("OUT")
            .add("OUTER")
            .add("OUTPUT")
            .add("OVER")
            .add("OVERLAPS")
            .add("PARAMETER")
            .add("PARTITION")
            .add("PRECISION")
            .add("PREPARE")
            .add("PRIMARY")
            .add("PROCEDURE")
            .add("RANGE")
            .add("READS")
            .add("REAL")
            .add("RECURSIVE")
            .add("REF")
            .add("REFERENCES")
            .add("REFERENCING")
            .add("RELEASE")
            .add("REPEAT")
            .add("RESIGNAL")
            .add("RESULT")
            .add("RETURN")
            .add("RETURNS")
            .add("REVOKE")
            .add("RIGHT")
            .add("ROLLBACK")
            .add("ROLLUP")
            .add("ROW")
            .add("ROWS")
            .add("PERCENT")
            .add("SAVEPOINT")
            .add("SCROLL")
            .add("SEARCH")
            .add("SECOND")
            .add("SELECT")
            .add("SENSITIVE")
            .add("SESSION_USE")
            .add("SET")
            .add("SIGNAL")
            .add("SIMILAR")
            .add("SMALLINT")
            .add("SOME")
            .add("SPECIFIC")
            .add("SPECIFICTYPE")
            .add("SQL")
            .add("SQLEXCEPTION")
            .add("SQLSTATE")
            .add("SQLWARNING")
            .add("START")
            .add("STATIC")
            .add("SUBMULTISET")
            .add("SYMMETRIC")
            .add("SYSTEM")
            .add("SYSTEM_USER")
            .add("TABLE")
            .add("TABLESAMPLE")
            .add("THEN")
            .add("TIME")
            .add("TIMESTAMP")
            .add("TIMEZONE_HOUR")
            .add("TIMEZONE_MINUTE")
            .add("TO")
            .add("TRAILING")
            .add("TRANSLATION")
            .add("TREAT")
            .add("TRIGGER")
            .add("TRUE")
            .add("UNDO")
            .add("UNION")
            .add("UNIQUE")
            .add("UNKNOWN")
            .add("UNNEST")
            .add("UNTIL")
            .add("UPDATE")
            .add("USER")
            .add("USING")
            .add("VALUE")
            .add("VALUES")
            .add("VARCHAR")
            .add("VARYING")
            .add("WHEN")
            .add("WHENEVER")
            .add("WHERE")
            .add("WHILE")
            .add("WINDOW")
            .add("WITH")
            .add("WITHIN")
            .add("WITHOUT")
            .add("YEAR")
            .build();
}


