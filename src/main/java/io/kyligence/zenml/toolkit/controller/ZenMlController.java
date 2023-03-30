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

import io.kyligence.zenml.toolkit.service.UploadFileService;
import io.kyligence.zenml.toolkit.service.ZenGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

@Slf4j
@RestController
public class ZenMlController {

    @Autowired
    private UploadFileService uploadFileService;

    @Autowired
    private ZenGenerator generator;

    @GetMapping("/hello")
    public String hello() {
        return "hello world";
    }

    @PostMapping(value = "/convert_metrics")
    public ResponseEntity uploadAndConvertMetrics(@RequestParam("files") MultipartFile file) throws IOException {

        var srcFilePath = uploadFileService.uploadFile(file);
        var outputFilePath = generator.generateZenMetricsZip(srcFilePath);

        log.info("Prepare downloading from file {}", outputFilePath);
        var outputFile = new File(outputFilePath);
        var resource = new ByteArrayResource(Files.readAllBytes(Paths.get(outputFilePath)));
        var headers = new HttpHeaders();

        headers.setContentDisposition(ContentDisposition.attachment().build());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(outputFile.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }
}



