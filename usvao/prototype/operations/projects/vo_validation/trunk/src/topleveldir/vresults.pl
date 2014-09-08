#!/usr/bin/perl -wT
#
#
#  name : vresults.pl 
#
#  description: shows the contents of the validation db
#
#
#
#
{
    use strict;
    use URI::URL;
    use LWP::UserAgent;
    use CGI;
    use lib "./";
    use lib "/www/server/vo/validation";
    use data::startup;
    use Table::Table;
    use HTML::DisplayTable;
    use DBI;
    use HTML::VOheader;
    use HTML::Layout;
    use HTML::StatsTable;
    use SQL::Queries;
    use HTML::ErrorMessage;
    use Switch;
    use HTML::Page;
    use Util::TableTools;
    use HTML::Title;
    use Tie::IxHash;
    use Connect::MySQLValidationDB; 
   

    use Table::VOTable;
    #$ENV{MYSQL_UNIX_PORT} = "$::mysqlunixport";
   
    my $cgi      = CGI->new();
	
    my $show        = detaint("show",$cgi->param("show"))     if ($cgi->param("show"));
    my $querystring = detaint("querystring",$cgi->param("querystring"))        if ($cgi->param("querystring"));
    my $orderby  = detaint("orderby",$cgi->param("orderby"))  if ($cgi->param("orderby"));
    my $uniqueid = detaint("unique",$cgi->param("uniqueid"))  if (  $cgi->param("uniqueid"));
    my $sid      = detaint("sid",$cgi->param("sid"))          if ($cgi->param("sid"));  
    my $runid    = detaint("runid",$cgi->param("runid"))      if ($cgi->param("runid"));
    my $index    = detaint("index",$cgi->param("index"))      if ($cgi->param("index"));
    my $switch   = detaint("switch",$cgi->param("switch"))    if ($cgi->param("switch")); 
    my $center   = detaint("center",$cgi->param("center"))    if ($cgi->param("center"));
    my $votable  = detaint("votable",$cgi->param("votable"))  if ($cgi->param("votable"));
    my $turnoff  = detaint("turnoff",$cgi->param("turnoff"))  if ($cgi->param("turnoff"));
    my $curator  = detaint("curator",$cgi->param("curator"))  if ($cgi->param("curator"));

    print "Content-type: text/html\n\n" if (! $votable); 
    print "Content-type: text/xml\n\n" if (($votable) && ($votable eq 'yes'));
    
   
    $show = '' if (!$show);
    $curator = 'off' if (! $curator);
   
    #connect;
    my $dbh = vodb_connect;
       
    #define container 
    my $container = {	
	                'condition'     => '',      
			'valcode'       => $::valcode,
			'summarycode'   => $::summarycode,
			'dbh'           => $dbh,
			'cgi'           => $cgi,
			'show'          => $show,		     		       
			'tests'         => 'null',
			'sid'           => $sid,
			'runid'         => $runid,
			'orderby'       => $orderby,
			'index'         => $index,
			'switch'        => $switch,
			'center'        => $center,
			'votable'       => $votable,
			'xsitypes'      => &SQL::Queries::get_types($dbh),
			'querystring'   => $querystring,
			'turnoff'       => $turnoff,
			'curator'       => $curator,
 		    };
    
   
    my @linknames = ('VAO Validation','Validation Docs','VAO Monitor','VAO Home', 'NVO Feedback');
    
    my $types_to_skip = get_types_to_skip($container);

    $container->{ignore} = $types_to_skip;

    my $voheader = new HTML::VOheader("Validation Results", \@linknames);
    $voheader->printheader   if (! $votable);
    
    

    if (($show) || ($querystring))
    {    

	#print "UU";
	run($container);
    }
    else 
    {       
	$container =  build_container_homepage($container);
	build_home_page($container);
    }
    gen_footer_bas() if (!$votable);
    $dbh->disconnect || warn "Disconnection failed: $DBI::errstr\n";
}
sub get_types_to_skip
{
    my ($con) = @_;
    my $hold = {'ssap'           => 'ssa:SimpleSpectralAccess',
		'siap'           => 'sia:SimpleImageAccess',
		'cone'           => 'cs:ConeSearch',
		'tap'            => 'tap:TableAccess',
		'vgsearch'       => 'vg:Search',
		'vgharvest'      => 'vg:Harvest',
		'null'           => 'null',
	        };
    my %holdreverse  = reverse %$hold; 
    my $hash = {};

    my $arrayref = getTypesSkipped($con->{dbh});
    foreach my $r (@$arrayref)
    {
          $hash->{$holdreverse{$r->[0]}} = $r->[0];
    }

    #runs when user passes in types to suppress...
    if ($con->{turnoff})
    {
	$hash = {};
	my @array = (split /\|/,$con->{turnoff});	
	
	foreach my $r (@array)
	{
	    $r= lc($r);
	    $hash->{$r} = $hold->{$r} if (exists $hold->{$r});
	}	
    }
    my $s = keys %$hash;
    return $hash;
}
sub detaint
{   
    my ($parname, $value) = @_;
    my $status; 
    
  
    
    switch($parname)
    {
	case "querystring"
	{
	   
	    if  ($value =~  m/([A-Za-z0-9].*[^\<\>\;])/){$value = $1;}
	    else {$status = 1;}
	    
	}
	case "show"
	{
            if  ($value =~ m/(ivo:\/\/.*[^\<\>\;])/){$value = $1;}

	   #elsif ($value =~ m/(sia\:SimpleImageAccess|cs\:ConeSearch|ssa\:SimpleSpectralAccess|vg\:Search|vg\:Harvest|sla\:SimpleLineAccess)/)	
	    elsif ($value =~ m/^(sia\:|cs\:|ssa\:|vg\:|tap\:)(\w+)$/i)
	    {	#print "Content-type: text/html\n\n";	
		#print "TTHhh";
		$value = "$1$2";
		#print "V: $value";
	    }
	    elsif ($value =~ m/(View Results by Center|View Last Set Tested|View Last Set Stats|View Results by Type|details|oldtests|all)/)
	    {
	      
		$value = $1;
	    }
	    else{ $status = 1;} 
	}
        case "curator" { if (($value eq 'on')  or ($value eq 'off')){return $value;}
			else {$status = 1;}}
	case "sid"     {if ($value !~ /(\d+)/)    { $status = 1;}}
	case "orderby" {
	                if ($value =~ m/(^[^\<\>\;]*)$/){$value = $1;}
			else { $status = 1;}
		        }
	case "runid"   {if ($value !~ /^(\d+)$/)  { $status = 1;}}	
	case "index"   {if ($value =~ m/(asc|desc)/) { $value = $1;}
	                else {$status = 1;}}
	case "switch"  {if (($value eq "no")|| ($value eq "yes"))  { return $value;}
	                else {$status = 1;}}
	case "center"  {
	                if ($value =~ /(ivo:\/\/.*[^ \<\>\;])/) {$value = $1;}
			else {$status = 1;}
		       }
	case "votable" { if ($value ne 'yes'){ $status =1;}}
	case "turnoff"  { if ($value =~ m/(^[^\<\>]*)$/){$value = $1;}
                         else { $status = 1;}
                       }
    }
    if ($status)
    {
	my $error = new HTML::ErrorMessage("The parameter or value entered is not recognized");
	$error->display();
	exit();
    }
    #return;
    return $value;
}
#########################
# run 
##########################
sub run
{
    my ($con) = @_;    

    #default setup for pars 
    
    if (!$con->{'orderby'})
    {               
	$con->{'orderby'} = 'time';
	$con->{'index'}   = 'desc';
    }
    $con->{'func'}    = "allids_html";
    
    $con->{'colfunc'} = "fixcolname_services";
    my $s             = $con->{'show'};
    my $q             = $con->{'querystring'};
    
    if (($s) and ($q)) {print "cannot have both";exit();}
    if (($s) or ($q))
    {
	
	if  (($s eq 'View Results by Center') || ($s eq 'View Results by Type')) 
	{ 
	    
	    #routine handling buttons on primary page
	    primarypage_button_handler($con);
	}
	elsif ($s eq 'View Last Set Tested')
	{
	    lastsethandler($con);
	    
	}
	elsif ($s eq  'View Last Set Stats')
	{
	    build_small_stats($con);	    
	}
	elsif ($s eq 'details')
	{		    
	    #handle unique details poge
	    detailspage_handler($con);	
	}
	elsif (($s eq 'oldtests')  and ($con->{'sid'}))
	{	 
	    #history of indidividual services
	    oldtestspage_handler($con);  
	}
	else
	{
	    #query box
	    default_handler($con);
	}
    }
}
##########################
#
##########################
sub lastsethandler
{
    my ($con) = @_;   
    my @cnames        =  ('serviceId','shortname','ivoid','status','time','serviceURL','runtest','type', 
			 'test_ra', 'test_dec', 'radius');  
    my $cnotes       = get_notes();
      
    my $string;
    $string = $con->{show} if ($con->{show});

    $string = $con->{querystring} if ($con->{querystring});
    $titleobj  =  new HTML::Title($string,\@cnames);
    my $dt     = new HTML::DisplayTable($titleobj, "getTodaysList", \@cnames,$con,$cnotes);
    
    #return a votable containing results
    if  ($con->{'votable'} && ($con->{'votable'} eq 'yes'))
    {	    
	show_votable($dt);
    }
    else
    {
	$dt->displayTable();
    }
}
##############################
#
##############################
sub default_handler
{
    my ($con)   = @_; 
    
    my @cnames  = ('serviceId','shortname','ivoid','status','time','serviceURL','runtest', 'type', 
			 'test_ra','test_dec', 'radius');
    my $cnotes  = get_notes(); 
    
    my $string;
    $string       = $con->{show} if ($con->{show});
    $string       = $con->{querystring} if ($con->{querystring});
    $titleobj     = new HTML::Title($string,\@cnames);
    my $dt        = new HTML::DisplayTable($titleobj, "getCenterStats", \@cnames,$con,$cnotes);
    
    #return a votable containing results
    if  ($con->{'votable'} && ($con->{'votable'} eq 'yes'))
    {	    
	show_votable($dt);
    }
    else
    {
	#handles show all services link on main page or search box 
	$dt->displayTable();	    
    }
}
sub get_notes
{
    my $hash ={};

    $hash = { 
             
	      'shortname'  => 'description',
	      'ivoid' => 'registry entry',
	      'status'     => 'last test status',
	      'serviceURL' => 'base url of service',
	      'runtest'    => 'run test',
	    };

    return $hash;
}
#################################################
# primary page buttons (display results for each)
#################################################
sub primarypage_button_handler
{
    my ($con)  = @_;
    my @cnames = ('serviceId','shortname','status','time','serviceURL','runtest', 'ivoid',  
			 'test_ra', 'test_dec', 'radius');
    my $cnotes = get_notes();
    my ($hash,$sqlquery,$types_to_skip,$pos);
    if ($con->{'show'} eq 'View Results by Center')
    {
	$hash = get_centernames($con->{'dbh'});
	$pos = '6';
    }
    else
    {	   
	$hash = get_types($con->{'dbh'});
	$pos  = '5';
    }
    $sqlquery  = "getResults";
   
	
    my $dt              =  new HTML::DisplayTable("none",$sqlquery, \@cnames,$con, $cnotes);
    my $res             =  $dt->getResponse();
    #$res                =  post_processb($res,$types_to_skip); 
    $types_to_skip      = $con->{ignore};

    $con->{'condition'} = $con->{'show'};
    tie my  %newhash,"Tie::IxHash";
   
 
    foreach my $name (sort keys %$hash)
    {	  
	$con->{'show'} =  $hash->{$name};	
	my @b          = grep { $_->[$pos] =~ /$hash->{$name}/i} @$res;	
	my $size       =  scalar(@b);	
	my $stats  = getnumbers(\@b,'2');	
	my @array                    =($stats->{'pass'},$stats->{'fail'},$stats->{'abort'},
				      $stats->{'notval'},$size,$con->{show},
				      $stats->{'skip'},$stats->{'deleted'},
				      $stats->{'deprecated'});
	$newhash{$name}              = \@array;	       
    }
    my $statstable                   = new HTML::StatsTable(\%newhash,$types_to_skip);	
    $statstable->display;
}
########################################
sub oldtestspage_handler
{
    my ($con) = @_;
    $con->{'func'}     =  "oldtests_html";
    $con->{'colfunc'}  = "fixcolname_oldtest";
    my @cnames         = ('serviceId','runid', 'status','type', 'time');
    my $cnotes         = get_notes();
    my $titleobj       = new HTML::Title("oldtests",\@cnames);
    my $dt             =  new HTML::DisplayTable($titleobj, "getOldTables", \@cnames, $con,$cnotes);	 
    $dt->displayTable(); 
}
########################################
# handle details page for a given center
########################################
sub detailspage_handler
{
    my ($con)  = @_;
    $con->{'func'}      =  "uniquetest_html";
    $con->{'colfunc'}   = "fixcolname_uniquetest";     
    my @cnames          = ('serviceId','runid', 'subtestid', 'error');
    my $cnotes         = get_notes();
    
    my $titleobj       = new HTML::Title("Details",\@cnames,$con);
    my $dt              =  new HTML::DisplayTable($titleobj, "getStatusTable", \@cnames, $con,$cnotes);
    $dt->displayTable() if (! $con->{'votable'});
    if ($con->{'votable'})
    {	    	   
	my $r = $dt->getResponse();
	my  $vt = new Table::VOTable($r);
	$vt->printVOTable();	    
    }
}
######################
sub get_centernames
{
    my ($dbh) = @_;
    my %hash;
    my %tmp;
    my %new;

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
	$hash{$id} =  $name;
    }
    close F;

    my $authids = get_authids($dbh);
    foreach my $n (@$authids)
    {	
	if (exists($hash{"@$n"}))	    
	{
	    $new{$hash{"@$n"}}  = "@$n";	    
	}
	else
	{
	    $new{"@$n"} = "@$n";
	}
    } 
    return \%new;
}
###############################
#
#################################
sub show_votable
{
    my ($dt) = @_;
    my $res       = $dt->getResponse();
    my $line      = pop @$res;
    my $runid     = pop @$line;
    my $sid       = shift @$line;
    
    my $url       = URI->new("$::valcode?show=details&sid=$sid&runid=$runid&switch=no&votable=yes");
    my $ua        = LWP::UserAgent->new;
    my $response = $ua->get($url);
    print $response->content; 
}
######################################
# build container data for home page
######################################
sub build_container_homepage
{
    my ($container)          = @_;
    
    $container->{'servicestable'} = getTotalServiceTests($container->{'dbh'});
    my $lastval                   = get_lastvaltime($container->{'dbh'});
    my $nextval                   = getNextValTime();
    my $notvalprior               = getNeverValidated($container);
    my $deleted_array             = getDeleted($container);
    my $deprecated_array         = getDeprecated($container);
    foreach my $n (@$deleted_array)
    {
    }
   
    my $deleted                   = $deleted_array->[0][0];
    my $deprecated               = $deprecated_array->[0][0];

    #arrayref does not contain untested services or deletes
    #it will contain tap/cone/siap xsitypes of the same ivoid;
    my $arrayref                  = getCurrentSnapshot($container);
    my $active                      = scalar(@$arrayref);
    
    #get number of services we are now skipping
    my $arrayref1 = getSkipped($container);
    my $skip = $arrayref1->[0][0]; 

    $active = 0 if (!$active);
    
    my $stats    = getnumbers($arrayref,"2","homepage");
    
    
    
    #final number of active needs fixing 
    #print "Size: $active, $deleted,   $skip, $deprecated, $notvalprior<br>";

    my $number    = $active;
    $number  = '1' if ($active == '0');
   
    $container->{'activeservices'} = $active;
    $container->{'validated'}      = $active;
    $container->{'deleted'}        = $deleted;
    
    $container->{'notvalprior'}    = $notvalprior;
    $container->{'lastval'}        = $lastval;
    $container->{'nextval'}        = $nextval;
    $container->{'passed'}         = $stats->{'pass'};
    $container->{'failed'}         = $stats->{'fail'};
    $container->{'skip'}           = $skip;
    #$container->{'deleted'}        = $stats->{'deleted'};
    $container->{'deprecated'}     = $deprecated;
    $container->{'aborted'}        = $stats->{'abort'};
    $container->{'p_passed'}       = substr((($stats->{'pass'}) * 100)/$number,0,4);
    $container->{'p_failed'}       = substr((($stats->{'fail'})*100)/$number,0,4);
    $container->{'p_aborted'}      = substr((($stats->{'abort'})*100)/$number,0,4);
    $container->{'p_deleted'}      = substr((($stats->{'deleted'})*100)/$number,0,4);
    $container->{'p_notvalprior'}  = substr((($stats->{'notval'})*100)/$number,0,4); 
    $container->{'p_skip'}         = substr ($skip*100/$number,0,4);
    $container->{'p_deprecated'}   = substr((($stats->{'deprecated'}) * 100)/$number,0,4);
    return $container;
}
#####################
sub getNextValTime
{
    my ($mday, $mon, $year) = (localtime(time))[3,4,5];    
    $year += 1900;
    
    my $nextmon     = $mon+1;
    my $daytoshow   = "1";
    my $hms_to_show = "00:00:00";
    
    if($nextmon == '12')
    {
	if ($mday == '31')
	{
	    $year += 1;
	}
	$nextmon = '0';
    }
    my @array = (Jan, Feb, Mar,Apr,May, Jun, Jul, Aug, Sep, Oct, Nov,Dec);
    return "$array[$nextmon] $daytoshow $year $hms_to_show";
}
######################### 
sub add_column_names
{
    my ($dbh,$c,$container) = @_;
    my $url = $container->{'valcode'};
    $url .=  "?show=";
    $url .= $container->{'show'};
    $url .= "&orderby=";
    $url .= $c;   
    #my $query  = get_special_sql_query($c); 
    print "<td>\n";
    print "<a href = \'$url\'><font style = \"color:white\"> $c</a>\n</td>\n";
}
###########################
sub show_all_urls
{
    my ($Services,$service_cols) = @_;
    print "<table class = tac  border =1, cellpadding  = 1 cellspacing = 3>\n";
    foreach   my $entry  (keys %$Services)
    {
        print "<tr class = greenln>\n";
        my @array  = split (/\|/, $entry);
	print "<td align = left><a name = $array[2]>$array[2]</td>";      
    }
    print "</table>";
}
#########################
sub build_column_names
{
    my ($name) = @_;
    print  "<tr class = greenln>\n";
    foreach my $a (@$name)
    {	
        my @row = @$a;	
        print "<td>$row[0]</td>";      
    } 
    print "</tr>\n";
}
#########################
# build_title
#########################
sub build_title
{
    my ($tableobj) = shift;
    print "<tr class = titleblue ><td colspan = 100>", $tableobj->getname(),"</td></tr><br>";
}
#########################
sub trim
{
    my ($string) = @_;
    $string =~ s/^\s+//g;
    $string =~ s/\s+$//g;
    return $string;
} 

