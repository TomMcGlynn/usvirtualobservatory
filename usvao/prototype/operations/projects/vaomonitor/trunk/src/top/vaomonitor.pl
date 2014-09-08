#!/usr/bin/perl -w
#
#
#
{
    
    use strict;
    #use lib '/www/htdocs/vo/monitor';
    #use lib '/www/htdocs/vo/notification';
    use lib '/www/htdocs/cgi-bin/W3Browse/lib';
    use lib "/www/server/vo/vaomonitor";
    use DBI;  
    use CGI;
    use Connect::MySQLVaomonitorDB;
    use HTML::VOheader;
    use SQLVao::Queries;
    use HTML::BuildForm;
    use HTML::Banner;
    use HTML::Table;
    use data::startup;
    print "Content-type: text/html\n\n";
    
    
    my $dbh = vaomonitor_connect();
    

    my @linknames   = ('VO Service Notices','VAO Monitor Interactive Testing','VAO Monitor Help','VAO Home', 'VAO Feedback');
    my $voheader        = new HTML::VOheader("VAO Monitor",\@linknames);
    $voheader->printheader();
    $voheader->print_banner(); 

    

    my $servicenames;
    $servicenames = getServicenamesTestid($dbh,"Portal Science Services");
    my $data         = getData($dbh);

   
    my $container = {};
    


    print "<table class = 'tac'   align = center  width=930 cellspacing='0'>\n";
    print "<tr><td  valign = top><table class = 'tac' width=460 cellspacing='0'>\n";
    
    #Top left:  bar
    print_bar("VAO Services","label","4","td");
    print_bar("Science Services", "titleblue","4","tr");

    dump_lines($servicenames,$data);

    #bottom left: Tools
    
    print_bar("Support Services","titleblue","4","tr");
    $servicenames = getServicenamesTestid($dbh,"Portal Support Services");
    dump_lines($servicenames,$data);
    

    print_bar("Testing","titleblue","4","tr");  
    $servicenames = getServicenamesTestid($dbh,"Testing");
    dump_lines($servicenames,$data);

    
    print "</table></td><td valign = top>";
    print "<table class= 'tac' width=460 cellspacing='0'>";
  
    print_bar("Services","label","4","td");
    print_bar("System","titleblue", "4", "tr");
    $servicenames = getServicenamesTestid($dbh,"System");
    dump_lines($servicenames, $data);
    

   

    print_bar("Legacy Services","titleblue","4","tr");
    $servicenames = getServicenamesTestid($dbh,"Legacy Support Services");
    dump_lines($servicenames,$data);
    
  
    $servicenames = getServicenamesTestid($dbh,"Legacy Science Services");
    dump_lines($servicenames,$data);



    print "</table></td></tr></table>";


    

    HTML::Table::add_footer();
}
sub dump_lines
{
    my ($servicenames, $data) = @_;
    print "<tbody colspan=4 valign='top' align=left>";
    foreach my $n (keys  %$servicenames)
    {		 
	write_line($n, $servicenames,$data->{$n});
    }
    print "</tbody>";
}
sub write_line
{

    my ($name,$servicenames,$data) = @_;
   print "<tr><td class=servicename>";
    print "<a href = @$data[1]>";
    print "$name</td><td class = servicename>@$data[0]</td>";

    my $form = new BuildForm($name, $servicenames);
    $form->display();
    print "</tr>";
    
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
   

