#!/usr1/local/bin/perl5 -wT
#
#
#
{    
    
    use strict;
    use lib "./";
    use lib "/www/server/vo/notification";
    use lib "/tmp/vo/notification/vaologin";
    use lib "/www/server/vo/vaomonitor";
    use CGI;
    use CGI::Carp 'fatalsToBrowser';
    use Table::VOheader;
    use Tools::Tools;
    use Table::Layout;
    use CGI::Cookie;
    use data::startup_d;
    my $cgi = CGI->new();
# print "Content-type: text/html\n\n";
    
    my $pid = $$;
    #get session id
    my ($id,$encoded) =  get_data_from_cookie();
    my $c = {};
    $c->{cgi} = $cgi;
    $c->{'encoded'} = $encoded;
    my $sub;
    if ($id && $id =~ m/(\d+\.\d+)/){
	$c->{sessionid} = $1; 
        $c->{status} = check_login_status($c->{sessionid});
        if ($c->{status} eq 'in'){
            print "Location: $::displaynote" . "\n\n"; 
	    exit();   
        }
        $sub = "Tools::Tools::login_status_" . $c->{status};
    }
    else
    {
	#generate an id,user has not been here before
	$c->{sessionid}  = (rand()) * $pid;
        $sub = "Tools::Tools::login_status_out";
    }
    #check status
    my $subref = \&$sub;
    &$subref($c);     
}
