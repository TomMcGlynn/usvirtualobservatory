#!/usr/contrib/linux/bin/perl -w 
#
#
#   This script uses the validation system java code
#   to test services so it assumes that you have installed
#   the VO validation system
#
{
    use lib '/software/jira/software2/projects/vo_operations/trunk/src/topleveldir/deprecate';
    use strict;
     

    use DBI;
    use vars qw (%param);
    
    use Getopt::Long;
    
    parse_opts();

    #currently set up for test area
    #change this to point to the location of your validation system java code
    `/usr1/local/bin/javac /www/htdocs/vovaltest/java/*java`;
    
    $ENV{'CLASSPATH'} = "/usr1/local/bin/java:/www/htdocs/vovaltest/java:/software/jira/software/class:/www/server/vo/mysql-connector-java-5.1.7/mysql-connector-java-5.1.7-bin.jar:/www/htdocs/vovaltest/:/software/jira/software/ljar/fits.jar:/software/jira/software/fjar/bzip2.jar";


    open (FILE,"$param{list}");
    while(<FILE>)
    {
	my $line = $_;
	chomp $line;
	my ($id, $url) = (split /\|/, $line);
	print "TT: $id\n";
	
 
        `/usr1/local/bin/java -Xms32m -Xmx180m RunValidation  $id>> /www/htdocs/vo/validation/log`;
	

    }
    close FILE;

}
sub parse_opts
{

    %param = ('verbose' =>0,
	      );
    GetOptions(\%param,
	       "list=s"	       ) or exit(1);
    

}
