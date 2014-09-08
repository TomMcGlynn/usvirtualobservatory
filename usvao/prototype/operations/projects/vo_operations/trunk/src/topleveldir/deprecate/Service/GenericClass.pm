#
#
#
# Registry XML parsing class
#
#
#

package Service::GenericClass;
use Exporter ();
@ISA = qw(Exporter);
@ISA = ("Service::ParentXML");

use warnings;

sub new 
{
    
    my ($class) = shift;   
    my $objectref= {};    
    bless $objectref, $class;
    $objectref->initialize(@_);
    return $objectref;
}
sub process_nodes
{
    my ($child) = shift;
    my $capability_node = 0;
    $child->{input} = 'cone';
   
    local $^W =0;
    my $res           = $child->get_children_nodes();
    my $xmlobj        = $child->get_xmlobject();
    my $typestring  = $child->get_types();
    my ($regxsi, $regstandard)   = (split /\|/, $typestring);

   
   

    
    foreach my $subres (@$res)
    {
       
        
        #print "SS: $subres\n";
        if($subres->name eq "shortName")
        {

           $child->{shortname} = $xmlobj->xpath_search("/xml/Resource/shortName")->value;
        }
        elsif ($subres->name eq "identifier")
        {
            #get ivo identifier
            $child->{id} =  $xmlobj->xpath_search("/xml/Resource/identifier")->value;
        }
	elsif ($subres->name eq 'curation')
        {
            if ($xmlobj->xpath_search("/xml/Resource/curation/contact/email"))
            {
                $child->{email} =  $xmlobj->xpath_search("/xml/Resource/curation/contact/email")->value;
            }
            else
            {
                $child->{email} = "no address listed";
            }                       
	    
	    
	}
	
    }
    $child->{sr} = ".3";
    $child->{ra} = "0" if ($child->{ra} eq "null");
    $child->{dec} = "0" if ($child->{dec} eq "null");
}
1;
