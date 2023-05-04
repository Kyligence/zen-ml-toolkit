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

package io.kyligence.zenml.toolkit.converter.sql;

import io.kyligence.zenml.toolkit.converter.FileType;
import io.kyligence.zenml.toolkit.converter.MetricsConverter;
import io.kyligence.zenml.toolkit.exception.ErrorCode;
import io.kyligence.zenml.toolkit.exception.ToolkitException;
import io.kyligence.zenml.toolkit.model.sql.SqlMetricSpec;
import io.kyligence.zenml.toolkit.model.zenml.*;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

public class SqlConverter implements MetricsConverter {
    private static final String SQL_TAG = "SQL";

    @Override
    public Metrics convert2Metrics(String filePath) {
        var file = new File(filePath);
        if (!file.exists()) {
            throw new ToolkitException(ErrorCode.FILE_NOT_EXISTS);
        }

        var fileSuffix = FilenameUtils.getExtension(filePath);
        if (StringUtils.equalsIgnoreCase(FileType.SQL_FILE, fileSuffix)) {
            return convertSql2Metrics(filePath);
        } else {
            throw new ToolkitException(ErrorCode.ILLEGAL_SQL_FILE_TYPE);
        }
    }

    @Override
    public List<String> createTags(String dataSource) {
        List<String> tags = new ArrayList<>();
        tags.add(dataSource);
        tags.add(SQL_TAG);
        return tags;
    }

    private Metrics convertSql2Metrics(String srcPath) {
        var metrics = new Metrics();
        var sqlAnalyzer = new SqlFileAnalyzer();
        List<SqlMetricSpec> sqlMetricSpecs = sqlAnalyzer.analyzeSqlMetricsSpec(srcPath);
        List<MetricSpec> metricSpecs = getMetricsSpecsFromSqlFile(sqlMetricSpecs);
        metrics.setMetrics(metricSpecs);
        return metrics;
    }

    private List<MetricSpec> getMetricsSpecsFromSqlFile(List<SqlMetricSpec> sqlMetricSpecs) {
        // some sqls will extract the same measure, need merge them
        // key:   [datasource:metricExpr]
        Map<String, MetricSpec> mergedMetrics = new HashMap<>();

        for (SqlMetricSpec sqlMetric : sqlMetricSpecs) {
            var metricSpec = convert2MetricSpec(sqlMetric);
            var key = metricSpec.getDataModel() + ":" + metricSpec.getExpression();
            if (mergedMetrics.containsKey(key)) {
                // the same key means the metric can be merged to 1 metric
                var metric2Merge = mergedMetrics.get(key);
                // merge dimensions
                List<String> dims = metricSpec.getDimensions();
                List<String> dims2Merge = metric2Merge.getDimensions();
                dims2Merge.addAll(dims);
                metric2Merge.setDimensions(new ArrayList<>(new HashSet<>(dims2Merge)));

                // merge time dimensions
                List<TimeDimension> timeDims = metricSpec.getTimeDimensions();
                List<TimeDimension> timeDims2Merge = metric2Merge.getTimeDimensions();
                timeDims2Merge.addAll(timeDims);
                metric2Merge.setTimeDimensions(new ArrayList<>(new HashSet<>(timeDims2Merge)));

                // merge descriptions
                var desc = metricSpec.getDescription();
                var desc2Merge = metric2Merge.getDescription();
                var newDesc = desc2Merge + "\n" + desc;
                metric2Merge.setDescription(newDesc);

            } else {
                mergedMetrics.put(key, metricSpec);
            }
        }

        return mergedMetrics.values().stream().toList();
    }

    private MetricSpec convert2MetricSpec(SqlMetricSpec sqlMetric) {
        var datasource = sqlMetric.getDatasource();
        var measureAlias = sqlMetric.getMeasureAlias();
        var measure = sqlMetric.getMeasure();
        var originSql = sqlMetric.getOriginalSql();
        List<String> dimensions = sqlMetric.getDimensions();
        List<TimeDimension> timeDimensions = sqlMetric.getTimeDimensions();

        var metricSpec = new MetricSpec();
        metricSpec.setName(measureAlias);
        metricSpec.setDisplay(measureAlias);
        metricSpec.setDimensions(dimensions);
        metricSpec.setTimeDimensions(timeDimensions);
        metricSpec.setExpression(measure);
        metricSpec.setDataModel(datasource);
        metricSpec.setStatus(MetricStatus.ONLINE);
        metricSpec.setType(MetricType.BASIC);

        String description = "Metrics from sql: \n" + originSql;
        metricSpec.setDescription(description);

        var tags = createTags(datasource);
        metricSpec.setTags(tags);
        return metricSpec;
    }

}
