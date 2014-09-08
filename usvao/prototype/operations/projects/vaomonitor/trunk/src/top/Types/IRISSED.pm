#
#
#
#
#
package Types::IRISSED;
@ISA  = ("Types::SimpleURLResponse");

use strict;
use lib '/www/htdocs/cgi-bin/W3Browse/lib';
use lib './';
use lib './GUITesting';
use lib './GUITesting/iris-1.0-unix-i386/lib/';


use warnings;

sub new 
{
    my ($class) = shift;
    my $hash = {};    
    bless $hash, $class;
    $hash->init(@_);
    return $hash;
}
sub init
{
    my $self = shift;
    $self->SUPER::initialize(@_);	
    $self->{desc} = 'is up';
    
}
sub showresponse
{
    my $self = shift;
    my $res = $self->getsimpleresponse();
    print $res;
}
sub test
{
    my $self         = shift;   
    my $withxml      = shift;
    $self->{withxml} = $withxml if ($withxml);
    my $url          = $self->{url};      
    my $res          = $self->getsimpleresponse(); 
  
     
    my $testname = $self->{testname};	 
    my @a = ($res);
    my @b = grep(/<TABLEDATA>/,@a);

    $self->setMessage("The '$testname' has failed. Response does not contain the string to be matched");
    $self->setStatus("fail");
    $self->setId("2.1");


    if (@b)
    {
	$self->setMessage("The $testname has passed. Response contains the string to be matched");
	$self->setStatus("pass");	
    }
    $self->build_table();
}
sub getsimpleresponse()
{
    my $self = shift;
    my $url = $self->{url};	
    my $javapath = '/www/htdocs/vo/java/jre/bin/';
    my $tmp = "tmp.shared";
    if ($ENV{'SERVER_NAME'} !~ /heasarcdev.*/)
    { 
       $javapath = '/usr1/local/java/bin/';
       #$tmp = "tmp.shared.prod";	  
    }     



    
    $ENV{'CLASSPATH'} = "/www/htdocs/vo/vaomonitor/GUITesting/iris-1.0-unix-i386/SedImporter";
    
    
    $ENV{"PATH"} = "/www/htdocs/vo/vaomonitor/GUITesting/iris-1.0-unix-i386/lib/importer/SedImporter.jar:"
	          ."/www/htdocs/vo/java/jre/bin/java:"
	          ."/usr/contrib/linux/bin/:/bin:/usr/bin:/bin/bash:/www/htdocs/vo/vaomonitor/GUITesting/iris-1.0-unix-i386/SedImporer:"
	         ."/www/htdocs/vo/vaomonitor/GUITesting/iris-1.0-unix-i386/:/usr/ucb:/usr/bin/X11:/usr/etc:"
	         ."/usr/bin:/usr/local/bin:/usr1/local/bin:/bin:/heasarc/bin:/usr/local/web_chroot/heasarc/sybase/OCS/bin:/software/jira/ant/ant/bin";

    
    my $votpath = "/$tmp/vo/vaomonitor/irisplugins/";
    if (-e "$votpath/out.vot") 
    { 
	chmod (0775,"$votpath/out.vot");
	unlink "$votpath/out.vot";
    }
    
    my $p = "/www/htdocs/vo/vaomonitor/GUITesting/";
    my $pa  = "$p/iris-1.0-unix-i386";
  
     
    my $cmd = "$javapath/java  -Duser.home=$votpath -jar /www/htdocs/vo/vaomonitor/GUITesting/iris-1.0-unix-i386/lib/importer/SedImporter-1.0.jar 2>/dev/null";
  
    my $pr =  `$cmd  $p/config_extra.ini $votpath/out.vot vot`;
    
    my $res = do {local $/; local @ARGV = "$votpath/out.vot"; <>};
    
    return $res;
   
}
1;
