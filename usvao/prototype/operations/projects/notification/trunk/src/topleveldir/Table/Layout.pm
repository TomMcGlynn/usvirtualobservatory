#
#
#
#

package Table::Layout;

use Exporter();

@ISA = qw(Exporter);

@EXPORT = qw(gen_header_layout build_tabletags gen_footer_bas);


######################
sub gen_header_layout
{
    my ($title,$array)  = @_;
    my  $hash = get_layout_links();
    my  $arraynames   =  build_tabletags($array,$hash);  
   
    print <<"    EOF";
   <HTML>
  <HEAD><TITLE>$title</TITLE>
     <LINK REL="StyleSheet" HREF='http://www.us-vo.org/app_templates/usvo_template.css' TYPE="text/css">  
     <LINK REL = "StyleSheet" HREF ="$::cssA" TYPE = "text/css">
    </HEAD>
   <BODY><a href='#content' title='Skip navigation'> </a>
  <table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>   
     <td width="112" height="32" align="center" valign="top">
        <a href="http://www.us-vo.org" class="nvolink" target="_top">
        <img src="http://www.usvao.org/documents/Templates/Logos/VAO_logo_100.png" alt="NVO HOME" border="0"/>
	 </a>
        <span class="nvolink">
          <a href="http://www.us-vo.org/" target="_top">Virtual Astronomical Observatory</a></span>
     </td>   
     <td valign="top"><table  width="100%" border="0" cellpadding="0" cellspacing="0">      
       <tr>       
         <td width="2" height="30" bgcolor="white"></td>
         <td width="678" height="39" align="center" valign="middle" 
         background="http://www.us-vo.org/images/templatestars.jpg" bgcolor="#CFE5FC"  class="nvoapptitle" style="background-repeat: repeat-y;">
    EOF

    print "<span class= \"nvoapptitle\" style= \"background\-repeat: repeat\-y;\">$title<\/span></td></tr>\n";
    print "</td>\n";  
    
    print <<"    EOF";
    <td bgcolor="white" width="2"></td>      
    </tr>     
    <tr>       
     <td bgcolor="white" width="2"></td>      
     <td bgcolor="white" width="2"></td>      
     </tr>      
     <tr>       
     <td align="center" valign="top" colspan="3"><table cellspacing="2" cellpadding="0" border="0" width="100%"  style="margin: 0pt;">
    EOF
     
    
     #<!-- the local links -->
    print "<tr>";
    foreach my $n (@$arraynames)
    {	
       print "<td>$n</td>"; 
    }
    print "</tr>";
    
    print << "    EOF";
    </table></td>
	</tr>
	</table></td>
      <td width = 140 align = center valign = top><a href = "http://www.heasarc.gsfc.nasa.gov">
      <img src= /vo/notification/images/nasa_logo.gif width=72 height=60 border=0 alt='NASA home'>
      </img></a><br><span>Hosted by:</span><br>HEASARC<br> 
      <br>
      </td></tr></table>
    EOF
}
#######################
sub get_layout_links
{        
    my %hash = ( 'Monitor'        => '../vaomonitor_test/vaodb.pl',
		 'notices'        => 'displaynote.pl',
		 'NVO Home'       => 'http://us-vo.org/',
		 'NVO Feedback'   => 'http://us-vo.org/feedback/index.cfm',
		 );
    return \%hash;
}
######################
sub build_tabletags
{
    my ($array,$hash) = @_;
    for (my $i = 0; $i<scalar(@$array);$i++)
    {
        my $url = $hash->{$$array[$i]};
	$$array[$i]  =  "<td class = 'navlink'><a href = '$url'>$$array[$i]</td>";	
    }
    return $array;
}
##########################################
# gen footer
##########################################
sub gen_footer_bas
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
