#!/bin/bash


EXECNAME=$0
LINUX_ADMIN_USER=$1
ADMINPASSWORD=$2
SSHPORT=$3
MASTER_NODE_VMNAME_PREFIX=$4
MASTER_NODE_COUNT=$5

DATANODE_NODE_VMNAME_PREFIX=$6
DATANODE_NODE_COUNT=$7

RUNNER_NODE_VMNAME_PREFIX=$8
RUNNER_NODE_COUNT=$9

WEB_NODE_VMNAME_PREFIX=${10}
WEB_NODE_COUNT=${11}

KAFKA_NODE_VMNAME_PREFIX=${12}
KAFKA_NODE_COUNT=${13}

STORM_NODE_VMNAME_PREFIX=${14}
STORM_NODE_COUNT=${15}

YUM_NODE_VMNAME_PREFIX=${16}
YUM_NODE_COUNT=${17}

MASTER_NODE_HOSTS=""
DATANODE_HOSTS=""
RUNNER_NODE_HOSTS=""
WEB_NODE_HOSTS=""
KAFKA_NODE_HOSTS=""
STORM_NODE_HOSTS=""
YUM_NODE_HOSTS=""


LOG_FILE="/var/log/bdos-azure-initialize.log"

# logs everything to the LOG_FILE
log() {
  echo "$(date) [${EXECNAME}]: $*" >> ${LOG_FILE}
}

log 'hello world ! , this is bootstrap-ambari.sh'


log "LINUX_ADMIN_USER : $LINUX_ADMIN_USER"
log "ADMINPASSWORD : $ADMINPASSWORD"
log "SSHPORT : $SSHPORT"
log "MASTER_NODE_VMNAME_PREFIX : $MASTER_NODE_VMNAME_PREFIX"
log "MASTER_NODE_COUNT : $MASTER_NODE_COUNT"
log "DATANODE_NODE_VMNAME_PREFIX : $DATANODE_NODE_VMNAME_PREFIX"
log "DATANODE_NODE_COUNT : $DATANODE_NODE_COUNT"
log "RUNNER_NODE_VMNAME_PREFIX : $RUNNER_NODE_VMNAME_PREFIX"
log "RUNNER_NODE_COUNT : $RUNNER_NODE_COUNT"
log "WEB_NODE_VMNAME_PREFIX : $WEB_NODE_VMNAME_PREFIX"
log "WEB_NODE_COUNT : $WEB_NODE_COUNT"
log "KAFKA_NODE_VMNAME_PREFIX : $KAFKA_NODE_VMNAME_PREFIX"
log "KAFKA_NODE_COUNT : $KAFKA_NODE_COUNT"
log "STORM_NODE_VMNAME_PREFIX : $STORM_NODE_VMNAME_PREFIX"
log "STORM_NODE_COUNT : $STORM_NODE_COUNT"
log "YUM_NODE_VMNAME_PREFIX : $YUM_NODE_VMNAME_PREFIX"
log "YUM_NODE_COUNT : $YUM_NODE_COUNT"






yum clean all
yum install coreutils* -y


#build master node hosts string
for i in `seq 3`
do
i=`expr $i - 1`
if [ $i -ne 0 ];then
  MASTER_NODE_HOSTS=${MASTER_NODE_HOSTS}","${MASTER_NODE_VMNAME_PREFIX}${i}
else
  MASTER_NODE_HOSTS=${MASTER_NODE_HOSTS}${MASTER_NODE_VMNAME_PREFIX}${i}
fi

done
log "master_hosts:$MASTER_NODE_HOSTS"

#build datanode node hosts string
for i in `seq $DATANODE_NODE_COUNT`
do
i=`expr $i - 1`
if [ $i -ne 0 ];then
  DATANODE_HOSTS=${DATANODE_HOSTS}","${DATANODE_NODE_VMNAME_PREFIX}${i}
else
  DATANODE_HOSTS=${DATANODE_HOSTS}${DATANODE_NODE_VMNAME_PREFIX}${i}
fi

done
log "datanode_hosts:$DATANODE_HOSTS"

#build web node hosts string
WEB_NODE_HOSTS=${WEB_NODE_VMNAME_PREFIX}"0"
log "webnode_hosts:$WEB_NODE_HOSTS"

#build yum node hosts string
YUM_NODE_HOSTS=${YUM_NODE_VMNAME_PREFIX}"0"
log "yumnode_hosts:$YUM_NODE_HOSTS"

#build runner node hosts string
for i in `seq $RUNNER_NODE_COUNT`
do
i=`expr $i - 1`
if [ $i -ne 0 ];then
  RUNNER_NODE_HOSTS=${RUNNER_NODE_HOSTS}","${RUNNER_NODE_VMNAME_PREFIX}${i}
else
  RUNNER_NODE_HOSTS=${RUNNER_NODE_HOSTS}${RUNNER_NODE_VMNAME_PREFIX}${i}
fi

done
log "runner_hosts:$RUNNER_NODE_HOSTS"


#build kafka node hosts string
if [ $KAFKA_NODE_COUNT != "0" ];then
  for i in `seq $KAFKA_NODE_COUNT`
  do
    i=`expr $i - 1`
    if [ $i -ne 0 ];then
      KAFKA_NODE_HOSTS=${KAFKA_NODE_HOSTS}","${KAFKA_NODE_VMNAME_PREFIX}${i}
    else
      KAFKA_NODE_HOSTS=${KAFKA_NODE_HOSTS}${KAFKA_NODE_VMNAME_PREFIX}${i}
    fi
    
  done
fi

log "kafka_hosts:$KAFKA_NODE_HOSTS"

#build storm node hosts string
if [ $STORM_NODE_COUNT != "0" ];then
  for i in `seq $STORM_NODE_COUNT`
  do
    i=`expr $i - 1`
    if [ $i -ne 0 ];then
      STORM_NODE_HOSTS=${STORM_NODE_HOSTS}","${STORM_NODE_VMNAME_PREFIX}${i}
    else
      STORM_NODE_HOSTS=${STORM_NODE_HOSTS}${STORM_NODE_VMNAME_PREFIX}${i}
    fi
    
  done
fi
log "storm_hosts:$STORM_NODE_HOSTS"


ALL_HOSTS=""

for HOSTS in $MASTER_NODE_HOSTS $DATANODE_HOSTS $RUNNER_NODE_HOSTS $WEB_NODE_HOSTS $KAFKA_NODE_HOSTS $STORM_NODE_HOSTS $YUM_NODE_HOSTS 
do
  if [ $HOSTS != "" ];then
    if [ $HOSTS != $MASTER_NODE_HOSTS ];then
      ALL_HOSTS=${ALL_HOSTS}","${HOSTS}
    else
      ALL_HOSTS=${ALL_HOSTS}${HOSTS}
    fi
  fi
done
log "all_hosts:$ALL_HOSTS"



log '-------------------------------begin to mianmi'
#mianmi.sh
cp mianmi.sh /home/$LINUX_ADMIN_USER/
chmod 755 /home/$LINUX_ADMIN_USER/mianmi.sh
chmod 777 $LOG_FILE
su - $LINUX_ADMIN_USER -c "sh /home/$LINUX_ADMIN_USER/mianmi.sh $ALL_HOSTS $LINUX_ADMIN_USER $ADMINPASSWORD $SSHPORT"

log '-------------------------------mianmi success '

# always `exit 0` on success
exit 0