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


echo "Begin to package frontend..."
# project root dir absolute path
root_dir=$(cd -P -- "$(dirname -- "$0")/../.." && pwd -P)
if [[ -n "${ZEN_HOME}" ]]; then
   root_dir=${ZEN_HOME}
fi

cd ${root_dir}/frontend
rm -rf package-lock.json
rm -rf node_modules
npm cache verify || { exit 1; }
npm install || { exit 1; }
npm run build || { exit 1; }
