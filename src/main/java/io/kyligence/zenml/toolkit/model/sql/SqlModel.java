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

package io.kyligence.zenml.toolkit.model.sql;

import lombok.*;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class SqlModel {
    // left table
    private String factTable;

    private List<JoinRelation> joinRelations;

    public void addJoinRelations(JoinRelation joinRelation) {
        joinRelations.add(joinRelation);
    }

    public String generateModelName() {
        if (joinRelations.isEmpty()) {
            return factTable.toLowerCase();
        }

        var builder = new StringBuilder(factTable);
        for (JoinRelation joinRelation : joinRelations) {
            builder.append("_");
            // add first char of jointype： l/i/r/c
            builder.append(joinRelation.getJoinType().charAt(0));
            builder.append("_");
            builder.append(joinRelation.getRightTable());
        }

        // The lenght of view name in zen has been restricted to 50
        if (builder.length() < 50) {
            return builder.toString().toLowerCase();
        } else {
            // shrink the view name
            var shrinkBuilder = new StringBuilder();
            shrinkBuilder.append(factTable, 0, 4);
            var length = 4;

            for (JoinRelation joinRelation : joinRelations) {
                if (length > 39) {
                    builder.append("_").append(Integer.toString(this.hashCode()), 0, 4);
                }
                builder.append("_");
                // add first char of jointype： l/i/r/c
                builder.append(joinRelation.getJoinType().charAt(0));
                builder.append("_");
                builder.append(joinRelation.getRightTable(), 0, 3);
                length += 6;
            }
            return shrinkBuilder.toString().toLowerCase();
        }
    }

    public boolean canMerge(SqlModel toCheck) {
        if (!StringUtils.equalsIgnoreCase(this.factTable, toCheck.getFactTable())) {
            return false;
        }

        return new HashSet<>(this.joinRelations).containsAll(toCheck.joinRelations);
    }

    public int getJoinDepth() {
        int dep = 1;
        if (this.joinRelations.isEmpty()) {
            return dep;
        }

        for (JoinRelation relation : joinRelations) {
            dep += 1;
        }
        return dep;
    }
}

