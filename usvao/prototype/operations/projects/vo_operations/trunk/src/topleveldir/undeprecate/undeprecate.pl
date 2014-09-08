#!/usr1/local/bin/perl5 -w
#
#
# author: michael preciado
#
# name:   undeprecate.pl
# 
# usage:  ./undeprecate.pl 
# 
# description: will query the db for services
#              that have been validated and passed
#              validation. Any services that have passed
#              in the 3 hours prior to running the
#              script will be output to the output file:
#              list_to_undeprecate.{year}{mon}{day}
#
# notes:  to be used as part of the VAO undeprecation
#         procedure
#
#
#

{ 

    
    use lib '/software/jira/software2/projects/vo_operations/trunk/src/topleveldir/deprecate';
    use lib './';  
    use strict;  
    use Connect::MySQLValidationDBII;
    use vars qw(%param);
    use Getopt::Long;
    use SQL::Queries;
    parse_opts();


    my $dbh = vodb_connect();
    my $con = { 
	         'dbh' => $dbh,
	      };

    my $list =  get_list($con);
    writeout($list);
 
    
}
sub writeout
{
    my ($list) = @_;
    my ($mday, $mon, $year) = (localtime(time))[3,4,5];
    $year += 1900;
    $mon += 1;
    $mon = sprintf ("%02d",$mon);
    $mday = sprintf("%02d",$mday);

    unlink "./to_undeprecate/list_to_undeprecate.$year$mon$mday" if (-e "./to_undeprecate/list_to_undeprecate.$year$mon$mday");
    open (OUT, ">>./to_undeprecate/list_to_undeprecate.$year$mon$mday") 
	|| die "cannot open file\n";
    foreach my $n (keys %$list)
    {
	print OUT "$n|$list->{$n}\n";
    } 
    close OUT 
	|| die "cannot close file\n";
    
}

sub get_list
{
    my ($con) = @_;
    my $data = getTodaysList($con);
    my $hash = {}; 
    foreach my $n (@$data)
    {
	my @a = @$n;
	if ($a[2] eq 'pass')
	{
	    $hash->{$a[6]} = $a[4];
	}
    }
    return $hash;
}
sub parse_opts
{
    %param = ('verbose'=>0);

    
    GetOptions(\%param,
	       'list=s',
	       );
}

    
