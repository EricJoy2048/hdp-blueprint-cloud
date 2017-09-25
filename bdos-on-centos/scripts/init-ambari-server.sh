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

LOG_FILE="/var/log/bdos-azure-initialize.log"

EXECNAME="init-ambari-server.sh"
AMBARI_MYSQL_ROOT_PASSWORD=$1
LINUX_ADMIN_USER=$2

# logs everything to the LOG_FILE
log() {
  echo "$(date) [${EXECNAME}]: $*" >> ${LOG_FILE}
}



#start mysql server
/etc/init.d/bfd-mysqld restart
#update mysql root user password
mysqladmin -u root password root123 $AMBARI_MYSQL_ROOT_PASSWORD

#start ambari-server
su - $LINUX_ADMIN_USER -c "sudo ambari-server restart && sudo ambari-server setup --jdbc-db=mysql --jdbc-driver=/usr/share/java/mysql-connector-java.jar"



exit 0