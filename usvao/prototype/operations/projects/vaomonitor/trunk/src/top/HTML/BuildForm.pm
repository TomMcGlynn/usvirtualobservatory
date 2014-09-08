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
	$string  = "\n<td valign='top'>\n"
                ."<Form  id = $lcname" ."_r"  . "name = $lcname action = \'./test.pl?\'>\n" 
	        ." <Select id  = \'$lcname"  . "_select\'>\n";
	foreach my $n (keys %$numandtestname)
	{		 	    
	    $string .= "   <option selected value = \'./test.pl?name=$name&testid=$n\'>$numandtestname->{$n}\n";	     
	}
	$string    .= " </Select></Form></td><td>"
                   .  "<input type = \"hidden\" name = \"sup.$lcname\" />\n"
	           .  "<a href   id  = ${lcname}.non onClick = \"getTestURL(\'$lcname\_r\',\'$lcname\_select\',\'image\_${lcname}\');return false;\">Test</a>\n"
      	           .  "<a href id  = ${lcname}.non  onClick = \"getRunURL(\'$lcname\_r\',\'$lcname\_select\');return false;\">&nbsp;Run</a>\n" 
                   . "<img id = \"image\_${lcname}\"  height=14 width =23 style = \"visibility:hidden\" src=\"./css/hourglass.png\">\n"
                   . "\n</td>\n";
    }
    else
    {	
	$string  = "\n<td valign='top'>\n<Form id = $lcname" . "_r" . " name  = $lcname action = \"./test.pl?\">\n"
	        . " <Select id = \'$lcname"  . "_select\' style=\"display:none\">\n";
	
	my @list = keys  %$numandtestname;
	my $n = pop @list;
	$string .= " <option selected value = \'./test.pl?name=$name&testid=$n\'>$numandtestname->{$n}\n"
	        ."</Select><font style = \"color:blue\">$numandtestname->{$n}</Form></td><td>\n"
	        ."<input type = \"hidden\" name = \"sup$lcname\"  />\n"
	        ."<a href id  = ${lcname}.non  onClick = \"getTestURL(\'$lcname\_r\',\'$lcname\_select\',\'image\_$n\_${lcname}\');return false;\">Test</a>\n"
                ."<a href id  = ${lcname}.non  onClick = \"getRunURL(\'$lcname\_r\',\'$lcname\_select\');return false;\">&nbsp;Run</a>\n"
                ."<img id = \"image\_$n\_${lcname}\"  height=14  width=23 style=\"visibility:hidden\" src = \"./css/hourglass.png\">\n"
                ."\n</td>\n";
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
