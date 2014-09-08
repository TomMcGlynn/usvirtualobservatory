#
# Author:      Michael Preciado
#
# Description: Gets the status of an individual 
#              VO service. Accepts an ivo id as input
#              and matches this id to nagios service
#              status information.
#
#
#
#
#
#

package Tools::VOServicestat;

use strict;
use lib '/www/htdocs/cgi-bin/lib/nagios/lib/perl';

use lib '/www/htdocs/cgi-bin/W3Browse/lib';

use vars qw($log);      

use  Nagios::StatusLog;
$log = Nagios::StatusLog->new
    ( Filename => "/www/htdocs/cgi-bin/lib/nagios/var/status.dat",
         Version  => 2.4); 

########################################
# subroutine to get status of a service 
#########################################
sub get_stat
{
    my ($ivoid,$cgi)  = @_;
    
    my $status;
    my $voservice = "/www/htdocs/cgi-bin/lib/nagios/etc/voservices.list"; 
    my $map  =  map_service_ivo_id($voservice,$cgi);

    #get service/service details hash
    my $services_details = filter_status_log($log,$map,$cgi);

    my ($services,$assessment)  = determine_matching_extent($ivoid,$cgi,$map);	

    #if no services found, return 
    if ($assessment eq "match=none")
    {
	return $assessment;
    }

    foreach my $n (@$services)
    {
	#print $cgi->body("S:$n<br>");
    }

    #if services found, get status information and overall assessment 
    $assessment = get_status($services,$services_details,$assessment,$cgi);
    return $assessment;
}
############################################
# get status for ivoid services 
############################################
sub get_status
{
    my($servicenames,$s_details,$assessment,$cgi) = @_; 
    my %service_details      = %$s_details;
    my @services             = @$servicenames;
    my $denominator               = scalar(@services);
    my $numerator            = 0;
    my $info_hash            = {};
    #print $cgi->body("You have $denominator services<br>");
    foreach my $service (@services)
    {
	my $service_stat  = $service_details{$service};
	my $status        = $service_stat->current_state;
	my $status_english;
	#print $cgi->body("S: $service: $status<br>");
	if ($status == "0")
	{
	    $status_english = "OK";
	    $numerator++;
	}
	else
	{
	    $status_english = "Failed";
	}
	$info_hash->{$service} = $status_english;	   
    }

    my $fraction  = $numerator/$denominator;
    #print $cgi->body("Your fraction of goodness is: $fraction<br>");
   
    if ($fraction == "1")
    {
	$assessment .= ";status=up";
    }
    elsif (($fraction < 1) and ($fraction > 0))
    {
	$assessment .= ";status=mixed";
    }
    elsif ($fraction == "0")
    {
	$assessment .= ";status=down";
    }
    return $assessment;   
}
############################################
# sub get service names for ivo ids entered
############################################
sub determine_matching_extent
{
    my ($ivoid,$cgi,$map_hash) = @_;
    my @array;
    my ($match,$partial_match);
    my @iv   = (split /\b\//,$ivoid);
    my $basename = $iv[0];
    #print  $cgi->body("C:$basename<br>");
    my %map = %$map_hash;

    #try to get an exact match
    while (my ($service,$id) = each (%map))
    {	
	if ($ivoid eq $id)
	{
	    #print $cgi->body("P:You matched<br>");
	    push @array, $service;
	    $match = "match=exact"; 
	}
    }

    #try to get a partial match
    if (! $match)
    {
        while (my ($service,$id) = each (%map))
	{
	    #see if there is a partial match and store
	    my $result = index($id,$basename);
	    if ($result != "-1")
	    {
		push @array, $service;
		$match = "match=authority";
	    }
	}
    }
   
    # give up 
    if (! $match)
    {	    
	$match = "match=none";
    }  
    return \@array,$match;
}
##########################################
# filter status log file
##########################################
sub filter_status_log
{
    my($log,$map,$cgi) = @_;
    my $services_details = {};
    
    #$cgi->body("H:$host<br>");
    #my $h_service = {};


    my @hosts = $log->list_hosts;
    foreach my $host (@hosts)
    {
	my @host_services = $log->list_services_on_host($host);
	foreach my $service (@host_services)
	{           
	    if ((($service  eq "SIMBAD") or ($service eq "NED")) and ($host eq "heasarcdev"))
	    { 
		next;
	    }	 
	    if (exists($$map{$service}))
	    {	       
		my $service_stat = $log->service($host,$service);
		$services_details->{$service} = $service_stat;		
	    }
	}
    }
    return $services_details;
}
############################################
# get map of ivo identifiers to service test
############################################
sub map_service_ivo_id
{
    my ($voservice,$cgi) = @_;
    my %map;
    open (File,"$voservice")|| die "cannot open voservice.list file\n";
    while(<File>)
    {
	my $line = $_;
	chomp $line;
	next if ($_ =~ /^\#(.*)/);
	next if ($_ =~ /^$/);
	my  @array = (split /\|/,$line);
	#print "JJ: $line";
	next if ($array[4] =~ /^$/);
   
	#print $cgi->body("$array[2]: $array[4]<br>");
	my  $ivo = trim($array[4]);
	my $name = trim($array[0]);
	$map{$name}     = $ivo;       
    }
    close File;
    return \%map;
}
#################################################
# trim string
################################################
sub trim
{
    my ($string) = @_;
    $string =~ s/^\s+//g;
    $string =~ s/\s+$//g; 
    return $string;
}
1;
