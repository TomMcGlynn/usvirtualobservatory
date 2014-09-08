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

    my ($class)  = @_;
    return bless  
    {
	_shortname => $_[1],
	_baseurl   => $_[3],
	_id        => $_[2],
	_type      => $_[4],
	_ra        => $_[5],
	_dec       => $_[6],
	_sr        => $_[7],
	_role      => $_[8],
	_xsitype   => $_[9],
	_urlerror  => $_[10],
    }, $class;
}
sub new_alternate
{

    
    my ($class)  = @_;
    return bless  
    {
	_shortname => $_[1],
	_baseurl   => $_[2],
	_id        => $_[3],
	_type      => $_[4],
	_xsitype   => $_[5],
	_email     => $_[6],
	_vallevel  => $_[7],
   
    }, $class;


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
sub get_email     {$_[0]->{_email}}
sub get_vallevel  { $_[0]->{_vallevel}}


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
