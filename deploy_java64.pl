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
my $path_tmp_server_start = $cwd."/".$filename_server."tmp";
open (SERVER, '<', $path_server_start) or die "Can't read $path_server_start: $!";
open (SERVER_OUT, '>', $path_tmp_server_start) or die "Can't write $path_tmp_server_start: $!";

while(<SERVER>)
{
	$_ =~ s/<ersetze_mich_durch_java_home>/\/usr\/java\/jdk1.7.0_07/;
	print SERVER_OUT $_;
}
close SERVER;
close SERVER_OUT;
system "mv $path_tmp_server_start $path_server_start";

# oeffnen des startscripts fuer administration und setzen des aufrufs fuer java64 oder java32
my $filename_server_admin = "bin/license4j-server-admin.sh";
my $path_server_admin = $cwd."/".$filename_server_admin;
my $path_tmp_server_admin = $cwd."/".$filename_server_admin."tmp";
open (SERVERADMIN, '<', $path_server_admin) or die "Can't read $path_server_admin: $!";
open (SERVERADMIN_OUT, '>', $path_tmp_server_admin) or die "Can't write $path_tmp_server_admin: $!";

while(<SERVERADMIN>)
{
	$_ =~ s/<ersetze_mich_durch_java_home>/\/usr\/java\/jdk1.7.0_07/;
	print SERVERADMIN_OUT $_;
}
close SERVERADMIN;
close SERVERADMIN_OUT;
system "mv $path_tmp_server_admin $path_server_admin";

# oeffnen des startscripts und setzen des aufrufs fuer java64 oder java32
my $filename_fls = "bin/fls.sh";
my $path_fls = $cwd."/".$filename_fls;
my $path_tmp_fls = $cwd."/".$filename_fls."tmp";
open (AUFRUFSCRIPT, '<', $path_fls) or die "Can't read $path_fls: $!";
open (AUFRUFSCRIPT_OUT, '>', $path_tmp_fls) or die "Can't write $path_tmp_fls: $!";

while(<AUFRUFSCRIPT>)
{
	$_ =~ s/<ersetze_mich_durch_aufruf>/\.\/jsvc64 -server -home \$JAVA -cp \$CLASS_PATH -pidfile \$PID $1 \$CLASS/;
	$_ =~ s/<ersetze_mich_durch_home>/\/home\/avo/;
	print AUFRUFSCRIPT_OUT $_;
}
close AUFRUFSCRIPT;
close AUFRUFSCRIPT_OUT;
system "mv $path_tmp_fls $path_fls";
