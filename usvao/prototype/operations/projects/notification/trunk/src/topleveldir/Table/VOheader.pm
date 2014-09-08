#
#
#
#
#
#
#
package Table::VOheader;

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
    my $hash = { 'VAO Home'                         => 'http://www.usvao.org/',
                 'VO Service Notices'               => '/vo/notification/displaynote.pl',
                 'VAO Monitor'                      => '/vo/vaomonitor/',
                 'VAO Monitor Help'                 => '/vo/vaomonitor/doc/help.html',
                 'VAO Feedback'                     => 'http://www.usvao.org/contact-connect',
                 'VAO Monitor Interactive Testing'  => '/vo/vaomonitor/vaomonitor.pl',
                 'VAO Notification Service'         => '/vo/notification/addnote.html',
	       };
    return $hash;
}
sub printheader
{
    my $self = shift;
    my ($title) = @_;
    print <<"    EOF";
  <HTML>
  <HEAD><TITLE></TITLE>
     <LINK REL="StyleSheet" HREF='http://www.us-vo.org/app_templates/usvo_template.css' TYPE="text/css">
     <LINK REL="StyleSheet" HREF='./css/styles.css' TYPE="text/css">
     <LINK REL="StyleSheet" HREF='./css/volib.css' TYPE="text/css">
     <LINK REL="StyleSheet" HREF='./css/annot.css' TYPE="text/css">
  </HEAD>
  
  <BODY ><a href='#content' title='Skip navigation'> </a>

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
      <img src= "/vo/notification/images/nasa_logo.gif" width=72 height=60 border=0 alt='NASA home'>
      </img></a><br><span>Hosted by:</span><br>HEASARC<br> 
      <br>
      </td></tr></table>
    EOF
}
1;
