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

import io.kyligence.zenml.toolkit.converter.tableau.tds.TdsSpec;
import io.kyligence.zenml.toolkit.converter.tableau.twb.TwbSpec;
import io.kyligence.zenml.toolkit.model.tableau.tds.TableauDatasource;
import io.kyligence.zenml.toolkit.model.tableau.twb.TableauWorkbook;

import java.util.ArrayList;
import java.util.List;

public class TwbAnalyzer {

    public TwbSpec analyzeTwbSpec(TableauWorkbook twb) {
        var twbDs = twb.getDatasources();
        List<TableauDatasource> tdsList = twbDs.getDatasources();

        var tdsAnalyzer = new TdsAnalyzer();
        List<TdsSpec> tdsSpecs = new ArrayList<>();
        for (TableauDatasource tds : tdsList) {
            var tdsSpec = tdsAnalyzer.analyzeTdsSpec(tds);
            if (tdsSpec == null)
                continue;
            tdsSpecs.add(tdsSpec);
        }
        var twbSpec = new TwbSpec();
        twbSpec.setTdsSpecs(tdsSpecs);
        return twbSpec;
    }
}
