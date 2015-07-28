#!/usr/bin/perl
#!/opt/cb2/perl/bin/perl

my $version = "[% version %]";
my $date = "[% date %]";
my $procname = "builder";

#	2012-12-25	AV	0.1	erste version

my ($filename, $directories, $suffix);
BEGIN
{
    use File::Basename;
    ($filename, $directories, $suffix) = fileparse ($0);
}

use lib $directories . "../lib";
#use lib "/data/workspace_pm/simpack_nastran_kopplung/lib";
use warnings;
use strict;
use Template;
use Getopt::Long;
use File::Find;
use File::Basename;
use File::Path;
use File::Spec;
use Cwd;
use IO::Handle;

eval "use Template";
if ($@)
{
    print "the template toolkit (perl module 'Template.pm') could not be detected on present machine.\n";
    print "please install module before starting process 'beulen'.\n";
    exit(1);
}

$|=1;

my $installdir = $directories . "..";
my $bin = $installdir . "/bin";
my $etc = $installdir . "/etc";

#-------------------
# zeitpunkt des aufrufs feststellen
my ($sec, $min, $hour, $day, $month, $year) = (localtime)[0,1,2,3,4,5];
$month++;
$month = "0".$month if ($month<10);
$day = "0".$day if ($day<10);
$year = $year+1900;
my $datum = $year.$month.$day;
my $moment = $hour.$min.$sec;
#-------------------

my $v = $version;
$v =~ s/\.//g;

my $error;

my $stack = $ENV{'HOME'}."/.".$procname."/".$procname.".stack";
my $conf = $etc . "/$procname.conf";

#my $commondriver = "/home/".$ENV{'USER'}."/.".$procname."/".$procname.".commondriver";
my $help;
my $batch;
my $app;
my $repodir;
my $target;
my $targetdir;
my $redirecttargetdir;
my $targetmachine;
my $targetuser;
my $ybranches = "lamei";
my @branch;
my $cleanapp;
my $cleanbranch;
my $genstack;
my $pack;
my $config;
my $setdefault;
my $log = $ENV{'HOME'}."/.builder/builder.log";
my $result = GetOptions(
#                        "scenesdir=s"=> \$scenesdir,
                        "help|h"        => \$help,
                        "batch"        => \$batch,
                        "app=s"        => \$app,
                        "repodir=s"    => \$repodir,
                        "target=s"  => \$target,
                        "targetdir=s"  => \$targetdir,
                        "redirecttargetdir=s"  => \$redirecttargetdir,
                        "targetmachine=s"  => \$targetmachine,
                        "targetuser=s" => \$targetuser,
                        "ybranches=s"  => \$ybranches,
                        "branch=s"  => \@branch,
                        "stack=s"     => \$stack,
                        "cleanapp"   => \$cleanapp,
                        "cleanbranch"=> \$cleanbranch,
                        "pack"=> \$pack,
                        "config=s" => \$config,
                        "setdefault=s"=> \$setdefault,
                        "genstack"=> \$genstack,
                        "log=s"=> \$log,
                        );

#-------------------
# festlegen des beulen konfigurationsfiles
# diese parameter muessen im konfigurationsfile zu finden sein
# wenn das 'value' des schluessels zu einem absoluten pfad expanded wird ($installverzeichnis+relpath) und darunter ein file gefunden wird,
# expandierten pfad beibehalten, sonst wieder auf urspruenglichen wert setzen
my @conf_muss_inhalt = (
							"example_stack",
							"commondriver",
							"callFingerprint",
							);
# festlegen des beulen konfigurationsfiles
# die werte dieser konfigurationsparameter muessen als files auffindbar sein
# die files, die die grenzkurven und standardviews fuer das angegebene parttype enthalten werden an anderer stelle abgeprueft (konsistenzpruefung der aufrufparameter)
my @conf_muss_files =   (
							"example_stack",
							"commondriver",
							);
#-------------------

if (@ARGV)
{
    die "unknown parameter @ARGV. Call --help for further information\n";
}

#-------------------
# helptext definieren
my $helptext = "Call:\n";
$helptext .= "builder PARAMETER\n";
$helptext .= "\n";
$helptext .= "Parameter:\n";
$helptext .= " -h, --help           prints this text\n";
$helptext .= " --stack             [optional; default: ~/.builder/builder.stack] stack-file with all build-data for all known projects.\n";
$helptext .= " --genstack          [exclusiv] generates an example stack file in current working directory. name will be example_builder.stack.\n";
$helptext .= " --app                [optional, filters stack] processes only the lines from the stack-file where this app is mentioned.\n";
$helptext .= " --repodir=DIR        [optional, filters stack] processes only the lines from the stack-file where this repodir is mentioned.\n";
$helptext .= " --targetdir=DIR      [optional, filters stack] processes only the lines from the stack-file where this targetdir is mentioned\n";
$helptext .= " --redirecttargetdir=DIR [optional] sets the targetdir to this path despite of definition in stack-file\n";
$helptext .= " --targetmachine=STRING  [optional, filters stack] processes only the lines from the stack-file where this targetmachine is mentioned\n";
$helptext .= " --target=STRING      [optional, filters stack] processes only the lines from the stack-file where this target is mentioned\n";
$helptext .= " --targetuser=STRING  [optional, filters stack] processes only the lines from the stack-file where this targetuser is mentioned\n";
$helptext .= " --ybranches=INT      [optional, overrides stack] installs the youngest <INT> branches (plus 'master' and all '*beta' younger than the <INT> youngest branches).\n";
$helptext .= " --branch=PATTERN     [optional, overrides --ybranches] installs the matching branch.\n";
$helptext .= " --cleanbranch		[optional] prior to install all content in target directory of the processed branch of the processed app will be deleted. this value overrides the value in configfile\n";
$helptext .= " --cleanapp		[optional] prior to build all branches of the processed app will be deleted.\n";
$helptext .= " --pack			[optional] delivers the installation directory as a *.tar.gz file\n";
$helptext .= " --config		[optional] integrates an alternative configuration e.g. bmw\n";
$helptext .= " --batch              [optional] direct execution of all relevant buildinstances without possibility to abort.\n";
$helptext .= " --setdefault=VERSION   [optional] creates a file 'version.<appname> with the version string inside\n";
$helptext .= " --log                [optional, default: ~/.builder/builder.log] this logfile will be used.\n";
$helptext .= "\n";
$helptext .= "Example 1:\n";
$helptext .= " builder\n";
$helptext .= "\n";
$helptext .= "Example 2:\n";
$helptext .= " builder --app cud\n";
$helptext .= "\n";
$helptext .= "Example 3:\n";
$helptext .= " builder --app cud --targetmachine lpcagw13.muc\n";
$helptext .= "\n";
$helptext .= "author: alexander.vogel\@caegroup.de | version: $version | date: $date\n";
#-------------------

#-------------------
# help?
if ($help)
{
	print $helptext;
	exit(0);
}
#-------------------

#-------------------
# check ob --cleanapp und --cleanbranch gleichzeitig genutzt werden
if (($cleanapp) && ($cleanbranch))
{
	print "fatal: dont use --cleanapp and --cleanbranch at the same time\n";
	exit(1);
}
#-------------------

#-------------------
# ist conf-file vorhanden und lesbar?
unless (stat $conf)
{
	print "fatal: cannot read ".$conf.". please check installation.\n";
	exit(1);
}
#-------------------

#-------------------
# einlesen der beulen-variablen aus
my %CONF_ORG = &getvars($conf);
my %CONF;
#-------------------
#-------------------
# viele parameter im parameterfile enthalten pfade relativ zum installationsverzeichnis
# diese pfade sollen auf absolute pfade expandiert werden
print "info: expanding config parameter to absolute path.\n";
foreach my $param (sort keys %CONF_ORG)
{
	# gibts da ein file?
	if (stat $installdir."/".$CONF_ORG{$param})
	{
		$CONF{$param} = File::Spec->rel2abs($installdir."/".$CONF_ORG{$param});
#		print "info: parameter $param (value=" . $BEULEN_ORG{$param} .") expanding to (new_value=".$BEULEN{$param}.")\n";
	}
	else
	{
		$CONF{$param} = $CONF_ORG{$param};
	}
}
#-------------------
#-------------------
# check ob prozess-notwendige variablen definiert wurden
print "check whether all necessary variables are defined\n";
foreach my $key (sort @conf_muss_inhalt)
{
    if ( grep { $_ eq $key } keys %CONF )
    {
        print "INFO: variable exists: $key\n";
    }
    else
    {
        print "FATAL: variable does not exist: $key\n";
        $error++;
    }
}
if ( $error ) { die "ERROR found - process broken - exit!\n";}
#-------------------
#-------------------
# check ob die files, die ueber die variablen definiert wurden, existieren / lesbar sind
print "check whether all files referenced by variables are defined\n";
foreach my $param ( sort @conf_muss_files )
{
	print "INFO: checking file referenced by $param\n";
    if ( stat $CONF{$param} )
    {
    	print "INFO: file readable: $CONF{$param}\n";
    }
    else
    {
        print "FATAL: file not readable: $CONF{$param}\n";
        $error++;
    }
}
if ( $error ) { die "ERROR found - process broken - exit!\n";}
#-------------------

#-------------------
# genstack?
if ($genstack)
{
	print "copying file 'example_builder.stack' to current working directory.\n";
	system "cp $CONF{'example_stack'} .";
	exit(0);
}
#-------------------

#-------------------
# config einlesen und gleichzeitig filtern
my @CONFIG;
my $header = 1;
my @header;

if (!open (STACK, "<$stack")) {die "cannot read $stack: $!\n";}

while(<STACK>)
{
	$_ =~ s/\s*//g;						# alle leerzeichen entfernen
	if    ( $_ =~ m/^#/) {next}			# kommentare raus
	elsif ( $_ =~ m/^\s*$/) {next}		# leere zeilen raus
	elsif ($header)
	{
		@header = split(";", $_);
		$header = 0;
		next;
	}
	else
	{
		my %paramset;
		my @action;
		my @line = split(";", $_);
		my $soll_beruecksichtigt_werden = 1;
		for(my $x=0; $x<@line; $x++)
		{
			if ($header[$x] eq "action")
			{
				push ( @action, $line[$x] );
				$paramset{$header[$x]} = \@action;
			}
			else
			{
				$paramset{$header[$x]} = $line[$x];
			}
		}
		
		# wenn parameter --app angegeben wurde, sollen nur eintraege im configfile mit dieser app beruecksichtigt werden
		if ($app)
		{
			if ($paramset{'app'} !~ m/^$app$/)
			{
				$soll_beruecksichtigt_werden = 0;
			}
		}
		
		# wenn parameter --repodir angegeben wurde, sollen nur eintraege im configfile mit dieser repo beruecksichtigt werden
		if ($repodir)
		{
			if ($paramset{'repodir'} ne $repodir)
			{
				$soll_beruecksichtigt_werden = 0;
			}
		}
		
		# wenn parameter --targetdir angegeben wurde, sollen nur eintraege im configfile mit diesem target beruecksichtigt werden
		if ($targetdir)
		{
			if ($paramset{'targetdir'} ne $targetdir)
			{
				$soll_beruecksichtigt_werden = 0;
			}
		}
		
		# wenn parameter --targetmachine angegeben wurde, sollen nur zeilen im configfile mit diesem targetmachine beruecksichtigt werden
		if ($targetmachine)
		{
			if ($paramset{'targetmachine'} ne $targetmachine)
			{
				$soll_beruecksichtigt_werden = 0;
			}
		}
		
		# wenn parameter --target angegeben wurde, sollen nur zeilen im configfile mit diesem target beruecksichtigt werden
		if ($target)
		{
			if ($paramset{'target'} ne $target)
			{
				$soll_beruecksichtigt_werden = 0;
			}
		}
		
		# wenn parameter --targetuser angegeben wurde, sollen nur zeilen im configfile mit diesem targetuser beruecksichtigt werden
		if ($targetuser)
		{
			if ($paramset{'targetuser'} ne $targetuser)
			{
				$soll_beruecksichtigt_werden = 0;
			}
		}
		
		# wenn parameter --ybranches angegeben wurde, soll fuer alle zeilen im configfile dieser ybranches-Wert verwendet werden (statt den den im file) 
		if ($ybranches =~ m/^\d+$/)
		{
			$paramset{'ybranches'} = $ybranches;
		}
		
		# ausgabe, welche zeilen aus config im weiteren verlauf aktiviert/deaktiviert sind.
		if ($soll_beruecksichtigt_werden)
		{
			print "----\n";
			print "info: activate:";
			foreach my $key (grep {!/action/} sort keys %paramset) {print " " . $key . "=" . $paramset{$key}}
			foreach my $key (grep { /action/} sort keys %paramset) {print " " . $key . "=" . "@{$paramset{$key}}"}
			print "\n";
			push @CONFIG, \%paramset;
		}
#		else
#		{
#			print "info: deactivate:";
#			foreach my $key (grep {!/action/} sort keys %paramset) {print " " . $key . "=" . $paramset{$key}}
#			foreach my $key (grep { /action/} sort keys %paramset) {print " " . $key . "=" . "@{$paramset{$key}}"}
#			print "\n";
#		}
	}
}
#-------------------

#-------------------
# fragen ob fortgefahren werden soll
unless ($batch)
{
	print "do you want to process the active buildsets (y/n) ? ";
	my $antwort = <>;
	if (!($antwort =~ m/^y$/i)) {print "bye\n"; exit;}
	print "ok.\n";
}
#-------------------

#-------------------
# umleiten der ausgaben in das logfile
print "all logging goes to $log\n";

open (LOG, '>', $log) or die "Can't write $log: $!";
STDOUT->fdopen( \*LOG, 'w' ) or die $!;
STDERR->fdopen( \*LOG, 'w' ) or die $!;
#-------------------

#-------------------
# fuer jede stack-line
foreach my $refh_stackline (@CONFIG)
{
	my $error;
	my $now_repodir = $$refh_stackline{'repodir'};
	my $now_app = $$refh_stackline{'app'};
	my $now_targetdir = $$refh_stackline{'targetdir'};
	my $now_targetbulk = $now_targetdir . "/install";
	my $now_targetbulkapp = $now_targetbulk . "/" . $$refh_stackline{'app'};
	my $now_targetbin = $now_targetdir . "/bin";
	my $now_target = $$refh_stackline{'target'};
	my $now_targetuser = $$refh_stackline{'targetuser'};
	my $now_targetmachine = $$refh_stackline{'targetmachine'};

# wenn der parameter --redirecttargetdir angegeben wurde, soll dieser wert anstatt der angabe im stack-file verwendung finden
# und die werte fuer user und machine auf lokale werte umgebogen werden (es soll ja nur eine temporaere installation erfolgen)
	if($redirecttargetdir)
	{
		$now_targetdir = $redirecttargetdir;
		$now_targetbulk = $now_targetdir . "/install";
		$now_targetbulkapp = $now_targetbulk . "/" . $$refh_stackline{'app'};
		$now_targetuser = $ENV{'USER'};
		$now_targetmachine = `hostname`;
		chomp $now_targetmachine;
		$now_targetbin = "/dev/null";
	}
	
	my $now_ybranches = $$refh_stackline{'ybranches'};
	my $now_deployscript = $$refh_stackline{'deployscript'};
	my @now_action = @{$$refh_stackline{'action'}};

# bestimmte actionangaben veraendern die directorydefinition
	if ( grep { $_ eq "targetbulk_is_targetdir_no_commondriver" } @now_action )
	{
		print "targetbulk is set to targetdir\n";
		print "commondriver is suppressed\n";
		$now_targetbulk = $now_targetdir;
		$now_targetbulkapp = $now_targetdir . "/" . $$refh_stackline{'app'};
		$now_targetbin = "/dev/null";
	}

	print "info: ----- start processing next stackline -----\n";
	
	#-------------------
	# ausgabe der parameter aus conf und abgeleiterer params
	print "info: ----- start these parameters (from stackline and user) -----\n";
	print "info: app = $now_app\n";
	print "info: config = $config\n";
	print "info: repodir = $now_repodir\n";
	print "info: targetdir = $now_targetdir\n";
	print "info: targetbulk = $now_targetbulk\n";
	print "info: targetbulkapp = $now_targetbulkapp\n";
	print "info: targetbin = $now_targetbin\n";
	print "info: target = $now_target\n";
	print "info: targetuser = $now_targetuser\n";
	print "info: targetmachine = $now_targetmachine\n";
	print "info: ybranches = $now_ybranches\n";
	print "info: deployscript = $now_deployscript\n";
	if ($cleanapp) {	print "info: cleanapp = $cleanapp\n";}
	if ($cleanbranch) {	print "info: cleanbranch = $cleanbranch\n";}
	foreach my $now_action (@now_action) {print "info: action = $now_action\n";}
	#-------------------

	#-------------------
	# check ob repodir existiert
	if (-d $now_repodir)
	{
		print "info: repodir exists.\n";
	}
	else
	{
		print "error: repodir (".$now_repodir . ") does not exist.\n";
		$error++;
	}
	
	if ($error)
	{
		print "error: ----- 'cause of errors skipping stackline -----\n";
		next;
	}
	print "info: ----- end these parameters (from stackline and user) -----\n";
	#-------------------
	
	#-------------------
	# kopieren des repos nach $instancedir/<random>
	my $instancedir = "/tmp/".$procname."_" . $datum . "_" . $now_app . "_" . $$;
	my $random = int(rand(10000000));
	my $TMPDIR = $instancedir . "/" . $now_app."_". $random;
	
	print "info: creating temporary path ($TMPDIR).\n";
	mkpath($TMPDIR);
	
	print "info: clone git repositiory to $TMPDIR\n";
#	print "info: rsync -avz ".$now_repodir."/ $TMPDIR\n";
#	system "rsync -avz ".$now_repodir."/ $TMPDIR";

	print "info: git clone $now_repodir $TMPDIR\n";
	system "git clone $now_repodir $TMPDIR";
	
	#-------------------
	# ermitteln aller branches in repositiory
	my $dir = getcwd;
	print "info: noticing current working directory $dir\n";
	print "info: temporarily changing to directory with tmp-repo.\n";
	chdir $TMPDIR;
	print "info: cd $TMPDIR\n";

	print "info: calling: git branch -a\n";
	my @branches = `git branch -a`;
	print "@branches";
	chomp @branches;
	@branches = grep { /\s*remotes.+\/([^\/]+)$/ && !/HEAD/ } @branches;	# herausfiltern von kurznamen und HEAD (es sollen nur noch die langnamen wie 'remotes/origin/master' bleiben
	for(my $x=0; $x<@branches; $x++)
	{
		$branches[$x] =~ s/\s*remotes\/\w+\///;
	}
	print "refined: '".join(":", @branches)."'\n";

	# das targetverzeichnis erstellen
	print "info: creating directory target $now_targetdir\n";
	print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetdir\"\n"; 
	system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetdir\"";
	
	print "info: creating directory targetbin ".$now_targetbin."\n";
	print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbin\"\n"; 
	system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbin\"";
	
	print "info: creating directory targetbulk ".$now_targetbulk."\n";
	print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbulk\"\n"; 
	system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbulk\"";
	
	if ($cleanapp)
	{
		print "info: deleting directory targetbulkapp (if exists) ".$now_targetbulkapp."\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkapp\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkapp\"";
	}
	
	print "info: creating directory targetbulkapp ".$now_targetbulkapp."\n";
	print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbulkapp\"\n"; 
	system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbulkapp\"";

	#-------------------
	# sonderzeichen und whitespaces aus den branchnamen entfernen
	for(my $x=0; $x<@branches; $x++)
	{
		$branches[$x] =~ s/\*//g;
		$branches[$x] =~ s/\s//g;
		print "info: branch found '$branches[$x]'\n";
	}
	print "refined2: '".join(":", @branches)."'\n";

	my @branches_numericnames = grep { $_ =~ /^[\d\.]+$/ } @branches;	# branches, die eine zahl im namen tragen (0.1 oder 2.1.10.2)
	my @branches_numericnames_sort = reverse sort @branches_numericnames;	# numerisch absteigend sortieren
	my @branches_alphanames = grep { $_ =~ /^[\w]+$/ } @branches;	# branches, die eine zahl im namen tragen (0.1 oder 2.1.10.2)
	my @branches_alphanames_sort = reverse sort @branches_alphanames;	# numerisch absteigend sortieren
	my @allbranches = (@branches_numericnames_sort);
	
	my @branches_wanted_by_name;
	foreach my $branch (@branch)
	{
		print "info: user wants branch '$branch' to be build.\n";
		if ( grep { $_ =~ /^$branch$/ } @branches ) # branches, die explizit angefragt wurden
		{
			print "info: user wanted branch found in all existent branches in git-repository\n";
			push (@branches_wanted_by_name, $branch);
		}
		else
		{
			print "warn: user wanted branch NOT found in all existent branches in git-repository\n";
		}
	}

	foreach my $branches_wanted_by_name (@branches_wanted_by_name)
	{
		my $branch_seen = 0;
		for(my $y=0; $y<$now_ybranches; $y++)
		{
			print "info: examining if user wanted branch is already scheduled by --ybranches.\n";
			if ( $allbranches[$y] =~ m/$branches_wanted_by_name/ )
			{
				$branch_seen = 1;
				print "info: yes - user wanted branch has been already scheduled - dismiss user request.\n";
			}
		}
		unless ($branch_seen)
		{
			unshift(@allbranches, $branches_wanted_by_name);
			print "info: scheduling user wanted branch '$branches_wanted_by_name' for building.\n";
			$now_ybranches++;
		}
	}

	# als ersten branch 'master' der liste hinzufuegen
#	unshift @branches_numericnames_sort, "master";

	#-------------------
	# gibt es eigentlich branches numerical/alphabetical?
	unless (@allbranches) {print "info: no branch found.\n";}
	for(my $x=0; $x < @allbranches; $x++)
	{
		print "info: ".($x+1).") in temporary git-repository branch with name found '$allbranches[$x]'\n";
	}
	print "info: only the first $now_ybranches branches will be build.\n";
	#-------------------

	#-------------------
	# fuer alle vorgemerkten branches durchfuehren
	if ($now_ybranches > scalar(@allbranches))
	{
		$now_ybranches = scalar(@allbranches);
	}
	
	for (my $x=0; $x<($now_ybranches); $x++)
	{
		print "info: will process branch '$allbranches[$x]'\n";
		my $actBranch = $allbranches[$x];

		# festlegen der zielverzeichnisse fuer programmdaten und den aufruftreiber
		my $now_targetbulkappbranch = $now_targetbulkapp . "/" . $allbranches[$x];
		print "info: target bulkappbranch directory: ".$now_targetbulkappbranch . "\n";

		print "info: calling: git checkout -f $allbranches[$x]\n"; 
		system "git checkout -f $allbranches[$x]";
		
		#-------------------
		# ermitteln des datums des letzten commits des gerade ausgecheckten branches
		print "info: determining the date of the last commit of that branch\n"; 
		print "info: calling: git log -n 1 | grep '^Date:'\n"; 
		my $rueck = `git log -n 1 | grep '^Date:'`;
		$rueck =~ m/^Date:\s+(\w+\s\w+\s\w+\s\d+:\d+:\d+\s\d+)\s.+$/;
		my $date_lastcommit = "unknown";
		$date_lastcommit = $1;
		print "info: determined date is: $date_lastcommit\n";
		#-------------------

		#-------------------
		# wird ein spezielles config gewuenscht, sollen diese files in die hauptdaten ueberspielt werden
		if($config)
		{
			print "a special is needed because of --config $config\n";
			if(-d $TMPDIR."/config/$config")
			{
				print "special config $config found in $TMPDIR/config/$config";
				print "copying the config files to main-data\n";
				print "scp -r $TMPDIR/config/$config/* $TMPDIR\n";
				system "scp -r $TMPDIR/config/$config/* $TMPDIR";
				print "special config finished";
			}
			else
			{
				print "warn: no special config $config found in $TMPDIR/config/$config";
				# kein exit bei nicht vorhandenem config, weil --config womoeglich fuer nachgelagerte builder aufrufe gedacht ist (action(merge))
#				exit(1);
			}
		}
		#-------------------

		#-------------------
		# --- START ACTION 'searchreplace' --- #
#		my $full_term;
#		if ( grep { /searchreplace/; /searchreplace\((.+)\)/ } @now_action )
		if ( grep { /searchreplace/ } @now_action )
		{
			my @filenames_from_parameter;
			# alle action-strings durchgehen und falls vorhanden die filenamen innerhalb der klammern feststellen
			foreach (@now_action)
			{
				if ($_ =~ m/searchreplace\((.+)\)/)
				{
					push(@filenames_from_parameter, split(",", $1));
				}
			}
			
			# default files zum ignorieren hier hinzufuegen
			push(@filenames_from_parameter, "Template.pm");
			
			# falls mit searchreplace ein parameter mit gegeben wurde, soll das festgestellt werden
			print "filenames_from_parameter_and_defaults bevor wanted aufgerufen wird: @filenames_from_parameter\n";
			#-------------------
			# suchen und ersetzen des platzhalters fuer 'version' in allen files
			print "info: action 'searchreplace'\n";
			print "info: search and replace placeholder [version] and [date] and [procname] and [installdir] and [home] in entry-point-file.\n";
			find( sub { wanted($now_app, @filenames_from_parameter) }, "$TMPDIR");
			
			sub wanted
			{
				my $now_app_inside_wanted = shift;
				my @filenames_from_parameter2 = @_;
				# wenn es ein directory ist, dann verwerfen
				print "filename in wanted: ".$File::Find::name."\n";
				if ( -d $File::Find::name )
				{
					print "skipping directory: ".$File::Find::name."\n";
					next;
				}
				#binaere files verwerfen
				if (!( -T $File::Find::name ))
				{
					print "skipping binary file: ".$File::Find::name."\n";
					next;
				}
				# tt-files verwerfen (diese gehoeren der applikation...) und pdf- und pptx auch
				if ( ($File::Find::name =~ m/\.tt/) || ($File::Find::name =~ m/\.pdf$/)  || ($File::Find::name =~ m/\.pptx$/) )
				{
					print "skipping template toolkit file: ".$File::Find::name."\n";
					next;
				}
				# sonstige files, die manchmal zu problemen fuehren
				if ( ($File::Find::name =~ m/Template.pm$/) )
				{
					print "skipping problematic file: ".$File::Find::name."\n";
					next;
				}
				# falls $now_app ein Namen wie z.b. "pradar-checkin" ist, soll bei searchreplace auch "checkin" als entrypoints beruecksichtigt werden.
				my $now_app_short;
				if ($now_app_inside_wanted =~ m/^\w+-(\w+)$/i) {$now_app_short = $1;}
#				print "info: $_\n";
				my $now_filename_without_path = $_;
#				print "now_filename_without_path: $now_filename_without_path\n";
				print "filenames_from_parameter_and_defaults2: @filenames_from_parameter2\n";
#				print "filename: $now_filename_without_path\n";
#				print "filename kurz vor 'if': ".$File::Find::name."\n";
#				print "kurzname kurz vor 'if': ".$_."\n";
#				print "now_app kurz vor 'if': ".$now_app_inside_wanted."\n";
#				print "now_app_short kurz vor 'if': ".$now_app_short."\n";
				if ( ( $_ =~ m/^$now_app_inside_wanted/i ) || ( $_ =~ m/^$now_app_inside_wanted\.\w+$/i ) || ($now_app_short && ( $_ =~ m/^$now_app_short\.\w+$/i )) || (@filenames_from_parameter && ( grep {$now_filename_without_path =~ /$_/i} @filenames_from_parameter )) )
				{
					print "info: processing file in search of tt placeholders: $File::Find::name\n";
					my $relname = File::Spec->abs2rel($File::Find::name);
					my $tt = Template->new();
					my $vars = {
								version	=> sub	{
													print "replacing placeholder for 'version' with '$allbranches[$x]'\n";
													return $allbranches[$x];
												},
								date	=> sub	{
													print "replacing placeholder for 'date' with '$date_lastcommit'\n";
													return $date_lastcommit;
												},
								procname	=> sub	{
													print "replacing placeholder for 'procname' with '$now_app'\n";
													return $now_app;
												},
								installdir => sub	{
													print "replacing placeholder for 'installdir' with '$now_targetbulkappbranch'\n";
													return $now_targetbulkappbranch;
												},
								home	=> sub	{
													print "replacing placeholder for 'home' with '".$ENV{'HOME'}."'\n";
													return $ENV{'HOME'};
												}
								};
 					$tt->process($relname, $vars, $relname) || die $tt->error();
#					$tt->process($relname, $vars, $relname) || print "error in subroutine $!"; next();
				}
			}
		}
		#-------------------
		# --- END ACTION 'searchreplace' --- #

		#-------------------
		# --- START ACTION 'merge(app,app,app,...)' --- #
		# diese apps sollen in die installation integriert werden
		# die angegebenen apps sollen jeweils in ein temporaeres verzeichnis installiert werden
		# aus den einzelinstallationen soll eine gesamte installtion (gemergte) gebaut werden
		if ( grep { /merge/ } @now_action )
		{
			print "info: action 'merge(...)' found in array (@now_action)\n";
			
			print "info: temporaeres umbenennen von bin\n";
			print "info: mv $TMPDIR/bin $TMPDIR/bin_temporaer_umbenannt\n";
			system("mv $TMPDIR/bin $TMPDIR/bin_temporaer_umbenannt");
			
			my %appsToMerge;
			
			#-------------------
			# alle action-strings durchgehen und bei merge alle app-names feststellen
			foreach my $now_action (@now_action)
			{
				if ($now_action =~ m/^merge\((.+)\)/)
				{
					my @tmp = split(",", $1);
					foreach (@tmp)
					{
						$appsToMerge{$_} = 1;
					}
				}
			}
			print "info: this apps will be merged with this installation: " . join(",", sort keys %appsToMerge) . "\n";
			
			#-------------------
			# jede der apps in ein temporaeres verzeichnis installieren und dabei diese parameter verwenden
			# --app=<appname> --branch=<gleichWieThis> --target=<gleichWieThis> --targetmachine=<gleichWieThis> --targetuser=<gleichWieThis> --redirecttargetdir --batch --log=targetdirectory<appname.log>
			foreach my $appToMerge (sort keys %appsToMerge)
			{
				
#				# das zielverzeichnis fuer die temporaere installation erstellen
#				print "info: creating dir\n";
#				print "mkdir $TMPDIR . /install\n"; 
#				mkdir $TMPDIR . "/install"; 
#				print "info: creating dir\n";
#				print "mkdir $TMPDIR . /install/$appToMerge\n"; 
#				mkdir $TMPDIR . "/install/$appToMerge";
#				print "info: creating dir\n";
#				print "mkdir $TMPDIR . /install/$appToMerge/$actBranch\n"; 
#				mkdir $TMPDIR . "/install/$appToMerge/$actBranch";
#				
				my @param;
				push(@param, "--app=".$appToMerge);
				push(@param, "--branch=".$actBranch);
				push(@param, "--target=".$now_target);
				push(@param, "--targetmachine=".$now_targetmachine);	# wird nur fuer das richtige filtern des stacks benoetigt
				push(@param, "--targetuser=".$now_targetuser);			# wird nur fuer das richtige filtern des stacks benoetigt
				push(@param, "--log=".$TMPDIR."/builder_".$appToMerge.".log");
				push(@param, "--redirecttargetdir=".$TMPDIR);
				push(@param, "--config=".$config);
				push(@param, "--batch");
				
				print "info: installing app $appToMerge (because of merge)\n";
				my $call = "builder --version=$version " . join(" ", @param);
				print "info: calling: " . $call . "\n";
				my $return = system($call);
				
				# falls ein fehler auftaucht soll abgebrochen werden
				if($return)
				{
					print "fatal: tried to install a dependent app (merge), but last call failed: $return";
					print STDERR "fatal: tried to install a dependent app (merge), but last call failed: $return";
					exit(10);
				}
				
				# die temporaere installation der dependent app in THIS integrieren
				print "rsync -avz $TMPDIR/install/$appToMerge/$actBranch/ $TMPDIR\n";
				system "rsync -avz $TMPDIR/install/$appToMerge/$actBranch/ $TMPDIR";
				
#				# evtl. vorhandenes script "bin/start.sh" soll umbenannt werden in bin/<appname>
#				print "mv $TMPDIR/bin/start.sh $TMPDIR/bin/$appToMerge\n";
#				system "mv $TMPDIR/bin/start.sh $TMPDIR/bin/$appToMerge";
#
#				# evtl. vorhandenes file "etc/default.ini" soll umbenannt werden in etc/<appname>.ini
#				print "mv $TMPDIR/etc/default.ini $TMPDIR/etc/$appToMerge.ini\n";
#				system "mv $TMPDIR/etc/default.ini $TMPDIR/etc/$appToMerge.ini";
			}
			
			# das bin, in das alle 'gemergten' bin(s) eingeflossen sind soll umbenannt werden in bin2
			print "mv $TMPDIR/bin $TMPDIR/bin2\n";
			system "mv $TMPDIR/bin $TMPDIR/bin2";
			
			# und das urspruengliche bin wieder hergestellt werden
			sleep(1);
			print "info: temporaeres umbenennen von bin wieder aufheben\n";
			print "info: mv $TMPDIR/bin_temporaer_umbenannt $TMPDIR/bin\n";
			system("mv $TMPDIR/bin_temporaer_umbenannt $TMPDIR/bin");
		}
		#-------------------
		# --- END ACTION 'merge(app:app:app:...)' --- #



		#-------------------
		# --- START ACTION 'altPerl' --- #
		# es sollen bei allen perlscripten die shebang-zeile augetauscht werden
		foreach my $actAction (@now_action)
		{
			if ( $actAction =~ m/altPerl\(([^)]+)\)/ )
			{
				my $alternativePerlShebangzeile = "#!$1";
				#-------------------
				# ersetzen des eingetragenen perlinterpreters durch den cb2-eigene installation
				print "info: action 'altPerl' found in array (@now_action)\n";
				print "info: add $alternativePerlShebangzeile as shebang-line to all perl-scripts (except the ones except=.....).\n";
				
				# gibt es ausnahmen?
				my @matched_now_action = grep { /altPerl/ } @now_action;
				my @except = grep { /except/ } @matched_now_action;
				
				# alles entfernen bis auf die ausnahmen
				foreach my $except(@except)
				{
					$except =~ s/altPerl\([^)]+\)//;
					$except =~ s/^\s*\(\s*//;
					$except =~ s/\s*\)\s*$//;
				}
				
				# splitten des string except=<filename>,<filename>,...)
				my @exceptString = split("=", $except[0]);
				my @exceptFiles = split(",", $exceptString[1]);
				
				find( sub { wanted2() }, "$TMPDIR");
				
				sub wanted2
				{
#					my $cb2_perl_shebang = "#!/share/sdmmisc/cb2/cb2perl/bin/perl";
					# wenn es ein directory ist, dann verwerfen
					print "filename in wanted: ".$File::Find::name."\n";
					if ( -d $File::Find::name )
					{
						print "skipping directory: ".$File::Find::name."\n";
						next;
					}
					if (!( -T $File::Find::name ))
					{
						print "skipping binary file: ".$File::Find::name."\n";
						next;
					}
					my $exceptFound = 0;
					foreach my $exceptFile (@exceptFiles)
					{
	#					print "is this file the one that should be excepted?\n";
	#					print $File::Find::name ." =~ m/" . $exceptFile . "/";
						if ($File::Find::name =~ m/$exceptFile/)
						{
							print "skipping file, because it is in the exception-list to the action 'perl_cb2': ".$File::Find::name."\n";
							$exceptFound++;
						}
					}
					if($exceptFound)
					{
						next;
					}
					
					if (!open (FILE, "<$File::Find::name")) {die "cannot read $File::Find::name: $!\n";}
					my $modeOldfile = (stat($File::Find::name))[2] & 07777;
					my $zeile = 1;
					my $ist_perl = 0;
					while(<FILE>)
					{
						if ($zeile == 1)
						{
							if ($_ =~ m/#!.*perl.*/)
							{
								print "found shebang that points to perl\n";
								$ist_perl = 1;
							}
							$zeile++;
							last;
						}
					}
					
					if ($ist_perl)
					{
						print "adding new shebang that point to a special perl-installation\n";
						# neue shebang als erste zeile einfuegen
						my @alles = <FILE>;
						unshift(@alles, $alternativePerlShebangzeile."\n");
						
						# alte datei ueberschreiben
						close FILE;
						unlink $File::Find::name;
						if (!open (FILE_TO_WRITE, ">$File::Find::name")) {die "cannot write $File::Find::name: $!\n";}
						print FILE_TO_WRITE @alles;
						close FILE_TO_WRITE;
						chmod($modeOldfile, $File::Find::name);
					}
					
				}
			}
		}
		#-------------------
		# --- END ACTION 'altPerl' --- #

		# deploy.sh script ausfuehren, wenn eines existiert
		if ($now_deployscript)
		{
			my $now_deployscript_withpath = $TMPDIR."/".$now_deployscript;
			print "info: deployscript should be executed: $now_deployscript_withpath.\n";
			if (stat $now_deployscript_withpath)
			{
				print "info: deployscript exists: $now_deployscript_withpath\n";
				print "info: executing deployscript starts --------------- $now_deployscript_withpath\n";
				my $rueck = `$now_deployscript_withpath`;
				
				print $rueck;
				print "info: executing deployscript ends --------------- $now_deployscript_withpath\n";
				if ($rueck =~ m/<newdir>(.+)<newdir>/)
				{
					$TMPDIR = $1;
					print "info: deployscript orders to change temporary directory to new path: $TMPDIR\n";
				}
			}
			else
			{
				print "warn: there is no deployscript ($now_deployscript_withpath)\n";
			}
		}
		
		
#		print "info: emptying target directory if already exists ".$now_targetbulkappbranch."\n";
#		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkappbranch/*\"\n"; 
#		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkappbranch/*\"";
#
		if ($cleanbranch)
		{
			print "info: deleting directory (if exists) ".$now_targetbulkappbranch."\n";
			print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkappbranch\"\n"; 
			system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkappbranch\"";
		}
	
		print "info: creating target directory ".$now_targetbulkappbranch."\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbulkappbranch\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbulkappbranch\"";
		
		print "info: setting actual timestamp to target directory if its an old one ".$now_targetbulkappbranch."\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"touch $now_targetbulkappbranch\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"touch $now_targetbulkappbranch\"";

		

		#-----------------
		# das quell-verzeichnis nach $target syncen und dabei die zu ignorierenden files beachten (deployignore.txt)
		my $deployignore = "deployignore.txt";
		my @ignorePatterns =  split(",", $CONF{"autoignore"});

		# erstellen eines fingerprints ueber die daten
		print "info: generating a fingerprint with ptest ".$now_targetbulkappbranch."\n";
		my $callPtest = $CONF{"callFingerprint"} . " -ignorefile $deployignore -path $TMPDIR";
		foreach my $ignorePattern(@ignorePatterns)
		{
			$callPtest .= " -ignore " . $ignorePattern;
		}
		
		print $callPtest . "\n";
		system($callPtest);

		# verarbeiten der ignore eintraege zu einem rsync aufruf...
		my @deploy_ignore;
		print "info: installing $TMPDIR to $now_targetbulkappbranch (excluding '.git', '.project' and '*deploy*' and everything from '$deployignore')\n";
#		if (stat $deployignore)
#		{
#			open (DEPLOYIGNORE, '<', $deployignore) or die "can't read $deployignore: $!";
#			@deploy_ignore = <DEPLOYIGNORE>;
#			chomp @deploy_ignore;
#		}
		my $rsynccall = "rsync";
#		foreach my $exclude_string (@deploy_ignore, @ignorePatterns)
#		die standardangaben der zu ignorierenden patterns werden aus dem CONF uebernommen
		foreach my $exclude_string (@ignorePatterns)
		{
			$rsynccall .= " --exclude=\"$exclude_string\"";
		}
		# die projekteigenen ignoreangaben werden als file uebergeben
		if(stat $deployignore)
		{
			$rsynccall .= " --exclude-from $deployignore";
		}
		# und der rest des aufrufes
		$rsynccall .= " -avz $TMPDIR/ ".$now_targetuser."\@".$now_targetmachine.":".$now_targetbulkappbranch;

		print $rsynccall."\n";
		system $rsynccall;
		#-----------------

		#-----------------
		# alle umbenennungen, die in der datei deploytargetrename enthalten sind, im targetbulkappbranch durchfuehren
		my $deploytargetrename = "deploytargetrename.txt";
		my @deploytargetrename;
		my %deploytargetrename;
		print "info: renaming files/directories in $now_targetbulkappbranch defined in file '$deploytargetrename'\n";
		if (stat $deploytargetrename)
		{
			open (DEPLOYTARGETRENAME, '<', $deploytargetrename) or die "can't read $deploytargetrename: $!";
			@deploytargetrename = <DEPLOYTARGETRENAME>;
			chomp @deploytargetrename;
		}
		foreach (@deploytargetrename)
		{
			my @zeile = split (" ", $_);
			if (@zeile != 2)
			{
				print "error: $deploytargetrename. cannot interprete line: $_\n";
			}
			else
			{
				print "info: $deploytargetrename. renaming $zeile[0] to $zeile[1]\n";
				print "info: $deploytargetrename. in case there exists already a file/dir called '$zeile[1]', i will send a 'remove' first.\n";
				print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkappbranch/$zeile[1]\"\n";
				system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkappbranch/$zeile[1]\"";
				print "info: $deploytargetrename. and now the 'move'.\n";
				print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mv $now_targetbulkappbranch/$zeile[0] $now_targetbulkappbranch/$zeile[1]\"\n";
				system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mv $now_targetbulkappbranch/$zeile[0] $now_targetbulkappbranch/$zeile[1]\"";
			}
		}

		# vorab rechte in zielverzeichnis setzen auf 755 (dies ist nur notwendig, wenn now_app == builder ist, schadet aber bei anderen nicht)
		print "info: vorab setting rights in targetbulk to 755 (dies ist nur notwendig, wenn now_app == builder ist, schadet aber bei anderen nicht)\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"chmod -R 755 $now_targetbulk\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"chmod -R 755 $now_targetbulk\"";

		# den allgemeinen commondriver in das temporaere verzeichnis kopieren
		print "info: copying common driver $CONF{'commondriver'} to $TMPDIR/commondriver\n";
		print "cp $CONF{'commondriver'} $TMPDIR/commondriver\n";
		system "cp $CONF{'commondriver'} $TMPDIR/commondriver";

		print "der aktuelle appname ist: $now_app\n";

#		# den allgemeinen commondriver an die aktuelle app anpassen
		# oeffnen der kopie des commondriver
		my $filename = "$TMPDIR/commondriver";
		my $filename_tmp = "$TMPDIR/commondriver-tmp";
		open (VON, '<', $filename) or die "Can't read $filename: $!";
		open (ZU, '>', $filename_tmp) or die "Can't write $filename_tmp: $!";
		
		while(<VON>)
		{
			$_ =~ s/<bundleName>/$now_app/;
			print ZU $_;
		}
		close VON;
		close ZU;
		system "mv $filename_tmp $filename";

		# es soll diese Ersetzung nur gemacht werden, wenn es $now_app NICHT 'builder' heisst
		# den allgemeinen commondriver an die aktuelle app anpassen
		
#		if (!($now_app eq "builder"))
#		{
#			find( sub { wanted3() }, "$TMPDIR/commondriver");
#				
#			sub wanted3
#			{
#				my $relname = File::Spec->abs2rel($File::Find::name);
#				my $tt = Template->new();
#				my $vars = {
#							bundleName	=> sub	{
#												print "replacing placeholder for 'bundleName' with '$now_app'\n";
#												return $now_app;
#											},
#							};
#	 			$tt->process($relname, $vars, $relname) || die $tt->error();
#			}
#		}

		# den angepassten commondriver installieren
		print "info: installing common driver $TMPDIR/commondriver to $now_targetbin"."/".$now_app."\n";
		print "rsync -avz $TMPDIR/commondriver ".$now_targetuser."\@".$now_targetmachine.":".$now_targetbin."/".$now_app."\n";
		system "rsync -avz $TMPDIR/commondriver ".$now_targetuser."\@".$now_targetmachine.":".$now_targetbin."/".$now_app;

		# zusaetzliche commondriver (jeweils einen fuer jeden eintrag in action=addcaller(caller1,caller2))
		#-------------------
		# --- START ACTION 'addcaller' --- #
#		my $full_term;
		if ( grep { /addcaller/; /addcaller\((.+)\)/ } @now_action )
		{
			my @all_caller;
			# alle action-strings durchgehen und falls vorhanden die filenamen innerhalb der klammern feststellen
			foreach (@now_action)
			{
				if ($_ =~ m/addcaller\((.+)\)/)
				{
					push(@all_caller, split(",", $1));
				}
			}

			foreach my $caller (@all_caller)
			{
				print "info: installing additional common driver $TMPDIR/commondriver to $now_targetbin"."/".$caller."\n";
				print "rsync -avz $TMPDIR/commondriver ".$now_targetuser."\@".$now_targetmachine.":".$now_targetbin."/".$caller."\n";
				system "rsync -avz $TMPDIR/commondriver ".$now_targetuser."\@".$now_targetmachine.":".$now_targetbin."/".$caller;
			}
		}
		#-------------------
		# --- END ACTION 'add_caller' --- #

		# wenn --setdefault verwendet wurde, soll ein file mit dem string im installationsverzeichnis angelegt werden
		if($setdefault)
		{
			print "info: set default version of app to $setdefault/\n";
			print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"echo $setdefault > $now_targetbulk/version.$now_app\"\n"; 
			system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"echo $setdefault > $now_targetbulk/version.$now_app\"\n"; 
		}

		# rechte in zielverzeichnis setzen auf 755
		print "info: setting rights in targetbulk to 755\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"chmod -R 755 $now_targetbulk\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"chmod -R 755 $now_targetbulk\"";
		print "info: setting rights in targetbin to 755\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"chmod -R 755 $now_targetbin\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"chmod -R 755 $now_targetbin\"";

		# rechte aller files, die mit "Makefile.PL" enden, sollen auf 444 (nur lesen) gesetzt werden
		print "info: setting rights in targetbulk to 444 for all files/dirs matching /*Makefile.PL/\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"find $now_targetbulkappbranch -depth -regex '.*Makefile.PL' -exec chmod -R 444 {} \\;\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"find $now_targetbulkappbranch -depth -regex '.*Makefile.PL' -exec chmod -R 444 {} \\;\""; 

		# rechte aller files und verzeichnisse, die mit ".source" enden, sollen auf 750 gesetzt werden
		print "info: setting rights in targetbulk to 750 for all files/dirs matching /.source*/\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"find $now_targetbulk -depth -regex '.*\\.source' -exec chmod -R 750 {} \\;\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"find $now_targetbulk -depth -regex '.*\\.source' -exec chmod -R 750 {} \\;\""; 

		# rechte aller files und verzeichnisse, die mit ".source" enden, sollen auf 750 gesetzt werden
		print "info: setting rights in targetbulk to 750 for all files/dirs matching /.source*/\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"find $now_targetbulk -depth -regex '.*\\.source' -exec chmod -R 750 {} \\;\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"find $now_targetbulk -depth -regex '.*\\.source' -exec chmod -R 750 {} \\;\""; 

		# wenn das flag --pack gesetzt wurde, soll das installationsverzeichnis in ein *.tar.gz archiv gepackt werden
		if($pack)
		{
			print "info: renaming destination directory to create a fine tarball\n";
			print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mv $now_targetbulkappbranch $now_targetbulkapp/" . $app . "-" . $actBranch . "\"\n"; 
			system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mv $now_targetbulkappbranch $now_targetbulkapp/" . $app . "-" . $actBranch . "\"";
			print "info: if a tarball with the desired name already exists, it shoul be deleted\n";
			print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkapp/" . $app . "-" . $actBranch . ".tar.gz\"\n"; 
			system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkapp/" . $app . "-" . $actBranch . ".tar.gz\"";
			print "info: packing the destination in a tar.gz-archiv\n";
			print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"tar -cvzf $now_targetbulkapp/" . $app . "-" . $actBranch . ".tar.gz -C $now_targetbulkapp " . $app . "-" . $actBranch . "\"\n"; 
			system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"tar -cvzf $now_targetbulkapp/" . $app . "-" . $actBranch . ".tar.gz -C $now_targetbulkapp " . $app . "-" . $actBranch . "\"";
			print "info: renaming tarball-destination to its previous name \n";
			print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mv $now_targetbulkapp/" . $app . "-" . $actBranch . " $now_targetbulkappbranch\"\n"; 
			system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mv $now_targetbulkapp/" . $app . "-" . $actBranch . " $now_targetbulkappbranch\"";
			print "info: delete directory \n";
			print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkappbranch\"\n"; 
			system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkappbranch\"";
		}
	}
}
#-------------------


#===================
# liest ein configfile ein und gibt eine Liste zurueck key1, val1, key2, val2, ...
# PROTO: getvars($database_file, $log4perl_handle)
# RETURNS: @sorted_list_of_stepnames
sub getvars
{
    my $fp_conf = shift;
    
    my %CONF;
    if (!open (CONF, "<$fp_conf")) {die "Kann $fp_conf nicht zum lesen oeffnen $!\n";}
    
    while(<CONF>)
    {
        if    ( $_ =~ m/^#/) {next}
        elsif ( $_ =~ m/^$/) {next}
		elsif ( $_ =~ m/^\s*$/) {next}
        else
        {
            my @tmp = split("=", $_);
        	$tmp[0] =~ s/\s$//g;
        	$tmp[0] =~ s/^\s//g;
        	$tmp[1] =~ s/\s$//g;
        	$tmp[1] =~ s/^\s//g;
            $CONF{$tmp[0]} = $tmp[1];
        }
    }
    return %CONF;
}


