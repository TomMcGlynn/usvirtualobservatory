#!/usr/bin/perl5.8.8 -w
#
#
# name: test_vaomonitor.pl
#
# author: michael preciado
#
# description: this script currently runs at nrao and tests the vaomonitor home page at GSFC. If the page is
#              not responding, an alert message will be sent to the appropriate parties. 
#
# extra: currently this script can only be accessed by connecting to "vaoops1.aoc.nrao.edu"
#
 
{ 
    use vars qw($timeout);
    #open (File, "./status");

    #my $last_status = do {local $/;<File>};
    
    my $last_status;
    open (File, "./status");
    while (<File>)
    {
        chomp $_;
        $last_status = $_;
    }
    close File;
    my @params;
   
    #send mail if timeout exceeded
    $timeout= 60;
    $SIG{'ALRM'} = sub {
                         if (($last_status eq 'up') ||  (((localtime(time))[2]) == '23'))
                         {
                             send_message("Alert: VAOMonitor", "Vaomonitor test exceeded $timeout second timeout. The page appears to be unavailable.","down"); 
                             update_status("down");
                         }
                         exit(1);
                   };    
    alarm($timeout);
    

    #test vaomonitor using wget    
    my $output  =  ( `/usr/bin/wget  -O- 'http://heasarc.gsfc.nasa.gov/vo/vaomonitor/vaodb.pl?format=xml' -nv  2>&1`);
    
    #see if there is an error
    my @c = grep (/ERROR/,$output);
    if (@c)
    {
        #down message params
        @params = ( "Alert: VAOMonitor", "The VAOMonitor service is down","down");

        #do nothing if last status was down and not 11 p.m. 
        if (($last_status eq 'down') and (((localtime(time))[2]) != '23'))
        {
            exit(1);        
        }
    }
    else
    {
       #set 'up' message params, don't do anything if up at last check   
       @params = ("Restored: VAOMonitor","The VAOMonitor page is back up","up");       
       exit(1) if ($last_status eq 'up');
    }
    #messages will be sent when service goes down or up; also at 11 p.m.if status is down 
    send_message($params[0],$params[1]);
    update_status($params[2]);

}
sub update_status
{     
       my ($status)  = @_;
       open (File, ">./status") || die "cannot open status file\n";
       print File "$status";
       close File ||die "cannot close status file\n";
}
sub send_message
{
    my ($subject,$message) = @_;
    my $to      = "all-notices\@usvao.org";
    #my $to     = "michael.e.preciado\@nasa.gov";
    my $from    = "operations\@usvao.org";
    
    
    open (Mail,"|/usr/sbin/sendmail -t");    
    print Mail  "To: $to\n"
               ."From: $from\n"
               ."Subject: $subject\n\n"
               ."$message\n\n";
    close Mail;
}

