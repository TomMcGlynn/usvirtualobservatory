pwd
#!/usr/contrib/linux/bin/perl -w 
#
#
#
#
{

    use strict;
     

    use DBI;
    use vars qw (%param);
    use Connect::MySQLValidationDB;
    use Getopt::Long;
    my $dbh = vodb_connect();
    parse_opts();
    `/usr1/local/bin/javac /www/htdocs/vo/validation/java/*java`;
    
    $ENV{'CLASSPATH'} = "/usr1/local/bin/java:/www/htdocs/vo/validation/java:/software/jira/software/class:/www/server/vo/mysql-connector-java-5.1.7/mysql-connector-java-5.1.7-bin.jar:/www/htdocs/vo/validation/:/software/jira/software/ljar/fits.jar:/software/jira/software/fjar/bzip2.jar";


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
	       "list=s",
	       ) or exit(1);
    

}
