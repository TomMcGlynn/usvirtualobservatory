<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0"
                xmlns:rbx="http://nvo.ncsa.uiuc.edu/xml/VORegInABox/v1.0"
                version="1.0">

   <xsl:output method="html" encoding="UTF-8" />

   <xsl:include href="Resource_Form_Site.xsl"/>

   <xsl:template match="/">
     <xsl:apply-templates select="." mode="site">
        <xsl:with-param name="title">Resource List</xsl:with-param>
     </xsl:apply-templates>
   </xsl:template>

   <xsl:template match="/" mode="appbody">
      <script type="text/javascript" src="/vopub/Resource_Form.js" ></script>
      <xsl:apply-templates select="ri:VOResources" />
   </xsl:template>

   <xsl:template match="ri:VOResources">
      <xsl:variable name="toppub">
         <xsl:choose>
            <xsl:when test="/*/@rbx:publisher">
               <xsl:value-of select="/*/@rbx:publisher"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="/*/@rbx:user"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
<h1>Resource List for <xsl:value-of select="$toppub"/></h1>

<xsl:if test="@rbx:tryout"><h2><font color="red"><em>***Test Registry - 
Data Will Not Be Published***</em></font></h2></xsl:if>
<p>
This page lets you register, view, and update your resources.  As a
way to minimize the amount you have to type in, you can add new
resources by <em>inheriting</em> the values from an existing one;
thus, you only update those items that are different.  
</p>

<p>
Resources that are added, edited, or deleted will not be made available
to the VO via the harvesting interface until the "Publish Resources" 
button is hit.
</p>

<form method="post" action="/cgi-bin/sso/vaologin.cgi/Resource_Form.cgi" enctype="multipart/form-data" >
        <table border="0" width="100%" cellpadding="1" cellspacing="8">
		<tr align="left">
		<th><font size="4"></font></th>
		<th><font size="4">Status</font></th>

		<th><font size="4">Resource</font></th>
		<th><font size="4">Resource Type</font></th></tr>

       <xsl:apply-templates select="ri:Resource" />      

		</table>
		<p />

<input type="hidden" name="uname" value="rplante" />
<xsl:if test="@rbx:tryout!=''">
     <input type="hidden" name="tryout" value="{@rbx:tryout}"/>
</xsl:if>
<center>
<table border="1" cellspacing="2" cellpadding="4" width="100%" >
  <tr>
    <td rowspan="3" valign="top" width="25%" bgcolor="#f9ebc9" >
      To <strong>add</strong> a new Resource...
    </td>
    <td align="left" bgcolor="#6ba5d7">
      1. Select one of the existing resources above to inherit values from, 
    </td>

  </tr>
  <tr>
    <td align="left" bgcolor="#6bc5d7">
      2. Choose the type of new resource being added: <span style="visibility: hidden">XX</span>
      <select name="defset">
	<option value="Organisation">Organization or Project</option>
	<option value="DataCollection">Data Collection</option>
	<option value="Authority">Naming Authority ID</option>

	<option value="BrowserBasedService">Service accessible from a Web Page</option>
	<option value="CGIService">CGI/URL-based Web Service</option>
	<option value="ConeSearch">Simple Cone Search Service</option>
	<option value="SIAService">Simple Image Access Service</option>
	<option value="SSAService">Simple Spectral Access Service</option>
	<option value="SkyNode">Sky Node Service</option>
	<option value="WebService">SOAP-based Web Service</option>
	<option value="Resource">Generic Resource</option>
      </select>
    <br/><span style="visibility: hidden">XXXXXXXX</span>
    Click <a href="/vopub/resourcedesc.html" target="nvohelp">here</a> for a description of the resources to help you decide.
    </td>
  </tr>

  <tr>
    <td bgcolor="#f5b21d">
      3. Click: <span style="visibility: hidden">XX</span> <input type="submit" name="ftype" value=" Create Resource" />
    </td>
  </tr>
</table> <p />

<table border="1" cellspacing="2" cellpadding="4" width="100%" >
  <tr>

    <td rowspan="2" valign="top" width="25%" bgcolor="#f9ebc9">
      To <strong>edit</strong> (or view) a Resource...
    </td>
    <td align="left" bgcolor="#6ba5d7">
      1. Select one of the existing resources above, 
    </td>
  </tr>
  <tr>

    <td align="left" bgcolor="#f5b21d">
      2. Click: <span style="visibility: hidden">XX</span> <input type="submit" name="ftype" value=" Edit Resource" onclick="return ensureSelection()"/>
    </td>
  </tr>
</table> <p />

<table border="1" cellspacing="2" cellpadding="4" width="100%" >
  <tr>
    <td rowspan="2" valign="top" width="25%" bgcolor="#f9ebc9">

      To <strong>delete</strong> a Resource (or remove an uncommitted change)...
    </td>
    <td align="left" bgcolor="#6ba5d7">
      1. Select one of the existing resources above, 
    </td>
  </tr>
 <tr>
    <td bgcolor="#f5b21d">

      2. Click: <span style="visibility: hidden">XX</span> <input type="submit" name="ftype" value="Delete Resource" onclick="return confirmDelete()"/> 
      <span style="visibility: hidden">XXXXX</span> <em>Confirmation will be requested.</em>
    </td>
  </tr>
</table>
<p />

<table border="1" cellspacing="2" cellpadding="4" width="100%" >
  <tr>

    <td rowspan="2" valign="top" width="25%" bgcolor="#f9ebc9">
      To <strong>undelete</strong> a deleted Resource...
    </td>
    <td align="left" bgcolor="#6ba5d7">
      1. Select one of the deleted resources above, 
    </td>
  </tr>
  <tr>

    <td bgcolor="#f5b21d">
      2. Click: <span style="visibility: hidden">XX</span> <input type="submit" name="ftype" value="Undelete Resource" onclick="return ensureSelection()"/> 
    </td>
  </tr>
</table>
<p />
<table border="1" cellspacing="2" cellpadding="4" width="100%" >
  <tr>
    <td rowspan="1" valign="top" width="25%" bgcolor="#f9ebc9">
      To <strong>commit</strong> all uncommitted Resources...
    </td>

    <td align="left" bgcolor="#f5b21d">
      Click: <span style="visibility: hidden">XX</span> <input type="submit" name="ftype" value="Publish Resources" /> 
    </td>
  </tr>
</table>
<p />
</center>

<p>
<strong>Want to see the XML?</strong>
<ul>
  <li> To see all resources (including the uncommitted ones), use 
       your browser's "View Source" feature (e.g. try selecting the
       "View-&gt;View Source" menu item) on this page. </li>
  <li> To see an the XML for an individual record, select the record, 
       click on "Edit Resource", and the use your browser's "View 
       Source" feature.  </li>
</ul>
</p>
</form>

   </xsl:template>

   <xsl:template match="ri:Resource">
      <xsl:variable name="statcolor">
         <xsl:choose>
            <xsl:when test="not(@rbx:pub-status) or @rbx:pub-status=''">
               <xsl:text>black</xsl:text>
            </xsl:when>
            <xsl:when test="@rbx:pub-status='published' or 
                            @rbx:pub-status='deleted'">green</xsl:when>
            <xsl:otherwise>red</xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      <xsl:for-each select="." xml:space="preserve">
      <tr>
         <td valign='top'>
            <input type="radio" name="resource" value="{@rbx:src}" 
                   rname="{title} ({shortName})" 
                   rid="{identifier}" rtype="{@rbx:user-type}"/>
         </td>
         <td valign='top'><font color="{$statcolor}"><xsl:value-of select="@rbx:pub-status"/></font><span style="visibility: hidden">XX</span></td>
         <td valign='top'><xsl:value-of select="title"/> (<xsl:value-of select="shortName"/>)<br />
   <font color='#6ba5d7'><strong><em><xsl:value-of select="identifier"/></em></strong></font>
</td><td valign='top'><xsl:value-of select="@rbx:user-type"/></td>
      </tr>
      </xsl:for-each>
   </xsl:template>

</xsl:stylesheet>
