#!/bin/sh
cd /var/lib/mysql
tar czf purseDatabase.tar.gz purseDatabase

MACHINES="nvo-vm1.ncsa.uiuc.edu nvo-vm2.ncsa.uiuc.edu"
TIME="`date +%Y`/`date +%m`/`date +%d`"
TARGET_DIR="~/purse/$TIME"

for MACHINE in $MACHINES; do
    echo "backing up to $MACHINE"
    ssh backup@$MACHINE "mkdir -p $TARGET_DIR"
    scp /var/lib/mysql/purseDatabase.tar.gz backup@$MACHINE:$TARGET_DIR/purseDatabase-`date +%Y-%m-%d-%H_%M`.tar.gz
done
