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

EXECNAME=$0
MASTERIP=$1
ADMINUSER=$2
NODETYPE=$3
COPYINDEX=$4
LINUX_ADMIN_USER=$5
AMBARI_MYSQL_ROOT_PASSWORD=$6
SSH_PORT=$7
YUM_NODE=$8


# logs everything to the LOG_FILE
log() {
  echo "$(date) [${EXECNAME}]: $*" >> ${LOG_FILE}
}

log $MASTERIP
log $ADMINUSER
log $NODETYPE
log $COPYINDEX
log $LINUX_ADMIN_USER
log $AMBARI_MYSQL_ROOT_PASSWORD
log $SSH_PORT
log $YUM_NODE


# Disable the need for a tty when running sudo and allow passwordless sudo for the admin user
sed -i '/Defaults[[:space:]]\+!*requiretty/s/^/#/' /etc/sudoers
echo "$ADMINUSER ALL=(ALL) NOPASSWD: ALL" >> /etc/sudoers

# Mount and format the attached disks base on node type
log "---------------Mount and format the attached disks for ${NODETYPE}"
if [ "$NODETYPE" == "masternode" ]
then 
  bash ./prepare-masternode-disks.sh >> ${LOG_FILE} 2>&1
elif [ "$NODETYPE" == "datanode" ]
then
  bash ./prepare-datanode-disks.sh >> ${LOG_FILE} 2>&1
elif [ "$NODETYPE" == "thriftnode" ]
then
  bash ./prepare-thriftnode-disks.sh >> ${LOG_FILE} 2>&1
elif [ "$NODETYPE" == "runnernode" ]
then
  bash ./prepare-runnernode-disks.sh >> ${LOG_FILE} 2>&1
elif [ "$NODETYPE" == "webnode" ]
then
  bash ./prepare-webnode-disks.sh >> ${LOG_FILE} 2>&1
elif [ "$NODETYPE" == "yumreponode" ]
then
  bash ./prepare-yumreponode-disks.sh >> ${LOG_FILE} 2>&1
else
  log "Unknown node type : ${NODETYPE}, default to datanode"
  bash ./prepare-datanode-disks.sh >> ${LOG_FILE} 2>&1
fi

log "---------------Done preparing disks. Now 'ls -la /' looks like this:"
ls -la / >> ${LOG_FILE} 2>&1

# Disable SELinux
log "---------------Disable SELinux"
setenforce 0 >> /tmp/setenforce.out
cat /etc/selinux/config > /tmp/beforeSelinux.out
sed -i 's^SELINUX=enforcing^SELINUX=disabled^g' /etc/selinux/config || true
cat /etc/selinux/config > /tmp/afterSeLinux.out

# Disable iptables
log "---------------Disable iptables"
/etc/init.d/iptables save
/etc/init.d/iptables stop
chkconfig iptables off

# Install and start NTP
log "---------------Install and start NTP"
yum install -y ntp
service ntpd start
service ntpd status
chkconfig ntpd on

# Disable THP
log "---------------Disable THP"
echo never | tee -a /sys/kernel/mm/transparent_hugepage/enabled
echo "echo never | tee -a /sys/kernel/mm/transparent_hugepage/enabled" | tee -a /etc/rc.local

# Set swappiness to 1
log "---------------Set swappiness to 1"
echo vm.swappiness=1 | tee -a /etc/sysctl.conf
echo 1 | tee /proc/sys/vm/swappiness


#modify max openfile and max process
cat >> /etc/security/limits.conf << EOF
*           soft   nofile       655350
*           hard   nofile       655350
*           soft   nproc        655350
*           hard   nproc        655350
EOF

sed -i 's/1024.*/65000/' /etc/security/limits.d/90-nproc.conf

# Set system tuning params
log "---------------Set system tuning params"
cat > /etc/sysctl.conf << EOF
net.bridge.bridge-nf-call-ip6tables = 0
net.bridge.bridge-nf-call-iptables = 0
net.bridge.bridge-nf-call-arptables = 0
net.ipv4.ip_forward = 0
net.ipv4.conf.default.rp_filter = 1
net.ipv4.conf.default.accept_source_route = 0
kernel.sysrq = 0
kernel.core_uses_pid = 1
net.ipv4.tcp_syncookies = 1
kernel.msgmnb = 65536
kernel.msgmax = 65536
kernel.shmmax = 68719476736
kernel.shmall = 4294967296
net.ipv4.tcp_max_tw_buckets = 60000
net.ipv4.tcp_sack = 1
net.ipv4.tcp_window_scaling = 1
net.ipv4.tcp_rmem = 4096 87380 4194304
net.ipv4.tcp_wmem = 4096 16384 4194304
net.core.wmem_default = 8388608
net.core.rmem_default = 8388608
net.core.rmem_max = 16777216
net.core.wmem_max = 16777216
net.core.netdev_max_backlog = 262144
net.core.somaxconn = 262144
net.ipv4.tcp_max_orphans = 3276800
net.ipv4.tcp_max_syn_backlog = 262144
net.ipv4.tcp_timestamps = 0
net.ipv4.tcp_synack_retries = 1
net.ipv4.tcp_syn_retries = 1
net.ipv4.tcp_tw_recycle = 1
net.ipv4.tcp_tw_reuse = 1
net.ipv4.tcp_mem = 94500000 915000000 927000000
net.ipv4.tcp_fin_timeout = 1
net.ipv4.tcp_keepalive_time = 1200
net.ipv4.ip_local_port_range = 1024 65535
vm.swappiness=10
EOF

sysctl -p


#create ambari.repo file
log "---------------create ambari.repo file"
sed  -i 's/localhost/'$YUM_NODE'/g' ./ambari.repo
#rm -rf /etc/yum.repos.d/ambari.repo
#cp ambari.repo /etc/yum.repos.d/



#update /etc/ambari-agent/conf/ambari-agent.ini and set:
#hostname=$MASTERIP
#run_as_user=$LINUX_ADMIN_USER

#begin

#log "---------------update /etc/ambari-agent/conf/ambari-agent.ini"
#sed  -i 's/hostname=localhost/'hostname=$MASTERIP'/g' /etc/ambari-agent/conf/ambari-agent.ini
#sed  -i 's/run_as_user=root/'run_as_user=$LINUX_ADMIN_USER'/g' /etc/ambari-agent/conf/ambari-agent.ini
#
#
#if [ "$NODETYPE" == "masternode" -a "$COPYINDEX" == "0" ]
#then
#  #init ambari server
#  log "---------------init-ambari-server.sh"
#  log "---------------$NODETYPE"
#  log "---------------$COPYINDEX"
#  bash ./init-ambari-server.sh $AMBARI_MYSQL_ROOT_PASSWORD $LINUX_ADMIN_USER >> ${LOG_FILE} 2>&1
#  #start ambari-agent
#  su - $LINUX_ADMIN_USER -c "ambari-agent restart"
#elif [ "$NODETYPE" != "yumreponode" ]
#then
#  #start ambari-agent
#  su - $LINUX_ADMIN_USER -c "ambari-agent restart"
#fi


#yum clean all


#end

#log "------- initialize-node.sh succeeded -------"

# always `exit 0` on success
exit 0