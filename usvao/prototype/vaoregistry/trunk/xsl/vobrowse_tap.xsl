<?xml version="1.0"?>
<!--
  -  templates for browsing TAP capabilities
  -->
<xsl:stylesheet xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="1.0">

   <!--
     -  summarize a TAP capability
     -->
   <xsl:template match="capability[@standardID='ivo://ivoa.net/std/TAP']">

      <xsl:apply-templates select="." mode="complexCapability">
         <xsl:with-param name="name">Table Access Protocol</xsl:with-param>
         <xsl:with-param name="desc">
            This is a standard IVOA service that takes as input an ADQL or PQL
            query and returns tabular data.
         </xsl:with-param>
      </xsl:apply-templates>

   </xsl:template>

</xsl:stylesheet>
