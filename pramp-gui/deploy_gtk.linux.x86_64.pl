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
print "mvn install -P gtk.linux.x86_64\n";
system "mvn install -P gtk.linux.x86_64";

# erstellen eines auslieferungsverzeichnisses und einkopieren des fertigen jars mit allem
my $cwd = getcwd;
my $target = $cwd . "/target";
my $auslieferungsverzeichnis = $cwd . "/fertig_zur_installation";

# kopieren des fertig zusammengestellten jars vom target ins auslieferungsverzeichnis
print "mkdir $auslieferungsverzeichnis\n";
system "mkdir $auslieferungsverzeichnis";
print "cp -r $cwd/bin $auslieferungsverzeichnis/.\n";
system "cp -r $cwd/bin $auslieferungsverzeichnis/.";
print "cp -r $cwd/lib32 $auslieferungsverzeichnis/.\n";
system "cp -r $cwd/lib32 $auslieferungsverzeichnis/.";
print "cp -r $cwd/lib64 $auslieferungsverzeichnis/.\n";
system "cp -r $cwd/lib64 $auslieferungsverzeichnis/.";
print "cp -r $target/*[0123456789].jar $auslieferungsverzeichnis/bin/.\n";
system "cp -r $target/*[0123456789].jar $auslieferungsverzeichnis/bin/.";


# kopieren von resourcen ausserhalb des jars ins auslieferungsverzeichnis
print "cp -r $cwd/etc $auslieferungsverzeichnis/.\n";
system "cp -r $cwd/etc $auslieferungsverzeichnis/.";

# loeschen von 32-Bit/64-Bit Jars aus dem allgemeinen lib-Verzeichnis
print "rm -rf $target/lib/gtk.linux.x86*\n";
system "rm -rf $target/lib/gtk.linux.x86*";

# kopieren der allgemeinen lib ins auslieferungsverzeichnis
print "cp -r $target/lib $auslieferungsverzeichnis/.\n";
system "cp -r $target/lib $auslieferungsverzeichnis/.";

# nachfolgern bescheid geben wie das auslieferungsverzeichnis heisst
print "<newdir>$auslieferungsverzeichnis<newdir>\n";
