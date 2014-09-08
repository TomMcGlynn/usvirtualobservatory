#
#
#
#
#
package Stats::TestResult;

sub new
{
    my ($class) = shift;
    my $hash = {};
    bless $hash,$class;
    $hash->init(@_);
    return $hash;
}
sub init
{
    my ($self) =shift;
    my ($name, $arrayoftests,$type) = @_;    
    $self->{type} = $type;
    
    $self->{name}      = $name;
    #$self->{sid}       =  @{@$arrayoftests[0]}[0];
    foreach my $j (@$arrayoftests)
    {
	#print "@$j";
	
    }
  
    #$self->{testid}      = @{@$arrayoftests[1];
    #$self->{runid}       = @$arrayoftests[6]; 
    $self->{status}       = "OK";
    $self->{class}        = "green";
    $self->{tests}        = $arrayoftests;
    $self->{total}        = scalar(@$arrayoftests); 
    $self->{passing}      = getNumbers($arrayoftests,"pass");
    $self->{failing}      = getNumbers($arrayoftests,"fail");
    $self->{skipped}      = getNumbers($arrayoftests, "skip");
    $self->{abort}        = getNumbers($arrayoftests, "abort");
    $self->{description}  = getDescription($name); 

    if ($self->{skipped} > 0)
    {
	
	#$self->{status} = "Skip";
	if ( $self->{skipped} eq $self->{total})
	{  
	    
	    $self->{status} = "Skip";
	    $self->{class} = "red";
	}
	else
	{
	    if (($self->{failing} > 0) || ($self->{abort} > 0))
	    {
		$self->{class} = "red";
		$self->{status} =  "Fail";
	    }	    
	}
    }
    else
    {
      
	if (($self->{failing} > 0) || ($self->{abort} > 0))
	{
	    $self->{class} = "red";
	    $self->{status} =  "Fail";
	}
    }
    
    return $self;
}
sub getDescription
{
    my ($name) = @_;
    my %hash = ('Cross Comparison Service'     => 'Cross Matching between an input
                                                   table and astronomical source catalogs',	      
		'IRIS'                         => 'Downdload page for tool to find, plot and fit spectral energy
                                                   distributions',
		'Time Series'                  => 'Discover time-series data from
                                                   major archives and analyze them',
		'IRIS SED'                     => 'Find, Plot,and fit spectral energy distributions (SEDs) with this desktop application',
		'NED SED'                      => 'SED generation tool used by Iris',
		'Data Discovery'               => 'Retrieve astronomical data about a given position or object in the sky',
		'VAO Website'                  => 'The US Virtual Astronomical Observatory Home Page',
		'DataScope Portal'             => 'Search a position for all known information',
	        'VAO Directory and Registry'   => 'Find data collections and catalogs by searching their descriptions', 
                'Data Discovery Tool'          => 'Retrieve astronomical data about a given position or object in the sky', 
                'VO Web Log'                   => '',
                 		
  
     );
    return $hash{$name} if (exists ($hash{$name}));     

}
sub printname
{
    my $self = shift;
    print $self->{name};
}
sub printlinexml
{
    my $self = shift;
    print "<Service>";
    print "<name>$self->{name}</name><status>$self->{status}</status>";
    print "<\/Service>";    
}
sub buildline_xml
{
    my $self = shift;
    my $xml = "<Service><name>$self->{name}</name><status>"
	    . "$self->{status}</status><\/Service>"; 

}
sub printlinehtml
{
    my $self = shift;
    my  $g = $self->{class};
    print qq{<tr class = greenlnextra><td>$self->{name}</td>	
		 <td><a class = $g  href = 'vaodb.pl?show=$self->{name}&orderby=time&type=oldtests&index=desc'>};
    print "$self->{status}";
    print "</td></tr>\n";
}
sub build_line
{
    my ($name, $status) = @_;
    my $desc = getDescription($name);
    my $n = $name;
    $n = 'Iris' if ($name eq 'IRIS');
    $n = 'Iris SED' if ($name eq 'IRIS SED');
    $n = 'Data Discovery Agent' if ($name eq 'DataScope Portal');
    $n = 'Data Discovery Engine'  if ( $name eq 'Data Discovery');
    #$n = 'Data Discovery'  if ($name eq 'Data Discovery Tool'); 
 
    my $string;
    $string = 'timeseries.gif' if ($name eq  'Time Series');
    $string = 'ddt.gif' if ($name eq  'Data Discovery Tool');
    $string = 'irissed.gif 'if ($name eq "IRIS");
    $string = 'iris.gif' if ($name eq 'IRIS SED');
    $string = 'vaowebsite.gif' if ($name eq 'VAO Website');
    $string = 'vaodir_registry.gif' if ($name eq 'VAO Directory and Registry');
    $string = 'nedsed.gif' if ($name eq 'NED SED');
    $string = 'ddtagent.gif' if ( $name eq 'DataScope Portal');
    $string = 'ddtengine.gif' if ($name eq 'Data Discovery');
    $string = 'crosscomparison.gif' if ($name eq 'Cross Comparison Service');

   my $line = "<tr class = greenlnextra><td>$n</td><td>$desc</td>"
	     . "<td><img src = '/tmp.shared/vo/usvao/icons/$string'></img></td>"
	     . "<td>$status</td></tr>\n";
    return $line;
    
}
sub getNumbers
{
    my ($arrayoftests, $type )  = @_;
    my $count =0;
    foreach my $n (@$arrayoftests)
    {
	$count ++ if (@$n[5] eq $type);
    }
    return $count;
}
1;

