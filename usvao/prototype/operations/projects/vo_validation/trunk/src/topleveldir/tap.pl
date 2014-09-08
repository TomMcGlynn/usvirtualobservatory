#!/usr/bin/perl -wT
# 
# name = tap.pl
#
# script is a layer to run java. This script will validate tap services 
# using taplint.
#
#
{
    use strict;
    use lib '/www/htdocs/vo/validation';
    use lib "/www/server/vo/validation";
    use CGI;
    use data::startup;
    use HTML::ErrorMessage;
    use HTML::Layout;
    use Switch;
    my $cgi = CGI->new();
    
    
    my $url =  detaint("url",$cgi->param("url")) if ($cgi->param("url"));   
  
    print "Content-type: text/plain\n\n";  
    print "Testing TAP service using stages = 'TME UWS QGE QPO QAS'";
    my $cmd = "/usr1/local/java/bin/java  -jar ./javalib/stilts.jar taplint stages='TME UWS QGE QPO QAS' report='EW' tapurl=\'$url\'";
    eval {exec($cmd); };
    print "Error executing test. Error is: $@"  if ($@);
    
}
sub detaint
{
    my ($param,$value) = @_;
    
    my $status = 1;
    switch($param)
    {
	case 'url'
	{
	    if ($value =~ /^(http:.*)$/)
	    {
		$value = $1;
		$status= '0';
	    }	        		
	}
    }
    if ($status ==  '1')
    {      
        my $error = new HTML::ErrorMessage("The parameter or value entered is not recognized");
        $error->display();
	exit();
    }
    return $value;
}
