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

package io.kyligence.zenml.toolkit.model.tableau;

import io.kyligence.zenml.toolkit.converter.FileType;
import io.kyligence.zenml.toolkit.exception.ErrorCode;
import io.kyligence.zenml.toolkit.exception.ToolkitException;
import io.kyligence.zenml.toolkit.utils.tableau.TableauDataTypeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TableauParser {

    public static final String TABLEAU_TAG = "Tableau";

    public static final String TABLE = "table";

    public static final String COLLECTION = "collection";

    private final SAXReader reader = new SAXReader();

    private final TableauContentFormatter formatter = new TableauContentFormatter();

    public List<TableauCalculatedFields> parseTableauFile(String filePath, String fileType) throws DocumentException {
        if (StringUtils.equalsIgnoreCase(fileType, FileType.TWB_FILE)) {
            return parseTwbFile(filePath);
        } else if (StringUtils.equalsIgnoreCase(fileType, FileType.TDS_FILE)) {
            return parseTdsFile(filePath);
        }

        log.error(ErrorCode.ILLEGAL_TABLEAU_FILE_TYPE.getReportMessage());
        throw new ToolkitException(ErrorCode.ILLEGAL_TABLEAU_FILE_TYPE);
    }

    public List<TableauCalculatedFields> parseTwbFile(String twbPath) throws DocumentException {
        var twbDoc = reader.read(new File(twbPath));
        return parseTwbContent(twbDoc);
    }

    public List<TableauCalculatedFields> parseTdsFile(String tdsPath) throws DocumentException {
        List<TableauCalculatedFields> metrics = new ArrayList<>();
        var tdsDoc = reader.read(new File(tdsPath));
        var datasourceRoot = tdsDoc.getRootElement();
        metrics.add(parseTdsContent(datasourceRoot));
        return metrics;
    }

    public List<TableauCalculatedFields> parseTableauFileContentInputStream(InputStream inputStream, String fileType)
            throws DocumentException {
        List<TableauCalculatedFields> metrics = new ArrayList<>();
        if (fileType.equalsIgnoreCase(FileType.TDS_FILE)) {
            var tdsDoc = reader.read(inputStream);
            var datasourceRoot = tdsDoc.getRootElement();
            metrics.add(parseTdsContent(datasourceRoot));
        } else if (fileType.equalsIgnoreCase(FileType.TWB_FILE)) {
            var twbDoc = reader.read(inputStream);
            metrics.addAll(parseTwbContent(twbDoc));
        } else {
            log.error(ErrorCode.ILLEGAL_TABLEAU_FILE_TYPE.getReportMessage());
            throw new ToolkitException(ErrorCode.ILLEGAL_TABLEAU_FILE_TYPE);
        }

        return metrics;
    }

    private List<TableauCalculatedFields> parseTwbContent(Document twbDoc) {
        List<TableauCalculatedFields> calculatedFields = new ArrayList<>();
        var workbook = twbDoc.getRootElement();
        var dsElements = workbook.element(TableauFileTag.DATA_SOURCES);
        List<Element> datasources = dsElements.elements(TableauFileTag.DATA_SOURCE);
        for (Element datasource : datasources) {
            // each datasource is a tds root
            calculatedFields.add(parseTdsContent(datasource));
        }

        return calculatedFields;
    }


    private TableauCalculatedFields parseTdsContent(Element datasourceRoot) {
        TableauCalculatedFields calculatedFields = new TableauCalculatedFields();
        checkIfUseColumnAlias(datasourceRoot);
        Map<String, String> columnAlias = parseColumnAlias(datasourceRoot);
        calculatedFields.setTags(getTags(datasourceRoot));
        calculatedFields.setViews(getViews(datasourceRoot));
        calculatedFields.setTables(getTables(datasourceRoot));
        calculatedFields.setAlias2columnMap(columnAlias);
        calculatedFields.setColumns(parseColumns(datasourceRoot, columnAlias));
        return calculatedFields;
    }

    private List<String> getTags(Element datasourceRoot) {
        List<String> tags = new ArrayList<>();
        tags.add(TABLEAU_TAG);

        var nameAttr = datasourceRoot.attribute(TableauFileTag.NAME);
        if (nameAttr != null) {
            tags.add(getAttributeOrDefault(nameAttr));
        } else {
            var formattedNameAttr = datasourceRoot.attribute(TableauFileTag.FORMATTED_NAME);
            if (formattedNameAttr != null) {
                tags.add(getAttributeOrDefault(formattedNameAttr));
            } else {
                var connectionElement = datasourceRoot.element(TableauFileTag.CONNECTION);
                if (connectionElement != null) {
                    var connectionsElement = connectionElement.element(TableauFileTag.NAMED_CONNECTIONS);
                    if (connectionsElement != null) {
                        List<Element> connections = connectionsElement.elements(TableauFileTag.NAMED_CONNECTION);
                        for (Element conn : connections) {
                            String caption = getAttributeOrDefault(conn.attribute(TableauFileTag.CAPTION));
                            if (caption != null) {
                                tags.add(caption);
                            }
                        }
                    }
                }
            }
        }

        return tags;
    }

    private List<String> getViews(Element datasourceRoot) {
        List<String> views = new ArrayList<>();

        var nameAttr = datasourceRoot.attribute(TableauFileTag.NAME);
        if (nameAttr != null) {
            views.add(getAttributeOrDefault(nameAttr));
        } else {
            var formattedNameAttr = datasourceRoot.attribute(TableauFileTag.FORMATTED_NAME);
            if (formattedNameAttr != null) {
                views.add(getAttributeOrDefault(formattedNameAttr));
            }
        }

        return views;
    }

    private List<String> getTables(Element datasourceRoot) {
        List<String> tables = new ArrayList<>();

        var connectionElement = datasourceRoot.element(TableauFileTag.CONNECTION);
        if (connectionElement == null) {
            return tables;
        }

        List<Element> elements = connectionElement.elements();
        for (Element element : elements) {
            if (element.getName().contains(TableauFileTag.RELATION)) {
                // to get tables from relations tag
                var typeAttr = element.attribute(TableauFileTag.TYPE);
                var typeVal = getAttributeOrDefault(typeAttr);
                if (typeVal != null && typeVal.equals(TABLE)) {
                    var nameAttr = element.attribute(TableauFileTag.NAME);
                    var nameVal = getAttributeOrDefault(nameAttr);
                    if (nameVal != null) {
                        tables.add(nameVal);
                    }
                }
            }
        }
        return tables;
    }

    private void checkIfUseColumnAlias(Element datasourceRoot) {
        var aliases = datasourceRoot.element(TableauFileTag.ALIASES);
        if (aliases != null) {
            var enabled = aliases.attribute(TableauFileTag.ENABLED);
            if (enabled != null && enabled.getValue().equalsIgnoreCase(TableauFileTag.YES)) {
                formatter.setUseColumnAlias(true);
            }
        }
    }

    private Map<String, String> parseColumnAlias(Element datasourceRoot) {
        Map<String, String> columnAlias = new HashMap<>();

        var connectionElement = datasourceRoot.element(TableauFileTag.CONNECTION);
        if (connectionElement != null) {
            var colsElement = connectionElement.element(TableauFileTag.COLS);
            if (colsElement != null) {
                List<Element> mapElements = colsElement.elements(TableauFileTag.MAP);
                if (mapElements != null) {
                    for (Element map : mapElements) {
                        var key = map.attributeValue(TableauFileTag.KEY);
                        var val = formatter.formatIdentifier(map.attributeValue(TableauFileTag.VALUE));
                        columnAlias.put(key, val);
                    }
                }
            }
        }
        // if there is no column aliases map, set to false
        if (columnAlias.isEmpty()) {
            formatter.setUseColumnAlias(false);
        }

        return columnAlias;
    }

    private List<TableauColumn> parseColumns(Element datasourceRoot, Map<String, String> columnAlias) {
        List<TableauColumn> tableauColumns = new ArrayList<>();
        List<Element> columns = datasourceRoot.elements(TableauFileTag.COLUMN);
        if (columns != null) {
            for (Element column : columns) {
                tableauColumns.add(retrieveTableauColumnAttributes(column, columnAlias));
            }
        }
        return tableauColumns;
    }

    private TableauColumn retrieveTableauColumnAttributes(Element column, Map<String, String> columnAlias) {
        TableauColumn tableauColumn = new TableauColumn();
        // caption
        var captionAttr = column.attribute(TableauFileTag.CAPTION);
        var caption = getAttributeOrDefault(captionAttr);
        tableauColumn.setCaption(caption);

        // datatype
        var datatypeAttr = column.attribute(TableauFileTag.DATATYPE);
        var datatype = getAttributeOrDefault(datatypeAttr);
        tableauColumn.setDatatype(TableauDataTypeUtils.convertDataType(datatype));

        // role
        var roleAttr = column.attribute(TableauFileTag.ROLE);
        var role = getAttributeOrDefault(roleAttr);
        tableauColumn.setRole(role);

        // name
        // dimension role --> name is column name or alias
        // aggregation role --> name is agg name
        var nameAttr = column.attribute(TableauFileTag.NAME);
        var name = getAttributeOrDefault(nameAttr);
        if (StringUtils.isNotEmpty(role) && StringUtils.equalsIgnoreCase(role, TableauColumn.DIMENSION)) {
            tableauColumn.setName(formatter.getColumnNameFromAlias(columnAlias, name));
        } else {
            tableauColumn.setName(formatter.formatName(name));
        }

        // type
        var typeAttr = column.attribute(TableauFileTag.TYPE);
        tableauColumn.setType(getAttributeOrDefault(typeAttr));

        // hidden
        var hiddenAttr = column.attribute(TableauFileTag.HIDDEN);
        tableauColumn.setHidden(getAttributeOrDefault(hiddenAttr));

        // semantic-role
        var semanticRoleAttr = column.attribute(TableauFileTag.SEMANTIC_ROLE);
        var semanticRole = getAttributeOrDefault(semanticRoleAttr);
        tableauColumn.setSemanticRole(formatter.formatName(semanticRole));

        // aggregation
        var aggAttr = column.attribute(TableauFileTag.AGGREGATION);
        tableauColumn.setAggregation(getAttributeOrDefault(aggAttr));

        // child node: calculation
        var calculationEle = column.element(TableauFileTag.CALCULATION);
        if (calculationEle != null) {
            tableauColumn.setCalculation(parserCalculation(calculationEle, columnAlias));
        }

        return tableauColumn;
    }

    private TableauCalculation parserCalculation(Element calculationEle, Map<String, String> columnAlias) {
        var calc = new TableauCalculation();

        var classAttr = calculationEle.attribute(TableauFileTag.CLASS);
        var calcClass = classAttr.getValue();
        calc.setClazz(calcClass);

        if (calcClass.equalsIgnoreCase(TableauCalculation.CLASS_TABLEAU)) {
            // tableau class means measure, the formula is measure expression
            var formulaAttr = calculationEle.attribute(TableauFileTag.FORMULA);
            calc.setFormula(formatter.formatFormula(getAttributeOrDefault(formulaAttr), columnAlias));
        } else if (calcClass.equalsIgnoreCase(TableauCalculation.CLASS_BIN)) {
            var decimalAttr = calculationEle.attribute(TableauFileTag.DECIMALS);
            calc.setDecimals(getAttributeOrDefault(decimalAttr));

            var formulaAttr = calculationEle.attribute(TableauFileTag.FORMULA);
            calc.setFormula(formatter.formatFormula(getAttributeOrDefault(formulaAttr), columnAlias));

            var pegAttr = calculationEle.attribute(TableauFileTag.PEG);
            calc.setPeg(getAttributeOrDefault(pegAttr));

            var sizeParamAttr = calculationEle.attribute(TableauFileTag.SIZE_PARAMETER);
            calc.setSizeParameter(getAttributeOrDefault(sizeParamAttr));

        } else if (calcClass.equalsIgnoreCase(TableauCalculation.CLASS_CATEGORICAL_BIN)) {
            // skip the child node parse, which named tag is <bin>
            var colAttr = calculationEle.attribute(TableauFileTag.COLUMN);
            calc.setColumn(formatter.getColumnNameFromAlias(columnAlias, getAttributeOrDefault(colAttr)));

            var newBinAttr = calculationEle.attribute(TableauFileTag.NEW_BIN);
            calc.setNewBin(getAttributeOrDefault(newBinAttr));
        }
        return calc;
    }


    private String getAttributeOrDefault(Attribute attr) {
        return attr != null ? attr.getValue() : null;
    }

}

