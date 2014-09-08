#!/usr/bin/perl -wT
#
#
#
# name : vaostats.pl
#
#
#
{
    use strict;
    use CGI;
    use lib './';   
    my $machine  = $ENV{'SERVER_NAME'};
    if ($machine =~ /^heasarcdev(.*)/) {use lib "/www/server/vo/vaomonitor_test";}
    if ($machine =~ /^heasarc(.*)/){use lib "/www/server/vo/vaomonitor";}
    use data::startup;
    use SQLVao::Queries;
    use HTML::TopVAOStats;
    use HTML::BannerVAOStats;
    use HTML::Table;
    use HTML::VOheader;
    use HTML::Bar;
    use HTML::DisplayVAOStatsTable;    
    use DBI;
    use Connect::MySQLVaomonitorDB;
    use Switch;
 
    print "Content-type: text/html\n\n";
    

    my $cgi= CGI->new();
    
    my $range = detaint("range",$cgi->param("range")) if ($cgi->param("range"));
    my $name  = detaint("name", $cgi->param("name")) if ($cgi->param("name"));
    my $month        = detaint("month",$cgi->param("month"))             if ( $cgi->param("month"));
    my $day          = detaint("dayA",$cgi->param("dayA"))               if ($cgi->param("dayA"));
    my $year         = detaint("year",$cgi->param("year"))               if ($cgi->param("year"));
    my $hour         = detaint("hourA",$cgi->param("hourA"))             if ($cgi->param("hourA"));
    my $zone         = detaint("zone",$cgi->param("zone"))               if ($cgi->param("zone"));
    my $showdel      = detaint("showdeletes",$cgi->param("showdeletes")) if ($cgi->param("showdeletes"));
    my $dayb         = detaint("dayB",$cgi->param("dayB"))               if ($cgi->param("dayB"));
    my $monthb       = detaint("monthB",$cgi->param("monthB"))           if ($cgi->param("monthB"));
    my $yearb        = detaint("yearB",$cgi->param("yearB"))             if ($cgi->param("yearB"));
    my $hourb        = detaint("hourB",$cgi->param("hourB"))             if ($cgi->param("hourB"));
    my $format       = detaint("format",$cgi->param("format"))           if ($cgi->param("format")); 
    my $caldate      = detaint("caldate",$cgi->param("caldate"))         if ($cgi->param("caldate"));
    my $newcaldate   = detaint("newcaldate", $cgi->param("newcaldate"))  if ($cgi->param("newcaldate"));
    my $zoneadjust   = detaint("zoneadjust",$cgi->param("zoneadjust"))   if ($cgi->param("zoneadjust"));
    my $delete       = detaint("delete",$cgi->param("delete"))           if ($cgi->param("delete"));

    my $dbh = vaomonitor_connect();

    my $expires       = load_expires($hourb,$dayb,$monthb,$yearb);
    
    
    my $con =  get_container($dbh, $cgi, $range, $name,$month, $day, $year,$hour,
			     $expires,
                             $delete, $zone, $zoneadjust,$caldate, $newcaldate);
    

    
    
    my @linknames   = ('VO Service Notices','VAO Monitor','VAO Home', 'NVO Feedback');
    my $voheader        = new HTML::VOheader("VAO Monitor",\@linknames);
    $voheader->printheader();
    $voheader->print_banner();
    run($con); 
    HTML::Table::add_footer();
    exit();

    
}
#########################################
# load expires container
#########################################
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

########################################
#  get container
########################################
sub get_container
{
    my ($dbh, $cgi, $range,$name, $month, $day, $year,$hour,
        $expires,$delete,
        $zone,$zoneadjust,$caldate, $newcaldate) = @_;
    
  
    my $hash = 
    {  
	'dbh'            => $dbh,
	'cgi'            => $cgi, 
	'range'          => $range,
	'name'           => $name,
        'month'          => $month,
        'day'            => $day,
        'year'           => $year,
        'hour'           => $hour,
        'expires'        => $expires,
        'delete'         => $delete,
        'zone'           => $zone,
        'zoneadjust'     => $zoneadjust,
        'caldate'        => $caldate,
        'newcaldate'     => $newcaldate,
    };
    return $hash;
}
sub calculate_uptime
{
    my ($allpass, $allfail) = @_;
    my $s_pass = {};
    my $s_fail = {};

    my $big = [];
    foreach my $n (@$allpass)
    {
        my @array  = @$n;
	$s_pass->{$array[0]}->{pass} =  $array[3];
	$s_pass->{$array[0]}->{fail} =  0;
	$s_pass->{$array[0]}->{url} = $array[1];
    }
    foreach my $n (@$allfail)
    {
        my @array  = @$n;
	if (exists ($s_pass->{$array[0]}))
	{
	    $s_pass->{$array[0]}->{fail} = $array[3];	    
	};	
    }
    
    foreach my $n (keys %$s_pass)
    {
	my $pass = $s_pass->{$n}->{pass};
	my $fail = $s_pass->{$n}->{fail};
	my $url = $s_pass->{$n}->{url};
	my $r = $pass+$fail;
	#print "RR: $r", ref($r);
        my $a = 3*3;
	my $ratio = substr($pass*100/$r,0,4);
        #my $ratio  = $pass /  $r;
	if ($n eq 'DataScope')
	{
	    #print "PASS: $pass,  $fail";
	    #print "<br>";
	    
	}
	
        my $array = [$n, $url,$ratio];  
	push @$big,$array;
	#p $n, $pass," " , $r, " ", $ratio;
	#print "<br>";
	
    }
    return $big;



}
sub run
{

    my ($con) = @_;
    my ($set_pass, $set_fail, $ratio , $query1, $query2);
    
    $query1 = 'getRangeStatsPassing';
    $query2 = 'getRangeStatsFailing';
    $con->{caldate} =~  s/\-/\//g;
    $con->{newcaldate} =~ s/\-/\//g;
      
    
	
    $set_pass = &$query1($con);
    $set_fail  = &$query2($con);
    
    $ratio   = calculate_uptime($set_pass,$set_fail);
    my $order = organize_list();
    
    foreach my $n (@$ratio)
    {
	my @a = @$n;
       
	
    }
    $con->{'allpass'}   = $set_pass;
    $con->{'allfail'}   = $set_fail;
    $con->{'ratio'}     = $ratio;
    $con->{'order'}     = $order;
    
    my @cnames = ('Service',"Home URL",  "Percentage Up");
    
    
    
    $con->{'colfunc'} = "fixcolname_uptime";               
    $con->{'func'}    =  "uptime_html"; 
    
    my $dt            = new HTML::DisplayVAOStatsTable("Stats", \@cnames,$con);
    $dt->displayTable();
    

}
sub organize_list
{

    my %hash = ( 'Legacy' =>
                            ['VO Inventory Service','NVO Security Service', 'NVO Security Service Mirror','Table Importer',
                              'DataScope','Simple Query', 
                              'VIM','NVO Importer','FootPrint Services',
                              'Spectrum Service', 'NVO Portal',        
			      ],
		 'VAO'    => ['VAO Website','VAO Directory and Registry','Data Discovery','VO Web Log','Registry Validator',
                             'Table Search Validator','Image Validator','VOResource Validator','VAO Notification Service',
                             'Twiki', 'Trac', 'VO Validation Service', 'JIRA','DataScope Portal',
                             'Cross Comparison Service','IRIS SED','IRIS','Time Series','SkyAlert','VAO Jenkins',
                             'SSO Replication Monitor'],


                     'CRIT'  => ['NED','SIMBAD'],
		 );
    
    return \%hash;

}
sub detaint
{
    
    my ($param,$value) =  @_;
    my $status;
    switch($param)
    {
	case "range" 
	{
	    if ($value =~ /\d+/) {return $value;}
	    else {$status =1;}
	    
	}
	case "name"
	{
	    if ($value =~ m/(^[^\<\>\;]*)$/){$value = $1;}
	    else {$status = 1;}
	}     
	case "month"        { if ($value =~ m/^(\w{3})$/)        {$value =$1;} else { $status =1;}}
        case "monthB"       { if ($value =~ m/^(\w{3})$/)        {$value =$1;} else {$status =1;}}
        case "year"         { if ($value =~ m/^(201\d)$/)        {$value =$1;} else { $status =1;}}
        case "yearB"        { if ($value =~ m/^(201\d)$/)        {$value =$1;} else {$status =1;}}
        case "hourA"        { if ($value =~ m/^(\d+)$/)          {$value =$1;} else { $status =1;}}
        case "hourB"        { if ($value =~ m/^(\d+)$/)          {$value =$1;} else {$status =1;}}
        case "zone"         { if ($value =~ m/^(\w\w\w)$/)       {$value =$1;} else { $status =1;}}
        case "priority"     { if ($value =~ m/^(\d)$/)           {$value =$1;} else { $status =1;}}
        case "showdeletes"  { if ($value =~ m/^(yes)$/)          {$value =$1;} else {$status =1;}}
        case "dayB"         { if ($value =~ m/^(\d+)$/)          {$value =$1;} else { $status =1;}} 
        case "dayA"         { if ($value =~ m/^(^\d+)$/)         {$value =$1;} else {$status =1;}}
        case "format"       { if ($value =~ m/^(rawdeleted|rawcurrent)$/)     {$value =$1;} else {$status =1;}}
        case "caldate"      { if ($value =~ m/^(\d{4}-\d\d\-\d\d)$/)          {$value =$1;} else { $status =1;}}
        case "newcaldate"   { if ($value =~ m/^(\d{4}-\d\d\-\d\d)$/)          {$value =$1;} else { $status =1;}}
        case "zoneadjust"   { if ($value =~ m/^(\d\,\d\,\d)$/)                {$value =$1;} else { $status =1;}} 
    }
    
    if ($status)
    {
	require HTML::ErrorMessage;
	require HTML::Layout;
        my $error = new HTML::ErrorMessage("The parameter or value entered is not recognized");
        $error->display();
        exit();
    }
    return $value;

  
	

}
