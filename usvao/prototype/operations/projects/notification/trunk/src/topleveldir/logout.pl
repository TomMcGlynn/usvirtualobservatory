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
    use Table::Message;
    use Table::Layout;
    use Table::ErrorMessage;
    use CGI::Cookie;
    use data::startup_d;
   
    my $cgi = CGI->new();
   #print "Content-type: text/html\n\n";
    my $c = {'cgi' => $cgi};
 
    #get session id
    my $id;
    ($id,$c->{encoded}) =  get_data_from_cookie();
    if ($id and $id =~ m/(\d+\.\d+)/){
       $c->{sessionid} = $1; 
       gen_encodedfile($c);
       gen_encryptedfile($c->{sessionid});
       decrypt_id($c->{sessionid});
     }
    else {
        my $message = "You are not logged into our site! It's possible that your session has expired."
                    . "Please login again";
        my $error = new Table::ErrorMessage($message);
        $error->display();
        exit;
    }
    #try to logout
    eval{
       logout($c);
    };
    if ($@){
        my $error = new Table::ErrorMessage("Cannot log you out. It's possible that your session has expired. Please login again");
        $error->display();
        exit;
    }
}
