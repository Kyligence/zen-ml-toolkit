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

package io.kyligence.zenml.toolkit.source.tableau;

import io.kyligence.zenml.toolkit.converter.FileType;
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

public class TableauParser {

    public static final String TABLEAU_TAG = "Tableau";

    public static final String TABLE = "table";

    public static final String COLLECTION = "collection";
    public static final String IDENTIFIER_REGEX = "[\\[\\]]";
    public static final String WHITE_SPACE = " ";

    public static final String DOT = ".";
    public static final String DOT_REGEX = "\\.";
    public static final String MATH_OPERATOR_REGEX = "((?<=[+\\-*/])|(?=[+\\-*/]))";
    public static final String PARENTHESIS_REGEX = "((?<=[()])|(?=[()]))";
    private final SAXReader reader = new SAXReader();
    private boolean useColumnAlias = false;


    public List<TableauCalculatedFields> parseTableauFile(String filePath, String fileType) throws DocumentException {
        if(StringUtils.equalsIgnoreCase(fileType, FileType.TWB_FILE)){
            return parseTwbFile(filePath);
        }else if (StringUtils.equalsIgnoreCase(fileType, FileType.TDS_FILE)) {
            return parseTdsFile(filePath);
        }

        throw new IllegalArgumentException("Not available tableau file type supported, please check the source file");
    }

    public List<TableauCalculatedFields> parseTwbFile(String twbPath) throws DocumentException {
        Document twbDoc = reader.read(new File(twbPath));
        return parseTwbContent(twbDoc);
    }

    public List<TableauCalculatedFields> parseTdsFile(String tdsPath) throws DocumentException {
        List<TableauCalculatedFields> metrics = new ArrayList<>();
        Document tdsDoc = reader.read(new File(tdsPath));
        Element datasourceRoot = tdsDoc.getRootElement();
        metrics.add(parseTdsContent(datasourceRoot));
        return metrics;
    }

    public List<TableauCalculatedFields> parseTableauFileContentInputStream(InputStream inputStream, String fileType)
            throws DocumentException {
        List<TableauCalculatedFields> metrics = new ArrayList<>();
        if (fileType.equalsIgnoreCase(FileType.TDS_FILE)) {
            Document tdsDoc = reader.read(inputStream);
            Element datasourceRoot = tdsDoc.getRootElement();
            metrics.add(parseTdsContent(datasourceRoot));
        } else if (fileType.equalsIgnoreCase(FileType.TWB_FILE)) {
            Document twbDoc = reader.read(inputStream);
            metrics.addAll(parseTwbContent(twbDoc));
        } else {
            throw new IllegalArgumentException("Only twb file or tds file supported");
        }

        return metrics;
    }

    private List<TableauCalculatedFields> parseTwbContent(Document twbDoc) {
        List<TableauCalculatedFields> calculatedFields = new ArrayList<>();
        Element workbook = twbDoc.getRootElement();
        Element dsElements = workbook.element(TableauFileTag.DATA_SOURCES);
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

        Attribute nameAttr = datasourceRoot.attribute(TableauFileTag.NAME);
        if (nameAttr != null) {
            tags.add(getAttributeOrDefault(nameAttr));
        } else {
            Attribute formattedNameAttr = datasourceRoot.attribute(TableauFileTag.FORMATTED_NAME);
            if (formattedNameAttr != null) {
                tags.add(getAttributeOrDefault(formattedNameAttr));
            } else {
                Element connectionElement = datasourceRoot.element(TableauFileTag.CONNECTION);
                if (connectionElement != null) {
                    Element connectionsElement = connectionElement.element(TableauFileTag.NAMED_CONNECTIONS);
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

        Attribute nameAttr = datasourceRoot.attribute(TableauFileTag.NAME);
        if (nameAttr != null) {
            views.add(getAttributeOrDefault(nameAttr));
        } else {
            Attribute formattedNameAttr = datasourceRoot.attribute(TableauFileTag.FORMATTED_NAME);
            if (formattedNameAttr != null) {
                views.add(getAttributeOrDefault(formattedNameAttr));
            }
        }

        return views;
    }

    private List<String> getTables(Element datasourceRoot) {
        List<String> tables = new ArrayList<>();

        Element connectionElement = datasourceRoot.element(TableauFileTag.CONNECTION);
        if (connectionElement == null) {
            return tables;
        }

        List<Element> elements = connectionElement.elements();
        for (Element element : elements) {
            if (element.getName().contains(TableauFileTag.RELATION)) {
                // to get tables from relations tag
                Attribute typeAttr = element.attribute(TableauFileTag.TYPE);
                String typeVal = getAttributeOrDefault(typeAttr);
                if (typeVal != null && typeVal.equals(TABLE)) {
                    Attribute nameAttr = element.attribute(TableauFileTag.NAME);
                    String nameVal = getAttributeOrDefault(nameAttr);
                    if (nameVal != null) {
                        tables.add(nameVal);
                    }
                }
            }
        }
        return tables;
    }

    private void checkIfUseColumnAlias(Element datasourceRoot) {
        Element aliases = datasourceRoot.element(TableauFileTag.ALIASES);
        if (aliases != null) {
            Attribute enabled = aliases.attribute(TableauFileTag.ENABLED);
            if (enabled != null && enabled.getValue().equalsIgnoreCase(TableauFileTag.YES)) {
                this.useColumnAlias = true;
            }
        }
    }

    private Map<String, String> parseColumnAlias(Element datasourceRoot) {
        Map<String, String> columnAlias = new HashMap<>();

        Element connectionElement = datasourceRoot.element(TableauFileTag.CONNECTION);
        if (connectionElement != null) {
            Element colsElement = connectionElement.element(TableauFileTag.COLS);
            if (colsElement != null) {
                List<Element> mapElements = colsElement.elements(TableauFileTag.MAP);
                if (mapElements != null) {
                    for (Element map : mapElements) {
                        String key = map.attributeValue(TableauFileTag.KEY);
                        String val = formatIdentifier(map.attributeValue(TableauFileTag.VALUE));
                        columnAlias.put(key, val);
                    }
                }
            }
        }
        // if there is no column aliases map, set to false
        if (columnAlias.isEmpty()) {
            this.useColumnAlias = false;
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
        Attribute captionAttr = column.attribute(TableauFileTag.CAPTION);
        String caption = getAttributeOrDefault(captionAttr);
        tableauColumn.setCaption(caption);

        // datatype
        Attribute datatypeAttr = column.attribute(TableauFileTag.DATATYPE);
        String datatype = getAttributeOrDefault(datatypeAttr);
        tableauColumn.setDatatype(TableauDataType.convertDataType(datatype));

        // role
        Attribute roleAttr = column.attribute(TableauFileTag.ROLE);
        String role = getAttributeOrDefault(roleAttr);
        tableauColumn.setRole(role);

        // name
        // dimension role --> name is column name or alias
        // aggregation role --> name is agg name
        Attribute nameAttr = column.attribute(TableauFileTag.NAME);
        String name = getAttributeOrDefault(nameAttr);
        if (role.equalsIgnoreCase(TableauColumn.DIMENSION)) {
            tableauColumn.setName(getColumnNameFromAlias(columnAlias, name));
        } else {
            tableauColumn.setName(formatName(name));
        }

        // type
        Attribute typeAttr = column.attribute(TableauFileTag.TYPE);
        tableauColumn.setType(getAttributeOrDefault(typeAttr));

        // hidden
        Attribute hiddenAttr = column.attribute(TableauFileTag.HIDDEN);
        tableauColumn.setHidden(getAttributeOrDefault(hiddenAttr));

        // semantic-role
        Attribute semanticRoleAttr = column.attribute(TableauFileTag.SEMANTIC_ROLE);
        String semanticRole = getAttributeOrDefault(semanticRoleAttr);
        tableauColumn.setSemanticRole(formatName(semanticRole));

        // aggregation
        Attribute aggAttr = column.attribute(TableauFileTag.AGGREGATION);
        tableauColumn.setAggregation(getAttributeOrDefault(aggAttr));

        // child node: calculation
        Element calculationEle = column.element(TableauFileTag.CALCULATION);
        if (calculationEle != null) {
            tableauColumn.setCalculation(parserCalculation(calculationEle, columnAlias));
        }

        return tableauColumn;
    }

    private TableauCalculation parserCalculation(Element calculationEle, Map<String, String> columnAlias) {
        TableauCalculation calc = new TableauCalculation();

        Attribute classAttr = calculationEle.attribute(TableauFileTag.CLASS);
        String calcClass = classAttr.getValue();
        calc.setClazz(calcClass);

        if (calcClass.equalsIgnoreCase(TableauCalculation.CLASS_TABLEAU)) {
            // tableau class means measure, the formula is measure expression
            Attribute formulaAttr = calculationEle.attribute(TableauFileTag.FORMULA);
            calc.setFormula(formatFormula(getAttributeOrDefault(formulaAttr), columnAlias));
        } else if (calcClass.equalsIgnoreCase(TableauCalculation.CLASS_BIN)) {
            Attribute decimalAttr = calculationEle.attribute(TableauFileTag.DECIMALS);
            calc.setDecimals(getAttributeOrDefault(decimalAttr));

            Attribute formulaAttr = calculationEle.attribute(TableauFileTag.FORMULA);
            calc.setFormula(formatFormula(getAttributeOrDefault(formulaAttr), columnAlias));

            Attribute pegAttr = calculationEle.attribute(TableauFileTag.PEG);
            calc.setPeg(getAttributeOrDefault(pegAttr));

            Attribute sizeParamAttr = calculationEle.attribute(TableauFileTag.SIZE_PARAMETER);
            calc.setSizeParameter(getAttributeOrDefault(sizeParamAttr));

        } else if (calcClass.equalsIgnoreCase(TableauCalculation.CLASS_CATEGORICAL_BIN)) {
            // skip the child node parse, which named tag is <bin>
            Attribute colAttr = calculationEle.attribute(TableauFileTag.COLUMN);
            calc.setColumn(getColumnNameFromAlias(columnAlias, getAttributeOrDefault(colAttr)));

            Attribute newBinAttr = calculationEle.attribute(TableauFileTag.NEW_BIN);
            calc.setNewBin(getAttributeOrDefault(newBinAttr));
        }
        return calc;
    }

    // replace [ ] to blank
    // replace whitespace and underscore to dash -
    // to lowercase
    private String formatIdentifier(String identifier) {
        if (identifier == null) {
            return null;
        }
        return identifier.replaceAll(IDENTIFIER_REGEX, "").replaceAll(WHITE_SPACE, "_").replaceAll("[/-]", "_").trim()
                .toLowerCase();
    }

    private String formatName(String name) {
        if (name == null) {
            return null;
        }
        return name.replaceAll(IDENTIFIER_REGEX, "").trim().toLowerCase();
    }

    private String formatFormula(String formula, Map<String, String> columnAlias) {
        if (formula == null) {
            return null;
        }

        if (useColumnAlias) {
            StringBuilder sb = new StringBuilder();
            if (formula.contains("+") || formula.contains("-") || formula.contains("*") || formula.contains("/")) {
                String[] parts = formula.split(MATH_OPERATOR_REGEX);
                for (int i = 0; i < parts.length; i++) {
                    String part = parts[i];
                    if (part.contains("(") || part.contains(")")) {
                        String[] subParts = part.split(PARENTHESIS_REGEX);
                        for (String subpart : subParts) {
                            String identifier = getColumnNameFromAlias(columnAlias, subpart);
                            sb.append(removeTableIdentifier(identifier));
                        }
                    } else {
                        sb.append(part.toLowerCase());
                    }

                    if (i < parts.length - 1) {
                        sb.append(" ");
                    }
                }
            } else if (formula.contains("(") || formula.contains(")")) {
                String[] subParts = formula.split(PARENTHESIS_REGEX);
                for (String subpart : subParts) {
                    String identifier = getColumnNameFromAlias(columnAlias, subpart);
                    sb.append(removeTableIdentifier(identifier));
                }
            } else {
                String identifier = getColumnNameFromAlias(columnAlias, formula);
                sb.append(removeTableIdentifier(identifier));
            }

            return sb.toString();
        } else {
            return formula.toLowerCase();
        }

    }

    private String getColumnNameFromAlias(Map<String, String> columnAlias, String alias) {
        if (useColumnAlias) {
            return columnAlias.getOrDefault(alias, formatIdentifier(alias));
        } else {
            return formatIdentifier(alias);
        }
    }


    private String removeTableIdentifier(String column) {
        if (column.contains(DOT)) {
            String[] parts = column.split(DOT_REGEX);
            return parts[1];
        }
        return column;
    }

    private String getAttributeOrDefault(Attribute attr) {
        return attr != null ? attr.getValue() : null;
    }

}

