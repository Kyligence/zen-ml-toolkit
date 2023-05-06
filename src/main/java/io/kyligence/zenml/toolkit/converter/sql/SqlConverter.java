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
import io.kyligence.zenml.toolkit.model.sql.SqlModel;
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
        var sqlModelMergeMgr = new SqlModelMergeManager();
        Map<SqlModel, Set<SqlMetricSpec>> mergedSqlMetrics = sqlModelMergeMgr.init(sqlMetricSpecs).merge().getMergedSqlMetrics();
        List<MetricSpec> results = new ArrayList<>();
        for (Map.Entry<SqlModel, Set<SqlMetricSpec>> entry : mergedSqlMetrics.entrySet()) {
            var modelName = entry.getKey().generateModelName();
            results.addAll(enrichSqlMetricsWithSameModel(modelName, entry.getValue().stream().toList()));
        }
        return results;
    }

    private List<MetricSpec> enrichSqlMetricsWithSameModel(String modelName, List<SqlMetricSpec> metricSpecs) {
        // key: measure expr
        Map<String, MetricSpec> mergedMetrics = new HashMap<>();

        // merge metrics with same expression
        for (SqlMetricSpec metric : metricSpecs) {
            var metricSpec = convert2MetricSpec(metric);
            var expr = metricSpec.getExpression();
            // update model name
            metricSpec.setDataModel(modelName);
            if (mergedMetrics.containsKey(expr)) {
                var toMerge = mergedMetrics.get(expr);
                // merge dimensions
                mergeDimensions(metricSpec, toMerge, modelName);

                // merge time dimensions
                mergeTimeDimensions(metricSpec, toMerge, modelName);

                // merge descriptions
                mergeDescription(metricSpec, toMerge);
            } else {
                mergedMetrics.put(expr, metricSpec);
            }
        }

        return mergedMetrics.values().stream().toList();
    }

    private static void mergeDimensions(MetricSpec metricSpec, MetricSpec toMerge, String modelName) {
        Set<String> mergedDims = new HashSet<>();
        List<String> dims = metricSpec.getDimensions();
        List<String> dims2Merge = toMerge.getDimensions();
        mergedDims.addAll(dims);
        mergedDims.addAll(dims2Merge);

        Set<String> results = new HashSet<>();
        for (String dim : mergedDims) {
            String newDim;
            if (dim.contains(".")) {
                newDim = modelName + "." + dim.split("\\.")[1];
            } else {
                newDim = modelName + "." + dim;
            }
            results.add(newDim);
        }
        toMerge.setDimensions(results.stream().sorted().toList());
    }

    private static void mergeTimeDimensions(MetricSpec metricSpec, MetricSpec toMerge, String modelName) {
        Set<TimeDimension> mergedTimeDims = new HashSet<>();
        List<TimeDimension> timeDims = metricSpec.getTimeDimensions();
        List<TimeDimension> timeDims2Merge = toMerge.getTimeDimensions();
        mergedTimeDims.addAll(timeDims);
        mergedTimeDims.addAll(timeDims2Merge);

        Set<TimeDimension> results = new HashSet<>();
        for (TimeDimension dim : mergedTimeDims) {
            var idtf = dim.getName();
            String newIdtf;
            if (idtf.contains(".")) {
                newIdtf = modelName + "." + dim.getName().split("\\.")[1];
            } else {
                newIdtf = modelName + "." + dim.getName();
            }
            dim.setName(newIdtf);
            results.add(dim);
        }

        toMerge.setTimeDimensions(results.stream().sorted().toList());
    }

    private static void mergeDescription(MetricSpec metricSpec, MetricSpec toMerge) {
        var desc = metricSpec.getDescription();
        var desc2Merge = toMerge.getDescription();
        var newDesc = desc2Merge + "\n" + desc;
        toMerge.setDescription(newDesc);
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
