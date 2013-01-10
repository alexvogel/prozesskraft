#!/usr/bin/perl
#!/opt/cb2/perl/bin/perl

my $version = "[% version %]";
my $date = "[% date $]";
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

my $instancedir = "/tmp/".$procname."_" . $datum . "_" . $$;
my $stack = "/home/".$ENV{'USER'}."/.".$procname."/".$procname.".stack";
my $conf = $etc . "/$procname.conf";

#my $commondriver = "/home/".$ENV{'USER'}."/.".$procname."/".$procname.".commondriver";
my $help;
my $batch;
my $app;
my $repodir;
my $targetdir;
my $targetmachine;
my $targetuser;
my $ybranches;
my $cleanapp;
my $cleanbranch;
my $genstack;
my $result = GetOptions(
#                        "scenesdir=s"=> \$scenesdir,
                        "help|h"        => \$help,
                        "batch"        => \$batch,
                        "app=s"        => \$app,
                        "repodir=s"    => \$repodir,
                        "targetdir=s"  => \$targetdir,
                        "targetmachine=s"  => \$targetmachine,
                        "targetuser=s" => \$targetuser,
                        "ybranches=s"  => \$ybranches,
                        "stack=s"     => \$stack,
                        "cleanapp"   => \$cleanapp,
                        "cleanbranch"=> \$cleanbranch,
                        "genstack"=> \$genstack,
                        );

#-------------------
# festlegen des beulen konfigurationsfiles
# diese parameter muessen im konfigurationsfile zu finden sein
# wenn das 'value' des schluessels zu einem absoluten pfad expanded wird ($installverzeichnis+relpath) und darunter ein file gefunden wird,
# expandierten pfad beibehalten, sonst wieder auf urspruenglichen wert setzen
my @conf_muss_inhalt = (
							"example_stack",
							"commondriver",
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
$helptext .= " --targetmachine=STRING  [optional, filters stack] processes only the lines from the stack-file where this targetmachine is mentioned\n";
$helptext .= " --targetuser=STRING  [optional, filters stack] processes only the lines from the stack-file where this targetuser is mentioned\n";
$helptext .= " --ybranches=INT      [optional, overrides stack] installs the youngest <INT> branches (plus 'master' and all '*beta' younger than the <INT> youngest branches).\n";
$helptext .= " --cleanbranch		[optional] prior to install all content in target directory of the processed branch of the processed app will be deleted. this value overrides the value in configfile\n";
$helptext .= " --cleanapp			[optional] prior to build all branches of the processed app will be deleted.\n";
$helptext .= " --batch              [optional] direct execution of all relevant buildinstances without possibility to abort.\n";
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
# check ob --cleanapp gleichzeitig genutzt werden
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
			if ($paramset{'app'} ne $app)
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
		
		# wenn parameter --targetuser angegeben wurde, sollen nur zeilen im configfile mit diesem targetuser beruecksichtigt werden
		if ($targetuser)
		{
			if ($paramset{'targetuser'} ne $targetuser)
			{
				$soll_beruecksichtigt_werden = 0;
			}
		}
		
		# wenn parameter --ybranches angegeben wurde, soll fuer alle zeilen im configfile dieser ybranches-Wert verwendet werden (statt den den im file) 
		if ($ybranches || $ybranches == 0)
		{
			$paramset{'ybranches'} = $ybranches;
		}
		
		# ausgabe, welche zeilen aus config im weiteren verlauf aktiviert/deaktiviert sind.
		if ($soll_beruecksichtigt_werden)
		{
			print "info: activate:";
			foreach my $key (grep {!/action/} sort keys %paramset) {print " " . $key . "=" . $paramset{$key}}
			foreach my $key (grep { /action/} sort keys %paramset) {print " " . $key . "=" . "@{$paramset{$key}}"}
			print "\n";
			push @CONFIG, \%paramset;
		}
		else
		{
			print "info: deactivate:";
			foreach my $key (grep {!/action/} sort keys %paramset) {print " " . $key . "=" . $paramset{$key}}
			foreach my $key (grep { /action/} sort keys %paramset) {print " " . $key . "=" . "@{$paramset{$key}}"}
			print "\n";
		}
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
# erstellen des logfiles
my $logfile = $ENV{'HOME'}."/.builder/builder.log";
print "all logging goes to $logfile\n";

open (LOG, '>', $logfile) or die "Can't write $logfile: $!";
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
	my $now_targetuser = $$refh_stackline{'targetuser'};
	my $now_targetmachine = $$refh_stackline{'targetmachine'};
	my $now_ybranches = $$refh_stackline{'ybranches'};
	my $now_deployscript = $$refh_stackline{'deployscript'};
	my @now_action = @{$$refh_stackline{'action'}};

	print "info: ----- processing next stackline -----\n";
	
	#-------------------
	# ausgabe der parameter aus conf und abgeleiterer params
	print "info: app = $now_app\n";
	print "info: repodir = $now_repodir\n";
	print "info: targetdir = $now_targetdir\n";
	print "info: targetbulk = $now_targetbulk\n";
	print "info: targetbulkapp = $now_targetbulkapp\n";
	print "info: targetbin = $now_targetbin\n";
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
	
	#-------------------
	# kopieren des repos nach $instancedir/<random>
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
	chomp @branches;
	@branches = grep { /\s*remotes.+\/([^\/]+)$/ && !/HEAD/ } @branches;	# herausfiltern von kurzangabe und HEAD
	for(my $x=0; $x<@branches; $x++)
	{
		$branches[$x] =~ s/\s*remotes\/\w+\///;
	}

	# das targetverzeichnis erstellen
	print "info: creating target directory $now_targetdir\n";
	print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetdir\"\n"; 
	system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetdir\"";
	
	print "info: creating target directory ".$now_targetbin."\n";
	print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbin\"\n"; 
	system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbin\"";
	
	print "info: creating target directory ".$now_targetbulk."\n";
	print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbulk\"\n"; 
	system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"mkdir $now_targetbulk\"";
	
	if ($cleanapp)
	{
		print "info: deleting directory (if exists) ".$now_targetbulkapp."\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkapp\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"rm -rf $now_targetbulkapp\"";
	}
	
	print "info: creating target directory ".$now_targetbulkapp."\n";
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
	
	my @branches_numericnames = grep { $_ =~ /^[\d\.]+$/ } @branches;	# branches, die eine zahl im namen tragen (0.1 oder 2.1.10.2)
	my @branches_numericnames_sort = reverse sort @branches_numericnames;	# numerisch absteigend sortieren
	my @branches_alphanames = grep { $_ =~ /^[\w]+$/ } @branches;	# branches, die eine zahl im namen tragen (0.1 oder 2.1.10.2)
	my @branches_alphanames_sort = reverse sort @branches_alphanames;	# numerisch absteigend sortieren
	my @allbranches = (@branches_alphanames_sort, @branches_numericnames_sort);

	# als ersten branch 'master' der liste hinzufuegen
#	unshift @branches_numericnames_sort, "master";

	unless (@branches_numericnames_sort) {print "info: no branch with numerical name found. exit.\n";exit;}

	foreach (@branches_numericnames_sort)
	{
		print "info: in temporary git-repository branch with numericname found '$_'\n";
	}
	print "info: only the $ybranches latest branches with numericname will be build.\n";
	
	foreach (@branches_alphanames_sort)
	{
		print "info: in temporary git-repository branch with alphaname found '$_'\n";
	}
	print "info: all the branches with alphaname will be build.\n";

	#-------------------
	# fuer die letzten ybranches und alle alphabenamten branches durchfuehren
	if ($now_ybranches > scalar(@branches_numericnames_sort))
	{
		$now_ybranches = scalar(@branches_numericnames_sort);
	}
	$now_ybranches += scalar(@branches_alphanames_sort);
	
	for (my $x=0; $x<($now_ybranches); $x++)
	{
		print "info: will process branch '$allbranches[$x]'\n";

		# festlegen der zielverzeichnisse fuer programmdaten und den aufruftreiber
		my $now_targetbulkappbranch = $now_targetbulkapp . "/" . $allbranches[$x];
		print "info: target bulkappbranch directory: ".$now_targetbulkappbranch . "\n";

		print "info: calling: git checkout -f $allbranches[$x]\n"; 
		system "git checkout -f $allbranches[$x]";
		
		#-------------------
		# ermitteln des datums des letzten commits des gerade ausgecheckten branches
		print "info: determining the date of the last commit of that branch\n"; 
		print "info: calling: git log -n 1 | grep 'Date'\n"; 
		my $rueck = `git log -n 1 | grep 'Date'`;
		$rueck =~ m/^Date:\s+(\w+\s\w+\s\w+)\s.+$/;
		my $date_lastcommit = "unknown";
		$date_lastcommit = $1;
		print "info: determined date is: $date_lastcommit\n";
		#-------------------

		#-------------------
		# --- START ACTION 'searchreplace' --- #
		if ( grep { /searchreplace/ } @now_action )
		{
			
			#-------------------
			# suchen und ersetzen des platzhalters [% version %] in allen files
			print "info: action 'searchreplace'\n";
			print "info: putting version-string into all files where [% version %] is found\n";
			find(\&wanted, $TMPDIR);
			
			sub wanted
			{
				unless ( -d $File::Find::name || $File::Find::name =~ /\.git/ || -B $File::Find::name )
				{
					print "info: processing file for filling placeholder [% version %] with string '$allbranches[$x]': $File::Find::name\n";
					my $relname = File::Spec->abs2rel($File::Find::name);
					my $tt = Template->new();
					my $vars = { version => $allbranches[$x], date => $date_lastcommit};
					$tt->process($relname, $vars, $relname) || print "error in subroutine"; next();
				}
			}
		}
		#-------------------
		# --- END ACTION 'searchreplace' --- #
		else
		{
			print "info: no action 'searchreplace'\n";
		}

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
		my @deploy_ignore;
		print "info: installing $TMPDIR to $now_targetbulkappbranch (excluding '.git' and '*deploy*' and everything from '$deployignore')\n";
		if (stat $deployignore)
		{
			open (DEPLOYIGNORE, '<', $deployignore) or die "can't read $deployignore: $!";
			@deploy_ignore = <DEPLOYIGNORE>;
			chomp @deploy_ignore;
		}
		my $rsynccall = "rsync";
		foreach my $exclude_string (@deploy_ignore)
		{
			$rsynccall .= " --exclude=\"$exclude_string\"";
		}
		$rsynccall .= " --exclude=\".git\" --exclude=\"**deploy**\" -avz $TMPDIR/ ".$now_targetuser."\@".$now_targetmachine.":".$now_targetbulkappbranch;

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
		

		# den commondriver installieren
		print "info: installing common driver $CONF{'commondriver'} to $now_targetbin"."/".$now_app."\n";
		print "rsync -avz $CONF{'commondriver'} ".$now_targetuser."\@".$now_targetmachine.":".$now_targetbin."/".$now_app."\n";
		system "rsync -avz $CONF{'commondriver'} ".$now_targetuser."\@".$now_targetmachine.":".$now_targetbin."/".$now_app;

		# rechte in zielverzeichnis setzen auf 755
		print "info: setting rights in targetbulk to 755\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"chmod -R 755 $now_targetbulk\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"chmod -R 755 $now_targetbulk\"";
		print "info: setting rights in targetbin to 755\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"chmod -R 755 $now_targetbin\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"chmod -R 755 $now_targetbin\"";
		
		# rechte aller files und verzeichnisse, die mit ".source" enden, sollen auf 700 gesetzt werden
		print "info: setting rights in targetbulk to 700 for all files/dirs matching /.source*/\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"find $now_targetbulk -depth -regex '.*\\.source' -exec chmod -R 700 {} \\;\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"find $now_targetbulk -depth -regex '.*\\.source' -exec chmod -R 700 {} \\;\""; 

		# rechte aller files und verzeichnisse, die mit "source." anfangen, sollen auf 700 gesetzt werden
		print "info: setting rights in targetbulk to 700 for all files/dirs matching /*source./\n";
		print "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"find $now_targetbulk -depth -regex '.*source\\..*' -exec chmod -R 700 {} \\;\"\n"; 
		system "ssh " . $now_targetuser . "\@" . $now_targetmachine . " -C \"find $now_targetbulk -depth -regex '.*source\\..*' -exec chmod -R 700 {} \\;\""; 

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


