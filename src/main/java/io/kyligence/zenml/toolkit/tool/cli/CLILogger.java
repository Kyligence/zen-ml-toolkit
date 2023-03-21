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

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.PrintStream;

@Slf4j
public class CLILogger {
    // to color output in console
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";
    public static final String ANSI_YELLOW = "\u001B[33m";
    public static final String ANSI_BLUE = "\u001B[34m";
    public static final String ANSI_PURPLE = "\u001B[35m";
    public static final String ANSI_CYAN = "\u001B[36m";
    public static final String ANSI_WHITE = "\u001B[37m";

    private Logger logger;
    private PrintStream out;
    private boolean printConsole;

    public CLILogger(Logger logger) {
        this(logger, System.out, true);
    }

    public CLILogger(Logger logger, boolean printConsole) {
        this(logger, System.out, printConsole);
    }

    public CLILogger(Logger logger, PrintStream out, boolean printConsole) {
        this.logger = logger;
        this.out = out;
        this.printConsole = printConsole;
    }

    public void disablePrintConsole() {
        this.printConsole = false;
    }

    public void enablePrintConsole() {
        this.printConsole = true;
    }

    //==========================================================
    //  print methods
    //==========================================================


    public void printProgress(int percentage) throws IOException {
        if (printConsole) {
            String anim = "|/-\\";
            String data = "\rProgress ongoing: "
                    + anim.charAt((int) (System.currentTimeMillis() / 1000) % anim.length()) + " " + percentage + "%";
            out.write(data.getBytes());
        }
    }

    public void info(String msg) {
        logger.info(msg);
        consoleInfo(msg);
    }

    private void consoleInfo(String msg) {
        if (printConsole) {
            out.println(msg);
        }
    }

    public void info(String msg, Integer replace) {
        logger.info(msg, replace);
        consoleInfo(msg, replace.toString());
    }

    private void consoleInfo(String msg, String replace) {
        if (printConsole) {
            out.println(StringUtils.replace(msg, "{}", replace));
        }
    }

    public void info(String msg, String replace1, Integer replace2) {
        logger.info(msg, replace1, replace2);
        consoleInfo(msg, replace1, replace2.toString());
    }

    private void consoleInfo(String msg, String replace1, String replace2) {
        if (printConsole) {
            msg = StringUtils.replaceOnce(msg, "{}", replace1);
            out.println(StringUtils.replaceOnce(msg, "{}", replace2));
        }
    }

    public void info(String msg, String replace) {
        logger.info(msg, replace);
        consoleInfo(msg, replace);
    }

    public void info(String msg, String replace1, String replace2) {
        logger.info(msg, replace1, replace2);
        consoleInfo(msg, replace1, replace2);
    }

    public void warn(String msg) {
        logger.warn(msg);
        if (printConsole) {
            out.println(ANSI_YELLOW + "WARNING: " + msg + ANSI_RESET);
        }
    }

    public void warn(String msg, String replace) {
        logger.warn(msg, replace);
        if (printConsole) {
            out.println(ANSI_YELLOW + "WARNING: " + StringUtils.replace(msg, "{}", replace) + ANSI_RESET);
        }
    }

    public void warn(String msg, Exception e) {
        warn(msg, ExceptionUtils.getStackTrace(e));
    }

    public void warn(String msg, String replace1, String replace2) {
        logger.warn(msg, replace1, replace2);
        if (printConsole) {
            msg = StringUtils.replaceOnce(msg, "{}", replace1);
            out.println(ANSI_YELLOW + "WARNING: " + StringUtils.replaceOnce(msg, "{}", replace2) + ANSI_RESET);
        }
    }

    public void warn(String msg, String replace1, Long replace2) {
        warn(msg, replace1, replace2.toString());
    }

    public void warn(String msg, String replace, Exception e) {
        warn(msg, replace, ExceptionUtils.getStackTrace(e));
    }

    public void quit(String msg) {
        consoleInfo(ANSI_RED + "ERROR: " + msg + ANSI_RESET);
        throw new RuntimeException(msg);
    }

    public void quit(String msg, String replace1, Exception e) {
        msg = StringUtils.replaceOnce(msg, "{}", replace1);
        quit(msg, e);
    }

    public void quit(String msg, String replace1) {
        msg = StringUtils.replaceOnce(msg, "{}", replace1);
        quit(msg);
    }

    public void quit(String msg, Exception e) {
        msg = StringUtils.replace(msg, "{}", ExceptionUtils.getStackTrace(e));
        consoleInfo(ANSI_RED + "ERROR: " + msg + ANSI_RESET);
        throw new RuntimeException(msg, e);
    }

    public void successInfo(String msg) {
        consoleInfo(ANSI_GREEN + msg + ANSI_RESET);
        logger.info(msg);
    }

    public void successInfo(String msg, String replace) {
        msg = StringUtils.replaceOnce(msg, "{}", replace);
        successInfo(msg);
    }

    public void emphasisInfo(String msg) {
        consoleInfo(ANSI_BLUE + msg + ANSI_RESET);
        logger.info(msg);
    }
}
