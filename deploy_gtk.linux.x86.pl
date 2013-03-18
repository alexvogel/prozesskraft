#!/usr/bin/perl
use strict;
use warnings;
use Cwd;

# buffering off
$|=1;

# tmp-target-directory loeschen
print "loeschen des targetverzeichnisses\n";
print "rm -rf /tmp/pradar\n";
system "rm -rf /tmp/pradar";

# maven ausfuehren
print "**\n";
print "maven ausfuehren\n";
print "mvn install -P gtk.linux.x86\n";
system "mvn install -P gtk.linux.x86";

# nachfolgern bescheid geben wie das targetverzeichnis heisst
#system "echo 'java -jar manager-0.1.jar $@' > /tmp/pradar/bin/pradar-core";

# nachfolgern bescheid geben wie das targetverzeichnis heisst
my $cwd = getcwd;
print "<newdir>$cwd/target<newdir>\n";

