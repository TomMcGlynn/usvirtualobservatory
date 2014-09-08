###
#
#
# Description:  A wrapper for XML::Parser. This
#               program processes a raw xml from the STScI
#               registry (containg entities) and determines 
#               if the xml is valid. 
# 
# Function: respond with xml parsing status and store
#           the ivoid for use in the calling program.
#
#
package Service::ParserWrap;
use XML::Parser;


use strict;
use warnings;
our $id;
our $message;
sub new
{
    my ($class) = shift;
    my $hashref= {};
    bless $hashref, $class;
    $hashref->init(@_); 
    return $hashref;
}
sub init
{
    my $self = shift;
    my $xml = fix_xml($_[0]);
    $self->{xml}  = $xml;    
    $self->{status} = $self->parse($xml);
    $self->{id}    = $id;
    return $self;
}
sub getid
{
    my $self = shift;
    return $self->{id};
}
sub getxml
{
    my $self = shift;
    return $self->{xml};
}
sub parse
{ 
    my $self = shift;
    my ($xml) = @_;
    my $parser  = new XML::Parser(
				  Handlers => {                                       
				                Start => \&hdl_start,
                                                Char  => \&hdl_char,
				                End   => \&hdl_end,                                              
					      }
				  );        
    eval 
    {
	$parser->parse($xml);
    };
    return "passed" if  (! $@);
    #print $@; #debug

}
sub hdl_end
{
    my ($p,$elt) = @_;
    if ($elt eq 'identifier')
    {
	$id = $message->{'str'}; 
    }
    undef $message;
}
sub hdl_start
{
    my ($p, $elt, %atts) = @_;
    return if ($elt ne 'identifier');
    $atts{'str'} = '';
    $message = \%atts;
}
sub hdl_char
{
    my ($p, $str) = @_;
    $message->{'str'} .= $str;
}
sub getstatus
{
    my $self = shift;
    return $self->{status};
}
sub fix_xml
{   
    my ($xml) = @_;
    $xml      =~ s/&lt;/\</g;
    $xml      =~ s/&gt;/>/g;
    $xml      =~  s/(\w+):Resource/Resource/g;    
    $xml      =~ s/vor:Resource/Resource/g;
    return $xml;
}
1;
