#!/usr/bin/perl

use strict;
use warnings;

my $driverversion = "0.1";
my $date = "Jan 07 2013";

my $query;

#------------
# appname feststellen
my ($filename, $directories, $suffix);
BEGIN
{
    use File::Basename;
    ($filename, $directories, $suffix) = fileparse ($0);
}
# falls $filename ein "-" enthaelt, kann der namensteil bis einschliesslich "-" entfallen
my $filename_short;
if ($filename =~ m/^\w+-(\.+)$/)
{
	$filename_short = $1;
}
#------------



#------------
# feststellen der verfuegbaren versionen
my $installdir = $directories."../install/$filename";
opendir INPDIR, $installdir;   # READ INPUT DIRECTORY FILE LIST
my @all_versions = readdir INPDIR;  # Read file list
@all_versions = grep { !/^\.$/ && !/^\.\.$/ } @all_versions;
#@all_versions = grep { !/^\.$/ && !/^\.\.$/ && !/^master$/ } @all_versions;
#print "all versions are: @all_versions\n";
closedir INPDIR;  #Close directory
#------------

#------------

#------------
# Produktivversion
my $version;
my @versions;

@versions = sort (grep { !/[abcdefghijklmnopqrstuvwxyz]/i } @all_versions);
#print "versions are: @versions\n";
my $default = $versions[-1];

@versions = sort (grep { /[abcdefghijklmnopqrstuvwxyz]/i } @all_versions);
#print "versions are: @versions\n";

my $newest = ($versions[-1] || $default);

my @neue_argumente;
#------------
#------------
## installierte Versionen
#my %versionen = (
##                 "0.1" => "/share/ams/marc/bmw_user/09_tools/install/beulen/0.1/bin/beulen.pl",
##                 "0.2" => "/share/ams/marc/bmw_user/09_tools/install/beulen/0.2/bin/beulen.pl",
##                 "0.3" => "/share/ams/marc/bmw_user/09_tools/install/beulen/0.3/bin/beulen.pl",
##                 "0.4" => "/share/ams/marc/bmw_user/09_tools/install/beulen/0.4/bin/beulen.pl",
##                 "0.4.1" => "/share/ams/marc/bmw_user/09_tools/install/beulen/0.4.1/bin/beulen.pl",
##                 "0.5" => "/share/ams/marc/bmw_user/09_tools/install/beulen/0.5/bin/beulen.pl",
#                 "0.6" => "/share/ams/marc/bmw_user/09_tools/install/beulen/0.6/bin/beulen.pl",
#                 "0.7" => "/share/ams/marc/bmw_user/09_tools/install/beulen/0.7/bin/beulen.pl",
#                 "0.7.1" => "/share/ams/marc/bmw_user/09_tools/install/beulen/0.7.1/bin/beulen.pl",
#                 "0.7.2" => "/share/ams/marc/bmw_user/09_tools/install/beulen/0.7.2/bin/beulen.pl",
#                 "0.7.3" => "/share/ams/marc/bmw_user/09_tools/install/beulen/0.7.3/bin/beulen.pl",
#		 );
##------------

#------------
# Den Aufruf durchsuchen nach '--query'
if ( grep { $_ eq "--query" } @ARGV ) { $query = 1; }
#------------

#------------
# Den Aufruf durchsuchen nach '--version'. version feststellen und aus den argumenten entfernen
elsif ( grep { $_ =~ /--version/ } @ARGV )
{
	my $last_arg_was_plain_version;
	for(my $x=0; $x<@ARGV; $x++)
	{
		if ($ARGV[$x] =~ m/--version/)
		{
			if ($ARGV[$x] =~ m/^--version$/)
			{
				$last_arg_was_plain_version = 1;
			}
			elsif ($ARGV[$x] =~ m/^--version=(.+)$/)
			{
				$version = $1;
			}
		}
		elsif ($last_arg_was_plain_version)
		{
			$last_arg_was_plain_version = 0;
			$version = $ARGV[$x];
		}
		else
		{
			push(@neue_argumente, $ARGV[$x]);
		}
	}
	if ($version eq "default")
	{
		$version = $default;
	}
	
	elsif ($version eq "newest")
	{
		if ($newest)
		{
			$version = $newest;
		}
		else
		{
			$version = $default;
		}
	}
	
}
#------------

#------------
# wenn @neue_argumente leer ist, dann die @ARGV kopieren
else
{
	@neue_argumente = @ARGV;
}
#------------

			



if ($query)
{
    my $eintraege_pro_zeile = 4;
    my $volle_zeilen = int(scalar(@all_versions) / $eintraege_pro_zeile);
    my $eintraege_letzte_zeile = scalar(@all_versions) % $eintraege_pro_zeile;

    print "------------------------------\n";
    print "available versions of $filename are:\n";

# ausgabe der voll befuellten zeilen
    for(my $x = 0; $x < $volle_zeilen; $x++)
    {
        for(my $y = 0; $y < $eintraege_pro_zeile; $y++)
        {
	    printf "%-10s", $all_versions[($x*$eintraege_pro_zeile)+$y];
        }
        print "\n";
    }

# ausgabe der teilbefuellten zeilen
    if($eintraege_letzte_zeile)
    {
        for(my $y=0; $y<$eintraege_letzte_zeile; $y++)
        {
            printf "%-10s", $all_versions[($volle_zeilen*$eintraege_pro_zeile)+$y];
        }
        print "\n";
    }
    if ($default) {print "default is $default\n";}
    if ($newest) {print "newest is  $newest\n";}
    print "------------------------------\n";
    exit;
}

unless ($version)
{
    if ($default) {$version = $default;}
    elsif ($newest) {$version = $newest;}
    else {print "no version of $filename installed.\n";exit(1);}
}

unless (grep { $_ eq $version } @all_versions)
{
    print "unknown version $version\n";
    print "call '$filename --query' for available versions\n";
    exit(1);
}

# liste der dateien feststellen, die in dem verzeichnis liegen
my @callpossibilities = glob("$installdir/$version/bin/*");
my @caller;
#print "$versionen{$version} @neue_argumente\n";
if (@caller = grep {$_ =~ m/$installdir\/$version\/bin\/$filename$/ } @callpossibilities)
{
	if (@caller == 1)
	{
		print "$installdir/$version/bin/$filename @neue_argumente\n";
		exec "$installdir/$version/bin/$filename @neue_argumente";
	}
	else
	{
		print "don't know what to call - @caller\n";
		exit(1);
	}
}

# evtl hat das einstiegsprogramm eine endung ".pl"
elsif (@caller = grep {$_ =~ m/$installdir\/$version\/bin\/$filename\.pl$/ } @callpossibilities)
{
#	# ist das file ein binary file? dann handelt es sich vermutlich um ein PAR-Archiv
#	if (-B "$installdir/$version/bin/$filename.pl")
#	{
#		print "checking for perl-module 'PAR'\n";
#		if (!(eval{require PAR;}))
#		{
#			print "to use this program you need to install perl-module 'PAR' on your machine first. bye.\n";
#			exit(1);
#		}
#	}
	if (@caller == 1)
	{
		print "$caller[0] @neue_argumente\n";
		exec "$caller[0] @neue_argumente";
	}
	else
	{
		print "don't know what to call - @caller\n";
		exit(1);
	}
}

# evtl hat das einstiegsprogramm im namen eine versionsbezeichnung und endet auf .jar
elsif (@caller = grep {$_ =~ m/$installdir\/$version\/bin\/$filename-\d\.\d*\.*\d*\.jar$/ } @callpossibilities)
{
	if (@caller == 1)
	{
		print "java -jar $caller[0] @neue_argumente\n";
		exec "java -jar $caller[0] @neue_argumente";
	}
	else
	{
		print "don't know what to call - @caller\n";
		exit(1);
	}
}

# falls filename ein "-" enthaelt kann der teil vor dem "-" fuer die suche nach einem aufrufer entfernt werden
elsif ( $filename_short && (@caller = grep {$_ =~ m/$installdir\/$version\/bin\/$filename_short-\d\.\d*\.*\d*\.jar$/ } @callpossibilities) )
{
	if (@caller == 1)
	{
		print "java -jar $caller[0] @neue_argumente\n";
		exec "java -jar $caller[0] @neue_argumente";
	}
	else
	{
		print "don't know what to call - @caller\n";
		exit(1);
	}
}
