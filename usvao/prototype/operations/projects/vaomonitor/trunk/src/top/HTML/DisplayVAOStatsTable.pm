#
#
#
#
#
#
package HTML::DisplayVAOStatsTable;




#use Exporter ();
#@ISA = qw(Exporter);
#@EXPORT = qw();
use DBI;
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
    $self->{titleobj}   = $_[0];
    $self->{_cnames}  = $_[1];
    $self->{_con}     = $_[2];
    
    
}
sub displayTable
{
    my $self = shift;
    
    my $table = $self->{_con}->{ratio};
    my $order = $self->{_con}->{order};
    
    my $size  = scalar(@$table);
    
    my $func  = $self->{_con}->{'func'};
    my $colfunc     = $self->{_con}->{'colfunc'};
    
    my $sizefields        = scalar(@{$self->{_cnames}});
    my $h = hashify_table($table); 
    my $newcaldate = $self->{_con}->{newcaldate};
    
    
    #add table title
    print "<table class = \'tac\' align = \'center\' border = 1>";
    print "<tr class = \'titleblue\'><td id = date1  colspan = $sizefields >VAO Services: $self->{_con}->{caldate} \- $self->{_con}->{newcaldate}</td></tr>";
    
    #result set size= 0 when clicking on any "pass" link in renderer
    #write columns names
    my  $line  = &$colfunc($self);
    write_colnames($line,'greenln');
   
    my $c = 'greenln';
    my $c1 = 'greenlnextra';
    my $legacy_services = $order->{'Legacy'};
    my $vao_services    = $order->{'VAO'};
    my $crit_services  = $order->{'CRIT'};
    
    
    
    foreach my $n (@$vao_services)
    {
	my $change = 'greenln';
	my $number  = $h->{$n}->{ratio};
	$change = 'tyellow'	if (($number >= '90') and ($number <= '95'));
	$change = 'tred' if ($number < '90');
	print "<tr><td class = $c1 >$n</td><td class = $c1>$h->{$n}->{url}</td><td class = $change>$h->{$n}->{ratio}</td></tr>";
    }
    print "<tr class = \'titleblue\'><td id = date2 colspan = $sizefields >Legacy Services: $self->{_con}->{caldate} \- $self->{_con}->{newcaldate}</td></tr>";   

    foreach my $n (@$legacy_services)
    { 
	my $change = 'greenln';
	my $number  = $h->{$n}->{ratio};
	$change = 'tyellow'	if (($number >= '90') and ($number <= '95'));
	$change = 'tred' if ($number < '90');	
	print "<tr><td class = $c1 >$n</td><td class = $c1>$h->{$n}->{url}</td><td class = $change>$h->{$n}->{ratio}</td></tr>";
    }
    
    print "</table><br><br>";

    #add table title
    print "<table class = \'tac\' align = \'center\' border = 1>";
    print "<tr class = \'titleblue\'><td colspan = $sizefields >Critical External Services</td></tr>";   
    
    #result set size= 0 when clicking on any "pass" link in renderer
    #write columns names
   
    write_colnames($line,'greenln');
    foreach my $n (@$crit_services)
    {
	my $change = 'greenln';
	my $number  = $h->{$n}->{ratio};
	$change = 'tyellow'	if (($number >= '90') and ($number <= '95'));
	$change = 'tred' if ($number < '90');	
	print "<tr><td class = $c1 >$n</td><td class = $c1>$h->{$n}->{url}</td><td class = $change>$h->{$n}->{ratio}</td></tr>";
    }
    #print "<tr><td class = $c1>NED</td><td class = $c>$h->{NED}->{ratio}</td></tr>";
    print "</table><br>";

    
    
    
    #foreach my $r (@$table)
    #{
	#my ($line,$deleted) = &$func($r,$self);
	#write_line($line,$class);    
    #}
    print "</table>";   
    print "<br><br>";     
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
	#print "$n, $hash->{$n}<br>";  

    }
    return $hash;

}
sub fixcolname_uptime
{    
    my ($self)   = @_;
    my $cnames    = $self->{_cnames};
   # my $valcode  = $self->{_con}->{'valcode'};
   # my $show     = $self->{_con}->{'show'};  
   # my $sid      = $self->{_con}->{'sid'};
   # my $error    = ''; 
 
    for (my $i = 0; $i<scalar(@$cnames);$i++)
    {     
        $$cnames[$i] = "<td>$$cnames[$i]</td>";         
    } 
    return $cnames;
}

sub uptime_html
{
    
    my ($r,$self) = @_;  
    
 
         
    my $name        = $$r[0];
    my $number   = $$r[1];   
    #my $number      = $$r[2];
    #my $range       = $self->{_con}->{range};
   
    
    #redefine array elements with html
    $$r[0]           = "<td align =  left>$name</td>";
    $$r[1]           = "<td align  = \'center\'>$number</td>";
   

    return $r;
    

}
sub write_line
{
    my ($r,$class) = @_;  
    
    print "<tr class = $class>";
    foreach my $n (@$r)
    {   
        print $n;       
    }
    print "</tr>\n";
}
sub write_colnames
{
    my ($r,$class) = @_;
   
    print "<tr class = greenln >"; 
    foreach my $n (@$r)
    {
        print "$n";
    }
    print "</tr>\n";
}

1;