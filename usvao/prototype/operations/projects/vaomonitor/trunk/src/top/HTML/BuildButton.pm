#
#
#
#
#
package HTML::BuildButton;
use strict;

sub new
{
    my ($class) = shift;
    my $hash ={};
    bless $hash, $class;
    $hash->init(@_);
    return $hash;
}
sub init
{
    my ($self) = shift;
    $self->{type} = $_[0];
    my $data = $_[1];    
    $self->{offset}  = $data->{_con}->{offset};
    $self->{sid}     = $data->{_con}->{sid};
    $self->{valcode} = $data->{_con}->{valcode};
    $self->{show}    = $data->{_con}->{show};
  
  
    if ($self->{offset} eq '')
    {
       
        $self->{offset} = 0;
	
    }
    else
    {  
        $self->{offset} = $self->{offset} + 100;
	$self->{previousbutton} = 'true' if ($self->{offset} > 100);
    }
    return $self;
}

sub display
{
    my $self = shift;
    my $offset  = $self->{offset};
    my $sid     = $self->{sid};
    my $valcode = $self->{valcode};
    my $show    = $self->{show};
    my $type    = $self->{type};
    
    print "<br><br>";
    print "<table class = \'tac\' align = center  border = '1'><tr>";
    if ($self->{previousbutton})
    {
	
	
	my $off = $offset-200;
	print "<td><table>";
	print "<tr><td><a href =  \'$valcode?";
	print "show=$show&type=$type&offset=$off\'>Previous (";
	print $offset-199, ' - ', $offset-100  . ")</td></tr>";
	print "</table></td>";   
	
    }
    my $title = 'Next';
    print "<td><table>";
    print "<tr><td><a href =  \'$valcode?";
    print "show=$show&type=$type&offset=$offset\'>$title (";
    print $offset+1, ' - ', $offset+100 . ")</td></tr>";
    print "</table></td></tr></table>";   
    print "<br>";
   
}
1;
