#!/usr1/local/bin/perl5 -wT
#
#
# 
# Title: displaynote.pl  
# Author: Michael Preciado
# 
# Description:  Program displays notices posted by users
#               
#
#
{
    use strict;
    use CGI;
    use lib '/www/server/vo/vaomonitor/';     
    use lib '/www/htdocs/vo/vaomonitor/';
    use lib './';
    use data::startup_d;
    use HTML::VOnote;
    use Tools::Tools;
    use XML::SimpleObject::LibXML;
    use LWP::UserAgent;
    use Table::Layout;
    use Switch;
    use Connect::MySQLVaoOperationsDB;
    use Table::Message;
    use Table::ErrorMessage;
    use Table::VOheader;
    use Table::NoteDumper;
    use Socket;
    use SQL::Queries;
    use Tie::IxHash;
    use Tools::TimeZone;
    
    my $cgi  = CGI->new();  
    my $c    = {'cgi' => $cgi,};
    my @z    = $cgi->param;
    
    #detaint all params  
    foreach my $n (@z) {
	my $val  =  detaint($n,$cgi->param($n)) if ($cgi->param($n) ne '');
        $c->{$n} = $val;
    }    
    #db connection
    $c->{dbh}  = vao_operations_connect();
  
    #handle login error 
    reset_cookie($cgi);
  
    #get login status
    my $id;
    ($id, $c->{encoded}) =  get_data_from_cookie();
    if ($id && $id =~ m/(\d+\.\d+)/){
	$c->{sessionid} = $1;
	gen_encodedfile($c);
        gen_encryptedfile($c->{sessionid});
	decrypt_id($c->{sessionid});	
	my $status  = check_login_status($c->{sessionid});
        if ($status eq 'in'){ 
	    $c->{login} = 1;
	    $c->{uid}   = get_user_id($c->{sessionid});
            cleanup($c->{sessionid},"1");
	}
	elsif ($status eq 'incomplete'){
	    branch_janrain_nonce_incomplete($c);
	}
    }
    
    #more pars
    my $notes_array = get_notes_array($c);
    $c->{current_notes}  = load_notes_db($notes_array);  
    $c->{urlcurrentnotes} = "$::displaynote?format=rawcurrent";
    
    
    if ($c->{format}) {
	branch_format($c);
    }  
    elsif ($c->{janrain_nonce}){
        #user logged in successfully
        $c->{login} = 1;
        $c->{identity}   = get_user_id($c->{sessionid});
	cleanup($c->{sessionid},"1");
    }
    else {	 	
	$c->{title}              = "Deleted Notes" if ($c->{showdeletes});
	$c->{urlcurrentnotes} = "$::displaynote?format=rawdeleted" if ($c->{showdeletes});
    }
    my $xml = get_xml($c);
    my ($notes,$outside_notes) = parse_xml($xml,$c,$cgi);
    build_page($notes,$outside_notes,$c);
    exit(1);
}
sub reset_cookie
{
    my ($cgi) = @_;
    if ($cgi->param("error"))
    {
        my $c = $cgi->cookie(-name=>'username',
                             -value=>'',
                             -path => '/vo/notification',
			     -expires=> '-1d'
			     );  
    } 
}
sub branch_janrain_nonce_incomplete
{
    my $c  = shift;   
    my $string;
    my $j = $ENV{QUERY_STRING};
    if ($j =~ m/(.*)/){
	$string = $1;
    }
    $c->{querystring} = $string;
    if (! $string) { 
	print "Location: $::displaynote" . "?error=true\n\n"; 
	exit();
    }
    my $url = "$::displaynote" . "?" . "$string";
    login_status_incomplete($c,$url);
    exit();
}
sub branch_format
{
    my ($c) = @_;
    print "Content-type: text/xml\n\n";  
    if(($c->{format} eq "rawdeleted") or ($c->{format} eq "rawcurrent")){      
	my $sorted_notes = sort_notes($c->{current_notes});
	output_xml($sorted_notes);
	exit(1);
    }
}
sub get_login_status
{
    my ($id)  = @_;
    return if (! $id);
    if ($id && $id =~ m/(\d+\.\d+)/){
        $sessionid = $1; 
	my $status = check_login_status($sessionid);
	return $status; 
    }
}
sub get_notes_array
{
    my $c  = shift;
    my $notes_array = load_notices($c->{dbh});
    #get deleted notes if needed
    if (($c->{showdeletes}) or (($c->{format}) and ($c->{format}  eq "rawdeleted"))) { 	  
	$notes_array  = load_notices($c->{dbh},"deleted");    
    }
    return $notes_array;
}
sub detaint
{
    my ($parname, $value) = @_;
    my $status;
    switch($parname)
    {
	case  "id"           { if ($value =~ m/^(\d+)$/) {$value = $1} else { $status =1;}}
	case "delete"        { if ($value =~ m/^(yes)$/) {$value =$1;} else { $status = 1;}}       
	case "showdeletes"   { if ($value =~ m/^(yes)$/) {$value =$1;}  else {$status =1;}}
	case "format"        { if ($value =~ m/^(rawdeleted|rawcurrent)$/) {$value =$1;} else { $status =1;}}
        case "janrain_nonce"  { if ($value =~ m/(.*)/)  {$value = $1;} else {$status = 1;}}
        case "error" { if ($value =~ m/(.*)/){$value = $1;} else {$status =1;}}
    }
    if ($status)
    {	
	my $error = new Table::ErrorMessage("The parameter or value entered is not recognized");
        $error->display();
        exit();
    }
    return $value;
}
sub get_xml
{
    my ($c) = @_;   
    my $ua  = LWP::UserAgent->new();
    my $res = $ua->get($c->{urlcurrentnotes});
    my $xml = $res->content; 
    return $xml;
}
sub load_notes_db 
{
    my ($array) = @_;
    my $hash = {};
    foreach my $n (@$array) {  
	#print "JOn: @$n<br>";
	$n->[4] = "$n->[4]:";
	my $row;
	for (my $i = 0;$i<scalar(@$n); $i++) {	
	    if (! $n->[$i]){
		$n->[$i]= " ";
	    }    	  
	    if (($i <11) and ($i != '4')) {
		$row .= "$n->[$i]|";
	    }
	    else{
		$row  .= "$n->[$i]";
	    }
	}
	$hash->{$row} =1; 
    }
    return $hash;   
}
sub parse_xml
{
    my ($xml,$c,$cgi) = @_;  
    my $showdel  = $c->{showdeletes};
    my $xmlobject      = new XML::SimpleObject::LibXML(XML => $xml);
    my $servicenotes   = $xmlobject->child("ServiceNotes"); 
    my @entries        = $servicenotes->children("Entry");
    my ($notes,$outside_notes) = parse_service_notes(\@entries);
    return $notes,$outside_notes;
}
sub build_page
{ 
    my ($notes,$outside_notes,$c) = @_; 
    my @linknames         = ('VO Service Notices','VAO Home','VAO Feedback');
    my $voheader             = new Table::VOheader("VO Notices",\@linknames);   
    print "Content-type: text/html\n\n";
    $voheader->printheader("VO");
    my $dumper = new Table::NoteDumper($notes,$c);
    my $loginbox = new Table::LoginBox($c);
    $loginbox->printbox();
    $dumper->gen_table_header($showdel,$c);  
    $dumper->print_notes();
    $dumper = new2 Table::NoteDumper($outside_notes,$c,'override');
    $dumper->gen_table_header($showdel,$c);
    $dumper->print_notes();
    $dumper->end_table();       
    gen_footer_bas();    
}
sub sort_notes
{
    my ($notes) = @_;  
    my $tmp = {};
    my %hash;    
    tie %hash, "Tie::IxHash";
    foreach my $entry (keys %$notes)
    {
	#print "EE: $entry<br>'";
       	my @array = (split /\|/, $entry);
	shift @array;	
        my @dates = (split /\:/,$array[3],4);
	#print "Oa: $dates[2]\n";
	my ($y,$m,$dh) = (split /\-/,$dates[0]);
	my ($d,$h) = (split / /, $dh);
	my ($yb, $mb, $dbhb) = (split /\-/, $dates[2]);
	my ($db,$hb)  = (split / /, $dbhb);
        #print "QQ: $yb, $mb, $db, $hb\n";
	my $epoch  = convert_date_to_epoch_A($y,$m,$d,$h);
	my $epochb = convert_date_to_epoch_A($yb,$mb, $db,$hb);
        my $newentry = "$epoch|$epochb|$entry";
	$$tmp{$newentry} = 1;
    }
    foreach my $e  (sort mysort  keys %$tmp){ 
	$hash{$e} = 1;
    }
    return \%hash;
}
sub mysort
{
    my @entryA = (split /\|/, $a); 
    my @entryB = (split /\|/, $b);
    my $epoch_A = $entryA[0];
    my $epoch_B = $entryB[0];
    return $epoch_A <=> $epoch_B;
}
