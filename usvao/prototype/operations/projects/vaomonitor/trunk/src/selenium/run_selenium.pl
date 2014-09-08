#!/usr/contrib/linux/bin/perl -w
################################################################################
#
# PROGRAM: run_selenium 
#
# AUTHORS:  Michael Preciado <Michael.E.Preciado@nasa.gov>
#         
# CREATED: 2009/05/11
#
# DESCRIPTION: Perl wrapper to test VO Services using Selenium. This program can also
#              be used as a general framework for non VO tests.
#            
#              
# HISTORY: Wed May 1 14:07:46 EST 2009
#  
#
###############################################################################
use strict;
package main;

use Getopt::Long;
use File::Basename;
use Time::HiRes qw [time];
use WWW::Selenium;
use Test::More;
use POSIX ":sys_wait_h";

use lib '/www/htdocs/cgi-bin/lib/nagios/libexec';

use utils qw(%ERRORS &print_revision &support &usage);
use vars qw($opt_V $opt_h $opt_t $verbose $timeout $host $urlnode $log $custom
            $DEFAULT_TIMEOUT $PROGNAME $VERSION $opt_debug $multiwindow $pid $childid $environ $session_id $port $dir  
	    $file $sel);
  

BEGIN
{
    $dir = dirname($0); 
    unshift @INC,"./Modulesnew";
}

$VERSION = '1.0.0';
$PROGNAME = basename($0);
$DEFAULT_TIMEOUT = 190;
 
sub print_help ();
sub print_usage ();

$ENV{'PATH'} = '';
$ENV{'BASH_ENV'} = '';
$ENV{'ENV'} = '';
my $test_home = "/software/jira/usvo/projects/web_test"; 
$ENV{PATH} = "$ENV{PATH}:<path to local firefox browser,perhaps firefox 2 (although old,this version works with the system)>:<path to system libraries and perl/5.8.">;

# Firefox needs the libpangocairo library file, which is in usr/contrib
#$ENV{LD_LIBRARY_PATH} = "/usr1/local/lib:/software/usr/contrib/linux/lib";

#Selenium remote control driver
$ENV{CLASSPATH} = "<path to selenium remote control driver (selenium-java-client-driver.jar) and selenium remote control (selenium-remote-control-1.0.1/)>";


Getopt::Long::Configure('bundling');
#my $window = '-multiwindow';
my $options_ok = GetOptions
        ("V"   => \$opt_V, "version"        => \$opt_V,
         "h"   => \$opt_h, "help"           => \$opt_h,
         "v"   => \$verbose, "verbose"      => \$verbose,
	 "H=s" => \$host, "host"            => \$host,
	 "u=s" => \$urlnode, "url"          => \$urlnode,
         "t=s" => \$opt_t, "timeout=i"      => \$opt_t,
	                    "debug"         => \$opt_debug,
	 "c=s" => \$custom, "custom"        => \$custom, 	 
	 "f=s" => \$file , "file"          => \$file,
	 "m" => \$multiwindow,"multiwindow"  => \$multiwindow,
	 "l=s" => \$log, "log"      => \$log,
	 "e=s" => \$environ,"environ"       => \$environ,
         );

if (! $options_ok) { # Bad option passed
        print_help();
        print "\nERROR: Bad command line option passed!\n";
        exit $ERRORS{'UNKNOWN'};
}

if ($opt_V) {
        print_revision($PROGNAME,$VERSION);
        exit $ERRORS{'OK'};
}

if ($opt_h) {
        print_help();
        exit $ERRORS{'OK'};
}
if ($multiwindow){
    $multiwindow = "-multiwindow";
}


$timeout = $opt_t  || $DEFAULT_TIMEOUT;

$SIG{'ALRM'} = sub {
        print "ERROR: $file  Check timed out. No response within $timeout seconds.\n";
	
        local $SIG{HUP}= 'IGNORE';
	kill HUP => -$$;
	exit $ERRORS{'UNKNOWN'};
};
alarm($timeout);

open (S,">>./selenium_results");
eval {
    my ($string_found, $response_time, $session_id,$port ) = run_test($host, $urlnode);    
    if ($string_found eq "passed") 
    {
	printf("$file Test OK -  %.3f seconds |time=%.6f;;;0.000000;;;0\n",$response_time,$response_time); 
	print S "$file: OK: $response_time\n"; 
        if ($pid != "0")
	{	 	    
	    stop_selenium();   
	    exit $ERRORS{'OK'};
	}
    } 
    else
    {
        print "CRITICAL: $file :  The string to be matched was not found!\n";
	print S "$file: Fail\n";
	#shut down selenium RC  
	if ($pid != "0")
	{	    
	    stop_selenium();
	    #cleanup();
	}
        exit $ERRORS{'CRITICAL'};
    }
};

close S;

if ($@)
{  
    stop_selenium();
    exit $ERRORS{'UNKNOWN'};
}
sub stop_selenium
{ 
    $SIG{HUP} = 'IGNORE';
    kill HUP => -$$;
}
#------------------------------------------------------------------------------
sub print_usage () {
        print "Usage: $PROGNAME [-VhHu] [-t <seconds>]\n";
}
#------------------------------------------------------------------------------
sub print_help () {
    print_usage();
    print <<_HELP_;
A nagios plugin that tests the VO Tools. 

Options:
  -t, --timeout         Timeout value, in seconds (default 115)
  -h, --help            This help message
  -V, --version         Version information ($VERSION)
  -v, --verbose         Verbose mode displays more information about the
                        directories being checked.
  -H, --host            (remote)
  -u, --urlnode         (remote)
_HELP_
}
#------------------------------------------------------------------------------
sub run_test
{
    my ($host,$urlnode) = @_;
    $ENV{DISPLAY} = ":$environ" if ($environ);
    $ENV{DISPLAY} = ":99" if (! $environ);
    
    my $customprofile = "<path to your selenium firefox custom profile>";
    my $status = 0;

    #determine port number to use:
    $port = $$  + 10000;   
   
    $multiwindow = "-singleWindow" if (! $multiwindow); 
   
    unless( $pid = fork() )
        {	
            # Child process      
	    open STDERR, ">> /dev/null";
	    open STDOUT ,">>/dev/null";   
     
            my $command = "/usr1/local/bin/java -jar  <path to your selenium remote-control code (selenium-server.jar) -log $log/selenium.log.$port -browserSideLog  -port $port   $multiwindow  -ensureCleanSession  -Djava.io.tmpdir=$custom  -firefoxProfileTemplate $customprofile";
	  
	   	  
            exec $command;  
	    
            exit(0);    
        }
    
    #need to give selenium server  a few seconds to start 
    sleep(10);  
   
    #get start time
    my $start_time        = time();
   
    #declare local vars
    my ($string_found, $size,$response_time);
            
    #run the user's test
    $string_found =  run_user_test($port);
     
    
    #get end time
    my $end_time   = time(); 
    $response_time = $end_time-$start_time;
    
    return $string_found, $response_time,$session_id, $port;
}
sub run_user_test
{
    my ($port) = @_;
    $sel = WWW::Selenium->new( host => "localhost", 
				  port => "$port", 
			          browser => "*firefox2 /usr1/local/firefox-2/firefox-bin",
                                  browser_url  => "http://$host/",);
         
				   
    #start selenium
    $sel->start();
    $sel->open($urlnode);
  
    my $string_found = "passed";
    eval
    {       
	$$::session_id = $sel->{session_id};
	print "C: $file";

	require "$file";
    };
    if ($@)     
    {   
	print "\nEnd of Test Errors: $@\n\n" if $opt_debug;
	$string_found = "failed";
    }    
    #stop selenium here to avoid problems in Selenium's automated cleanup
    $sel->stop();
      
    return $string_found;
}
sub cleanup
{
    #need a double check here.
   
    return if (!$log);
    my @container;
    
   
    open (Log,"$log/selenium.log.$port")
	|| die "cannot open selenium log";
    
    
    while (<Log>)
    {
	my $line = $_;
	chomp $line;	  
	my @array = grep (/Cannot run program \"kill\"/ ,  $line);
	push @container,@array;
    }
   
    close Log || die "cannot close selenium log";
    
    if (@container)
    {

	sleep(6);
	`kill  -HUP $pid`;
    }
}
