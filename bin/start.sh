#!/bin/sh

BASEDIR=$(dirname $0)

# wenn eine lokale javaversion vorhanden ist, soll diese bevorzugt verwendet werden

if [ -f $BASEDIR/../../../java/latest/bin/java ];
	then
		echo using local java-installation
		AUSGABE=$(($BASEDIR/../../../java/latest/bin/java -d32) 2>&1)
		ERRORSTRING=$(echo $AUSGABE | grep -P '^Error' -o)
		if [ "$ERRORSTRING" == "Error" ];
			then
				echo its a 64-Bit java
				$BASEDIR/../../../java/latest/bin/java -Djava.ext.dirs=$BASEDIR/../lib/:$BASEDIR/../lib64/ -jar $BASEDIR/*.jar
			else
				echo its a 32-Bit java
				$BASEDIR/../../../java/latest/bin/java -Djava.ext.dirs=$BASEDIR/../lib/:$BASEDIR/../lib32/ -jar $BASEDIR/*.jar
		fi
	else
		echo using central java-installation
		AUSGABE=$((java -d32) 2>&1)
		ERRORSTRING=$(echo $AUSGABE | grep -P '^Error' -o)
		if [ "$ERRORSTRING" == "Error" ];
			then
				echo its a 64-Bit java
				java -Djava.ext.dirs=$BASEDIR/../lib/:$BASEDIR/../lib64/ -jar $BASEDIR/*.jar
			else
				echo its a 32-Bit java
				java -Djava.ext.dirs=$BASEDIR/../lib/:$BASEDIR/../lib32/ -jar $BASEDIR/*.jar
		fi
		
fi
