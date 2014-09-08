<?xml version="1.0"?>
<!-- 
  - Create an HTML view of a single resource.  This wraps around
  -   vobrowse.xsl to add the NVO header and footer.
  -->
<xsl:stylesheet xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="1.0">

   <!-- 
     - load the core templates for rendering a VOResource
     -->
   <xsl:import href="vobrowse.xsl"/> 

   <!-- 
     - add additional templates for specific VOResource types.  
     -
     -->
   <xsl:import href="vobrowse_scs.xsl"/>
   <xsl:import href="vobrowse_sia.xsl"/>
   <xsl:import href="vobrowse_ssa.xsl"/>
   <xsl:import href="vobrowse_tap.xsl"/>
   <xsl:import href="vobrowse_reg.xsl"/>

  <!--
     -  
   <xsl:import href="vobrowse_sn.xsl"/>
     -->

   <xsl:output method="html"/>

   <xsl:template match="/">
     <xsl:for-each select="." xml:space="preserve">
<html> 
<head>
<title><xsl:value-of select="//ri:Resource/shortName|//resource/shortName"/>: Resource Record Summary</title>
<!--<link href="http://www.us-vo.org/usvo3.css" type="text/css" rel="stylesheet" /> -->
<link href="js/usvo_template.css" type="text/css" rel="stylesheet" />
<xsl:call-template name="doccss"/>
<xsl:call-template name="docjs"/>
</head>
<body>
 
<table width="100%" border="0" cellpadding="0" cellspacing="0">
<tr>
    <td width="112" height="32" align="center" valign="top"><a
href="http://www.usvao.org" class="nvolink" target="_top"><img
        src="http://www.usvao.org/images/VAO_logo_100.png" border="0"/></a><span class="nvolink"><a
href="http://www.usvao.org/" target="_top">Virtual Astronomical Observatory</a></span></td>
      <td width="50" align="center" valign="middle"><img src="images/directory50.png" alt="ICON" width="50" height="50"/></td>
    <td valign="top"><table  width="100%" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td width="2" height="30" bgcolor="white"></td>
<td width="678" height="39" align="center"
                      valign="middle"
                      background="images/stars.jpg"
                      bgcolor="#CFE5FC"  class="nvoapptitle" style="background-repeat: repeat-y;"><span class="nvoapptitle" style="background-repeat: repeat-y;">VAO Directory</span></td>
        <td bgcolor="white" width="2"></td>
      </tr>
      <tr>
        <td bgcolor="white" width="2"></td>
        <td bgcolor="white" width="2"></td>
      </tr>
      <tr>
        <td align="center" valign="top" colspan="3"><table cellspacing="2" cellpadding="0" border="0" width="100%"
                style="margin: 0pt;">
          <tr>
             <!-- the local links -->
        <td class="navlink"><a href="http://www.usvao.org/">VAO Home</a></td>
        <td class="navlink"><a href="keywordsearch.aspx">Search</a></td>
        <td class="navlink"><a href="xpublish.aspx">Publish</a></td>
        <td class="navlink"><a href="riws.aspx">Developers</a></td>
        <td class="navlink"><a href="helpnew.aspx">Help</a></td>
        <td class="helplink"><a href="http://www.us-vo.org/feedback/">Contact Us</a></td>
           </tr>
         </table>
         </td>
       </tr>
       <tr>
          <td width="2" bgcolor="white"/>
          <td align="right">
             <span style="visibility: hidden; font-size: 13pt;">X</span>
             <em>Tip: <a href="javascript:" title="click to pop-up answer"
                   onclick="return false;"
                   id="whatsResource">What's a "Resource"?</a></em>
          </td>
          <td width="2" bgcolor="white"/>
       </tr>
    </table>
    </td>
    <td width="140" align="center" valign="top">
      <!-- local logo and link -->
      <span class="tiny">Hosted By</span><br/><a href="http://www.stsci.edu"><img height="54"
         src="images/hst.gif"
         alt="STScI Home" border="0"/></a>
      <br />
      <span class="nvolink"><span class="tiny"><a
            href="http://www.stsci.edu">Space Telescope<br/> Science 
      Institute</a> </span></span>
    </td>
   </tr>

</table>
<p />

<xsl:comment> =======================================================================
  -  Page Content 
  -  ======================================================================= </xsl:comment>

<fieldset>
   <legend style="font-size: 10pt;">Resource Record Summary</legend>
      <xsl:apply-templates select="//ri:Resource|//resource" />
</fieldset>

<xsl:comment> =======================================================================
  -  End Page Content
  -  ======================================================================= </xsl:comment>

<br />
<hr align="left" noshade=""/>
    <table width="100%"  border="0" align="center" cellpadding="4" cellspacing="0">
  <tr align="center" valign="top">
    
    <td width="16%" valign="top"><div align="center" class="style10"><a href="http://www.nsf.gov"><img src="http://www.us-vo.org/images/nsf_logo.gif" alt="NSF HOME" width="50" height="50" border="0"/></a><a href="http://www.nasa.gov"><img src="http://www.us-vo.org/images/nasa_logo_sm.gif" alt="NASA HOME" width="50" height="47" border="0"/></a></div></td>
    <td width="76%"><div align="center">
        <p class="style10"> Developed with the support of the <a href="http://www.nsf.gov">National Science Foundation</a> <br/>

          under Cooperative Agreement AST0122449 with the Johns Hopkins University <br/>
          The VAO is a member of the <a href="http://www.ivoa.net">International Virtual Observatory Alliance</a></p>
        <p class="style10">This VAO Application is hosted by the <a href="http://www.stsci.edu">Space Telescope Science Institute</a></p>
    </div></td>
    <td width="8%"><div align="center"><span class="tiny">Member<br/>
    </span><a href="http://www.ivoa.net"><img src="images/ivoa_small.jpg" alt="ivoa logo" width="68" height="39" border="0" align="top"/></a></div></td>

    <td width="8%"><span class="nvolink"><span class="tiny"><a href="contactus.aspx">Meet the Developers</a></span><br/>
    <img src="http://www.us-vo.org/images/bee_hammer.gif" alt="MEET THE DEVELOPERS" width="50" border="0"/></span></td>
  </tr>
</table>
</body> </html>
      </xsl:for-each>
   </xsl:template>
</xsl:stylesheet>
