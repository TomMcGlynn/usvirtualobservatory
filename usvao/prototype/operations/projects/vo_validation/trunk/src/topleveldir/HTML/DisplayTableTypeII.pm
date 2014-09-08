# used to build different tables for
# 'getcenter.pl software'
# 
#
#
package HTML::DisplayTableTypeII;
use HTML::Parent;

use  Tie::IxHash;
use Exporter ();
use DBI;


use warnings;

sub new
{
    my ($classname,$array,$name,$switch) = @_;
    my $objref = {};
    bless $objref,$classname; 
    $objref->initialize($array,$name,$switch);
    return $objref;
}
sub initialize
{
    my ($self,$array,$name,$switch) = @_;
   
    $self->{_array}   = $array;
    $self->{_name}    = $name;  
    $self->{_switch}  = $switch;
}
sub getContainer
{
    my $self = shift;
    return $self->{_con};
}
sub getTitle
{
    my $self  = shift;
    return $self->{_title};
}
sub getQuery
{
    my $self = shift;
    return $self->{_sqlquery};
}
sub getColumns  
{
    my $self =shift;
    return $self->{_cnames};
}
sub getResponse
{
    my $self =shift;
    return $self->{_response};    
}
sub displayTable
{
    my $self = shift;
    my $switch = $self->{_switch};
    my $array = $self->{_array};
    my $name = $self->{_name};
   
    my $g = scalar(@$array);
   
    display_columns("Error","Error Message", "Frequency") if ($switch eq 'default');
    display_columns ("ID","serviceId","runid") if ($switch eq 'error');
    
    
    
    
    foreach  my $n (@$array)
    {
	print "<tr class = greenln>";
	foreach my $entry (@$n)
	{
	    
	    print "<td>$entry</td>";
		
	}
        print "</tr>";
        
        
	#print "<td align = left>$string</td>";  
       
        #print "</tr>";
    }
    print "</table><br><br>";
   
    
}
sub display_columns
{
    my ($column1, $column2,$column3,$name) = @_;
    print "<table class = 'tac' width = 680 align = center border =1>";
    print "<tr class = titleblue><td colspan = 3></td></tr>";
    print "<tr class = greenln><td>$column1</td><td>$column2</td><td>$column3</td></tr>";


}
sub load_types
{

    my %names = ( 'sia:SimpleImageAccess'    => 'SIA',
		  'cs:ConeSearch'            => 'Cone',
		  'ssa:SimpleSpectralAccess' => 'SSA',
		  'vg:Search'                => 'Registry',
		  'vg:Harvest'              => 'Registry',
		  );
    return \%names;

}
1;
