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

package io.kyligence.zenml.toolkit.metrics;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.util.List;
import java.util.Map;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@Getter
@Setter
@Builder
@ToString
public class MetricSpec {
    @JsonProperty("id")
    private String id;

    @JsonProperty("status")
    private MetricStatus status;

    @JsonProperty("extend")
    private Map<String, String> extend;

    @JsonProperty("name")
    private String name;
    @JsonProperty("display_name")
    private String display;

    @JsonProperty("description")
    private String description;
    @JsonProperty("type")
    private MetricType type;
    @JsonProperty("expression")
    private String expression;
    @JsonProperty("format")
    private Object format;
    @JsonProperty("data_model")
    private String dataModel;
    @JsonProperty("dimensions")
    private List<String> dimensions;
    @JsonProperty("time_dimensions")
    private List<TimeDimension> timeDimensions;
    @JsonProperty("filters")
    private List<Object> filters;
    @JsonProperty("tags")
    private List<String> tags;
}
