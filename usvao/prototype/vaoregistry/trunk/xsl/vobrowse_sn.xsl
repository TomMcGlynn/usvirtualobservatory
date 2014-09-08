<?xml version="1.0"?>
<!--
  -  templates for browsing SIA capabilities
  -->
<xsl:stylesheet xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="1.0">

   <!--
     -  summarize a SkyNode capability
     -->
   <xsl:template match="capability[@standardID='ivo://ivoa.net/std/OpenSkyNode]">
      <xsl:apply-templates select="." mode="simpleCapability">
         <xsl:with-param name="name">Open Sky Node</xsl:with-param>
         <xsl:with-param name="desc">
            This is a standard IVOA service for that allows for sophisticated 
            searching of a set of database tables provided by this resource.
         </xsl:with-param>
      </xsl:apply-templates>
   </xsl:template>

   <!--
     -  The template for rendering the SkyNode capabilities extended 
     -  metadata
     -->
   <xsl:template match="capability[@standardID='ivo://ivoa.net/std/OpenSkyNode]"
                 mode="extendedCapability">
       <xsl:apply-templates select="compliance" mode="osn"/> <br />
       <strong>Server Location: </strong>
       <xsl:apply-templates select="." mode="osnlocation"/> <br />
       <strong>Primary table containing source sky positions: </strong>
       <xsl:value-of select="primaryTable"/> <br />
       <strong>Primary key for the primary table: </strong>
       <xsl:value-of select="primaryKey"/> <br />
   </xsl:template>

   <!--
     -  render the compliance level
     -->
   <xsl:template match="compliance" mode="osn">
       <xsl:choose>
          <xsl:when test=".='Full'">
             <strong>This is a Full Sky Node: </strong>
             <xsl:text>its data can be cross-correllated with data </xsl:text>
             <xsl:text>from other Full Sky Nodes.</xsl:text>
          </xsl:when>
          <xsl:when test=".='Basic'">
             <strong>This is a Basic Sky Node: </strong>
             <xsl:text>it does not support corss-corrllation.</xsl:text>
          </xsl:when>
          <xsl:otherwise>
             <strong>Unrecognized support level: </strong>
             <xsl:value-of select="."/>
          </xsl:otherwise>
       </xsl:choose>
   </xsl:template>
   
   <!--
     -  render the global position of the sky node server
     -->
   <xsl:template match="capability" mode="osnlocation">
      <xsl:param name="long" select="number(normalize-space(longitude))"/>
      <xsl:param name="lat" select="number(normalize-space(latitude))"/>

      <xsl:choose>
         <xsl:when test="$lat &lt; 0">
            <xsl:value-of select="-1 * $lat"/>
            <xsl:text> S</xsl:text>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$lat"/>
            <xsl:text> N</xsl:text>
         </xsl:otherwise>
      </xsl:choose>

      <xsl:text> </xsl:text>

      <xsl:choose>
         <xsl:when test="$long &lt; 0">
            <xsl:value-of select="-1 * $long"/>
            <xsl:text> E</xsl:text>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$long"/>
            <xsl:text> W</xsl:text>
         </xsl:otherwise>
      </xsl:choose>

   </xsl:template>
   

</xsl:stylesheet>
