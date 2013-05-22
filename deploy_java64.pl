#!/usr/bin/perl
use strict;
use warnings;
use Cwd;

# buffering off
$|=1;

my $cwd = getcwd;


# oeffnen des startscripts und setzen des aufrufs fuer java64 oder java32
my $filename_server = "bin/license4j-server.sh";
my $path_server_start = $cwd."/".$filename_server;
open (SERVER, '<', $path_server_start) or die "Can't read $path_server_start: $!";

while(<SERVER>)
{
	$_ =~ s/<ersetze_mich_durch_java_home>/\/usr\/java\/jdk1.7.0_07/;
}
close SERVER;

# oeffnen des startscripts fuer administration und setzen des aufrufs fuer java64 oder java32
my $filename_server_admin = "bin/license4j-server-admin.sh";
my $path_server_admin = $cwd."/".$filename_server_admin;
open (SERVERADMIN, '<', $path_server_admin) or die "Can't read $path_server_admin: $!";

while(<SERVERADMIN>)
{
	$_ =~ s/<ersetze_mich_durch_java_home>/\/usr\/java\/jdk1.7.0_07/;
}
close SERVERADMIN;

# oeffnen des startscripts und setzen des aufrufs fuer java64 oder java32
my $filename_fls = "bin/fls.sh";
my $path_fls = $cwd."/".$filename_fls;
open (AUFRUFSCRIPT, '<', $path_server_start) or die "Can't read $path_server_start: $!";

while(<AUFRUFSCRIPT>)
{
	$_ =~ s/<ersetze_mich_durch_aufruf>/\.\/jsvc64 -server -home \$JAVA -cp \$CLASS_PATH -pidfile \$PID $1 \$CLASS/;
}
close AUFRUFSCRIPT;
