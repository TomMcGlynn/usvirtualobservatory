<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" exclude-result-prefixes="vo">

<xsl:import href="voview.xsl" />
<xsl:output method="html" />

<xsl:param name="allFitsCheck" />

<!-- prefix-* templates do nothing but serve as place holders to be overriden -->
<xsl:template name="prefix-header">
   <th> Selection </th>
</xsl:template>

<xsl:template name="prefix-column">
   <xsl:param name="index" />
   <xsl:param name="format" />
   <xsl:param name="isSelected" />
   <xsl:param name="urlColumnNum" />
   <xsl:variable name="xrow" select="position()" />
   <td>
      <xsl:if test="contains(translate($format, $lc, $uc),'FITS')" >
         <xsl:value-of select="position()" />. 
         <xsl:variable name="ffile" select="TD[number($urlColumnNum)]" />
         <xsl:choose>
            <xsl:when test="contains($isSelected, 'selectedimage')">
               <input checked="checked" type="checkbox" />
            </xsl:when>
            <xsl:otherwise>
               <input type="checkbox" />
            </xsl:otherwise>
         </xsl:choose>
         <a href="" onClick="return fov(this, {$xrow}, '{$ffile}')"
            title="Where does this image lie in the requested field?">FOV</a>
         <xsl:text>&#160;-&#160;</xsl:text>
         <a href="" onClick="return view(this, {$xrow}, '{$ffile}')"
            title="Render the FITS file as a JPEG">View</a>
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
