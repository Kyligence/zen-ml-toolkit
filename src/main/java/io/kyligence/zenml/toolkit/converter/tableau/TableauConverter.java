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

package io.kyligence.zenml.toolkit.converter.tableau;

import io.kyligence.zenml.toolkit.converter.FileType;
import io.kyligence.zenml.toolkit.converter.MetricsConverter;
import io.kyligence.zenml.toolkit.converter.tableau.tds.TableauColumn;
import io.kyligence.zenml.toolkit.converter.tableau.tds.TdsSpec;
import io.kyligence.zenml.toolkit.exception.ErrorCode;
import io.kyligence.zenml.toolkit.exception.ToolkitException;
import io.kyligence.zenml.toolkit.model.tableau.tds.TableauDatasource;
import io.kyligence.zenml.toolkit.model.tableau.twb.TableauWorkbook;
import io.kyligence.zenml.toolkit.model.zenml.*;
import io.kyligence.zenml.toolkit.utils.tableau.TableauRWUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.*;

@Slf4j
public class TableauConverter implements MetricsConverter {

    // todo
    // 1. find right data model (relation --> table, dsname --> view, 单表 table，多表 view)
    // 2. expression to sql formatter

    private static final String TABLEAU_TAG = "Tableau";


    @Override
    public Metrics convert2Metrics(String filePath) {
        var fileSuffix = FilenameUtils.getExtension(filePath);
        if (StringUtils.equalsIgnoreCase(FileType.TDS_FILE, fileSuffix)) {
            var tds = TableauRWUtils.getTds(new File(filePath));
            return convertTds2Metrics(tds);
        } else if (StringUtils.equalsIgnoreCase(FileType.TWB_FILE, fileSuffix)) {
            var twb = TableauRWUtils.readTwb(filePath);
            return convertTwb2Metrics(twb);
        } else {
            throw new ToolkitException(ErrorCode.ILLEGAL_TABLEAU_FILE_TYPE);
        }
    }


    private Metrics convertTds2Metrics(TableauDatasource tds) {
        var metrics = new Metrics();
        var tdsAnalyzer = new TdsAnalyzer();
        var tdsSpec = tdsAnalyzer.analyzeTdsSpec(tds);
        Set<String> measureNameSet = new HashSet<>();
        if (tdsSpec != null) {
            List<MetricSpec> metricSpecs = getMetricSpecsFromTds(tdsSpec, measureNameSet);
            metrics.setMetrics(metricSpecs);
        }

        return metrics;
    }

    private Metrics convertTwb2Metrics(TableauWorkbook twb) {
        List<MetricSpec> allMetricSpecs = new ArrayList<>();

        var twbAnalyzer = new TwbAnalyzer();
        var twbSpec = twbAnalyzer.analyzeTwbSpec(twb);
        List<TdsSpec> tdsSpecs = twbSpec.getTdsSpecs();
        Set<String> measureNameSet = new HashSet<>();
        for (TdsSpec tdsSpec : tdsSpecs) {
            List<MetricSpec> metricSpecs = getMetricSpecsFromTds(tdsSpec, measureNameSet);
            allMetricSpecs.addAll(metricSpecs);
        }
        var metrics = new Metrics();
        metrics.setMetrics(allMetricSpecs);
        return metrics;
    }


    private List<MetricSpec> getMetricSpecsFromTds(TdsSpec tdsSpec, Set<String> measureNames) {
        List<MetricSpec> metricSpecs = new ArrayList<>();
        var tdsName = tdsSpec.getTableauDsName();
        List<String> tags = generateTags(tdsSpec);
        var tableauMeasures = tdsSpec.getMeasures();
        var tableauDimensions = tdsSpec.getDimensions();

        // dimensions
        Map<String, List<String>> table2Dims = parseDimensionsFromTdsSpec(tableauDimensions);
        List<String> dimensions = new ArrayList<>();
        for (List<String> dims : table2Dims.values()) {
            dimensions.addAll(dims);
        }

        // time dimensions
        Map<String, List<TimeDimension>> table2TimeDims =
                parseTimeDimensionsFromTdsSpec(tableauDimensions);
        List<TimeDimension> timeDimensions = new ArrayList<>();
        for (List<TimeDimension> timeDim : table2TimeDims.values()) {
            timeDimensions.addAll(timeDim);
        }

        for (TableauColumn measure : tableauMeasures) {
            var measureName = measure.getCaption();
            var tableauIdentifier = measure.getTableauIdentifier();
            if (StringUtils.isEmpty(measureName))
                measureName = tableauIdentifier;
            var calculation = measure.getCalculation();
            if (calculation == null)
                continue;
            var expr = measure.getCalculation().getFormula();

            var metricSpec = new MetricSpec();
            if (measureNames.contains(measureName)) {
                // measure name should be unique
                var new_name = measureName + Math.abs(measure.hashCode());
                metricSpec.setName(new_name);
                measureNames.add(new_name);
                var description = "Tableau Identifier: " + tableauIdentifier + " Origin Name: " + measureName;
                metricSpec.setDescription(description);
            } else {
                metricSpec.setName(measureName);
                measureNames.add(measureName);
                var description = "Tableau Identifier: " + tableauIdentifier;
                metricSpec.setDescription(description);
            }

            metricSpec.setDisplay(measureName);
            metricSpec.setExpression(expr);
            metricSpec.setStatus(MetricStatus.ONLINE);
            metricSpec.setType(MetricType.BASIC);
            metricSpec.setTags(tags);
            metricSpec.setDataModel(tdsName);
            metricSpec.setDimensions(dimensions);
            metricSpec.setTimeDimensions(timeDimensions);
            metricSpecs.add(metricSpec);
        }
        return metricSpecs;
    }

    private List<String> generateTags(TdsSpec spec) {
        var tableauDsName = spec.getTableauDsName();
        List<String> tags = new ArrayList<>();
        tags.add(tableauDsName);
        tags.add(TABLEAU_TAG);
        return tags;
    }

    private Map<String, List<String>> parseDimensionsFromTdsSpec(List<TableauColumn> dimensions) {
        Map<String, List<String>> table2Dims = new HashMap<>();
        for (TableauColumn dimension : dimensions) {
            if (!dimension.isCC()) {
                var tabName = dimension.getSourceColumn().getSourceTable().getTableWithSchema();
                var colName = dimension.getSourceColumn().getColName();
                var dimName = tabName + "." + colName;
                addDimensionName2TabDimsMap(table2Dims, tabName, dimName);
            } else {
                //todo: process cc
            }
        }
        return table2Dims;
    }

    private Map<String, List<TimeDimension>>
    parseTimeDimensionsFromTdsSpec(List<TableauColumn> dimensions) {
        Map<String, List<TimeDimension>> table2TimeDims = new HashMap<>();
        for (TableauColumn dimension : dimensions) {
            if (dimension.isTimeDimension()) {
                var tabName = dimension.getSourceColumn().getSourceTable().getTableWithSchema();
                var colName = dimension.getSourceColumn().getColName();
                var dimName = tabName + "." + colName;
                var dim = new TimeDimension();
                dim.setName(dimName);
                addTimeDimension2TabDimsMap(table2TimeDims, tabName, dim);
            }
        }
        return table2TimeDims;
    }

    private void addDimensionName2TabDimsMap(Map<String, List<String>> table2Dims, String table, String dim) {
        if (table2Dims.containsKey(table)) {
            List<String> dims = table2Dims.get(table);
            dims.add(dim);
        } else {
            List<String> dims = new ArrayList<>();
            dims.add(dim);
            table2Dims.put(table, dims);
        }
    }

    private void addTimeDimension2TabDimsMap(Map<String, List<TimeDimension>> table2Dims, String table,
                                             TimeDimension dim) {
        if (table2Dims.containsKey(table)) {
            List<TimeDimension> dims = table2Dims.get(table);
            dims.add(dim);
        } else {
            List<TimeDimension> dims = new ArrayList<>();
            dims.add(dim);
            table2Dims.put(table, dims);
        }
    }


    private String getDataModelName(String view, List<String> tables, String expr) {
        // todo, need to find the correct table name from column tag
        if (tables.size() == 1) {
            return tables.get(0).toLowerCase();
        }

        var tableHit = 0;
        var tableNameHit = "";
        for (String table : tables) {
            if (expr.toLowerCase().contains(table.toLowerCase())) {
                tableHit += 1;
                tableNameHit = table.toLowerCase();
            }
        }

        if (tableHit == 1) {
            // use table as data model, means measure expr just has one table
            return tableNameHit;
        } else {
            // measure cross tables, use view name instead
            return view.toLowerCase();
        }
    }
}
