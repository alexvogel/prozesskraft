#!/usr/bin/perl
use strict;
use warnings;

# buffering off
$|=1;

# tmp-target-directory loeschen
print "loeschen des targetverzeichnisses";
print "rm -rf /tmp/pradar\n";
system "rm -rf /tmp/pradar";

# maven ausfuehren
print "**\n";
print "maven ausfuehren";
print "mvn install\n";
system "mvn install";

# nachfolgern bescheid geben wie das targetverzeichnis heisst
print "<newdir>/tmp/pradar<newdir>\n"
