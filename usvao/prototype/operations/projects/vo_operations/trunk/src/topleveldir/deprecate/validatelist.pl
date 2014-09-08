#!/usr/contrib/linux/bin/perl -w 
#
#
#  Description: a small tool to validate a bunch of services
#               in a file.
{

    use strict;
    use DBI;
    use File::Basename;
    use vars qw (%param);
    use Connect::MySQLValidationDB;
    use Getopt::Long;
    parse_opts();
    my $homedir= '/software/jira/software2/projects/vo_operations/trunk/src/topleveldir/deprecate';

    $ENV{'CLASSPATH'} =  "/usr1/local/javasdk/bin/javac:/usr1/local/javasdk/bin/java:/www/htdocs/vo/validation/java:"
                          . "/software/jira/software/class:/www/server/vo/mysql-connector-java-5.1.7/mysql-connector-java-5.1.7-bin.jar:"
                          . "/www/htdocs/vo/validation/:/software/jira/software/ljar/fits.jar:"
                          . "/software/jira/software/fjar/bzip2.jar:/www/htdocs/vo/validation/javalib/stilts.jar";


    #get list
    open FILE,"$homedir/$param{list}" || die "cannot open file\n";
    my @array = <FILE>;
    close FILE;
   
    #chdir and compile
    chdir "/www/htdocs/vo/validation/java";
    `/usr1/local/bin/javac /www/htdocs/vo/validation/java/*java`;
    
    #loop over and test 
    foreach my $n (@array)
    {
	chomp $n;
	my ($id, $url,$type,$status) = (split /\|/, $n);
	print "$id\n";
	print "$id  >> ids_tested\n";
        `/usr1/local/bin/java -Xms32m -Xmx180m -Ddatabase=validation RunValidation   $id>> /www/htdocs/vo/validation/log`;
    }

}
sub parse_opts
{

    %param = ('verbose' =>0,
	      );
    GetOptions(\%param,
	       "list=s",
	       ) or exit(1);
    

}
