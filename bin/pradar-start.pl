#!/usr/bin/perl

my $prog = "list";

if (@ARGV)
{
	if (($ARGV[0] =~ m/-help/) || ($ARGV[0] =~ m/-h/))
	{
		print "you may use one of these subcommands:\n";
		print " init     to initialize a database\n";
		print " list     to list contents of database\n";
		print " checkin  to checkin a process\n";
		print " checkout to checkout a process\n";
		exit(0);
	}
	elsif ($ARGV[0] =~ m/^[^-]/)
	{
		$prog = shift;
	}
}
	
if ( grep { $_ eq $prog } ("list", "checkin", "checkout", "init") )
{
#	print "pradar-$prog @ARGV\n";
	system "pradar-$prog @ARGV";
}
else
{
	print "unknown subcommand $prog. try --help for help.\n";
	exit(0);
}

