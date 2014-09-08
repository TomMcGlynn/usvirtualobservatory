#
# StatsTable.pm
# 
# description: describes a statistics table 
#              object to be displayed with the validation results
#              
#
#
#
#
package HTML::StatsTable;


use lib "../data";
use data::startup;
use HTML::DisplayTable;
use warnings;

sub new 
{
    my ($classname) = shift;
   
    my $hashref =  
    {	
	'table'    => $_[0],
    };
    my $ignoretypes   = $_[1];
    my @a = keys %$ignoretypes;
    my $types_to_ignore = join("|",@a);
    $hashref->{ignore}  = $types_to_ignore;  

    bless $hashref,$classname;
    return $hashref;
}
sub get_passed
{
    my $self = shift;
    $self->{pass};
}
sub getfailed
{
    my $self = shift;
    $self->{failed};
}
sub getaborted
{
    my $self = shift;
    $self->{aborted};
}
sub getid
{
    my $self = shift;
    $self->{center};
}
sub getnum_validated
{
    my $self = shift;
    $self->{size};
}
sub getnotval
{
    my $self = shift;
    $self->{notval};
}
sub display
{
    my $self = shift;
    my $contents =  $self->{'table'};
    my $type_to_skip = $self->{ignore};
   
    display_column_names();

    
    #change strings on webpage
    my $t  = load_types();
    my %types = %$t; 
    $types{'vg:Harvest'} = 'Registry (Harvest)';
    $types{'vg:Search'}  = 'Registry (Search)';
    
    foreach my $name (keys %$contents)
    {
       
	my $array = $contents->{$name};
	
        #dereference so that this section is more readable
	my $total              = $$array[4];
	my $notval             = $$array[3];
	my $passed             = $$array[0];
	my $failed             = $$array[1];
	my $aborted            = $$array[2];
	my $skipped            = $$array[6];
	my $deleted            = $$array[7];
	my $deprecated         = $$array[8];
	
	#print "TT:$name: $total<br>";
	my $tactive               = $total-$deleted-$deprecated;
	#print "PP: $tactive<br>";
	my $t                  = $passed+$failed+$aborted; 
       	my $nameshown          = $name;
	
	if ($types{$name})
	{
	    $nameshown = $types{$name};
	}  
	     
        print "<tr><td><a href = '$::valcode?show=$$array[5]&orderby=time&index=desc&turnoff=$type_to_skip'>$nameshown</a></td>";
	print "<td class = greenln>$tactive</td>";
        print "<td class =greenln>$t</td>";
	print "<td class = greenln>$notval</td>";
	
        print "<td cellpadding = 0 border =0  ><img src = './doc/votablebar.jpg' width = '8' height  = 20></td>";

	print "<td class = greenln>$passed</td>";
	print "<td class = greenln bgcolor = FAE9B2>$skipped</td>";
	print "<td class = greenln bgcolor = FAE9B2>$deleted</td>";
	print "<td cellpadding = 0 border =0  ><img src = './doc/votablebar.jpg' width = '8' height  = 20></td>";
	print "<td class = greenln bgcolor = FAE9B2>$deprecated</td>";
	print "<td class = greenln bgcolor = FAE9B2>$failed</td>";
	print "<td class = greenln bgcolor = FAE9B2>$aborted</td>";
	print "<td align = center><a href = \"$::valdir/getcenter.pl?show=$name\">view list</a></td></tr>\n";
    }
    print "</table>";
}
sub display_column_names
{
    print "<table class = 'tac' align = center border = 1 width = 900>";
    print "<tr><td class = tblue>Center (*click for individual test results)</td><td class = tblue >Active Services</td>\n";
    print "<td class = tblue>Total Validated</td>";
    print "<td class = tblue>Not validated</td>\n";
    print "<td cellpadding = 0 border =0  ><img src = './doc/votablebar.jpg' width = '8' height  = 30></td>";
   
    print "<td class = tblue>Passed</td>\n";
    print "<td class  = tblue>Skipped</td>";
    print "<td class  = tblue>Deleted</td>";
    print "<td cellpadding = 0 border =0  ><img src = './doc/votablebar.jpg' width = '8' height  = 30></td>";
    print "<td class = tred>Deprecated</td>\n";
    print "<td class = tred>Failed</td>\n";
    print "<td class  =tred>Aborted</td>\n";   
    print "<td class = tred>Error Types per Center</td></tr>\n"; 
}
1;
