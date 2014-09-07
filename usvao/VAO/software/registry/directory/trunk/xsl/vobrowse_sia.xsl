<?xml version="1.0"?>
<!--
  -  templates for browsing SIA capabilities
  -->
<xsl:stylesheet xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                version="1.0">

   <!--
     -  summarize an SIA capability
     -->
   <xsl:template match="capability[@standardID='ivo://ivoa.net/std/SIA']">

      <xsl:apply-templates select="." mode="simpleCapability">
         <xsl:with-param name="name">Simple Image Access</xsl:with-param>
         <xsl:with-param name="desc">
            This is a standard IVOA service used to search for images from 
            this resource that overlap with a specified region of the sky.
         </xsl:with-param>
      </xsl:apply-templates>

   </xsl:template>

   <!--
     -  the template for rendering the SIA capability's extended 
     -  metadata.  It is called from the mode="simpleCapability" template.
     -->
   <xsl:template match="capability[@standardID='ivo://ivoa.net/std/SIA']"
                 mode="extendedCapability">

     <strong>Image service type: </strong>
     <xsl:apply-templates select="imageServiceType" mode="sia"/><br />
     <strong>Maximum query region accepted: </strong>
     <xsl:apply-templates select="maxQueryRegionSize" mode="sia" /><br />
     <strong>Maximum region size returned: </strong>
     <xsl:apply-templates select="maxImageExtent" mode="sia" /><br />
     <strong>Maximum image size returned: </strong>
     <xsl:apply-templates select="maxImageSize" mode="sia" /><br />
     <strong>Maximum image size returned: </strong>
     <xsl:apply-templates select="maxFileSize" mode="sia" /><br />
     <strong>Maximum number of matching records returned: </strong>
     <xsl:value-of select="maxRecords"/> <br />
   </xsl:template>

   <!--
     -  render a region size
     -->
   <xsl:template match="maxQueryRegionSize|maxImageExtent" mode="sia">
     <xsl:choose>
       <xsl:when test="number(normalize-space(long))&gt;=360 and 
                       number(normalize-space(lat))&gt;=180">
         <i>no size limit</i>
       </xsl:when>
       <xsl:otherwise>
         <xsl:text>longitude: </xsl:text>
         <xsl:choose>
           <xsl:when test="number(normalize-space(long))&gt;=360">
             <i>no limit</i>
           </xsl:when>
           <xsl:otherwise>
             <xsl:value-of select="normalize-space(long)"/>
             <xsl:text> degrees</xsl:text>
           </xsl:otherwise>
         </xsl:choose>
         <xsl:text>; latitude: </xsl:text>
         <xsl:choose>
           <xsl:when test="number(normalize-space(lat))&gt;=180">
             <i>no limit</i>
           </xsl:when>
           <xsl:otherwise>
             <xsl:value-of select="normalize-space(lat)"/>
             <xsl:text> degrees</xsl:text>
           </xsl:otherwise>
         </xsl:choose>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <!--
     -  render an image size
     -->
   <xsl:template match="maxImageSize" mode="sia">
     <xsl:value-of select="normalize-space(long)"/>
     <xsl:text> X </xsl:text>
     <xsl:value-of select="normalize-space(lat)"/>
     <xsl:text> pixels</xsl:text>
   </xsl:template>

   <!--
     -  render the image service type
     -->
   <xsl:template match="imageServiceType" mode="sia">
     <xsl:value-of select="normalize-space(.)"/>
     <xsl:text>: </xsl:text><i>
     <xsl:choose>
       <xsl:when test=".='Cutout'">
         <xsl:text>images will be cut out to match the </xsl:text>
         <xsl:text>requested region.</xsl:text>
       </xsl:when>
       <xsl:when test=".='Mosaic'">
         <xsl:text>images will be cut out and resampled to </xsl:text>
         <xsl:text>match the requested region, resolution, and </xsl:text>
         <xsl:text>projection.</xsl:text>
       </xsl:when>
       <xsl:when test=".='Atlas'">
         <xsl:text>pre-computed survey images will be returned.</xsl:text>
       </xsl:when>
       <xsl:when test=".='Pointed'">
         <xsl:text>pre-computed non-survey images will be returned.</xsl:text>
       </xsl:when>
     </xsl:choose></i>
   </xsl:template>

   <xsl:template match="maxFileSize" mode="sia">
     <xsl:param name="size" select="number(normalize-space(.))"/>

     <xsl:choose>
       <xsl:when test="$size &lt; 0">
         <i> no size limit </i>
       </xsl:when>
       <xsl:when test="$size &gt;= 1000000000">
         <xsl:value-of select="round($size div 1000000000.0)"/>
         <xsl:text> GB</xsl:text>
       </xsl:when>
       <xsl:when test="$size &gt;= 1000000">
         <xsl:value-of select="round($size div 1000000.0)"/>
         <xsl:text> MB</xsl:text>
       </xsl:when>
       <xsl:when test="$size &gt;= 1000">
         <xsl:value-of select="round($size div 1000.0)"/>
         <xsl:text> kB</xsl:text>
       </xsl:when>
       <xsl:otherwise>
         <xsl:value-of select="$size"/>
         <xsl:text> bytes</xsl:text>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
