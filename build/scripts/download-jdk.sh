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

#JDK Download Page: https://jdk.java.net/archive/

root_dir=$(cd -P -- "$(dirname -- "$0")/../.." && pwd -P)
if [[ -n "${ZEN_HOME}" ]]; then
   root_dir=${ZEN_HOME}
fi

mkdir -p ${root_dir}/dist/download

cd ${root_dir}/dist/download

function checkSum256() {
    if [ "$1" == "$2" ]; then
      echo "    [OK]...sha256 is correct"
    else
      echo "    [ERROR]...sha256 is not correct, please have a check"
      exit 1
    fi
}

function download_jdk_and_check() {
  # $1: jdk file name
  # $2: jdk download url
  # $3: jdk sha256

  echo "Downloading jdk file: '$1'"
  echo "    - file name: '$1'"
  echo "    - download url: '$2'"
  echo "    - sha256 expected: '$3'"

  if [ ! -f "$1" ]; then
    # jdk not exist, download and check
    wget "$2" -O "$1"
    SHA256_TO_CHECK=$(sha256sum "$1")
    checkSum256 "$3" ${SHA256_TO_CHECK}
  else
    # jdk already downloaded, go check
    SHA256_TO_CHECK=$(sha256sum "$1")
    checkSum256 "$3" ${SHA256_TO_CHECK}
  fi
}

# for linux
LINUX_JDK17="linux_openjdk_17.tar.gz"
LINUX_JDK17_URL="https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_linux-x64_bin.tar.gz"
LINUX_JDK17_SHA256="0022753d0cceecacdd3a795dd4cea2bd7ffdf9dc06e22ffd1be98411742fbb44"

# for mac intel chip
MAC_JDK17="mac_intel_openjdk_17.tar.gz"
MAC_JDK17_URL="https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_macos-x64_bin.tar.gz"
MAC_JDK17_SHA256="b85c4aaf7b141825ad3a0ea34b965e45c15d5963677e9b27235aa05f65c6df06"

# for mac m chip
MAC_M_CHIP_JDK17="mac_m_openjdk_17.tar.gz"
MAC_M_CHIP_JDK17_URL="https://download.java.net/java/GA/jdk17.0.2/dfd4a8d0985749f896bed50d7138ee7f/8/GPL/openjdk-17.0.2_macos-aarch64_bin.tar.gz"
MAC_M_CHIP_SHA256="602d7de72526368bb3f80d95c4427696ea639d2e0cc40455f53ff0bbb18c27c8"

download_jdk_and_check ${LINUX_JDK17} ${LINUX_JDK17_URL} ${LINUX_JDK17_SHA256}
download_jdk_and_check ${MAC_JDK17} ${MAC_JDK17_URL} ${MAC_JDK17_SHA256}
download_jdk_and_check ${MAC_M_CHIP_JDK17} ${MAC_M_CHIP_JDK17_URL} ${MAC_M_CHIP_SHA256}

