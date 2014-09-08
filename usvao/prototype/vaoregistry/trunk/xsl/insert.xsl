<?xml version="1.0" encoding="UTF-8"?>
<oxsl:stylesheet xmlns:oxsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:stc="http://www.ivoa.net/xml/STC/stc-v1.30.xsd" xmlns:sn="http://www.ivoa.net/xml/OpenSkyNode/v0.2" xmlns:ssa="http://www.ivoa.net/xml/SSA/v1.0" xmlns:sia="http://www.ivoa.net/xml/SIA/v1.0" xmlns:cs="http://www.ivoa.net/xml/ConeSearch/v1.0" xmlns:vd="http://www.ivoa.net/xml/VOStandard/v0.1" xmlns:vg="http://www.ivoa.net/xml/VORegistry/v1.0" xmlns:vs="http://www.ivoa.net/xml/VODataService/v1.0" xmlns:vr="http://www.ivoa.net/xml/VOResource/v1.0" version="1.0">

   <oxsl:output method="text"/>

   <!--
     -  set this parameter to the rkey value of an existing record if this
     -  new record is intended to replace it.
     -->
   <oxsl:param name="existingrkey"/>

   <!--
     -  if the input record was harvested, set this to the identifier of
     -  the registry it was harvested from.
     -->
   <oxsl:param name="harvestedFrom"/>

   <!--
     -  if the input record was harvested, set this to the date and time 
     -  of when it was harvested
     -->
   <oxsl:param name="harvestedFromDate"/>

   <!--
     -  annotate with an internal tag value
     -->
   <oxsl:param name="tag"/>

   <!--
     -  set up the insert transaction, including the declaration variables
     -->
   <oxsl:template match="/">
      <oxsl:text> -- VOResource-to-database Converter 

declare @pkey bigint;
declare @rkey bigint;
declare @Resource_key bigint;
declare @Relationship_key bigint;
declare @Capability_key bigint;
declare @Interface_key bigint;
declare @DataCollection_key bigint;
declare @Catalog_key bigint;
declare @ParamHTTP_key bigint;
declare @Table_key bigint;

</oxsl:text>

      <!--
        -  check if we need to "delete" (deprecate, actually) an old record
        -->
      <oxsl:if test="$existingrkey!=''">
         <oxsl:text>-- Replacing exising record: change the status of the old one before inserting 
</oxsl:text>
         <oxsl:text>UPDATE [dbo].[Resource] SET [@status]=2 WHERE </oxsl:text>
         <oxsl:text>[pkey]=</oxsl:text>
         <oxsl:value-of select="$existingrkey"/>
         <oxsl:text>;

</oxsl:text>
      </oxsl:if>

      <oxsl:apply-templates select="*" mode="announce"/>

      <oxsl:text>
-- First add the core Resource metadata
</oxsl:text>

      <oxsl:apply-templates select="*"/>
   </oxsl:template>

   <!--
     - handle the metadata added by the Resource type
     -->
   <oxsl:template match="*" mode="table_Resource">
      <oxsl:param name="container"/>

      <oxsl:variable name="tagval">
         <oxsl:text>#</oxsl:text>
         <oxsl:apply-templates select="." mode="gettag"/>
         <oxsl:if test="$tag!=''">
            <oxsl:value-of select="$tag"/>
            <oxsl:text>#</oxsl:text>
         </oxsl:if>
      </oxsl:variable>

      <oxsl:text>INSERT INTO [dbo].[Resource] ( 
      [@created], [@updated], [@status], [title], [shortName], [identifier],
      [curation/publisher/@ivo-id], [curation/publisher], [curation/version],
      [content/subject], [content/description], [content/source/@format],
      [content/source], [content/referenceURL], [content/type],
      [content/contentLevel], [rights], [coverage/footprint/@ivo-id],
      [coverage/footprint], [coverage/waveband], [coverage/regionOfRegard], [validationLevel], [harvestedFrom], [harvestedFromDate], [tag], [xsi_type]
    ) VALUES ( </oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@created and not(contains(@created, '+'))">
            <oxsl:call-template name="mkstrval">
              <oxsl:with-param name="valnodes" select="@created"/>
            </oxsl:call-template>
        </oxsl:when>
        <oxsl:when test="@created and contains(@created, '+')">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="substring-before(@created, '+')"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@updated">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="@updated"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@status">
          <oxsl:choose>
             <oxsl:when test="@status='active'">1</oxsl:when>
             <oxsl:when test="@status='deleted'">3</oxsl:when>
             <oxsl:otherwise>0</oxsl:otherwise>
          </oxsl:choose>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="title">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="title"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="shortName">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="shortName"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="identifier">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="identifier"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="curation/publisher/@ivo-id">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="curation/publisher/@ivo-id"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="curation/publisher">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="curation/publisher"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="curation/version">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="curation/version"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="content/subject">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="content/subject"/>
            <oxsl:with-param name="asarray" select="true()"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="content/description">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="content/description"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="content/source/@format">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="content/source/@format"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="content/source">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="content/source"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="content/referenceURL">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="content/referenceURL"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="content/type">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="content/type"/>
            <oxsl:with-param name="asarray" select="true()"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="content/contentLevel">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="content/contentLevel"/>
            <oxsl:with-param name="asarray" select="true()"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="rights">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="rights"/>
            <oxsl:with-param name="asarray" select="true()"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="coverage/footprint/@ivo-id">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="coverage/footprint/@ivo-id"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="coverage/footprint">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="coverage/footprint"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="coverage/waveband">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="coverage/waveband"/>
            <oxsl:with-param name="asarray" select="true()"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,</oxsl:text><oxsl:text>
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="coverage/stc:STCResourceProfile/stc:AstroCoords/stc:Position1D/stc:Size">
          <oxsl:value-of select="coverage/stc:STCResourceProfile/stc:AstroCoords/stc:Position1D/stc:Size"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>,
</oxsl:text>

     <oxsl:text>      </oxsl:text>
     <oxsl:choose>
       <oxsl:when test="validationLevel">
         <oxsl:value-of select="validationLevel"/>
       </oxsl:when>
       <oxsl:otherwise>
         <!-- default validation level -->
         <oxsl:text>      2</oxsl:text>
       </oxsl:otherwise>
     </oxsl:choose>
     <oxsl:text>,
</oxsl:text>

      <!-- identity of registry that record was harvested from -->
      <oxsl:text>      '</oxsl:text>
      <oxsl:value-of select="$harvestedFrom"/>
      <oxsl:text>'</oxsl:text><oxsl:text>,
</oxsl:text>

      <!-- date that the record was harvested -->
      <oxsl:text>      '</oxsl:text>
      <oxsl:value-of select="$harvestedFromDate"/>
      <oxsl:text>'</oxsl:text><oxsl:text>,
</oxsl:text>

      <!-- an internal resource annotation -->
      <oxsl:text>      '</oxsl:text>
      <oxsl:value-of select="$tagval"/>
      <oxsl:text>'</oxsl:text><oxsl:text>,
</oxsl:text>

      <oxsl:text>      '</oxsl:text>
      <oxsl:value-of select="@xsi:type"/>
      <oxsl:text>'</oxsl:text>
      <oxsl:text>
    );

</oxsl:text>
       <oxsl:text>SELECT @Resource_key = MAX([pkey]) FROM [dbo].[Resource];
SELECT @rkey = @Resource_key;

</oxsl:text>

      <!-- Now load the metadata that goes into subtables -->

      <oxsl:if test="curation/creator">
         <oxsl:text>-- Add a curation/creator record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="curation/creator" mode="type_Creator">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

      <oxsl:if test="curation/contributor">
         <oxsl:text>-- Add a curation/contributor record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="curation/contributor" mode="type_ResourceName">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

      <oxsl:if test="curation/date">
         <oxsl:text>-- Add a curation/date record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="curation/date" mode="type_Date">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

      <oxsl:if test="curation/contact">
         <oxsl:text>-- Add a curation/contact record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="curation/contact" mode="type_Contact">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

      <oxsl:if test="content/relationship">
         <oxsl:text>-- Add a content/relationship record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="content/relationship" mode="type_Relationship">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

   </oxsl:template>

   <!--
     - handle the Resource type
     -->
   <oxsl:template match="*" mode="type_Resource">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_Resource">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole Resource instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='Resource' or substring-after(@xsi:type,':')='Resource' or (not(@xsi:type) and local-name()='resource')]">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Resource">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the ResourceName type
     -->
   <oxsl:template match="*" mode="table_ResourceName">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[ResourceName] ( [rkey], [container_key], 
      [@ivo-id],
      [ResourceName], [element_name]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@ivo-id">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="@ivo-id"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
      </oxsl:text>

      <oxsl:call-template name="mkstrval">
         <oxsl:with-param name="valnodes" select="."/>
      </oxsl:call-template><oxsl:text>,
</oxsl:text>

      <oxsl:text>      '</oxsl:text>
      <oxsl:value-of select="name()"/>
      <oxsl:text>'</oxsl:text>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the ResourceName type
     -->
   <oxsl:template match="*" mode="type_ResourceName">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_ResourceName">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Contact type
     -->
   <oxsl:template match="*" mode="table_Contact">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Contact] ( [rkey], [container_key], 
      [name/@ivo-id], [name], [address], [email], [telephone]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="name/@ivo-id">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="name/@ivo-id"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="name">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="name"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="address">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="address"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="email">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="email"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="telephone">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="telephone"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the Contact type
     -->
   <oxsl:template match="*" mode="type_Contact">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_Contact">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Creator type
     -->
   <oxsl:template match="*" mode="table_Creator">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Creator] ( [rkey], [container_key], 
      [name/@ivo-id], [name], [logo]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="name/@ivo-id">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="name/@ivo-id"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="name">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="name"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>''</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="logo">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="logo"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the Creator type
     -->
   <oxsl:template match="*" mode="type_Creator">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_Creator">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <oxsl:template match="*" mode="table_Date">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Date] ( 
      [rkey], [container_key], [@role], [Date]
    ) VALUES ( @rkey, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>, </oxsl:text>

      <oxsl:choose>
         <oxsl:when test="@role">
            <oxsl:text>'</oxsl:text>
            <oxsl:value-of select="@role"/>
            <oxsl:text>', </oxsl:text>
         </oxsl:when>
         <oxsl:otherwise>NULL, </oxsl:otherwise>
      </oxsl:choose>

      <oxsl:text>'</oxsl:text>
      <oxsl:value-of select="normalize-space(.)"/><oxsl:text>' );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the Date type
     -->
   <oxsl:template match="*" mode="type_Date">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_Date">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Relationship type
     -->
   <oxsl:template match="*" mode="table_Relationship">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Relationship] ( [rkey], [container_key], 
      [relationshipType]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="relationshipType">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="relationshipType"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
       <oxsl:text>SELECT @Relationship_key = MAX([pkey]) FROM [dbo].[Relationship];

</oxsl:text>

      <!-- Now load the metadata that goes into subtables -->

      <oxsl:if test="relatedResource">
         <oxsl:text>-- Add a relatedResource record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="relatedResource" mode="type_ResourceName">
         <oxsl:with-param name="container" select="'Relationship'"/>
      </oxsl:apply-templates>

   </oxsl:template>

   <!--
     - handle the Relationship type
     -->
   <oxsl:template match="*" mode="type_Relationship">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_Relationship">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <oxsl:template match="*" mode="table_Organisation">
      <oxsl:param name="container"/>

      <oxsl:if test="facility">
         <oxsl:text>-- Add a facility record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="facility" mode="type_ResourceName">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

      <oxsl:if test="instrument">
         <oxsl:text>-- Add a instrument record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="instrument" mode="type_ResourceName">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

   </oxsl:template>

   <!--
     - handle the Organisation type
     -->
   <oxsl:template match="*" mode="type_Organisation">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Resource">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:apply-templates select="." mode="table_Organisation"/>
   </oxsl:template>

   <!--
     - handle the whole Organisation instance, including 
     - parent metadata
     -->
   <oxsl:template match="*[@xsi:type='Organisation' or substring-after(@xsi:type,':')='Organisation']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Organisation">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <oxsl:template match="*" mode="table_Service">
      <oxsl:param name="container"/>

      <oxsl:if test="capability">
         <oxsl:text>-- Add a capability record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="capability">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

   </oxsl:template>

   <!--
     - handle the Service type
     -->
   <oxsl:template match="*" mode="type_Service">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Resource">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:apply-templates select="." mode="table_Service"/>
   </oxsl:template>

   <!--
     - handle the whole Service instance, including 
     - parent metadata
     -->
   <oxsl:template match="*[@xsi:type='Service' or substring-after(@xsi:type,':')='Service']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Service">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Capability type
     -->
   <oxsl:template match="*" mode="table_Capability">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Capability] ( [rkey], [container_key], 
      [maxRecords], [@standardID], [description], [validationLevel], [xsi_type]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxRecords/text()" >
          <oxsl:value-of select="maxRecords"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@standardID">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="@standardID"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="description">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="description"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

     <oxsl:text>      </oxsl:text>
     <oxsl:choose>
       <oxsl:when test="validationLevel">
         <oxsl:value-of select="validationLevel"/>
       </oxsl:when>
       <oxsl:otherwise>
         <!-- default validation level -->
         <oxsl:text>      2</oxsl:text>
       </oxsl:otherwise>
     </oxsl:choose>
     <oxsl:text>,
</oxsl:text>

      <oxsl:text>      '</oxsl:text>
      <oxsl:value-of select="@xsi:type"/>
      <oxsl:text>'</oxsl:text>
      <oxsl:text>
    );

</oxsl:text>
       <oxsl:text>SELECT @Capability_key = MAX([pkey]) FROM [dbo].[Capability];

</oxsl:text>

      <!-- Now load the metadata that goes into subtables -->

      <oxsl:if test="interface">
         <oxsl:text>-- Add a interface record
</oxsl:text>
      </oxsl:if>
      <oxsl:choose>
         <oxsl:when test="interface/@xsi:type">
            <!-- load all extended metadata via specialized template -->
            <oxsl:apply-templates select="interface">
               <oxsl:with-param select="'Capability'" name="container"/>
            </oxsl:apply-templates>
         </oxsl:when>
         <oxsl:otherwise>
            <!-- no extended metadata detected -->
            <oxsl:apply-templates select="interface" mode="type_Interface">
               <oxsl:with-param select="'Capability'" name="container"/>
            </oxsl:apply-templates>
         </oxsl:otherwise>
      </oxsl:choose>

   </oxsl:template>

   <!--
     - fallback template for capability.
     - This is invoked when the xsi:type is not recognized or nonexistent
     -->
   <oxsl:template match="capability" priority="-1.0">
      <oxsl:param name="container"/>

      <oxsl:apply-templates mode="type_Capability" select=".">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the Capability type
     -->
   <oxsl:template match="*" mode="type_Capability">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_Capability">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole Capability instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='Capability' or substring-after(@xsi:type,':')='Capability' or (not(@xsi:type) and local-name()='capability')]">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Capability">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Interface type
     -->
   <oxsl:template match="*" mode="table_Interface">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Interface] ( [rkey], [container_key], 
      [@version], [@role], [accessURL/@use], [accessURL], [xsi_type]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@version">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="@version"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@role">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="@role"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="accessURL/@use">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="accessURL/@use"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="accessURL">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="accessURL"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      '</oxsl:text>
      <oxsl:value-of select="@xsi:type"/>
      <oxsl:text>'</oxsl:text>
      <oxsl:text>
    );

</oxsl:text>
       <oxsl:text>SELECT @Interface_key = MAX([pkey]) FROM [dbo].[Interface];

</oxsl:text>

      <!-- Now load the metadata that goes into subtables -->

      <oxsl:if test="securityMethod">
         <oxsl:text>-- Add a securityMethod record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="securityMethod" mode="type_SecurityMethod">
         <oxsl:with-param name="container" select="'Interface'"/>
      </oxsl:apply-templates>

   </oxsl:template>

   <!--
     - fallback template for interface.
     - This is invoked when the xsi:type is not recognized or nonexistent
     -->
   <oxsl:template match="interface" priority="-1.0">
      <oxsl:param name="container"/>

      <oxsl:apply-templates mode="type_Interface" select=".">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the Interface type
     -->
   <oxsl:template match="*" mode="type_Interface">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_Interface">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole Interface instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='Interface' or substring-after(@xsi:type,':')='Interface' or (not(@xsi:type) and local-name()='interface')]">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Interface">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the SecurityMethod type
     -->
   <oxsl:template match="*" mode="table_SecurityMethod">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[SecurityMethod] ( [rkey], [container_key], 
      [@standardID]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@standardID">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="@standardID"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the SecurityMethod type
     -->
   <oxsl:template match="*" mode="type_SecurityMethod">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_SecurityMethod">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <oxsl:template match="*" mode="table_WebBrowser">
      <oxsl:param name="container"/>

   </oxsl:template>

   <!--
     - handle the WebBrowser type
     -->
   <oxsl:template match="*" mode="type_WebBrowser">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Interface">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:apply-templates select="." mode="table_WebBrowser"/>
   </oxsl:template>

   <!--
     - handle the whole WebBrowser instance, including 
     - parent metadata
     -->
   <oxsl:template match="*[@xsi:type='WebBrowser' or substring-after(@xsi:type,':')='WebBrowser']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_WebBrowser">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the WebService type
     -->
   <oxsl:template match="*" mode="table_WebService">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[WebService] ( [rkey], [Interface_key], [container_key], 
      [wsdlURL]
    ) VALUES ( @rkey, @Interface_key</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="wsdlURL">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="wsdlURL"/>
            <oxsl:with-param name="asarray" select="true()"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the WebService type
     -->
   <oxsl:template match="*" mode="type_WebService">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Interface">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:text>-- Add the WebService extension metadata
</oxsl:text>
      <oxsl:apply-templates select="." mode="table_WebService">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole WebService instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='WebService' or substring-after(@xsi:type,':')='WebService']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_WebService">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Registry type
     -->
   <oxsl:template match="*" mode="table_Registry">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Registry] ( [rkey], [Resource_key], 
      [full], [managedAuthority]
    ) VALUES ( @rkey, @Resource_key</oxsl:text>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="full">
          <oxsl:choose>
             <oxsl:when test="string(full)='true'">1</oxsl:when>
             <oxsl:otherwise>0</oxsl:otherwise>
          </oxsl:choose>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="managedAuthority">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="managedAuthority"/>
            <oxsl:with-param name="asarray" select="true()"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the Registry type
     -->
   <oxsl:template match="*" mode="type_Registry">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Resource">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:text>-- Add the Registry extension metadata
</oxsl:text>
      <oxsl:apply-templates select="." mode="table_Registry">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole Registry instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='Registry' or substring-after(@xsi:type,':')='Registry']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Registry">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Harvest type
     -->
   <oxsl:template match="*" mode="table_Harvest">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Harvest] ( [rkey], [Capability_key], [container_key], 
      
    ) VALUES ( @rkey, @Capability_key</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the Harvest type
     -->
   <oxsl:template match="*" mode="type_Harvest">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Capability">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:text>-- Add the Harvest extension metadata
</oxsl:text>
      <oxsl:apply-templates select="." mode="table_Harvest">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole Harvest instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='Harvest' or substring-after(@xsi:type,':')='Harvest']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Harvest">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Search type
     -->
   <oxsl:template match="*" mode="table_Search">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Search] ( [rkey], [Capability_key], [container_key], 
      [extensionSearchSupport], [optionalProtocol]
    ) VALUES ( @rkey, @Capability_key</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="extensionSearchSupport">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="extensionSearchSupport"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="optionalProtocol">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="optionalProtocol"/>
            <oxsl:with-param name="asarray" select="true()"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the Search type
     -->
   <oxsl:template match="*" mode="type_Search">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Capability">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:text>-- Add the Search extension metadata
</oxsl:text>
      <oxsl:apply-templates select="." mode="table_Search">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole Search instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='Search' or substring-after(@xsi:type,':')='Search']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Search">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <oxsl:template match="*" mode="table_OAIHTTP">
      <oxsl:param name="container"/>

   </oxsl:template>

   <!--
     - handle the OAIHTTP type
     -->
   <oxsl:template match="*" mode="type_OAIHTTP">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Interface">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:apply-templates select="." mode="table_OAIHTTP"/>
   </oxsl:template>

   <!--
     - handle the whole OAIHTTP instance, including 
     - parent metadata
     -->
   <oxsl:template match="*[@xsi:type='OAIHTTP' or substring-after(@xsi:type,':')='OAIHTTP']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_OAIHTTP">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <oxsl:template match="*" mode="table_OAISOAP">
      <oxsl:param name="container"/>

   </oxsl:template>

   <!--
     - handle the OAISOAP type
     -->
   <oxsl:template match="*" mode="type_OAISOAP">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_WebService">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:apply-templates select="." mode="table_OAISOAP"/>
   </oxsl:template>

   <!--
     - handle the whole OAISOAP instance, including 
     - parent metadata
     -->
   <oxsl:template match="*[@xsi:type='OAISOAP' or substring-after(@xsi:type,':')='OAISOAP']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_OAISOAP">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Authority type
     -->
   <oxsl:template match="*" mode="table_Authority">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Authority] ( [rkey], [Resource_key], 
      [managingOrg/@ivo-id], [managingOrg]
    ) VALUES ( @rkey, @Resource_key</oxsl:text>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="managingOrg/@ivo-id">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="managingOrg/@ivo-id"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="managingOrg">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="managingOrg"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the Authority type
     -->
   <oxsl:template match="*" mode="type_Authority">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Resource">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:text>-- Add the Authority extension metadata
</oxsl:text>
      <oxsl:apply-templates select="." mode="table_Authority">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole Authority instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='Authority' or substring-after(@xsi:type,':')='Authority']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Authority">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <oxsl:template match="*" mode="table_Standard">
      <oxsl:param name="container"/>

      <oxsl:if test="endorsedVersion">
         <oxsl:text>-- Add a endorsedVersion record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="endorsedVersion" mode="type_EndorsedVersion">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

   </oxsl:template>

   <!--
     - handle the Standard type
     -->
   <oxsl:template match="*" mode="type_Standard">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Resource">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:apply-templates select="." mode="table_Standard"/>
   </oxsl:template>

   <!--
     - handle the whole Standard instance, including 
     - parent metadata
     -->
   <oxsl:template match="*[@xsi:type='Standard' or substring-after(@xsi:type,':')='Standard']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Standard">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the EndorsedVersion type
     -->
   <oxsl:template match="*" mode="table_EndorsedVersion">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[EndorsedVersion] ( [rkey], [container_key], 
      [@status],
      [EndorsedVersion]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@status">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="@status"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
      </oxsl:text>

      <oxsl:call-template name="mkstrval">
         <oxsl:with-param name="valnodes" select="."/>
      </oxsl:call-template>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the EndorsedVersion type
     -->
   <oxsl:template match="*" mode="type_EndorsedVersion">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_EndorsedVersion">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <oxsl:template match="*" mode="table_ServiceStandard">
      <oxsl:param name="container"/>

      <oxsl:if test="endorsedVersion">
         <oxsl:text>-- Add a endorsedVersion record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="endorsedVersion" mode="type_EndorsedVersion">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

      <oxsl:if test="interface">
         <oxsl:text>-- Add a interface record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="interface">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

   </oxsl:template>

   <!--
     - handle the ServiceStandard type
     -->
   <oxsl:template match="*" mode="type_ServiceStandard">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Resource">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:apply-templates select="." mode="table_ServiceStandard"/>
   </oxsl:template>

   <!--
     - handle the whole ServiceStandard instance, including 
     - parent metadata
     -->
   <oxsl:template match="*[@xsi:type='ServiceStandard' or substring-after(@xsi:type,':')='ServiceStandard']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_ServiceStandard">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the DataCollection type
     -->
   <oxsl:template match="*" mode="table_DataCollection">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[DataCollection] ( [rkey], [Resource_key], 
      [accessURL/@use], [accessURL]
    ) VALUES ( @rkey, @Resource_key</oxsl:text>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="accessURL/@use">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="accessURL/@use"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="accessURL">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="accessURL"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
       <oxsl:text>SELECT @DataCollection_key = MAX([pkey]) FROM [dbo].[DataCollection];

</oxsl:text>

      <!-- Now load the metadata that goes into subtables -->

      <oxsl:if test="facility">
         <oxsl:text>-- Add a facility record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="facility" mode="type_ResourceName">
         <oxsl:with-param name="container" select="'DataCollection'"/>
      </oxsl:apply-templates>

      <oxsl:if test="instrument">
         <oxsl:text>-- Add a instrument record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="instrument" mode="type_ResourceName">
         <oxsl:with-param name="container" select="'DataCollection'"/>
      </oxsl:apply-templates>

      <oxsl:if test="format">
         <oxsl:text>-- Add a format record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="format" mode="type_Format">
         <oxsl:with-param name="container" select="'DataCollection'"/>
      </oxsl:apply-templates>

      <oxsl:if test="catalog">
         <oxsl:text>-- Add a catalog record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="catalog" mode="type_Catalog">
         <oxsl:with-param name="container" select="'DataCollection'"/>
      </oxsl:apply-templates>

   </oxsl:template>

   <!--
     - handle the DataCollection type
     -->
   <oxsl:template match="*" mode="type_DataCollection">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Resource">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:text>-- Add the DataCollection extension metadata
</oxsl:text>
      <oxsl:apply-templates select="." mode="table_DataCollection">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole DataCollection instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='DataCollection' or substring-after(@xsi:type,':')='DataCollection']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_DataCollection">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Catalog type
     -->
   <oxsl:template match="*" mode="table_Catalog">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Catalog] ( [rkey], [container_key], 
      [name], [description]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="name">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="name"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="description">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="description"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
       <oxsl:text>SELECT @Catalog_key = MAX([pkey]) FROM [dbo].[Catalog];

</oxsl:text>

      <!-- Now load the metadata that goes into subtables -->

      <oxsl:if test="table">
         <oxsl:text>-- Add a table record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="table" mode="type_Table">
         <oxsl:with-param name="container" select="'Catalog'"/>
      </oxsl:apply-templates>

   </oxsl:template>

   <!--
     - handle the Catalog type
     -->
   <oxsl:template match="*" mode="type_Catalog">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_Catalog">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Format type
     -->
   <oxsl:template match="*" mode="table_Format">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Format] ( [rkey], [container_key], 
      [@isMIMEType],
      [Format]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@isMIMEType">
          <oxsl:choose>
             <oxsl:when test="string(@isMIMEType)='true'">1</oxsl:when>
             <oxsl:otherwise>0</oxsl:otherwise>
          </oxsl:choose>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
      </oxsl:text>

      <oxsl:call-template name="mkstrval">
         <oxsl:with-param name="valnodes" select="."/>
      </oxsl:call-template>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the Format type
     -->
   <oxsl:template match="*" mode="type_Format">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_Format">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <oxsl:template match="*" mode="table_DataService">
      <oxsl:param name="container"/>

      <oxsl:if test="facility">
         <oxsl:text>-- Add a facility record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="facility" mode="type_ResourceName">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

      <oxsl:if test="instrument">
         <oxsl:text>-- Add a instrument record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="instrument" mode="type_ResourceName">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

      <oxsl:if test="capability">
         <oxsl:text>-- Add a capability record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="capability">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

   </oxsl:template>

   <!--
     - handle the DataService type
     -->
   <oxsl:template match="*" mode="type_DataService">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Resource">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:apply-templates select="." mode="table_DataService"/>
   </oxsl:template>

   <!--
     - handle the whole DataService instance, including 
     - parent metadata
     -->
   <oxsl:template match="*[@xsi:type='DataService' or substring-after(@xsi:type,':')='DataService']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_DataService">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the ParamHTTP type
     -->
   <oxsl:template match="*" mode="table_ParamHTTP">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[ParamHTTP] ( [rkey], [Interface_key], [container_key], 
      [queryType], [resultType]
    ) VALUES ( @rkey, @Interface_key</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="queryType">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="queryType"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="resultType">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="resultType"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
       <oxsl:text>SELECT @ParamHTTP_key = MAX([pkey]) FROM [dbo].[ParamHTTP];

</oxsl:text>

      <!-- Now load the metadata that goes into subtables -->

      <oxsl:if test="param">
         <oxsl:text>-- Add a param record
</oxsl:text>
      </oxsl:if>
      <oxsl:choose>
         <oxsl:when test="param/@xsi:type">
            <!-- load all extended metadata via specialized template -->
            <oxsl:apply-templates select="param">
               <oxsl:with-param select="'ParamHTTP'" name="container"/>
            </oxsl:apply-templates>
         </oxsl:when>
         <oxsl:otherwise>
            <!-- no extended metadata detected -->
            <oxsl:apply-templates select="param" mode="type_Param">
               <oxsl:with-param select="'ParamHTTP'" name="container"/>
            </oxsl:apply-templates>
         </oxsl:otherwise>
      </oxsl:choose>

   </oxsl:template>

   <!--
     - handle the ParamHTTP type
     -->
   <oxsl:template match="*" mode="type_ParamHTTP">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Interface">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:text>-- Add the ParamHTTP extension metadata
</oxsl:text>
      <oxsl:apply-templates select="." mode="table_ParamHTTP">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole ParamHTTP instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='ParamHTTP' or substring-after(@xsi:type,':')='ParamHTTP']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_ParamHTTP">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <oxsl:template match="*" mode="table_CatalogService">
      <oxsl:param name="container"/>

      <oxsl:if test="table">
         <oxsl:text>-- Add a table record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="table" mode="type_Table">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

      <oxsl:if test="capability">
         <oxsl:text>-- Add a capability record
</oxsl:text>
      </oxsl:if>
      <oxsl:apply-templates select="capability">
         <oxsl:with-param name="container" select="'Resource'"/>
      </oxsl:apply-templates>

   </oxsl:template>

  <oxsl:template match="*" mode="table_CeaApplication">
    <oxsl:param name="container"/>

    <oxsl:if test="table">
      <oxsl:text>-- Add a table record
</oxsl:text>
    </oxsl:if>
    <oxsl:apply-templates select="table" mode="type_Table">
      <oxsl:with-param name="container" select="'Resource'"/>
    </oxsl:apply-templates>

    <oxsl:if test="capability">
      <oxsl:text>-- Add a capability record
</oxsl:text>
    </oxsl:if>
    <oxsl:apply-templates select="capability">
      <oxsl:with-param name="container" select="'Resource'"/>
    </oxsl:apply-templates>

  </oxsl:template>
  

   <!--
     - handle the CatalogService type
     -->
   <oxsl:template match="*" mode="type_CatalogService">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Resource">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:apply-templates select="." mode="table_CatalogService"/>
   </oxsl:template>

   <!--
     - handle the whole CatalogService instance, including 
     - parent metadata
     -->
   <oxsl:template match="*[@xsi:type='CatalogService' or substring-after(@xsi:type,':')='CatalogService']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_CatalogService">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

  <!--
     - handle the CeaApplication type
     -->
  <oxsl:template match="*" mode="type_CeaApplication">
    <oxsl:param name="container"/>

    <oxsl:apply-templates select="." mode="type_Resource">
      <oxsl:with-param name="container" select="$container"/>
    </oxsl:apply-templates>
    <oxsl:apply-templates select="." mode="table_CeaApplication"/>
  </oxsl:template>

  <!--
     - handle the whole CeaApplication instance, including 
     - parent metadata
     -->
  <oxsl:template match="*[@xsi:type='CeaApplication' or substring-after(@xsi:type,':')='CeaApplication']">
    <oxsl:param name="container"/>

    <oxsl:apply-templates select="." mode="type_CeaApplication">
      <oxsl:with-param name="container" select="$container"/>
    </oxsl:apply-templates>
  </oxsl:template>  

   <!--
     - handle the metadata added by the Table type
     -->
   <oxsl:template match="*" mode="table_Table">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Table] ( [rkey], [container_key], 
      [@role], [name], [description]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@role">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="@role"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="name">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="name"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="description">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="description"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
       <oxsl:text>SELECT @Table_key = MAX([pkey]) FROM [dbo].[Table];

</oxsl:text>

      <!-- Now load the metadata that goes into subtables -->

      <oxsl:if test="column">
         <oxsl:text>-- Add a column record
</oxsl:text>
      </oxsl:if>
      <oxsl:choose>
         <oxsl:when test="column/@xsi:type">
            <!-- load all extended metadata via specialized template -->
            <oxsl:apply-templates select="column">
               <oxsl:with-param select="'Table'" name="container"/>
            </oxsl:apply-templates>
         </oxsl:when>
         <oxsl:otherwise>
            <!-- no extended metadata detected -->
            <oxsl:apply-templates select="column" mode="type_Param">
               <oxsl:with-param select="'Table'" name="container"/>
            </oxsl:apply-templates>
         </oxsl:otherwise>
      </oxsl:choose>

   </oxsl:template>

   <!--
     - handle the Table type
     -->
   <oxsl:template match="*" mode="type_Table">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_Table">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the Param type
     -->
   <oxsl:template match="*" mode="table_Param">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[Param] ( [rkey], [container_key], 
      [name], [description], [unit], [ucd], [@use], [@std], [dataType/@arraysize],
      [dataType], [xsi_type], [element_name]
    ) VALUES ( @rkey</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="name">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="name"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="description">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="description"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="unit">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="unit"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="ucd">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="ucd"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@use">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="@use"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="@std">
          <oxsl:choose>
             <oxsl:when test="string(@std)='true'">1</oxsl:when>
             <oxsl:otherwise>0</oxsl:otherwise>
          </oxsl:choose>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="dataType/@arraysize">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="dataType/@arraysize"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="dataType">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="dataType"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      '</oxsl:text>
      <oxsl:value-of select="@xsi:type"/>
      <oxsl:text>'</oxsl:text><oxsl:text>,
</oxsl:text>

      <oxsl:text>      '</oxsl:text>
      <oxsl:value-of select="name()"/>
      <oxsl:text>'</oxsl:text>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - fallback template for param.
     - This is invoked when the xsi:type is not recognized or nonexistent
     -->
   <oxsl:template match="param" priority="-1.0">
      <oxsl:param name="container"/>

      <oxsl:apply-templates mode="type_Param" select=".">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - fallback template for column.
     - This is invoked when the xsi:type is not recognized or nonexistent
     -->
   <oxsl:template match="column" priority="-1.0">
      <oxsl:param name="container"/>

      <oxsl:apply-templates mode="type_Param" select=".">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the Param type
     -->
   <oxsl:template match="*" mode="type_Param">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="table_Param">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole Param instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='Param' or substring-after(@xsi:type,':')='Param' or (not(@xsi:type) and local-name()='param')]">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Param">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <oxsl:template match="*" mode="table_StandardSTC">
      <oxsl:param name="container"/>

   </oxsl:template>

   <!--
     - handle the StandardSTC type
     -->
   <oxsl:template match="*" mode="type_StandardSTC">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Resource">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:apply-templates select="." mode="table_StandardSTC"/>
   </oxsl:template>

   <!--
     - handle the whole StandardSTC instance, including 
     - parent metadata
     -->
   <oxsl:template match="*[@xsi:type='StandardSTC' or substring-after(@xsi:type,':')='StandardSTC']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_StandardSTC">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the ConeSearch type
     -->
   <oxsl:template match="*" mode="table_ConeSearch">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[ConeSearch] ( [rkey], [Capability_key], [container_key], 
      [maxSR], [verbosity], [testQuery/ra], [testQuery/dec], [testQuery/sr],
      [testQuery/verb], [testQuery/catalog], [testQuery/extras]
    ) VALUES ( @rkey, @Capability_key</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxSR">
          <oxsl:value-of select="maxSR"/>
        </oxsl:when>
        <oxsl:otherwise>180</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="verbosity">
          <oxsl:choose>
             <oxsl:when test="string(verbosity)='true'">1</oxsl:when>
             <oxsl:otherwise>0</oxsl:otherwise>
          </oxsl:choose>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/ra">
          <oxsl:value-of select="testQuery/ra"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/dec">
          <oxsl:value-of select="testQuery/dec"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/sr">
          <oxsl:value-of select="testQuery/sr"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/verb">
          <oxsl:value-of select="testQuery/verb"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/catalog">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="testQuery/catalog"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/extras">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="testQuery/extras"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the ConeSearch type
     -->
   <oxsl:template match="*" mode="type_ConeSearch">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Capability">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:text>-- Add the ConeSearch extension metadata
</oxsl:text>
      <oxsl:apply-templates select="." mode="table_ConeSearch">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole ConeSearch instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='ConeSearch' or substring-after(@xsi:type,':')='ConeSearch']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_ConeSearch">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the SimpleImageAccess type
     -->
   <oxsl:template match="*" mode="table_SimpleImageAccess">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[SimpleImageAccess] ( [rkey], [Capability_key], [container_key], 
      [imageServiceType], [maxQueryRegionSize/long], [maxQueryRegionSize/lat],
      [maxImageExtent/long], [maxImageExtent/lat], [maxImageSize/long],
      [maxImageSize/lat], [maxFileSize], [testQuery/pos/long], [testQuery/pos/lat],
      [testQuery/size/long], [testQuery/size/lat], [testQuery/verb],
      [testQuery/extras]
    ) VALUES ( @rkey, @Capability_key</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="imageServiceType">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="imageServiceType"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxQueryRegionSize/long">
          <oxsl:value-of select="maxQueryRegionSize/long"/>
        </oxsl:when>
        <oxsl:otherwise>360</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxQueryRegionSize/lat">
          <oxsl:value-of select="maxQueryRegionSize/lat"/>
        </oxsl:when>
        <oxsl:otherwise>360</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxImageExtent/long">
          <oxsl:value-of select="maxImageExtent/long"/>
        </oxsl:when>
        <oxsl:otherwise>360</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxImageExtent/lat">
          <oxsl:value-of select="maxImageExtent/lat"/>
        </oxsl:when>
        <oxsl:otherwise>360</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxImageSize/long">
          <oxsl:value-of select="maxImageSize/long"/>
        </oxsl:when>
        <oxsl:otherwise>0</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxImageSize/lat">
          <oxsl:value-of select="maxImageSize/lat"/>
        </oxsl:when>
        <oxsl:otherwise>0</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxFileSize">
          <oxsl:value-of select="maxFileSize"/>
        </oxsl:when>
        <oxsl:otherwise>0</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/pos/long">
          <oxsl:value-of select="testQuery/pos/long"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/pos/lat">
          <oxsl:value-of select="testQuery/pos/lat"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/size/long">
          <oxsl:value-of select="testQuery/size/long"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/size/lat">
          <oxsl:value-of select="testQuery/size/lat"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/verb">
          <oxsl:value-of select="testQuery/verb"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/extras">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="testQuery/extras"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the SimpleImageAccess type
     -->
   <oxsl:template match="*" mode="type_SimpleImageAccess">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Capability">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:text>-- Add the SimpleImageAccess extension metadata
</oxsl:text>
      <oxsl:apply-templates select="." mode="table_SimpleImageAccess">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole SimpleImageAccess instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='SimpleImageAccess' or substring-after(@xsi:type,':')='SimpleImageAccess']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_SimpleImageAccess">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the SimpleSpectralAccess type
     -->
   <oxsl:template match="*" mode="table_SimpleSpectralAccess">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[SimpleSpectralAccess] ( [rkey], [Capability_key], [container_key], 
      [complianceLevel], [dataSource], [creationType], [maxSearchRadius],
      [defaultMaxRecords], [maxAperture], [maxFileSize], [testQuery/pos/long],
      [testQuery/pos/lat], [testQuery/pos/refframe], [testQuery/size],
      [testQuery/queryDataCmd]
    ) VALUES ( @rkey, @Capability_key</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="complianceLevel">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="complianceLevel"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="dataSource">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="dataSource"/>
            <oxsl:with-param name="asarray" select="true()"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="creationType">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="creationType"/>
            <oxsl:with-param name="asarray" select="true()"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxSearchRadius">
          <oxsl:value-of select="maxSearchRadius"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="defaultMaxRecords">
          <oxsl:value-of select="defaultMaxRecords"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxAperture and normalize-space(maxAperture)">
          <oxsl:value-of select="maxAperture"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="maxFileSize and normalize-space(maxFileSize)">
          <oxsl:value-of select="maxFileSize"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/pos/long">
          <oxsl:value-of select="testQuery/pos/long"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/pos/lat">
          <oxsl:value-of select="testQuery/pos/lat"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/pos/refframe">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="testQuery/pos/refframe"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/size">
          <oxsl:value-of select="testQuery/size"/>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="testQuery/queryDataCmd">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="testQuery/queryDataCmd"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the SimpleSpectralAccess type
     -->
   <oxsl:template match="*" mode="type_SimpleSpectralAccess">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Capability">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:text>-- Add the SimpleSpectralAccess extension metadata
</oxsl:text>
      <oxsl:apply-templates select="." mode="table_SimpleSpectralAccess">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole SimpleSpectralAccess instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='SimpleSpectralAccess' or substring-after(@xsi:type,':')='SimpleSpectralAccess']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_SimpleSpectralAccess">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the metadata added by the OpenSkyNode type
     -->
   <oxsl:template match="*" mode="table_OpenSkyNode">
      <oxsl:param name="container"/>

      <oxsl:text>INSERT INTO [dbo].[OpenSkyNode] ( [rkey], [Capability_key], [container_key], 
      [compliance], [longitude], [latitude], [primaryTable], [primaryKey]
    ) VALUES ( @rkey, @Capability_key</oxsl:text>
      <oxsl:text>, </oxsl:text>
      <oxsl:value-of select="concat('@',$container,'_key')"/>
      <oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="compliance and normalize-space(compliance)" >
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="compliance"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="longitude and normalize-space(longitude)">
          <oxsl:value-of select="longitude"/>
        </oxsl:when>
        <oxsl:otherwise>0</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="latitude and normalize-space(latitude)">
          <oxsl:value-of select="latitude"/>
        </oxsl:when>
        <oxsl:otherwise>0</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="primaryTable">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="primaryTable"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose><oxsl:text>,
</oxsl:text>

      <oxsl:text>      </oxsl:text>
      <oxsl:choose>
        <oxsl:when test="primaryKey">
          <oxsl:call-template name="mkstrval">
            <oxsl:with-param name="valnodes" select="primaryKey"/>
          </oxsl:call-template>
        </oxsl:when>
        <oxsl:otherwise>NULL</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>
    );

</oxsl:text>
   </oxsl:template>

   <!--
     - handle the OpenSkyNode type
     -->
   <oxsl:template match="*" mode="type_OpenSkyNode">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_Capability">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
      <oxsl:text>-- Add the OpenSkyNode extension metadata
</oxsl:text>
      <oxsl:apply-templates select="." mode="table_OpenSkyNode">
         <oxsl:with-param select="$container" name="container"/>
      </oxsl:apply-templates>
   </oxsl:template>

   <!--
     - handle the whole OpenSkyNode instance, including parent metadata
     -->
   <oxsl:template match="*[@xsi:type='OpenSkyNode' or substring-after(@xsi:type,':')='OpenSkyNode']">
      <oxsl:param name="container"/>

      <oxsl:apply-templates select="." mode="type_OpenSkyNode">
         <oxsl:with-param name="container" select="$container"/>
      </oxsl:apply-templates>
   </oxsl:template>



   <!-- 
     - announce the type of Resource description being loaded 
     -->
   <oxsl:template match="*[identifier]" mode="announce">
      <!--detect if the resource type starts with a vowel-->
      <oxsl:variable name="n">
         <oxsl:if test="starts-with(translate(substring-after(@xsi:type,':'),                                               'AeEiIoOuU',                                               'aaaaaaaaa'),'a')">
            <oxsl:text>n</oxsl:text>
         </oxsl:if>
      </oxsl:variable>

      <oxsl:text>-- a</oxsl:text>
      <oxsl:value-of select="$n"/><oxsl:text> </oxsl:text>
      <oxsl:choose>
         <oxsl:when test="contains(@xsi:type,':')">
            <oxsl:value-of select="substring-after(@xsi:type, ':')"/>
         </oxsl:when>
         <oxsl:when test="@xsi:type">
            <oxsl:value-of select="@xsi:type"/>
         </oxsl:when>
         <oxsl:otherwise>generic</oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text> Resource
</oxsl:text>
   </oxsl:template>

   <!--
     -  Get the proper value of the tag column for a given Resource
     -->
   <oxsl:template match="*[identifier]" mode="gettag">
      <oxsl:variable name="rxsitype" select="substring-after(@xsi:type,':')"/>
                    
      <oxsl:choose>
         <oxsl:when test="$rxsitype='Registry'">
            <oxsl:if test="capability[substring-after(@xsi:type,':')='Search']">
               <oxsl:text>Searchable</oxsl:text>
            </oxsl:if>
            <oxsl:if test="capability[substring-after(@xsi:type,':')='Search'] and capability[substring-after(@xsi:type,':')='Harvest']">
               <oxsl:text> </oxsl:text>
            </oxsl:if>
            <oxsl:if test="capability[substring-after(@xsi:type,':')='Harvest']">
               <oxsl:text>Publishing</oxsl:text>
            </oxsl:if>
            <oxsl:text> Registry#</oxsl:text>
         </oxsl:when>
         <oxsl:when test="capability">
           <oxsl:for-each select="capability">
            <oxsl:variable name="cxsitype" select="substring-after(@xsi:type,':')"/>
            <oxsl:choose>
               <oxsl:when test="$cxsitype='ConeSearch' or $cxsitype='OpenSkyNode'">
                  <oxsl:text>Catalog</oxsl:text>
               </oxsl:when>
               <oxsl:when test="$cxsitype='SimpleImageAccess'">
                   <oxsl:text>Images</oxsl:text>
               </oxsl:when>
               <oxsl:when test="$cxsitype='SimpleSpectralAccess'">
                   <oxsl:text>Spectra</oxsl:text>
               </oxsl:when>
               <oxsl:when test="$cxsitype='Search' or $cxsitype='Harvest'">
                   <oxsl:text>Registry</oxsl:text>
               </oxsl:when>
               <oxsl:otherwise>
                  <oxsl:text>Custom Service</oxsl:text>
               </oxsl:otherwise>
            </oxsl:choose>
            <oxsl:text>#</oxsl:text>
           </oxsl:for-each>
         </oxsl:when>
         <oxsl:when test="$rxsitype='DataCollection'">
            <oxsl:text>Data Collection#</oxsl:text>
         </oxsl:when>
         <oxsl:when test="$rxsitype='Organisation'">
            <oxsl:text>Organisation#</oxsl:text>
         </oxsl:when>
         <oxsl:when test="contains($rxsitype,'Standard') or $rxsitype='Authority'">
            <oxsl:text>VO Support#</oxsl:text>
         </oxsl:when>
         <oxsl:when test="not(@xsi:type)">
            <oxsl:text>Generic Resource</oxsl:text>
         </oxsl:when>
         <oxsl:otherwise><oxsl:value-of select="$rxsitype"/></oxsl:otherwise>
      </oxsl:choose>
   </oxsl:template>

   <!--
     -  create a string value 
     -->
   <oxsl:template name="mkstrval">
      <oxsl:param name="valnodes"/>
      <oxsl:param select="false()" name="asarray"/>

      <oxsl:text>'</oxsl:text>
      <oxsl:choose>
         <oxsl:when test="$asarray">
            <oxsl:text>#</oxsl:text>
            <oxsl:for-each select="$valnodes">
               <oxsl:call-template name="escapeQuotes">
                  <oxsl:with-param select="normalize-space(.)" name="text"/>
               </oxsl:call-template>
               <oxsl:text>#</oxsl:text>
            </oxsl:for-each>
         </oxsl:when>
         <oxsl:otherwise>
            <oxsl:call-template name="escapeQuotes">
              <oxsl:with-param select="normalize-space($valnodes)" name="text"/>
            </oxsl:call-template>
         </oxsl:otherwise>
      </oxsl:choose>
      <oxsl:text>'</oxsl:text>
   </oxsl:template>

   <!--
     -  escape any single quotes found in a string value
     -->
   <oxsl:template name="escapeQuotes">
      <oxsl:param name="text"/>
      <oxsl:param name="quote">'</oxsl:param>
      
      <oxsl:choose>
         <oxsl:when test="contains($text,$quote)">
            <oxsl:value-of select="substring-before($text,$quote)"/>
            <oxsl:value-of select="$quote"/>
            <oxsl:value-of select="$quote"/>
            <oxsl:call-template name="escapeQuotes">
               <oxsl:with-param select="substring-after($text,$quote)" name="text"/>
               <oxsl:with-param select="$quote" name="quote"/>
            </oxsl:call-template>
         </oxsl:when>
         <oxsl:otherwise>
            <oxsl:value-of select="$text"/>
         </oxsl:otherwise>
      </oxsl:choose>      
   </oxsl:template>

   <oxsl:template priority="-2.0" match="*">
      <oxsl:message>
         <oxsl:text>warning: </oxsl:text>
         <oxsl:value-of select="name()"/>
         <oxsl:text> (xsi:type=</oxsl:text>
         <oxsl:value-of select="@xsi:type"/>
         <oxsl:text>): 
   Unknown extension (or possible template error) </oxsl:text>
      </oxsl:message>
   </oxsl:template>
   
</oxsl:stylesheet>
