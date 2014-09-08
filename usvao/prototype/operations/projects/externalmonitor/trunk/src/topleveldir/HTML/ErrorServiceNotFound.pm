#
#
#
#
#
package HTML::ErrorServiceNotFound;
use HTML::Table;

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
                       . "Error</td></tr><tr class = greenln><td>$note</td></tr></table>";	
 
    return $self;		
}
sub display
{
    my ($self) = shift;
    print "Content-type: text/html\n\n";      
    
    #top
    HTML::Table::add_top();  
    
    #body
    print  $self->{message};
    
    #footer
    HTML::Table::add_footer();
}
1;

