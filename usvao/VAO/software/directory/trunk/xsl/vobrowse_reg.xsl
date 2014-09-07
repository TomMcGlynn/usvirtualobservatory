<?xml version="1.0"?>
<!--
  -  templates for browsing Registry resources
  -->
<xsl:stylesheet xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="1.0">

   <xsl:variable name="regsearchpage">http://nvo.stsci.edu/voregistry/search.aspx</xsl:variable>

   <!--
     -  display a "digest" summary of the resource via a selection of the 
     -  metadata that is considered most important to see at the top of the 
     -  page.  This does not include the resource description.  This  
     -  overrides the generic version to add Registry-specific metadata.
     -->
   <xsl:template match="ri:Resource[contains(@xsi:type,':Registry')] |
                        resource[contains(@xsi:type,':Registry')]"
                 mode="digest" xml:space="default">

      <xsl:apply-templates select="." mode="digestCore"/><xsl:text>
</xsl:text>

      <dl style="margin-top: 0pt;"><xsl:text>
   </xsl:text>
      <xsl:choose>
         <xsl:when test="(full='true' or boolean(full)) and full!='false'">
            <dt> <strong>This is a "full" registry: </strong> </dt><xsl:text>
   </xsl:text>
            <dd> 
            <xsl:text>it collects metadata for all resource known </xsl:text>
            <xsl:text>to the VO.</xsl:text>
            </dd> 
         </xsl:when>
         <xsl:otherwise>
            <dt> <strong>This is a "local" registry: </strong> </dt> 
            <dd> 
            <xsl:text>it contains metadata for only a a specialized </xsl:text>
            <xsl:text>subset of resources known to the VO.</xsl:text>
            </dd> 
         </xsl:otherwise>
      </xsl:choose>
      <xsl:text>
</xsl:text>
      </dl><xsl:text>
</xsl:text>

   </xsl:template>

   <!--
     -  Display detailed renderings of the extended registry metadata for the 
     -  section "More About this Resource".  
     -->
   <xsl:template match="ri:Resource[contains(@xsi:type,':Registry')] |
                        resource[contains(@xsi:type,':Registry')]" 
                        mode="moreAboutExtended">
      <xsl:if test="managedAuthority">
<table class="more" border="0" cellpadding="0" cellspacing="0" width="100%"><tr>
<td class="morebut"></td>
<td class="moretitle">Authority IDs Managed by this Registry</td></tr>
<tr><td></td><td>
<p class="morehelp">This section lists the Authority identifiers that originate from this registry.</p>
<div class="moresect">
<ul><xsl:for-each select="managedAuthority">
   <li> <a href="{$getRecordSrvc}ivo://{.}"><xsl:value-of select="."/></a> </li>
</xsl:for-each></ul>
</div>
</td></tr></table>
      </xsl:if>
   </xsl:template>

   <!--
     -  summarize a Registry Search capability
     -->
   <xsl:template match="capability[@standardID='ivo://ivoa.net/std/Registry' and
                                   (@xsi:type='Search' or 
                                    contains(@xsi:type,':Search'))]">

<table class="more" border="0" cellpadding="0" cellspacing="0" width="100%"><tr>
<td class="morebut"></td>
<td class="moretitle">Standard IVOA Registry Search Interface
</td></tr>
<tr><td></td><td>
<p class="morehelp">
This interface allows other tools and applications search the contents of this registry.  
</p>
<div class="moresect">
<xsl:call-template name="showValidationLevel" /><br/>
<strong>Available endpoints for the standard SOAP Web Service interface:</strong>

<ul><xsl:for-each select="interface[@role='std']">
   <li> 
     <xsl:for-each select="accessURL[1]">
       <i><xsl:value-of select="."/></i>
       <span style="visibility: hidden">X</span>
       <xsl:choose>
          <xsl:when test="wsdlURL">
             <xsl:text> (</xsl:text>
             <a href="{normalize-space(../wsdlURL[1])}">WSDL</a>
             <xsl:text>)</xsl:text>
          </xsl:when>
          <xsl:otherwise>
             <xsl:text> (</xsl:text>
             <a href="{normalize-space(.)}?wsdl">WSDL</a>
             <xsl:text>)</xsl:text>
          </xsl:otherwise>
       </xsl:choose>
       <xsl:choose>
          <xsl:when test="../@version">
             <span style="visibility: hidden">X</span>
             <xsl:text>(version </xsl:text>
             <xsl:value-of select="../@version"/>
             <xsl:text> compatible)</xsl:text>
          </xsl:when>
          <xsl:when test="../../interface[@role='std']/@version">
             <span style="visibility: hidden">X</span>
             <xsl:text>(assuming version 1.0 compatible)</xsl:text>
          </xsl:when>
       </xsl:choose>
     </xsl:for-each>
     <xsl:for-each select="accessURL[position()>1]">
       <br /><xsl:text>Alternate: </xsl:text><i><xsl:value-of select="."/></i>
     </xsl:for-each>
   </li> 
</xsl:for-each></ul>

      
<xsl:if test="description">
<strong>Description:</strong>
<blockquote class="vordesc"><xsl:value-of select="description"/></blockquote>
</xsl:if>

<xsl:if test="interface[@role!='std']">
<strong>Additional non-standard search interfaces:</strong>
<ul>
   <xsl:apply-templates select="interface[@role!='std']" />
</ul>
</xsl:if>
<br />

</div>
</td></tr></table>

   </xsl:template>

   <!--
     -  summarize a Registry Harvest capability
     -->
   <xsl:template match="capability[@standardID='ivo://ivoa.net/std/Registry' and
                                   (@xsi:type='Harvest' or 
                                    contains(@xsi:type,':Harvest'))]">

<table class="more" border="0" cellpadding="0" cellspacing="0" width="100%"><tr>
<td class="morebut"></td>
<td class="moretitle">Standard IVOA Registry Harvesting Interface
</td></tr>
<tr><td></td><td>
<p class="morehelp">
This interface allows other registries retrieve the lastest resource descriptions published in this registry.  It uses the 
<a href="http://www.openarchives.org">Open Archives Initiative</a> standard for 
<a href="http://www.openarchives.org/OAI/openarchivesprotocol.html">Protocol for 
Metadata Harvesting</a> (OAI-PMH).  
</p>
<div class="moresect">
<xsl:call-template name="showValidationLevel" /><br/>

<xsl:if test="description">
<strong>Description:</strong>
<blockquote class="vordesc"><xsl:value-of select="description"/></blockquote>
</xsl:if>

<strong>Available endpoints for the standard URL-based (OAI-PMH) interface:</strong>

<ul><xsl:for-each select="interface[@role='std' and contains(@xsi:type,'OAIHTTP')]">
   <li> 
     <xsl:for-each select="accessURL[1]">
       <i><xsl:value-of select="."/></i>
       <xsl:choose>
          <xsl:when test="../@version">
             <span style="visibility: hidden">X</span>
             <xsl:text>(version </xsl:text>
             <xsl:value-of select="../@version"/>
             <xsl:text> compatible)</xsl:text>
          </xsl:when>
          <xsl:when test="../../interface[@role='std']/@version">
             <span style="visibility: hidden">X</span>
             <xsl:text>(assuming version 1.0 compatible)</xsl:text>
          </xsl:when>
       </xsl:choose>
     </xsl:for-each>
     <xsl:for-each select="accessURL[position()>1]">
       <br /><xsl:text>Alternate: </xsl:text><i><xsl:value-of select="."/></i>
     </xsl:for-each>
   </li> 
</xsl:for-each></ul>

<xsl:if test="interface[@role='std' and contains(@xsi:type,'OAISOAP')]">
<strong>Available endpoints for the standard SOAP Web Service interface:</strong>

<ul><xsl:for-each select="interface[@role='std' and contains(@xsi:type,'OAISOAP')]">
   <li> 
     <xsl:for-each select="accessURL[1]">
       <i><xsl:value-of select="."/></i>
       <span style="visibility: hidden">X</span>
       <xsl:choose>
          <xsl:when test="wsdlURL">
             <xsl:text> (</xsl:text>
             <a href="{normalize-space(../wsdlURL[1])}">WSDL</a>
             <xsl:text>)</xsl:text>
          </xsl:when>
          <xsl:otherwise>
             <xsl:text> (</xsl:text>
             <a href="{normalize-space(.)}?wsdl">WSDL</a>
             <xsl:text>)</xsl:text>
          </xsl:otherwise>
       </xsl:choose>
       <xsl:choose>
          <xsl:when test="../@version">
             <span style="visibility: hidden">X</span>
             <xsl:text>(version </xsl:text>
             <xsl:value-of select="../@version"/>
             <xsl:text> compatible)</xsl:text>
          </xsl:when>
          <xsl:when test="../../interface[@role='std']/@version">
             <span style="visibility: hidden">X</span>
             <xsl:text>(assuming version 1.0 compatible)</xsl:text>
          </xsl:when>
       </xsl:choose>
     </xsl:for-each>
     <xsl:for-each select="accessURL[position()>1]">
       <br /><xsl:text>Alternate: </xsl:text><i><xsl:value-of select="."/></i>
     </xsl:for-each>
   </li> 
</xsl:for-each></ul>
</xsl:if>
      
<xsl:if test="interface[@role!='std']">
<strong>Additional non-standard harvest interfaces:</strong>
<ul>
   <xsl:apply-templates select="interface[@role!='std']" />
</ul>
</xsl:if>
<br />

</div>
</td></tr></table>

   </xsl:template>

</xsl:stylesheet>
