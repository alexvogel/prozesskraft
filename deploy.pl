#!/usr/bin/perl
use strict;
use warnings;
use Cwd;

# feststellen des installationsortes des scripts
my ($filename, $directories, $suffix);
BEGIN
{
    use File::Basename;
    ($filename, $directories, $suffix) = fileparse ($0);
}

$directories = File::Spec->rel2abs($directories);

# einbinden der avoge module
# zuerst, falls aus installationsverzeichnis gesucht wird
use lib $directories . "../../../myperllib/master/lib";

# buffering off
$|=1;

# maven ausfuehren
print "**\n";
print "maven ausfuehren\n";
print "mvn install\n";
system "mvn install";

# docs erzeugen
system $directories . "src/main/resources/gen_xsd_documentation.sh";
