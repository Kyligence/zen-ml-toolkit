#!/bin/bash
#
# Licensed to the Apache Software Foundation (ASF) under one or more
# contributor license agreements.  See the NOTICE file distributed with
# this work for additional information regarding copyright ownership.
# The ASF licenses this file to You under the Apache License, Version 2.0
# (the "License"); you may not use this file except in compliance with
# the License.  You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
root_dir=$(cd -P -- "$(dirname -- "$0")/../.." && pwd -P)
if [[ -n "${ZEN_HOME}" ]]; then
  root_dir=${ZEN_HOME}
fi
export VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:2.1.1:evaluate -Dexpression=project.version | grep -Ev '(^\[|Download\w+:)')

cd ${root_dir}
sh build/scripts/package.sh

cd ${root_dir}/dist
tar -zxvf Kyligence-ZenML-Toolkit-Linux-x64-$VERSION.tar.gz

cd ${root_dir}/dist/Kyligence-ZenML-Toolkit-Linux-x64-$VERSION
mv conf/toolkit.properties.docker conf/toolkit.properties.override

cd ${root_dir}/dist
mv Kyligence-ZenML-Toolkit-Linux-x64-$VERSION Kyligence-ZenML-Toolkit

docker build -t kyligence/zenml-toolkit:${VERSION}  --no-cache ${root_dir}/
