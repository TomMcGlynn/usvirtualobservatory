#!/usr/bin/perl -w
#
#
#
#
{
    
    use strict;
    use lib '/www/server/vo/external_monitor';
    use lib '/www/htdocs/cgi-bin/W3Browse/lib';
    use DBI;  
    use CGI;
    use Connect::MySQLVaomonitorDB;
    use HTML::Top;
    use SQLVo::Queries;
    use HTML::BuildForm;
    use HTML::VOheader;
    use HTML::Table;
    use HTML::Bar;
    use data::startup;
    print "Content-type: text/html\n\n";
    
    my $dbh = vaomonitor_connect();
     
    my $array = ['VAO Monitor'];
    my $voh = new HTML::VOheader('External Services Monitor', $array);	
    $voh->printheader(); 
    

    my $servicenames;
    $servicenames = getServicenamesTestid($dbh,"External");
    my $data      = getData($dbh);
    my $j =  keys %$servicenames;
    use integer;
    my $rowlimit = $j/2;
    my $remainder = $j%2;
    $rowlimit++ if ($remainder == '1');
    no integer;
   
    print "<table class = 'tac' align = center>\n";
    print "<tr class = greenln><td>*Services provided by institutions affiliated with the VAO are shown in green</td></tr>";
    print "</table>";
    print "<br>";
   
    print "<table class = 'tac'  align = center >\n";
    #print "<tr><td  valign = top>\n";
    
     
    my $bar =  new HTML::Bar("Interactive Testing of External Services", "titleblue", "8","tr");
    $bar->print_bar();
    print "\n";

    #table 
    $bar =  new HTML::Bar("Center", "label", "","td");
    $bar->start();
    $bar->print_bar();    
    $bar =  new HTML::Bar("Home Inst", "label", "","td"); 
    $bar->print_bar();    
    $bar =  new HTML::Bar("Service Type", "label", "","td"); 
    $bar->print_bar();
    $bar =  new HTML::Bar("Testing", "label", "","td");
    $bar->print_bar();  
   
      
    $bar =  new HTML::Bar("Center", "label", "","td");
    $bar->print_bar();
    $bar =  new HTML::Bar("Home Inst ", "label", "","td");
    $bar->print_bar();
    $bar =  new HTML::Bar("Service Type", "label", "","td");
     $bar->print_bar();
    $bar =  new HTML::Bar("Testing", "label", "","td");
    $bar->print_bar();
    $bar->end();
    build_lines($servicenames,$data,$rowlimit);
    print  "</tr></table>";

    HTML::Table::add_footer();
}
sub build_lines
{
    my ($servicenames, $data,$rowlimit) = @_;
    my ($count,$newcount,@longrows);
    $count =0;
    
    foreach my $n (sort keys  %$servicenames)
    { 
	last if ($count == $rowlimit);
	my $line =  build_tdtags($n, $servicenames,$data->{$n});
	my  $array = ["<tr>$line"];
	push @longrows,$array;
	delete $servicenames->{$n};
	$count++;
    }
    
    $newcount=0;
    $count = 0;
   
    foreach my $n (sort keys  %$servicenames)
    { 
	last if ($count == $rowlimit);
	my $line =  build_tdtags($n, $servicenames,$data->{$n});
	$longrows[$newcount]->[1]  = "$line</tr>";
	delete $servicenames->{$n};
	$count++;
	$newcount++;
    }
    
    foreach my $j (@longrows)
    {
	print "@$j";	
    }
}
sub build_tdtags
{

    my ($name,$servicenames,$data) = @_;
    
    #data[1] is the base url of the service     
    @$data[1] = '' if (! @$data[1]);
    my $vao_service  = @$data[2];
 
    my $color = 'blue';
    $color = 'green' if ($vao_service == '1');

    #@$data[0] is the homeinst
    @$data[0] = '' if (! @$data[0]);
    my $line = "<td class=servicename width =180><a href = @$data[1]><font style = 'color:$color'>$name</td>";
    $line   .= "<td class = servicename valign = top align = left>@$data[0]</td>";

    my $form = new BuildForm($name, $servicenames);
    $form = $form->getForm();
    $line  .=  "$form";
    return $line;
}
sub print_bar
{
    my ($title,$class,$col,$tag) = @_;
    if ($tag eq 'td')
    {
	print "<tr><td class = $class colspan = $col > $title</td></tr>\n";
    }
    elsif ($tag eq 'tr')
    {
	print "<tr class = $class><td colspan = $col>$title</td></tr>\n";
    }
}
   

