#
#
#
#
#
#
#
package HTML::VOheader;

use strict;

sub new
{
    my ($class) = shift;
    my $hash   = {};
    bless $hash, $class;
    $hash->init(@_);
    
    return $hash;
}
sub init
{
    my $self = shift;
    $self->{title}  = $_[0];
    $self->{links}  = $_[1];
    $self->{urls}   = load_links();
    


}
sub get_title
{
    my $self = shift;
    return $self->{title};    
}
sub load_links
{
    my $hash = { 
	         'VAO Home'                         => 'http://www.usvao.org/',
	         'VO Service Notices'               => '/vo/notification/shownote.pl',
		 'VAO Monitor'                      => '/vo/vaomonitor/',
		 'VAO Monitor Help'                 => '/vo/vaomonitor/doc/help.html',
		 'VAO Feedback'                     => 'http://www.usvao.org/contact.php',
		 'VAO Monitor Interactive Testing'  => '/vo/vaomonitor/vaomonitor.pl',
	       };
    return $hash;
}
sub printheader
{
    my $self = shift;
    
    print <<"    EOF";
  <HTML>
  <HEAD><TITLE>$self->{title}</TITLE>
     <LINK REL="StyleSheet" HREF='http://www.us-vo.org/app_templates/usvo_template.css' TYPE="text/css">
     <LINK REL="StyleSheet" HREF='./css/styles.css' TYPE="text/css">
     <LINK REL="StyleSheet" HREF='./css/volib.css' TYPE="text/css">
     <LINK REL="StyleSheet" HREF='./css/annot.css' TYPE="text/css">
     <LINK REL="StyleSheet" HREF='./css/vao.css' TYPE="text/css">
     <script language = 'JavaScript' type = 'text/javascript' src="./js/voutil.js"></script>
    <script language = 'JavaScript' type = 'text/javascript'>
     getTestURL = function (n,s,im) 
     {
            
        var p  = document.getElementById(s);	    
        //var q = p.s.options[s.selectedIndex].value; 
        var q = p.options[p.selectedIndex].value;
        var h = document.getElementById(im);
        h.style.visibility = "visible";
        //document.n.s.options[document.n.selectedIndex].value;
        //location.href =  q;
        wrapTestURLResponse(q,im);

	//document.n.submit(); 
     }
    getRunURL = function (n,s) 
     {
            
        var p  = document.getElementById(s);	    
        //var q = p.s.options[s.selectedIndex].value; 
        var q = p.options[p.selectedIndex].value;
        //document.n.s.options[document.n.selectedIndex].value;
        location.href =  q + '&getresponse=yes';
        //document.n.submit(); 
   }
    </script>


 </HEAD>
    EOF

    $self->print_banner();
}
sub print_banner
{
    my $self = shift;
    print << "    EOF";
    <BODY ><a href='#content' title='Skip navigation'> </a>

  <table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>   
     <td width="112" height="32" align="center" valign="top">
        <a href="http://www.usvao.org" class="nvolink" target="_top">
          <img src="http://www.usvao.org/documents/Templates/Logos/VAO_logo_100.png" alt="NVO HOME" border="0"/>
        </a>
        <span class="nvolink">
          <a href="http://www.usvao.org/" target="_top">Virtual Astronomical Observatory</a></span>
     </td>   
     <td valign="top"><table  width="100%" border="0" cellpadding="0" cellspacing="0">      
       <tr>       
         <td width="2" height="30" bgcolor="white"></td>
         <td width="678" height="39" align="center" valign="middle" 
         bgcolor="#CFE5FC"  class="nvoapptitle" style="background-repeat: repeat-y;">
    EOF

    print "<span class= \"nvoapptitle\" style= \"background\-repeat: repeat\-y;\">$self->{title}<\/span></td></tr>\n";
    print "</td>\n";
    
    print <<"     EOF";
    <td bgcolor="white" width="2"></td>     
    </tr>     
    <tr>       
     <td bgcolor="white" width="2"></td>      
     <td bgcolor="white" width="2"></td>      
     </tr>      
     <tr>       
     <td align="center" valign="top" colspan="3"><table cellspacing="2" cellpadding="0" border="0" width="100%"  style="margin: 0pt;">         
     <tr>
     
     <!-- the local links -->     
     EOF
     

     foreach my $n (@{$self->{links}})
     {
	 
	 print "<td  class='navlink'><a href='$self->{urls}->{$n}'>$n</a></td>";
	 
     }

    print << "    EOF";
     </tr>       
     </table></td>
      </tr>
      </table></td>
      <td width = 140 align = center valign = top><a href = "http://www.heasarc.gsfc.nasa.gov">
      <img src=/vo/monitor//images/nasa_logo.gif width=72 height=60 border=0 alt='NASA home'>
      </img></a><br><span>Hosted by:</span><br>HEASARC<br> 
      <br>
      </td></tr></table>
    EOF
}
1;
