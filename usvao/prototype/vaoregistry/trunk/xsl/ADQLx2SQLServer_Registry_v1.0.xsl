<?xml version="1.0" encoding="UTF-8"?>
<!-- 
  - Stylesheet to convert ADQL version 1.0 to an SQL String 
  - This stylesheet was created automatically from mkquery.xsl and is 
  - based on stylesheets from ADQLlib Version 1.1 
  -   updated by Ray Plante (NCSA) updated for ADQLlib
  - Based on v1.0 by Ramon Williamson, NCSA (April 1, 2004)
  - Based on the schema: http://www.ivoa.net/xml/ADQL/v1.0
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:ad="http://www.ivoa.net/xml/ADQL/v1.0" version="1.0">

   <xsl:output method="text"/>

   

   <!--
     - Mapping Templates
     -
     - These templates map xpathName identifiers in the ADQL query to 
     - column names in the data base.  
     -->

   <xsl:template match="@xpathName[self::node()='@created']" mode="columns">
      <xsl:text>[Resource].[@created]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='@status']" mode="columns">
      <xsl:text>[Resource].[@status]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='@updated']" mode="columns">
      <xsl:text>[Resource].[@updated]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='@xsi:type']" mode="columns">
      <xsl:text>[Resource].[xsi_type]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='accessURL/@use']" mode="columns">
      <xsl:text>[DataCollection].[accessURL/@use]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='accessURL/@use']" mode="tables">
      <xsl:text>DataCollection#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/@standardID']" mode="columns">
      <xsl:text>[Capability].[@standardID]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/@standardID']" mode="tables">
      <xsl:text>Capability#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/@xsi:type']" mode="columns">
      <xsl:text>[Capability].[xsi_type]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/@xsi:type']" mode="tables">
      <xsl:text>Capability#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/compliance']" mode="columns">
      <xsl:text>[OpenSkyNode].[compliance]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/compliance']" mode="tables">
      <xsl:text>OpenSkyNode#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/description']" mode="columns">
      <xsl:text>[Capability].[description]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/description']" mode="tables">
      <xsl:text>Capability#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/extensionSearchSupport']" mode="columns">
      <xsl:text>[Search].[extensionSearchSupport]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/extensionSearchSupport']" mode="tables">
      <xsl:text>Search#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/imageServiceType']" mode="columns">
      <xsl:text>[SimpleImageAccess].[imageServiceType]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/imageServiceType']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/@role']" mode="columns">
      <xsl:text>[Interface].[@role]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/@role']" mode="tables">
      <xsl:text>Interface#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/@version']" mode="columns">
      <xsl:text>[Interface].[@version]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/@version']" mode="tables">
      <xsl:text>Interface#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/@xsi:type']" mode="columns">
      <xsl:text>[Interface].[xsi_type]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/@xsi:type']" mode="tables">
      <xsl:text>Interface#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/accessURL/@use']" mode="columns">
      <xsl:text>[AccessURL].[@use]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/accessURL/@use']" mode="tables">
      <xsl:text>AccessURL#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/param/@std']" mode="columns">
      <xsl:text>[InputParam].[@std]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/param/@std']" mode="tables">
      <xsl:text>InputParam#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/param/@use']" mode="columns">
      <xsl:text>[InputParam].[@use]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/param/@use']" mode="tables">
      <xsl:text>InputParam#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/param/dataType/@arraysize']" mode="columns">
      <xsl:text>[InputParam].[dataType/@arraysize]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/param/dataType/@arraysize']" mode="tables">
      <xsl:text>InputParam#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/queryType']" mode="columns">
      <xsl:text>[ParamHTTP].[queryType]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/queryType']" mode="tables">
      <xsl:text>ParamHTTP#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/resultType']" mode="columns">
      <xsl:text>[ParamHTTP].[resultType]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/resultType']" mode="tables">
      <xsl:text>ParamHTTP#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/securityMethod/@standardID']" mode="columns">
      <xsl:text>[SecurityMethod].[@standardID]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/securityMethod/@standardID']" mode="tables">
      <xsl:text>SecurityMethod#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/interface/wsdlURL']" mode="columns">
      <xsl:text>[WebService].[wsdlURL]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/interface/wsdlURL']" mode="tables">
      <xsl:text>WebService#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/latitude']" mode="columns">
      <xsl:text>[OpenSkyNode].[latitude]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/latitude']" mode="tables">
      <xsl:text>OpenSkyNode#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/longitude']" mode="columns">
      <xsl:text>[OpenSkyNode].[longitude]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/longitude']" mode="tables">
      <xsl:text>OpenSkyNode#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/maxFileSize']" mode="columns">
      <xsl:text>[SimpleImageAccess].[maxFileSize]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxFileSize']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/lat']" mode="columns">
      <xsl:text>[SimpleImageAccess].[maxImageExtent/lat]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/lat']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/long']" mode="columns">
      <xsl:text>[SimpleImageAccess].[maxImageExtent/long]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageExtent/long']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/lat']" mode="columns">
      <xsl:text>[SimpleImageAccess].[maxImageSize/lat]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/lat']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/long']" mode="columns">
      <xsl:text>[SimpleImageAccess].[maxImageSize/long]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxImageSize/long']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/maxQueryRegionSize/lat']" mode="columns">
      <xsl:text>[SimpleImageAccess].[maxQueryRegionSize/lat]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxQueryRegionSize/lat']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/maxQueryRegionSize/long']" mode="columns">
      <xsl:text>[SimpleImageAccess].[maxQueryRegionSize/long]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxQueryRegionSize/long']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/maxRecords']" mode="columns">
      <xsl:text>[Harvest].[maxRecords]#[Search].[maxRecords]#[ConeSearch].[maxRecords]#[SimpleImageAccess].[maxRecords]#[OpenSkyNode].[maxRecords]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxRecords']" mode="tables">
      <xsl:text>Harvest#Search#ConeSearch#SimpleImageAccess#OpenSkyNode#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/maxSR']" mode="columns">
      <xsl:text>[ConeSearch].[maxSR]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/maxSR']" mode="tables">
      <xsl:text>ConeSearch#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/optionalProtocol']" mode="columns">
      <xsl:text>[Search].[optionalProtocol]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/optionalProtocol']" mode="tables">
      <xsl:text>Search#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/primaryKey']" mode="columns">
      <xsl:text>[OpenSkyNode].[primaryKey]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/primaryKey']" mode="tables">
      <xsl:text>OpenSkyNode#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/primaryTable']" mode="columns">
      <xsl:text>[OpenSkyNode].[primaryTable]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/primaryTable']" mode="tables">
      <xsl:text>OpenSkyNode#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/testQuery/catalog']" mode="columns">
      <xsl:text>[ConeSearch].[testQuery/catalog]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/catalog']" mode="tables">
      <xsl:text>ConeSearch#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/testQuery/dec']" mode="columns">
      <xsl:text>[ConeSearch].[testQuery/dec]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/dec']" mode="tables">
      <xsl:text>ConeSearch#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/testQuery/extras']" mode="columns">
      <xsl:text>[ConeSearch].[testQuery/extras]#[SimpleImageAccess].[testQuery/extras]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/extras']" mode="tables">
      <xsl:text>ConeSearch#SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/testQuery/pos/lat']" mode="columns">
      <xsl:text>[SimpleImageAccess].[testQuery/pos/lat]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/pos/lat']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/testQuery/pos/long']" mode="columns">
      <xsl:text>[SimpleImageAccess].[testQuery/pos/long]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/pos/long']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/testQuery/ra']" mode="columns">
      <xsl:text>[ConeSearch].[testQuery/ra]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/ra']" mode="tables">
      <xsl:text>ConeSearch#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/lat']" mode="columns">
      <xsl:text>[SimpleImageAccess].[testQuery/size/lat]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/lat']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/long']" mode="columns">
      <xsl:text>[SimpleImageAccess].[testQuery/size/long]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/size/long']" mode="tables">
      <xsl:text>SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/testQuery/sr']" mode="columns">
      <xsl:text>[ConeSearch].[testQuery/sr]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/sr']" mode="tables">
      <xsl:text>ConeSearch#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/testQuery/verb']" mode="columns">
      <xsl:text>[ConeSearch].[testQuery/verb]#[SimpleImageAccess].[testQuery/verb]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/testQuery/verb']" mode="tables">
      <xsl:text>ConeSearch#SimpleImageAccess#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='capability/verbosity']" mode="columns">
      <xsl:text>[ConeSearch].[verbosity]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='capability/verbosity']" mode="tables">
      <xsl:text>ConeSearch#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='catalog/description']" mode="columns">
      <xsl:text>[Catalog].[description]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/description']" mode="tables">
      <xsl:text>Catalog#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='catalog/name']" mode="columns">
      <xsl:text>[Catalog].[name]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/name']" mode="tables">
      <xsl:text>Catalog#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='catalog/table/@role']" mode="columns">
      <xsl:text>[Table].[@role]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/table/@role']" mode="tables">
      <xsl:text>Table#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='catalog/table/column/@std']" mode="columns">
      <xsl:text>[TableParam].[@std]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/table/column/@std']" mode="tables">
      <xsl:text>TableParam#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='catalog/table/column/dataType/@arraysize']" mode="columns">
      <xsl:text>[TableParam].[dataType/@arraysize]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/table/column/dataType/@arraysize']" mode="tables">
      <xsl:text>TableParam#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='catalog/table/description']" mode="columns">
      <xsl:text>[Table].[description]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/table/description']" mode="tables">
      <xsl:text>Table#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='catalog/table/name']" mode="columns">
      <xsl:text>[Table].[name]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='catalog/table/name']" mode="tables">
      <xsl:text>Table#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/contentLevel']" mode="columns">
      <xsl:text>[Resource].[content/contentLevel]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/description']" mode="columns">
      <xsl:text>[Resource].[content/description]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/referenceURL']" mode="columns">
      <xsl:text>[Resource].[content/referenceURL]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/relationship/relatedResource/@ivo-id']" mode="columns">
      <xsl:text>[ResourceName].[@ivo-id]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='content/relationship/relatedResource/@ivo-id']" mode="tables">
      <xsl:text>ResourceName#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/relationship/relationshipType']" mode="columns">
      <xsl:text>[Relationship].[relationshipType]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='content/relationship/relationshipType']" mode="tables">
      <xsl:text>Relationship#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/source/@format']" mode="columns">
      <xsl:text>[Resource].[content/source/@format]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/subject']" mode="columns">
      <xsl:text>[Resource].[content/subject]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='content/type']" mode="columns">
      <xsl:text>[Resource].[content/type]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='coverage/footprint/@ivo-id']" mode="columns">
      <xsl:text>[DataService].[coverage/footprint/@ivo-id]#[DataCollection].[coverage/footprint/@ivo-id]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='coverage/footprint/@ivo-id']" mode="tables">
      <xsl:text>DataService#DataCollection#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='coverage/waveband']" mode="columns">
      <xsl:text>[DataService].[coverage/waveband]#[DataCollection].[coverage/waveband]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='coverage/waveband']" mode="tables">
      <xsl:text>DataService#DataCollection#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/contact/address']" mode="columns">
      <xsl:text>[Contact].[address]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/address']" mode="tables">
      <xsl:text>Contact#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/contact/email']" mode="columns">
      <xsl:text>[Contact].[email]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/email']" mode="tables">
      <xsl:text>Contact#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/contact/name/@ivo-id']" mode="columns">
      <xsl:text>[Contact].[name/@ivo-id]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/name/@ivo-id']" mode="tables">
      <xsl:text>Contact#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/contact/telephone']" mode="columns">
      <xsl:text>[Contact].[telephone]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contact/telephone']" mode="tables">
      <xsl:text>Contact#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/contributor/@ivo-id']" mode="columns">
      <xsl:text>[ResourceName].[@ivo-id]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/contributor/@ivo-id']" mode="tables">
      <xsl:text>ResourceName#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/creator/logo']" mode="columns">
      <xsl:text>[Creator].[logo]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/creator/logo']" mode="tables">
      <xsl:text>Creator#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/creator/name/@ivo-id']" mode="columns">
      <xsl:text>[Creator].[name/@ivo-id]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/creator/name/@ivo-id']" mode="tables">
      <xsl:text>Creator#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/date/@role']" mode="columns">
      <xsl:text>[Date].[@role]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='curation/date/@role']" mode="tables">
      <xsl:text>Date#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/publisher/@ivo-id']" mode="columns">
      <xsl:text>[Resource].[curation/publisher/@ivo-id]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/publisher']" mode="columns">
      <xsl:text>[Resource].[curation/publisher]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='curation/version']" mode="columns">
      <xsl:text>[Resource].[curation/version]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='facility/@ivo-id']" mode="columns">
      <xsl:text>[ResourceName].[@ivo-id]#[ResourceName].[@ivo-id]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='facility/@ivo-id']" mode="tables">
      <xsl:text>ResourceName#ResourceName#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='format/@isMIMEType']" mode="columns">
      <xsl:text>[Format].[@isMIMEType]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='format/@isMIMEType']" mode="tables">
      <xsl:text>Format#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='full']" mode="columns">
      <xsl:text>[Registry].[full]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='full']" mode="tables">
      <xsl:text>Registry#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='identifier']" mode="columns">
      <xsl:text>[Resource].[identifier]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='instrument/@ivo-id']" mode="columns">
      <xsl:text>[ResourceName].[@ivo-id]#[ResourceName].[@ivo-id]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='instrument/@ivo-id']" mode="tables">
      <xsl:text>ResourceName#ResourceName#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='managedAuthority']" mode="columns">
      <xsl:text>[Registry].[managedAuthority]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='managedAuthority']" mode="tables">
      <xsl:text>Registry#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='managingOrg/@ivo-id']" mode="columns">
      <xsl:text>[Authority].[managingOrg/@ivo-id]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='managingOrg/@ivo-id']" mode="tables">
      <xsl:text>Authority#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='rights']" mode="columns">
      <xsl:text>[Service].[rights]#[DataCollection].[rights]#</xsl:text>
   </xsl:template>
   <xsl:template match="@xpathName[self::node()='rights']" mode="tables">
      <xsl:text>Service#DataCollection#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='shortName']" mode="columns">
      <xsl:text>[Resource].[shortName]#</xsl:text>
   </xsl:template>

   <xsl:template match="@xpathName[self::node()='title']" mode="columns">
      <xsl:text>[Resource].[title]#</xsl:text>
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
      <xsl:apply-templates select="ad:Where"/>
   </xsl:template>

   <xsl:template match="*" mode="tables"/>
   
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

   
   <!--
     -  Union: a OR b
     -->
   <xsl:template match="*[@xsi:type='unionSearchType'] |                          *[substring-after(@xsi:type,':')='unionSearchType']">
      <xsl:if test="not(@xsi:nil='true')">

         <!-- table joins for lh of OR -->
         <xsl:variable name="jtbl1">
            <xsl:apply-templates select="*[1]" mode="tables"/>
         </xsl:variable>

         <!-- table joins for rh of OR -->
         <xsl:variable name="jtbl2">
            <xsl:apply-templates select="*[2]" mode="tables"/>
         </xsl:variable>

         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*[1]"/>
         <xsl:call-template name="makeJoin">
            <xsl:with-param name="tables" select="$jtbl1"/>
         </xsl:call-template>
         <xsl:text>)</xsl:text>
         <xsl:text> OR </xsl:text>
         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*[2]"/>
         <xsl:call-template name="makeJoin">
            <xsl:with-param name="tables" select="$jtbl2"/>
         </xsl:call-template>
         <xsl:text>)</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='unionSearchType'] |                          *[substring-after(@xsi:type,':')='unionSearchType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="boolean($forselect) and not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]" mode="tables"/>
         <xsl:apply-templates select="*[2]" mode="tables"/>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='comparisonPredType'] |                          *[substring-after(@xsi:type,':')='comparisonPredType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg[1]"/>
         <xsl:text> </xsl:text>
         <xsl:value-of select="@Comparison"/>
         <xsl:text> </xsl:text>
         <xsl:apply-templates select="ad:Arg[2]"/>
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

   
   <xsl:template match="*[@xsi:type='likePredType'] |                          *[substring-after(@xsi:type,':')='likePredType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg"/>
         <xsl:text> LIKE </xsl:text>
         <xsl:apply-templates select="ad:Pattern/ad:Literal"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='likePredType'] |                          *[substring-after(@xsi:type,':')='likePredType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='notLikePredType'] |                          *[substring-after(@xsi:type,':')='notLikePredType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg"/>
         <xsl:text> NOT LIKE </xsl:text>
         <xsl:apply-templates select="ad:Pattern/ad:Literal"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='notLikePredType'] |                          *[substring-after(@xsi:type,':')='notLikePredType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='betweenPredType'] |                          *[substring-after(@xsi:type,':')='betweenPredType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]"/>
         <xsl:text> BETWEEN </xsl:text>
         <xsl:apply-templates select="*[2]"/>
         <xsl:text> AND </xsl:text>
         <xsl:apply-templates select="*[3]"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='betweenPredType'] |                          *[substring-after(@xsi:type,':')='betweenPredType']" mode="tables">
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

   
   <xsl:template match="*[@xsi:type='notBetweenPredType'] |                          *[substring-after(@xsi:type,':')='notBetweenPredType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*[1]"/>
         <xsl:text> NOT BETWEEN </xsl:text>
         <xsl:apply-templates select="*[2]"/>
         <xsl:text> AND </xsl:text>
         <xsl:apply-templates select="*[3]"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='notBetweenPredType'] |                          *[substring-after(@xsi:type,':')='notBetweenPredType']" mode="tables">
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

   
   <xsl:template match="*[@xsi:type='closedSearchType'] |                          *[substring-after(@xsi:type,':')='closedSearchType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*"/>
         <xsl:text>)</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='closedSearchType'] |                          *[substring-after(@xsi:type,':')='closedSearchType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="ad:Where">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:text> SELECT [Resource].[xml] FROM [Resource] [Resource]</xsl:text>
         <xsl:call-template name="extendFrom">
            <xsl:with-param name="tables">
               <xsl:apply-templates select="ad:Condition" mode="tables">
                  <xsl:with-param name="forselect" select="true()"/>
               </xsl:apply-templates>
            </xsl:with-param>
         </xsl:call-template>
         <xsl:text> WHERE ([Resource].[@status] = 1) and </xsl:text>
         <xsl:apply-templates select="ad:Condition"/>
         <xsl:call-template name="makeJoin">
            <xsl:with-param name="tables">
               <xsl:apply-templates select="ad:Condition" mode="tables"/>
            </xsl:with-param>
         </xsl:call-template>
      </xsl:if>
   </xsl:template>

   

   
   <xsl:template match="*[@xsi:type='columnReferenceType'] |                        *[substring-after(@xsi:type,':')='columnReferenceType']">
      <xsl:variable name="collist">
         <xsl:apply-templates mode="columns" select="@xpathName"/>
      </xsl:variable>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:value-of select="substring-before($collist,'#')"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='columnReferenceType'] |                        *[substring-after(@xsi:type,':')='columnReferenceType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates mode="tables" select="@xpathName"/>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='unaryExprType'] |                          *[substring-after(@xsi:type,':')='unaryExprType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg"/>
         <xsl:text> </xsl:text>
         <xsl:value-of select="@Oper"/>
         <xsl:text> </xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='unaryExprType'] |                          *[substring-after(@xsi:type,':')='unaryExprType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='binaryExprType'] |                          *[substring-after(@xsi:type,':')='binaryExprType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="ad:Arg[1]"/>
         <xsl:text> </xsl:text>
         <xsl:value-of select="@Oper"/>
         <xsl:text> </xsl:text>
         <xsl:apply-templates select="ad:Arg[2]"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='binaryExprType'] |                          *[substring-after(@xsi:type,':')='binaryExprType']" mode="tables">
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

   
   <xsl:template match="*[@xsi:type='atomType'] |                          *[substring-after(@xsi:type,':')='atomType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*"/>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='closedExprType'] |                          *[substring-after(@xsi:type,':')='closedExprType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*"/>
         <xsl:text>)</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='closedExprType'] |                          *[substring-after(@xsi:type,':')='closedExprType']" mode="tables">
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

   
   <xsl:template match="*[@xsi:type = 'trigonometricFunctionType'] |                   *[substring-after(@xsi:type,':')='trigonometricFunctionType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:value-of select="@Name"/>
         <xsl:text>(</xsl:text>
         <xsl:apply-templates select="*"/>
         <xsl:text>)</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type = 'trigonometricFunctionType'] |                   *[substring-after(@xsi:type,':')='trigonometricFunctionType']" mode="tables">
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

   <xsl:template match="*[@xsi:type = 'mathFunctionType'] |                          *[substring-after(@xsi:type,':')='mathFunctionType']" mode="tables">
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

   <xsl:template match="*[@xsi:type = 'aggregateFunctionType'] |                       *[substring-after(@xsi:type,':')='aggregateFunctionType']" mode="tables">
      <xsl:param name="forselect" select="false()"/>

      <xsl:if test="not(@xsi:nil='true')">
         <xsl:apply-templates select="*" mode="tables">
            <xsl:with-param name="forselect" select="$forselect"/>
         </xsl:apply-templates>
      </xsl:if>
   </xsl:template>

   
   <xsl:template match="*[@xsi:type='integerType'] |                          *[substring-after(@xsi:type,':')='integerType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:value-of select="@Value"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='realType'] |                          *[substring-after(@xsi:type,':')='realType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:value-of select="@Value"/>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*[@xsi:type='stringType'] |                          *[substring-after(@xsi:type,':')='stringType']">
      <xsl:if test="not(@xsi:nil='true')">
         <xsl:text>'</xsl:text>
         <xsl:value-of select="@Value"/>
         <xsl:text>'</xsl:text>
      </xsl:if>
   </xsl:template>

   <xsl:template mode="columns" match="@xpathName" />
   <xsl:template mode="tables"  match="@xpathName" />

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

   <xsl:template name="makeJoin">
      <xsl:param name="tables" select="''"/>

      <xsl:if test="contains($tables,'#')">
         <xsl:variable name="tbl" select="substring-before($tables,'#')"/>
         <xsl:variable name="rest" select="substring-after($tables,'#')"/>

         <xsl:if test="$tbl!='' and 
                       not(contains(concat('#',$rest), concat('#',$tbl,'#')))">
            <xsl:text> AND [</xsl:text>
            <xsl:value-of select="$tbl"/>
            <xsl:text>].rkey=Resource.pkey</xsl:text>
         </xsl:if>

         <xsl:if test="contains($rest,'#')">
            <xsl:call-template name="makeJoin">
               <xsl:with-param name="tables" select="$rest"/>
            </xsl:call-template>
         </xsl:if>
      </xsl:if>
      
   </xsl:template>

 </xsl:stylesheet>
