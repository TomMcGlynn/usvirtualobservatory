#!/usr1/local/bin/perl5 -wT
#
#
# 
# Title: addnote.pl
# Author: Michael Preciado
# 
# Description:  Program adds notices to the VO monitor 
#               notices table.
#
{
    use strict;
    use lib '/www/htdocs/cgi-bin/lib/nagios/lib/perl';
    use CGI; 
    use lib '/www/server/vo/vaomonitor/';     
    use lib '/www/htdocs/vo/vaomonitor/';
    use lib './';
    use data::startup_d;
    use HTML::VOMonutil;
    use HTML::VOnote;
    use Tools::VOMailer;
    use Table::Layout;
    use Switch;
    use Connect::MySQLVaoOperationsDB;
    use Table::Message;
    use Table::ErrorMessage;
    use Table::VOheader;
    use Table::NoteDumper;
    use SQL::Queries;
    use Tie::IxHash;
    use Tools::TimeZone;
    use Tools::Tools;
    use DateTime;    
    #print "Content-type: text/html\n\n";
    my $cgi                = CGI->new();  
    my $c = {'cgi' => $cgi};
    my @linknames          = ('VO Service Notices','VAO Home','VAO Feedback');
    my $voheader           = new Table::VOheader("Add note",\@linknames);
    #db connection 
    $c->{dbh} = vao_operations_connect();

    my $id;
    ($id,$c->{encoded})      = get_data_from_cookie();
    if ($id and $id =~ m/(\d+\.\d+)/){
	$c->{sessionid} = $1; 
	gen_encodedfile($c);
	gen_encryptedfile($c->{sessionid});
	decrypt_id($c->{sessionid});	
	#check status
	my $status = check_login_status($c->{sessionid});
        if ($status eq 'in'){
           $c->{uid}   = get_user_id($c->{sessionid});
           $c->{login}  = 1; 
            cleanup($c->{sessionid},"1");
 
        }
	else {
	    print "Content-type: text/html\n\n";
            cleanup($c->{sessionid},"1");
	    delete_cookie($cgi,$c->{sessionid},$::displaynote);
	    exit();   
        }
    } 
    else
    {
	#print "Content-type: text/html\n\n";
	my $error = new Table::ErrorMessage("Error: You are not logged in yet",$c);
	$error->display();
	exit();   
    }
    
    #detaint all params
    my @z = $cgi->param;
    foreach my $n (@z){   
	my $par =  detaint($n,$cgi->param($n),$c) if ($cgi->param($n) ne ''); 
	$c->{$n} = $par;
     } 
   
    #exit if no notice
    if (! $c->{text})
    { 
	my $error = new Table::ErrorMessage("You did not enter a notice",$c);
        $error->display();
        exit();
    }    
    
    #process and store all other pars
    process_pars($c,$cgi);
    
    if (exists ($c->{text})) {		
	store_eff_removebydate($c);		
	process($c);		
    }     
   print "Location: $::displaynote\n\n";  
    exit(1);
}
sub process_pars
{
    my ($c,$cgi) = @_;
    #store user services selected in drop down list 
    my @multiple       = $cgi->param("multiple")        if ($cgi->param("multiple"));
    my @multipleother  = $cgi->param("multipleother")   if ($cgi->param("multipleother"));  
    my $affected       = [];
    my $otheraffected  = [];
    $affected          = load_multiple_services(\@multiple,"affected") if (@multiple);
    $otheraffected     = load_multiple_services(\@multipleother,"otheraffected") if (@multipleother);    
    push @$affected,'' if (! @$affected);
    push @$otheraffected,'' if (! @$otheraffected);	
    $c->{'host'} =  '' if (! $c->{host});
    $c->{affectedservices} = $affected;
    $c->{otheraffected} = $otheraffected; 
    #get current time and date
    $c->{currenttime}  = get_current_gmtime();
    escape_chars($c); 
    
    
    #encode notice,get remote host, allowed hosts
    $c->{encoded} = encode($c->{text});
    $c->{ipaddress}  = $ENV{'REMOTE_ADDR'};
    
    $c->{rhost}            = get_hostsuffixes($ENV{'REMOTE_ADDR'});
    $c->{prioritystring}   = nice_priority($c->{priority}) if ($c->{priority});
    $c->{expires}      = load_expires($c->{hourB},$c->{dayB},$c->{monthB},$c->{yearB});
    my @a = (split /\//, $cgi->param("identity"));
    $c->{identity} = pop @a; 
}
sub escape_chars
{
   my ($c) = @_;
   $c->{text} =~ s/\</&lt;/g;
   $c->{text} =~ s/\>/&gt;/g;

}
sub load_multiple_services
{
    my ($array,$string) = @_;
    my @services;

    if  (@$array)
    {
	foreach (@$array)
	{	    
	    my $m =  detaint($string, $_);
	    $m    =  trim($m);	    
	    push @services,"$m";
	}
    }  
    return  \@services;
}
sub detaint
{
    my ($parname, $value,$c) = @_;
    my $status;
    switch($parname)
    {
	case "text" 
	{ 
	    #if ($value =~  m/^([A-Za-z0-9\s+\-\:\'\*\"\s+\.\@\;\,\(\)\\\/]*[^\<\>\;])$/)
	    
	    if ($value =~  m/^(.*)$/s)
            {
	       $value = $1;
	       $value =~ s/\'/\'\'/g;              
	    } 
	    else {  $status =1;}
	}
	case  "id"           { if ($value =~ m/^(\d+)$/)            {$value = $1} else { $status =1;}}
	case "delete"        { if ($value =~ m/^(yes)$/)          {$value =$1;} else { $status = 1;}}       
	case "monthA"         { if ($value =~ m/^(\w{3})$/)        {$value =$1;}  else { $status =1;}}
	case "monthB"        { if ($value =~ m/^(\w{3})$/)        {$value =$1;}  else {$status =1;}}
	case "yearA"          { if ($value =~ m/^(201\d)$/)        {$value =$1;}  else { $status =1;}}
	case "yearB"         { if ($value =~ m/^(201\d)$/)        {$value =$1;}  else {$status =1;}}
        case "hourA"         { if ($value =~ m/^(\d+)$/)          {$value =$1;}  else { $status =1;}}
	case "hourB"         { if ($value =~ m/^(\d+)$/)          {$value =$1;}  else {$status =1;}}
	case "zone"          { if ($value =~ m/^(\w\w\w)$/)       {$value =$1;}  else { $status =1;}}
        case "priority"      { if ($value =~ m/^(\d)$/)           {$value =$1;}  else { $status =1;}}
	case "showdeletes"   { if ($value =~ m/^(yes)$/)          {$value =$1;}  else {$status =1;}}
	case "dayB"          { if ($value =~ m/^(\d+)$/)          {$value =$1;}  else { $status =1;}} 
	case "dayA"          { if ($value =~ m/^(^\d+)$/)         {$value =$1;}  else {$status =1;}}
	case "format"        { if ($value =~ m/^(rawdeleted|rawcurrent)$/)     {$value =$1;} else { $status =1;}}
	case "caldate"       { if ($value =~ m/^(\d{4}-\d\d\-\d\d)$/)          {$value =$1;} else { $status =1;}}
	case "newcaldate"    { if ($value =~ m/^(\d{4}-\d\d\-\d\d)$/)          {$value =$1;} else { $status =1;}}
	case "zoneadjust"    { if ($value =~ m/^((-\d+|\d+),(-\d+|\d+),(-\d+|\d+))$/) {$value =$1;} else { $status =1;}}	
	case "host"          { if ($value =~ m/^([A-Za-z0-9\s+\-\:\@\;\,\.\*]*[^\<\>\;])$/){$value = $1;} else{$status =1;}}
	case "multiple"      { if ($value =~ m/^([A-Za-z0-9\s+\-\:\@\;\,\.]*[^\<\>\;])$/){$value = $1;} else{$status =1;}}
	case "multipleother" { if ($value =~ m/^([A-Za-z0-9\s+\-\:\@\;\,\.]*[^\<\>\;])$/){$value = $1;} else{$status =1;}}
        case "identity"      { if ($value =~ m/^(.*)$/){$value = $1;} else{$status =1;}}
    }
    if ($status)
    {
	my $error = new Table::ErrorMessage("The parameter or value entered is not recognized",$c);
        $error->display();
        exit();
    }
    return $value;
}
sub addentities
{
    my ($decoded) = @_;
    $decoded =~ s/\&amp;lt;/\&lt;/g;
    $decoded =~ s/\&amp;gt;/\&gt;/g;
    return $decoded;
}
sub load_expires
{
    my ($hourb,$dayb, $monthb,$yearb) = @_;
    my %hash = ("hourb"  => $hourb,
                "dayb"   => $dayb,  
                "monthb" => $monthb,
                "yearb"  => $yearb,
                );
    return \%hash;
}
sub store_eff_removebydate
{
    my ($c) = @_;    
    my ($julyzone, $janzone, $currentoffset)  = (split /\,/, $c->{'zoneadjust'});
    
    #convert user's input dates to gmtime (store under one standard: GMT)
    my ($yA,$mA,$dA,$hA) = add_zone_adjustment($c,"eff", $julyzone, $janzone,$currentoffset);
    my ($yB,$mB,$dB,$hB) = add_zone_adjustment($c,"exp",$julyzone, $janzone, $currentoffset);
      
    #determine zone name (EST, MST,etc);
    my $zonename = $c->{'zone'};
    $zonename =  get_zonename($julyzone, $janzone,$currentoffset) if ($c->{'zone'} ne "none");
    
    
    #convert date into epoch
    my $effective_date = convert_date_to_epoch($yA,$mA,$dA,$hA);
    #rename date
    my $ymdhA          = "$yA-$mA-$dA-$hA";
    my $ymdhB          = "$yB-$mB-$dB-$hB";
    #get current time and date
    my $current_time = time();

    #give 1 hour grace (current_time-3600)
    my $current_time_adj = $current_time-3600;
    
    #set upper limit on input time
    my $time_allowed     = 30*24*60*60;    
    my $remove_by_date   = $effective_date+$time_allowed;
    $c->{removebydate}   = $remove_by_date;
    $c->{effectivedate}  = $effective_date;
    $c->{ymdhA}          = $ymdhA;    
    $c->{ymdhB}          = $ymdhB;
    $c->{currenttime}    = get_current_gmtime();
}
sub process
{
    my ($c)  = @_;
    if ($c->{effectivedate} > $c->{removebydate})
    {
	my $m = "You cannot choose that date. You must choose"
	     ." a date within the next 30 days";
	my $error = new Table::ErrorMessage($m,$c);
	$error->display();
	exit(0);
    }
    my $affectedservices = join (';',@{$c->{affectedservices}});
    my $otheraffected    =  join (';',@{$c->{otheraffected}});
    
    #store nice dates 
    $c->{niceeff} = nice_date($c->{ymdhA});
    $c->{niceexp} = nice_date($c->{ymdhB});
    
    #store input in db tables
    my $dbh  = vao_operations_connect();
    store_user_input($dbh,$c);  
    #send mail
    my $vomailer = new Tools::VOMailer($c);
   #$vomailer->send_mail();
    eval 
    {
        #system("/www/htdocs/vo/usvao/update_service_icons.pl");
    };
}
