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

package io.kyligence.zenml.toolkit.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Getter
public enum ErrorCode {
    // Zen-0xxxx: System level exception
    UNKNOWN("ZEN-00000", "Unknown error"),
    LOAD_CONFIG_ERROR("ZEN-00001", "Failed to load the config file"),
    ENV_NOT_FOUND("ZEN_00002", "Environment variable not found"),

    // Zen-1xxxx: input arguments exception
    ILLEGAL_ARGUMENTS("ZEN-10001", "Illegal arguments"),

    // Zen-2xxxx: file exception
    EMPTY_FILE("ZEN-20001", "File content is empty, please check the file"),
    FILE_NOT_EXISTS("ZEN-20002", "File not exists"),
    FOLDER_NOT_EXISTS("ZEN-20003", "Directory not exists"),

    SAVE_FILE_ERROR("ZEN-20004", "Failed to save file"),
    UPLOAD_FILE_TOO_LARGE("ZEN-20005", "Upload file too large to exceed the file size limit"),
    DOWNLOAD_FILE_ERROR("ZEN-20006", "Download file failed"),


    //Zen-3xxxx: security exception
    PWD_DECODE_ERROR("ZEN-30001", "Password decode hex error"),
    PWD_DECRYPTION_ERROR("ZEN-30002", "Password decryption error"),

    // Zen-4xxxx: metric converter error
    TABLEAU_FILE_PARSE_ERROR("ZEN-40001", "Failed to parse tableau file"),
    ILLEGAL_TABLEAU_FILE_TYPE("ZEN-40002", "Only support twb file or tds file for Tableau"),
    FAILED_GENERATE_ZENML("ZEN-40003", "Failed to write metrics to file"),

    ILLEGAL_SQL_FILE_TYPE("ZEN-40004", "Only support .sql as suffix for sql file"),
    FAILED_READ_SQL_FILE("ZEN-40005", "Failed to read sql file as string"),
    SQL_SELECT_STATEMENT_SUPPORT_ONLY("ZEN-40006", "Only support extract metrics from sql select statement"),
    MEASURE_NOT_FOUND_IN_SQL("ZEN-40007", "Measure not found in sql statement"),

    // Zen-9xxxx: cli error
    CLI_ILLEGAL_ARGUMENTS("ZEN-90001", "Illegal arguments, please check the help usage"),

    CLI_SOURCE_FILE_PATH_EMPTY("ZEN-90002", "Source file path is empty, please check the help usage"),

    CLI_OUTPUT_DIR_PATH_EMPTY("ZEN-90003", "Output directory path is empty, please check the help usage"),

    ;

    private final String code;
    private final String msg;

    private static final Map<String, ErrorCode> map = new HashMap<>();

    static {
        for (ErrorCode code : ErrorCode.values()) {
            map.put(code.name(), code);
        }
    }

    public String getReportMessage() {
        return this.code + " : " + this.msg;
    }
}
