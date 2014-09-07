<?xml version="1.0"?>
<!--
  -  templates for browsing ConeSearch capabilities
  -->
<xsl:stylesheet xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="1.0">

   <!--
     -  summarize a ConeSearch capability
     -->
   <xsl:template match="capability[@standardID='ivo://ivoa.net/std/ConeSearch']">

      <xsl:apply-templates select="." mode="simpleCapability">
         <xsl:with-param name="name">Simple Cone Search</xsl:with-param>
         <xsl:with-param name="desc">
            This is a standard IVOA service that takes as input a position in 
            the sky and a radius and returns catalog records with positions 
            within that radius.
         </xsl:with-param>
      </xsl:apply-templates>

   </xsl:template>

   <!--
     -  the template for rendering the ConeSearch capability's extended 
     -  metadata.  It is called from the mode="simpleCapability" template.
     -->
   <xsl:template match="capability[@standardID='ivo://ivoa.net/std/ConeSearch']"
                 mode="extendedCapability">

      <strong>Maximum search radius accepted: </strong>
      <xsl:value-of select="maxSR"/> <xsl:text> degrees</xsl:text><br />
      <strong>Maximum number of matching records returned: </strong>
      <xsl:value-of select="maxRecords"/> <br />
      <xsl:choose>
        <xsl:when test="verbosity='true' or verbosity='1' or 
                  (boolean(verbosity) and verbosity!='false' and verbosity!='0')">
          <dl>
            <dt>
              <strong>This service supports the VERB input parameter:</strong>
            </dt>
            <dd>Use <code>VERB=1</code> to minimize the returned columns or 
                <code>VERB=3</code> to maximize.</dd>
          </dl>
        </xsl:when>
        <xsl:otherwise>
          <xsl:text>This service will ignore the </xsl:text> 
          <code>VERB</code> 
          <xsl:text> input parameter.</xsl:text>
        </xsl:otherwise>
      </xsl:choose>
   </xsl:template>



</xsl:stylesheet>
