#
#
#
#
#
package Table::VOTable;


use Exporter ();
@ISA = qw(Exporter);
@EXPORT = qw();
use strict;
use warnings;


sub new
{
    my ($r) = @_;
    my $classname = $_[0]; 
    my $hash =  {  
	_r   => $_[1],
    };
	
    bless  $hash, $classname;
    return $hash;

}
sub printVOTable
{

    my ($self) = shift;
    my $r = $self->getResponse();
    print "<?xml version = \"1.0\"?>\n";
    print "<!DOCTYPE VOTABLE SYSTEM  \"http://us-vo.org/xml/VOTable.dtd\">\n";
    print "<VOTABLE version = \"1.0\">\n";
    print "<RESOURCE>\n<TABLE>\n";
    print "<DESCRIPTION/>\n";
    print "<FIELD name = \"serviceId\" datatype = \"int\"></FIELD>\n";
    print "<FIELD name = \"runid\" datatype = \"int\"></FIELD>\n";
    print "<FIELD name = \"item\" datatype = \"char\"></FIELD>\n";
    print "<FIELD name = \"error\" datatype = \"char\"></FIELD>\n";
    print "<DATA>\n<TABLEDATA>\n";
    my $r_fixed = fix($r);
    foreach my $line (@$r_fixed)
    {
	print "<TR>\n";
	foreach my $j (@$line)
	{
	    print $j;
	}               
	print "</TR>\n";
    }
   
    print "</TABLEDATA>\n</DATA>\n</TABLE>\n</RESOURCE>\n";
    print "</VOTABLE>\n";
}
sub getResponse
{
    my ($self)  = shift;
    return $self->{_r};
}
sub fix
{
    my ($r) = @_;
    my $rnew =  ();
    foreach my $line (@$r)
    {	
	@$line[0]  = "<TD>@$line[0]</TD>";
	@$line[1]  = "<TD>@$line[1]</TD>";
	@$line[2] = "<TD>@$line[2]</TD>";
	@$line[3] = "<TD>@$line[3]</TD>"; 
	push @$rnew,$line;
    }
    return $rnew;
}
1;
