

#!/usr/bin/env bash

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

#title=Checking OS

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh

echo "Checking OS..."

if [[ $MACHINE_OS == "Linux" ]]; then
    echo "The current operating system is Linux based distribution."
elif [[ $MACHINE_OS == "Mac" ]]; then
    echo "The current operating system is MacOS."
else
    quit "Not suppported operating system:  $MACHINE_OS"
fi