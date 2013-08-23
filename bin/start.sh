#!/bin/sh

BASEDIR=$(dirname $0)

# wenn eine lokale javaversion vorhanden ist, soll diese bevorzugt verwendet werden

if [ -f $BASEDIR/../../../java/latest/bin/java ];
	then $BASEDIR/../../../java/latest/bin/java -Djava.ext.dirs=$BASEDIR/../lib/ -jar $BASEDIR/*.jar
	else java -Djava.ext.dirs=$BASEDIR/../lib/ -jar $BASEDIR/*.jar
fi
exit 0
