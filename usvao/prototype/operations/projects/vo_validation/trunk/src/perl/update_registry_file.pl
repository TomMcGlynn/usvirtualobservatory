#!/usr/bin/perl -w
#
#
#
#
{
    use strict;
    my $file = 'registry_res_for_validation'; 
    my $dump      =  do { local $/; local @ARGV = "./dump/$file"; <>};
    my $vgharvest =  do {local $/;local @ARGV = "./data/publishing_registries";<>};
    
    my @s = (split /\n/,$vgharvest);
    my %hash = map {s/\s+//g; $_=> '1'} @s;


    open (OUT,">./dump/$file.1");
    my @array = split ("\n",$dump);
    foreach my $j (@array)
    {
        my @a = (split /\|/,$j);
        if ((exists $hash{$a[1]}) and ($a[8] eq 'vg:Harvest'))
        {
	   print OUT "$j\n";
        }
	else
	{
           if ($a[8] eq 'vg:Harvest')
	   {
	      $a[8] = 'vg:Unknown';
	      $j = join ("|", @a);
           }
	   print OUT "$j\n";
	}
    } 
    close OUT;
    unlink ("./dump/$file");
    rename ("./dump/$file.1", "./dump/$file");
}
