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

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh $@
source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/setenv.sh

"${ZEN_HOME}"/sbin/rotate-logs.sh "$@"

MAIN_JAR="zen-ml-toolkit.jar"

function runCommand() {
    java -Xms${JAVA_VM_XMS} -Xmx${JAVA_VM_XMX} -Dfile.encoding=UTF-8 -jar "${ZEN_HOME}/lib/${MAIN_JAR}" "$@"
    exit $?
}

runCommand "$@"
