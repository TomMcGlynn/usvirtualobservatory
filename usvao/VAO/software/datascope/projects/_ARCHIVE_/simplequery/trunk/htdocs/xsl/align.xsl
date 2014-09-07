<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" 
    xmlns:vo1="http://vizier.u-strasbg.fr/VOTable"
    xmlns:vo2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
    xmlns:vo3="http://www.ivoa.net/xml/VOTable/v1.0"
    xsl:exclude-result-prefixes="vo v1 v2 v3"
>
<xsl:output method="text" />

<xsl:param name="separator"><xsl:value-of select="string('|')" /></xsl:param>

<!-- Computed variables -->

<xsl:variable name="fieldlist" select="//FIELD|//vo:FIELD|//vo1:FIELD|vo2:FIELD|vo3:FIELD" />

<xsl:template match="/" >
    <xsl:for-each select="$fieldlist">
        <xsl:variable name="p" select="position()" />
        <xsl:for-each select="//TR" >
	    <xsl:sort select="string-length(TD[position()=$p])" order="descending" />
	    <xsl:if test="position()=1">
	        <xsl:value-of select="string-length(TD[position()=$p])" />
		<xsl:value-of select="string('&#xa;')" />
	    </xsl:if>
	</xsl:for-each>
    </xsl:for-each>
</xsl:template>
</xsl:stylesheet>
