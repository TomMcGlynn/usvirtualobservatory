<?xml version="1.0"?>
<!--
  -  templates for browsing SSA capabilities
  -->
<xsl:stylesheet xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="1.0">

   <!--
     -  summarize an SSA capability
     -->
   <xsl:template match="capability[@standardID='ivo://ivoa.net/std/SSA']">

      <xsl:apply-templates select="." mode="simpleCapability">
         <xsl:with-param name="name">Simple Spectral Access</xsl:with-param>
         <xsl:with-param name="desc">
            This is a standard IVOA service for searches for spectra from 
            this resource that were observed within a specified region of 
            the sky.
         </xsl:with-param>
      </xsl:apply-templates>

   </xsl:template>

</xsl:stylesheet>
