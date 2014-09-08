#!/usr1/local/bin/perl5 -w  
#
#
#
# Description: test for DataScope. This
#              is a special test meant to 
#              be used in the vaomonitor
#              framework.
#
#
#
{
    use strict;
    
    use LWP::UserAgent;
    use URI::URL;
    use LWP::ConnCache;
    use HTTP::Request::Common qw(GET);
    use CGI;
    my $cgi = CGI->new();
    my $url = $cgi->param("url");
    print "Content-type: text/html\n\n";

    if  ($url)
    {
       	#print "Foo";
	run_test($url);
    }
    else
    {
	print "here";
    }
    exit(0);

}
#################################################
# run test
#################################################
sub run_test
{
    my ($url) = @_;

    #get start time
 #  my $start_time        = time();    
    

    my $ua              = LWP::UserAgent->new;      
    my $page            = $ua->get($url);
    my $content         =  $page->content;
    print "$content";
  

}
