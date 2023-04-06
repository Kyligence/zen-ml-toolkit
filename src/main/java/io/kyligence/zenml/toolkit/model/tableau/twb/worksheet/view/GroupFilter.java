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

package io.kyligence.zenml.toolkit.model.tableau.twb.worksheet.view;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class GroupFilter {

    @JacksonXmlProperty(localName = "function", isAttribute = true)
    private String function;

    @JacksonXmlProperty(localName = "level", isAttribute = true)
    private String level;

    @JacksonXmlProperty(localName = "member", isAttribute = true)
    private String member;

    @JacksonXmlProperty(localName = "from", isAttribute = true)
    private String from;

    @JacksonXmlProperty(localName = "to", isAttribute = true)
    private String to;

    @JacksonXmlProperty(localName = "groupfilter")
    @JacksonXmlElementWrapper(useWrapping = false)
    private List<GroupFilter> values;
}
