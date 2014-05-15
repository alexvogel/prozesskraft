#!/bin/bash
SCRIPT=$(readlink -f "$0")
SCRIPTPATH=$(dirname "$SCRIPT")
#echo $SCRIPTPATH

# translate xsd to html
xsltproc $SCRIPTPATH/translateXsdToHtml_xs3p.xsl $SCRIPTPATH/process.xsd > $SCRIPTPATH/../../../doc/process.xsd.xhtml

# translate xsd to dot
echo xsd2dot -o $SCRIPTPATH/../../../doc/output.dot file:$SCRIPTPATH/process.xsd
xsd2dot -o $SCRIPTPATH/../../../doc/output.dot file:$SCRIPTPATH/process.xsd

# translate dot to postscript 
echo dot -Tps2 $SCRIPTPATH/../../../doc/output.dot -o $SCRIPTPATH/../../../doc/output.ps
dot -Tps2 $SCRIPTPATH/../../../doc/output.dot -o $SCRIPTPATH/../../../doc/output.ps

# translate postscript to pdf
echo ps2pdf $SCRIPTPATH/../../../doc/output.ps $SCRIPTPATH/../../../doc/process.xsd.pdf
ps2pdf $SCRIPTPATH/../../../doc/output.ps $SCRIPTPATH/../../../doc/process.xsd.pdf

# delete tmp-files
echo rm -rf $SCRIPTPATH/../../../doc/output.ps
rm -rf $SCRIPTPATH/../../../doc/output.ps
echo rm -rf $SCRIPTPATH/../../../doc/output.dot
rm -rf $SCRIPTPATH/../../../doc/output.dot
