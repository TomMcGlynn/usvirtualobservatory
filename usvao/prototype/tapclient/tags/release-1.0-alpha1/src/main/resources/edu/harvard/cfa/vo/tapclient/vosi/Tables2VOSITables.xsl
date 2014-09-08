<?xml version='1.0' encoding='UTF-8'?>

<!-- To convert the Astrogrid Tables.xsd to the IVOA VOSITables-v1.0.xsd -->
<!-- http://www.astrogrid.org/viewcvs/astrogrid/dsa/dsa-catalog/src/main/webapp/schema/Tables.xsd?view=markup -->
<!-- http://www.ivoa.net/Documents/VOSI/20110511/PR-VOSI-1.0-20110511.html#appA -->
<xsl:stylesheet
    version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
    xmlns:tab="urn:astrogrid:schema:TableMetadata"
    xmlns:vosi="http://www.ivoa.net/xml/VOSITables/v1.0"
    xmlns:vs="http://www.ivoa.net/xml/VODataService/v1.1">

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()" />
    </xsl:copy>
  </xsl:template>

  <!-- 
       Astrogrid DSA/Catalogue implementations have document root
       <tables><table>...
       Instead of the VOSI specified
       <tableset><schema><table>...
  -->
  <xsl:template match="tables">
    <xsl:element name="tableset">
      <!-- xmlns="http://www.ivoa.net/xml/VOSITables/v1.0"> -->
      <xsl:element name="schema">
	<xsl:element name="name" />
	<xsl:apply-templates />
      </xsl:element>
    </xsl:element>
  </xsl:template>

  <!-- 
       Some Astrogrid DSA/Catalogue implementations have 
       <dataType xsi:type="vod:TAPType">FLOAT</dataType> 
       instead of                                        
       <dataType xsi:type="vod:TAPType">REAL</dataType>  
  -->
  <xsl:template match='dataType[@type = "vod:TAPType"]/text()'>
    <xsl:choose>
      <xsl:when test='. = "FLOAT"'>
	<xsl:text>REAL</xsl:text>
      </xsl:when>
      <xsl:otherwise>
	<xsl:value-of select="." />
     </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

</xsl:stylesheet>
