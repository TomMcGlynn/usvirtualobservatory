<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns:vs="http://www.ivoa.net/xml/VODataService/v1.0" 
                xmlns:stc="http://www.ivoa.net/xml/STC/stc-v1.30.xsd" 
                xmlns:vr="http://www.ivoa.net/xml/VOResource/v1.0" 
                xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0" 
                xmlns:vot="http://www.ivoa.net/xml/VOTable/v1.1" 
                xmlns="http://www.ivoa.net/xml/VOTable/v1.1" 
                version="1.0">

   <xsl:template match="/">
      <xsl:apply-templates select="ri:VOResources" />
   </xsl:template>

   <xsl:template match="ri:VOResources" xml:space="preserve">
<VOTABLE xmlns="http://www.ivoa.net/xml/VOTable/v1.1">
   <DESCRIPTION>Registry Search Results</DESCRIPTION>
   <RESOURCE name="Search Results">
      <TABLE name="results">
         <FIELD name="title" datatype="char" arraysize="*"/>
         <FIELD name="shortName" datatype="char" arraysize="*"/>
         <FIELD name="identifier" datatype="char" arraysize="*"/>
         <FIELD name="updated" datatype="char" arraysize="*"/>
         <FIELD name="publisher" datatype="char" arraysize="*"/>
         <FIELD name="version"  datatype="char" arraysize="*"/>
         <FIELD name="subject" datatype="char" arraysize="*"/>
         <FIELD name="reference URL" datatype="char" arraysize="*"/>
         <FIELD name="type" datatype="char" arraysize="*"/>
         <FIELD name="content level" datatype="char" arraysize="*"/>
         <FIELD name="waveband" datatype="char" arraysize="*"/>
         <FIELD name="regionOfRegard" datatype="int" unit="arcsec"/>
         <FIELD name="capability class" datatype="char" arraysize="*"/>
         <FIELD name="capability standard ID" datatype="char" arraysize="*"/>
         <FIELD name="capability validation level" datatype="char" arraysize="*"/>
         <FIELD name="interface class" datatype="char" arraysize="*"/>
         <FIELD name="interface version" datatype="char" arraysize="*"/>
         <FIELD name="interface role" datatype="char" arraysize="*"/>
         <FIELD name="accessURL" datatype="char" arraysize="*"/>
         <FIELD name="supported input param" datatype="char" arraysize="*"/> 
         <FIELD name="max search radius" datatype="int"/>
         <FIELD name="max no. of records" datatype="int"/>
         <DATA>
            <TABLEDATA>
               <xsl:apply-templates select="ri:Resource" />
            </TABLEDATA>
         </DATA>
      </TABLE>
   </RESOURCE>

</VOTABLE>
   </xsl:template>

   <xsl:template match="ri:Resource">
      <TR><xsl:text>
</xsl:text>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="title" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="shortName" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="identifier" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="@updated" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="curation/publisher" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="curation/version" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/subject" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/referenceURL" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/type" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/contentLevel" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="coverage/waveband" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="coverage/stc:STCResourceProfile/stc:AstroCoords/stc:Position1D/stc:Size" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="capability/@xsi:type" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="capability/@standardID" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="capability/validationLevel" />
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="capability/interface/@xsi:type" />
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="capability/interface/@version" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="capability/interface/@role" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="capability/interface/accessURL" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="capability/interface/param/name" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="capability/maxSearchRadius|capability/maxSR" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="capability/maxRecords" />   
         </xsl:call-template>
      </TR><xsl:text>
</xsl:text>
   </xsl:template>

   <xsl:template name="valOrNull">
      <xsl:param name="val"/>
      <xsl:param name="asarray" select="false()"/>
      <xsl:variable name="count" select="count($val)"/>

      <xsl:text>   </xsl:text>
      <TD>
         <xsl:choose>
            <xsl:when test="$asarray">
               <xsl:if test="count($val)>0">
                  <xsl:text>#</xsl:text>
               </xsl:if>
               <xsl:for-each select="$val">
                  <xsl:value-of select="normalize-space(.)"/>
                  <xsl:text>#</xsl:text>
               </xsl:for-each>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="normalize-space($val)"/>
            </xsl:otherwise>
         </xsl:choose>
      </TD><xsl:text>
</xsl:text>
   </xsl:template>

</xsl:stylesheet>
