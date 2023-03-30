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

package io.kyligence.zenml.toolkit.controller;

import io.kyligence.zenml.toolkit.service.DownloadService;
import io.kyligence.zenml.toolkit.service.UploadService;
import io.kyligence.zenml.toolkit.service.ZenGenerator;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
public class ZenMlController {

    @Autowired
    private UploadService uploadFileService;

    @Autowired
    private DownloadService downloadService;

    @Autowired
    private ZenGenerator generator;

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @PostMapping(value = "/convert_metrics")
    public void uploadAndConvertMetrics(@RequestParam("file") MultipartFile file, final HttpServletResponse response) throws IOException {
        var uuid = UUID.randomUUID().toString();
        var srcFilePath = uploadFileService.uploadFile(file, uuid);
        var outputFilePath = generator.generateZenMetricsZip(srcFilePath, uuid);
        downloadService.download(outputFilePath, response);
    }

    @PostMapping(value = "/upload")
    public Map<String, String> upload(@RequestParam("files") MultipartFile file) throws IOException {
        var uuid = UUID.randomUUID().toString();
        var srcFilePath = uploadFileService.uploadFile(file, uuid);
        var outputFilePath = generator.generateZenMetricsZip(srcFilePath, uuid);

        Map<String, String> resp = new HashMap<>();
        resp.put("code", "000");
        resp.put("uuid", uuid);
        resp.put("file", outputFilePath);
        return resp;
    }

    @GetMapping(value = "/download")
    public void download(String uuid, HttpServletResponse response) throws IOException {
        String outputFilePath = downloadService.findZipFileToDownloadByUuid(uuid);
        downloadService.download(outputFilePath, response);
    }
}



