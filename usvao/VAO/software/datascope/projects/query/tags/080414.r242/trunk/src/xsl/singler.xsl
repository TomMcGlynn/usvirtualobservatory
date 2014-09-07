<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" xsl:exclude-result-prefixes="vo">

<xsl:output method="xml" />

<xsl:template match="/" >
<xsl:copy-of select="/VOTABLES/VOTABLE_ENTRY/VOTABLE" />
</xsl:template>


<!--
<xsl:template match="/" >
<xsl:apply-templates select="/VOTABLES/VOTABLE_ENTRY/VOTABLE" />
</xsl:template>

<xsl:template match="@*|node()">
        <xsl:copy>
                <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
</xsl:template>

<xsl:template match="//PARAM">
<PARAM>
<xsl:apply-templates select="@*" />
</PARAM>
</xsl:template>
-->

</xsl:stylesheet>
