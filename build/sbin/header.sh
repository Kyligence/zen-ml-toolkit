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


# source me

function isValidJavaVersion() {
    version=$(${JAVA} -version 2>&1 | awk -F\" '/version/ {print $2}')
    version_first_part="$(echo ${version} | cut -d '.' -f1)"
    version_second_part="$(echo ${version} | cut -d '.' -f2)"

    if [[ "$version_first_part" -lt "17" ]]; then
        echo "false"
        exit 0;
    fi

    echo "true"
}

function verboseLog() {
    (>&2 echo $(date '+%F %H:%M:%S') "$@")
}

# avoid re-entering
if [[ "$dir" == "" ]]
then
    dir=$(cd -P -- "$(dirname -- "$0")" && pwd -P)

    # misc functions
    function quit {
        echo "$@"
        if [[ -n "${QUIT_MESSAGE_LOG}" ]]; then
            echo `setColor 31 "$@"` >> ${QUIT_MESSAGE_LOG}
        fi
        exit 1
    }

    function verbose {
        if [[ -n "$verbose" ]]; then
            echo "$@"
        fi
    }

    function setColor() {
        echo -e "\033[$1m$2\033[0m"
    }

    function getValueByKey() {
        while read line
        do key=${line%=*} val=${line#*=}
        if [ "${key}" == "$1" ]; then
            echo $val
            break
        fi
        done<$2
    }


    # set ZEN_HOME with consideration for multiple instances that are on the same node
    CURRENT=`cd "${dir}/../"; pwd`
    [[ -z "$ZEN_HOME" ]] || [[ "${CURRENT}" == "${ZEN_HOME}" ]] || quit "ZEN_HOME=${ZEN_HOME}, doesn't set correctly, please make sure it is set as current dir: ${CURRENT}, or leave it NULL, i.e. 'export ZEN_HOME='"

    # have a check to avoid repeating verbose message
    if [[ "${ZEN_HOME}" != "${CURRENT}" ]]; then
        export ZEN_HOME=${CURRENT}
        verbose "ZEN_HOME is ${ZEN_HOME}"
    fi

    # set ZenML toolkit server IP
    if [ -z $TOOLKIT_SERVER_IP ];then
      export TOOLKIT_SERVER_IP=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -n 1)
    fi



    # set ZenML toolkit server port
    export TOOLKIT_SERVER_PORT=$($ZEN_HOME/sbin/get-properties.sh zen.ml.toolkit.server.port)

    if [[ -z ${TOOLKIT_SERVER_PORT} ]]; then
        export TOOLKIT_SERVER_PORT=9000
    fi

    unameOut="$(uname -s)"
    case "${unameOut}" in
        Linux*)     os=Linux;;
        Darwin*)    os=Mac;;
        CYGWIN*)    os=Cygwin;;
        MINGW*)     os=MinGw;;
        *)          os="UNKNOWN:${unameOut}"
    esac
    export MACHINE_OS=$os
    
     # set JAVA
     if [[ "${JAVA}" == "" ]]; then
         if [[ $MACHINE_OS == "Mac" ]]; then
             if [[ -d "${ZEN_HOME}/jdk/Contents/Home/" ]]; then
                 # try to use embedded open jdk first
                 JAVA_HOME=${ZEN_HOME}/jdk/Contents/Home
             elif  command -v java &> /dev/null ; then
                  # embedded jdk not found, try to use jdk in system
                 JAVA_HOME=$(dirname $(dirname $(readlink $(which java))))
             else
                 quit "Java environment not found, Java 17 or above is required."
             fi
         elif [[ $MACHINE_OS == "Linux" ]]; then
             if [[ -d "${ZEN_HOME}/jdk/" ]]; then
                 # No Java found, try to use embedded open jdk
                 JAVA_HOME="${ZEN_HOME}"/jdk
             elif command -v java &> /dev/null ; then
             # try to use jdk in system
                 JAVA_HOME=$(dirname $(dirname $(readlink -f $(which java))))
             else
                 quit "Java environment not found, Java 17 or above is required."
             fi
         else
             quit "Not suppported operating system:  $MACHINE_OS"
         fi

         [[ -z "$JAVA_HOME" ]] && quit "JAVA_HOME is not found, please set JAVA_HOME"
         export JAVA_HOME
  
         # check java command is found
         export JAVA=$JAVA_HOME/bin/java
         [[ -e "${JAVA}" ]] || quit "${JAVA} does not exist. Please set JAVA_HOME correctly."
         verbose "java is ${JAVA}"
     fi
fi