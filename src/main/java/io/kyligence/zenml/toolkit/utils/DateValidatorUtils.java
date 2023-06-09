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

import lombok.experimental.UtilityClass;
import org.apache.commons.validator.GenericValidator;

@UtilityClass
public class DateValidatorUtils {

    public static final String YYYY = "yyyy";
    public static final String YYYY_MM = "yyyy-MM";
    public static final String YYYYMM = "yyyyMM";
    public static final String YYYY_MM_DD = "yyyy-MM-dd";
    public static final String YYYYMMDD = "yyyyMMdd";
    public static final String YYYY_MM_DD_HH_mm = "yyyy-MM-dd HH:mm";
    public static final String YYYY_MM_DD_HH_mm_ss = "yyyy-MM-dd HH:mm:ss";
    public static final String YYYY_SLASH_MM = "yyyy/MM";
    public static final String YYYY_SLASH_MM_SLASH_DD = "yyyy/MM/dd";


    public static boolean isValid(String dateStr) {
        return isValid(dateStr, YYYY) ||
                isValid(dateStr, YYYY_MM) ||
                isValid(dateStr, YYYYMM) ||
                isValid(dateStr, YYYY_MM_DD) ||
                isValid(dateStr, YYYYMMDD) ||
                isValid(dateStr, YYYY_MM_DD_HH_mm) ||
                isValid(dateStr, YYYY_MM_DD_HH_mm_ss) ||
                isValid(dateStr, YYYY_SLASH_MM) ||
                isValid(dateStr, YYYY_SLASH_MM_SLASH_DD);
    }

    public static boolean isValid(String dateStr, String dateFormat) {
        return GenericValidator.isDate(dateStr, dateFormat, true);
    }
}
