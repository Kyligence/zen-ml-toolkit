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

import io.kyligence.zenml.toolkit.model.sql.SqlMetricSpec;
import io.kyligence.zenml.toolkit.model.sql.SqlModel;

import java.util.*;


// to merge the metrics by same data model to reduce the data model creation
public class SqlModelMergeManager {
    private Map<SqlModel, Set<SqlMetricSpec>> mergedMetrics = new HashMap<>();

    private Map<SqlModel, List<SqlMetricSpec>> model2SqlMetricsMap = new HashMap<>();

    private List<SqlModel> sortedSqlModel;


    // init --> merge -- getMergedSqlMetrics
    public SqlModelMergeManager init(List<SqlMetricSpec> sqlMetricSpecs) {


        for (SqlMetricSpec sqlMetricSpec : sqlMetricSpecs) {

            var sqlModel = sqlMetricSpec.getSqlModel();
            if (model2SqlMetricsMap.containsKey(sqlModel)) {
                model2SqlMetricsMap.get(sqlModel).add(sqlMetricSpec);
            } else {
                List<SqlMetricSpec> metrics = new ArrayList<>();
                metrics.add(sqlMetricSpec);
                model2SqlMetricsMap.put(sqlModel, metrics);
            }
        }

        this.sortedSqlModel = sortSqlModels(model2SqlMetricsMap.keySet().stream().toList());
        return this;
    }

    private List<SqlModel> sortSqlModels(List<SqlModel> models) {
        return models.stream().sorted(Comparator.comparingInt(SqlModel::getJoinDepth).reversed()).toList();
    }

    public SqlModelMergeManager merge() {

        Set<SqlModel> merged = new HashSet<>();
        // from the biggest model to the smallest model
        for (SqlModel model : this.sortedSqlModel) {
            if (merged.contains(model)) {
                continue;
            }

            merged.add(model);
            if (this.mergedMetrics.containsKey(model)) {
                this.mergedMetrics.get(model).addAll(model2SqlMetricsMap.get(model));
            } else {
                Set<SqlMetricSpec> metricSpecs = new HashSet<>(model2SqlMetricsMap.get(model));
                this.mergedMetrics.put(model, metricSpecs);
            }

            for (SqlModel toCheck : this.sortedSqlModel) {
                if (model.canMerge(toCheck)) {
                    merged.add(toCheck);
                    this.mergedMetrics.get(model).addAll(model2SqlMetricsMap.get(toCheck));
                }
            }
        }

        return this;
    }

    public Map<SqlModel, Set<SqlMetricSpec>> getMergedSqlMetrics() {
        return this.mergedMetrics;
    }

}
