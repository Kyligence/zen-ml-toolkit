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

package io.kyligence.zenml.toolkit.converter.tableau;

import io.kyligence.zenml.toolkit.ZenMlToolkitServer;
import io.kyligence.zenml.toolkit.utils.tableau.TableauRWUtils;
import io.kyligence.zenml.toolkit.utils.tableau.TableauRWUtilsTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest(classes = ZenMlToolkitServer.class)
public class TdsAnalyzerTest {
    private static final String TDS_BASE_DIR = "src/test/resources/sources/tableau/tds/";

    @BeforeAll
    public static void setup() {
        System.setProperty("ZEN_HOME", TableauConverterTest.class.getResource("/").getPath());
    }

    @AfterAll
    public static void clean() {
        System.clearProperty("ZEN_HOME");
    }

    @Test
    public void testAnalyzeTdsFileCase1(){
        var tdsName = "SSB.tds";
        var tdsPath = TDS_BASE_DIR + tdsName;
        var analyzer = new TdsAnalyzer();
        var tds = TableauRWUtils.getTds(new File(tdsPath));
        var tdsSpec = analyzer.analyzeTdsSpec(tds);
        System.out.println(tdsSpec);
    }
}
