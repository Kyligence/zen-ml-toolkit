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

import io.kyligence.zenml.toolkit.config.ToolkitConfig;
import lombok.experimental.UtilityClass;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import java.io.File;
import java.util.Date;

@UtilityClass
public class WorkDirUtils {

    public static File getTmpFolder(String uuid) {
        var config = ToolkitConfig.getInstance();
        var tmpDir = new File(config.getLocalTmpFolder());
        if (!tmpDir.exists()) {
            tmpDir.mkdirs();
        }

        var uuidDir = new File(tmpDir, uuid);
        if (!uuidDir.exists()) {
            uuidDir.mkdirs();
        }

        return uuidDir;
    }


    public static File getTmpFolderOfToday(String uuid) {
        var tmpDir = getTmpFolder(uuid);
        var today = getTodayStr();
        var todayDir = new File(tmpDir, today);
        if (!todayDir.exists()) {
            todayDir.mkdirs();
        }
        return todayDir;
    }

    private static String getTodayStr() {
        return getDateStr("yyyy-MM-dd");
    }

    private static String getDateStr(String pattern) {
        return DateFormatUtils.format(new Date(), pattern);
    }


    public static String getOutputFolder2Compress(String filePath, String uuid) {
        var fileName = FilenameUtils.getBaseName(filePath);
        var tmpDir = getTmpFolderOfToday(uuid);
        var dateStr = getDateStr("yyyyMMddHHmmss");
        var destDirName = fileName + "_metrics_" + dateStr + "/";
        var destDir = new File(tmpDir, destDirName);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
        return destDir.getAbsolutePath();
    }
}
