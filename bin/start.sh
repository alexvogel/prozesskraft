#!/bin/sh

BASEDIR=$(dirname $0)
#java -Djava.ext.dirs=$BASEDIR/../lib/:$BASEDIR/../lib64/ -jar $BASEDIR/*.jar
java -Djava.ext.dirs=$BASEDIR/../lib/ -jar $BASEDIR/*.jar
