#!/usr1/local/bin/perl5  -w
#
# Description: program sees if either the Image or Table validator is down.
# If either is down, the java validation  of VO services does not run. 
{
    use strict;
    use warnings;
    use lib '/www/htdocs/cgi-bin/lib/nagios/lib/perl';
    use lib '/www/htdocs/cgi-bin/W3Browse/lib';   
    use File::Basename qw (dirname);
    use XML::SimpleObject::LibXML;
    use URI::URL;
    use LWP::UserAgent;
    my $homedir;
    
    BEGIN
    {
	$ENV{'CLASSPATH'} = "/usr1/local/javasdk/bin/javac:/usr1/local/java/bin/java:/www/htdocs/vo/validation/java:"
                          . "/software/jira/software/class:/www/server/vo/mysql-connector-java-5.1.7/mysql-connector-java-5.1.7-bin.jar:"
                          . "/www/htdocs/vo/validation/:/software/jira/software/ljar/fits.jar:"
                          . "/software/jira/software/fjar/bzip2.jar:/www/htdocs/vo/validation/javalib/stilts.jar";
	$homedir = dirname($0);
    }
    
    my $url    = "http://heasarc.gsfc.nasa.gov/vo/vaomonitor/vaodb.pl?format=xml";
    my $ua     = LWP::UserAgent->new();   
    my $res    = $ua->get($url);
    my $xml    = $res->content;    
    my $xmlobj = new XML::SimpleObject::LibXML(XML => $xml);
    my $data   = $xmlobj->child("TABLE");
    my @tests  = $data->children("Service");

    my $fail;
    foreach my $t (@tests)
    {	
	my $name = $t->child("name")->value;
        if (($name eq "Image Validator")|| ($name eq "Table Search Validator"))
	{
	   my $status = $t->child("status")->value;
           if ($status eq 'Fail')
           {
	    $fail=1;
	    last;
           }	    
	}     
    }
    if (! $fail)
    {        
	#get date
	my ($mday,$mon,$year) = (localtime(time))[3,4,5];
	$year+= 1900;
	$mon+=1;
        $mon  = sprintf('%02d',$mon) if ($mon =~ /^\d$/);
	$mday = sprintf('%02d',$mday) if ($mday =~ /^\d$/);
	my $date = "$year.$mon.$mday";

	my $dbname;
        open (File, "/www/server/vo/validation/data/startup.pm");
        while (<File>)
	{
	  my $line = $_;
	  chomp $line;
          if ($line =~ /\$\:\:dbname(.*?)=(.*)/)
	  {
	    my @array = (split /\=/,$line);
            $dbname = $array[1];
          }
	}	
	$dbname =~ s/\s+//g;
	$dbname =~ s/\;//g;
	print "Proceed with validation";
	`/usr1/local/javasdk/bin/javac /www/htdocs/vo/validation/java/*java`; 	
        chdir "/www/htdocs/vo/validation/java";
   	system("/usr1/local/java/bin/java  -Ddebug=true -Ddatabase=$dbname -Xms32m -Xmx180m RunValidation  >> /www/server/vo/validation/logs/$date.log");	
    }      
    exit(0);
}
