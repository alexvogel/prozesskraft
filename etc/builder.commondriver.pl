#!/usr/bin/perl

use strict;
use warnings;
use Getopt::Long;
Getopt::Long::Configure("pass_through");

my $driverversion = "0.3.0";
my $date = "Sep 3 2014";

#------------
# appname feststellen
my ($filename, $directories, $suffix);
BEGIN
{
    use File::Basename;
    ($filename, $directories, $suffix) = fileparse ($0);
}
#------------

my $appname = $filename;
my $default;

#------------
# feststellen der verfuegbaren versionen
#my $installdir = $directories."../install/$filename";
my $installdir = $directories."../install/$appname";
opendir INPDIR, $installdir;   # READ INPUT DIRECTORY FILE LIST
my @all_versions = readdir INPDIR;  # Read file list
@all_versions = grep { !/^\.$/ && !/^\.\.$/ } @all_versions;
#@all_versions = grep { !/^\.$/ && !/^\.\.$/ && !/^master$/ } @all_versions;
#print "all versions are: @all_versions\n";
closedir INPDIR;  #Close directory
#------------

#------------
# feststellen der default version
my $defaultVersionFile = $directories."../install/version.$appname";
if(stat $defaultVersionFile)
{
	open (DEFAULT, '<', $defaultVersionFile) or die "Can't read $defaultVersionFile: $!";
	my @defaultVersionFile = <DEFAULT>;
	chomp @defaultVersionFile;
	close DEFAULT;
	
	# in der ersten zeile steht die version
	$default = shift @defaultVersionFile;
}
#------------


#------------
# Produktivversion
my @versions;

# alle numerischen versionsstring durchsuchen und sortieren.
@versions = sort (grep { !/[abcdefghijklmnopqrstuvwxyz]/i } @all_versions);

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

my $query;
my $version;
my $result = GetOptions(
#                        "scenesdir=s"=> \$scenesdir,
                        "query"	=> \$query,
                        "version=s"	=> \$version,
                        );
#------------

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
    if ($default)
    {
    	print "default is $default\n";
    }
    else
    {
    	print "no default defined\n";
    }
    print "------------------------------\n";
    exit;
}

unless ($version)
{
    if ($default) {$version = $default;}
    else
    {
    	print "no default version of command '$filename' defined\n";
    	exit(1);
    }
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

my @neue_argumente;
foreach my $arg (@ARGV)
{
	push(@neue_argumente, '"' . $arg . '"');
}

# wenn ein einstiegsprogramm gefunden wird, das genauso heisst wie dieses, soll das verwendet werden
if (@caller = grep {$_ =~ m/$installdir\/$version\/bin\/$filename$/ } @callpossibilities)
{
	if (@caller == 1)
	{
#		print "$installdir/$version/bin/$filename @neue_argumente\n";
		exec "$installdir/$version/bin/$filename @neue_argumente";
	}
	else
	{
		print "don't know what to call - @caller\n";
		exit(1);
	}
}

# wenn ein einstiegsprogramm gefunden wird, das 'start.sh'
elsif (@caller = grep {$_ =~ m/$installdir\/$version\/bin\/start\.sh$/ } @callpossibilities)
{
	if (@caller == 1)
	{
#		print "$caller[0] @neue_argumente\n";
		exec "$caller[0] @neue_argumente";
	}
	else
	{
		print "don't know what to call - @caller\n";
		exit(1);
	}
}

# evtl hat das einstiegsprogramm eine endung ".sh"
elsif (@caller = grep {$_ =~ m/$installdir\/$version\/bin\/$filename\.sh$/ } @callpossibilities)
{
	if (@caller == 1)
	{
#		print "$caller[0] @neue_argumente\n";
		exec "$caller[0] @neue_argumente";
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
#		print "$caller[0] @neue_argumente\n";
		exec "$caller[0] @neue_argumente";
	}
	else
	{
		print "don't know what to call - @caller\n";
		exit(1);
	}
}

# evtl hat das einstiegsprogramm eine endung ".py"
elsif (@caller = grep {$_ =~ m/$installdir\/$version\/bin\/$filename\.py$/ } @callpossibilities)
{
	if (@caller == 1)
	{
#		print "$caller[0] @neue_argumente\n";
		exec "$caller[0] @neue_argumente";
	}
	else
	{
		print "don't know what to call - @caller\n";
		exit(1);
	}
}
# evtl hat das einstiegsprogramm eine endung "-<irgendwas>.pl"
elsif (@caller = grep {$_ =~ m/$installdir\/$version\/bin\/$filename-\w+\.pl$/ } @callpossibilities)
{
	if (@caller == 1)
	{
#		print "$caller[0] @neue_argumente\n";
		exec "$caller[0] @neue_argumente";
	}
	else
	{
		print "don't know what to call - @caller\n";
		exit(1);
	}
}
# evtl hat das einstiegsprogramm im namen eine versionsbezeichnung und endet auf -jar-with-dependencies.jar
elsif (@caller = grep {$_ =~ m/$installdir\/$version\/bin\/$filename-\d\.\d*\.*\d*-jar-with-dependencies\.jar$/ } @callpossibilities)
{
	if (@caller == 1)
	{
#		print "java -jar $caller[0] @neue_argumente\n";
		exec "java -jar $caller[0] @neue_argumente";
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
#		print "java -jar $caller[0] @neue_argumente\n";
		exec "java -jar $caller[0] @neue_argumente";
	}
	else
	{
		print "don't know what to call - @caller\n";
		exit(1);
	}
}

# evtl hat das einstiegsprogramm im namen einen beliebigen string und danach eine versionsbezeichnung und endet auf -jar-with-dependencies.jar
elsif (@caller = grep {$_ =~ m/$installdir\/$version\/bin\/$filename-\w+-\d\.\d*\.*\d*\.jar$/ } @callpossibilities)
{
	if (@caller == 1)
	{
#		print "java -jar $caller[0] @neue_argumente\n";
		exec "java -jar $caller[0] @neue_argumente";
	}
	else
	{
		print "don't know what to call - @caller\n";
		exit(1);
	}
}

else
{
	print "error: no target found.";
	print "please check installation of $filename.\n";
	exit(2);
}
