#!/usr/bin/perl
use strict;
use warnings;
use Cwd;

# buffering off
$|=1;

# maven ausfuehren
print "**\n";
print "maven ausfuehren\n";
print "mvn install\n";
system "mvn install";
