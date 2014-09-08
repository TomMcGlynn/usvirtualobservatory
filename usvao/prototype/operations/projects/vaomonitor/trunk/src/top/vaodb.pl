#!/usr/bin/perl -wT
#
#
#
{
    use lib './';
    use lib '/www/server/vo/vaomonitor';
    use CGI;
    use DBI;
    use data::startup;
    use Connect::MySQLVaomonitorDB;
    use Connect::MySQLVaoOperationsDB;
    use HTML::VOheader;
    use XML::SimpleObject::LibXML;
    use LWP::UserAgent;
    use HTML::VOnote;
    use Table::VOTable;
    use SQLVao::Queries;
    use HTML::Top;
    use HTML::Banner;
    use HTML::Table;
    use Tie::IxHash;
    use HTML::Bar;
    use HTML::Layout;
    use Util::TimeZone;
    use Util::Util;
    use HTML::HarvesterPageScraper;
    use HTML::Title;
    use Stats::TestResult;
    use Switch;
    use HTML::ErrorMessage;
    use HTML::DisplayTable;
    use HTML::BuildButton;
   
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
               'non_science_list' =>  "./data/non_science_services",
	   };    
    $con->{format} = $format if ($format);

    my @linknames   = ('VO Service Notices','VAO Notification Service','VAO Home', 'VAO Feedback');
    my $voheader        = new HTML::VOheader("VAO Monitor",\@linknames);  
    if (! $cgi->param())
    {	
	$voheader->printheader();
	my $script = 'onload = loadColorbar();';
	$voheader->print_banner($script);
	$con->{orderby} = 'm.displayorder';
	build_homepage($con);
	HTML::Table::add_footer();
	exit(1);	
    }
    else
    { 
	if ((scalar(@pars) == '1') and ($pars[0] eq 'format'))
	{	
	    my $servicenames = getcurrentHealthOnlyVAO($con);  
	    my $hash         =  store_data($servicenames);
	    #build xml for data
	    my $content = get_xmldata($hash,"xml") if ((scalar keys %$hash) >  0);  
	    my $xml     = '<TABLE></TABLE>';  
            $xml =~ s/(\<TABLE\>)/$1$content/ if ($content);

            $xml  = post_process_xml($xml);
	    if ($format eq 'xml')
	    {		
		print "Content-type: text/xml\n\n";
		print $xml;
		exit;
	    }	    
	    elsif ($format eq 'html')
	    {
		print "Content-type: text/html\n\n";
		my @linknames       = ('VO Service Notices','VAO Notification Service','VAO Home', 'VAO Feedback');
                my $voheader        = new HTML::VOheader("VAO Science Service Monitor",\@linknames);
		$voheader->printheader();
		$voheader->print_banner();
		generate_page_fragment($xml,$con) if ($xml ne '<TABLE></TABLE>');
		HTML::Table::add_footer();
		exit(1);
	    }
	}	
	$voheader->printheader();
        $voheader->print_banner();	
        $show = '' if (!$show);
	$con->{show} = $show;
	run($con);
	HTML::Table::add_footer();
	exit(1); 
    }	
}
sub generate_page_fragment
{
    my ($content,$con) = @_;
    my $lines = {};
    my $non_science = get_non_science($con->{non_science_list});
    my $xmlobj      = new XML::SimpleObject::LibXML::(XML=>$content);
    my $table       = $xmlobj->child("Table");    
    my @entries     = $table->children("Service");
    foreach my $entry (@entries)
    {
        my $name              = $entry->child("name")->value;
        my $status            = $entry->child("status")->value;
	my $line              =  Stats::TestResult::build_line($name, $status) if (! exists ($non_science->{$name}));
	$lines->{$name} = $line; 
    }    
    
    print "<table class = 'tac' align = center>\n";    
    my $bar =  new HTML::Bar("Science Services","titleblue","4","tr");
    $bar->print_bar;	  
    print "$lines->{'VAO Website'}<tr/><tr/><tr/><tr/>"
	. "$lines->{'Data Discovery Tool'}"
        . "$lines->{'Data Discovery'}"
        . "$lines->{'DataScope Portal'}<tr/><tr/><tr/><tr/>"
        . "$lines->{'IRIS SED'}"
	. "$lines->{'IRIS'}"
        . "$lines->{'NED SED'}<tr/><tr/><tr/><tr/>"
        . "$lines->{'Time Series'}"
        . "$lines->{'Cross Comparison Service'}"
        . "$lines->{'VAO Directory and Registry'}";
    print "</table><br>";
    #add key and notifications
    add_key();
    eval{
    #leave inside eval
     add_notifications();
    };
}
sub get_non_science
{
    my ($non_science_list) = @_;
    my %hash;
    open (File, "$non_science_list")
        || die "cannot open file";
    while(<File>)
    {
        my $line = $_;
        chomp $line;
        $hash{$line} = 1;
    }
    close File || die "cannot close file";
    return \%hash;
}
sub add_key
{
    print "<table class  = 'tac' align = center>"
	 ."<tr><td class = greenln><img src = /tmp.shared/vo/usvao/icons/greensun.png>service up</td>" 
	 ."<td><td class = greenln><img src = /tmp.shared/vo/usvao/icons/redsun.png>service down</td>"
         ."<td><td class = greenln><img src = /tmp.shared/vo/usvao/icons/yellowring.png>halo appears around a service with a pending notice</td>"  
   ."</tr></table><br>";

}
sub post_process_xml
{
    my ($xml) = @_;
    return $xml if ( $xml eq '<TABLE></TABLE>');

    my $xmlobj = new XML::SimpleObject::LibXML::(XML=>$xml);
    my $table  = $xmlobj->child("TABLE");
    
    my %iris;
    my $ddt_status = $xmlobj->xpath_search("/TABLE/Service[name='Data Discovery']/status")->value; 

    #delete IRIS and IRIS sed nodes from xml  tree,get status of each
    my @entries = $table->children("Service");
    foreach my $entry (@entries)
    {
        my $name              = $entry->child("name")->value;
        my $status            = $entry->child("status")->value;
	if (($name  eq 'IRIS SED') || ($name eq 'IRIS'))
	{
	    $iris{$name} =  $status;
	    $entry->delete;
	}
    }
    #see if iris and iris sed are passing;set an overall status
    my @iris = values %iris;
    my @overall = grep (/Fail/, @iris);
    my $overallstatus = 'OK';
    $overallstatus = "Fail" if (@overall);
   
    #build xml chunk with IRIS and DDT data 
    my $newxmlfrag = "<Service><name>IRIS SED</name><status>$overallstatus</status></Service>"
	           . "<Service><name>IRIS</name><status>$iris{IRIS}</status></Service>"
                   . "<Service><name>NED SED</name><status>$iris{'IRIS SED'}</status></Service>"
                   . "<Service><name>Data Discovery Tool</name><status>$ddt_status</status></Service>";
    my $n = $xmlobj->output_xml();
    
    $n =~ s/<\/TABLE>//;
    my $bigxml = $n . $newxmlfrag . "</TABLE>";
    $bigxml =~ s/>\s+/>/g;
    return  $bigxml;
}
sub detaint
{   
    my ($parname, $value) = @_;
    my $status;
  
    switch($parname)
    {
        case "show"
        {	  
            if  ($value =~  m/(^[A-za-z\s\/]*[^ \<\>\:])$/){ $value = $1;}
            
            elsif ($value =~  m/^(details|oldtests|all)$/)
            {	       
                $value = $1;
            }
            else{ $status = 1;} 
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
        case "format" { if (($value ne 'xml') and ($value ne 'html')){ $status =1;}}
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
    my $titleobj      = new HTML::Title($con->{show});
    my $dt            = new HTML::DisplayTable($titleobj, "getcurrentHealth", \@cnames,$con);
    #use hash of hashes
    $dt->reProcess();  
    $dt->displayTable();
}   
sub sort_notes
{
    my ($notes) = @_;
    my $bighash = {};
    my $bighash1 = {};
    
       
    foreach my $entry (keys %$notes)
    {
	my $c = {};
        my @array = (split /\|/, $entry);
        my $id  = $array[0];

        my @dates = (split /\:/,$array[3],4);       

        my ($y,$m,$dh) = (split /\-/,$dates[0]);
	my ($d,$h)  = (split /\s/,$dh);
        my ($yb, $mb, $dbhb) = (split /\-/, $dates[2]);
	my ($db,$hb) = (split /\s/,$dbhb);
	$c->{css_class}   = "noteB";
	$c->{decodedeff}  =  $dates[0] . ":00";
	$c->{decodedexp}  =  $dates[2] . ":00";
	$c->{hostname}    = $array[5];
	$c->{id} = $id;
        $c->{epochA}      = convert_date_to_epoch_A($y,$m,$d,$h);
        $c->{epochB}      = convert_date_to_epoch_A($yb,$mb, $db,$hb);
	$c->{notestatus}  = see_if_active_note($c->{epochA},$c->{epochB});
        if ($c->{notestatus} eq 'pending')
	{
	  $c->{css_class} = "noteA";	
	}
	$c->{priority} = $array[4];
	$c->{prioritystring} = nice_priority($c->{priority}); 
        $c->{note} = $array[2];    
	$c->{reason_deleted}  = $reason  if ($reason);      	
        $c->{affectedservices} = $array[6];
        $c->{otheraffected} = $array[7];
	
        if ((($c->{affectedservices} eq "null") || ($c->{affectedservices} eq 'none')) &&
             (($c->{otheraffected} eq "null") || ($c->{otheraffected} eq 'none')))
        {
            $bighash1->{$id} = $c;
        }
        else  
        {            
            $bighash->{$id} = $c;
        }

    }
    return $bighash1, $bighash;
}
sub add_notifications
{
    my $dbh = vao_operations_connect(); 
    my $hosts = load_notices($dbh,"vao_notices","currentnotes");
    my $notes = load_notes_db($hosts);
    my ($sort1,$sort)= sort_notes($notes);
    
    #gen_help_text($c->{showdeletes});
    display_user_notes($sort);
    display_user_notes($sort1,"1");
}
sub load_notes_db 
{
    my ($array) = @_;
    my $hash = {};
    foreach my $n (@$array)
    {  
        #print "JOn: @$n<br>";
        $n->[3] = "$n->[3]:";
        my $row;
        for (my $i = 0;$i<scalar(@$n); $i++)
        {       
            if (! $n->[$i]){$n->[$i]= '';}        
            if (($i <10) and ($i != '3'))
            {
                
                $row .= "$n->[$i]|";
            }
            else
            {
                $row  .= "$n->[$i]";
            }
        }
        $hash->{$row} =1; 
      
    }
    return $hash;   
}
sub build_homepage
{
    my ($con)  = @_;
    use HTML::Addons;
    
    eval{
    #add notices. Leave inside eval so that the vaomonitor page still loads
    # when there is an issue retrieving notices 
    add_notifications(); 
    };


    my $servicenames;
    
    print "<table class = tac  align = center cellspacing = 3>\n";
    print "<tr><td  valign = top><table class = 'tac' width =300>\n";    
    my $bar =  new HTML::Bar("&nbsp;","label","2","tr");
    $bar->print_bar;

    #portal services table    
    $servicenames = getcurrentHealth($con);
    $bar =  new HTML::Bar("VAO Services", "titleblue", "2","tr"); 
    $bar->print_bar();    
    $bar =  new HTML::Bar("Service name", "label", "","td"); 
    $bar->start;
    $bar->print_bar();
    $bar =  new HTML::Bar("Status", "label", "","td");
    $bar->print_bar();
    $bar->end;   
    
    $bar = new HTML::Bar("Science Services","italicentry","2", "td");
    $bar->start;
    $bar->print_bar();
    $bar->end();
    my $hash = store_data_by_type($servicenames,'Portal Science Services');
    dump_lines($hash,"html");

    $bar = new HTML::Bar("Support Services","italicentry","2", "td");
    $bar->start;
    $bar->print_bar();
    $bar->end();
    $hash = store_data_by_type($servicenames, 'Portal Support Services');
    dump_lines($hash,"html");

    $bar = new HTML::Bar("Testing","italicentry","2", "td");
    $bar->start;
    $bar->print_bar();
    $bar->end();
    $hash = store_data_by_type($servicenames, 'Testing');
    dump_lines($hash,"html");
    print "</table></td>\n";

    #tools table
    print "<td valign = top>";
    print "<table class = 'tac' width =300>\n";   
    $bar =  new HTML::Bar("&nbsp;","label","2","tr");
    $bar->print_bar;    
    $bar =  new HTML::Bar("Legacy Services", "titleblue", "2","tr"); 
    $bar->print_bar();    
    $bar =  new HTML::Bar("Service name", "label", "","td"); 
    $bar->start;
    $bar->print_bar();    
    $bar =  new HTML::Bar("Status", "label", "","td"); 
    $bar->print_bar();
    $bar->end;       
    $hash = store_data_by_type($servicenames, 'Legacy Support Services');
    dump_lines($hash,"html");
    print "</table></td>\n";

    #non-vao services table
    
    my ($services_down,$up_count)    = Util::Util::parse_xml_status("hostsup");
    my $down           = scalar(@$services_down);
    
  
    print "<td valign = 'top'>";
    print "<table style=\"background-color: #FFF8C6;\" width = 300 >\n";   
    $bar = new HTML::Bar("External Sites","label","","tr");
    $bar->print_bar();
    $bar =  new HTML::Bar("$up_count Centers up, $down Centers down", "titleblue", "","tr");
    $bar->print_bar();
    print "<tr><td bgcolor = FFE0A3 ><div id = 'no' stats = \"$up_count:$down\" ></div></td></tr>";
    print "<tr class = greenln><td>List of Centers Failing</td></tr>";  
    
    dump_monitor_results($services_down);	
    print "</table></td></tr>\n";
 
    #system table
    print "<tr><td valign = top><table class = 'tac' width =300>\n";    
    $bar =  new HTML::Bar("System", "titleblue", "2","tr"); 
    $bar->print_bar();
    $bar =  new HTML::Bar("Service name", "label", "","td"); 
    $bar->start;
    $bar->print_bar();   
    $bar =  new HTML::Bar("Status", "label", "","td"); 
    $bar->print_bar();
    $bar->end;
    $hash = store_data_by_type($servicenames, 'System');
    my $p = keys %$hash; 
    dump_lines($hash,"html");    
    print "</table></td>\n";

    #harvests
    $con->{'type'} = 'Harvesters';
    print "<td valign = top><table class = 'tac' width =300>\n";    
    $bar =  new HTML::Bar("Harvesters", "titleblue", "2","tr"); 
    $bar->print_bar(); 
    $bar =  new HTML::Bar("STScI Automated Registry Harvesting", "label", "","td"); 
    $bar->start;
    $bar->print_bar();   
    print "<tr><td class = greenlnextra><a href = 'http://vao.stsci.edu/directory/harvesttable.aspx'>http://vao.stsci.edu/directory/harvesttable.aspx</td></tr>";
    print "</table></td>\n";
    
    #selenium table
    $con->{'type'} = 'Selenium Tests';
    my $names      = load_selenium_testnames();
    $servicenames = getSeleniumStatus();
    print "<td valign = top><table style=\"background-color: #FFF8C6;\" width =300  >\n";    
 
    $bar =  new HTML::Bar("Web Interface Tests Beta", "titleblue", "2","tr"); 
    $bar->print_bar();  
    $bar =  new HTML::Bar("Service name", "label", "","td"); 
    $bar->start;
    $bar->print_bar();
    $bar =  new HTML::Bar("Status", "label", "","td"); 
    $bar->print_bar();
    $bar->end;
    dump_selenium_lines($servicenames,$names);
    print "</table></td></tr></table>\n<br><br>";
    HTML::Addons::add_timestamp_box($con->{cgi},$con->{dbh});
}
sub oldtestspage_handler
{
    my ($con) = @_;
    $con->{'colfunc'}   = "fixcolname_oldtest";    
    my @cnames          = ('name','serviceId','testid','testname','type', 'status','time','runid');
    my $titleobj        = new HTML::Title("oldtests");
    my $dt              = new HTML::DisplayTable($titleobj, "getOldTables", \@cnames, $con);     
    $dt->displayTable(); 
}
sub details_page_handler
{
    my ($con)  = @_;
    $con->{'func'}      = "uniquetest_html";
    $con->{'colfunc'}   = "fixcolname_uniquetest";     
    my @cnames          = ('serviceId','runid', 'subtestid', 'error');   
    my $titleobj        = new HTML::Title("Details",$con);   
    my $dt              = new HTML::DisplayTable($titleobj, "getStatusTable", \@cnames, $con);
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
	my $name = @$n[0];
	my $type = @$n[4];
	my $order = @$n[10];
	
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
	my $name  = @$n[0];
	my $type  = @$n[4];
	my $order = @$n[10];
	
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
sub get_xmldata
{
    my ($hash,$type) = @_;   
    my $xmlbig;
    my $size = keys %$hash;
    foreach my $name ( keys %$hash)
    {
	my $testresult = new Stats::TestResult($name, $hash->{$name},$type);	
        my $xml        = $testresult->buildline_xml();
	$xmlbig       .= $xml;
    }
    return $xmlbig;
}
sub dump_lines
{
    my ($hash,$type) = @_;   
    my @array;
    foreach my $name ( keys %$hash)
    {
	my $testresult = new Stats::TestResult($name, $hash->{$name},$type);	
	if ($type eq 'htmlfrag')
	{
	    my $string = $testresult->build_line();
	    push @array, $string;
	}
	else
	{      
	    $testresult->printlinehtml();
	}    
    }
    if ( @array)
    {
	splice @array, 1,0, "<tr/><tr/><tr/>";
	splice @array, 4,0, "<tr/><tr/><tr/>";
	splice @array, 7,0,"<tr/><tr/><tr/>";
	foreach my $n (@array)
	{
	    print "$n";
	}
    }
}
