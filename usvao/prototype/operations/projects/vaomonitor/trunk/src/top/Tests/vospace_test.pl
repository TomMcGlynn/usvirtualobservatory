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
    my $database     = $cgi->param("test");
 
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
###############################################
#
###############################################
sub run_node_test
{
    my ($url)  = @_;
    #print "$database<br>";
   
    my $message = '<soap:Envelope
             xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
             xmlns:xsd="http://www.w3.org/2001/XMLSchema" 
             xmlns:soap="http://schemas.xmlsoap.org/soap/envelope/">
             <soap:Body>
             <GetViews  xmlns = "http://www.ivoa.net/xml/VOSpaceContract-v1.1rc1" />
             </soap:Body> 
             </soap:Envelope>';
    

    my $ua       = LWP::UserAgent->new(); 
    my $request  = HTTP::Request->new(POST => $url);
    $request->header(SOAPAction => '"http://www.ivoa.net/xml/VOSpaceContract-v1.1rc1/GetViews"');
    $request->content($message);
    $request->content_type("text/xml; charset=utf-8");
  
    #print response
    my $response = $ua->request($request); 
    print $response->as_string; 
}


