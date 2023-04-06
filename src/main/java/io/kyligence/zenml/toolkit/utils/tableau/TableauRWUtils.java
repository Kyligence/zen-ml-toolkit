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

package io.kyligence.zenml.toolkit.utils.tableau;

import com.ctc.wstx.stax.WstxInputFactory;
import com.ctc.wstx.stax.WstxOutputFactory;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlFactory;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import io.kyligence.zenml.toolkit.config.ToolkitConfig;
import io.kyligence.zenml.toolkit.model.tableau.tds.TableauDatasource;
import io.kyligence.zenml.toolkit.model.tableau.twb.TableauWorkbook;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TableauRWUtils {
    private static ToolkitConfig config = ToolkitConfig.getInstance();
    private static final Pattern datasourceDepPattern = Pattern.compile("<datasource-dependencies[\\s\\S]+?</datasource-dependencies>");

    private static final Pattern colInstancePattern = Pattern.compile("(<column-instanceTableauRW?/>)|(<column-instance[\\s\\S]+?</column-instance>)");

    private static final Pattern columnPattern = Pattern.compile("(<column (?!column).*?/>)|(<column [\\s\\S]*?</column>)");

    private static final Pattern categoryCCPattern = Pattern.compile("<bin default-name=.*?value=.*?>");

    private static final String LINE_SEP = System.getProperty("line.separator");

    public static <T> String writeObjectAsXmlString(T source) {
        try (StringWriter stringWriter = new StringWriter()) {
            XmlMapper xmlMapper = new XmlMapper();
            XMLStreamWriter xsw = xmlMapper.getFactory().getXMLOutputFactory().createXMLStreamWriter(stringWriter);
            xmlMapper.enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION);
            xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
            xmlMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            xmlMapper.getFactory().getXMLOutputFactory().setProperty("javax.xml.stream.isRepairingNamespaces", false);
            xmlMapper.writeValue(xsw, source);
            return stringWriter.toString();
        } catch (IOException | XMLStreamException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TableauWorkbook readTwb(String filePath) {
        File twb = new File(filePath);
        return readTwb(twb);
    }

    public static TableauWorkbook readTwb(File twbFile) {
        String twbStr = readFileAsString(twbFile);
        StringReader strReader = new StringReader(twbStr);
        XmlMapper mapper = new XmlMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        try (BufferedReader reader = new BufferedReader(strReader)) {
            return mapper.readValue(reader, TableauWorkbook.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static TableauDatasource getTemplateTds() {
        String templateTdsFilePath = config.getTemplateTdsFilePath();
        File tdsFile = new File(templateTdsFilePath);
        XmlMapper mapper = new XmlMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);

        try (BufferedReader reader = new BufferedReader(new FileReader(tdsFile))) {
            return mapper.readValue(reader, TableauDatasource.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String readFileAsString(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            StringBuffer sb = new StringBuffer();
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
                sb.append(LINE_SEP);
            }
            return sb.toString();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getTableauMapping(String mappingFileName) {
        String mappingFilePath = ToolkitConfig.getConfDirPath() + File.separator + mappingFileName;
        File mappingFile = new File(mappingFilePath);
        return readFileAsString(mappingFile);
    }

    public static TableauDatasource getTds(File tdsFile) {
        String originTds = readFileAsString(tdsFile);
        return getTds0(originTds);
    }

    public static TableauDatasource getTds0(String tdsStr) {
        Matcher matcher = categoryCCPattern.matcher(tdsStr);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            String origin = matcher.group(0);
            matcher.appendReplacement(sb, origin.replaceFirst("value=", "value_10086="));
        }
        matcher.appendTail(sb);

        XMLInputFactory input = new WstxInputFactory();
        input.setProperty(XMLInputFactory.IS_NAMESPACE_AWARE, Boolean.FALSE);
        XmlMapper mapper = new XmlMapper(new XmlFactory(input, new WstxOutputFactory()));
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        try (BufferedReader reader = new BufferedReader(new StringReader(sb.toString()))) {
            return mapper.readValue(reader, TableauDatasource.class);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void writeTds(TableauDatasource tds, String outputDir, String fileName) {
        String originTds = writeObjectAsXmlString(tds);
        writeTds0(outputDir, fileName, originTds);
    }

    public static void writeTds0(String outputDir, String fileName, String tdsStr) {
        // 提取分组维
        tdsStr = tdsStr.replaceAll("value_10086", "value");
        File file = new File(outputDir + "/" + fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(tdsStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static <T> void writeModelJson(T t, String outputDir, String fileName) {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdir();
        }
        File file = new File(outputDir + "/" + fileName);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(writer, t);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
