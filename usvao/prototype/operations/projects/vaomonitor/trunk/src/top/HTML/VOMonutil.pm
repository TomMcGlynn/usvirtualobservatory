#  
#
#  Description: Utilities for 
#  the VO monitoring tool. Routines
#  that are common/frequently used
#  and may be needed by other programs
#
#
#

package HTML::VOMonutil;
use Date::Calc  qw(Delta_DHMS);
use Socket;
use Exporter ();

@ISA = qw(Exporter);

@EXPORT = qw(trim convert_time status_number_to_text 
	     convert_seconds_to_days  nice_date
	     gen_footer_basic get_hostsuffixes);
use lib '../';
use Tools::ConvertDate;


#######################################################
# see if host is allowed to add a note to monitor page
#######################################################
sub get_hostsuffixes
{
    my ($rhost) = @_;
    my $string;   

    my $iaddr =  inet_aton($rhost);

	#print "JJ: $rhost<br>";
    my($name,$aliases,$addrtype,$length,@addrs) = gethostbyaddr($iaddr,AF_INET);
     
    #see if $name contains a string and handle accordingly. 
    # Goddard connections do not have a name
    #print "$name,$aliases, $addrtype, $length, @addrs<br>";
    if (! $name)
    {         
        $string = $rhost;
    }
    else 
    {
        my @pieces      = (split /\./,$name);
        my $end         =  pop @pieces;    
        my $penultimate =  pop @pieces; 
        $string         = "$penultimate.$end";
    }
    return $string;  
}
#################################################
# nice date
#################################################
sub nice_date
{
    my($date) = @_;
    my ($y,$m, $d,$h) = (split /\-/,$date);
    my $month_hash    = Tools::ConvertDate::get_month_numbers();    
    my $monthuc       = ucfirst($m);  
    $monthuc          = $$month_hash{$monthuc};
    $monthuc          = pad_number($monthuc);
    $d                = pad_number($d);
    $h                = pad_number($h);
    my $nicedate      = "$y-$monthuc-$d $h:00";
    return $nicedate;
}
#################################################
# pad number
#################################################
sub pad_number
{
    my ($number)  = @_;
    $number = "0$number" if ($number =~ m/^[0-9]{1}$/);
    return $number;
}

###################################
# trim whitespace
###################################
sub trim
{
    my ($string) = @_;
    #print "SSPRPRPRR: $string<br>";
    $string =~ s/^\s+//g;
    $string =~ s/\s+$//g;
    return $string;
}
###########################################
# convert time 
###########################################
sub convert_time
{
    my ($time) = @_;
    if ($time  == "0"){return "not available";}
    my ($sec, $min, $hour,$mday,$mon,$year,$wday,$yday,$isdt) = gmtime($time);
    #print "S: $sec, $min, $hour, $mday, $mon, $year, $wday, $yday, $isdt<br>";
    my $date = sprintf("%04d-%02d-%02d %02d:%02d:%02d", $year+1900, $mon+1, $mday, $hour, $min, $sec);
    return $date;
}


#############################################
# convert seconds to days
#############################################
sub convert_seconds_to_days
{    
   
    my ($state_time,$time)  = @_;
    
    my (@st_new, @t_new);
    my @st   = (split / /, $state_time);
    my @st_year_m_d = (split /\-/,$st[0]);
    my @st_hour_etc = (split /\:/,$st[1]);
    #pop @st_hour_etc;
    
    my @t    = (split / /, $time);
    my @t_year_m_d  = (split /\-/,$t[0]);
    my @t_hour_etc  = (split /\:/,$t[1]);
    #pop @t_hour_etc;

    my (@S,@T);
    push @S, @st_year_m_d;
    push @S, @st_hour_etc;
    push @T, @t_year_m_d;
    push @T, @t_hour_etc;
    foreach my $n (@S)
    {
	#$n =~ s/^0*//;
	push @st_new, $n;    
    }
    foreach my $n (@T)
    {
	#$n =~ s/^0*//;
	push @t_new, $n;
    }
    #print "S: @st_new<br>";
    #print "P: @t_new<br>";
    my @difference = Delta_DHMS(@st_new,@t_new);
    return \@difference;
}

##########################################
# gen footer
##########################################
sub gen_footer_basic
{
    print << "    EOF";
    <hr align="left" noshade>
	<table width="100%"  border="0" align="center" cellpadding="4" cellspacing="0">
	<tr align="center" valign="top">   <td width="16%" valign="top"><div align="center" class="style10">
	<a href="http://www.nsf.gov"><img src="http://www.us-vo.org/images/nsf_logo.gif" alt="NSF HOME" width="50" height="50" border="0">
	</a>
	<a href="http://www.nasa.gov"><img src="http://www.us-vo.org/images/nasa_logo_sm.gif" alt="NASA HOME" width="50" height="47" border="0">
	</a>
	</div></td>
	<td width="76%"><div align="center">       
	<p class="style10"> Developed with the support of the <a href="http://www.nsf.gov">National Science Foundation</a> 
	<br>          under Cooperative Agreement AST0122449 with the Johns Hopkins University 
	<br>         The NVO is a member of the <a href="http://www.ivoa.net">International Virtual Observatory Alliance</a></p>
	<p class="style10">This NVO Application is hosted by <a href="#">LocalSiteName</a></p>
	</div></td>    
	<td width="8%"><div align="center"><span class="tiny">Member<br>
	</span>
	<a href="http://www.ivoa.net">
	<img src="http://www.us-vo.org/images/ivoa_small.jpg" alt="IVOA HOME" width="68" height="39" border="0" align="top"></a></div></td>
	<td width="8%"><span class="style4"><span class="tiny">Meet the Developers</span><br>
	<img src="http://www.us-vo.org/images/bee_hammer.gif" alt="MEET THE DEVELOPERS" width="50" border=0></span></td> </tr></table>
	<hr noshade='noshade' />
	<p>Hosted by the <a href='http://universe.gsfc.nasa.gov/'>Astrophysics Science Division</a>and the 
	<a href='http://heasarc.gsfc.nasa.gov/'>High Energy Astrophysics Science Archive Research Center (HEASARC)</a>at
	<a href='http://www.nasa.gov/'>NASA/</a><a href='http://www.gsfc.nasa.gov/'>GSFC</a>
	</p><p>HEASARC Director:
	<a href='http://heasarc.gsfc.nasa.gov/docs/bios/white.html'>Dr. Nicholas E. White</a>,</p>
	<p>HEASARC Associate Director: Dr. Roger Brissenden,</p><p>Responsible NASA Official:
	<a href='http://heasarc.gsfc.nasa.gov/docs/nospam/panewman.html'>Phil Newman</a> </p>
	<p class='tiny'> <a href='/banner.html'>Privacy, Security, Notices</a></p>
	</div><!-- id='footer' --></body>
    EOF
}
1;
