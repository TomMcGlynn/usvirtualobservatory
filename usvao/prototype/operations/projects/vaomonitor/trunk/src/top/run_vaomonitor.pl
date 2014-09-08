#!/usr/bin/perl -w


{
    use strict; 
    use lib './java_automonitor';
    use File::Basename qw(dirname);
    my $log = './log';
    
    my $homedir;
    BEGIN
    {
        $ENV{'CLASSPATH'} = "/usr1/local/javasdk/bin/java"
	                 .  ":/www/htdocs/vo/vaomonitor/java_automonitor/"
	                 .  ":/software/jira/software/class"
	                 .  ":/www/server/vo/mysql-connector-java-5.1.7/mysql-connector-java-5.1.7-bin.jar"
	                 .  ":/www/htdocs/vo/vaomonitor/";
        $homedir = dirname($0);
               
    }

    
    #open (Log, "$log") || die "cannot open log file";
    `/usr1/local/bin/javac /www/htdocs/vo/vaomonitor/java_automonitor/*java`; 
    `date >> /www/htdocs/vo/vaomonitor/log`;
    `/usr1/local/bin/java  RunMonitor  >> /www/htdocs/vo/vaomonitor/log`;     
    sleep(100);
    `/www/htdocs/vo/vaomonitor/rerun_tests.pl  >> /www/htdocs/vo/vaomonitor/log`;
    `/www/htdocs/vo/vaomonitor/run_crashmail.pl  >> /www/htdocs/vo/vaomonitor/log`; 
    `wget -O /www.prod/server/vo/vaomonitor/usvaohome/status.xml http://heasarc.gsfc.nasa.gov/vo/vaomonitor/vaodb.pl?format=xml`;
    exit();
}
