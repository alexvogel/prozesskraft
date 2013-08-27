#!/bin/sh

MAINCLASS="de.caegroup.pradar.parts.PradarPartUi3"
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
				$BASEDIR/../../../java/latest/bin/java -cp "$BASEDIR/../lib/*:$BASEDIR/../lib64/*:pradar-gui-0.4.0.jar" $MAINCLASS
			else
				echo its a 32-Bit java
				$BASEDIR/../../../java/latest/bin/java -cp "$BASEDIR/../lib/*:$BASEDIR/../lib32/*:pradar-gui-0.4.0.jar" $MAINCLASS
		fi
	else
		echo using central java-installation
		AUSGABE=$((java -d32) 2>&1)
		ERRORSTRING=$(echo $AUSGABE | grep -P '^Error' -o)
		if [ "$ERRORSTRING" == "Error" ];
			then
				echo its a 64-Bit java
				java -cp "$BASEDIR/../lib/*:$BASEDIR/../lib64/*:pradar-gui-0.4.0.jar" $MAINCLASS
			else
				echo its a 32-Bit java
				java -cp "$BASEDIR/../lib/*:$BASEDIR/../lib32/*:pradar-gui-0.4.0.jar" $MAINCLASS
		fi
		
fi
