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
sub load_centernames
{
    my %centers;
    open (C,$::centernames) || die "cannot open centernames"; 
    my @array  = <C>;
    close C;
    foreach my $n (@array)
    {
	chomp $n;
	my ($id,$shortname) = (split /\,/, $n);
	$id = trim($id);
	$centers{$id} = $shortname;
    }
    return \%centers;
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
    my $string2 = $_[1];
    my $string3 = $_[2];
   
    my $newstring;
    my $centers = load_centernames();

    if (($string1)  and (! $string3))
    {	
	my $shortname = $string1;
       
        $shortname = $centers->{$string1} if ($centers->{$string1});
	
	if ($string1 eq 'oldtests')
	{
	    $newstring = "Old Tests";
	}
	elsif ($string1 eq 'View Last Set Tested')
	{
	    $newstring   = "Last Set Tested"; 
	}
	else
	{ 
	 
	    $newstring = "Current Error types for center: $shortname";
	    if  (scalar(@$string2) <= '4') 
	    {
		$newstring = "Error types for center: $shortname";		
	    }
	}

	
    }
    elsif (($string1) and ($string3))
    {

	
	$newstring = $string1;
	if (ref($string3) eq 'HASH')
	{
      
	    if ($string1 ne  'Details')
	    {			 
      	       
		$newstring = "Current Errors: $string1 (Services matching error: $string3->{error})";
	    }
	    else
	    {
	      
		my $id = get_id_fromdb($string3->{sid},$string3->{dbh});
		$newstring = "$string1: $id";
	    }
	}
    }
    return $newstring;
}
sub get_id_fromdb
{
    my ($sid,$dbh) = @_;
    my $q = qq(select ivoid from Services where serviceId = '$sid');
    my $sth = $dbh->prepare($q);
    $sth->execute;

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
