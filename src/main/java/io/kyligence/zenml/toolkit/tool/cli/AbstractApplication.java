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

package io.kyligence.zenml.toolkit.tool.cli;

import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;

public abstract class AbstractApplication {
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_BLUE = "\u001B[34m";
    private static final String ANSI_RESET = "\u001B[0m";

    protected abstract Options getOptions();

    protected abstract void execute(OptionsHelper optionsHelper) throws Exception;

    public final void execute(String[] args) {
        OptionsHelper optionsHelper = new OptionsHelper();
        System.out.println(
                ANSI_BLUE + "Running " + this.getClass().getName() + " " + StringUtils.join(args, " ") + ANSI_RESET);
        try {
            optionsHelper.parseOptions(getOptions(), args);
            execute(optionsHelper);
        } catch (ParseException e) {
            System.out
                    .println(ANSI_RED + " Found error when parsing options, please check usage and retry" + ANSI_RESET);
            optionsHelper.printUsage(this.getClass().getName(), getOptions());
            throw new RuntimeException("error parsing options", e);
        } catch (Exception e) {
            throw new RuntimeException("error execute " + this.getClass().getName(), e);
        }
    }

    protected String parseStringArgFromOption(OptionsHelper optionsHelper, Option option, String defaultval) {
        boolean hasOpt = optionsHelper.hasOption(option);
        String value = defaultval;
        if (hasOpt) {
            value = optionsHelper.getOptionValue(option);
        }
        return value;
    }

    /**
     * Parse integer argument in CLI options
     *
     * @param optionsHelper
     * @param option
     * @param defaultVal:   default value for the option
     * @param minVal:       minimal int value for the option
     * @return int argument
     */
    protected int parseIntArgFromOption(OptionsHelper optionsHelper, Option option, int defaultVal, int minVal) {
        boolean hasOpt = optionsHelper.hasOption(option);
        int value = defaultVal;
        if (hasOpt) {
            try {
                value = Integer.valueOf(optionsHelper.getOptionValue(option));
            } catch (NumberFormatException e) {
                printInvaildArgument(optionsHelper, option);
                throw new RuntimeException(option.getLongOpt() + " argument is not valid", e);
            }
        }
        if (value < minVal) {
            printInvaildArgument(optionsHelper, option);
            throw new RuntimeException(option.getLongOpt() + " argument is not valid");
        }
        return value;
    }

    private void printInvaildArgument(OptionsHelper optionsHelper, Option option) {
        optionsHelper.printUsage(this.getClass().getName(), getOptions());
        System.out.println(
                ANSI_RED + option.getLongOpt() + " argument is not valid, please check usage and retry" + ANSI_RESET);
    }
}
