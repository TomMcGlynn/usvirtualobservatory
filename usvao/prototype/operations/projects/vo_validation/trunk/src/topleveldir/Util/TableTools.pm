#
#
#
# some useful routines for processing tables
#
#
#
package Util::TableTools;



use Exporter();
@ISA = qw(Exporter);

@EXPORT = qw (add_up getnumbers  postprocess_table);

sub add_up
{
    my ($matchstring, $pos, $table) = @_;
    my $count =0;
    foreach my $n (@$table)
    {
	if (@$n[$pos])
	{
	    my $val  = @$n[$pos];
	    $count++ if ($val eq $matchstring);
	}
    }
    return $count;
}
sub postprocess_table
{
    my ($response,$ignoretypes) = @_;
    my $pos = 5;
    my %new = reverse %$ignoretypes;
    my $q = keys %$ignoretypes;
 

    #when on old tests page, check for type at different position  
    my $size = scalar (@{$$response[0]});
    $pos = '3'  if ($size == '5');

    foreach my $row (@$response)
    {
	$row->[2] = 'skip' if (exists $new{$row->[$pos]});
    }
}
######################
sub getnumbers
{
    my ($arrayref,$pos,$page) = @_;
     
    my $deletedcolumn = '3';
    my $deprecatedcolumn = '4';
    
    my $turnoff = '5';
    $deletedcolumn = 11  if (!$page);
    $deprecatedcolumn = '12' if (!$page);
    $turnoff = '13' if  (!$page);
   
    my $hash;
    $hash  = 
    {
        'pass'    => '0',
        'fail'    => '0',
        'abort'   => '0',
        'skip'    => '0',
        'notval'  => '0',
        'deleted' => '0',
        'deprecated' => '0',
    };
    
    foreach  my $r (@$arrayref)
    {   
        my @row = @$r;   
	#print scalar(@row);
        #print "@row<br>";
	#sforeach my $j (@row) {print "J: $j<br>"; }
	#print "RRR $row[4]<br>";
	
        if ($row[$deletedcolumn])
	{
	    
	    $hash->{'deleted'}++ if ($row[$deletedcolumn] eq 'yes');
	    next;
	    
	}	
        if ($row[$deprecatedcolumn])
	{	    
	    $hash->{deprecated}++ if  ($row[$deprecatedcolumn] eq 'yes');
	    next;       
	}

	
	if ($row[$pos])
	{
	    
	    if (exists ($hash->{$row[$pos]}))
	    {                  
		$hash->{$row[$pos]}++;
	    }
	}
	else
	{  
	    
	    $hash->{'notval'}++;
	}
    }
    return $hash;
}
1;
