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

import io.kyligence.zenml.toolkit.config.ToolkitConfig;
import io.kyligence.zenml.toolkit.converter.FileType;
import io.kyligence.zenml.toolkit.exception.ErrorCode;
import io.kyligence.zenml.toolkit.exception.ToolkitException;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.springframework.stereotype.Service;

import java.io.*;

@Slf4j
@Service
public class DownloadService {
    public String findZipFileToDownloadByUuid(String uuid) {
        log.info("Downloading zip file by uuid: {}", uuid);
        var config = ToolkitConfig.getInstance();
        var tmpDir = new File(config.getLocalTmpFolder());

        var uuidDir = new File(tmpDir, uuid);
        if (!uuidDir.exists()) {
            log.error(ErrorCode.FOLDER_NOT_EXISTS.getReportMessage() + " uuid: {}", uuid);
            throw new ToolkitException(ErrorCode.FOLDER_NOT_EXISTS);
        }

        File[] dirs = uuidDir.listFiles(File::isDirectory);
        if (dirs == null || dirs.length != 1) {
            throw new ToolkitException("There should be only 1 directory inside {}", uuidDir.getAbsolutePath());
        }

        var dateDir = dirs[0];
        File[] files = dateDir.listFiles(pathname -> FilenameUtils.isExtension(pathname.getName(), FileType.ZIP_FILE));
        if (files == null || files.length != 1) {
            throw new ToolkitException("There should be only 1 zip file inside {}", dateDir.getAbsolutePath());
        }
        var filePath = files[0].getAbsolutePath();
        log.info("Zip file found: {}", filePath);
        return filePath;
    }


    public void download(String downloadFile, final HttpServletResponse response) throws IOException {
        log.info("Prepare downloading from file {}", downloadFile);
        var file = new File(downloadFile);
        try (InputStream fileInputStream = new FileInputStream(file);
             var bufferedInputStream = new BufferedInputStream(fileInputStream);
             var output = response.getOutputStream();
             var bufferedOutputStream = new BufferedOutputStream(output)) {

            response.reset();
            response.setContentType("application/x-download");
            response.setContentLength((int) (file.length()));
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");

            int bytesRead;
            var buffer = new byte[1024];

            while ((bytesRead = bufferedInputStream.read(buffer)) != -1) {
                bufferedOutputStream.write(buffer, 0, bytesRead);
            }
            bufferedOutputStream.flush();
        } catch (IOException e) {
            throw new ToolkitException(ErrorCode.DOWNLOAD_FILE_ERROR, e);
        }
    }
}
