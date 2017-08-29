#!/bin/sh
# -----------------------------------------------------------------------------
#
# Startup script for License4J Floating License Server
#
# -----------------------------------------------------------------------------

# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# 
# SET YOUR JAVA PATH HERE
#
#JAVA=/usr/local/jdk1.7.0_21
#JAVA=/usr/java/jdk1.7.0_07
#JAVA=<ersetze_mich_durch_java_home>
JAVA=/usr/java/latest
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

PRG="$0"
PRGDIR=`dirname "$PRG"`

cd $PRGDIR
. ./fls.sh $1
