<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  - Stylesheet to convert ADQL version 1.0 to an SQL String 
  - Adapted for RegTAP DB (pre1.0)
  - This stylesheet was created automatically from mkquery.xsl and is 
  - based on stylesheets from ADQLlib Version 1.1 
  -   updated by Ray Plante (NCSA) updated for ADQLlib
  - Based on v1.0 by Ramon Williamson, NCSA (April 1, 2004)
  - Based on the schema: http://www.ivoa.net/xml/ADQL/v1.0
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns:ad="http://www.ivoa.net/xml/ADQL/v1.0" version="1.0">

   <xsl:output method="text"/>

   <xsl:param name="nl">
      <xsl:text>
</xsl:text>
   </xsl:param>

    <xsl:param name="schema">dbo</xsl:param>
   <!--<xsl:param name="schema">rr</xsl:param>-->

   <xsl:param name="intcasttype">int</xsl:param>
   <!--<xsl:param name="intcasttype">SIGNED</xsl:param>-->
   <xsl:param name="floatcasttype">float</xsl:param>
   <!--<xsl:param name="floatcasttype">DECIMAL</xsl:param>-->

   <!--<xsl:param name="statusconstraint">
      <xsl:value-of select="$schema"/>
      <xsl:text>.resource.status = 'active'</xsl:text>
   </xsl:param>-->
   
   <xsl:param name="statusconstraint">
      <xsl:value-of select="$schema"/>
      <xsl:text>.resource.rstat=1</xsl:text>
   </xsl:param>
    

   <xsl:param name="usepkey" select="true()"/>

   <xsl:param name="detailns">vor</xsl:param>

   <xsl:param name="pretty"/>

   <xsl:param name="reporterrors" select="true()"/>

   <xsl:param name="br">
      <xsl:choose>
         <xsl:when test="$pretty!=''">
            <xsl:value-of select="$nl"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="' '"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:param>
   <xsl:param name="sp">
      <xsl:if test="$pretty=''">
         <xsl:value-of select="' '"/>
      </xsl:if>
   </xsl:param>
   
   <xsl:variable name="detail_utypes">
      <xsl:apply-templates select="/ad:Where/ad:Condition" mode="utypes"/>
   </xsl:variable>

   <xsl:variable name="role_utypes">
      <xsl:apply-templates select="/ad:Where/ad:Condition" mode="roleutypes"/>
   </xsl:variable>

   <xsl:variable name="select_tables">
      <xsl:apply-templates select="/ad:Where/ad:Condition" mode="tables"/>
   </xsl:variable>

   <xsl:variable name="constrained_columns">
      <xsl:apply-templates select="/ad:Where/ad:Condition" mode="columns" />
   </xsl:variable>

   <xsl:template match="@xpathName" mode="columns">
      <xsl:call-template name="listify">
         <xsl:with-param name="item">
            <xsl:apply-templates select="." mode="column"/>
         </xsl:with-param>
      </xsl:call-template>
   </xsl:template>

   <xsl:template match="@xpathName" mode="tables">
      <xsl:call-template name="listify">
         <xsl:with-param name="item">
            <xsl:apply-templates select="." mode="table"/>
         </xsl:with-param>
      </xsl:call-template>
   </xsl:template>

   <xsl:template match="@xpathName" mode="utypes">
      <xsl:call-template name="listify">
         <xsl:with-param name="item">
            <xsl:apply-templates select="." mode="utype"/>
         </xsl:with-param>
      </xsl:call-template>
   </xsl:template>

   <xsl:template match="@xpathName" mode="roleutypes">
      <xsl:call-template name="listify">
         <xsl:with-param name="item">
            <xsl:apply-templates select="." mode="roleutype"/>
         </xsl:with-param>
      </xsl:call-template>
   </xsl:template>

   <xsl:template name="listify">
      <xsl:param name="item"/>
      <xsl:if test="$item!=''">
         <xsl:value-of select="$item"/><xsl:text>#</xsl:text>
      </xsl:if>
   </xsl:template>

   <!--
     - Mapping Templates
     -
     - These templates map xpathName identifiers in the ADQL query to 
     - column names in the data base.  
     -->

   <xsl:template match="@xpathName[self::node()='@created']" mode="column">
      <xsl:text>created</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='@status']" mode="column">
      <xsl:text>status</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='@updated']" mode="column">
      <xsl:text>updated</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='@xsi:type']" mode="column">
      <xsl:text>res_type</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='accessURL/@use']" mode="column">
      <xsl:text>url_use</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='accessURL/@use']" mode="table">
      <xsl:text>interface</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/@standardID']" mode="column">
      <xsl:text>standard_id</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/@standardID']" mode="table">
      <xsl:text>capability</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/@xsi:type']" mode="column">
      <xsl:text>cap_type</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/@xsi:type']" mode="table">
      <xsl:text>Capability</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/compliance']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/compliance']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/compliance']" mode="utype">
      <xsl:text>capability.compliance</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/description']" mode="column">
      <xsl:text>cap_description</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/description']" mode="table">
      <xsl:text>capability</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/extensionSearchSupport']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/extensionSearchSupport']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/extensionSearchSupport']" mode="utype">
      <xsl:text>capability.extensionSearchSupport</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/imageServiceType']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/imageServiceType']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/imageServiceType']" mode="table">
      <xsl:text>capability.imageServiceType</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/@role']" mode="column">
      <xsl:text>intf_role</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/@role']" mode="table">
      <xsl:text>interface</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/@version']" mode="column">
      <xsl:text>std_version</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/@version']" mode="table">
      <xsl:text>interface</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/@xsi:type']" mode="column">
      <xsl:text>intf_type</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/@xsi:type']" mode="table">
      <xsl:text>interface</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/accessURL/@use']" mode="column">
      <xsl:text>url_use</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/accessURL/@use']" mode="table">
      <xsl:text>interface</xsl:text>
   </xsl:template>

   <!-- ATTN: std is a smallint, @std is a string? -->
   <xsl:template match="@xpathName[self::node()='capability/interface/param/@std']" mode="column">
      <xsl:text>std</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/param/@std']" mode="table">
      <xsl:text>intf_param</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/param/@use']" mode="column">
      <xsl:text>form</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/param/@use']" mode="table">
      <xsl:text>intf_param</xsl:text>
   </xsl:template>

   <!-- ATTN: no longer supported for searching?  
     - 
   <xsl:template match="@xpathName[self::node()='capability/interface/param/dataType/@arraysize']" mode="column">
      <xsl:text>[InputParam].[dataType/@arraysize]</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/param/dataType/@arraysize']" mode="table">
      <xsl:text>InputParam</xsl:text>
   </xsl:template>
     -->

   <xsl:template match="@xpathName[self::node()='capability/interface/queryType']" mode="column">
      <xsl:text>query_type</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/queryType']" mode="table">
      <xsl:text>interface</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/resultType']" mode="column">
      <xsl:text>result_type</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/resultType']" mode="table">
      <xsl:text>interface</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/interface/securityMethod/@standardID']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/securityMethod/@standardID']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/securityMethod/@standardID']" mode="table">
      <xsl:text>capability.interface.securityMethod.standardID</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/wsdlURL']" mode="column">
      <xsl:text>wsdl_url</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/wsdlURL']" mode="table">
      <xsl:text>interface</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail: OpenSkyNode numeric valued; drop support -->
   <xsl:template match="@xpathName[self::node()='capability/latitude']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/latitude']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/latitude']" mode="utype">
      <xsl:text>capability.latitude</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/latitude']" mode="dtype">
      <xsl:text>float</xsl:text>
   </xsl:template>
    

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/longitude']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/longitude']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/longitude']" mode="utype">
      <xsl:text>capability.longitude</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/longitude']" mode="dtype">
      <xsl:text>float</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/maxFileSize']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxFileSize']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxFileSize']" mode="utype">
      <xsl:text>capability.maxFileSize</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxFileSize']" mode="dtype">
      <xsl:text>int</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/lat']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/lat']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/lat']" mode="utype">
      <xsl:text>capability.maxImageExtent.lat</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/lat']" mode="dtype">
      <xsl:text>float</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/long']" mode="column">
      <xsl:text></xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/long']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/long']" mode="utype">
      <xsl:text>capability.maxImageExtent.long</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/long']" mode="dtype">
      <xsl:text>float</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/lat']" mode="column">
      <xsl:text></xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/lat']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/lat']" mode="utype">
      <xsl:text>capability.maxImageSize.lat</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/lat']" mode="dtype">
      <xsl:text>int</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/long']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/long']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/long']" mode="utype">
      <xsl:text>capability.maxImageSize.long</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/long']" mode="dtype">
      <xsl:text>int</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/maxQueryRegionSize/lat']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxQueryRegionSize/lat']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxQueryRegionSize/lat']" mode="utype">
      <xsl:text>capability.maxQueryRegionSize.lat</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/maxQueryRegionSize/long']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxQueryRegionSize/long']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxQueryRegionSize/long']" mode="utype">
      <xsl:text>capability.maxQueryRegionSize.long</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxQueryRegionSize/long']" mode="dtype">
      <xsl:text>float</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/maxRecords']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxRecords']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxRecords']" mode="utype">
      <xsl:text>capability.maxRecords</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxRecords']" mode="utype">
      <xsl:text>capability.maxRecords</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxRecords']" mode="dtype">
      <xsl:text>int</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/maxSR']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxSR']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxSR']" mode="utype">
      <xsl:text>capability.maxSR</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxSR']" mode="dtype">
      <xsl:text>float</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/optionalProtocol']" mode="column">
      <xsl:text></xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/optionalProtocol']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/optionalProtocol']" mode="utype">
      <xsl:text>capability.optionalProtocol</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/primaryKey']" mode="column">
      <xsl:text></xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/primaryKey']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/primaryKey']" mode="utype">
      <xsl:text>capability.primaryKey</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/primaryTable']" mode="column">
      <xsl:text></xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/primaryTable']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/primaryTable']" mode="utype">
      <xsl:text>capability.primaryTable</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/testQuery/catalog']" mode="column">
      <xsl:text></xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/catalog']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/catalog']" mode="utype">
      <xsl:text>capability.testQuery.catalog</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/testQuery/dec']" mode="column">
      <xsl:text></xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/dec']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/dec']" mode="utype">
      <xsl:text>capability.testQuery.dec</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/dec']" mode="dtype">
      <xsl:text>float</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/testQuery/extras']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/extras']" mode="table">
      <xsl:text></xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/extras']" mode="utype">
      <xsl:text>capability.testQuery.extras</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/testQuery/pos/lat']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/pos/lat']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/pos/lat']" mode="utype">
      <xsl:text>capability.testQuery.pos.lat</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/testQuery/pos/long']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/pos/long']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/pos/long']" mode="utype">
      <xsl:text>capability.testQuery.pos.long</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/testQuery/ra']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/ra']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/ra']" mode="utype">
      <xsl:text>capability.testQuery.ra</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/ra']" mode="dtype">
      <xsl:text>float</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/lat']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/lat']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/lat']" mode="utype">
      <xsl:text>capability.testQuery.size.lat</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/lat']" mode="dtype">
      <xsl:text>float</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/long']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/long']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/long']" mode="utype">
      <xsl:text>capability.testQuery.size.long</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/long']" mode="dtype">
      <xsl:text>float</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/testQuery/sr']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/sr']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/sr']" mode="utype">
      <xsl:text>capability.testQuery.sr</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/sr']" mode="dtype">
      <xsl:text>float</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/testQuery/verb']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/verb']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/verb']" mode="utype">
      <xsl:text>capability.testQuery.verb</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/verb']" mode="dtype">
      <xsl:text>int</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='capability/verbosity']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/verbosity']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/verbosity']" mode="utype">
      <xsl:text>capability.verbosity</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='catalog/description']" mode="column">
      <xsl:text>schema_description</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/description']" mode="table">
      <xsl:text>res_schema</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/description']" mode="column">
      <xsl:text>schema_description</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/description']" mode="table">
      <xsl:text>res_schema</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='catalog/name']" mode="column">
      <xsl:text>schema_name</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/name']" mode="table">
      <xsl:text>res_schema</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/name']" mode="column">
      <xsl:text>schema_name</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/name']" mode="table">
      <xsl:text>res_schema</xsl:text>
   </xsl:template>

   <!-- ATTN: transform value of @role -->
   <xsl:template match="@xpathName[self::node()='catalog/table/@role']" mode="column">
      <xsl:text>table_type</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/table/@role']" mode="table">
      <xsl:text>res_table</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/table/@type']" mode="column">
      <xsl:text>table_type</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/table/@type']" mode="table">
      <xsl:text>res_table</xsl:text>
   </xsl:template>

   <!-- ATTN: std is a smallint, @std is a string? -->
   <xsl:template match="@xpathName[self::node()='catalog/table/column/@std']" mode="column">
      <xsl:text>std</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/table/column/@std']" mode="table">
      <xsl:text>table_column</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/table/column/@std']" mode="column">
      <xsl:text>std</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/table/column/@std']" mode="table">
      <xsl:text>table_column</xsl:text>
   </xsl:template>

   <!-- ATTN: no longer supported for searching?  
     - 
   <xsl:template match="@xpathName[self::node()='catalog/table/column/dataType/@arraysize']" mode="column">
      <xsl:text>[table_column].[arraysize]</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/table/column/dataType/@arraysize']" mode="table">
      <xsl:text>table_column</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/table/column/dataType/@arraysize']" mode="column">
      <xsl:text>[table_column].[arraysize]</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/table/column/dataType/@arraysize']" mode="table">
      <xsl:text>table_column</xsl:text>
   </xsl:template>
     -->

   <xsl:template match="@xpathName[self::node()='catalog/table/description']" mode="column">
      <xsl:text>table_description</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/table/description']" mode="table">
      <xsl:text>res_table</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/table/description']" mode="column">
      <xsl:text>table_description</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/table/description']" mode="table">
      <xsl:text>res_table</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='catalog/table/name']" mode="column">
      <xsl:text>table_name</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/table/name']" mode="table">
      <xsl:text>res_table</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/table/name']" mode="column">
      <xsl:text>table_name</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='tableset/schema/table/name']" mode="table">
      <xsl:text>res_table</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/contentLevel']" mode="column">
      <xsl:text>content_level</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/description']" mode="column">
      <xsl:text>res_description</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/referenceURL']" mode="column">
      <xsl:text>reference_url</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/relationship/relatedResource/@ivo-id']" mode="column">
      <xsl:text>related_id</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='content/relationship/relatedResource/@ivo-id']" mode="table">
      <xsl:text>relationship</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/relationship/relationshipType']" mode="column">
      <xsl:text>relationship_type</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='content/relationship/relationshipType']" mode="table">
      <xsl:text>relationship</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/source/@format']" mode="column">
      <xsl:text>source_format</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/subject']" mode="column">
      <xsl:text>subject</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='content/subject']" mode="table">
      <xsl:text>subject</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/type']" mode="column">
      <xsl:text>content_type</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='coverage/footprint/@ivo-id']" mode="column">
      <xsl:text>footprint_ivoid</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='coverage/waveband']" mode="column">
      <xsl:text>waveband</xsl:text>
   </xsl:template>

   <!-- ATTN: needs constraint on base_utype? -->
   <xsl:template match="@xpathName[self::node()='curation/contact/address']" mode="column">
      <xsl:text>address</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/address']" mode="table">
      <xsl:text>res_role</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/address']" mode="roleutype">
      <xsl:text>resource.curation.contact</xsl:text>
   </xsl:template>

   <!-- ATTN: needs constraint on base_utype? -->
   <xsl:template match="@xpathName[self::node()='curation/contact/email']" mode="column">
      <xsl:text>email</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/email']" mode="table">
      <xsl:text>res_role</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/email']" mode="roleutype">
      <xsl:text>resource.curation.contact</xsl:text>
   </xsl:template>

   <!-- ATTN: needs constraint on role_name -->
   <xsl:template match="@xpathName[self::node()='curation/contact/name/@ivo-id']" mode="column">
      <xsl:text>role_ivoid</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/name/@ivo-id']" mode="table">
      <xsl:text>res_role</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/name/@ivo-id']" mode="roleutype">
      <xsl:text>resource.curation.contact</xsl:text>
   </xsl:template>

   <!-- ATTN: needs constraint on base_utype? -->
   <xsl:template match="@xpathName[self::node()='curation/contact/telephone']" mode="column">
      <xsl:text>telephone</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/telephone']" mode="table">
      <xsl:text>res_role</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/telephone']" mode="roleutype">
      <xsl:text>resource.curation.contact</xsl:text>
   </xsl:template>

   <!-- ATTN: needs constraint on role_name -->
   <xsl:template match="@xpathName[self::node()='curation/contributor/@ivo-id']" mode="column">
      <xsl:text>role_ivoid</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contributor/@ivo-id']" mode="table">
      <xsl:text>res_role</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contributor/@ivo-id']" mode="roleutype">
      <xsl:text>resource.curation.contributor</xsl:text>
   </xsl:template>

   <!-- ATTN: needs constraint on base_utype? -->
   <xsl:template match="@xpathName[self::node()='curation/creator/logo']" mode="column">
      <xsl:text>logo</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/creator/logo']" mode="table">
      <xsl:text>res_role</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/creator/logo']" mode="roleutype">
      <xsl:text>resource.curation.creator</xsl:text>
   </xsl:template>

   <!-- ATTN: needs constraint on base_utype? -->
   <xsl:template match="@xpathName[self::node()='curation/creator/name/@ivo-id']" mode="column">
      <xsl:text>role_ivoid</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/creator/name/@ivo-id']" mode="table">
      <xsl:text>res_role</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/creator/name/@ivo-id']" mode="roleutype">
      <xsl:text>resource.curation.creator</xsl:text>
   </xsl:template>

   <!-- ATTN: needs constraint on value_role? -->
   <xsl:template match="@xpathName[self::node()='curation/date/@role']" mode="column">
      <xsl:text>value_role</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/date/@role']" mode="table">
      <xsl:text>res_date</xsl:text>
   </xsl:template>

   <!-- ATTN: needs constraint on base_utype? -->
   <xsl:template match="@xpathName[self::node()='curation/publisher/@ivo-id']" mode="column">
      <xsl:text>role_ivoid</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/publisher/@ivo-id']" mode="table">
      <xsl:text>res_role</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/publisher/@ivo-id']" mode="roleutype">
      <xsl:text>resource.curation.publisher</xsl:text>
   </xsl:template>

   <!-- ATTN: needs constraint on base_utype? -->
   <xsl:template match="@xpathName[self::node()='curation/publisher']" mode="column">
      <xsl:text>role_name</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/publisher']" mode="table">
      <xsl:text>res_role</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/publisher']" mode="roleutype">
      <xsl:text>resource.curation.publisher</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/version']" mode="column">
      <xsl:text>version</xsl:text>
   </xsl:template>

   <!-- ATTN: not in schema: how to handle?
   <xsl:template match="@xpathName[self::node()='facility/@ivo-id']" mode="column">
      <xsl:text>ivoid</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='facility/@ivo-id']" mode="table">
      <xsl:text>resource</xsl:text>
   </xsl:template>  -->

   <!-- ATTN: not in schema: how to handle?
   <xsl:template match="@xpathName[self::node()='format/@isMIMEType']" mode="column">
      <xsl:text>[Format].[@isMIMEType]</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='format/@isMIMEType']" mode="table">
      <xsl:text>Format</xsl:text>
   </xsl:template> -->

   <!-- ATTN: res_detail; handle boolean value -->
   <xsl:template match="@xpathName[self::node()='full']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='full']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='full']" mode="utype">
      <xsl:text>resource.full</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='identifier']" mode="column">
      <xsl:text>resource.ivoid</xsl:text>
   </xsl:template>

   <!-- ATTN: not in schema: how to handle?
   <xsl:template match="@xpathName[self::node()='instrument/@ivo-id']" mode="column">
      <xsl:text>[ResourceName].[@ivo-id]#[ResourceName].[@ivo-id]</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='instrument/@ivo-id']" mode="table">
      <xsl:text>ResourceName#ResourceName</xsl:text>
   </xsl:template> -->

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='managedAuthority']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='managedAuthority']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='managedAuthority']" mode="utype">
      <xsl:text>resource.managedAuthority</xsl:text>
   </xsl:template>

   <!-- ATTN: res_detail -->
   <xsl:template match="@xpathName[self::node()='managingOrg/@ivo-id']" mode="column">
      <xsl:text>detail_value</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='managingOrg/@ivo-id']" mode="table">
      <xsl:text>res_detail</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='managingOrg/@ivo-id']" mode="utype">
      <xsl:text>resource.managingOrg.ivoid</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='rights']" mode="column">
      <xsl:text>rights</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='shortName']" mode="column">
      <xsl:text>short_name</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='title']" mode="column">
      <xsl:text>res_title</xsl:text>
   </xsl:template>



   
   <!--
     -  xsitype:  a utility template that extracts the local type name 
     -             (i.e., without the namespace prefix) of the value of 
     -             the @xsi:type for the matched element
     -->
   <xsl:template mode="xsitype" match="*">
      <xsl:for-each select="@xsi:type">
         <xsl:choose>
            <xsl:when test="contains(.,':')">
               <xsl:value-of select="substring-after(.,':')"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="."/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:for-each>
   </xsl:template>

   <xsl:template match="/">
      <xsl:variable name="sql">
          <xsl:apply-templates select="ad:Where"/>
      </xsl:variable>

      <xsl:choose>
         <xsl:when test="$reporterrors and contains($sql,'[ERROR:')">
            <!-- translation error detected -->
            <xsl:call-template name="report-errors">
                <xsl:with-param name="sql" select="$sql"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <!-- no errors detected; spit out the query -->
            <xsl:value-of select="$sql"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template match="*" mode="table"/>
   
   <!--
     - ADQL Element templates
     -
     - These convert ADQL statement components into the corresponding SQL 
     - clause
     -->

   <!-- Search Types -->

   <!--
     -  Intersection Search:  a AND b
     -->
   <xsl:template match="*[@xsi:type='intersectionSearchType'] |                      *[substring-after(@xsi:type,':')='intersectionSearchType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]"/>
         <xsl:text> AND </xsl:text>
         <xsl:apply-templates select="*[2]"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='intersectionSearchType'] |                      *[substring-after(@xsi:type,':')='intersectionSearchType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="*[2]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='intersectionSearchType'] |                      *[substring-after(@xsi:type,':')='intersectionSearchType']" mode="utypes">
         <xsl:if test="not(@xsi:nil='true')">
            <xsl:apply-templates select="*[1]" mode="utypes"/>
            <xsl:apply-templates select="*[2]" mode="utypes"/>
         </xsl:if>
   </xsl:template>

   
   <!--
     -  Union: a OR b
     -->
   <xsl:template match="*[@xsi:type='unionSearchType'] |
                        *[substring-after(@xsi:type,':')='unionSearchType']">
      <xsl:if test="not(@xsi:nil='true')">

         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*[1]"/>

         <xsl:text>)</xsl:text>
         <xsl:text> OR </xsl:text>
         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*[2]"/>

         <xsl:text>)</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='unionSearchType'] |                          *[substring-after(@xsi:type,':')='unionSearchType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="boolean($forselect) and not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="*[2]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='unionSearchType'] |                          *[substring-after(@xsi:type,':')='unionSearchType']" mode="utypes">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]" mode="utypes"/>
         <xsl:apply-templates select="*[2]" mode="utypes"/>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='comparisonPredType'] |                          *[substring-after(@xsi:type,':')='comparisonPredType']">
      <xsl:param name="tbl"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg[1]">
            <xsl:with-param name="table" select="$tbl"/>
         </xsl:apply-templates>
         <xsl:text> </xsl:text>
         <xsl:value-of select="@Comparison"/>
         <xsl:text> </xsl:text>
         <xsl:apply-templates select="ad:Arg[2]">
            <xsl:with-param name="table" select="$tbl"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <!--
     -  This is a special filter for coverage/waveband='...'; handle as 
     -  a LIKE against an string array value
     -->
   <xsl:template match="*[@xsi:type='comparisonPredType' and @Comparison='=' and count(ad:Arg[@xpathName])=1 and ad:Arg/@xpathName='coverage/waveband'] | 
                        *[substring-after(@xsi:type,':')='comparisonPredType' and @Comparison='=' and count(ad:Arg[@xpathName])=1 and ad:Arg/@xpathName='coverage/waveband']" priority="1.0">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg[@xpathName]"/>
         <xsl:text> LIKE '</xsl:text>

         <xsl:variable name="val">
           <xsl:apply-templates select="ad:Arg[not(@xpathName)]"/>
         </xsl:variable>
         <xsl:value-of select='translate($val,"&#39;","%")'/>
         <xsl:text>'</xsl:text>
      </xsl:if>
   </xsl:template>

   <!--
     -  This is a special filter for content/contentLevel='...'; handle as 
     -  a LIKE against an string array value
     -->
   <xsl:template match="*[@xsi:type='comparisonPredType' and @Comparison='=' and count(ad:Arg[@xpathName])=1 and ad:Arg/@xpathName='content/contentLevel'] | 
                        *[substring-after(@xsi:type,':')='comparisonPredType' and @Comparison='=' and count(ad:Arg[@xpathName])=1 and ad:Arg/@xpathName='content/contentLevel']" priority="1.0">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg[@xpathName]"/>
         <xsl:text> LIKE '</xsl:text>

         <xsl:variable name="val">
           <xsl:apply-templates select="ad:Arg[not(@xpathName)]"/>
         </xsl:variable>
         <xsl:value-of select='translate($val,"&#39;","%")'/>
         <xsl:text>'</xsl:text>
      </xsl:if>
   </xsl:template>

   <!--
     -  This is a special filter for rights='...'; handle as 
     -  a LIKE against an string array value
     -->
   <xsl:template match="*[@xsi:type='comparisonPredType' and @Comparison='=' and count(ad:Arg[@xpathName])=1 and ad:Arg/@xpathName='rights'] | 
                        *[substring-after(@xsi:type,':')='comparisonPredType' and @Comparison='=' and count(ad:Arg[@xpathName])=1 and ad:Arg/@xpathName='rights']" priority="1.0">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg[@xpathName]"/>
         <xsl:text> LIKE '</xsl:text>

         <xsl:variable name="val">
           <xsl:apply-templates select="ad:Arg[not(@xpathName)]"/>
         </xsl:variable>
         <xsl:value-of select='translate($val,"&#39;","%")'/>
         <xsl:text>'</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='comparisonPredType'] |                          *[substring-after(@xsi:type,':')='comparisonPredType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg[1]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="ad:Arg[2]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='comparisonPredType'] |                          *[substring-after(@xsi:type,':')='comparisonPredType']" mode="utypes">

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg[1]" mode="utypes"/>
         <xsl:apply-templates select="ad:Arg[2]" mode="utypes"/>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='inverseSearchType'] |                          *[substring-after(@xsi:type,':')='inverseSearchType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:text>NOT </xsl:text>
         <xsl:apply-templates select="*"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='inverseSearchType'] |                          *[substring-after(@xsi:type,':')='inverseSearchType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='inverseSearchType'] |                          *[substring-after(@xsi:type,':')='inverseSearchType']" mode="utypes">

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*" mode="utypes"/>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='likePredType'] |  
                        *[substring-after(@xsi:type,':')='likePredType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg"/>
         <xsl:text> LIKE </xsl:text>
         <xsl:apply-templates select="ad:Pattern/ad:Literal"/>
      </xsl:if>
   </xsl:template>


   <xsl:template match="*[@xsi:type='likePredType'] |  
                        *[substring-after(@xsi:type,':')='likePredType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='likePredType'] |  
                        *[substring-after(@xsi:type,':')='likePredType']" 
                 mode="utypes">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg" mode="utypes"/>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='notLikePredType'] |  
                        *[substring-after(@xsi:type,':')='notLikePredType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg"/>
         <xsl:text> NOT LIKE </xsl:text>
         <xsl:apply-templates select="ad:Pattern/ad:Literal"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='notLikePredType'] |  
                        *[substring-after(@xsi:type,':')='notLikePredType']" 
                 mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='notLikePredType'] |  
                        *[substring-after(@xsi:type,':')='notLikePredType']" 
                 mode="utypes">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg" mode="utypes"/>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='betweenPredType'] | 
                        *[substring-after(@xsi:type,':')='betweenPredType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]"/>
         <xsl:text> BETWEEN </xsl:text>
         <xsl:apply-templates select="*[2]"/>
         <xsl:text> AND </xsl:text>
         <xsl:apply-templates select="*[3]"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='betweenPredType'] |   
                        *[substring-after(@xsi:type,':')='betweenPredType']" 
                 mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="*[2]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="*[3]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='betweenPredType'] |   
                        *[substring-after(@xsi:type,':')='betweenPredType']" 
                 mode="utypes">

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]" mode="utypes"/>
         <xsl:apply-templates select="*[2]" mode="utypes"/>
         <xsl:apply-templates select="*[3]" mode="utypes"/>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='notBetweenPredType'] |                          *[substring-after(@xsi:type,':')='notBetweenPredType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]"/>
         <xsl:text> NOT BETWEEN </xsl:text>
         <xsl:apply-templates select="*[2]"/>
         <xsl:text> AND </xsl:text>
         <xsl:apply-templates select="*[3]"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='notBetweenPredType'] |   
                        *[substring-after(@xsi:type,':')='notBetweenPredType']" 
                 mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="*[2]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="*[3]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='notBetweenPredType'] |   
                        *[substring-after(@xsi:type,':')='notBetweenPredType']" 
                 mode="utypes">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]" mode="utypes"/>
         <xsl:apply-templates select="*[2]" mode="utypes"/>
         <xsl:apply-templates select="*[3]" mode="utypes"/>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='closedSearchType'] |   
                        *[substring-after(@xsi:type,':')='closedSearchType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*"/>
         <xsl:text>)</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='closedSearchType'] |   
                        *[substring-after(@xsi:type,':')='closedSearchType']" 
                 mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='closedSearchType'] |   
                        *[substring-after(@xsi:type,':')='closedSearchType']" 
                 mode="utypes">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*" mode="utypes"/>
      </xsl:if>
   </xsl:template>

   <!-- Special handling of xsi:type criteria -->
   
   <!--
     -  @xsi:type='...' or capability@xsi:type='...'
     -->
   <xsl:template match="*[@xsi:type='comparisonPredType' and @Comparison='=' and (ad:Arg/@xpathName='@xsi:type' or ad:Arg/@xpathName='capability/@xsi:type') and contains(ad:Arg/ad:Literal/@Value,':')] |                          *[substring-after(@xsi:type,':')='comparisonPredType' and @Comparison='=' and (ad:Arg/@xpathName='@xsi:type' or ad:Arg/@xpathName='capability/@xsi:type') and contains(ad:Arg/ad:Literal/@Value,':')]" priority="10">
      <xsl:param name="tbl"/>

      <xsl:variable name="_arg1">
         <xsl:choose>
            <xsl:when test="ad:Arg[1]/ad:Literal">
               <xsl:text>'</xsl:text>
               <xsl:value-of 
                    select="substring-after(ad:Arg[1]/ad:Literal/@Value,':')"/>
               <xsl:text>'</xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:apply-templates select="ad:Arg[1]">
                  <xsl:with-param name="table" select="$tbl"/>
               </xsl:apply-templates>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="_arg2">
         <xsl:choose>
            <xsl:when test="ad:Arg[2]/ad:Literal">
               <xsl:text>'</xsl:text>
               <xsl:value-of 
                    select="substring-after(ad:Arg[2]/ad:Literal/@Value,':')"/>
               <xsl:text>'</xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:apply-templates select="ad:Arg[2]">
                  <xsl:with-param name="table" select="$tbl"/>
               </xsl:apply-templates>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:value-of select="$_arg1"/>
         <xsl:text> </xsl:text>
         <xsl:value-of select="@Comparison"/>
         <xsl:text> </xsl:text>
         <xsl:value-of select="$_arg2"/>
      </xsl:if>
   </xsl:template>

   <!--
     -  @xsi:type LIKE '...' or capability/@xsi:type LIKE '...'
     -->
   <xsl:template match="*[@xsi:type='likePredType' and (ad:Arg/@xpathName='@xsi:type' or ad:Arg/@xpathName='capability/@xsi:type') and contains(ad:Pattern/ad:Literal/@Value,':')] |  
                        *[substring-after(@xsi:type,':')='likePredType' and (ad:Arg/@xpathName='@xsi:type' or ad:Arg/@xpathName='capability/@xsi:type') and contains(ad:Pattern/ad:Literal/@Value,':')]">

      <xsl:variable name="_pat">
         <xsl:text>'</xsl:text>
         <xsl:value-of select="substring-after(ad:Pattern/ad:Literal/@Value,':')"/>
         <xsl:text>'</xsl:text>
      </xsl:variable>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg"/>
         <xsl:text> LIKE </xsl:text>
         <xsl:value-of select="$_pat"/>
      </xsl:if>
   </xsl:template>



   
   <xsl:template match="ad:Where">
      <xsl:param name="indent"/>

      <xsl:param name="subindent">
         <xsl:if test="$pretty!=''">
            <xsl:value-of select="$indent"/>
            <xsl:text>  </xsl:text>
         </xsl:if>
      </xsl:param>
      <xsl:param name="hang">
         <xsl:if test="$pretty!=''">
            <xsl:text>       </xsl:text>
         </xsl:if>
      </xsl:param>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:text>SELECT xml FROM resource WHERE </xsl:text>
         <xsl:choose>
            <xsl:when test="$usepkey">pkey</xsl:when>
            <xsl:otherwise>ivoid</xsl:otherwise>
         </xsl:choose>
         <xsl:text> in </xsl:text>
         <xsl:value-of select="$br"/><xsl:value-of select="$subindent"/>
         <xsl:text>(SELECT DISTINCT resource.</xsl:text>
         <xsl:choose>
            <xsl:when test="$usepkey">pkey</xsl:when>
            <xsl:otherwise>ivoid</xsl:otherwise>
         </xsl:choose>
         <xsl:text> </xsl:text>

         <xsl:value-of select="$br"/><xsl:value-of select="$subindent"/>
         <xsl:value-of select="$hang"/>
         <xsl:text>FROM </xsl:text>
         <xsl:value-of select="$schema"/>
         <xsl:text>.resource </xsl:text>

         <xsl:call-template name="fmt-detailJoins">
            <xsl:with-param name="indent" select="concat($subindent,$hang)"/>
         </xsl:call-template>

         <xsl:call-template name="fmt-roleJoins">
            <xsl:with-param name="indent" select="concat($subindent,$hang)"/>
         </xsl:call-template>

         <xsl:call-template name="innJoin">
            <xsl:with-param name="tables">
               <xsl:apply-templates select="ad:Condition" mode="tables">
                  <xsl:with-param name="forselect" select="true()"/>
               </xsl:apply-templates>
            </xsl:with-param>
            <xsl:with-param name="indent" select="concat($subindent,$hang)"/>
         </xsl:call-template>

         <xsl:value-of select="$br"/><xsl:value-of select="$subindent"/>
         <xsl:value-of select="$hang"/>
         <xsl:text>WHERE </xsl:text>

         <xsl:if test="not(contains(concat('#',$constrained_columns), 
                                    '#status#'))">
            <xsl:value-of select="$statusconstraint"/>
            <xsl:text> AND </xsl:text>
            <xsl:value-of select="$br"/><xsl:value-of select="$subindent"/>
            <xsl:value-of select="$hang"/>
            <xsl:value-of select="substring($hang,2)"/>
         </xsl:if>

         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="ad:Condition">
            <xsl:with-param name="indent" 
                            select="concat($subindent,$hang,$hang)"/>
         </xsl:apply-templates>
         <xsl:text>))</xsl:text>
         <xsl:value-of select="$br"/>
      </xsl:if>
   </xsl:template>

   

   
   <xsl:template match="*[@xsi:type='columnReferenceType'] | 
                       *[substring-after(@xsi:type,':')='columnReferenceType']">
      <xsl:param name="table"/>
      <xsl:param name="column">
         <xsl:apply-templates select="@xpathName" mode="column"/>
      </xsl:param>
      <xsl:param name="utype"/>
      <xsl:param name="rtype"/>
      <xsl:param name="castas">
         <xsl:call-template name="casttype">
            <xsl:with-param name="type">
               <xsl:apply-templates select="@xpathName" mode="dtype"/>
            </xsl:with-param>
         </xsl:call-template>
      </xsl:param>

      <xsl:variable name="utypehere">
         <xsl:apply-templates select="@xpathName" mode="utype"/>
      </xsl:variable>
      <xsl:variable name="roletype">
         <xsl:apply-templates select="@xpathName" mode="roleutype"/>
      </xsl:variable>

      <xsl:variable name="utp">
         <xsl:choose>
            <xsl:when test="$utype!=''">
               <xsl:value-of select="$utype"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="$utypehere"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="rtp">
         <xsl:choose>
            <xsl:when test="$rtype!=''">
               <xsl:value-of select="$rtype"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="$roletype"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="tbl">
         <xsl:choose>
            <xsl:when test="$table!=''">
               <xsl:value-of select="$table"/>
            </xsl:when>
            <xsl:when test="$utp!=''">
               <xsl:call-template name="asname-for">
                  <xsl:with-param name="utype" select="$utp"/>
               </xsl:call-template>
            </xsl:when>
            <xsl:when test="$rtp!=''">
               <xsl:call-template name="asname-for">
                  <xsl:with-param name="utype" select="$rtp"/>
                  <xsl:with-param name="utypes" select="$role_utypes"/>
                  <xsl:with-param name="asnames">
                     <xsl:call-template name="gen-asnames">
                        <xsl:with-param name="list" select="$role_utypes"/>
                        <xsl:with-param name="base">rl</xsl:with-param>
                     </xsl:call-template>
                  </xsl:with-param>
               </xsl:call-template>
            </xsl:when>
         </xsl:choose>
      </xsl:variable>

      <xsl:variable name="col">
         <xsl:choose>
            <xsl:when test="starts-with($tbl,'rd')">
               <!-- we're in the main query and this is a res_detail item;
                    select from the subquery results using manufactured cols -->
               <xsl:value-of select="translate($utp,'.','_')"/>
            </xsl:when>
            <xsl:when test="starts-with($tbl,'rl')">
               <!-- we're in the main query and this is a res_role item;
                    select from the subquery results using manufactured cols -->
               <xsl:call-template name="lastfield">
                  <xsl:with-param name="seq" select="$rtp"/>
               </xsl:call-template>
               <xsl:text>_</xsl:text>
               <xsl:call-template name="lastfield">
                  <xsl:with-param name="seq" select="$column"/>
                  <xsl:with-param name="delim">_</xsl:with-param>
               </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="$column"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:if test="$castas!=''">CAST(</xsl:if>
         <xsl:if test="$tbl!=''">
            <xsl:value-of select="$tbl"/>
            <xsl:text>.</xsl:text>
         </xsl:if>
         <xsl:if test="$col=''">
            <xsl:call-template name="error">
               <xsl:with-param name="msg">
             <xsl:text>Unspecified column name (missing @xpathName?)</xsl:text>
               </xsl:with-param>
            </xsl:call-template>
         </xsl:if>
         <xsl:value-of select="$col"/>
         <xsl:if test="$castas!=''">
            <xsl:text> AS </xsl:text>
            <xsl:value-of select="$castas"/>
            <xsl:text>)</xsl:text>
         </xsl:if>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='columnReferenceType'] |  
                        *[substring-after(@xsi:type,':')='columnReferenceType']"
                 mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates mode="tables" select="@xpathName"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='columnReferenceType'] |  
                        *[substring-after(@xsi:type,':')='columnReferenceType']"
                 mode="utypes">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates mode="utypes" select="@xpathName"/>
      </xsl:if>
   </xsl:template>


   <xsl:template match="*[@xsi:type='unaryExprType'] | 
                        *[substring-after(@xsi:type,':')='unaryExprType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg"/>
         <xsl:text> </xsl:text>
         <xsl:value-of select="@Oper"/>
         <xsl:text> </xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='unaryExprType'] | 
                        *[substring-after(@xsi:type,':')='unaryExprType']" 
                 mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='binaryExprType'] | 
                        *[substring-after(@xsi:type,':')='binaryExprType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg[1]"/>
         <xsl:text> </xsl:text>
         <xsl:value-of select="@Oper"/>
         <xsl:text> </xsl:text>
         <xsl:apply-templates select="ad:Arg[2]"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='binaryExprType'] | 
                        *[substring-after(@xsi:type,':')='binaryExprType']" 
                 mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg[1]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
         <xsl:apply-templates select="ad:Arg[2]" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='atomType'] |  
                        *[substring-after(@xsi:type,':')='atomType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*"/>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='closedExprType'] | 
                        *[substring-after(@xsi:type,':')='closedExprType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*"/>
         <xsl:text>)</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='closedExprType'] | 
                        *[substring-after(@xsi:type,':')='closedExprType']" 
                 mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="ad:Function">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:value-of select="*[1]"/>
         <xsl:text>(</xsl:text>
         <xsl:choose>
            <xsl:when test="ad:Allow[position()=2]">
               <xsl:apply-templates select="*[2]/@Option"/>
               <xsl:text> </xsl:text>
               <xsl:apply-templates select="*[3]"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:apply-templates select="*[2]"/>
            </xsl:otherwise>
         </xsl:choose>
         <xsl:text>)</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="ad:Function" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:choose>
            <xsl:when test="ad:Allow[position()=2]">
               <xsl:apply-templates select="*[3]" mode="tables">
                  <xsl:with-param name="forselect" select="$forselect"/>
               </xsl:apply-templates>
            </xsl:when>
            <xsl:otherwise>
               <xsl:apply-templates select="*[2]" mode="tables">
                  <xsl:with-param name="forselect" select="$forselect"/>
               </xsl:apply-templates>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type = 'trigonometricFunctionType'] | 
                 *[substring-after(@xsi:type,':')='trigonometricFunctionType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:value-of select="@Name"/>
         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*"/>
         <xsl:text>)</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type = 'trigonometricFunctionType'] | 
                  *[substring-after(@xsi:type,':')='trigonometricFunctionType']"
                 mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type = 'mathFunctionType'] |                          *[substring-after(@xsi:type,':')='mathFunctionType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:value-of select="@Name"/>
         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*"/>
         <xsl:text>)</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type = 'mathFunctionType'] | 
                        *[substring-after(@xsi:type,':')='mathFunctionType']" 
                 mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type = 'aggregateFunctionType'] |                       *[substring-after(@xsi:type,':')='aggregateFunctionType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:value-of select="@Name"/>
         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*"/>
         <xsl:text>)</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type = 'aggregateFunctionType'] |  
                     *[substring-after(@xsi:type,':')='aggregateFunctionType']" 
                 mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='integerType'] | 
                        *[substring-after(@xsi:type,':')='integerType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:value-of select="@Value"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='realType'] | 
                        *[substring-after(@xsi:type,':')='realType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:value-of select="@Value"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='stringType'] | 
                        *[substring-after(@xsi:type,':')='stringType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:text>'</xsl:text>
         <xsl:value-of select="@Value"/>
         <xsl:text>'</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template mode="dtype" match="@xpathName" />
   <xsl:template mode="table" match="@xpathName">resource</xsl:template>
   <xsl:template mode="tables" match="*" />

   <!--
     -  create a subquery select that generates a table of relevent details
     -->
   <xsl:template name="fmt-detailJoins">
      <xsl:param name="utypes" select="$detail_utypes"/>
      <xsl:param name="asnames">
         <xsl:call-template name="gen-asnames">
            <xsl:with-param name="list" select="$utypes"/>
            <xsl:with-param name="base">rd</xsl:with-param>
         </xsl:call-template>
      </xsl:param>
      <xsl:param name="indent"/>

      <xsl:if test="contains($utypes,'#') and contains($asnames,'#')">
         <xsl:variable name="frstu" select="substring-before($utypes,'#')"/>
         <xsl:variable name="restu" select="substring-after($utypes,'#')"/>

         <xsl:choose>
            <xsl:when test="not(contains(concat('#',$restu), 
                                         concat('#',$frstu,'#')))">
              <xsl:call-template name="detailJoin">
                 <xsl:with-param name="utype" select="$frstu"/>
                 <xsl:with-param name="asname" 
                                 select="substring-before($asnames,'#')"/>
                 <xsl:with-param name="indent" select="$indent"/>
              </xsl:call-template>

              <xsl:call-template name="fmt-detailJoins">
                 <xsl:with-param name="utypes" select="$restu"/>
                 <xsl:with-param name="asnames"
                                 select="substring-after($asnames,'#')"/>
                 <xsl:with-param name="indent" select="$indent"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="fmt-detailJoins">
                 <xsl:with-param name="utypes" select="$restu"/>
                 <xsl:with-param name="indent" select="$indent"/>
                 <xsl:with-param name="asnames" select="$asnames"/>
              </xsl:call-template>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>
   </xsl:template>

   <!--
     -  format an INNER JOIN clause for a particular res_detail item.
     -->
   <xsl:template name="detailJoin">
      <xsl:param name="utype"/>
      <xsl:param name="asname"/>
      <xsl:param name="constraint"/>
      <xsl:param name="indent"/>

      <xsl:param name="utypec" select="translate($utype,'.','_')"/>

      <xsl:param name="hang">
         <xsl:if test="$pretty!=''">
            <xsl:value-of select="$indent"/>
            <xsl:text>            </xsl:text>
         </xsl:if>
      </xsl:param>

      <xsl:if test="$utype!='' and $asname!=''">
         <xsl:value-of select="$br"/>
         <xsl:value-of select="$indent"/>
         <xsl:text>INNER JOIN (SELECT </xsl:text>
         <xsl:choose>
            <xsl:when test="$usepkey">
               <xsl:text>rkey</xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:text>ivoid</xsl:text>
            </xsl:otherwise>
         </xsl:choose>
         <xsl:text>, detail_value </xsl:text>
         <xsl:value-of select="$utypec"/>
         <xsl:text> </xsl:text>

         <xsl:value-of select="$br"/>
         <xsl:value-of select="$hang"/>
         <xsl:text>FROM res_detail </xsl:text>

         <xsl:value-of select="$br"/>
         <xsl:value-of select="$hang"/>
         <xsl:text>WHERE </xsl:text>
         <xsl:if test="$constraint!=''">(</xsl:if>
         <xsl:text>detail_utype='</xsl:text>
         <xsl:if test="$detailns!=''">
            <xsl:value-of select="$detailns"/>
            <xsl:text>:</xsl:text>
         </xsl:if>
         <xsl:value-of select="$utype"/>
         <xsl:text>'</xsl:text>

         <xsl:if test="$constraint!=''">
            <xsl:text> AND </xsl:text>
            <xsl:value-of select="$br"/><xsl:value-of select="$hang"/>
            <xsl:if test="$pretty"><xsl:text>       </xsl:text></xsl:if>
            <xsl:value-of select="$constraint"/>
            <xsl:text>)</xsl:text>
         </xsl:if>
         <xsl:text>) AS </xsl:text>
         <xsl:value-of select="$asname"/>
         <xsl:value-of select="$br"/>
         <xsl:value-of select="substring($hang,7)"/>
         <xsl:choose>
            <xsl:when test="$usepkey">
               <xsl:text>ON (resource.pkey = </xsl:text>
               <xsl:value-of select="$asname"/>
               <xsl:text>.rkey)</xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:text>ON (resource.ivoid = </xsl:text>
               <xsl:value-of select="$asname"/>
               <xsl:text>.ivoid)</xsl:text>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>
   </xsl:template>
   
   <!--
     -  create a subquery select that generates a table of relevent details
     -->
   <xsl:template name="fmt-roleJoins">
      <xsl:param name="utypes" select="$role_utypes"/>
      <xsl:param name="asnames">
         <xsl:call-template name="gen-asnames">
            <xsl:with-param name="list" select="$utypes"/>
            <xsl:with-param name="base">rl</xsl:with-param>
         </xsl:call-template>
      </xsl:param>
      <xsl:param name="indent"/>

      <xsl:if test="contains($utypes,'#') and contains($asnames,'#')">
         <xsl:variable name="frstu" select="substring-before($utypes,'#')"/>
         <xsl:variable name="restu" select="substring-after($utypes,'#')"/>

         <xsl:choose>
            <xsl:when test="not(contains(concat('#',$restu), 
                                         concat('#',$frstu,'#')))">
              <xsl:call-template name="roleJoin">
                 <xsl:with-param name="utype" select="$frstu"/>
                 <xsl:with-param name="asname" 
                                 select="substring-before($asnames,'#')"/>
                 <xsl:with-param name="indent" select="$indent"/>
              </xsl:call-template>

              <xsl:call-template name="fmt-roleJoins">
                 <xsl:with-param name="utypes" select="$restu"/>
                 <xsl:with-param name="asnames"
                                 select="substring-after($asnames,'#')"/>
                 <xsl:with-param name="indent" select="$indent"/>
              </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
              <xsl:call-template name="fmt-roleJoins">
                 <xsl:with-param name="utypes" select="$restu"/>
                 <xsl:with-param name="indent" select="$indent"/>
                 <xsl:with-param name="asnames" select="$asnames"/>
              </xsl:call-template>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>
   </xsl:template>

   <!--
     -  format an INNER JOIN clause for a particular res_role item.
     -->
   <xsl:template name="roleJoin">
      <xsl:param name="utype"/>
      <xsl:param name="asname"/>
      <xsl:param name="constraint"/>
      <xsl:param name="indent"/>

      <xsl:param name="utypec">
         <xsl:call-template name="lastfield">
            <xsl:with-param name="seq" select="$utype"/>
         </xsl:call-template>
      </xsl:param>

      <xsl:param name="hang">
         <xsl:if test="$pretty!=''">
            <xsl:value-of select="$indent"/>
            <xsl:text>            </xsl:text>
         </xsl:if>
      </xsl:param>
      <xsl:variable name="collist" select="concat('#',$constrained_columns)"/>

      <xsl:if test="$utype!='' and $asname!=''">
         <xsl:value-of select="$br"/>
         <xsl:value-of select="$indent"/>
         <xsl:text>INNER JOIN (SELECT </xsl:text>
         <xsl:choose>
            <xsl:when test="$usepkey">
               <xsl:text>rkey</xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:text>ivoid</xsl:text>
            </xsl:otherwise>
         </xsl:choose>
         <xsl:text>, role_name </xsl:text>
         <xsl:value-of select="$utypec"/>
         <xsl:text>_name, </xsl:text>
         <xsl:value-of select="$br"/><xsl:value-of select="$hang"/>
         <xsl:if test="$pretty"><xsl:text>             </xsl:text></xsl:if>
         <xsl:text>role_ivoid </xsl:text>
         <xsl:value-of select="$utypec"/>
         <xsl:text>_ivoid</xsl:text>

         <xsl:choose>
            <xsl:when test="$utypec='creator' and 
                            contains($collist, '#logo#')">
               <xsl:text>, </xsl:text>
               <xsl:text>logo creator_logo</xsl:text>
            </xsl:when>
            <xsl:when test="$utypec='contact' and 
                            contains($collist, '#address#')">
               <xsl:text>, </xsl:text>
               <xsl:text>address contact</xsl:text>
            </xsl:when>
            <xsl:when test="$utypec='contact' and 
                            contains($collist, '#email#')">
               <xsl:text>, </xsl:text>
               <xsl:text>email contact_email</xsl:text>
            </xsl:when>
            <xsl:when test="$utypec='contact' and 
                            contains($collist, '#telephone#')">
               <xsl:text>, </xsl:text>
               <xsl:text>telephone contact_telephone</xsl:text>
            </xsl:when>
         </xsl:choose>
         <xsl:text> </xsl:text>

         <xsl:value-of select="$br"/>
         <xsl:value-of select="$hang"/>
         <xsl:text>FROM res_role </xsl:text>

         <xsl:value-of select="$br"/>
         <xsl:value-of select="$hang"/>
         <xsl:text>WHERE </xsl:text>
         <xsl:if test="$constraint!=''">(</xsl:if>
         <xsl:text>base_utype='</xsl:text>
         <xsl:if test="$detailns!=''">
            <xsl:value-of select="$detailns"/>
            <xsl:text>:</xsl:text>
         </xsl:if>
         <xsl:value-of select="$utype"/>
         <xsl:text>'</xsl:text>

         <xsl:if test="$constraint!=''">
            <xsl:text> AND </xsl:text>
            <xsl:value-of select="$br"/><xsl:value-of select="$hang"/>
            <xsl:if test="$pretty"><xsl:text>       </xsl:text></xsl:if>
            <xsl:value-of select="$constraint"/>
            <xsl:text>)</xsl:text>
         </xsl:if>
         <xsl:text>) AS </xsl:text>
         <xsl:value-of select="$asname"/>
         <xsl:value-of select="$br"/>
         <xsl:value-of select="substring($hang,7)"/>
         <xsl:choose>
            <xsl:when test="$usepkey">
               <xsl:text>ON (resource.pkey = </xsl:text>
               <xsl:value-of select="$asname"/>
               <xsl:text>.rkey)</xsl:text>
            </xsl:when>
            <xsl:otherwise>
               <xsl:text>ON (resource.ivoid = </xsl:text>
               <xsl:value-of select="$asname"/>
               <xsl:text>.ivoid)</xsl:text>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>
   </xsl:template>
   

   <!--
     -  generate a list of utype column names for the res_detail table needed 
     -  by the res_detail subquery
     -->
   <xsl:template name="gen-details-colnames">
      <xsl:param name="utypes" select="$detail_utypes"/>

      <xsl:if test="contains($utypes,'#')">
         <xsl:variable name="utp" select="substring-before($utypes,'#')"/>
         <xsl:variable name="rest" select="substring-after($utypes,'#')"/>

         <xsl:if test="$utp!='' and 
                       not(contains(concat('#',$rest), concat('#',$utp,'#')))">
            <xsl:value-of select="translate($utp,'.','_')"/>
            <xsl:text>#</xsl:text>
         </xsl:if>

         <xsl:if test="contains($rest,'#')">
            <xsl:call-template name="gen-details-colnames">
               <xsl:with-param name="utypes" select="$rest"/>
            </xsl:call-template>
         </xsl:if>
      </xsl:if>
   </xsl:template>

   <!--
     -  count the number of unique items in the given list
     -->
   <xsl:template name="unique-count-items">
      <xsl:param name="list"/>
      <xsl:param name="count" select="0"/>

      <xsl:choose>
         <xsl:when test="contains($list,'#')">
            <xsl:variable name="first" select="substring-before($list,'#')"/>
            <xsl:variable name="rest" select="substring-after($list,'#')"/>

            <xsl:choose>
               <xsl:when test="$first!='' and 
                      not(contains(concat('#',$rest), concat('#',$first,'#')))">
                   <xsl:call-template name="unique-count-items">
                      <xsl:with-param name="list" select="$rest"/>
                      <xsl:with-param name="count" select="number($count)+1"/>
                   </xsl:call-template>
               </xsl:when>
               <xsl:otherwise>
                   <xsl:call-template name="unique-count-items">
                      <xsl:with-param name="list" select="$rest"/>
                      <xsl:with-param name="count" select="$count"/>
                   </xsl:call-template>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$count"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  return the last field in a sequence given as delimited string
     -->
   <xsl:template name="lastfield">
      <xsl:param name="seq"/>
      <xsl:param name="delim">.</xsl:param>

      <xsl:choose>
         <xsl:when test="contains($seq,$delim)">
            <xsl:call-template name="lastfield">
               <xsl:with-param name="seq" 
                               select="substring-after($seq,$delim)"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="$seq"/></xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   
   <!--
     -  generate a list of res_detail table aliases needed 
     -  by the res_detail subquery
     -->
   <xsl:template name="gen-asnames">
      <xsl:param name="list"/>
      <xsl:param name="base">rd</xsl:param>
      <xsl:param name="count" select="0"/>

      <xsl:if test="contains($list,'#')">
         <xsl:variable name="utp" select="substring-before($list,'#')"/>
         <xsl:variable name="rest" select="substring-after($list,'#')"/>

         <xsl:if test="$utp!='' and 
                       not(contains(concat('#',$rest), concat('#',$utp,'#')))">
            <xsl:value-of select="$base"/>
            <xsl:value-of select="$count"/>
            <xsl:text>#</xsl:text>
         </xsl:if>

         <xsl:if test="contains($rest,'#')">
            <xsl:call-template name="gen-asnames">
               <xsl:with-param name="list" select="$rest"/>
               <xsl:with-param name="base" select="$base"/>
               <xsl:with-param name="count" select="number($count)+1"/>
            </xsl:call-template>
         </xsl:if>
      </xsl:if>
   </xsl:template>

   <!--
     -  provide the table alias that should be used for selectng res_detail
     -  values with a given utype
     -->
   <xsl:template name="asname-for">
      <xsl:param name="utype"/>
      <xsl:param name="utypes" select="$detail_utypes"/>
      <xsl:param name="asnames">
         <xsl:call-template name="gen-asnames">
            <xsl:with-param name="list" select="$utypes"/>
            <xsl:with-param name="base">rd</xsl:with-param>
         </xsl:call-template>
      </xsl:param>

      <xsl:variable name="frstu" select="substring-before($utypes,'#')"/>
      <xsl:variable name="restu" select="substring-after($utypes,'#')"/>

      <xsl:choose>
        <xsl:when test="$utypes='' or $asnames=''">
          <xsl:call-template name="error">
             <xsl:with-param name="msg">
                <xsl:text>Unexpected translation error (</xsl:text>
                <xsl:text>empty utypes or as-name list</xsl:text>
                <xsl:text>)</xsl:text>
             </xsl:with-param>
          </xsl:call-template>
        </xsl:when>
        <xsl:when test="contains(concat('#',$restu), concat('#',$frstu,'#'))">
           <xsl:call-template name="asname-for">
              <xsl:with-param name="utype" select="$utype"/>
              <xsl:with-param name="utypes" select="$restu"/>
              <xsl:with-param name="asnames" select="$asnames"/>
           </xsl:call-template>
        </xsl:when>
        <xsl:when test="$utype=$frstu">
           <xsl:value-of select="substring-before($asnames,'#')"/>
        </xsl:when>
        <xsl:otherwise>
           <!-- $utype is an expected one but not the lead one -->
           <xsl:call-template name="asname-for">
              <xsl:with-param name="utype" select="$utype"/>
              <xsl:with-param name="utypes" select="$restu"/>
              <xsl:with-param name="asnames" 
                              select="substring-after($asnames,'#')"/>
           </xsl:call-template>
        </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  list the utypes of items being queried from the res_details table.
     -  We need versions for each type of searchType that can include a 
     -  column reference.
     -
   <xsl:template mode="utypes" match="*">
      <xsl:apply-templates select="*" mode="utypes"/>
   </xsl:template>
     -->
   <xsl:template mode="utypes" match="*"/>
   <xsl:template mode="utype" match="@xpathName"/>

   <!--
     -  list the utypes of items being queried from the res_details table.
     -  We need versions for each type of searchType that can include a 
     -  column reference.
     -
     -->
   <xsl:template mode="roleutypes" match="*">
      <xsl:apply-templates select="*" mode="roleutypes"/>
   </xsl:template>
   <xsl:template mode="roleutypes" match="*[@xpathName]">
      <xsl:apply-templates select="@xpathName" mode="roleutypes"/>
   </xsl:template>
   <xsl:template mode="roleutype" match="@xpathName"/>

   <!--
     -  list the columns being queried within the search constraints
     -->
   <xsl:template mode="columns" match="*">
      <xsl:apply-templates select="*" mode="columns"/>
   </xsl:template>
   <xsl:template mode="columns" match="*[@xpathName]">
      <xsl:apply-templates select="@xpathName" mode="columns"/>
   </xsl:template>
   <xsl:template mode="column" match="@xpathName">
      <xsl:call-template name="error">
         <xsl:with-param name="msg">
            <xsl:choose>
               <xsl:when test=".=''">
             <xsl:text>Missing resource metadatum name (@xpathName)</xsl:text>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:text>resource metadatum name not supported: </xsl:text>
                  <xsl:value-of select="."/>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:with-param>
      </xsl:call-template>
   </xsl:template>

   <!--
     -  extend the list of tables that should appear in the FROM clause
     -  to include tables to be joined with the Resource table.
     -->
   <xsl:template name="extendFrom">
      <xsl:param name="tables"/>

      <xsl:if test="contains($tables,'#')">
         <xsl:variable name="tbl" select="substring-before($tables,'#')"/>
         <xsl:variable name="rest" select="substring-after($tables,'#')"/>

         <xsl:if test="$tbl!='' and $tbl!='Resource' and 
                       not(contains(concat('#',$rest), concat('#',$tbl,'#')))">
            <xsl:text>, [</xsl:text>
            <xsl:value-of select="$tbl"/>
            <xsl:text>] [</xsl:text>
            <xsl:value-of select="$tbl"/>
            <xsl:text>]</xsl:text>
         </xsl:if>

         <xsl:if test="contains($rest,'#')">
            <xsl:call-template name="extendFrom">
               <xsl:with-param name="tables" select="$rest"/>
            </xsl:call-template>
         </xsl:if>
      </xsl:if>
   </xsl:template>

   <xsl:template name="innJoin">
      <xsl:param name="tables" select="''"/>
      <xsl:param name="indent"/>

      <xsl:if test="contains($tables,'#')">
         <xsl:variable name="tbl" select="substring-before($tables,'#')"/>
         <xsl:variable name="rest" select="substring-after($tables,'#')"/>

         <xsl:if test="$tbl!='' and $tbl!='resource' and 
                       $tbl!='res_detail' and $tbl!='res_role' and
                       not(contains(concat('#',$rest), concat('#',$tbl,'#')))">
            <xsl:value-of select="$br"/>
            <xsl:value-of select="$indent"/>
            <xsl:text>INNER JOIN </xsl:text>
            <xsl:value-of select="$schema"/>
            <xsl:text>.</xsl:text>
            <xsl:value-of select="$tbl"/>
            <xsl:choose>
               <xsl:when test="$usepkey">
                  <xsl:text> ON (resource.pkey = </xsl:text>
                  <xsl:value-of select="$tbl"/>
                  <xsl:text>.rkey)</xsl:text>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:text> ON (resource.ivoid = </xsl:text>
                  <xsl:value-of select="$tbl"/>
                  <xsl:text>.ivoid)</xsl:text>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:if>

         <xsl:if test="contains($rest,'#')">
            <xsl:call-template name="innJoin">
               <xsl:with-param name="tables" select="$rest"/>
               <xsl:with-param name="indent" select="$indent"/>
               <xsl:with-param name="br"     select="$br"/>
            </xsl:call-template>
         </xsl:if>
      </xsl:if>
      
   </xsl:template>

   <xsl:template name="makeJoin">
      <xsl:param name="tables" select="''"/>

      <xsl:if test="contains($tables,'#')">
         <xsl:variable name="tbl" select="substring-before($tables,'#')"/>
         <xsl:variable name="rest" select="substring-after($tables,'#')"/>

         <xsl:if test="$tbl!='' and 
                       not(contains(concat('#',$rest), concat('#',$tbl,'#')))">
            <xsl:text> AND </xsl:text>
            <xsl:value-of select="$tbl"/>
            <xsl:text>.rkey=resource.pkey</xsl:text>
         </xsl:if>

         <xsl:if test="contains($rest,'#')">
            <xsl:call-template name="makeJoin">
               <xsl:with-param name="tables" select="$rest"/>
            </xsl:call-template>
         </xsl:if>
      </xsl:if>
      
   </xsl:template>

   <!--
     -  return an appropriate CAST type argument for a given type
     -->
   <xsl:template name="casttype">
      <xsl:param name="type"/>

      <xsl:choose>
         <xsl:when test="$type=''"></xsl:when>
         <xsl:when test="$type='int' and $intcasttype!=''">
            <xsl:value-of select="$intcasttype"/>
         </xsl:when>
         <xsl:when test="$type='float' and $floatcasttype!=''">
            <xsl:value-of select="$floatcasttype"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$type"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  extract ERROR messages from text and format them for output
     -->
   <xsl:template name="report-errors">
      <xsl:param name="sql"/>
      <xsl:if test="contains($sql,'[ERROR:')">
         <xsl:variable name="start" select="substring-after($sql,'[ERROR:')"/>
         <xsl:variable name="msg" select="substring-before($start,']')"/>
         <xsl:variable name="rest" select="substring-after($start,']')"/>

         <xsl:text>-- ERROR: </xsl:text>
         <xsl:value-of select="normalize-space($msg)"/>
         <xsl:text>
</xsl:text>

         <xsl:call-template name="report-errors">
            <xsl:with-param name="sql" select="$rest"/>
         </xsl:call-template>
      </xsl:if>
   </xsl:template>

   <!--
     -  format an error pragma
     -->
   <xsl:template name="error">
      <xsl:param name="msg" select="' '"/>
      <xsl:text>[ERROR: </xsl:text>
      <xsl:value-of select="normalize-space($msg)"/>
      <xsl:text>]</xsl:text>
   </xsl:template>

 </xsl:stylesheet>
