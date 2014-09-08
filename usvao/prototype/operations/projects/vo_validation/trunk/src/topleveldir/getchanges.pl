#!/usr/bin/perl -wT
#
#
#
{
    
    use strict;
    use lib "./";
    use CGI;
    use DBI;
    use SQL::QueriesExtra;
    use data::startup;
    use Connect::MySQLValidationDB;
    use HTML::Layout;
    
    my $cgi  = CGI->new();
    print "Content-type: text/html\n\n";
    my $dbh = vodb_connect();
  
    my $res =  getCurrentStats($dbh);
    my $last = getLastSet($dbh);
    my ($current, $old) = hashify($res,$last);
    process($current,$old);
}
sub hashify
{
    my ($res,$last) = @_;
 
    my $current = {};
    my $old = {};
     
    
    foreach my $n (@$res)
    {
	my @array  = @$n;
	
	#print "@$n<br>";
	next	if (!$array[2]);
	
	$current->{$array[0]} = {
	                        status => $array[2],
				id     => $array[6], 
			    };	
    }

    foreach my $n (@$last)
    {
	my @array = @$n;

	next if (!$array[2]);
	$old->{$array[0]}  = $array[2];
    }
   
    return $current,$old;
}
sub process
{

    my ($current, $old) = @_;
    my $array = []; 
    foreach my $n (keys %$current)
    {
	my $currentstatus = $current->{$n}->{status};
	my $oldstatus = $old->{$n};
       
	if (($currentstatus) and ($oldstatus))
	{	    
	    
	    if (($currentstatus eq 'fail') and ($oldstatus eq 'pass'))
	    {
		my $a = ["$n","$current->{$n}->{id}", "$currentstatus"];
		push @$array,$a;
	    }
	    
	}
    }
    build_page($array);
}
sub build_page
{
    my ($array) = @_;
    my @linknames = ('Validation','Monitor','NVO Home', 'NVO Feedback');
    gen_header_layout("Changes", \@linknames);
    $array =  htmlify($array);
    display($array);
    gen_footer_bas();


}
sub display
{
    my ($array) = @_;
    
    my %hash;
    
    foreach my $n (@$array)
    {
	my $name = "@$n[1]";
	$hash{$name} = "@$n";
	
    }
   
    
    print "<table class = tac>";
    foreach my $n (sort keys(%hash))
    {
	print $hash{$n};
    }
    print "</table>";
}
sub htmlify
{
    my ($array) = @_;
    for (my $i = 0;$i < scalar(@$array); $i++)
    {
	my $line = $array->[$i];
	for (my $j =0; $j<scalar(@$line);$j++)
	{
	    my $v = $array->[$i][$j];
	    my $pars = 'class = greenlnleft';
	    if ($j == '0')
	    {
		$array->[$i][$j]  = "<tr><td $pars>$v</td>";
	    }
	    elsif ($j =='1')
	    {
		$array->[$i][$j] = "<td $pars>$v</td>";
	    }
	    else
	    {
		$array->[$i][$j] = "<td $pars>$v</td></tr>";
	    }
	}	
    }
    return $array;
}
