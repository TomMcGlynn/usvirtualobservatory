#
# Title class
# 
# creates title objects to
# be used at the tops of dynamically
# displayed tables
#
# 

package HTML::Title;

sub new
{
    my ($classname)  = shift;
    my $objectref    = {};
    bless $objectref, $classname; 
    $objectref->initialize(@_);
    return $objectref;
}
sub initialize
{
    my ($self)     = shift;  
    $self->{title} = wrap_text(@_);  
}
sub trim
{
    my ($string) = @_;
    $string =~ s/^\s+//g;
    $string =~ s/\s+$//g;
    return $string;
}
sub wrap_text
{   
    my $string1 = $_[0];  
    my $string2 = $_[1] if ($_[1]);
    my $newstring;
   

    if (($string1)  and (! $string2))
    {	
	$newstring = "Current errors for service: $string1";
	if ($string1 eq 'oldtests')
	{
	    $newstring = "Old Tests";
	}
	elsif ($string1 =~ /^Uptime*/)
	{
	    $newstring = $string1;
	}
    }
    elsif (($string1) and ($string2))
    {
	$newstring = $string1;
	if (ref($string2) eq 'HASH')
	{   
	    
	    if ($string1 eq 'Details')
	    {
		my $id = get_id_fromdb($string2->{sid},$string2->{dbh});
		$newstring = "$string1: $id";
	    }
	}
    }
    return $newstring;
}
sub get_id_fromdb
{
    my ($sid,$dbh) = @_;
    my $q = qq(select name from Services where serviceId = ?);
    my $sth = $dbh->prepare($q);
    $sth->execute($sid);

    my $data    = $sth->fetchall_arrayref();
    my $array   =  @$data[0];
    my $string  =  @$array[0];
    return $string;
    
}
sub get_title
{
    my $self = shift;
    return $self->{title};
}
1;
