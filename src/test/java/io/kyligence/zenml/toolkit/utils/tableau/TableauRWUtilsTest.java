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

import io.kyligence.zenml.toolkit.ZenMlToolkitServer;
import io.kyligence.zenml.toolkit.converter.tableau.TableauConverterTest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;

@SpringBootTest(classes = ZenMlToolkitServer.class)
public class TableauRWUtilsTest {
    private static final String TDS_BASE_DIR = "src/test/resources/sources/tableau/tds/";
    private static final String TWB_BASE_DIR = "src/test/resources/sources/tableau/twb/";
    private static final String TWBX_BASE_DIR = "src/test/resources/sources/tableau/twbx/";

    @BeforeAll
    public static void setup() {
        System.setProperty("ZEN_HOME", TableauConverterTest.class.getResource("/").getPath());
    }

    @AfterAll
    public static void clean() {
        System.clearProperty("ZEN_HOME");
    }

    @Test
    public void testReadTdsFile(){
        var tdsName = "SSB.tds";
        var tdsPath = TDS_BASE_DIR + tdsName;
        var tds = TableauRWUtils.getTds(new File(tdsPath));
        Assertions.assertEquals("Kylin_SSB", tds.getFormattedName());
        Assertions.assertEquals("yes", tds.getAliases().getEnabled());
        Assertions.assertEquals(63, tds.getColumns().size());
        Assertions.assertEquals(1, tds.getSemanticValues().getSemanticValueList().size());
        Assertions.assertEquals("federated", tds.getTableauConnection().getClassName());
        Assertions.assertEquals(59, tds.getTableauConnection().getCols().getCols().size());
    }


    @Test
    public void testReadTwbFile(){
        var twbName = "SuperStoreSample.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var twb = TableauRWUtils.readTwb(twbPath);
        Assertions.assertEquals(1, twb.getWorksheets().getWorksheets().size());
        Assertions.assertEquals(2, twb.getDatasources().getDatasources().size());
        Assertions.assertEquals("Parameters", twb.getDatasources().getDatasources().get(0).getName());
        Assertions.assertEquals(2, twb.getDatasources().getDatasources().get(0).getColumns().size());
        Assertions.assertEquals("Sample - Superstore", twb.getDatasources().getDatasources().get(1).getName());
        Assertions.assertEquals(16, twb.getDatasources().getDatasources().get(1).getColumns().size());
    }
}
