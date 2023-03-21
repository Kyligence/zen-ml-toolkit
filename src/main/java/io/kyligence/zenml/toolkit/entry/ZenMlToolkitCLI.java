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

import io.kyligence.zenml.toolkit.tool.OptionsHelper;
import io.kyligence.zenml.toolkit.tool.cli.AbstractApplication;
import io.kyligence.zenml.toolkit.tool.cli.CLILogger;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

@Slf4j
public class ZenMlToolkitCLI extends AbstractApplication {
    private final CLILogger cliLog = new CLILogger(log);

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
        final Options options = new Options();
        options.addOption(OPTION_SRC_FILE_PATH);
        options.addOption(OPTION_DEST_FILE_PATH);
        options.addOption(OPTION_HELP);
        return options;
    }

    private boolean printUsage(OptionsHelper optionsHelper) {
        boolean help = optionsHelper.hasOption(OPTION_HELP);
        if (help) {
            optionsHelper.printUsage(this.getClass().getName(), getOptions());
        }
        return help;
    }

    private void initOptionValues(OptionsHelper optionsHelper) {
        this.srcFilePath = parseStringArgFromOption(optionsHelper, OPTION_SRC_FILE_PATH, "");
        this.destDirPath = parseStringArgFromOption(optionsHelper, OPTION_DEST_FILE_PATH, "");
        if (StringUtils.isBlank(srcFilePath)) {
            printUsage(optionsHelper);
            cliLog.quit("Source file path is empty, please check the help usage");
        }
        if (StringUtils.isBlank(destDirPath)) {
            printUsage(optionsHelper);
            cliLog.quit("Output directory path is empty, please check the help usage");
        }

    }

    @Override
    protected void execute(OptionsHelper optionsHelper) throws Exception {
        if (printUsage(optionsHelper)) {
            return;
        }
        initOptionValues(optionsHelper);

        try {
            ZenGenerator generator = new ZenGenerator();
            generator.generateZenMetrics(srcFilePath, destDirPath);
        } catch (IOException e) {
            cliLog.quit("Failed to write metrics to file. please check the details: ", e);
        }
    }

    public static void main(String[] args) {
        ZenMlToolkitCLI cli = new ZenMlToolkitCLI();
        cli.execute(args);
    }
}
