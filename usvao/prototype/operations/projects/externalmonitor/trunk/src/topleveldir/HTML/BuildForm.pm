#
#
#
package BuildForm;

use strict;

sub new
{
    my ($class) = shift;
    my $hash = {};
    bless $hash, $class;
    $hash->build_form(@_);
    return $hash;
}
sub build_form
{
    my $self   = shift;
    my $name   = shift;    
    my $lcname =  lc($name);
    $lcname    =~ s/\s+//g;
    $lcname    =~ s/\///g;
    my $servicenames = shift;
    my $numandtestname = $servicenames->{$name};
    my $size =  keys %$numandtestname;
    
    my $inputelement = "    \n";
    
    my $string;
    if ($size > 1)
    { 
	$string = $string ."<Form  id = $lcname" ."_r"  . "name = $lcname action = \'./test.pl?\'>\n" 
	        ." <td><Select id  = \'$lcname"  . "_select\'>\n";
	            foreach my $n (keys %$numandtestname)
	            {		 	    
	               $string = $string ."<option selected value = \'./test.pl?name=$name&testid=$n\'>$numandtestname->{$n}";	     
	            }
	$string    = $string . " </Select></td></Form><td>"
                   ."<input type = \"hidden\" name = \"sup.$lcname\"/>\n"
	           ."<a href   id  = ${lcname}.non onClick = \"getTestURL(\'$lcname\_r\',\'$lcname\_select\',\'image\_${lcname}\');return false;\">Test</a>"
      	           ."<a href id  = ${lcname}.non  onClick = \"getRunURL(\'$lcname\_r\',\'$lcname\_select\');return false;\">&nbsp;Run</a>" 
                   ."<img id = \"image\_${lcname}\"  height=14 width =23 style = \"visibility:hidden\" src=\"./css/hourglass.png\"/>"
                   ."</td>\n";
    }
    else
    {	
	$string  = "\n<Form id = $lcname" . "_r" . " name  = $lcname action = \"./test.pl?\">"
	        ."<td><Select id = \'$lcname"  . "_select\' style=\"display:none\">";
	
	my @list = keys  %$numandtestname;
	my $n = pop @list;
	$string = $string . "<option selected value = \'./test.pl?name=$name&testid=$n\'>$numandtestname->{$n}"
	        ."</Select><font style = \"color:blue\">$numandtestname->{$n}</font></td></Form><td>"
	        ."<input type = \"hidden\" name = \"sup$lcname\"/>"
	        ."<a href id  = ${lcname}.non  onClick = \"getTestURL(\'$lcname\_r\',\'$lcname\_select\',\'image\_$n\_${lcname}\');return false;\">Test</a>"
                ."<a href id  = ${lcname}.non  onClick = \"getRunURL(\'$lcname\_r\',\'$lcname\_select\');return false;\">&nbsp;Run</a>"
                ."<img id = \"image\_$n\_${lcname}\"  height=14  width=23 style=\"visibility:hidden\" src = \"./css/hourglass.png\">"
                ."</td>";
    }
    $self->{form} = $string;
}
sub display
{
    my $self = shift;
    print $self->{form};
}
sub getForm
{
    my $self = shift;
    return $self->{form};
}
1;
