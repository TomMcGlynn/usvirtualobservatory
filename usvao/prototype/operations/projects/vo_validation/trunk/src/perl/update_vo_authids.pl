#!/usr1/local/bin/perl5 -w
#
#
#
{ 
    use strict;
    
    use lib "/heasarc/src/misc/perl/lib";
    use lib '/www/server/vo/validation';
    use lib './';
    
    use data::startup;
    use DBI;
    use Connect::MySQLVoOperationsDB;

    my $hash = {};
    add_ids("siap", $hash);
    add_ids("cone", $hash);
    add_ids("ssa",$hash);
    add_ids("tap",$hash);

     my $dbh = vao_operations_connect();
     my $sql  = qq(delete  from vo_authids);
     $dbh->do($sql);
     $sql = qq(alter table vo_authids auto_increment = 1); 
     $dbh->do($sql);
	#my $dbauthids = $sth->fetchall_hashref('authid');
      # foreach my $j(keys %$dbauthids)
       # {
             #print "PP: $j, $a->{$j}->{authid}<br>";	

       #}

    update_vo_authid_table($dbh, $hash);
}
sub update_vo_authid_table
{	


     my ($dbh, $hash) = @_;
    foreach my $n (keys %$hash)
     {
	my $a = $hash->{$n};
	my $size = scalar(@$a);
	print " CC:       $n ", scalar(@$a), "\n";
        	
           my  $sql = qq(insert into vo_authids values ('','$n', '$size'));	
           $dbh->do($sql);

     }

}
sub add_ids
{
    my ($type,$hash) = @_;		
    open (File, "./dump/${type}_res_for_validation");
    while (<File>)
    {

	my $line = $_;
	chomp $line;
	my @array = (split /\|/, $line);
	my @authid   = (split /\b\//, $array[1]);
	#print "$authid[0]\n";
	if ($hash->{$authid[0]})
        {
	      my $a  = $hash->{$authid[0]};
	      push @$a, $array[1];
	}
        else
	{
	   	 $hash->{$authid[0]}  = [ $array[1] ] ;

	}		
    }
    close File;
    #my $dbh = vao_operations_connect();

    #my $sth = $dbh->prepare("


}
