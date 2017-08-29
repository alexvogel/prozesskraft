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
#JAVA=/usr/java/jdk1.7.0_07
#JAVA=<ersetze_mich_durch_java_home>
BASEDIR=$(dirname $0)
JAVAHOME=$BASEDIR/../java/latest
#
# !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

PRG="$0"
PRGDIR=`dirname "$PRG"`

cd $PRGDIR
. ./flsgui.sh
