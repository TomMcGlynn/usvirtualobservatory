#
#
#
#
#
package HTML::ErrorMessage;

use strict;

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
    my ($self) =shift;
    my $note = shift;
   
    $self->{message} = "<table class = tac width = 300 align =center>" 
                       . "<tr class = titleblue><td  align = center>" 
                       . "Error</td></tr><tr class = greenln><td>$note</td></tr></table><br><br>";      
 
    return $self;               
}
sub display
{
    my ($self) = shift;
#   print "Content-type: text/html\n\n";      
    
    #top
    HTML::Layout::gen_header_layout("ErrorPage", ["Monitor","notices","NVO Home","NVO Feedback", "Validation"]);  
    
    #body
    print  $self->{message};
    
    #footer
    HTML::Layout::gen_footer_bas();
}
1;

