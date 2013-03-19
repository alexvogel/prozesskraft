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
print "mvn install\n";
system "mvn install";

# nachfolgern bescheid geben wie das targetverzeichnis heisst
#system "echo 'java -jar manager-0.1.jar $@' > /tmp/pradar/bin/pradar-core";

# nachfolgern bescheid geben wie das targetverzeichnis heisst
#system "echo 'java -jar manager-0.1.jar $@' > /tmp/pradar/bin/pradar-core";

# erstellen eines auslieferungsverzeichnisses und einkopieren des fertigen jars mit allem
my $cwd = getcwd;
my $target = $cwd . "/target";
my $auslieferungsverzeichnis = $target . "/fertig_zur_installation";

# kopieren des fertig zusammengestellten jars vom target ins auslieferungsverzeichnis
system "mkdir $auslieferungsverzeichnis";
system "mkdir $auslieferungsverzeichnis/bin";
system "mv $target/*jar-with-dependencies* $auslieferungsverzeichnis/bin";

# nachfolgern bescheid geben wie das targetverzeichnis heisst
print "<newdir>$auslieferungsverzeichnis<newdir>\n";
