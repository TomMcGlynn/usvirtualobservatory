#
#
#
#
#
package Table::Message;
use Exporter;
@ISA = qw (Exporter);
use Table::ErrorMessage;
use Table::LoginBox;



use strict;
use lib '..';
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
    my $self = shift;
    $self->{note} = shift;
    $self->{data}   = shift;
    $self->{type} = "Message";
    $self->set_message();
}
sub set_message
{
    my $self  = shift;
    $self->{message} = "<table class = tac  align =center>" 
                       . "<tr class = titleblue>"
                       . "<td  align = center>$self->{type}</td>"
                       . "</tr>"
                       . "<tr class = greenln>"
                       . "<td>$self->{note}</td>"
                       . "</tr></table>";
}
sub display
{
    my $self = shift;
    print "Content-type: text/html\n\n" unless $self->{data}->{cgi}->{".header_printed"};    
    
    #top
    Table::Layout::gen_header_layout("$self->{type} Page", ["Monitor","notices","NVO Home","NVO Feedback"]);  
    
    #body
    my $loginbox = new Table::LoginBox($self->{data});
    $loginbox->printbox();
    my $message = $self->get_message();
    print $message;
    
    #footer
    Table::Layout::gen_footer_bas();
}
sub get_message
{
    my $self = shift;
    return $self->{message}; 
}
1;

