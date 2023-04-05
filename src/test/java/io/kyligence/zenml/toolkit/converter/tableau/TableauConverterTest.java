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
import org.dom4j.DocumentException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ZenMlToolkitServer.class)
public class TableauConverterTest {

    private static final String TDS_BASE_DIR = "src/test/resources/sources/tableau/tds/";

    private static final String TWB_BASE_DIR = "src/test/resources/sources/tableau/twb/";

    @BeforeAll
    public static void setup() {
        System.setProperty("ZEN_HOME", TableauConverterTest.class.getResource("/").getPath());
        System.setProperty("PROPERTIES_PATH",TableauConverterTest.class.getResource("/").getPath());

    }

    @AfterAll
    public static void clean() {
        System.clearProperty("ZEN_HOME");
        System.clearProperty("PROPERTIES_PATH");
    }

    @Test
    public void testConvert2ZenMLCase1() throws DocumentException {
        var tdsName = "SSB.tds";
        var tdsPath = TDS_BASE_DIR + tdsName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(tdsPath);
        Assertions.assertEquals(4, metrics.getMetrics().size());
    }


    @Test
    public void testTwb2ZenMLCase1() throws DocumentException {
        var twbName = "twb1.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(16, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase2() throws DocumentException {
        var twbName = "twb2.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(7, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase3() throws DocumentException {
        var twbName = "twb3.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(10, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase4() throws DocumentException {
        var twbName = "twb4.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(3, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase5() throws DocumentException {
        var twbName = "twb5.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(28, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase6() throws DocumentException {
        var twbName = "twb6.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(6, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase7() throws DocumentException {
        var twbName = "twb7.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(19, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase8() throws DocumentException {
        var twbName = "twb8.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(3, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase9() throws DocumentException {
        var twbName = "twb9.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(6, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase10() throws DocumentException {
        var twbName = "twb10.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(8, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase11() throws DocumentException {
        var twbName = "twb11.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(15, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase12() throws DocumentException {
        var twbName = "twb12.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(2, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase13() throws DocumentException {
        var twbName = "small is beautiful - TP.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(9, metrics.getMetrics().size());
    }

    @Test
    public void testTwb2ZenMLCase14() throws DocumentException {
        var twbName = "US Fruit Consumption.twb";
        var twbPath = TWB_BASE_DIR + twbName;
        var converter = new TableauConverter();
        var metrics = converter.convert2Metrics(twbPath);
        Assertions.assertEquals(3, metrics.getMetrics().size());
    }
}
