<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" 
    xmlns:v1="http://vizier.u-strasbg.fr/VOTable"
    xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
    xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"
    xmlns:v4="http://www.ivoa.net/xml/VOTable/VOTable-1.1.xsd"
    exclude-result-prefixes="vo v1 v2 v3 v4"
>

<!-- Strip out the first VOTable in a VOTABLES/VOTABLE_ENTRY/VOTABLE format
     concatenation of VOTABLEs as produced by the VOClient VODATA program.
  -->
  
<xsl:output method="xml" />

<xsl:template match="/" >
<xsl:copy-of
select="/VOTABLES/VOTABLE_ENTRY[1]/VOTABLE|/VOTABLES/VOTABLE_ENTRY[1]/vo:VOTABLE|/VOTABLES/VOTABLE_ENTRY[1]/v1:VOTABLE|/VOTABLES/VOTABLE_ENTRY[1]/v2:VOTABLE|/VOTABLES/VOTABLE_ENTRY[1]/v3:VOTABLE|/VOTABLES/VOTABLE_ENTRY[1]/v4:VOTABLE"
/>
</xsl:template>

</xsl:stylesheet>
