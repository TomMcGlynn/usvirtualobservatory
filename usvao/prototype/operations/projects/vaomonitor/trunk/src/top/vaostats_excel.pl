#!/usr/contrib/linux/bin/perl -w
#
#
#
# name : vaostats_excel.pl
# description: script gets the status of all VAO services 
#              for any time window defined in the "dates" file. 
#              It then generates an excel spreadsheet containing
#              this data.
# 
# usage: ./vaostats_excel.pl
#
#
{
    use strict;
    use lib './';
    use lib '/www/server/vo/vaomonitor'; 
    use SQLVao::Queries;
    use data::startup;
    use HTML::GenerateVAOStatsExcel;
    use Tie::IxHash;
    use DBI;
    use Spreadsheet::WriteExcel;
    use Connect::MySQLVaomonitorDB;
    use Switch;
        
    my $dbh = vaomonitor_connect();
    
    
    #my $tome   = Spreadsheet::WriteExcel->new("history.xls");
    #my $sheeta = $tome->add_worksheet();
    #$sheeta->set_margin_left(.5);
    #$sheeta->set_margin_right(.5); 
    #$sheeta->center_horizontally();
    #$sheeta->set_column(0,0,20);
    #$sheeta->set_column(0,1,20);   
    #$sheeta->activate();
    #$sheeta->write(0,0,"Service");
    #$sheeta->write(0,1,"Status");
        
    open (G,"./dates");
    my $pos =1;
    
    while (<G>)
    {
	my $line = $_;
	chomp $line;
	my  ($start,$end)         = (split /\|/,$line);	
	my ($year,$month,$day)    = (split /\-/,$start);
	my ($yearb,$monthb,$dayb) = (split /\-/,$end);
	
	my $expires  = load_expires($dayb,$monthb,$yearb);	
	my $con      = get_container($dbh,$month,$day,$year,$expires,"EST",$start,$end);
	
	run($con,$pos,$sheeta);
	$pos++;
	print "\n\n";
    }
    close G;
    $tome->close();
    exit();
    
}
#########################################
# load expires container
#########################################
sub load_expires
{
    my ($dayb, $monthb,$yearb) = @_;
    my %hash = (
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
    my ($dbh,$month, $day, $year,$expires,
        $zone,$caldate,$newcaldate) = @_;
    
    
    my $hash = 
    {  
	'dbh'            => $dbh,
        'month'          => $month,
        'day'            => $day,
        'year'           => $year,
        'expires'        => $expires,
        'zone'           => $zone,
        'caldate'        => $caldate,
        'newcaldate'     => $newcaldate,
    };
    return $hash;
}
sub process
{
    my ($passarray, $failarray) = @_;
    my $s_pass = {};
    my $s_fail = {};
    
   
    foreach my $n (@$passarray)
    {
        my @array  = @$n;
	$s_pass->{$array[0]}->{pass} =  $array[3];
	$s_pass->{$array[0]}->{fail} =  0;
	$s_pass->{$array[0]}->{url} = $array[1];
    }
    foreach my $n (@$failarray)
    {
	
        my @array  = @$n;
	print "PP: $array[0]\n"; 
	if (exists ($s_pass->{$array[0]}))
	{
	    $s_pass->{$array[0]}->{fail} = $array[3];	    
	}
	else
	{
	    #the test must have a 0% pass rate
	    $s_pass->{$array[0]}->{pass}  = 0;
	    $s_pass->{$array[0]}->{fail} = $array[3];
	    $s_pass->{$array[0]}->{url}   = $array[1];
	}
    }

    return $s_pass, $s_fail;
}
sub calculate_uptime
{
    my ($s_pass,$s_fail) = @_;

    my $big =[];
    foreach my $n (keys %$s_pass)
    {
	
	my $pass = $s_pass->{$n}->{pass};
	my $fail = $s_pass->{$n}->{fail};
	my $url  = $s_pass->{$n}->{url};
	my $ratio;
		
	my $r = $pass+$fail;
	
	if (($pass == '0') and ($fail == '0'))
	{
	    $ratio = 'unknown';
	}
	else
	{
	    $ratio = substr($pass*100/$r,0,4);
	}
	

        my $array = [$n, $url,$ratio];  
	push @$big,$array;
	#print $n, $pass," " , $r, " ", $ratio, "\n";	
    }
    return $big;
    
}
sub run
{
    
    my ($con,$pos,$sheeta) = @_;
    my ($passarray, $failarray, $ratio , $query1, $query2);
    
   
    $query1 = 'getRangeStatsPassing';
    $query2 = 'getRangeStatsFailing';
      
    $con->{caldate} =~   s/\-/\//g;
    $con->{newcaldate} =~ s/\-/\//g;
      
     
    $passarray  = &$query1($con);
    $failarray  = &$query2($con);
    
    my ($s_pass,$s_fail) = process($passarray,$failarray);

   


    $ratio   = calculate_uptime($s_pass,$s_fail);
    my $order = organize_list();
    
    
    $con->{'allpass'}   = $passarray;
    $con->{'allfail'}   = $failarray;
    $con->{'ratio'}     = $ratio;
    $con->{'order'}     = $order;
    
    my @cnames = ('Service',"Home URL",  "Percentage Up");
    
    $con->{'colfunc'} = "fixcolname_uptime";               
    $con->{'func'}    = "uptime_html"; 
    
    my $dt            = new HTML::GenerateVAOStatsExcel("Stats", \@cnames,$con);
    $dt->gen_excel($sheeta,$pos);    
}
sub organize_list
{
    
    my %hash = ( 'Legacy' => ['VO Inventory Service','NVO Security Service', 'NVO Security Service Mirror','Table Importer',
			      'DataScope','Simple Query','Open Sky Query', 
			      'VIM','NVO Importer','FootPrint Services',
			      'Querying for data at Caltech using Carnivore',
			      'Spectrum Service', 'NVO Portal'],		 
		 'VAO'   => ['VAO Website','VAO Directory and Registry','Data Discovery','VO Web Log','Registry Validator',
			     'Table Search Validator','Image Validator','VOResource Validator','VAO Notification Service',
			     'Twiki', 'Trac', 'VO Validation Service', 'JIRA','JIRA Mirror','VO Inventory Portal','DataScope Portal',
			     'Cross Comparison Service','IRIS SED','IRIS','Time Series','SkyAlert','VAO Jenkins',
	                     'SSO Replication Monitor'],
		 'CRIT'  => ['NED','SIMBAD'],
		 );
    
    return \%hash;

}
