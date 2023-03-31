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

import io.kyligence.zenml.toolkit.ZenMlToolkitServer;
import org.dom4j.DocumentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = ZenMlToolkitServer.class)
public class TableauParserTest {
    private static final String TDS_BASE_DIR = "src/test/resources/sources/tableau/tds/";
    private static final String TWB_BASE_DIR = "src/test/resources/sources/tableau/twb/";
    private static final String TWBX_BASE_DIR = "src/test/resources/sources/tableau/twbx/";


    @Test
    public void testParseTdsFileCase1() throws DocumentException {
        var tdsName = "SSB.tds";
        var tdsPath = TDS_BASE_DIR + tdsName;
        var parser = new TableauParser();
        List<TableauCalculatedFields> calculatedFields = parser.parseTdsFile(tdsPath);
        Assertions.assertEquals(1, calculatedFields.size());
        Assertions.assertEquals(63, calculatedFields.get(0).getColumns().size());
        Assertions.assertEquals(2, calculatedFields.get(0).getTags().size());
    }

    @Test
    public void testParseTdsFileCase2() throws DocumentException {
        var tdsName = "superstore.tds";
        var tdsPath = TDS_BASE_DIR + tdsName;
        TableauParser parser = new TableauParser();
        List<TableauCalculatedFields> calculatedFields = parser.parseTdsFile(tdsPath);
        Assertions.assertEquals(1, calculatedFields.size());
        Assertions.assertEquals(28, calculatedFields.get(0).getColumns().size());
        Assertions.assertEquals(2, calculatedFields.get(0).getTags().size());
    }

    @Test
    public void testParseTdsFileCase3() throws DocumentException {
        var tdsName = "Sample-Superstore.tds";
        var tdsPath = TDS_BASE_DIR + tdsName;
        var parser = new TableauParser();
        List<TableauCalculatedFields> calculatedFields = parser.parseTdsFile(tdsPath);
        Assertions.assertEquals(1, calculatedFields.size());
        Assertions.assertEquals(16, calculatedFields.get(0).getColumns().size());
        Assertions.assertEquals(2, calculatedFields.get(0).getTags().size());
    }

    @Test
    public void testParseTwbFileCase1() throws DocumentException {
        var twbName = "SuperStoreSample.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var parser = new TableauParser();
        List<TableauCalculatedFields> calculatedFields = parser.parseTwbFile(twbPath);
        Assertions.assertEquals(2, calculatedFields.size());
        Assertions.assertEquals(16, calculatedFields.get(1).getColumns().size());
        Assertions.assertEquals(2, calculatedFields.get(0).getTags().size());
    }

    @Test
    public void testParseTwbFileCase2() throws DocumentException {
        var twbName = "twb_test_1.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var parser = new TableauParser();
        List<TableauCalculatedFields> calculatedFields = parser.parseTwbFile(twbPath);
        Assertions.assertEquals(2, calculatedFields.size());
        Assertions.assertEquals(16, calculatedFields.get(1).getColumns().size());
        Assertions.assertEquals(2, calculatedFields.get(0).getTags().size());
    }

}
