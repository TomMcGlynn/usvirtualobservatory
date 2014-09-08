#!/usr1/local/bin/perl5 -w
#
#
#
#
# Description: tests the Importer service (IRSA). Used
#              to test the service in real time by a user
#
{
    use strict;
    use lib '/www/htdocs/cgi-bin/lib/nagios/lib/perl';
    use lib '/www/htdocs/vo/voamonitor/'; 
    use lib '/www/htdocs/cgi-bin/W3Browse/lib';
   
    use URI::URL;
    use XML::SimpleObject::LibXML;
    use LWP::UserAgent;
    use HTTP::Request;
    use HTTP::Response;
    use HTML::Form;
    use HTML::TreeBuilder;
    use CGI;    
    use Socket;
    use Tie::IxHash;
    use lib '/www/htdocs/cgi-bin/lib/heasarc';
    my $cgi = CGI->new();    
    my $debug = $cgi->param("debug");
	
    my ($status,$content)  = run_test();
    if 	(($debug) and ($debug == 0))
    {
	print "Content-type: text/html\n\n";
	 print $status;
    }
    else
    {
      print "Content-type: text/xml\n\n"; 
      print $content; 
    }

}
################################################
#run test on service 
################################################
sub run_test
{
   

    #get start time
    my $start_time        = time();

    my $url = "http://irsa.ipac.caltech.edu/applications/TblIngest/";
   
    my $ua                = LWP::UserAgent->new;
    my $response          = $ua->get($url);
    my $content           = $response->content;
    #print "C: $content";

    my @nvo_importer      = HTML::Form->parse($response);
    my $form              = shift @nvo_importer;

    #specify file for upload
    my $file_to_upload = 'nvo_upload';
    foreach my $n ($form->inputs) {
	if ($n->type eq "file")	{
	    $n->value("/www/htdocs/vo/vaomonitor/data/$file_to_upload");
	}
    }

    my $req = $form->click;
    my $res = $ua->request($req);
    my $resnewcont = $res->content;   
    return unless $resnewcont;

    my $tree = HTML::TreeBuilder->new_from_content($resnewcont);
    my @urls = $tree->look_down('_tag','a', 'href', qr/${file_to_upload}\/votbl.xml/); 
    my $a    = shift(@urls);
    return unless ref($a);
    my $urlext = $a->attr_get_i('href');
    return unless $urlext;

    #build final url and call it to get the VOTable response.
    my $final_url = $urlext;     
    my $final_res = $ua->get($final_url);
    my $c = $final_res->content;
#	print $final_res->code;
     if ($final_res->code == 200)  
    {

      my @data = $final_res->content;
      my @grepout = grep (/<TABLEDATA>/,@data);
	my $status = "fail";	
        if (@grepout)	
	{
	      $status = "pass";
	}
        return $status, $c;	
	 
    }

}
