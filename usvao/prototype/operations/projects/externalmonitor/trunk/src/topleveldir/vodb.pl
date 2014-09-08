#!/usr/bin/perl -wT
#
#
#
{

    use lib './';
    use lib '/www/server/vo/external_monitor';

    use CGI;
    use DBI;
    use data::startup;
    use Connect::MySQLVaomonitorDB;
    use HTML::VOheader;
    use LWP::UserAgent;
    use HTML::VOnote;
    use Table::VOTable;
    use SQLVo::Queries;
    use HTML::Top;
    use HTML::Banner;
    use HTML::Table;
    use Tie::IxHash;
    use HTML::Bar;
    use HTML::Layout;
    use HTML::Title;
    use Stats::TestResult;
    use Switch;
    use HTML::ErrorMessage;
    use HTML::DisplayTable;
    use HTML::BuildButton;
    
    #print "Content-type: text/html\n\n";
    
   my $cgi = CGI->new();
    my $dbh = vaomonitor_connect();
    
    my $show     = detaint("show",$cgi->param("show"))        if ($cgi->param("show"));
    my $orderby  = detaint("orderby",$cgi->param("orderby"))  if ($cgi->param("orderby"));
    my $uniqueid = detaint("unique",$cgi->param("uniqueid"))  if (  $cgi->param("uniqueid"));
    my $sid      = detaint("sid",$cgi->param("sid"))          if ($cgi->param("sid"));  
    my $runid    = detaint("runid",$cgi->param("runid"))      if ($cgi->param("runid"));
    my $index    = detaint("index",$cgi->param("index"))      if ($cgi->param("index"));
    my $switch   = detaint("switch",$cgi->param("switch"))    if ($cgi->param("switch")); 
    my $type     = detaint("type",$cgi->param("type"))        if ($cgi->param("type")); 
    my $offset   = detaint("offset", $cgi->param("offset"))   if ($cgi->param("offset"));
    my $output   = detaint("output",$cgi->param("output"))    if ($cgi->param("output"));
    my $format   = detaint("votable",$cgi->param("format"))   if ($cgi->param("format"));
    print "Content-type: text/html\n\n" if  (! $format);   
    print "Content-type: text/xml\n\n" if ($format);  

    my @pars = $cgi->param;

    if ($show)
    {
	$offset = '' if (($show eq 'uptime') and (!$offset));
	$offset = '0' if (($show ne 'uptime') and (!$offset));
    }
  
    
    
    my $con = {'dbh' => $dbh,
	       'cgi' => $cgi,
	       'runid'  => $runid,
	       'sid'   => $sid,
	       'orderby' =>$orderby,
	       'index'  => $index,
	       'valcode'=> $::valcode,
	       'type'   => $type,
	       'offset' => $offset,
	   };    

    $con->{format} = $format if ($format);

    my @linknames   = ('VO Service Notices','VAO Monitor Interactive Testing','VAO Monitor Help','VAO Home', 'VAO Feedback');
    my $voheader        = new HTML::VOheader("Monitor",\@linknames);
    
    
    
    if (! $cgi->param())
    {
	
	$voheader->printheader();
	$con->{orderby} = 'm.displayorder';
	build_homepage($con);
	HTML::Table::add_footer();
	exit(1);
	
    }
    else
    {
	if (($pars[0] eq 'format') and (scalar(@pars) == '1'))
	{ 
	    my $servicenames = getcurrentHealth($con);
	    my $hash =  store_data($servicenames);
	    
	    generate_xml($hash);	   
	    exit;
	}
	$voheader->printheader();
	$show = '' if (!$show);
	$con->{show} = $show;
	run($con);
	HTML::Table::add_footer();
	exit(1); 
    }
	
}
sub generate_xml
{
    my ($h) = @_;
    print "<TABLE>";
    dump_lines($h,"xml");   
    print "<\/TABLE>";
   
    
}
sub detaint
{   
    my ($parname, $value) = @_;
    my $status;
  
    switch($parname)
    {
        case "show"
        {
	  
            if  ($value =~  m/(^[A-za-z0-9\s\/]*[^ \<\>\:])$/){ $value = $1;}
            
            elsif ($value =~  m/^(details|oldtests|all)$/)
            {	       
                $value = $1;
            }
            else{ print "should not be here<br>"; $status = 1;} 
        }
        case "sid"     {if ($value !~ /(\d+)/)    { $status = 1;}}
        case "orderby" {if ($value !~ m/^(\w+)$/) { $status = 1;}}
        case "runid"   {if ($value !~ /^(\d+)$/)  { $status = 1;}}      
        case "index"   {if ($value =~ m/(asc|desc)/) { $value = $1;}
                        else {$status = 1;}}
        case "switch"  {if (($value eq "no")|| ($value eq "yes"))  { return $value;}
                        else {$status = 1;}}
        case "center"  {
                        if ($value =~ /(ivo:\/\/.*[^ \<\>\;])/) {$value = $1;}   
                        else {$status = 1;}
		       }
        case "format" { if ($value ne 'yes'){ $status =1;}}
	case "type"   { if ($value ne 'oldtests'){ $status =1;}}
	case "offset" {
	               if ($value =~ /^(\d*)$/){ $value = $1;}		      
		       else {$status = 1;}
		      }
	case "output" { 
	               if ($value eq 'xml'){ $value = $1;}
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
sub run
{ 
    my ($con)  = @_;   

    #default setup for pars 
   
    if (!$con->{'orderby'})
    {               
        $con->{'orderby'} = 'time';
        $con->{'index'}   = 'desc';
    }
    $con->{'func'}    = "allids_html";
    
    $con->{'colfunc'} = "fixcolname_services";
    switch ($con->{show})
    {
	case   'all'     {all_services_handler($con);}
	case   'details' {details_page_handler($con);}
	case   'uptime'  {uptime_handler($con);}
       
        else             
	{
	    #service name passed as cgi par
	    if (($con->{type}) && ($con->{type}  eq 'oldtests')) 	       
	    {
		oldtestspage_handler($con);
	    }
	    else
	    {
	        #show current errors for services	 
		default_handler($con);
	    }
	}	    
    }
}
sub uptime_handler
{  
    my ($con)   = @_;
    use Util::TimeStampWrapper;   
    my $up           = new Util::Uptime($con);
    my $uptime       = $up->getUpTime();   
    my $name         = $up->getName();   
    $con->{name}     = $name;    
    my @cnames        = ('name', $up->getColChange());
    $con->{'colfunc'} = "fixcolname_uptime";
    $con->{'func'}    =  "uptime_html"; 
    my $titleobj     = new HTML::Title("Uptime/Downtime");
    my $dt            = new HTML::DisplayTable($titleobj, $uptime, \@cnames,$con);
    $dt->displayTable();
} 
sub default_handler
{
   
    my ($con)   = @_;
    my @cnames        = ('name','serviceId','testid','testname','type', 'status','time','runid');
    
    $con->{'colfunc'} = "fixcolname_services";	   
    
    my $titleobj     = new HTML::Title($con->{show});
    my $dt            = new HTML::DisplayTable($titleobj, "getCurrentHealthService", \@cnames,$con);
    $dt->displayTable();
} 
sub all_services_handler
{
    my ($con)   = @_;

     my @cnames        = ('name','serviceId','testid','testname','type','status','time','runid');	  
    $con->{'colfunc'} = "fixcolname_services";	
    my $titleobj     = new HTML::Title($con->{show});
    my $dt            = new HTML::DisplayTable($titleobj, "getcurrentHealth", \@cnames,$con);
    #dump all data to a hash of hashes
    $dt->reProcess();  
    $dt->displayTable();
} 
sub build_homepage
{
    my ($con)  = @_;
    use HTML::Addons;
    my $rowlimit = '30';
    
    my $servicenames;

    print "<table class = tac  align = center cellspacing = 3 >\n";
    #print "<tr><td  valign = top>\n";   
   
    #get data 
    $servicenames = getcurrentHealth($con);
    $bar =  new HTML::Bar("External Services", "titleblue", "4","tr"); 
    $bar->print_bar();

    print "<tr><td valign = top><table class = 'tac' width =300>\n"; 
    $bar =  new HTML::Bar("Service name", "label", "","td"); 
    $bar->start;
    $bar->print_bar();
    $bar =  new HTML::Bar("Status", "label", "","td");
    $bar->print_bar();
    $bar->end;   
    
    #print the first 30 lines
    my $hash = store_data_by_type($servicenames,'External');
    dump_lines($hash,"html",$rowlimit);
    print "</table></td>\n";
   
    #print the remaining lines
    print "<td valign = top>";
    print "<table class = 'tac' width =300>\n";
    $bar =  new HTML::Bar("Service name", "label", "","td"); 
    $bar->start;
    $bar->print_bar();
    $bar =  new HTML::Bar("Status", "label", "","td");
    $bar->print_bar();
    $bar->end;   
    dump_lines($hash,"html",$rowlimit);       
    print "</table></td></tr>\n";
    print "</table>\n";

    #print monitor check table
    print "<br><br>";
    HTML::Addons::add_timestamp_box($con->{cgi},$con->{dbh});
}
sub run_notes_tool
{
    my ($url) = @_;
    my %notes;
    tie %notes, "Tie::IxHash";
    my $ua             = LWP::UserAgent->new();
    my $res            = $ua->get($url);
    my $xml            = $res->content;
    my $xmlobject      = new XML::SimpleObject::LibXML(XML => $xml);
    my $servicenotes   = $xmlobject->child("ServiceNotes"); 
    my @entries        = $servicenotes->children("Entry");
    $notes             = parse_service_notes(\@entries);   
    return $notes;    
}
sub oldtestspage_handler
{
    my ($con) = @_;
    #$con->{'func'}     =  "oldtests_html";
    $con->{'colfunc'}  = "fixcolname_oldtest";
    
    my @cnames        = ('name','serviceId','testid','testname','type', 'status','time','runid');
    my $titleobj       = new HTML::Title("oldtests");
    my $dt             =  new HTML::DisplayTable($titleobj, "getOldTables", \@cnames, $con);     
    $dt->displayTable(); 
}
sub details_page_handler
{
    my ($con)  = @_;
    $con->{'func'}      =  "uniquetest_html";
    $con->{'colfunc'}   = "fixcolname_uniquetest";     
    my @cnames          = ('serviceId','runid', 'subtestid', 'error');
    
    my $titleobj       = new HTML::Title("Details",$con);
   
    my $dt              =  new HTML::DisplayTable($titleobj, "getStatusTable", \@cnames, $con);
    $dt->displayTable() if (! exists($con->{'format'}));
    if ($con->{'format'})
    {              
	
        my $r = $dt->getResponse();	
        my  $vt = new Table::VOTable($r);
        $vt->printVOTable();        
    }
}
sub store_data_by_type
{
    my ($servicenames, $matchtype) = @_;
    my %hash;
    tie %hash, "Tie::IxHash";
    
    foreach my $n (@$servicenames)
    {

	#print "@$n<br>";
	
	my $name = @$n[0];
	my $type = @$n[6];
        my $order = @$n[12];
	my $deleted = @$n[13];
	my $notAvailable = @$n[14];
	
	if ($type eq $matchtype)
	{	   
	    if (exists $hash{$name})
	    {	      
		push (@{$hash{$name}}, [@$n]);
	    }
	    else
	    {
		my @arrayofarrays = ([@$n]);
		$hash{$name} = \@arrayofarrays;
	    }
	}
    }    
    return \%hash;
}

sub store_data
{
    my ($servicenames) = @_;
    my %hash;
    tie %hash, "Tie::IxHash";
    
    foreach my $n (@$servicenames)
    {    
	my $name = @$n[0];
	my $authid = @$n[3];
        my $type = @$n[6];
	my $order = @$n[12];
	
	if (exists $hash{$name})
	{	      
	    push (@{$hash{$name}}, [@$n]);
	}
	else
	{
	    my @arrayofarrays = ([@$n]);
	    $hash{$name} = \@arrayofarrays;
	}
    }
    return \%hash;
}
sub dump_lines
{
    my ($hash,$type,$limit) = @_;
    my $count =0;
    foreach my $name (sort  keys %$hash)
    {
	last if (($count == '30') and ($type ne 'xml'));
	my $array  = $hash->{$name};
	#print "$name: ", scalar(@$array), "<br>";
	my $testresult = new Stats::TestResult($name, $hash->{$name});	
	$testresult->printlinexml()  if ($type eq 'xml');
	$testresult->printlinehtml() if ($type eq 'html');
	delete $hash->{$name};	
	$count++;
    }   
}
