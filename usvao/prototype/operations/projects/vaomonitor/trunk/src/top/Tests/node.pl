#!/usr1/local/bin/perl5 -w -T
# 
# Title: getservicestat.pl  
# Author: Michael Preciado
# 
# Description: Adds a way to test Databases (nodes) to the VO monitor 
#              tool.
#
#
{
    use strict;
    use lib '/www/htdocs/cgi-bin/lib/nagios/lib/perl';
    use lib '/www/htdocs/cgi-bin/vo/monitor/';
    use lib '/www/htdocs/cgi-bin/W3Browse/lib';
    use lib '/www/htdocs/vo/monitor/';
    use CGIVO::VOMonutil;
    use URI::URL;
    use XML::SimpleObject::LibXML;
    use LWP::UserAgent;
    use HTTP::Request;
    use HTTP::Response;
   
    use CGI;    
    use Socket;
    use CGIVO::VOServicestat;
    use Tie::IxHash;
    use lib '/www/htdocs/cgi-bin/lib/heasarc';
  
         
    my $cgi          = CGI->new();     
    my $database     = $cgi->param("database");
 
    print "Content-type: text/plain\n\n";
    if ($database)
    {
        run_node_test($database);    
    }
    else
    {
        print "Error: Incorrect input parameter";
    }
}
#------------------------------------------------------------------------------
sub run_node_test {
    
    my($url,$type) =  @_;
    
    my $ua      = LWP::UserAgent->new();
    my $strings = get_strings();
    my $request;
    
    if (! $type)
    {	
        $request =  build_request("availability",$url,$strings);
    }
    else
    {
	$request  = build_request("table",$url, $strings);

    }
    my ($response,$restext,$restime,$size) = submit_request($request,$ua);
    
    my ($found) = test_response($response,$restext,$strings);
    if (! $found)
    {
	run_node_test($url,"table");
	exit(0);
    }
    else
    {
	print "$restext";
    }
   
}
#----------------------------------------------
sub test_response
{
    my ($response,$restext,$strings,$type) = @_;
    print "HTTP response code: ", $response->code, "\n$restext"
	if $opt_debug;
    if ($response->code == 200) {
        my @outputs     = $response->as_string;
        my @grepoutputs = grep(/$strings->{$type}->{match}/, @outputs);
        if (@grepoutputs)
	{	    
	    return("found");
        }    
    }
    return;
}
#---------------------------------------------------------
sub submit_request    
{
    my ($request,$ua) = @_;
    #get response time and response size
    my $start_time    = time(); 
    my $response      = $ua->request($request);
    my $restext       = $response->as_string;
    my $end_time      = time();
    my $restime       = $end_time-$start_time;
    my $size          = length $restext;
    return $response, $restext, $restime, $size;
}

#------------------------------------------------------------------------------
sub build_request
{
    my ($type,$url,$strings)  = @_;
     
    my $tag = $strings->{$type}->{tag};
    my $message = '<soap:Envelope
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
             xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
             <soap:Body><';
    $message .= $tag; 
    $message .=  ' xmlns = "SkyNode.ivoa.net" />
             </soap:Body> 
             </soap:Envelope>';
   
    
    my $request = HTTP::Request->new(POST => $url);
    my $value = $strings->{$type}->{tag};
    $value = "\"SkyNode.ivoa.net/$value\"";
    
    $request->header(SOAPAction => $value);
    $request->content($message);
    $request->content_type("text/xml; charset=utf-8");  
    return $request;
}
#-----------------------------------------------------
sub get_strings
{
    my $strings = {}; 
    $strings->{availability}->{tag} = "GetAvailability";
    $strings->{availability}->{match}  = "\<GetAvailabilityResponse";    
    $strings->{table}->{tag}    = "Tables";
    $strings->{table}->{match}  = "\<(ns1:)?Description\>";
    return $strings;
}
