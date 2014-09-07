<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" xsl:exclude-result-prefixes="vo">
<xsl:import href="http://heasarcdev.gsfc.nasa.gov/vo/datascope/xsl/voview.xsl" />
<xsl:output method="html" />

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
	      http://simbad.u-strasbg.fr/sim-id.pl?protcol=html&amp;Frame3=G&amp;Ident=<xsl:value-of select="translate(TD[3],' ', '+')"/>
	   </xsl:attribute>
	   <xsl:attribute name="title">Link to SIMBAD details</xsl:attribute>
	   <xsl:attribute name="onclick">return nw(this.href,this.target</xsl:attribute>
	   <xsl:attribute name="target">extern</xsl:attribute>
           Data
       </a>
    </td>
</xsl:template>
<xsl:template name="prefix-filter">
    <th>Objects</th>
</xsl:template>

<xsl:template name="start"  match="/">
    <xsl:apply-imports />
</xsl:template>


</xsl:stylesheet>
