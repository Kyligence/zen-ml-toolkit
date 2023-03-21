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

import io.kyligence.zenml.toolkit.converter.MetricsConverter;
import io.kyligence.zenml.toolkit.metrics.*;
import io.kyligence.zenml.toolkit.source.tableau.TableauCalculatedFields;
import io.kyligence.zenml.toolkit.source.tableau.TableauColumn;
import io.kyligence.zenml.toolkit.source.tableau.TableauParser;
import org.apache.commons.io.FilenameUtils;
import org.dom4j.DocumentException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TableauConverter implements MetricsConverter {
    @Override
    public Metrics convert2Metrics(String filePath) {
        String fileSuffix = FilenameUtils.getExtension(filePath);
        TableauParser parser = new TableauParser();
        try {
            List<TableauCalculatedFields> cfs = parser.parseTableauFile(filePath, fileSuffix);
            return convertTableauCalculatedField2Metrics(cfs);
        } catch (DocumentException e) {
            throw new RuntimeException(e);
        }
    }

    private Metrics convertTableauCalculatedField2Metrics(List<TableauCalculatedFields> cfs) {
        List<MetricSpec> metricSpecs = new ArrayList<>();
        for(TableauCalculatedFields cf : cfs) {
            List<TableauColumn> tableauColumns = cf.getColumns();
            List<String> tags = cf.getTags();

            // a tds should just have one connection tag, one connection should have one view
            String view = cf.getViews().get(0);
            List<String> tables = cf.getTables();

            if (!tableauColumns.isEmpty()) {
                metricSpecs.addAll(convertTableauColumnsToMetrics(tableauColumns, tags, view, tables));
            }
        }
        Metrics metrics = new Metrics();
        metrics.setMetrics(metricSpecs);
        return metrics;
    }

    private List<MetricSpec> convertTableauColumnsToMetrics(List<TableauColumn> tableauColumns, List<String> tags,
                                                            String view, List<String> tables) {
        List<MetricSpec> metricSpecs = new ArrayList<>();
        Map<String, List<String>> table2Dims = parseDimensionsFromTableauColumns(tableauColumns);
        Map<String, List<TimeDimension>> table2TimeDims =
                parseTimeDimensionsFromTableauColumns(tableauColumns);

        for (TableauColumn column : tableauColumns) {
            if (column.isMeasure()) {
                MetricSpec metricSpec = new MetricSpec();
                String displayName = column.getMeasureDisplayName();
                String measureName = column.getMeasureName();
                if (displayName == null || displayName.isEmpty()) {
                    displayName = measureName;
                }
                String expr = column.getMeasureExpression();
                String dataModel = getDataModelName(view, tables, expr);

                List<String> dimensions = new ArrayList<>();
                List<TimeDimension> timeDimensions = new ArrayList<>();
                if (dataModel.equalsIgnoreCase(view)) {
                    // use view as data model, all dims should be applied to metrics
                    // dimensions
                    for (List<String> dims : table2Dims.values()) {
                        dimensions.addAll(dims);
                    }
                    // time dimension
                    for (List<TimeDimension> timeDims : table2TimeDims.values()) {
                        timeDimensions.addAll(timeDims);
                    }
                } else {
                    // use table as data model, only apply the dimension belongs to this table
                    if (table2Dims.get(dataModel) != null) {
                        dimensions.addAll(table2Dims.get(dataModel));
                    }
                    if (table2TimeDims.get(dataModel) != null) {
                        timeDimensions.addAll(table2TimeDims.get(dataModel));
                    }
                }

                metricSpec.setName(measureName);
                metricSpec.setDisplay(displayName);
                metricSpec.setExpression(expr);
                metricSpec.setStatus(MetricStatus.ONLINE);
                metricSpec.setType(MetricType.BASIC);
                metricSpec.setTags(tags);
                metricSpec.setDataModel(dataModel);
                metricSpec.setDimensions(dimensions);
                metricSpec.setTimeDimensions(timeDimensions);

                metricSpecs.add(metricSpec);
            }
        }
        return metricSpecs;
    }

    private Map<String, List<String>> parseDimensionsFromTableauColumns(List<TableauColumn> tableauColumns) {
        Map<String, List<String>> table2Dims = new HashMap<>();
        for (TableauColumn column : tableauColumns) {
            if (column.isDimension() && !column.isHidden()) {
                String tabName = column.getDimensionTable();
                String dim = column.getDimension();
                addDimensionName2TabDimsMap(table2Dims, tabName, dim);
            }
        }
        return table2Dims;
    }

    private Map<String, List<TimeDimension>>
    parseTimeDimensionsFromTableauColumns(List<TableauColumn> tableauColumns) {
        Map<String, List<TimeDimension>> table2TimeDims = new HashMap<>();
        for (TableauColumn column : tableauColumns) {
            if (column.isTimeDimension()) {
                String tabName = column.getDimensionTable();
                String dimName = column.getTimeDimensionName();

                TimeDimension dim = new TimeDimension();
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

        int tableHit = 0;
        String tableNameHit = "";
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
