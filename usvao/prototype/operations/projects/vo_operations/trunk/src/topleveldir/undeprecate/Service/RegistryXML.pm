#
#
#
# Registry XML parsing class
#
#
#

package Service::RegistryXML;
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
    $child->{input} = 'registry';
   
    local $^W =0;
    my $res           = $child->get_children_nodes();
    my $xmlobj        = $child->get_xmlobject();
    my $typestring    = $child->get_types();
    
    print "AA: $typestring\n";
    my @regtypes  =  (split /\;/,$typestring);
    my $regtypehash = {};


    foreach my $r (@regtypes)
    {   	
	my ($regxsi, $regstandard)   = (split /\|/, $r);
	$regtypehash->{$regxsi} = $regstandard;
    }


    #process the elements
    my $count =0;
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
        
	
        elsif ($subres->name eq "capability")
        {          	   
            my $xsi = $subres->attribute('xsi:type');	    
            my $standardid = $subres->attribute('standardID');
       	               
		
            if (($xsi ) && ($regtypehash->{$xsi}) && ($regtypehash->{$xsi} eq $standardid))           
            {
	                                         
                $child->{role} = 'std';
                my @capchildren  = $subres->children;
                foreach my $ch (@capchildren)             
                {                   
                    my $c = $ch->name;
		    
                    if (($c) && ($c =~ /(.*?)interface(.*)/s))
                    {
		       
		        if (($child->{base} ne "null" ) && ($child->{xsitype} ne "null"))
			{			  		       
			    $child->{base} = $child->{base} . "\\"  .  $ch->child("accessURL")->value;
			    $child->{xsitype} = $child->{xsitype} . "\\" . $xsi; 		    			    			   
			}
			else
			{
			    $child->{base} = $ch->child("accessURL")->value;
			    $child->{xsitype} = $xsi;	   
			}
			last;
                    }                        		    
		}
            }
        }
    }    	
    
}
1;
