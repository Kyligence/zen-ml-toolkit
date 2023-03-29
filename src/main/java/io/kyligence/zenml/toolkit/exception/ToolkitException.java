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

import org.slf4j.helpers.MessageFormatter;

public class ToolkitException extends RuntimeException {

    public ToolkitException(String msg) {
        super(msg);
    }

    public ToolkitException(Throwable cause) {
        super(cause);
    }

    public ToolkitException(String msg, Throwable cause) {
        super(msg, cause);
    }

    public ToolkitException(String format, Object... args) {
        super(formatMsg(format, args), ex(args));
    }

    public ToolkitException(ErrorCode errorCode) {
        super(errorCode.getReportMessage());
    }

    public ToolkitException(ErrorCode errorCode, Throwable throwable) {
        super(errorCode.getReportMessage(), throwable);
    }

    public ToolkitException(ErrorCode errorCode, Object... extensionMessages) {
        this(errorCode.getReportMessage(), extensionMessages);
    }

    private static String formatMsg(String format, Object... args) {
        var ft = MessageFormatter.arrayFormat(format, args);
        return ft.getMessage();
    }

    private static Throwable ex(Object... args) {
        return MessageFormatter.getThrowableCandidate(args);
    }
}
