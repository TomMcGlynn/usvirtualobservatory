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

  <!--
     -  summarize VOSI capabilities
     -->
  <xsl:template match="capability[@standardID='ivo://ivoa.net/std/VOSI#capabilities']">

    <xsl:apply-templates select="." mode="complexCapability">
      <xsl:with-param name="name">VOSI Capabilities</xsl:with-param>
      <xsl:with-param name="desc">
        This is a standard IVOA service endpoint that returns the detailed capabilities of the main IVOA standard service.
      </xsl:with-param>
      <xsl:with-param name="assumeStandard" select="true()"/>
    </xsl:apply-templates>

  </xsl:template>

  <xsl:template match="capability[@standardID='ivo://ivoa.net/std/VOSI#availability']">

    <xsl:apply-templates select="." mode="complexCapability">
      <xsl:with-param name="name">VOSI Availability</xsl:with-param>
      <xsl:with-param name="desc">
        This is a standard IVOA service endpoint that returns information about the availability and uptime of the main IVOA standard service.
      </xsl:with-param>
    </xsl:apply-templates>

  </xsl:template>

  <xsl:template match="capability[@standardID='ivo://ivoa.net/std/VOSI#tables']">

    <xsl:apply-templates select="." mode="complexCapability">
      <xsl:with-param name="name">VOSI Tables</xsl:with-param>
      <xsl:with-param name="desc">
        This is a standard IVOA service endpoint that returns information about the table schema managed by the main IVOA standard service.
      </xsl:with-param>
    </xsl:apply-templates>

  </xsl:template>

</xsl:stylesheet>