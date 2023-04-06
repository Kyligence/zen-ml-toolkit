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

MAIN_JAR="zen-ml-toolkit.jar"

function setLogRotate() {
    $ZEN_HOME/sbin/rotate-logs.sh
}

function recordStartOrStop() {
    currentIp=$(ifconfig | grep -Eo 'inet (addr:)?([0-9]*\.){3}[0-9]*' | grep -Eo '([0-9]*\.){3}[0-9]*' | grep -v '127.0.0.1' | head -n 1)
    serverPort=`$ZEN_HOME/bin/get-properties.sh zen.ml.toolkit.server.port`
    echo `date '+%Y-%m-%d %H:%M:%S '`"INFO : [Operation: $1] user:`whoami`, start time:$2, ip and port:${currentIp}:${serverPort}" >> ${ZEN_HOME}/logs/security.log
}

function clearRedundantProcess {
    if [ -f "${ZEN_HOME}/pid" ]
    then
        pidKeep=0
        pidRedundant=0
        for pid in `cat ${ZEN_HOME}/pid`
        do
            pidActive=`ps -ef | grep $pid | grep ${ZEN_HOME} | wc -l`
            if [ "$pidActive" -eq 1 ]
            then
                if [ "$pidKeep" -eq 0 ]
                then
                    pidKeep=$pid
                else
                    echo "Redundant ZenML Toolkit process $pid to running process $pidKeep, stop it."
                    bash ${ZEN_HOME}/bin/kill-process-tree.sh $pid
                    ((pidRedundant+=1))
                fi
            fi
        done
        if [ "$pidKeep" -ne 0 ]
        then
            echo $pidKeep > ${ZEN_HOME}/pid
        else
            rm ${ZEN_HOME}/pid
        fi
        if [ "$pidRedundant" -ne 0 ]
        then
            quit "ZenML Toolkit is redundant, start canceled."
        fi
    fi
}

function prepareEnv {
    export TOOLKIT_CONFIG_FILE="${ZEN_HOME}/conf/toolkit.properties"
    mkdir -p ${ZEN_HOME}/logs

    echo ""
    echo "ZEN_HOME is:${ZEN_HOME}"
    echo "TOOLKIT_CONFIG_FILE is:${TOOLKIT_CONFIG_FILE}"
    echo "ZEN_LOG_FOLDER is：${ZEN_HOME}/logs ."
    echo ""
}

function checkIfStopUserSameAsStartUser() {
    startUser=`ps -p $1 -o user=`
    currentUser=`whoami`

    if [ ${startUser} != ${currentUser} ]; then
        echo `setColor 33 "Warning: You started Kyligence ZenML Toolkit server as user [${startUser}], please stop the instance as the same user."`
    fi
}


function start() {
      setLogRotate
      clearRedundantProcess

      # check $ZEN_HOME
      [[ -z ${ZEN_HOME} ]] && quit "{ZEN_HOME} is not set, exit"
      if [ -f "${ZEN_HOME}/pid" ]; then
          PID=`cat ${ZEN_HOME}/pid`
          if ps -p $PID > /dev/null; then
            quit "Kyligence ZenML Toolkit server is already running, stop it first, PID is $PID"
          fi
      fi

      START_TIME=$(date "+%Y-%m-%d %H:%M:%S")

      recordStartOrStop "start" "${START_TIME}"

      prepareEnv


      ${ZEN_HOME}/sbin/port-check.sh >> ${ZEN_HOME}/logs/check-env.out 2>&1
      [[ $? == 0 ]] || quit "ERROR: Port ${NOTEBOOK_PORT} is in use, another Kyligence ZenML Toolkit server is running?"


      echo "${START_TIME} Start Kyligence ZenML Toolkit server..."
      nohup ${JAVA} -Xms${JAVA_VM_XMS} -Xmx${JAVA_VM_XMX} -Dserver.port=${TOOLKIT_SERVER_PORT} -DZEN_HOME=${ZEN_HOME}  -jar  "${ZEN_HOME}/lib/${MAIN_JAR}"  >> ${ZEN_HOME}/logs/toolkit.out 2>&1 & echo $! >> ${ZEN_HOME}/pid &

      sleep 3
      clearRedundantProcess

      PID=`cat ${ZEN_HOME}/pid`
      [[ -z $PID ]] && quit "Starting Kyligence ZenML Toolkit server failed, please check error in ${ZEN_HOME}/logs/shell.stderr"

      CUR_DATE=$(date "+%Y-%m-%d %H:%M:%S")
      echo $CUR_DATE" new Kyligence ZenML Toolkit process pid is "$PID >> ${ZEN_HOME}/logs/toolkit.out

      echo ""
      echo $(setColor 33 "Kyligence ZenML Toolkit server is starting. It may take a while. For status, please visit http://$TOOLKIT_SERVER_IP:$TOOLKIT_SERVER_PORT.")
      echo ""
      echo "You may also check status via: PID:`cat ${ZEN_HOME}/pid`, or Log: ${ZEN_HOME}/logs/toolkit.out."
      recordStartOrStop "start success" "${START_TIME}"

}

function stop(){

    STOP_TIME=$(date "+%Y-%m-%d %H:%M:%S")
    if [ -f "${ZEN_HOME}/pid" ]; then
        PID=`cat ${ZEN_HOME}/pid`
        if ps -p $PID > /dev/null; then

           checkIfStopUserSameAsStartUser $PID

           kill $PID
           for i in {1..10}; do
              sleep 3
              if ps -p $PID -f | grep zen-ml > /dev/null; then
                echo "loop $i"
                 if [ "$i" == "10" ]; then
                    echo `date '+%Y-%m-%d %H:%M:%S '`"Killing Kyligence ZenML toolkit server process: $PID"
                    kill -9 $PID
                 fi
                 continue
              fi
              break
           done
           rm ${ZEN_HOME}/pid

           recordStartOrStop "stop" "${STOP_TIME}"
           echo `date '+%Y-%m-%d %H:%M:%S '`"Killing Kyligence ZenML toolkit server: $PID has been Stopped"
           return 0
        else
           return 1
        fi

    else
        return 1
    fi
}

# start command
if [ "$1" == "start" ]; then
    echo "Starting Kyligence ZenML toolkit server..."
    start
# stop command
elif [ "$1" == "stop" ]; then
    echo `date '+%Y-%m-%d %H:%M:%S '`"Stopping Kyligence ZenML toolkit server..."
    stop
    if [[ $? == 0 ]]; then
        exit 0
    else
        quit "Kyligence ZenML toolkit server is not running"
    fi
# restart command
elif [ "$1" == "restart" ]; then
    echo "Restarting Kyligence ZenML toolkit server..."
    echo "--> Stopping Kyligence ZenML toolkit server first if it's running..."
    stop
    if [[ $? != 0 ]]; then
        echo "    Kyligence ZenML toolkit server is not running, now start it"
    fi
    echo "--> Starting Kyligence ZenML toolkit server..."
    start
else
    quit "Illegal arguments"
fi