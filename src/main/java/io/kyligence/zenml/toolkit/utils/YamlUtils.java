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

package io.kyligence.zenml.toolkit.utils;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator;
import lombok.SneakyThrows;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;

@Slf4j
@UtilityClass
public class YamlUtils {
    @SuppressWarnings("AlibabaConstantFieldShouldBeUpperCase")
    private static final ObjectMapper OBJECT_MAPPER;

    static {
        YAMLFactory factory = new YAMLFactory();
        factory.disable(YAMLGenerator.Feature.USE_NATIVE_TYPE_ID);
        OBJECT_MAPPER = new ObjectMapper(factory);
        OBJECT_MAPPER.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
        OBJECT_MAPPER.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES);
        OBJECT_MAPPER.configure(SerializationFeature.INDENT_OUTPUT, true);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);
        OBJECT_MAPPER.setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY);
        OBJECT_MAPPER.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
    }

    @SneakyThrows(JsonProcessingException.class)
    public String objectToString(Object obj) {
        return OBJECT_MAPPER.writeValueAsString(obj);
    }

    @SneakyThrows(JsonProcessingException.class)
    public <T> T stringToObject(String json, Class<T> clazz) {
        return OBJECT_MAPPER.readValue(json, clazz);
    }

    @SneakyThrows(JsonProcessingException.class)
    public <T> T stringToObject(String json, TypeReference<T> valueTypeRef) {
        return OBJECT_MAPPER.readValue(json, valueTypeRef);
    }

    @SneakyThrows(JsonProcessingException.class)
    public <T> T stringToObject(byte[] json, TypeReference<T> valueTypeRef) throws IOException {
        return OBJECT_MAPPER.readValue(json, valueTypeRef);
    }

    @SneakyThrows(JsonProcessingException.class)
    public <T> T stringToObject(byte[] json, Class<T> valueTypeRef) throws IOException {
        return OBJECT_MAPPER.readValue(json, valueTypeRef);
    }

    public void writeValue(File resultFile, Object value) throws IOException {
        OBJECT_MAPPER.writeValue(resultFile, value);
    }

}
