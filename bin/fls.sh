#!/bin/sh
# -----------------------------------------------------------------------------
#
#	DO NOT EDIT THIS SCRIPT
# MARKED LINES EDITED BY AVOGE
#
# -----------------------------------------------------------------------------

CLASS_PATH=FloatingLicenseServer.jar
CLASS=com.license4j.floatinglicenseserver.server.FloatingLicenseServerDaemon
PID=/tmp/fls.pid

BASEDIR=$(dirname $0)
JAVA=$BASEDIR/../java/latest

# AVOGE START
do_exec() {
#	if [ `getconf LONG_BIT` == "32" ]
#	then
#		./jsvc32 -server -home $JAVA -cp $CLASS_PATH -pidfile $PID $1 $CLASS
#	else
#		./jsvc64 -server -home $JAVA -cp $CLASS_PATH -pidfile $PID $1 $CLASS
#	fi
# es soll immer die 32 bit fls gestartet werden
# die bmw-maschine lpcagw13.muc liefert einen getconf = 64, aber hat nur die 32-Bit java version installiert...
./jsvc32 -server -home $JAVA -cp $CLASS_PATH -pidfile $PID $1 $CLASS
}
# AVOGE END

case "$1" in
	start)  
		echo -n "Starting Floating License Server....."
		do_exec 
		echo "started"
		exit 0
		;;
	stop)
		echo -n "Stopping Floating License Server....."
		do_exec "-stop"
		echo "stopped"
		exit 0
		;;
	restart)
		if [ -f "$PID" ]; then
			echo -n "Restarting Floating License Server....."
			do_exec "-stop"
			do_exec
			echo "restarted"
			exit 0
	        else
			echo "Floating License Server not running, will do nothing."  
			exit 1
		fi
		;;
	*)
		echo "usage: daemon {start|stop|restart}" >&2       
		exit 3
		;;
esac
