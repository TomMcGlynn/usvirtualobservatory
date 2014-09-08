#  
#  Author:        Michael Preciado
#
#  Description:   Parent class that assists in the building
#                 of validation URLs to be used on the validation
#                 web interface.from the VO registry. Children of 
#                 this class inherit the methods defined here.
#
#
# 
package HTML::Parent;

use HTML::Cone;
use HTML::SIAP;
use HTML::Util;
use HTML::RegistryHarvest;
use HTML::SSA;
use HTML::TAP;
use SQL::Queries;
use Connect::MySQLValidationDB;



     
sub init
{
    my ($url,$type,$id,$valURL,$test_dec,$test_ra,$sr) = @_;
    my $name        = "HTML::$type";
    my $child       = new $name($url, $type,$id,$valURL,$test_dec,$test_ra,$sr);
    return $child;
}
sub trim
{
    my ($string) = @_;
    $string =~ s/^\s+//g;
    $string =~ s/\s+$//g;
    return $string;
}
sub initialize
{
    my $self        = shift;
    my $url         = $_[0];    
    $self->{type}   = $_[1];
    $self->{id}     = $_[2];
    $self->{url}    = $url; 
    $self->{valURL} = $_[3];
    $self->{dec}    = $_[4];
    $self->{ra}     = $_[5];
    $self->{sr}     = $_[6];	

}
sub fix_base
{
    my ($base) = @_; 

    $base =~ s/\&amp\;/\&/g;
    return $base;
}
sub fix_url
{   
    my ($self,$url) = @_;
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
            $url = $url . "&";
        }       
    }
    else
    {
        $url  = $url ."?";      
    }
    return $url
}
sub getvoresourceurl
{
    my $child = shift;
    my $base = "http://rofr.ivoa.net/regvalidate/VOResourceValidater?record=&record=&recordURL=";
    my $regurl = encode("http://nvo.stsci.edu/vor10/getRecord.aspx?id=$child->{id}&format=xml");
    my $datastring = "&recordURL=&format=xml&show=fail";
    $base = $base . $regurl . $datastring;
    return $base; 
}
1;
