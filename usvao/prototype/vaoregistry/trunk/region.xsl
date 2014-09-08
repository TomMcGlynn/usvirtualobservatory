<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ds="http://www.ivoa.net/xml/VODataService/v0.5">

<xsl:template match="*|/"><xsl:apply-templates/></xsl:template>

<xsl:template match="text()|@*"> <xsl:value-of select="."/> </xsl:template>

<xsl:template match="//ds:region">
 <xsl:value-of select="@xsi:type"/> 
   <xsl:apply-templates select="//ds:long"/>
   <xsl:apply-templates select="//ds:lat"/>
   <xsl:apply-templates select="//ds:radius"/>
</xsl:template>


<xsl:template match="//ds:long"> <xsl:value-of select="."/>, </xsl:template>

<xsl:template match="//ds:lat"> <xsl:value-of select="."/>, </xsl:template>

<xsl:template match="//ds:radius"> <xsl:value-of select="."/> </xsl:template>


</xsl:stylesheet>