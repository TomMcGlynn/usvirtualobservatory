#
#
#
#  ParentXML class to parse the XMLs
#  from the VO registry.
#  Children of this class inherit 
#  the methods defined here.
#
#
# 
package Service::ParentXML;
use Service::RegistryXML;
use Service::SiapXML;
use Service::ConeXML;
use Service::SsaXML;
use Service::GenericClass;



use strict;
sub init
{
    my ($res, $xmlobj,$name,$string) = @_;
  
    my $n = "Service::$name";
    my $child = new $n($res,$xmlobj, $string);
    $child->process_nodes();
    $child->cleanup();
    return $child;

}
sub get_children_nodes
{
    my $self = shift;
    return $self->{res};

}
sub trim
{
    my ($string) = @_;
    $string =~ s/^\s+//mg;
    $string =~ s/\s+$//mg;
    return $string;
}
sub get_xmlobject
{
    my $self = shift;
    return $self->{xmlobj};
}
sub cleanup
{
    my $self  = shift;
    foreach my $n (keys %$self)
    { 
        if (! (defined($self->{$n})))
	{
	    $self->{$n} = 'null';
	}		
	$self->{$n}  = trim($self->{$n});
	  
	
    }  
}
sub initialize
{
    my $self = shift; 
    $self->{res} = $_[0];
    
    $self->{xmlobj} = $_[1];
    $self->{string} = $_[2];
    $self->{shortname} = "null";
    $self->{id}        = "null";
    $self->{sr}        = "null";
    $self->{ra}        = "null";
    $self->{dec}       = "null";
    $self->{base}      = "null";
    $self->{role}      = "null";
    $self->{xsitype} = "null";
    $self->{errorinurl}  = "null";
    $_[3];
}
sub get_errorinurl
{
    my $self = shift;
    return $self->{errorinurl};
}
sub getshortname
{
    my $self = shift;
    return $self->{shortname};
}
sub getid
{
    my $self =shift;
    return $self->{id};

}
sub setxsi
{
    my ($self,$xsi) = @_;
    $self->{xsitype}  = $xsi;

}

sub get_types
{
    my $self = shift;
    return $self->{string};
}
sub getra
{
    my $self = shift;
    return $self->{ra};
}
sub getdec
{
    my $self = shift;
    return $self->{dec};
}
sub getradius
{
    my $self = shift;
    return $self->{sr};
    
}
sub getxsitype
{
    my $self = shift;
    return $self->{xsitype};

}
sub getrole
{
    my $self = shift;
    return $self->{role};
}
sub getbase
{
    my $self = shift;
    return $self->{base};
}
sub setbase
{
    my ($self, $url)  = @_;
    $self->{base} = $url;
    
}
sub getxmltype
{
    my $self =shift;
    return $self->{input};
}
sub error_ornot
{
    my ($self, $url)  = @_;
    $url   = trim($url);
 
    #see if this is an invalid url
    if ( ($url !~ /(\?)$/) and  ($url !~ /(\&amp;)$/))
    {
	
	print "Does have chars:$url\n";
	$self->set_errorinurl($url);	
    }
 
}
sub fix_url
{   
    my ($self, $url) = @_;
    $url   = trim($url); 
    #is there a "?" anywhere
    if ($url =~ /\?/)
    {
	#there is a "?" somewhere in the url	
	if ($url =~ /[\?\&]$/)
	{	    
	   # print "URL correct:$url\n";
	}      
	elsif ($url =~  /\&amp\;$/)
	{
	    #$url =~ s/\&amp\;$/\&/;
	}
	else
	{
	    $url = $url . "&amp;";
	}	
    }
    else
    {
	$url  = $url ."?";	
    }
    return $url
}
sub set_errorinurl
{
    my ($self,$url)  = @_;
    #print "JQ: $url\n";
    $self->{errorinurl} = $url;

}
1;
