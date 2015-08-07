#!/bin/sh
# -----------------------------------------------------------------------------
#
# Startup script for License4J Floating License Server Management GUI
#
# -----------------------------------------------------------------------------

# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
# 
# SET YOUR JAVA PATH HERE
#
#JAVA=/usr/local/jdk1.7.0_21
BASEDIR=$(dirname $0)
JAVA=$BASEDIR/../java/latest/bin/java
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

PRG="$0"
PRGDIR=`dirname "$PRG"`

cd $PRGDIR
. ./flsgui.sh
