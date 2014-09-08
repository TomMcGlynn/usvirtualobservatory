#
#
# A service object (from the registry
# records). A service object has
# various properties. This class can be
# used to build different types of service
# objects. 
#
#
#
package Service::Service;



use Exporter ();
@ISA = qw(Exporter);

@EXPORT = qw (get_baseurl get_id  get_type  get_valurl get_ra get_dec get_sr new);



use strict;
use warnings;
sub new 
{

    my ($class)  = shift;
    my $hash = {}; 
    bless $hash,$class; 
    $hash->initialize(@_);
    return  $hash;
}
sub initialize
{
     my $self = shift;	
        my @a   =  @{$_[0]};
	$self->{_shortname} =  $a[0],
	$self->{_baseurl}   = $a[2],
	$self->{_id}        = $a[1],
	$self->{_type}      = $a[3],
	$self->{_ra}        = $a[4],
	$self->{_dec}       = $a[5],
	$self->{_sr}        = $a[6],
	$self->{_role}      = $a[7],
	$self->{_xsitype}   = $a[8],
	$self->{_urlerror}  = $a[9],
    
}

sub get_baseurl  { $_[0]->{_baseurl}}
sub get_id       { $_[0]->{_id}}
sub get_type     { $_[0]->{_type}}
sub get_ra       { $_[0]->{_ra}}
sub get_dec      { $_[0]->{_dec}}
sub get_sr       { $_[0]->{_sr}}
sub get_role     { $_[0]->{_role} }
sub get_xsitype  { $_[0]->{_xsitype}}
sub get_shortname { $_[0]->{_shortname}}
sub get_urlerror  {$_[0]->{_urlerror}}

sub get_basewithparams  
{ 
    my ($self)  = @_;
    my $newbase;
    if ($self->{_type} eq "cone")
    {       	
	$newbase = $self->{_baseurl} . "RA=" . $self->{_ra} . "&DEC=" . $self->{_dec} .  "&SR="  . $self->{_sr};	
    }
    elsif  ($self->{_type} eq "siap")
    {
	$newbase = $self->{_baseurl} . "RA=" . $self->{_ra} . "&DEC=" . $self->{_dec} . "&RASIZE=" . $self->{_sr} . "&DECSIZE=" . $self->{_sr};
    }
    $newbase = $newbase . "&format=xml&show=fail&op=Validate";
    return $newbase;
}
sub get_validateurl
{
    my ($self) = @_;
    my $newbase = get_basewithparams($self);
    my $valurl;
    $valurl = "http://nvo.ncsa.uiuc.edu/dalvalidate/ConeSearchValidater?endpoint=$newbase" if ($self->{_type} eq "cone");
    $valurl = "http://nvo.ncsa.uiuc.edu/dalvalidate/SIAValidater?endpoint=$newbase" if ($self->{_type} eq "siap");
    $valurl = "http://rofr.ivoa.net/regvalidate/HarvestValidater?endpoint=$newbase" if ($self->{_type} eq "registry");
    return $valurl;
}
sub print_validateurl
{
    my ($self) = @_;
    print $self->get_validateurl, "\n";
   
}
1;
