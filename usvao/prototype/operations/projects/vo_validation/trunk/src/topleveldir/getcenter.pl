#!/usr/bin/perl -wT
#
#

#

use strict;
{   
   
    use vars qw(%param);
    
    use CGI;
    use lib "./";
    use lib "/www/server/vo/validation/";
    use data::startup;
    use Getopt::Long;
    use Tie::IxHash;
    use HTML::Layout;
    use Switch;
    use HTML::DisplayTable;
    
    use HTML::ErrorMessage;
    use Connect::MySQLValidationDB;    
    use DBI;
    use SQL::Queries;
    use HTML::Title;
    
    

   
    #params
    my $cgi      = CGI->new();
    
    my $orderby  = detaint("orderby", $cgi->param("orderby"))  if ($cgi->param("orderby"));
    my $uniqueid = detaint("uniqueid",$cgi->param("uniqueid")) if ($cgi->param("uniqueid"));
    my $sid      = detaint("sid",$cgi->param("sid"))           if ($cgi->param("sid"));
    my $runid    = detaint("runid", $cgi->param("runid"))      if ($cgi->param("runid"));
    my $index    = detaint("index", $cgi->param("index"))      if ($cgi->param("index"));
    my $switch   = detaint("switch",$cgi->param("switch"))     if ($cgi->param("switch")); 
    my $center   = detaint("center",$cgi->param("center"))     if ($cgi->param("center"));
    my $votable  = detaint("votable",$cgi->param("votable"))   if ($cgi->param("votable"));    
    my $error    = detaint("error",$cgi->param("error"))       if ($cgi->param("error"));    
     
    #centernames hash
    my $centernames= get_centernames();
   
    print "Content-type: text/html\n\n";
    my $show     = detaint("show",$cgi->param("show"), $centernames)   if ($cgi->param("show"));
    

    #connect to the db
    my $dbh = vodb_connect();

   
    
   
    #error handling
    if (! $show)
    {
	print "need a center name";
	exit();
    }
    
    #main routine
    
    #gen top
    my $title = "Center: $show";
    $title = "Errors for Center: $show" if ($error);
    
    gen_header_layout($title,["Monitor","notices","NVO Home","NVO Feedback", "Validation"]);    
    $show        = $param{show} if $param{show}; 
    

    #get data 
    my $con = {
	       'cgi'     => $cgi,
	       'runid'   => $runid,
	       'orderby' => $orderby,
	       'center'  => $center,
	       'dbh'     => $dbh,
	       'show'    => $show,
	       'colfunc' => "fixcolname_services",
	       'index'   => $index,
	       'valcode' => $::valcode,
	       'summarycode' => $::summarycode,
	       'centernames' => $centernames,  
	       'error'      => $error,
	       'xsitypes'  =>  &SQL::Queries::get_types($dbh), 		   
 	      };

    if (!$con->{'orderby'})
    {               
        $con->{'orderby'} = 'time';
        $con->{'index'}   = 'desc';
    }

    my ($array,@cnames,$sqlquery,$titleobj);
    my $cnotes = get_notes();

    if (! $error)
    {
	#default    
	@cnames = ("Errorname", "Error Description","Frequency");
	$con->{func} = "special";
	$titleobj = new HTML::Title($show,\@cnames);
	$sqlquery = "get_error_frequency";
	
    }
    else
    {
	
	@cnames = ('serviceId','shortname','ivoid', 'status','time','serviceURL','runtest', 'type',
		   'test_ra','test_dec','radius');
	
	$con->{func} = "allids_html";
	$con->{center} = $show;
	
	$titleobj   = new HTML::Title($show,\@cnames,$con);
	
	$sqlquery  = "get_services_matching_errortype";
    } 
    
    $dbh->disconnect;

    #display results
    
    my $dt   = new HTML::DisplayTable($titleobj,$sqlquery,\@cnames,$con,$cnotes);
    $dt->displayTable();
    gen_footer_bas();
}
sub get_notes
{
    my $hash ={};

    $hash = { 
              'shortname'  => 'description',
              'ivoid' => 'registry entry',
              'status'     => 'last test result',
              'serviceURL' => 'base url of service',
              'runtest'    => 'run test',
            };

    return $hash;

}
sub detaint
{
    my ($parname, $value, $centernames) = @_;
    my $status;
  
    switch($parname)
    {
       
        case "show" {  
	    
	                if (exists ($centernames->{$value}))
			{
			    return $value;
			}
			elsif ($value =~ m/^(sia\:|cs\:|ssa\:|vg\:|tap\:)(\w+)$/i)
			{   
			    $value ="$1$2";
			    return $value;
			}
			
			else {$status = 1;}
		    }
        case "sid"     {if ($value !~ /(\d+)/)    { $status = 1;}}
        case "orderby" {if ($value !~ m/^(\w+[^ \;])$/) { $status = 1;}}
        case "runid"   {if ($value !~ /^(\d+)$/)  { $status = 1;}}      
        case "index"   {if ($value =~ m/(asc|desc)/) { return $value;}
                        else {$status = 1;}}
        case "switch"  {if (($value eq "no")|| ($value eq "yes"))  { return $value;}
                        else {$status = 1;}}
        case "center"  {
                        if ($value =~ /(ivo:\/\/.*[^ \<\>\;])/) {$value = $1;}   
                        else {$status = 1;}
                       }
        case "votable" { if ($value ne 'yes'){ $status =1;}}
	case "error" { if ($value =~ /(^[0-9a-zA-Z\.\/\-\s]*[^ \<\>\;\:]$)/){ $value = $1;}
		       #elsif ($value =~ m/(^[A-Za-z]+$)/){ $value= $1;}
		       else { $status = 1;}
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
###########################
#
###########################
sub show_data
{
    my ($array,$name) = @_;
    print "<table class = 'tac' width = 680 align = center border =1>";
    print "<tr class = titleblue><td colspan = 3>Center: $name</td></tr>";
    print "<tr class = greenln><td>Error</td><td>Error Message</td><td>Frequency</td></tr>";
    foreach my $errorline (@$array)
    {          
	print "<tr class = greenln>";
	foreach my $string (@$errorline)
	{
	    print "<td align = left>$string</td>";  
	}
	print "</tr>";
    }
    print "</table><br><br>";
}
############################
#
###########################
sub load_centers
{
    
    tie my %centers, "Tie::IxHash";
    %centers = (
		'%CDS%'    =>1,
		'%ledas%'  => 1,
		'%Heasarc%' => 1,
		);

    
    return \%centers;
}
###########################
sub trim
{

    my ($string) = @_;
    $string =~ s/^\s+//g;
    $string =~ s/\s+$//g;
    return $string;
}
############################
sub get_centernames
{
    my %hash;
    tie %hash, "Tie::IxHash";
    
    open (F,"$::centernames") ||
        die "cannot open file $::centernames";
    while (<F>)
    {
        my $line = $_;
        next if $line =~ /^$/;
        chomp $line;
        my ($id,$name)  = (split /,/, $line);
        $id = trim($id);
	$name = trim($name);
        $hash{$name} = $id;
    }
    
    close F || die "cannot close $::centernames";
    return \%hash;
}

   
   
