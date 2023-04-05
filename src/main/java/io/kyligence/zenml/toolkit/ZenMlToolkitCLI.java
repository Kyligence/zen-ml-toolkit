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

package io.kyligence.zenml.toolkit;

import io.kyligence.zenml.toolkit.exception.ErrorCode;
import io.kyligence.zenml.toolkit.exception.ToolkitException;
import io.kyligence.zenml.toolkit.service.ZenGenerator;
import io.kyligence.zenml.toolkit.utils.cli.AbstractApplication;
import io.kyligence.zenml.toolkit.utils.cli.OptionsHelper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

@Slf4j
public class ZenMlToolkitCLI extends AbstractApplication {

    private String srcFilePath;
    private String destDirPath;

    private static final Option OPTION_SRC_FILE_PATH;
    private static final Option OPTION_DEST_FILE_PATH;
    private static final Option OPTION_HELP;

    static {
        OPTION_SRC_FILE_PATH = new Option("i", "in", true,
                "specify the location of source file");
        OPTION_DEST_FILE_PATH = new Option("o", "output", true,
                "specify the output directory for metrics file generated");
        OPTION_HELP = new Option("h", "help", false, "print help message.");
    }

    @Override
    protected Options getOptions() {
        final var options = new Options();
        options.addOption(OPTION_SRC_FILE_PATH);
        options.addOption(OPTION_DEST_FILE_PATH);
        options.addOption(OPTION_HELP);
        return options;
    }

    private void printUsage(OptionsHelper optionsHelper) {
        optionsHelper.printUsage(this.getClass().getName(), getOptions());
    }

    private void initOptionValues(OptionsHelper optionsHelper) {
        if (optionsHelper.getOptions().length != 2) {
            printUsage(optionsHelper);
            log.error(ErrorCode.CLI_ILLEGAL_ARGUMENTS.getReportMessage());
            throw new ToolkitException(ErrorCode.CLI_ILLEGAL_ARGUMENTS);
        }

        this.srcFilePath = parseStringArgFromOption(optionsHelper, OPTION_SRC_FILE_PATH, "");
        this.destDirPath = parseStringArgFromOption(optionsHelper, OPTION_DEST_FILE_PATH, "");

        if (StringUtils.isBlank(srcFilePath)) {
            printUsage(optionsHelper);
            log.error(ErrorCode.CLI_SOURCE_FILE_PATH_EMPTY.getReportMessage());
            throw new ToolkitException(ErrorCode.CLI_SOURCE_FILE_PATH_EMPTY);
        }
        if (StringUtils.isBlank(destDirPath)) {
            printUsage(optionsHelper);
            log.error(ErrorCode.CLI_OUTPUT_DIR_PATH_EMPTY.getReportMessage());
            throw new ToolkitException(ErrorCode.CLI_OUTPUT_DIR_PATH_EMPTY);
        }

    }

    @Override
    protected void execute(OptionsHelper optionsHelper) throws Exception {
        if (optionsHelper.hasOption(OPTION_HELP)) {
            printUsage(optionsHelper);
            return;
        }
        initOptionValues(optionsHelper);

        try {
            var generator = new ZenGenerator();
            generator.convertMetrics2ZenMlFile(srcFilePath, destDirPath);
        } catch (IOException e) {
            log.error(ErrorCode.FAILED_GENERATE_ZENML.getReportMessage(), e);
            throw new ToolkitException(ErrorCode.FAILED_GENERATE_ZENML, e);
        }
    }

    public static void main(String[] args) {
        var cli = new ZenMlToolkitCLI();
        cli.execute(args);
    }
}
