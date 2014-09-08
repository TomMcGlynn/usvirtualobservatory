#!/usr/bin/perl -wT


{
    use strict; 
    use lib './';   
    use lib '/www/server/vo/validation';
    use CGI;
    use data::startup;
    use HTML::ErrorMessage;
    use HTML::Layout;
    use Switch;
    use HTML::VOheader;
    use HTML::Parent;
    use SQL::Queries;
    use LWP::UserAgent;
    use HTML::Util;
    use Connect::MySQLValidationDB;
    my $cgi = CGI->new(); 

    print "Content-type: text/xml\n\n";
    #print "Content-type: text/html\n\n";
     
    my $id   = detaint("id",$cgi->param("id")) if ($cgi->param("id"));
    my $type = detaint("type",$cgi->param("type")) if ($cgi->param("type"));

    #connect to db
    my $dbh  = vodb_connect();
    
    #get xsitypes    
    my $file = "./data/loadtypes";
    my $typeshash = load_types($file);  

    #get validator url, pos info
    my $valURL; 
    my $r = getData($dbh,$type,$id);
    my @n = @$r;
    my $row = shift @n;
    my ($regtype,$valurl,$validatorid,$t,$ivoid,$test_dec,$test_ra,$sr) = @$row; 
     $valURL = $valurl if ($type  eq $t);

    #get service url 
    my $urlarrayref = getserviceURL($dbh,$id,$typeshash->{$type});
    my $url = $$urlarrayref[0][0];
    $url =~ s/\&amp;/&/g;
    $url = encode($url);

    #get complete url
    my $service      =  HTML::Parent::init($url,$type,$id,$valURL,$test_dec,$test_ra,$sr);
    my $runurl       =  $service->getvalurl;
    #run validation test
    my $ua = LWP::UserAgent->new();
    my $res = $ua->get($runurl);
    
    print $res->content;
    exit();
}
sub load_types
{
    my $file  = shift; 
    open (FILE, "$file") || die "cannot open file";
    my %hash; 
    while (<FILE>)
    {
       my $line = $_;
       chomp $line;
       my ($t, $type) = split (/,/,$line);
       $hash{$type}  = $t;
     }
    close FILE;
    return \%hash;
}
sub detaint
{
    my ($parname, $value) = @_;
    my $status; 
    
    switch($parname)
    {
        case "id"{ 
            if  ($value =~ m/(ivo:\/\/.*[^\<\>\;])/){$value = $1;}
            else {$status = 1;}    
        }
        case "type" {
            if ($value =~ /^(\w+)$/)  { $value  = $1;}
            else {$status = 1;} 
        }     
        case "url"  {
            if ($value =~ /^(http:\/\/(.*))$/) { $value = $1;}
            else {$status = 1;}
        }
    }

    if ($status)
    {
        my $error = new HTML::ErrorMessage("The parameter or value entered is not recognized");
        $error->display();
        exit();
    }
    return $value;
 
}
