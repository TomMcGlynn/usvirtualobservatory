#
#
#
#
package HTML::Util;

use Exporter ();
@ISA = qw (Exporter);
@EXPORT = qw (encode getrand encode_array);

sub encode 
{
    my($string) = shift(@_);
    #$string =~ s/([^A-Za-z0-9])/sprintf("%%%02X", ord($1))/seg;
    $string =~ s/([^ 0-9A-Za-z])/sprintf("%%%02X",ord($1))/ge;
    $string =~ tr/ /+/;
    return($string);
}
sub getrand
{
    my $range = 9000;
    my $randnum = int(rand($range));
    return $randnum;

}
sub encode_array
{
   my ($array) = @_; 
   my $arraynew = [];
   foreach my $n (@$array)
   {
     	
       $n  =~ s/</&lt;/g;
        $n =~ s/>/&gt;/g;
       #rint "CC  $n<br>"; 
        #my $g   = encode($n);
        push @$arraynew,$n;    	 
    }
    return $arraynew;

}
