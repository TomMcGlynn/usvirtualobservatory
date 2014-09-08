#!/usr1/local/bin/perl5 -wT
#
#
#
{    
    
    use strict;
    use lib "./";
    use lib "/www/server/vo/notification";
    use lib "/www/server/vo/vaomonitor";
    use CGI;
    use CGI::Carp 'fatalsToBrowser';
    use Table::VOheader;
    use Tools::Tools;
    use Tools::Form;  
    use Table::Layout;
    use CGI::Cookie;
    use data::startup_d;
    
    #print "Content-type: text/html\n\n";
    
    my $cgi = CGI->new();
    my $c = {};
    my $id;
    ($id, $c->{encoded}) =  get_data_from_cookie();

    #cookie exists
    if ($id && $id =~ m/(\d+\.\d+)/){
	$c->{'sessionid'} = $1;
	gen_encodedfile($c);
	gen_encryptedfile($c->{sessionid});
	decrypt_id($c->{sessionid});
        my $status = check_login_status($c->{sessionid});
        $c->{uid}  = get_user_id($c->{sessionid});
        cleanup($c->{sessionid},"1");
        if ($status eq 'in'){
            $c->{login}   = 1;
            generate_form($c);
            exit();
	}
        else {
             delete_cookie($cgi,$c->{sessionid},$::displaynote);
        }
    }
    #no cookie
    else{ 
	print "Location: $::displaynote\n\n";
	exit(1);
    }
    exit();
}
