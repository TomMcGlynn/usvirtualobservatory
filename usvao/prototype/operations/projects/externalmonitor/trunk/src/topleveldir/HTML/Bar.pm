#
#
# create a Bar object for the
# vaomonitor pages
#
package HTML::Bar;


sub new 
{
    my ($class) = shift;
    my $hash = {};
    bless $hash, $class;
    $hash->init(@_);
    return $hash;
}
sub init
{
    my $self = shift;
    $self->{title}     = $_[0]; 
    $self->{classname} = $_[1];
    $self->{cols}      = $_[2];
    $self->{tag}       = $_[3];
    $self->{type}      = $_[4]  if ($_[4]);
    
    
}
sub print_bar
{
    my $self = shift;
    my $title = $self->{title};
    $buf = "";
    $buf   = "<tr " .  ' class = ' . $self->{classname} . '>'  if ($self->{tag} eq 'tr');   
    
    $buf  .= '<td ';
    #if  ($self->{cols} ne '' and $self->{cols} >0)
    $buf  .= 'colspan = '  . $self->{cols} if (($self->{cols} ne '') and ($self->{cols} > 0));
    $buf   .= ' class = ' . $self->{classname}  if (($self->{tag} eq 'td') and ($self->{cols} ne ''));
    $buf  .= ' width = 250' if ($title eq 'Service name');
    $buf .= '>'  . $self->{title}  . '</td>'; 
    $buf .= '</tr>' if ($self->{tag} eq 'tr');
    print $buf;
}
sub start
{
    my $self = shift;
    print "<tr class = ";
    print $self->{classname};
    print ">";
}
sub end
{
    my $self = shift;
    print "</tr>";
}
1;
