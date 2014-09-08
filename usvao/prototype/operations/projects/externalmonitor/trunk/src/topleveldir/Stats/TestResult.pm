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
    my ($name, $arrayoftests) = @_;
    
    
    $self->{name}      = $name;
    $self->{authid}   = 'null';
    $self->{authid}    =  $arrayoftests->[0][3]  if ($arrayoftests->[0][3]);
    foreach my $j (@$arrayoftests)
    {
#	print "@$j";
	
    }
  
    #$self->{testid}    = @{@$arrayoftests[1];
    #$self->{runid}     = @$arrayoftests[6]; 
    $self->{status}    = "OK";
    $self->{class}     = "green";
    $self->{tests}     = $arrayoftests;
    $self->{total}     = scalar(@$arrayoftests); 
    $self->{passing}   = getNumbers($arrayoftests,"pass");
    $self->{failing}   = getNumbers($arrayoftests,"fail");
    $self->{skipped}   = getNumbers($arrayoftests, "skip");
    $self->{abort}     = getNumbers($arrayoftests, "abort");
    

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
sub printname
{
    my $self = shift;
    print $self->{name};
}
sub printlinehtml
{
    my $self = shift;
    
    my  $g = $self->{class};
    print "\n<tr class = greenlnextra>";
    print qq{<td>$self->{name}</td>};
    print "<td><a class = $g  href = 'vodb.pl?show=$self->{name}&orderby=time&type=oldtests&index=desc'>",  $self->{status},"</td>\n";
    print "</tr>\n";
    

}
sub printlinexml
{
    my $self = shift;
    print "<Service>";
    print "<name>$self->{name}</name>"
         ."<status>$self->{status}</status>"
	 ."<authid>$self->{authid}</authid>";
    print "<\/Service>";
    
}
sub getNumbers
{
    my ($arrayoftests, $status)  = @_;
    my $count =0;
    foreach my $n (@$arrayoftests)
    {
	
	$count ++ if ((@$n[6] eq $status) and (@$n[12] ne 'T'));
    }
    return $count;
}
1;

