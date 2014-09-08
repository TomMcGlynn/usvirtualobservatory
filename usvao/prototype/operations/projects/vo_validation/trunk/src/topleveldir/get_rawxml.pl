#!/usr/bin/perl -wT
#
#
# Description: get raw xml for 
#              a unique identifier from the
#              registry 
#
#
# Usage: get_xml.pl?type=xxx&id=yyy 
#      : requires "type" and "id" values
#      : type is either siap,cone,or ssa,registry
#      : id is an ivo identifier
# 
# E.g.
# http://heasarcdev.gsfc.nasa.gov/vo/validation/get_rawxml.pl?type=siap&id=ivo://nasa.heasarc/skyview/skyview
#
#

{
    use strict;
    use lib "/www/htdocs/vo/validation";

    use CGI;
    use LWP::UserAgent;
    use HTML::ErrorMessage;
    use HTML::Layout;
    use Switch;
    my $cgi = CGI->new();
    my $ua  = LWP::UserAgent->new();
		   
    
    
    #handle pars
    my $id   = detaint("id",$cgi->param("id"));
    my $type = detaint("type",$cgi->param("type"));


    
    print "Content-type: text/xml\n\n";
  


    #read unique registry queries from external file
    my $queries ={}; 
    open (File, "./perl/data/downloadtypes") 
	|| die "cannot open downloadtypes";

    my @array = <File>;
    foreach my $n (@array)
    {
	my @a = (split "\\|", $n);
	my $type = shift @a;
	$a[2] =~ s/\"//g;
	
	$queries->{$type} = $a[2];	
    }
    close File|| 
	die "cannot close downloadtypes"; 
    
   
    my $query = $queries->{$type};
    
    
    $query    = "$query and identifier = \'$id\'";
    
    #get response
    my $response = $ua->get($query);
    my $cont = $response->content;
    print "$cont";
   
}
sub detaint
{
    my ($parname, $value) = @_;
    my $status;

        
    switch($parname)
    {
        case "id" 
        { 
            if  ($value =~  /(ivo:\/\/.*[^ \<\>\;])/){$value = $1;}               	
	    else {$status = 1;} 
	}
	case "type"
	{ 
	    if ($value =~ m/(cone|siap|ssa|registry)/)
	    {$value = $1;}
	    else {$status = 1;}
	}
    }
    if ($status)
    { 
	#my $error = new HTML::ErrorMessage("The parameter or value entered is not recognized");
        #$error->display();
        #exit();
    }
    return $value;

}

