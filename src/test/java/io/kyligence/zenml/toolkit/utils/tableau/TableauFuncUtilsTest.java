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
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ZenMlToolkitServer.class)
public class TableauFuncUtilsTest {
    @BeforeAll
    public static void setup() {
        System.setProperty("ZEN_HOME", TableauRWUtilsTest.class.getResource("/").getPath());
    }

    @AfterAll
    public static void clean() {
        System.clearProperty("ZEN_HOME");
    }

    @Test
    public void testReplaceCalculation(){
        String replacedCalculation = "if year(#2018-09-11#) and year(#2011-09-11#) and month([cat])";
        replacedCalculation = TableauFuncUtils.replaceDateFun(replacedCalculation, "YEAR");
        replacedCalculation = TableauFuncUtils.replaceDateFun(replacedCalculation, "MONTH");
        replacedCalculation = TableauFuncUtils.replaceDateFun(replacedCalculation, "DAY");
        replacedCalculation = TableauFuncUtils.replaceDateFun(replacedCalculation, "QUARTER");
        System.out.println(replacedCalculation);
    }
}
