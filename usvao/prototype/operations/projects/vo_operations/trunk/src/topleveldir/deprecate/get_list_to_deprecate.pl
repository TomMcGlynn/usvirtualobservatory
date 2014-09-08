#!/usr/contrib/linux/bin/perl -w   
#
#
# author: michael preciado
# 
# name: get_list.pl
# 
# description: get_list of VO services to demote
# usage: ./get_list.pl 
#
# outputs: 2 files, "file.out" and "file.xls"
#          file.xls is an excel file.
# notes: CDS, heasarc and ledas services have been
#        removed from the results.
#
{
    use strict;
    use DBI;
    use Connect::MySQLValidationDB;
    use SQL::QueriesOperations;
    use Util::LoadTypes;
    use Spreadsheet::WriteExcel;
    use File::Basename qw(dirname);
    my $dir;
    BEGIN 
    {
        $dir = dirname($0);
        print "$dir\n";
    }
    
   
    #connect to db
    my $dbh  = vodb_connect();

    #get services that failed in the last 30 days.
    my $res   = getStatsFailingInInterval($dbh, "30");
    
    my $hash = {};
    foreach my $n (@$res)
    {
	my $serviceId = shift @$n;
 	
	if (exists ($hash->{$serviceId}))
	{
	    if (exists ($hash->{$serviceId}->{$$n[4]}))
	    {
		$hash->{$serviceId}->{$$n[4]}++;
	    }
	    else
	    {
		$hash->{$serviceId}->{$$n[4]}  = 1;
	    }
	}
	else
	{
	    $hash->{$serviceId}->{$$n[4]}  = 1;
	    $hash->{$serviceId}->{id}      = $$n[1];
            $hash->{$serviceId}->{url}     = $$n[2];
	    $hash->{$serviceId}->{xsitype} = $$n[3];
            $hash->{$serviceId}->{runid}   = $$n[0];
	}
    }
    my $s = keys %$hash;
    #print "SS:$s\n";
    foreach my $n (sort keys %$hash)
    {
	#print "GG: $n\n";
	my $h = $hash->{$n};
	#if ($h->{'abort'})
	#{
	    #if ($h->{'fail'})
	    #{
		#print "TOOO: $n: $h->{id}\n";
		#print "GOOOOOOTCH\n";
	    #}
	#}
	#print "$h->{'fail'}\n";
	if (exists ($hash->{$n}->{pass}))
	{
	    delete $hash->{$n}; 
	    next;
	}
	elsif (exists ($hash->{$n}->{skip}))
	{	    
	    delete $hash->{$n};  
	    next;
	}
     	
    }
    my $ss = keys %$hash;
    #print "JJ: $ss\n";
    #create spreadsheet.
    my $sheet = create_outfile($hash,$dir);
    create_spreadsheet($dir);
    
    exit();    
}
sub create_spreadsheet
{
    my ($dir) = @_;
    
    my $t  = Spreadsheet::WriteExcel->new("$dir/to_deprecate/file.xls");
    my $sheet = $t->add_worksheet();
    $sheet->set_margin_left(.5);
    $sheet->set_margin_right(.5); 
    $sheet->center_horizontally();
    $sheet->set_column(0,0,100);
    $sheet->set_column(0,1,300);
    $sheet->set_column(0,2,10);
    $sheet->set_column(0,3,10);
    $sheet->activate();
    $sheet->write(0,0,"IVOID");
    $sheet->write(0,1,"URL");
    $sheet->write(0,2,"TYPE");
    $sheet->write(0,3,"STATUS");
    
    open (INPUT, "$dir/to_deprecate/file.out") 
	|| die "cannot open $dir/file.out";
    
    my $row =1;
    while(<INPUT>)
    {
	my $line = $_;
	my @array = (split /\|/,$line);
	my $col =0; 
	foreach my $n (@array)
	{	    
	    $sheet->write($row,$col,$n);
	    $col++;
	}
	$row++;
    }
    close INPUT 
	||die "cannnot close $dir/to_deprecate/file.out";
}
sub create_outfile_old
{
    my ($res,$dir) = @_;
    my $types = load_types($dir);
    open (FILE, ">$dir/to_deprecate/file.out") 
	|| die "cannot open $dir/to_deprecate/file.out";
    foreach my $n (@$res)
    {	
	
	my ($id, $url, $status);
	
	$status  = $$n[2];
	$url     = $$n[4];
	my $type = $types->{$$n[5]}->{'type'};
	$id     = $$n[6]; 
	
	print FILE "$id|$url|$type|$status\n";
    }
    close FILE 
	|| die "cannot close $dir/file.out";
}
sub create_outfile
{
    my ($hash,$dir) = @_;
    my $types = load_types($dir);
    open (FILE, ">$dir/to_deprecate/file.out") 
	|| die "cannot open $dir/to_deprecate/file.out";
    foreach my $n (keys %$hash)
    {	
	#print "$n\n";
	my $j = $hash->{$n};
	
	
	
	my $t = $j->{xsitype};
	
	my $type = $types->{$t}->{'type'};
        $type = 'null' if (! $type); 
	next if ($j->{id} =~ /CDS.VizieR/);
	#next if ($j->{id} =~ /ledas/);
	next if ($j->{id} =~ /heasarc/);
        my $runid = $j->{runid};
	#print "TT; $j->{id}\n";
	print FILE "$j->{id}|$j->{url}|$type|fail|$runid\n";
    }
    close FILE 
	|| die "cannot close $dir/to_deprecate/file.out";

}




