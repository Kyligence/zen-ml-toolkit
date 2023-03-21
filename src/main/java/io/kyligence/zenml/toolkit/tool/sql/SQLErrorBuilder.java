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

package io.kyligence.zenml.toolkit.tool.sql;

import org.apache.commons.lang3.StringUtils;

public class SQLErrorBuilder {

    public static final String ILLEGAL_METRIC_SQL_PARSE_CODE = "ILLEGAL METRICS SQL EXPRESSION";

    public static class MetricSQLException extends RuntimeException {

        public MetricSQLException(String message) {
            super(message);
        }

        public MetricSQLException(Throwable cause) {
            super(cause);
        }

        public MetricSQLException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    private final String errorMessage;

    private SQLErrorBuilder(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public MetricSQLException of(String message, Throwable cause) {
        return new MetricSQLException(combineErrMsg(message), cause);
    }

    public MetricSQLException of(String message) {
        return new MetricSQLException(combineErrMsg(message));
    }

    public MetricSQLException of(Throwable cause) {
        if (StringUtils.isNotBlank(errorMessage)) {
            return new MetricSQLException(errorMessage, cause);
        } else {
            return new MetricSQLException(cause);
        }
    }

    private String combineErrMsg(String msg) {
        if (StringUtils.isBlank(msg))
            return this.errorMessage;

        if (StringUtils.isBlank(this.errorMessage)) {
            return msg;
        } else {
            return this.errorMessage + " " + msg;
        }
    }

    public static SQLErrorBuilder parseError() {
        return new SQLErrorBuilder(ILLEGAL_METRIC_SQL_PARSE_CODE);
    }

}
