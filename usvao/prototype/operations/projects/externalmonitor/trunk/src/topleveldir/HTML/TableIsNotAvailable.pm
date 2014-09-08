#
#
#
#
#
package HTML::TableIsNotAvailable;


sub new
{
    my ($class) = shift;
    my $hash = {};
    bless $hash,$class;
    $hash->init(@_);
    return $hash;
}
sub init
{
    my ($self) =shift;
    my $url    = shift @_;
    my $mimetype = shift @_; 
    $self->{url}= $url;
    $self->{mimetype} = $mimetype;
    return $self;
}
sub display
{
    my ($self) = shift;
     
    #top
    
    add_top();
  
    
    #body
    print "<table  class = 'tac' align = center border =1 width =300>";
    print "<tr class = titleblue><td>Status</td></tr><br>";
    print "<tr><td class = critmessage>Fail</td></tr><br>";
    print "</table>"; 

    #footer
    add_footer();

}
sub add_top
{ 
    print  STDOUT <<   "    EOF";
   
    <html> 
    <head><LINK REL="StyleSheet" HREF= './css/vao.css' TYPE="text/css">
    <LINK REL= "StyleSheet" HREF='http://www.us-vo.org/app_templates/usvo_template.css' TYPE="text/css">
    <script language = 'JavaScript' type = 'text/javascript' src="./js/service.js"></script>
    <script type = "text/javascript">
    function run() { addheader();}
    window.onload =run;</script>
    </head>
    <div id = child></div>
    <body><table class = 'tac' align = center width = 390>
    EOF
    




}
sub add_footer
{
    print << "    EOF"
    <br><br>
    <hr align = "left" noshade>
    <table width="100%"  border="0" align="center" cellpadding="4" cellspacing="0">
    <tr align="center" valign="top">   
     <td width="16%" valign="top">
        <div align="center" class="style10">
        <a href="http://www.nsf.gov">
         <img src="http://www.us-vo.org/images/nsf_logo.gif" alt="NSF HOME" width="50" height="50" border="0">
        </a>
        <a href="http://www.nasa.gov">
          <img src="http://www.us-vo.org/images/nasa_logo_sm.gif" alt="NASA HOME" width="50" height="47" border="0">
         </a>
        </div>
      </td>
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
        </div><!-- id='footer' -->
    EOF
}
1;

