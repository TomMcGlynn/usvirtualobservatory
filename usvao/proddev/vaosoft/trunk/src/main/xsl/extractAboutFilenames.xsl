<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
  -  This stylesheet extract the about.properties files representing 
  -  products that need to be installed as product dependencies.
  -
  -  This will create a property file with one property,
  -  build.dep.orderedAbouts, defined in it.  This will contain the 
  -  the dependency-ordered list of products that need to be
  -  installed.  
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                version="1.0">

   <xsl:output method="text" encoding="UTF-8" />

   <xsl:template match="/">
     <xsl:text>build.dep.ordered.about: </xsl:text>
     <xsl:apply-templates select="/modules/module/artifact[@type='about']"/>
     <xsl:text>
</xsl:text>
   </xsl:template>

   <xsl:template match="artifact">
     <xsl:if test="position()>1"><xsl:text>,</xsl:text></xsl:if>
     <xsl:value-of select="cache-location" />
   </xsl:template>

</xsl:stylesheet>
