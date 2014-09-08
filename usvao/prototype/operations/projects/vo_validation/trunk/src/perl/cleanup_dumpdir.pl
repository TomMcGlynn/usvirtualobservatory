#!/usr/bin/perl -w
#
#
#
{
    use lib './';
    use File::Basename qw (dirname);
    my $homedir = dirname($0);
    
    
    opendir (DIR,"$homedir/dump/");
    while (defined ($file = readdir(DIR)))
    {
	if (($file =~ m/\_res$/) || ($file =~ m/\_for_validation$/))
	{
	    unlink "$homedir/dump/$file";   
	}	
    }
    closedir DIR;
    exit();
}
