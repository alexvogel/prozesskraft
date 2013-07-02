#!/usr/bin/perl

use strict;

my ($filename, $directories, $suffix);
BEGIN
{
    use File::Basename;
    ($filename, $directories, $suffix) = fileparse ($0);
}

my $basedir = $directories . "../../../../bin/";

my $prog = "gui";

if (@ARGV)
{
	if (($ARGV[0] =~ m/-help/) || ($ARGV[0] =~ m/-h/))
	{
		print "you may use one of these subcommands:\n";
		print " init     to initialize a database\n";
		print " list     to list contents of database\n";
		print " checkin  to checkin a process\n";
		print " checkout to checkout a process\n";
		print " server   to start the server\n";
		print " cleandb  to clean the database\n";
		print " progress to set new progress state\n";
		print " gui      for a gui\n";
		exit(0);
	}
	elsif ($ARGV[0] =~ m/^[^-]/)
	{
		$prog = shift;
	}
}
	
if ( grep { $_ eq $prog } ("list", "checkin", "checkout", "init", "server", "cleandb", "progress", "gui") )
{
	print "basedir: " . $basedir . "\n";
	print $basedir . "pradar-$prog @ARGV\n";
	system $basedir . "pradar-$prog @ARGV";
}
else
{
	print "unknown subcommand $prog. try --help for help.\n";
	exit(0);
}

