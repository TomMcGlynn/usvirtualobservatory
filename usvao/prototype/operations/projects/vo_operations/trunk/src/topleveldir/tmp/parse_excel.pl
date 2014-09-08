#!/usr/contrib/linux/bin/perl -w   
#
#
# author: michael preciado
# 
# name: parsens_excel.pl
# 
# description: parse excel sheet containing
#              services to be deprecated.
#
# 
#
#
{
    use strict;
    
   
    
    use Util::LoadTypes;
    
    use Spreadsheet::ParseExcel;
    use File::Basename qw(dirname);
    use vars qw(%param);
    use Getopt::Long;
    my $dir;
    BEGIN 
    {
        $dir = dirname($0);
	
    }
    
    parse_opts();
    #connect to db
    print "you need a file\n" if (! $param{file});

    my $excelparser = new Spreadsheet::ParseExcel;
    
    my $page = $excelparser->Parse($param{file});
    my($iR, $iC, $oWkS, $oWkC);
    print "FILE  :", $page->{File} , "\n";
    print "COUNT :", $page->{SheetCount} , "\n";
    print "AUTHOR:", $page->{Author} , "\n";
    
    foreach my $oWkS (@{$page->{Worksheet}}) 
    {
        print "--------- SHEET:", $oWkS->{Name}, "\n";
        for(my $iR = $oWkS->{MinRow} ;  defined $oWkS->{MaxRow} && $iR <= $oWkS->{MaxRow} ; $iR++) 
	{
            for(my $iC = $oWkS->{MinCol} ; defined $oWkS->{MaxCol} && $iC <= $oWkS->{MaxCol} ; $iC++) 
	    {
                $oWkC = $oWkS->{Cells}[$iR][$iC];
		if ($oWkC)
		{
		    #print "( $iR , $iC ) =>", $oWkC->Value, "\n";
		    if (($iC) and ($iC == '5') and  ($oWkC->Value eq 'yes'))
		    {
			my $urlentry= $oWkS->{Cells}[$iR][1];
			my $identry = $oWkS->{Cells}[$iR][0];
			my $url = $urlentry->Value;
			my $id  = $identry->Value;
			print "$id   |  $url\n";
			
		    }
		}
	    }
	    #print "\n";
        }
    }

    
    

    
}
################################################
# subroutine for parsing command line options
################################################
sub parse_opts
{ 
    %param = (
              verbose => 0
              );
    
    GetOptions (\%param, 
                "version",
                "file=s",
                "all",
                "count=s",
                "debug",
                "help") or exit(1);
    
    return if $param{help} or $param{version};
    
    my $err =0;
    while (my ($par,$val) = each (%param))
    {
        next if defined $val;
	warn ("parameter `$par` not set\n");
        $err++;
    }
    exit(1) if $err;
}





