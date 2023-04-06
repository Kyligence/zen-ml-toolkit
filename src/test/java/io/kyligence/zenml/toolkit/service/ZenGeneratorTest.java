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

import io.kyligence.zenml.toolkit.ZenMlToolkitServer;
import io.kyligence.zenml.toolkit.config.ToolkitConfig;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.UUID;

@SpringBootTest(classes = ZenMlToolkitServer.class)
public class ZenGeneratorTest {

    private static final String destDirPath = Path.of(FileUtils.getTempDirectoryPath(),
            "zen-ml-toolkit").toString();

    @BeforeAll
    public static void setup() {
        var destDir = new File(destDirPath);
        FileUtils.deleteQuietly(destDir);
        destDir.mkdirs();
        System.setProperty("ZEN_HOME", ZenGeneratorTest.class.getResource("/").getPath());
    }

    @AfterAll
    public static void clean() {
        ToolkitConfig config = ToolkitConfig.getInstance();
        FileUtils.deleteQuietly(new File(destDirPath));
        FileUtils.deleteQuietly(new File(config.getLocalTmpFolder()));
        System.clearProperty("ZEN_HOME");
    }

    @Test
    public void testGenerateZenMetrics() throws IOException {
        var tdsPath = "src/test/resources/sources/tableau/tds/superstore.tds";
        var destPath = Path.of(destDirPath, "superstore.zen.yml");
        var destFile = destPath.toFile();
        var generator = new ZenGenerator();
        generator.convertMetrics2ZenMlFile(tdsPath, destDirPath);
        Assertions.assertTrue(destFile.exists());
    }

    @Test
    public void testGenerateZenMetricsZip() throws IOException {
        var tdsPath = "src/test/resources/sources/tableau/tds/superstore.tds";
        var generator = new ZenGenerator();
        var uuid = UUID.randomUUID().toString();
        var zipPath = generator.generateZenMetricsZip(tdsPath, uuid);
        Assertions.assertTrue(new File(zipPath).exists());
    }
}
