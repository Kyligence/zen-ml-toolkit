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

package io.kyligence.zenml.toolkit.entry;

import io.kyligence.zenml.toolkit.converter.ConverterFactory;
import io.kyligence.zenml.toolkit.converter.FileType;
import io.kyligence.zenml.toolkit.converter.MetricsConverter;
import io.kyligence.zenml.toolkit.metrics.MetricSpec;
import io.kyligence.zenml.toolkit.metrics.Metrics;
import io.kyligence.zenml.toolkit.utils.YamlUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class ZenGenerator {
    private final ConverterFactory converterFactory = new ConverterFactory();

    public void generateZenMetrics(String srcPath, String destDir) throws IOException {
        MetricsConverter converter = converterFactory.getMetricsConverter(srcPath);

        log.info("Begin to convert metrics from {}", srcPath);
        Metrics metrics = converter.convert2Metrics(srcPath);

        List<MetricSpec> metricSpecs = metrics.getMetrics();
        log.info("{} metrics extracted",metricSpecs.size() );
        for(MetricSpec ms : metricSpecs){
            log.info("    - Metrics Name: {}, Expression:{}", ms.getName(), ms.getExpression());
        }


        String destPath = getFullOutputPath(srcPath, destDir);

        log.info("Begin to write metrics to path: {}", destPath);
        YamlUtils.writeValue(new File(destPath), metrics);
        log.info("Metrics file generated successfully, location: {}", destPath);
    }

    private String getFullOutputPath(String srcPath, String destDir) {
        String fileName = FilenameUtils.getBaseName(srcPath);
        String destFileName = fileName + "." + FileType.ZEN_FILE;
        Path path = Paths.get(destDir, destFileName);
        return path.toString();
    }
}
