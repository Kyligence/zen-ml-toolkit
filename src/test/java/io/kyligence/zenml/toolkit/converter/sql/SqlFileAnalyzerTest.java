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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes = ZenMlToolkitServer.class)
public class SqlFileAnalyzerTest {


    @Test
    public void testParseSelect01() {

        var sql = "select country, sum(amount) as revenue \n" +
                "from line_order \n" +
                "where order_date > '2022-02-02' \n" +
                "group by country";

        var sqlAnalyzer = new SqlFileAnalyzer();
        var sqlMetricSpecs = sqlAnalyzer.parseSqls(sql);
        System.out.println(sqlMetricSpecs);
    }

    @Test
    public void testParseSelect02() {
        var sql = "select country, sum(amount) revenue \n" +
                "from line_order \n" +
                "where (c_city='UNITED KI1' or c_city='UNITED KI5')\n" +
                "and (s_city='UNITED KI1' or s_city='UNITED KI5')\n" +
                "and d_year >= 1992 and d_year <= 1997 \n" +
                "group by country";

        var sqlAnalyzer = new SqlFileAnalyzer();
        var sqlMetricSpecs = sqlAnalyzer.parseSqls(sql);
        System.out.println(sqlMetricSpecs);
    }

    @Test
    public void testParseSelect03() {
        var sql = "select country, sum(amount) / sum(quantity) \n" +
                "from line_order \n" +
                "where (c_city='UNITED KI1' or c_city='UNITED KI5')\n" +
                "and (s_city='UNITED KI1' or s_city='UNITED KI5')\n" +
                "and d_year >= 1992 and d_year <= 1997 \n" +
                "group by country";

        var sqlAnalyzer = new SqlFileAnalyzer();
        var sqlMetricSpecs = sqlAnalyzer.parseSqls(sql);
        System.out.println(sqlMetricSpecs);
    }

    @Test
    public void testParseSelect04() {
        var sql = "select country, sum(amount) / sum(quantity) \n" +
                "from line_order \n" +
                "left join product on line_order.pid = product.pid \n" +
                "where (c_city='UNITED KI1' or c_city='UNITED KI5')\n" +
                "and (s_city='UNITED KI1' or s_city='UNITED KI5')\n" +
                "and d_year >= 1992 and d_year <= 1997 \n" +
                "group by country";

        var sqlAnalyzer = new SqlFileAnalyzer();
        var sqlMetricSpecs = sqlAnalyzer.parseSqls(sql);
        System.out.println(sqlMetricSpecs);
    }

    @Test
    public void testParseSelect05() {
        var sql = "select country, sum(amount) / sum(quantity) \n" +
                "from line_order \n" +
                "left join product on line_order.pid = product.pid \n" +
                "inner join supplier on line_order.uid = supplier.pid \n" +
                "where (c_city='UNITED KI1' or c_city='UNITED KI5')\n" +
                "and (s_city='UNITED KI1' or s_city='UNITED KI5')\n" +
                "and d_year >= 1992 and d_year <= 1997 \n" +
                "group by country";

        var sqlAnalyzer = new SqlFileAnalyzer();
        var sqlMetricSpecs = sqlAnalyzer.parseSqls(sql);
        System.out.println(sqlMetricSpecs);
    }

    @Test
    public void testParseSelect06() {
        var sql = "select sum(lo_revenue) as revenue\n" +
                "from LINEORDER\n" +
                "left join dates on lo_orderdate = d_datekey\n" +
                "where d_year = 1993\n" +
                "and lo_discount between 1 and 3\n" +
                "and lo_quantity < 25;";

        var sqlAnalyzer = new SqlFileAnalyzer();
        var sqlMetricSpecs = sqlAnalyzer.parseSqls(sql);
        System.out.println(sqlMetricSpecs);
    }

    @Test
    public void testParseSelect07() {
        var sql = "select sum(lo_revenue) as revenue\n" +
                "from lineorder\n" +
                "left join dates on lo_orderdate = d_datekey\n" +
                "where d_yearmonthnum = 199401\n" +
                "and lo_discount between 4 and 6\n" +
                "and lo_quantity between 26 and 35;";

        var sqlAnalyzer = new SqlFileAnalyzer();
        var sqlMetricSpecs = sqlAnalyzer.parseSqls(sql);
        System.out.println(sqlMetricSpecs);
    }

    @Test
    public void testParseSelect08() {
        var sql = "select sum(lo_revenue) as revenue\n" +
                "from lineorder\n" +
                "left join dates on lo_orderdate = d_datekey\n" +
                "where d_weeknuminyear = 6 and d_year = 1994\n" +
                "and lo_discount between 5 and 7\n" +
                "and lo_quantity between 26 and 35;";

        var sqlAnalyzer = new SqlFileAnalyzer();
        var sqlMetricSpecs = sqlAnalyzer.parseSqls(sql);
        System.out.println(sqlMetricSpecs);
    }

    @Test
    public void testParseSelect09() {
        var sql = "select sum(lo_revenue) as lo_revenue, d_year, p_brand\n" +
                "from lineorder\n" +
                "left join dates on lo_orderdate = d_datekey\n" +
                "left join part on lo_partkey = p_partkey\n" +
                "left join supplier on lo_suppkey = s_suppkey\n" +
                "where p_category = 'MFGR#12' and s_region = 'AMERICA'\n" +
                "group by d_year, p_brand\n" +
                "order by d_year, p_brand;";

        var sqlAnalyzer = new SqlFileAnalyzer();
        var sqlMetricSpecs = sqlAnalyzer.parseSqls(sql);
        System.out.println(sqlMetricSpecs);
    }

}
