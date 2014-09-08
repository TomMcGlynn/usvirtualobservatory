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
    
  
    my $hash = {};
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
        elsif ($subres->name eq "capability")
        {          
	    
            my $standardid = $subres->attribute('standardID');
            
#print "GG: $xsi, $regxsi, $standardid, $regstandard\n";         	    
            if (($standardid) && ($standardid ne $regstandard))
            {         	      
                $subres->delete;
                $capability_node++;
                next;      
            }
            elsif (($standardid) && ($regstandard eq $standardid))           
            {
		                                     
                $child->{role} = 'std';
                my @capchildren  = $subres->children;
                foreach my $ch (@capchildren)             
                {                   
                    my $c = $ch->name;
		    
                    if (($c) && ($c =~ /(.*?)interface(.*)/s))
                    {
			my  $version = $ch->attribute("version");

			#store urls and version numbers
			if ($version)
			{			    
			    $hash->{$version} = $ch->child("accessURL")->value;
			}
			else
			{
			    #store other url 
			    $hash->{'0'} = $ch->child("accessURL")->value;
			}
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
    
    my $accessurl;
    
    foreach my $n (sort {$b <=> $a}  keys %$hash)
    {	
	$accessurl = $hash->{$n};
	last;
    }

    if($child->{xsitype} eq 'null')
    { 
	$child->{xsitype} = "ssa:SimpleSpectralAccess";

    }
    
    $child->{base} = $accessurl;
    $child->error_ornot($accessurl);
    $child->{sr} = ".3";
    $child->{ra} = "0" if ($child->{ra} eq "null");
    $child->{dec} = "0" if ($child->{dec} eq "null");
    $child->{xsitype} = "ssa:SimpleSpectralAccess" if ($child->{xsitype} eq "null");
}
1;
