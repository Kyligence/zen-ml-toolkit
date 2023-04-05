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

package io.kyligence.zenml.toolkit.utils.excel;

import io.kyligence.zenml.toolkit.converter.FileType;
import io.kyligence.zenml.toolkit.model.zenml.Metrics;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

@Slf4j
public class ExcelWriter {

    private final Workbook workbook;
    private static final String HEADER_METRIC = "Metric";
    private static final String HEADER_EXPRESSION = "Expression";

    public ExcelWriter() {
        this.workbook = new XSSFWorkbook();
    }

    public Sheet createSheet(String sheetName) {
        var sheet = workbook.createSheet(sheetName);
        sheet.setColumnWidth(0, 50 * 256);
        sheet.setColumnWidth(1, 150 * 256);
        return sheet;
    }

    public void createHeader(Sheet sheet) {
        var header = sheet.createRow(0);
        var headerStyle = workbook.createCellStyle();
        headerStyle.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

        var headerCell = header.createCell(0);
        headerCell.setCellValue(HEADER_METRIC);
        headerCell.setCellStyle(headerStyle);

        headerCell = header.createCell(1);
        headerCell.setCellValue(HEADER_EXPRESSION);
        headerCell.setCellStyle(headerStyle);
    }

    public void writeMetricsToSheet(Sheet sheet, Metrics metrics) {
        var cellStyle = workbook.createCellStyle();
        cellStyle.setWrapText(true);

        var ms = metrics.getMetrics();
        for (var i = 0; i < ms.size(); i++) {
            var metric = ms.get(i);
            var row = sheet.createRow(i + 1);
            var metricCell = row.createCell(0);
            metricCell.setCellValue(metric.getName());
            metricCell.setCellStyle(cellStyle);

            var exprCell = row.createCell(1);
            exprCell.setCellValue(metric.getExpression());
            exprCell.setCellStyle(cellStyle);
        }
    }

    public String writeExcelFile(String destDir, String fileName) throws IOException {
        var path = destDir + File.separator + fileName + "." + FileType.EXCEL_FILE;
        var outputStream = new FileOutputStream(path);
        workbook.write(outputStream);
        workbook.close();
        return path;
    }

}
