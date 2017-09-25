#!/bin/bash
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#
# See the License for the specific language governing permissions and
# limitations under the License.

#脚本以ADMINUSER用户执行，注意sudo


LOG_FILE="/var/log/bdos-azure-initialize.log"

EXECNAME="mianmi.sh"

HOSTS_LIST=$1
ADMINUSER=$2
ADMINPASSWORD=$3
SSHPORT=$4


log() {
    echo "$(date) [${EXECNAME}]: $*" >> "${LOG_FILE}"
}

log "install expect "
#install expect
sudo yum install expect -y > /dev/null
if [ $? != 0  ];then
    log "------------------------------please chech your yum resource"
    exit 1
fi

HOSTS_LIST_ARR=$(echo $HOSTS_LIST|tr "," "\n")
log "------------------------------for all node do ..."
for HOST in $HOSTS_LIST_ARR;do
    log "----------------------------hostname:$HOST"
    if [ ! -f ~/.ssh/id_rsa ] || [ !  -f ~/.ssh/id_rsa.pub ];then

        log "create ssh key"
        expect -c "
                set timeout -1;
                spawn ssh-keygen
                expect {
                        \"*Enter file*\" {send \"\r\"; exp_continue}
                        \"*Enter passphrase*\" {send \"\r\"; exp_continue}
                        \"*Enter same passphrase*\" {send \"\r\"; exp_continue}
                        \"*The key fingerprint*\" {send \"\r\"; exp_continue}
                }"
        log "successs make key"
    fi

    if [ -f ~/.ssh/id_rsa ] && [ -f ~/.ssh/id_rsa.pub ];then
        log "ssh key is maked , ssh-copy-id "
        expect -c "
                set timeout -1;
                spawn  ssh-copy-id \"-p$SSHPORT $USER@$HOST\"
                expect {
                        \"*yes/no*\" {send \"yes\r\"; exp_continue}
                        \"*password*\" {send \"$ADMINPASSWORD\r\"; exp_continue}
                }"
        log "ssh-copy-id success !"
    fi

done

exit 0