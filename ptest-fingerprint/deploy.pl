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

# erstellen eines auslieferungsverzeichnisses und einkopieren des fertigen jars mit allem
my $cwd = getcwd;
my $target = $cwd . "/target";
my $auslieferungsverzeichnis = $cwd . "/fertig_zur_installation";

# kopieren des fertig zusammengestellten jars vom target ins auslieferungsverzeichnis
print "mkdir $auslieferungsverzeichnis\n";
system "mkdir $auslieferungsverzeichnis";
print "cp -r $cwd/bin $auslieferungsverzeichnis/.\n";
system "cp -r $cwd/bin $auslieferungsverzeichnis/.";
print "cp -r $target/*[0123456789].jar $auslieferungsverzeichnis/bin/.\n";
system "cp -r $target/*[0123456789].jar $auslieferungsverzeichnis/bin/.";

# kopieren von resourcen ausserhalb des jars ins auslieferungsverzeichnis
system "cp -r $cwd/etc $auslieferungsverzeichnis/.";

# kopieren der allgemeinen lib ins auslieferungsverzeichnis
print "cp -r $target/lib $auslieferungsverzeichnis/.\n";
system "cp -r $target/lib $auslieferungsverzeichnis/.";

# kopieren der perllib ins auslieferungsverzeichnis
print "cp -r $cwd/perllib $auslieferungsverzeichnis/.\n";
system "cp -r $cwd/perllib $auslieferungsverzeichnis/.";

# nachfolgern bescheid geben wie das auslieferungsverzeichnis heisst
print "<newdir>$auslieferungsverzeichnis<newdir>\n";
