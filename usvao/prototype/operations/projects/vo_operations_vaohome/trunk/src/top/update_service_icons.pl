#!/usr1/local/bin/perl5 -w
#
# name: update_service_icons.pl
#
# description: script updates the service color icons used
#              by the external usvaohome page. This script 
#              runs hourly and updates the symbolic links that
#              point to one of the following color icons: green, red, or
#              yellow. The links and the gif files should be in 
#              an area that is writeable by the outside world 
#
#              This script needs to have access to two files. One is an
#              xml file containing the status of each vao service and the other contains
#              the gif file names associated with each service.
#             
#
#
{
    use strict;   
    my ($tmp,$wwwdir);

    BEGIN
    { 
	if (exists $ENV{'GATEWAY_INTERFACE'})
	{	
	    $tmp =  "/tmp.shared/";
	    $wwwdir = "/www/";
	    use lib "/www/server/vo/usvao"; 
            use CGI;
            print "Content-type: text/html\n\n"  if (! exists  $ENV{HTTP_REFERER});
	    
	}
	else 
	{ 
	   use lib "/www/server/vo/usvao/";   
	   $wwwdir = "/www/";
	   $tmp = "/tmp.shared/"; 
       }
    }   
    use lib "/$wwwdir/htdocs/vo/usvao";
    use data::startup;    
    use XML::SimpleObject::LibXML; 
    use LWP::UserAgent;
    use Switch;
    use DBI;
    use File::Copy;    
    use Connect::MySQLVaoOperationsDB;
    use SQL::Queries;
    
    #connect to db.
    my $dbh = vao_operations_connect();
    
    #stringify service status file    
    my $contents = do { local $/; local @ARGV = "/$wwwdir/server/vo/vaomonitor/usvaohome/status.xml"; <> };
  
    #store primary vao service names,soft link names
    my $hash ={};
    open (P, "./data/map_icons");   
    while (<P>)
    {
      chomp $_;
      my @a = split (/\|/,$_);
      $hash->{$a[0]} = $a[1];  
    }
    close P;
    

    #get current notices     	
    my $notes_array  = load_notices($dbh,"vao_notices","currentnotes");    
           
    #parse xml containing notices and hashify
    my $notices  = parse_notes($notes_array);
        
    #get color icon for each service and create link
    chdir "/$tmp/vo/usvao/icons";

    foreach my $servicename (keys %$hash)
    {	
	my $icon =  get_icon_type($contents,$servicename,$notices);
	unlink "./$hash->{$servicename}" 
	    || die "cannot delete\n";
	symlink("$icon","$hash->{$servicename}")
	    || die "cannot create soft link\n";	
    }    
}
sub parse_notes
{
    my ($notes_array) = @_;
    my $hash = {};
    foreach my $n (@$notes_array)
    {
	my @a  = (split /;/, $n->[7]);
	foreach my $c ( @a)
	{
	    $hash->{$c} = $n->[2];  
	}
    }
    return $hash;
}
sub get_icon_type
{
    my ($contents,$servicename,$notices) = @_;
    my $hash = {};
    my $xmlobj = new XML::SimpleObject::LibXML(XML=>$contents);
    my $table  = $xmlobj->child("TABLE");
    
    my @services = $table->children("Service");
    foreach my $service (@services)
    {	
	my @ch     = $service->children();
	my $name   = $ch[0]->value;	
	my $status = $ch[1]->value;

	if ($name eq $servicename)
        {
	    if (exists ($notices->{$name}))
	    {
		$status = 'notice_green' if ($status ne "Fail");
		$status = 'notice_red' if ($status eq "Fail");
	    }	   
	    my $icon = get_gif($status);
	    return $icon;
	}	
    }
}
sub get_gif
{
    my ($status) = @_;
    
    my $gif;
    switch ($status)
    {	
	case "OK"           { $gif = "greensun.png";}
	case "Fail"         { $gif = "redsun.png";}
	case "notice_green" { $gif = "greensun_ring.png";}
	case "notice_red"   { $gif = "redsun_ring.png";}
    }
    return $gif;
}
