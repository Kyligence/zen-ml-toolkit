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

import io.kyligence.zenml.toolkit.ZenMlToolkitServer;
import io.kyligence.zenml.toolkit.converter.tableau.TableauConverterTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ZenMlToolkitServer.class)
public class SqlConverterTest {
    private static final String SQL_BASE_DIR = "src/test/resources/sources/sql/";

    @BeforeAll
    public static void setup() {
        System.setProperty("ZEN_HOME", TableauConverterTest.class.getResource("/").getPath());
    }

    @AfterAll
    public static void clean() {
        System.clearProperty("ZEN_HOME");
    }

    @Test
    public void testConvertSql2ZenMLCase0() {
        var sqlFile = "ssb.sql";
        var sqlFilePath = SQL_BASE_DIR + sqlFile;
        var converter = new SqlConverter();
        var metrics = converter.convert2Metrics(sqlFilePath);
        Assertions.assertEquals(4, metrics);
    }
}
