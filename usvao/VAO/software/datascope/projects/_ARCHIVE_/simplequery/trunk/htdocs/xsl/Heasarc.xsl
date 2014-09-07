<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" xsl:exclude-result-prefixes="vo">
<xsl:import href="http://heasarcdev.gsfc.nasa.gov/vo/datascope/xsl/voview.xsl" />
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
	      http://heasarc.gsfc.nasa.gov/db-perl/W3Browse/w3hdprods.pl?files=Preview&amp;Target=heasarc_<xsl:value-of select="$currentTable" />|||_unique_id=<xsl:value-of select="TD[1]"/>||
	   </xsl:attribute>
	   <xsl:attribute name="title">Link to HEASARC archive</xsl:attribute>
	   <xsl:attribute name="onclick">return nw(this.href,this.target</xsl:attribute>
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
