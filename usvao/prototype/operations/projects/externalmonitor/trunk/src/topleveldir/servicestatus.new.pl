#!/usr/bin/perl -wT
#
#
#
#
{
    use strict;
    use CGI;
    use LWP::UserAgent;
    use XML::SimpleObject::LibXML;
    use Switch;
    print "Content-type: text/html\n\n";
    my $cgi = CGI->new();

    #get input pars
    my $name = $cgi->param("name");
    my $n    = $cgi->param("notes");    
    exit if ( (!$name) || (!$n));
    exit if (($name|$n)  =~ /[<>\/]/); 
    
    
    #stringify file    
    my $contents = do { local $/; local @ARGV = "./$name"; <> };
    
    #default xml container
    my $notes = "<ServiceNotes></ServiceNotes>";
    if ($n eq 'yes')
    {
	$notes    = do { local $/; local @ARGV = "./notes.xml"; <> };	
    }
    
    #parse xml,store status of each service 
    my $results =  get_set($contents);
    
    #only include primary services
    filter_results($results);
    
    #parse xml and hashify notes
    my $notices  = parse_notes($notes);
    
    #process and emit necessary html
    process($results,$notices);
}
sub parse_notes
{
    my ($notes) = @_;    
    my $hash    = {};
    my $xmlobj  = new XML::SimpleObject::LibXML(XML=>$notes);
    my $table   = $xmlobj->child("ServiceNotes");
    
    my @entries = $table->children("Entry");
    foreach my $entry (@entries)
    {
	my $note      = $entry->child("Note");
	$hash->{$note->value} = '1';	
    }
    return $hash;    
}
sub filter_results
{
    my ($results) = @_;
    #remove irrelevant services 
    my @array = ('SIMBAD', 'Registry Validator','Image Validator',
		 'Table Search Validator','VOResource Validator','NED',
		 'VO Validation Service','SkyAlert', 'Service Registration',
		 'VAO Portal Test Version'
		 );
    foreach my $n (@array)
    {
	delete $results->{$n};
    }
}
sub process
{
    my ($results,$notices) = @_;
    
    #get number of notices
    my $number_of_notices = keys %$notices;
    print "<html><head><link rel='stylesheet' href = '/vo/vaomonitor/css/vao.css'>"
          . "<body><table class = 'tac'>";
    
    foreach my $n (keys %$results)
    {	
	print  "<tr class= greenlnextra><td>$results->{$n}->{dot}</td><td>$n</td>";
    }
    print "</table></body>";
    #html page url
    my $page = "http://heasarc.gsfc.nasa.gov/vo.test/vaomonitor/vaodb.pl?format=html";
    
    
}
sub get_services_down
{
    my ($results) = @_;
    my $array = [];
    foreach my $n  ( keys  %$results)
    {
	if ($results->{$n}  eq 'Fail')
	{
	    push @$array,$n;
	}
    }
    return $array;
}
sub all_up
{
    my ($miniresults) = @_;
    my $total  = keys %$miniresults;
    my @values = values %$miniresults;
    my $count = grep $_ eq 'OK', @values;
    return '1' if ($total eq $count);    
}
sub get_set
{
    my ($contents) = @_;
    my $hash = {};
    my $xmlobj = new XML::SimpleObject::LibXML(XML=>$contents);
    my $table  = $xmlobj->child("TABLE");
    
    my @services = $table->children("Service");
    foreach my $service (@services)
    {
      
	my @ch     = $service->children();
	my $status = $ch[1]->value;
	my $dotcolorurl =  get_dotcolor($status);
	$hash->{$ch[0]->value} =   { 
	                             'status' => $ch[1]->value,
				     'dot'    => "$dotcolorurl",					 
				   };
    }
    return $hash;
}
sub get_dotcolor
{
    my ($status) = @_;
    my $color;
    switch ($status)
    {	
	case "OK"   { $color = '<img src  = /vo/vaomonitor/greendot.gif>';}
	case "Fail" { $color = '<img src  = /vo/vaomonitor/reddot.gif>';}
	case "note" { $color = '<img src  = /vo/vaomonitor/yellowdot.gif>';}
    }
    return $color;
}
