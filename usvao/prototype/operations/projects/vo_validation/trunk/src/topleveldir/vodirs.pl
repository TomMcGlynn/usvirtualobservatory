#!/usr/bin/perl -wT
{
    use strict;
    use URI::URL;
    use LWP::UserAgent;
    use CGI;
    use lib "/www/htdocs/vo/validation";
    use data::startup;
    use Table::Table;
    use HTML::DisplayTable;
    use DBI;
    use HTML::Layout;
    use HTML::StatsTable;
    use SQL::Queries;
    use HTML::ErrorMessage;
    use Switch;
    use HTML::Page;
    use HTML::Title;
    use Tie::IxHash;
    use Connect::MySQLValidationDB;
   

    use Table::VOTable;
    #$ENV{MYSQL_UNIX_PORT} = "$::mysqlunixport";
   
    my $cgi      = CGI->new();
    
    print "Content-type: text/html\n\n";  
    my $data = get_data();
    
    
   
    #connect;
    #my $dbh = vodb_connect;
       
    #define container 
    
    my @linknames = ('Validation','Monitor','NVO Home', 'NVO Feedback');
    gen_header_layout("Validation Results", \@linknames);
    build_table($data);
        
    
    gen_footer_bas();
}
sub build_table
{
    my ($data) = @_;
    
    print "<table class = 'tac' align = center border = 1>";
    print "<tr><td class = tblue>Service</td>";
   
    print "<td class  = greenln>Proposed Location</td>\n"; 
    print "<td class  = greenln>Current Location</td></tr>\n"; 
    
    foreach my $n (keys %$data)
       
    {
	print "<tr><td class = greenlnleft>$n</td>";
	print "<td class = greenlnleft>$data->{$n}->{new}</td>";
	print "<td class = greenlnleft>$data->{$n}->{old}</td></tr>";
    }
    print "</table>"; 
    print "<br>";

}
sub get_data
{
    
    open (OUT, "./vodirlayout") ||  die "cannot open file";
    my $hash  = {};
    while (<OUT>)
    {
	my $line = $_;
	chomp $line;
	my @array = (split /\;/, $line);
	#print "$array[0], $array[1], $array[2]<br>";
	$hash->{$array[0]}->{new} = $array[1];
	$hash->{$array[0]}->{old} = $array[2];
	
	
    }
	
    close OUT;
    return $hash;


}






