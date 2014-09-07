<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
        xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
        xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" 
        xmlns:v1="http://vizier.u-strasbg.fr/VOTable"
        xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
        xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"
        exclude-result-prefixes="vo v1 v2 v3"
>
<!--

   This stylesheet is used to convert a VOTABLE to a more Java-parsable
   XML document to be converted into a NodeList and then HashMap.

-->

<xsl:output method="xml" />

<xsl:variable name="fieldlist" select="//FIELD|//vo:FIELD|//v1:FIELD|//v2:FIELD|//v3:FIELD"/>

<xsl:template match="/">
<catalogs>
 <xsl:for-each select="//TR|//vo:TR|//v1:TR|//v2:TR|//v3:TR">
  <catalog>
   <xsl:variable name="row" select="position()" />
   <xsl:for-each select="TD|vo:TD">
    <xsl:variable name="col" select="position()" />
    <xsl:element name="{$fieldlist[position()=$col]/@ID}">
     <xsl:value-of select="."/>
    </xsl:element>
   </xsl:for-each>
  </catalog>
 </xsl:for-each>
</catalogs>
</xsl:template>

</xsl:stylesheet>
