#!/usr1/local/bin/perl5 -w
#
# Author: Michael Preciado
#
# name: run_crashmail.pl
#
# notes:
#     -gets service tests from database
#      with the status of each.
#     -sends mail based on the following 
#      scheme
#
#

{
    use strict;
    use lib '/www/htdocs/vo/external_monitor';
    use lib './java_automonitor';
    use lib '/www/server/vo/monitor_test'; 

    use LWP::UserAgent;
    use DBI;
    use Connect::MySQLVaomonitorDB;
    use File::Basename qw(dirname);
    use Connect::MySQLVaoOperationsDB;
    use SQLVo::Queries;
    use Mail::Mailer;
    use Mail::VOMailer;
    use XML::ParseTable;
 
    use data::startup; 
  
    
    my $homedir;
    BEGIN
    {
        $ENV{'CLASSPATH'} =
"/usr1/local/javasdk/bin/java:/www/htdocs/vo/external_monitor/java_automonitor/:/software/jira/software/class:/www/server/vo/mysql-connector-java-5.1.7/mysql-connector-java-5.1.7-bin.jar:/www/htdocs/vo/external_monitor/";
        $homedir = dirname($0);
	
    }
    
    my $dbh = vaomonitor_connect();

    my $con = {
	        'dbh' => $dbh,
	      };

    
    #get status of each service
    my $set  = get_current_status($con);
    
    my $size = keys %$set;
    print "Services: $size total\n";
       
    process($con,$set); 

}
sub process
{

    my ($con,$set) = @_;
    
    #retest and store results;
    `/usr1/local/bin/javac /www/htdocs/vo/external_monitor/java_automonitor/*java`; 
    my $array = [];
    foreach my $service (keys %$set)
    {	
	if (exists $set->{$service}->{overall})
	{
	    print "Should not be nno\n"; 
	    my $call = "/usr1/local/bin/java  RunMonitor \'$service\' T";
	    system($call);	    
	}
	else
	{
	    update_service_entry($con,$set->{$service});
	}
    }   
}
sub update_service_entry
{
    my ($con,$service) = @_;
    updateTable($con,$service);
    


}
sub display
{
    my ($hash ) = @_;

    my $size = keys %$hash;
    print "SIZE: $size\n";
    foreach my $n (sort keys %$hash)
    {	
	
	my $service  = $hash->{$n};
	my $newsize = keys %$service;
	print "QQ: $newsize\n";
	foreach my $r (keys %$service)
	{
	    next if (($r eq 'overall') || ($r eq 'lastval'));
	    
	    print "TT: $service->{$r}->{status}";
	    #print "TT: $service->{$r}->{time} $service->{lastval}\n";
	    #print "Contains: $n: $hash->{$n}->{time}\n";	
	}
	print "\n";
    }
}
sub get_current_status
{
    my ($con) = @_;
    my $set = {};
   
    my $array = getcurrentHealth($con);
    foreach my $n (@$array)
    {
	my $name     = shift @$n;
	my $testid   = @$n[1];
        my $authid   = @$n[2];
	my $testname = @$n[3];
	my $status   = @$n[5];
       print "S: $status\n";
	my $runid    = @$n[7];
	#my $match   = @$n[8]  if (@$n[8]);
	
        if (($status eq 'abort') or ($status eq 'fail'))
	{
           print "THIS IS great\n";
	    $set->{$name}->{overall}           = "fail";
	    #get error associated with run
	    my $a                              = getErrorMessage($con->{dbh},$runid);
	    my $j                              = shift @$a;	    
	    my $error                          = shift @$j;	    
	    $set->{$name}->{$testname}->{errormessage}  = $error;
	    
	}
	$set->{$name}->{$testname}->{testid}   = $testid;
	$set->{$name}->{$testname}->{status}   = $status;
	$set->{$name}->{$testname}->{time}     = @$n[6];
	$set->{$name}->{$testname}->{url}      = @$n[8];
	$set->{$name}->{$testname}->{params}   = @$n[9];
 	$set->{$name}->{$testname}->{runid}    = $runid;
    }
    return $set;  
}
