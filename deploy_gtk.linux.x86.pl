#!/usr/bin/perl
use strict;
use warnings;
use Cwd;

# buffering off
$|=1;

# tmp-target-directory loeschen
print "loeschen des targetverzeichnisses\n";
print "rm -rf /tmp/pramp\n";
system "rm -rf /tmp/pramp";

# maven ausfuehren
print "**\n";
print "maven ausfuehren\n";
print "mvn install -P gtk.linux.x86\n";
system "mvn install -P gtk.linux.x86";

# erstellen eines auslieferungsverzeichnisses und einkopieren des fertigen jars mit allem
my $cwd = getcwd;
my $target = $cwd . "/target";
my $auslieferungsverzeichnis = $cwd . "/fertig_zur_installation";

# kopieren des fertig zusammengestellten jars vom target ins auslieferungsverzeichnis
print "mkdir $auslieferungsverzeichnis\n";
system "mkdir $auslieferungsverzeichnis";
print "mkdir $auslieferungsverzeichnis/bin\n";
system "mkdir $auslieferungsverzeichnis/bin";
print "cp -r $target/*jar-with-dependencies* $auslieferungsverzeichnis/bin/.\n";
system "cp -r $target/*jar-with-dependencies* $auslieferungsverzeichnis/bin/.";

# kopieren von resourcen ausserhalb des jars ins auslieferungsverzeichnis
system "cp -r $cwd/etc $auslieferungsverzeichnis/.";

# nachfolgern bescheid geben wie das auslieferungsverzeichnis heisst
print "<newdir>$auslieferungsverzeichnis<newdir>\n";
