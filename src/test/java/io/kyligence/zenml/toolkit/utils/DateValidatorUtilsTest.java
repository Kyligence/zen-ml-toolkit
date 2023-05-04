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
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = ZenMlToolkitServer.class)
public class DateValidatorUtilsTest {
    @Test
    public void testIsValid_validDateStr_YYYY() {
        String dateStr = "2020";
        assertTrue(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_invalidDateStr_YYYY() {
        String dateStr = "20201";
        assertFalse(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_validDateStr_YYYY_MM() {
        String dateStr = "2020-01";
        assertTrue(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_invalidDateStr_YYYY_MM() {
        String dateStr = "2020-13";
        assertFalse(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_validDateStr_YYYY_MM_DD() {
        String dateStr = "2020-01-01";
        assertTrue(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_invalidDateStr_YYYY_MM_DD() {
        String dateStr = "2020-01-32";
        assertFalse(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_validDateStr_YYYY_MM_DD_HH_mm() {
        String dateStr = "2020-01-01 12:00";
        assertTrue(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_invalidDateStr_YYYY_MM_DD_HH_mm() {
        String dateStr = "2020-01-01 25:00";
        assertFalse(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_validDateStr_YYYY_MM_DD_HH_mm_ss() {
        String dateStr = "2020-01-01 12:00:00";
        assertTrue(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_invalidDateStr_YYYY_MM_DD_HH_mm_ss() {
        String dateStr = "2020-01-01 12:60:00";
        assertFalse(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_validDateStr_YYYY_SLASH_MM() {
        String dateStr = "2020/01";
        assertTrue(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_invalidDateStr_YYYY_SLASH_MM() {
        String dateStr = "2020/13";
        assertFalse(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_validDateStr_YYYY_SLASH_MM_SLASH_DD() {
        String dateStr = "2020/01/01";
        assertTrue(DateValidatorUtils.isValid(dateStr));
    }
    @Test
    public void testIsValid_invalidDateStr_YYYY_SLASH_MM_SLASH_DD() {
        String dateStr = "2020/01/32";
        assertFalse(DateValidatorUtils.isValid(dateStr));
    }
}
