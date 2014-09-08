<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" exclude-result-prefixes="vo">

<xsl:import href="voview.xsl" />
<xsl:output method="html" />

<xsl:param name="currentTable" />

<xsl:template name="prefix-header">
   <th></th>
</xsl:template>

<xsl:template name="prefix-column">
   <xsl:param name="index" />
   <xsl:param name="format" />
   <xsl:param name="isSelected" />
   <td>
      <a>
         <xsl:attribute name="href"> 
            http://archive.stsci.edu/cgi-bin/mastpreview?mission=<xsl:value-of select="$currentTable" />&amp;dataid=<xsl:value-of select="TD[1]"/>
         </xsl:attribute>
         <xsl:attribute name="title">Link to MAST archive</xsl:attribute>
         <xsl:attribute name="onclick">return nw(this.href,this.target)</xsl:attribute>
         <xsl:attribute name="target">extern</xsl:attribute>
         Data
      </a>
   </td>
</xsl:template>

<xsl:template name="prefix-filter">
   <th>Archive</th>
</xsl:template>

<xsl:template name="start"  match="/">
   <xsl:apply-imports />
</xsl:template>

</xsl:stylesheet>
