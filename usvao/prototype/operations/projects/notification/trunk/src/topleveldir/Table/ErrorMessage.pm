#
#
#
#
#
package Table::ErrorMessage;
@ISA = ("Table::Message");

use strict;
use lib '..';
use Table::LoginBox;
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
    my $self =shift;
    $self->{note} = shift;
    $self->{data} = shift;
    $self->{title} = "Error Page";
    $self->{type} = "Error";
    $self->set_message();
}
sub set_message
{
   my $self =  shift;
   
    $self->{message} = "<br><br><table class = tac  align = center>"
                       . "<tr class = titleblue>"
                       . "<td  align = center>$self->{type}</td>"
                       . "</tr>"
                       . "<tr class = greenln><td>$self->{note}</td></tr></table><br><br><br>";

}
1;

