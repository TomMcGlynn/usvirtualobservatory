#
#
#
# Registry XML parsing class
#
#
#

package Service::TapXML;
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
    $child->{input} = 'tap';
   
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
        
	
        elsif ($subres->name eq "capability")
        {          
	    
	    my $standardid = '';
	    $standardid = $subres->attribute('standardID') if ($subres->attribute('standardID'));
            
	    
	    if ($regstandard eq $standardid)           
	    {
		$child->{xsitype} = "tap:TableAccess";	
		$child->{role} = 'std';
                my @capchildren  = $subres->children;
                foreach my $ch (@capchildren)             
                {                   
                    my $c = $ch->name;
		    
                    if (($c) && ($c =~ /(.*?)interface(.*)/s))
                    {
			
			if ($ch->child("accessURL"))
			{
			    my $url = $ch->child("accessURL")->value;
			    $child->{base} = $url;
			    $child->error_ornot($url);
			    #$child->{base} = $child->fix_url($url);
			    
			}
                    }
		                       
                         		  		    
		}
	    }
	}   
    }
    $child->{sr} = ".3";
    $child->{ra} = "0" if ($child->{ra} eq "null");
    $child->{dec} = "0" if ($child->{dec} eq "null");
}
1;
