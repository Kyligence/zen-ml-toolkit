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

package io.kyligence.zenml.toolkit.converter;

import io.kyligence.zenml.toolkit.converter.excel.ExcelConverter;
import io.kyligence.zenml.toolkit.converter.sql.SqlConverter;
import io.kyligence.zenml.toolkit.converter.tableau.TableauConverter;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

public class ConverterFactory {
    public MetricsConverter getMetricsConverter(String filePath) {
        var suffix = FilenameUtils.getExtension(filePath);
        if (StringUtils.isBlank(filePath))
            throw new IllegalArgumentException("Source file path is null, please check the file name");

        if (StringUtils.equalsIgnoreCase(suffix, FileType.TDS_FILE)) {
            return new TableauConverter();
        } else if (StringUtils.equalsIgnoreCase(suffix, FileType.TWB_FILE)) {
            return new TableauConverter();
        } else if (StringUtils.equalsIgnoreCase(suffix, FileType.EXCEL_FILE)) {
            return new ExcelConverter();
        } else if (StringUtils.equalsIgnoreCase(suffix, FileType.SQL_FILE)) {
            return new SqlConverter();
        } else {
            throw new IllegalArgumentException("Current file type is not supported.");
        }
    }
}
