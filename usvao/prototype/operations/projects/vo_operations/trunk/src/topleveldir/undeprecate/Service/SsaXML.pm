#
#
#
# Registry XML parsing class
#
#
#

package Service::SsaXML;
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
    $child->{input} = 'ssa';
   
    local $^W =0;
    my $res           = $child->get_children_nodes();
    my $xmlobj        = $child->get_xmlobject();
    my $typestring  = $child->get_types();
    my ($regxsi, $regstandard)   = (split /\|/, $typestring);
    
   
   

    
    foreach my $subres (@$res)
    {  
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
        elsif ($subres->name eq "capability")
        {          
	    
            my $xsi = $subres->attribute('xsi:type');
            my $standardid = $subres->attribute('standardID');
               
            if (($xsi ) && ($xsi  ne $regxsi))
            {         	      
                $subres->delete;
                $capability_node++;
                next;      
            }
            elsif (($xsi ) && ($xsi eq $regxsi) && ($regstandard eq $standardid))           
            {
		
                $child->{xsitype} = $xsi;                      
                $child->{role} = 'std';
                my @capchildren  = $subres->children;
                foreach my $ch (@capchildren)             
                {                   
                    my $c = $ch->name;
		    
                    if (($c) && ($c =~ /(.*?)interface(.*)/s))
                    {
			  
                        my $url= $ch->child("accessURL")->value;
			$child->{base} = $url;
                        $child->error_ornot($url);
                        #$child->{base} = $child->fix_url($url);		
                        #$child->{base} = $ch->child("accessURL")->value;
			                  
                    }
		    elsif ($c  eq "testQuery")
                    {
                        if (($ch->{pos}) and ($ch->{dec}))
			{
			    
			    $child->{ra}  = $ch->child("pos")->child("long")->value;
			    $child->{dec} = $ch->child("pos")->child("lat")->value;
			}
			else
			{
			    $child->{ra} = "0";
			    $child->{dec} = "0";
			}
		    }                       
                         		  		    
		}
	    }
	}   
    }
    $child->{sr} = ".3";
    $child->{ra} = "0" if ($child->{ra} eq "null");
    $child->{dec} = "0" if ($child->{dec} eq "null");
    $child->{xsitype} = "ssa:SimpleSpectralAccess" if ($child->{xsitype} eq "null");
}
1;
