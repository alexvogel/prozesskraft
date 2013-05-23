#!/bin/sh
# -----------------------------------------------------------------------------
#
#	DO NOT EDIT THIS SCRIPT
#
# -----------------------------------------------------------------------------

CLASS_PATH=FloatingLicenseServer.jar
CLASS=com.license4j.floatinglicenseserver.server.FloatingLicenseServerDaemon
#PID=/var/run/fls.pid
PID=/var/tmp/fls.pid

do_exec() {
#	if [ `getconf LONG_BIT` == "32" ]
#	then
#		./jsvc32 -server -home $JAVA -cp $CLASS_PATH -pidfile $PID $1 $CLASS
#	else
#		./jsvc64 -server -home $JAVA -cp $CLASS_PATH -pidfile $PID $1 $CLASS
#	fi	
<ersetze_mich_durch_aufruf>
}

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
