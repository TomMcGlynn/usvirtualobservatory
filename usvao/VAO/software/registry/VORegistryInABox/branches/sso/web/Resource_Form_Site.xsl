<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0"
                xmlns:rbx="http://nvo.ncsa.uiuc.edu/xml/VORegInABox/v1.0"
                version="1.0">

   <xsl:template match="/" mode="site">
      <xsl:param name="title">Publishing Registry Form</xsl:param>

<html xmlns="http://www.w3.org/1999/xhtml" lang="en-US" xml:lang="en-US">
<head>
<title><xsl:value-of select="$title"/></title>
<link type="text/css" rel="stylesheet" href="/vopub/usvo_template.css" />
<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
</head>

<body bgcolor="white" >
<table width="100%" border="0" cellpadding="0" cellspacing="0">
  <tr>
    <td valign="top">
      <table  width="100%" border="0" cellpadding="0" cellspacing="0">
        <tr>
          <td bgcolor="white" width="2"></td>
          <td bgcolor="#CFE5FC" valign="middle" align="center" height="32"
              class="nvoapptitle">

       <!-- Name of Application -->
       Publishing Registry Portal

          </td>
          <td bgcolor="white" width="2"></td>
       </tr>
       <tr>
          <td bgcolor="white" width="2"></td>
          <td height="10" valign="top"
              background="http://www.us-vo.org/app_templates/stars.jpg" >
          </td>

          <td bgcolor="white" width="2"></td>
       </tr>
       <tr>
         <td align="center" valign="top" colspan="3">
         <table cellspacing="2" cellpadding="0" border="0" width="100%"
                style="margin: 0pt;">
           <tr>
             <!-- the local links -->
             <td class="navlink"><a href="/vopub/welcome.html">Re-enter Publishing Portal</a></td>

             <td class="navlink"><a href="http://www.ivoa.net/doc/latest/ConeSearch.html">How to Publish</a></td>
             <td class="navlink"><a href="http://nvo.stsci.edu/voregistry/index.aspx">Search the STScI Registry</a></td>
             <td class="navlink"><a href="http://nvo.caltech.edu:8080/carnivore">Search the Carnivore Registry</a></td>
             <td class="navlink"><a href="http://rofr.ivoa.net/">RofR</a></td>
           </tr>

         </table>
         </td>
       </tr>
    </table>
    </td>
   </tr>
</table>

<xsl:apply-templates select="." mode="appbody"/>        

</body>

</html>   
   </xsl:template>

</xsl:stylesheet>

