#!/usr/bin/perl -wT
#
#
#
# name: vometadata.pl
#
# description: this program retrieves metadata for
#              a given VO Resource and displays it.
#              It is meant to be used with the vresults.pl tool.
#              This is a cgi wrapper around the program:
#              /www/server/vo/voclient/voregistry.
#
#
{
    use strict;
    use lib '/www/htdocs/vo/validation/';
    use lib '/www/htdocs/vo/monitor/';
    use Switch;
    use HTML::ErrorMessage;
    use CGI;
    use HTML::Layout;
    use CGIVO::VOMonutil;
    $ENV{PATH} = '/www/server/vo/voclient/:';

    my $cgi = CGI->new();
    my $id =  detaint("id",$cgi->param("id"));
    print "Content-type: text/HTML\n\n";
    
   

    my $cline_tool = 'vodirectory';
    my $flag  = '--list';
	    
    my $content = get_metadata($id,$cline_tool,$flag);
    my ($snippetA,$snippetB,$snippetC) = parse_metadata($content);
    display($snippetA, $snippetB,$snippetC);
    exit(1);
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
            else{ $status = 1;} 
	}
    }
    if ($status)
    {
        my $error = new HTML::ErrorMessage("The parameter or value entered is not recognized");
        $error->display();
        exit();
    }
    return $value;


}
sub get_metadata
{
    my ($id, $cline_tool,$flag) = @_;
    my @content = `$cline_tool  $flag  $id`;
   
    return \@content;
}
sub parse_metadata
{
    my ( $content) = @_;
    my ($beginning, $middle,$end,$posB);
    
    my $posA = 0;

    for (my $i = 0;$i<scalar(@$content);$i++)
    {   
	$$content[$i] = trim($$content[$i]);
	if ($$content[$i] =~ /^Description:(.*)/)
	{
	    $posA = $i;	    
	}
	if ($$content[$i] =~ /^Creator:(.*)/)
	{
	    $posB = $i;
	}	
    }
    
    #if no description element, return everything
    if ($posA == '0')
    {	
	return $content,"","";
    }
    else
    {
	my @newarray = splice(@$content,0,$posA);      
	my @middle   = splice(@$content,0,$posB-$posA);
	$middle      = do {local $/; join("",@middle);};	
	my @end      = splice(@$content,0,);	
	return \@newarray, $middle,\@end;
    }
}
sub display
{
    my ($snippetA, $snippetB,$snippetC) = @_;
    my $array = ['Monitor','notices','NVO Home','NVO Feedback','Validation'];
    gen_header_layout("VO Metadata", $array);
    print "<table class = 'tac' align = center  width = '600'>";
    
    #if snippetB,snippetA empty, print everything
     
    foreach my $n (@$snippetA)
    {	
	print "<tr class = 'greenln'><td align = left >$n</td></tr>";
    }

    if (($snippetB ne '') or ($snippetC ne ''))
    {
	print "<tr class = 'greenln'><td align = left>$snippetB</td></tr>";
	foreach my $n (@$snippetC)
	{
	    print "<tr class = 'greenln'><td align = left >$n</td></tr>";
	}	
    }
    print "</table>";
    HTML::Layout::gen_footer_bas();
}
