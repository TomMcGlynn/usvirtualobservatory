#
#
#
#
#
#
package HTML::GenerateVAOStatsExcel;


use DBI;
use Spreadsheet::WriteExcel;
use Tie::IxHash;

use warnings;

sub new
{
    my ($classname) = shift;
    my $objref = {};
    bless $objref,$classname; 
    $objref->initialize(@_);
    return $objref;
}
sub initialize
{
    my $self = shift;
    $self->{titleobj} = $_[0];
    $self->{_cnames}  = $_[1];
    $self->{_con}     = $_[2];    
    
    
    my $table = $self->{_con}->{ratio};
    my $order = $self->{_con}->{order};
    
    my $size  = scalar(@$table);
    
    my $func     = $self->{_con}->{'func'};
    my $colfunc  = $self->{_con}->{'colfunc'};
    
    
    my $h = hashify_table($table); 
    my $newcaldate = $self->{_con}->{newcaldate};
    
   
    my $legacy_services = $order->{'Legacy'};
    my $vao_services    = $order->{'VAO'};
    my $crit_services   = $order->{'CRIT'};
    
    my @alltypes = ($vao_services,$legacy_services,$crit_services);
    my @big;
  
    foreach my $t (@alltypes)
    {
	foreach my $n (@$t)
	{	
	    if (! exists ($h->{$n}))
	    {
		push @big,[$n,"null"];
	    }
	    else
	    {
		my $number  = $h->{$n}->{ratio};
		push @big,[$n,$number];
	    }
	}
    }
    $self->{big} = \@big;     
}
sub hashify_table
{
    my ($table) = @_;
    my $hash = {};
    foreach my  $r (@$table)
    {
	$hash->{$$r[0]}->{ratio}  = $$r[2];
	$hash->{$$r[0]}->{url}   = $$r[1];
    }
    foreach my $n (keys %$hash)
    {
	#print "$n, $hash->{$n}->{ratio}<br>";  

    }
    return $hash;

}
sub gen_excel
{
    my $self = shift;
    my $sheeta = $_[0];
    my $pos = $_[1];
    my $big = $self->{big};
    my $newcaldate = $self->{_con}->{newcaldate};
    foreach my $n (@$big)
    {	
	printf ("%-40s","@$n[0]");
	my $number = printf("%10s",@$n[1]);
	print "\n";
	
    }
    
    $sheeta->write("1",$pos,$newcaldate);
    my ($row,$column);
    $row= 2;
    $column = $pos;
    foreach my $n (@$big)
    {	
	my $service= @$n[0];
	my $status = @$n[1];
	if ($pos == '1')
	{
	    $sheeta->write($row,$column-1,$service);
	    $sheeta->write($row,$column,$status);
        }
	else
	{
	    $sheeta->write($row,$column,$status);
	}
	$row++;
    }
}
1;
