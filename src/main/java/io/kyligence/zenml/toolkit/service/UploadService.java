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
import io.kyligence.zenml.toolkit.exception.ErrorCode;
import io.kyligence.zenml.toolkit.exception.ToolkitException;
import io.kyligence.zenml.toolkit.utils.WorkDirUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Slf4j
@Service
public class UploadService {

    public String uploadFile(MultipartFile file, String uuid) {
        if (file.isEmpty()) {
            log.error("File to upload is empty");
            throw new ToolkitException(ErrorCode.EMPTY_FILE);
        }

        var todayDir = WorkDirUtils.getTmpFolderOfToday(uuid);
        var fileName = file.getOriginalFilename();

        checkFileLimit(fileName, file.getSize());

        if (StringUtils.isBlank(fileName)) {
            log.error("Upload file name is empty");
            throw new ToolkitException(ErrorCode.EMPTY_FILE);
        }

        var dest = new File(todayDir, fileName);
        try {
            file.transferTo(dest);
            log.info("Uploaded file {} to directory {}", file, todayDir.getAbsolutePath());
        } catch (IOException e) {
            log.error("Failed to save file: {} to dest: {}", fileName, dest.getAbsolutePath());
            throw new ToolkitException(ErrorCode.SAVE_FILE_ERROR, e);
        }
        return dest.getAbsolutePath();
    }


    private void checkFileLimit(String fileName, long fileSize) {
        var config = ToolkitConfig.getInstance();
        var sizeKB = (double) fileSize / 1024;
        if (config.getFileSizeLimit() > 0 && sizeKB > config.getFileSizeLimit()) {
            log.error("The size of {} exceeds the upper limit: {} MB", fileName,
                    config.getFileSizeLimit().intValue() / 1024);
            throw new ToolkitException(ErrorCode.UPLOAD_FILE_TOO_LARGE);
        }
    }


    public void saveFile2S3() {
        // todo: implements upload file to s3 bucket
    }


}
