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

echo "Begin to package..."
# project root dir absolute path

root_dir=$(cd -P -- "$(dirname -- "$0")/../.." && pwd -P)
if [[ -n "${ZEN_HOME}" ]]; then
   root_dir=${ZEN_HOME}
fi

echo "ZEN_HOME: ${root_dir}"
cd ${root_dir}

## build console fat jar
mvn_version=$(mvn -q -Dexec.executable=echo -Dexec.args='${project.version}' --non-recursive exec:exec)
if [[ -z "${version}" ]]; then
    export version=${mvn_version}
fi

#[[ -z "${mvn_profile}" ]] && profile='test' || profile=${mvn_profile}

echo "Maven Package Profile : ${profile}"

#mvn clean install -DskipTests -P${profile}

mvn clean install -DskipTests

# collect release package resource
cd ${root_dir}
export package_name=Kyligence-ZenML-Toolkit-${version}

[[ ! -d "dist" ]] && mkdir -p "dist" || rm -rf dist/*
mkdir -p dist/${package_name}

cd dist/${package_name}

## 1. make changelog file
if [[ -z "${changelog}" ]]; then
    echo "changelog not set, use UNKNOWN instead."
    changelog="UNKNOWN"
fi

echo "${changelog}" > ./CHANGELOG.md

## 2. make commit_sha file
echo `git rev-parse HEAD` | tee commit_SHA1
cd ${root_dir}/dist/${package_name}

## 3. make version file
echo "${version}" > VERSION


## 4. copy console jar file
mkdir lib
cp ${root_dir}/target/zen-ml-toolkit-${version}.jar lib/zen-ml-toolkit.jar

## 6. copy scripts
mkdir bin
cp ${root_dir}/build/bin/* bin
mkdir sbin
cp ${root_dir}/build/sbin/* sbin


## 7. others
mkdir logs

## 8. package
cd ${root_dir}/dist

tar -zcvf ${package_name}.tar.gz ${package_name}
echo "====================================="
echo "Build Finished!"
echo "Location: ${root_dir}/dist/${package_name}.tar.gz"
