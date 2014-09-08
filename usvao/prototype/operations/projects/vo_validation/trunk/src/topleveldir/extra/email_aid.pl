#!/usr/bin/perl -wT
#
#
#
{
    use lib '/www/htdocs/vo/validation';
    use CGI;
    use data::startup;
    use DBI;
    use SQL::QueriesExtra;
    use HTML::Layout;
    use Connect::MySQLValidationDB;
    
    
    
    my $cgi = CGI->new();
    my $name = detaint("name",$cgi->param("name"));
    
    print "Content-type: text/html\n\n";
    gen_header_layout;
    my $q = $name;
   

    my $dbh = vodb_connect();
    
    my $array = getShortCurrentStats($dbh,$name);

    open (OUT, ">/www/htdocs/vo/validation/mail/$q") ||die "cannot open out";
    foreach my $n (@$array)
    {
	
	my @array  = @$n;
	for (my $i=0;$i<scalar(@array);$i++)
	{
	    if ($i == '3')
	    {
		if (!$array[$i])
		{
		    print   "BARnull"; 
		    printf OUT ("%-100s", "null"); 
		    next;
		}
	    }
	    elsif ($i == '0')
	    {
		print "$array[$i]";
		printf OUT ("%-30s",$array[$i]);
	    }
	    elsif ( $i== '2')
	    {
		print "$array[$i]";
		printf OUT ("%-60s",$array[$i]);
		
	    }
	    else
	    {
		print "$array[$i]";
		printf OUT ("%-10s",$array[$i]);
		
	    }
	}
	print OUT "\n";
	print "<br>";
	
    }
    close OUT;

}
sub detaint
{
    my ($name, $val) = @_;
    my $value;
   
    if ($val   =~ /^([A-Za-z0-9].*[^\<\>])/){ $value = $1;}
    return $value;
    

}
