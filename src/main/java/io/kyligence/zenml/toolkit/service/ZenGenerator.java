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

package io.kyligence.zenml.toolkit.service;

import io.kyligence.zenml.toolkit.converter.ConverterFactory;
import io.kyligence.zenml.toolkit.converter.FileType;
import io.kyligence.zenml.toolkit.metrics.MetricSpec;
import io.kyligence.zenml.toolkit.metrics.Metrics;
import io.kyligence.zenml.toolkit.tool.excel.ExcelWriter;
import io.kyligence.zenml.toolkit.utils.WorkDirUtils;
import io.kyligence.zenml.toolkit.utils.YamlUtils;
import io.kyligence.zenml.toolkit.utils.ZipUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

@Slf4j
@Service
public class ZenGenerator {
    private final ConverterFactory converterFactory = new ConverterFactory();

    /**
     * For rest api entry
     *
     * @param srcPath
     * @return path of zipped file
     */
    public String generateZenMetricsZip(String srcPath, String uuid) throws IOException {
        // create folder
        var outputDirPath = WorkDirUtils.getOutputFolder2Compress(srcPath, uuid);
        log.info("Create folder which to be compressed, path: {}", outputDirPath);
        // generate zen ml file & excel file
        convertMetrics2ZenMLFileAndExcelFile(srcPath, outputDirPath);
        // compress folder to zip
        var zipFilePath = outputDirPath + "." + FileType.ZIP_FILE;
        ZipUtils.compressZipFile(outputDirPath, zipFilePath);
        FileUtils.deleteQuietly(new File(outputDirPath));
        log.info("Folder has been compressed, the folder will be removed, zip path: {}", zipFilePath);
        return zipFilePath;
    }

    /**
     * For CLI entry
     *
     * @param srcPath
     * @param destDir
     * @throws IOException
     */
    public void convertMetrics2ZenMlFile(String srcPath, String destDir) throws IOException {
        var metrics = extractMetrics(srcPath);
        writeZenMlFile(srcPath, destDir, metrics);
    }

    public void convertMetrics2ZenMLFileAndExcelFile(String srcPath, String destDir) throws IOException {
        var metrics = extractMetrics(srcPath);
        writeZenMlFile(srcPath, destDir, metrics);
        writeExcelFile(srcPath, destDir, metrics);
    }

    private Metrics extractMetrics(String srcPath) {
        var converter = converterFactory.getMetricsConverter(srcPath);

        log.info("Begin to convert metrics from {}", srcPath);
        var metrics = converter.convert2Metrics(srcPath);

        var metricSpecs = metrics.getMetrics();
        log.info("{} metrics extracted", metricSpecs.size());
        for (MetricSpec ms : metricSpecs) {
            log.info("    - Metrics Name: {}, Expression: {}", ms.getName(), ms.getExpression());
        }
        return metrics;
    }

    private void writeZenMlFile(String srcPath, String destDir, Metrics metrics) throws IOException {
        var destPath = getFullOutputPathOfZenFile(srcPath, destDir);

        log.info("Begin to write metrics to zenml file");
        YamlUtils.writeValue(new File(destPath), metrics);
        log.info("Metrics file generated successfully, location: {}", destPath);
    }

    private void writeExcelFile(String srcPath, String destDir, Metrics metrics) throws IOException {
        log.info("Begin to write metrics to Excel file");
        var writer = new ExcelWriter();
        var fileName = FilenameUtils.getBaseName(srcPath);
        var sheet = writer.createSheet(fileName);
        writer.createHeader(sheet);
        writer.writeMetricsToSheet(sheet, metrics);
        var filePath = writer.writeExcelFile(destDir, fileName);
        log.info("Excel file generate successfully, location: {}", filePath);
    }

    private String getFullOutputPathOfZenFile(String srcPath, String destDir) {
        var fileName = FilenameUtils.getBaseName(srcPath);
        var destFileName = fileName + "." + FileType.ZEN_FILE;
        var path = Paths.get(destDir, destFileName);
        return path.toString();
    }
}
