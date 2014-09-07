<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" xsl:exclude-result-prefixes="vo">
<xsl:import href="http://heasarcdev.gsfc.nasa.gov/vo/datascope/xsl/voview.xsl" />
<xsl:output method="html" />

<xsl:param name="allFitsCheck" />

<!-- prefix-* templates do nothing 
     but serve as place holders to be overriden -->
<xsl:template name="prefix-header">
    <th> Selection
    </th>
</xsl:template>

<xsl:template name="prefix-column">
    <xsl:param name="index" />
    <xsl:param name="format" />
    <xsl:param name="isSelected" />
    <td>
       <xsl:if test="contains(translate($format, $lc, $uc),'FITS')" >
           <xsl:choose>
               <xsl:when test="contains($isSelected, 'selectedimage')">
                   <input checked="checked" type="checkbox" />
	       </xsl:when>
	       <xsl:otherwise>
		   <input type="checkbox" />
	       </xsl:otherwise>
	   </xsl:choose>
           <a href="" onClick='return fov(this)'
             title="View FOV overlay on DSS background">FOV</a>
       </xsl:if>
   </td>
</xsl:template>
<xsl:template name="prefix-filter">
    <xsl:choose>
        <xsl:when test="$allFitsCheck = 'checked'">
           <td><input type="checkbox" checked="checked" id="allFits" onClick="return setAllFits(this)" /> All</td>
	</xsl:when>
	<xsl:otherwise>
           <td><input type="checkbox" id="allFits" onClick="return setAllFits(this)" /> All</td>
	</xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="start"  match="/">
    <xsl:apply-imports />
</xsl:template>


</xsl:stylesheet>
