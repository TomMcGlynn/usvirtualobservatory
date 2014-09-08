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
#             |time now | time(1.5h ago) | send mail | retest |send mail after retest|  
#             |         |                |           |        |                      |
#  Service A  |  pass   |    fail        |    yes    |   no   |         ----         | 
#             |         |                |           |        |                      |
#  Service B  |  fail   |    pass        |    ----   |   yes  |         yes*         |
#             |         |                |           |        |                      |
#  Service C  |  pass   |    pass        |     no    |   no   |         ----         |
#             |         |                |           |        |                      |
#  Service D  |  fail   |    fail        |     no    |   no   |         ----         |
#
#   * only if tests are still failing
#  
#   *All services that have a 'lastval' either have a pass/fail or fail/pass
#    status for the current and last recognized tests. There are some test results
#    that this code will recognize and others that it will not. 
#    
#   *Recognized test results - This script will recognize only certain test results. After 
#    all tests are run by the java code, if any have failed, they are retested 5-10 minutes later by the
#    "rerun_tests.pl code". These results are stored in the local database. The key here
#    is that the timestamp of the retest will be within a few minutes of the last test. 
#      
#    E.g. The following is for the VO Inventory Service test. There are two tests for the
#    Inventory Service (testid 47 and 48)
#
#mysql> select * from Testhistory
#+-------+-----------+--------+---------------+---------------------+
#| runid | serviceId | testid | monitorstatus | time                |
#+-------+-----------+--------+---------------+---------------------+
#|     1 |        32 |     47 | fail          | 2011/11/24 12:50:00 | 
#|     2 |        32 |     48 | fail          | 2011/11/24 12:50:00 | 
#|     3 |        32 |     47 | pass          | 2011/11/24 13:54:00 | -penultimate test  (code sees this from 1+ hours ago)  
#|     4 |        32 |     48 | pass          | 2011/11/24 13:54:00 | -penultimate test  (code sees this from 1+ hours ago)
#|     5 |        32 |     47 | fail          | 2011/11/24 14:50:30 | -last test    (code ignores this)  
#|     6 |        32 |     48 | fail          | 2011/11/24 14:50:30 | -last test    (code ignores  this)
#|    11 |        32 |     48 | pass          | 2011/11/24 14:55:30 | -retest results (code sees *this* as the last test)
#|    10 |        32 |     47 | pass          | 2011/11/24 14:55:30 | -retest results (code sees *this* as the last test)
#+-------+-----------+--------+---------------+---------------------+
#8 rows in set (0.00 sec)
#
#  This script will proceed based on what it thinks are the current status
#  and the penultimate status. It will see runids 10 and 11 as the current tests. It will
#  then see runids 3 and 4 as the penultimate tests.The code will not send out a message in this
#  case since all tests (penultimate and current) have been passing from its point of view.  
#

{
    use strict;
    use lib '/www.prod/server/vo/external_monitor';
    use lib '/www.prod/htdocs/vo/external_monitor/';
    use DBI;
    use LWP::UserAgent;
    use Connect::MySQLVaomonitorDB;
    use Connect::MySQLVaoOperationsDB;
    use SQLVo::Queries;
    use Mail::Mailer;
    use Mail::VOMailer;
    use XML::ParseTable;

    use data::startup; 
    
    my $dbh = vaomonitor_connect();
    my $opsdbh = vao_operations_connect(); 
    my $con = {
	        'dbh' => $dbh,
	      };

    
    #get status of each service
    my $set  = get_current_status($con);
    
    my $size = keys %$set;
    
    #get contact info for each service
    my $emails = get_email_addresses($opsdbh);
    my $email  =  {};
    
    foreach my $n (@$emails)
    {	
	$email->{@$n[0]} = @$n[1];	
    }
    
    #store last validation status for each 
    storemorehistory($con,$set);

    #run all and mail
    process($set,$email); 

}

sub process
{

    my ($set,$email) = @_;
    
    #send mail on those that are up 
       
    
    foreach my $service (keys %$set)
    {	
	if (($set->{$service}->{overall} eq 'pass') and ($set->{$service}->{lastval} eq 'pass'))	       
	{	
	    #do nothing
	}
	elsif (($set->{$service}->{overall} eq 'fail') and ($set->{$service}->{lastval} eq 'pass'))
	{
	    #send mail on services that crashed... 
	    my $mailer = new Mail::VOMailer($service,$set->{$service},"crash",$email->{$service});
	    $mailer->send_mail;
	}
	elsif (($set->{$service}->{overall} eq 'pass') and ($set->{$service}->{lastval} eq 'fail'))	 
	{	
	    #print "the last service was not passing, this one is,sending is up message\n";
	    #send mail on services that are up again
	    
	    my $mailer = new Mail::VOMailer($service,$set->{$service},"pass",$email->{$service});
	    $mailer->send_mail;	       
	}
	
    }
   
    
}
sub storemorehistory
{
    my ($con,$set) = @_;

    #see if the failing services have failed in the last hour too 
    foreach my $service (keys  %$set)
    {	

	my $status = get_last_validation_status($con,$service,"getPenultimateStats");
	
	$set->{$service}->{lastval} = "fail" if ($status eq 'fail');
	$set->{$service}->{lastval} = "pass" if ($status eq 'pass'); 	
     
	#print "the last recognized test did not crash\n";	    
	
    }
} 
sub get_last_validation_status
{
    my ($con,$service,$query)  = @_;
    my $array = &$query($con,$service);

    #pass is the default. If service was never
    #tested in the system, a "pass" is returned for
    #the last test.
    my $status = "pass";   
    
    if (scalar(@$array)  > 0)
    {	
	#some status was returned by the query and status variable should
	#be initialized
       
	foreach my $n (@$array)	
	{
	    if ((@$n[2] eq 'abort') or (@$n[2] eq 'fail'))
	    {
		$status   = 'fail';
		last;
	    }      
	}
    }     
    return $status;
}
sub display
{
    my ($hash ) = @_;

    my $size = keys %$hash;
    foreach my $n (sort keys %$hash)
    {	
	
	my $service  = $hash->{$n};
	my $newsize = keys %$service;
      
	foreach my $r (keys %$service)
	{
	    next if (($r eq 'overall') || ($r eq 'lastval'));
	    
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
	my $runid    = @$n[7];
	#my $match   = @$n[8]  if (@$n[8]);
	
	
        if (($status eq 'abort') or ($status eq 'fail'))
	{
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
    }
    foreach my $name (keys %$set)
    {
	if (! exists ($set->{$name}->{overall}))
	{
	    $set->{$name}->{overall} = 'pass';
	}
    }

    return $set;  
}
