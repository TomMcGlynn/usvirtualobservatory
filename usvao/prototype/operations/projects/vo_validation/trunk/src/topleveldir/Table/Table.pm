#
#
# A table object (from the registry
# records). This class can be
# used to build different types of table
# objects. 
#
#
#
package Table::Table;


use Tie::IxHash;

use Exporter ();
use DBI;
@ISA = qw(Exporter);

@EXPORT = qw (new);



use strict;
use warnings;
sub new 
{

    my ($class)  =  $_[0];
    my $objref = {
        _name     => $_[1],
	_dbhandle => $_[3],
	_colname  => $_[2],
	_data    => undef,
    };
    bless $objref, $class;
}
sub getname
{
    my $self  = shift;
    $self->{_name};
}
sub getcol
{
    my $self  = shift;
    $self->{_colname};
}
sub getTableData
{    
    my ($self) = @_;
    my $dbh       = $self->{_dbhandle};
    my $tablename = $self->{_name};
    my $colname   = $self->{_colname};
  
    my $hash = {};
    tie  %$hash, "Tie::IxHash";
    
    my $sth  = $dbh->prepare("select * from $tablename order by $colname");   
    $sth->execute;
    my $data  = $sth->fetchall_arrayref();
    my $size = scalar(@$data);
    #print "Size; $size\n";
    foreach my $n (@$data)
    {
        my @row  = @$n; 
        my $r = join ("|",@row);        
        $hash->{"$r"} =1;
    }
    $self->{_data} = $hash;
    return $hash;
}
sub getTableCols
{
    my ($self) = @_;
    my $dbh = $self->{_dbhandle};
    my $tablename = $self->{_name};
    my $sthcols = $dbh->prepare("describe $tablename");
    $sthcols->execute;    
    my $cols  = $sthcols->fetchall_arrayref();
    return $cols;
}
sub getcount
{
    my ($self) = @_;
    my $hash  =   $self->{_data};
    my $count = keys %$hash;
    return $count;
    
}
1;
