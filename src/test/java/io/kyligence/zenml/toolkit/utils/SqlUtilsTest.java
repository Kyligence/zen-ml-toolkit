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

package io.kyligence.zenml.toolkit.utils;

import io.kyligence.zenml.toolkit.ZenMlToolkitServer;
import org.apache.calcite.util.Pair;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest(classes = ZenMlToolkitServer.class)
public class SqlUtilsTest {

    @Test
    public void testQuoteKeyword() {
        String expr = "sum(order.sales)";
        String quotedExpr = SqlUtils.quoteKeyword(expr);
        Assertions.assertEquals("sum(\"order\".\"sales\")", quotedExpr);

        expr = "sum(order.profits)/sum(order.sales)";
        quotedExpr = SqlUtils.quoteKeyword(expr);
        Assertions.assertEquals("sum(\"order\".\"profits\")/sum(\"order\".\"sales\")", quotedExpr);

        expr = "count(1)";
        quotedExpr = SqlUtils.quoteKeyword(expr);
        Assertions.assertEquals("count(1)", quotedExpr);

        expr = "count(*)";
        quotedExpr = SqlUtils.quoteKeyword(expr);
        Assertions.assertEquals("count(*)", quotedExpr);

        expr = "count(sales)";
        quotedExpr = SqlUtils.quoteKeyword(expr);
        Assertions.assertEquals("count(sales)", quotedExpr);
    }

    @Test
    public void testGetSingleNameIdentifiers() {
        String expr = "sum(order.sales)";
        List<String> identifiers = SqlUtils.getSingleNameIdentifiers(expr);
        Assertions.assertEquals(0, identifiers.size());

        expr = "sum(sales)";
        identifiers = SqlUtils.getSingleNameIdentifiers(expr);
        Assertions.assertEquals(1, identifiers.size());
        Assertions.assertEquals("sales", identifiers.get(0));

        expr = "sum(profits)/sum(sales)";
        identifiers = SqlUtils.getSingleNameIdentifiers(expr);
        Assertions.assertEquals(2, identifiers.size());
        Assertions.assertTrue(identifiers.contains("sales"));
        Assertions.assertTrue(identifiers.contains("profits"));

        expr = "count(*)";
        identifiers = SqlUtils.getSingleNameIdentifiers(expr);
        Assertions.assertEquals(0, identifiers.size());

        expr = "count(1)";
        identifiers = SqlUtils.getSingleNameIdentifiers(expr);
        Assertions.assertEquals(0, identifiers.size());
    }

    @Test
    public void testGetExpCallAndArgs() {
        String expr = "sum(order.sales)";
        Pair<String, List<String>> pairs = SqlUtils.getExpCallAndArgs(expr);
        Assertions.assertEquals("sum", pairs.getKey());
        Assertions.assertTrue(pairs.getValue().contains("order.sales"));

        expr = "SUM(sales)";
        pairs = SqlUtils.getExpCallAndArgs(expr);
        Assertions.assertEquals("SUM", pairs.getKey());
        Assertions.assertTrue(pairs.getValue().contains("sales"));

        expr = "SUM(order.profits) / sum(order.sales)";
        pairs = SqlUtils.getExpCallAndArgs(expr);
        Assertions.assertEquals("/", pairs.getKey());
        Assertions.assertTrue(pairs.getValue().contains("SUM(`order`.`profits`)"));
        Assertions.assertTrue(pairs.getValue().contains("SUM(`order`.`sales`)"));

        expr = "sum(order.profits) - sum(order.sales)";
        pairs = SqlUtils.getExpCallAndArgs(expr);
        Assertions.assertEquals("-", pairs.getKey());
        Assertions.assertTrue(pairs.getValue().contains("SUM(`order`.`profits`)"));
        Assertions.assertTrue(pairs.getValue().contains("SUM(`order`.`sales`)"));

        expr = "count(*)";
        pairs = SqlUtils.getExpCallAndArgs(expr);
        Assertions.assertEquals("count", pairs.getKey());
        Assertions.assertEquals("count", pairs.getKey());
    }

    @Test
    public void testGetFUllNameIdentifiers() {
        String expr = "sum(order.sales)";
        List<String> rets = SqlUtils.getFullNameIdentifiers(expr);
        Assertions.assertEquals("order.sales", rets.get(0));

        expr = "sum(order.profits) - sum(order.sales)";
        rets = SqlUtils.getFullNameIdentifiers(expr);
        Assertions.assertEquals(2, rets.size());

        expr = "count(sales)";
        rets = SqlUtils.getFullNameIdentifiers(expr);
        Assertions.assertEquals("sales", rets.get(0));

        expr = "count(*)";
        rets = SqlUtils.getFullNameIdentifiers(expr);
        Assertions.assertEquals(0, rets.size());

        expr = "count(1)";
        rets = SqlUtils.getFullNameIdentifiers(expr);
        System.out.println(rets);
        Assertions.assertEquals(0, rets.size());
    }
}
