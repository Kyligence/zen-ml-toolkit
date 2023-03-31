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
# The Dockerfile for Byzer Sandbox
# Byzer Sandbox has different tags for Spark 3.x and Spark 2.4.x.
# Therefore, Byzer-lang and Spark tar names are arguments
#

FROM ubuntu:22.04
MAINTAINER Kyligence


## Environment Variables
ENV ZEN_HOME /opt/Kyligence-ZenML-Toolkit

ADD dist/Kyligence-ZenML-Toolkit $ZEN_HOME
WORKDIR $ZEN_HOME

ENTRYPOINT $ZEN_HOME/bin/entrypoint.sh