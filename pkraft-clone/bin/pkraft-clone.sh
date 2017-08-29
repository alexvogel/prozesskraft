#!/bin/sh

MAINCLASS="de.prozesskraft.pkraft.Clone"
BASEDIR=$(dirname $0)

# wenn eine eigene javainstallation vorhanden ist, soll diese verwendet werden

if [ -f $BASEDIR/../java/latest/bin/java ];
	then
		
		AUSGABE=$(($BASEDIR/../java/latest/bin/java -d32) 2>&1)
		ERRORSTRING=$(echo $AUSGABE | grep -P '^Error' -o)
		if [ "$ERRORSTRING" == "Error" ];
			then
				echo using the own - very local - java \(64-bit\)
				$BASEDIR/../java/latest/bin/java -cp "$BASEDIR/../lib/*:$BASEDIR/../lib64/*:$BASEDIR/*" $MAINCLASS "$@"
			else
				echo using the own - very local - java \(32-bit\)
				$BASEDIR/../java/latest/bin/java -cp "$BASEDIR/../lib/*:$BASEDIR/../lib32/*:$BASEDIR/*" $MAINCLASS "$@"
		fi

# ansonsten wenn eine lokale javainstallation vorhanden ist, soll diese verwendet werden
elif [ -f $BASEDIR/../../../java/latest/bin/java ];
	then
		
		AUSGABE=$(($BASEDIR/../../../java/latest/bin/java -d32) 2>&1)
		ERRORSTRING=$(echo $AUSGABE | grep -P '^Error' -o)
		if [ "$ERRORSTRING" == "Error" ];
			then
				echo using local java \(64-bit\)
				$BASEDIR/../../../java/latest/bin/java -cp "$BASEDIR/../lib/*:$BASEDIR/../lib64/*:$BASEDIR/*" $MAINCLASS "$@"
			else
				echo using local java \(32-bit\)
				$BASEDIR/../../../java/latest/bin/java -cp "$BASEDIR/../lib/*:$BASEDIR/../lib32/*:$BASEDIR/*" $MAINCLASS "$@"
		fi

# wenn keine lokalen javainstallationen vorhanden sind, soll die zentral installierte verwendet werden
	else
		
		AUSGABE=$((java -d32) 2>&1)
		ERRORSTRING=$(echo $AUSGABE | grep -P '^Error' -o)
		if [ "$ERRORSTRING" == "Error" ];
			then
				echo using central java \(64-bit\)
				java -cp "$BASEDIR/../lib/*:$BASEDIR/../lib64/*:$BASEDIR/*" $MAINCLASS "$@"
			else
				echo using central java \(32-bit\)
				java -cp "$BASEDIR/../lib/*:$BASEDIR/../lib32/*:$BASEDIR/*" $MAINCLASS "$@"
		fi
		
fi
