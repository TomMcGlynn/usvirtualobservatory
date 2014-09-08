#!/usr1/local/bin/perl5 -wT
#
#
# 
# Title: deletenote.pl  
# Author: Michael Preciado
# 
# Description:  Program deletes notices from the db. 
#               
#
{
    use strict;
    use CGI;
    use CGI::Carp 'fatalsToBrowser';
    use lib '/www/server/vo/vaomonitor/';     
    use lib './';
    use data::startup_d;
    use Switch;
    use Connect::MySQLVaoOperationsDB;
    use Table::Message;
    use Table::ErrorMessage;
    use Table::NoteDumper;
    use SQL::Queries;
    use Tools::Tools; 
  
    my $cgi = CGI->new();      
    my $c = {'cgi'=> $cgi}; 
    #print "Content-type: text/html\n\n";

    #db connection
    my $dbh        = vao_operations_connect();
    $c->{dbh}      =  $dbh;
    
    #check if cookie exists
    my $id;
    ($id,$c->{encoded})  =  get_data_from_cookie();
   
    if ($id and $id =~ m/(\d+\.\d+)/){
	$c->{sessionid} = $1; 
        gen_encodedfile($c);
        gen_encryptedfile($c->{sessionid});
        decrypt_id($c->{sessionid});    
        $c->{identity}  = get_user_id($c->{sessionid});
	#check status
	my $status = check_login_status($c->{sessionid});
        cleanup($c->{sessionid},"1");
	if ($status ne  'in'){
	    print "Content-type: text/html\n\n";
	    my $error = new Table::ErrorMessage("You cannot delete this note. You have to login before deleting notes");
	    $error->display();
	    exit();
        }
    } 
    else{
	my $error = new Table::ErrorMessage("You are not logged in yet. Please login to delete messages");
	$error->display();
	exit();   
    }

    #detaint pars
    my @z = $cgi->param;
    foreach my $n (@z){
	my $par =  detaint($n,$cgi->param($n)) if ($cgi->param($n) ne '');
        $c->{$n} = $par;
    }  
    #process
    if (($c->{delete}) && ($c->{id}))
    {   
	my $arrayref = get_hostname_for_note($c->{dbh},$c->{id});	
        my $dbidentity   = $$arrayref[0][0];
        #delete entry
        if ($dbidentity  eq $c->{identity}){   
	    update_note_status($c,"by user");
        }
    } 
    print "Location: $::displaynote\n\n";
    exit(1);
}
sub detaint
{
    my ($parname, $value) = @_;
    my $status;
    switch($parname)
    {
	case  "id"           { if ($value =~ m/^(\d+)$/) {$value = $1} else { $status =1;}}
   	case "delete"        { if ($value =~ m/^(yes)$/) {$value =$1;} else { $status = 1;}}         
    }
    if ($status){	
	my $error = new Table::ErrorMessage("The parameter or value entered is not recognized");
        $error->display();
        exit();
    }
    return $value;
}
