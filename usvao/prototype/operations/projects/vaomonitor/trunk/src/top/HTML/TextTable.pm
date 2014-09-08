#
#
#
#
#
package HTML::TextTable;

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
    my ($res,$mimetype,$status,$message)  = @_;
    $self->{res}      = $res;
    $self->{mimetype} = $mimetype;
    $self->{status}   = $status;
    $self->{width}    = '200'        if ($status ne 'fail');
    $self->{width}    = '400'        if ($status eq 'fail');
    $self->{class} = "noteC"         if ($status eq 'fail');
    $self->{class} = "passedMessage" if ($status ne 'fail');
    $self->{greenfont} = "greenln";
    
    $self->{message}      = $message;
    return $self;
}
sub display
{
    my ($self) = shift;  
    #body
    print "$self->{message}";
    
}
sub add_top
{ 

}
sub add_footer
{
   
}
1;

