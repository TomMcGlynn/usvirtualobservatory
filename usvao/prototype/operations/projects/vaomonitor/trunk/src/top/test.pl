#!/usr/bin/perl -wT
#
#   name: test.pl (this should be renamed)
#   
#   author: michael preciado
# 
#   description: this perl program is responsible for driving the
#                 specific test on a VO service. The name of the
#                 test is first found in the local db and then this
#                 script will execute it.
#
#   notes: the getJavaResults routine has not been implemented and
#          should be ignored. This script may at some point in the
#          future drive Java tests but right now it is pure perl.
#
{
    use strict;
    use lib '/www/htdocs/cgi-bin/W3Browse/lib';
    use lib "./";
    use lib "/www/server/vo/vaomonitor";
    
    use CGI;   
    use data::startup;
    use URI;
    use DBI;
    use HTML::Table;
    use HTML::ErrorMessage;
    use XML::Table;
    use HTML::Layout;
   
    use Types::SimpleURLResponse;
    use LWP::UserAgent;
    use vars qw($homedir);
    use File::Basename qw(dirname);
    use Connect::MySQLVaomonitorDB;
    
    use SQLVao::Queries;
    use Switch;
    
   BEGIN
   {      
       $homedir = dirname($0);
       push  @INC, "$homedir/Types";   
   }	
    
    
    my $cgi    = CGI->new();
    
    
    my $name        = detaint("name",$cgi->param("name"))                   if ($cgi->param("name"));
    my $testid      = detaint("testid", $cgi->param("testid"))              if ($cgi->param("testid"));
    my $getresponse = detaint("getresponse",$cgi->param("getresponse"))     if ($cgi->param("getresponse")); 
    my $testresult  = detaint("testresult", $cgi->param("testresult"))      if ($cgi->param("testresult"));

    my $dbh = vaomonitor_connect();   
   
    if ($name && $testid)
    {   	    
	my $arrayref = getTestUrls($dbh,$name,$testid);
	unless ($arrayref) 
	{
	    require HTML::ErrorServiceNotFound;
	    my $error = new HTML::ErrorServiceNotFound("Servicename or testid not found");
	    $error->display();
	    exit();
	}
	my $entry  = shift  @$arrayref;
	
	my ($url, $mimetype, $classname,$testname,$notAvailable, $params) = @$entry;
	    
	#print "Content-type: text/plain\n\n";
    
	#print "@INC : $classname";
	my $name = "Types::$classname";
	$name = "Types::Skip" if ($notAvailable);
	
	#print "JJ : $mimetype,$classname,$testname,  $params,$name<br>";
	eval "require $name";
	my $uniquetest = new $name($url,$testname,$params);


	#my @results = getJavaResults($classname,$getresponse,$url);

        if (($getresponse ) && ($getresponse eq 'yes'))
	{
	    print "Content-type: text/$mimetype; charset=ISO-8859-1\n\n";	    
	    #print "Content-type: text/html\n\n";   
	   
	    $uniquetest->showresponse();	    
	}
	elsif (($testresult) && ($testresult eq 'yes'))
	{
	    print "Content-type: text/xml;  charset=ISO-8859-1\n\n";
            $uniquetest->test("getxml");
	}
	else
     	{		    
	      print "Content-type: text/html\n\n";	    
	      $uniquetest->test();		
	}	
    }
    else
    {
	my $error = new HTML::ErrorServiceNotFound("name or test not defined");
	$error->display();
    }
}
sub getJavaResults
{
    #my ($classname, $getresponse, $url) = @_;
    #print "$classname<br>$homedir";
    #my @results =  `java  -cp /web_chroot/.www_mountpnt/www/htdocs/vo/vaomonitor/java/ $homedir/java/RunTest $classname $url $getresponse 2>&1`;  
    #print "@results";
    
}
sub detaint
{
    my ($parname,$value) = @_;
    
    my $status; 
    switch ($parname)
    {
	case "name"  
	{
	    if ($value =~ /^([A-Za-z0-9\s+\/\%\+]*[^\<\>\;])$/) {$value = $1;}   
	    else {$status = 1;}
	}
        case "testid" { if ($value =~ /^([A-Za-z0-9\s+\/\%\+]*[^\<\>\;])$/){$value = $1;}   else { $status =1;}}
	case "getresponse" 
	{
	    if ($value  =~ /^(yes)$/){ $value = $1;}
	    else {$status = 1;}
	}
	case "testresult"  { if ($value =~ /^(yes)$/) {$value = $1;} else {$status =1;}}
    }
    if ($status)
    {
        my $error = new HTML::ErrorMessage("The parameter or value entered is not recognized");
        $error->display();
        exit();
    }
    return $value;

}



