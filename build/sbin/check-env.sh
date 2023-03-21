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

source $(cd -P -- "$(dirname -- "$0")" && pwd -P)/header.sh

mkdir -p ${ZEN_HOME}/logs

# avoid re-entering
if [[ "$CHECKENV_ING" == "" ]]; then
    export CHECKENV_ING=true

    mkdir -p ${ZEN_HOME}/logs
    LOG=${ZEN_HOME}/logs/shell.stdout
    ERRORS=${ZEN_HOME}/logs/shell.stderr
    TITLE="#title"

        verbose ""
        verbose $(setColor 33 "Zen-ML-ToolKit is checking installation environment, log is at ${LOG}")
        verbose ""

        rm -rf ${ZEN_HOME}/logs/tmp
        rm -f ${ERRORS}
        touch ${ERRORS}

        export CHECKENV_REPORT_PFX=">   "
        export QUIT_MESSAGE_LOG=${ERRORS}


#        CHECK_FILES=
        CHECK_FILES=$(ls ${ZEN_HOME}/sbin/check-*.sh)
        for f in ${CHECK_FILES[@]}
        do
            if [[ ! $f == *check-env.sh ]]; then

                verbose ""                                                                             >>${LOG}
                verbose "============================================================================" >>${LOG}
                verbose "Checking $(basename $f)"                                                      >>${LOG}
                verbose "----------------------------------------------------------------------------" >>${LOG}
                bash $f >>${LOG} 2>&1
                rtn=$?
                if [[ $rtn == 0 ]]; then
                    verbose "[$(setColor 32 PASS)] "$(getValueByKey ${TITLE} ${f})
                elif [[ $rtn == 3 ]]; then
                    verbose "[$(setColor 33 SKIP)] "$(getValueByKey ${TITLE} ${f})
                elif [[ $rtn == 4 ]];then
                    verbose "[$(setColor 33 WARN)] "$(getValueByKey ${TITLE} ${f})
                    WARN_INFO=$(tail -n 3 ${LOG})
                    verbose $(setColor 33 "WARNING:")
                    verbose -e "$WARN_INFO"  | sed 's/^/    &/g'
                else
                    echo "[$(setColor 31 FAIL)] "$(getValueByKey ${TITLE} ${f})
                    cat  ${ERRORS} >> ${LOG}
                    tail ${ERRORS}
                    verbose $(setColor 33 "Full log is at: ${LOG}")
                    exit 1
                fi
            fi
        done
        verbose ""
        cat ${LOG} | grep "^${CHECKENV_REPORT_PFX}"
        verbose $(setColor 33 "Checking environment finished successfully. To check again, run 'sbin/check-env.sh' manually.")
        verbose ""

    export CHECKENV_ING=
fi