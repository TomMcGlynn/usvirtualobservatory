#
#
#
# Registry XML parsing class
#
#
#

package Service::ConeXML;
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
 
   
        my $count = 1;
    foreach my $node (@$res)
    {

        if ($node->name eq "shortName")
        {
           $child->{shortname} = $xmlobj->xpath_search("/xml/Resource/shortName")->value;
        }
        elsif ($node->name eq "identifier")
        {
            #get ivo identifier
            $child->{id} =  $xmlobj->xpath_search("/xml/Resource/identifier")->value;
	    $child->{idorig} = $xmlobj->xpath_search("/xml/Resource/identifier")->value;
        }
	elsif ($node->name eq 'curation')
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
        elsif ($node->name eq "capability")
        {          
            
            my $xsi = $node->attribute('xsi:type');
            my $standardid = $node->attribute('standardID');
            
           
            if (($xsi ) && ($xsi  ne $regxsi))
            {         	
                $node->delete;
                $capability_node++;
                next;      
            }
            elsif (($xsi ) && ($xsi eq $regxsi) && ($regstandard eq $standardid))           
            {
              
                #$child->{xsitype} = $xsi;                      
                $child->{role} = 'std';
                my @capchildren  = $node->children;
                foreach my $ch (@capchildren)             
                {                   
                    my $c = $ch->name;
                    if (($c) && ($c =~ /(.*?)interface(.*)/s))
                    {
                        if (($child->{base} ne "null" ) && ($child->{xsitype} ne "null"))
                        {   print $ch->child("accessURL")->value;                                           
                            $child->{base} = $child->{base} . "\\"  .  $ch->child("accessURL")->value;
                            $child->{xsitype} = $child->{xsitype} . "\\" . $xsi;                                                                   
                            $child->{id} = $child->{id} . "\\" . $child->{idorig} . "#$count";                      
                           print "SSWE: $child->{id}\n";
                            print "unbde : $child->{xsitype}\n";
                            $count++; 
                        }
                        else
                        {
                            $child->{base} = $ch->child("accessURL")->value;
                            $child->{xsitype} = $xsi;
                                  
                        }
			#$child->error_ornot($url);
			#$child->{base} = $child->fix_url($url);
                    }
		    elsif ($c  eq "testQuery")
                    {
                                                                     
			$child->{ra}  = $ch->child("ra")->value;
			$child->{dec} = $ch->child("dec")->value;
		    }                      
                   #print "CC: $child->{base}\n"; 
                         		  		    
		}
	    }
	}   
    }
    $child->{sr} = ".3";
    $child->{ra} = "0" if ($child->{ra} eq "null");
    $child->{dec} = "0" if ($child->{dec} eq "null");
}
1;
