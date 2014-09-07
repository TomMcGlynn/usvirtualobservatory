<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" exclude-result-prefixes="vo">
<xsl:output method="html" />

<xsl:variable name="lc" select="'abcdefghijklmnopqrstuvwxyz'" />
<xsl:variable name="uc" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

<!-- Computed variables -->

<xsl:variable name="fieldlist" select="//FIELD|//vo:FIELD"/>

<xsl:variable name="raColumnNums">
    <xsl:call-template name="getColumnByUCDs">
        <xsl:with-param name="value" select="'|POS.EQ.RA;META.MAIN|POS_EQ_RA_MAIN|'"/>
    </xsl:call-template>
</xsl:variable>
<xsl:variable name="raColumnNum">
    <xsl:value-of select="substring-before($raColumnNums, '|')" />
</xsl:variable>


<xsl:variable name="decColumnNums">
    <xsl:call-template name="getColumnByUCDs">
        <xsl:with-param name="value" select="'|POS.EQ.DEC;META.MAIN|POS_EQ_DEC_MAIN|'"/>
    </xsl:call-template>
</xsl:variable>

<xsl:variable name="decColumnNum">
    <xsl:value-of select="substring-before($decColumnNums, '|')" />
</xsl:variable>

<xsl:template name="getColumnByUCDs">
    <xsl:param name="value"/>
    <xsl:for-each select="$fieldlist">
        <xsl:if test="contains($value, concat('|',translate(@ucd,$lc,$uc),'|'))">
            <xsl:value-of select="position()"/><xsl:text>|</xsl:text>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template match="/">
    <xsl:apply-templates />
</xsl:template>

<xsl:template match="TR">
    <xsl:value-of select="TD[position()=$raColumnNum]" /><xsl:text> </xsl:text> <xsl:value-of select="TD[position()=$decColumnNum]" /><xsl:text>&#xa;</xsl:text>
</xsl:template>

<xsl:template match="text()" />

</xsl:stylesheet>
