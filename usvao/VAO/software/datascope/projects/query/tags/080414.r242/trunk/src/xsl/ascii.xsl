<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" 
    xmlns:v1="http://vizier.u-strasbg.fr/VOTable"
    xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
    xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"
    xsl:exclude-result-prefixes="vo v1 v2 v3"
>
<xsl:output method="text" />

<xsl:param name="separator"><xsl:value-of select="string('|')" /></xsl:param>

<!-- Computed variables -->

<xsl:variable name="fieldlist" select="//FIELD|//vo:FIELD|//v1:FIELD|v2:FIELD|v3:FIELD" />

<xsl:template match="/" >

    <xsl:for-each select="$fieldlist">
    
        <xsl:choose>
            <xsl:when test="@name">
                <xsl:value-of select="@name" /><xsl:value-of select="$separator" />
	    </xsl:when>
	    <xsl:when test="@id">
	        <xsl:value-of select="@id" /><xsl:value-of select="$separator" />
	    </xsl:when>
	    <xsl:otherwise>
	        <xsl:value-of select="position()" />
	    </xsl:otherwise>
        </xsl:choose>
	
    </xsl:for-each>
    <xsl:value-of       select="string('&#xA;')"    />
    <xsl:value-of       select="string('&#xA;')"    />
    
    <xsl:for-each select="//TR|//vo:TR|//v1:TR|//v2:TR|//v3:TR">
        <xsl:for-each select="TD|vo:TD|v1:TD|v2:TD|v3:TD">
            <xsl:value-of select="." /><xsl:value-of select="$separator" />
        </xsl:for-each>
        <xsl:value-of       select="string('&#xA;')"    />
    </xsl:for-each>
</xsl:template>
</xsl:stylesheet>
