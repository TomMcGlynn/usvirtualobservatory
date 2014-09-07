<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:top="ivoa.net.riws.v10"
    xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0"
    xmlns:stc="http://www.ivoa.net/xml/STC/stc-v1.30.xsd"
>
 
  <xsl:variable name="uc" select="ABCDEFGHIJKLMNOPQRSTUVWXYZ" />
  <xsl:variable name="lc" select="abcdefghijklmnopqrstuvwxyz" />
  <xsl:variable name="capstd">ivo://ivoa.net/std/SIA</xsl:variable>
  <xsl:variable name="capname">SIAP</xsl:variable>
  
  <xsl:variable name="nl" select="'&#xA;'" />
  
  <xsl:template name="init" match="/top:SearchResponse">
<ResourceList>
     <xsl:apply-templates />
</ResourceList>
  </xsl:template>

  <xsl:template name="start" match="ri:VOResources/ri:Resource">
 
<xsl:value-of select='$nl' />
<Resource>
<xsl:value-of select='$nl' />

  <Identifier>
     <xsl:value-of select="identifier"/>
  </Identifier>
  <xsl:value-of select='$nl' />
  
  <ShortName>
     <xsl:value-of select="shortName" />
  </ShortName>
  <xsl:value-of select='$nl' />
  
  <Title>
    <xsl:value-of select="title"/>  
  </Title>
  <xsl:value-of select='$nl' />

  <Description>
     <xsl:value-of select="content/description" />    
  </Description>
  <xsl:value-of select='$nl' />

  <ContactEmail>
     <xsl:value-of select="curation/contact/email" />     
  </ContactEmail>
  <xsl:value-of select='$nl' />
  
  <ContactName>
     <xsl:value-of select="curation/contact/name" />     
  </ContactName>
  <xsl:value-of select='$nl' />
 
  <ContentLevel>
     <xsl:value-of select="content/contentLevel" />     
  </ContentLevel>
  <xsl:value-of select='$nl' />

  <Contributor>
     <xsl:value-of select="curation/contributor" />   
  </Contributor>
  <xsl:value-of select='$nl' />

  <xsl:for-each select="coverage/waveband">
    <CoverageSpectral>
       <xsl:value-of select="." />
    </CoverageSpectral>
    <xsl:value-of select='$nl' />
  </xsl:for-each>

  
  <Creator>
     <xsl:value-of  select="curation/creator" />   
  </Creator>
  <xsl:value-of select='$nl' />

  <Date>
     <xsl:value-of select="@updated" />   
  </Date>
  <xsl:value-of select='$nl' />

  <EntrySize>
    <xsl:variable name="unit" select="coverage/stc:STCResourceProfile/stc:AstroCoords/stc:Position1D/stc:Size[@pos_unit]" />
    <xsl:variable name="val"  select="coverage/stc:STCResourceProfile/stc:AstroCoords/stc:Position1D/stc:Size" />
    <xsl:choose>
       <xsl:when test="$unit = 'arcsec'" >
	   <xsl:choose>
	       <xsl:when test="$val &gt; 0.1">	       
	           <xsl:value-of select="$val div 3600."/>
	       </xsl:when>
	       <xsl:otherwise>
		   <xsl:value-of select="$val" />
		</xsl:otherwise>
	    </xsl:choose>
	</xsl:when>
       <xsl:when test="$unit = 'arcmin'" >
	    <xsl:value-of select="$val div 60." />
	</xsl:when>
	<xsl:otherwise>
	    <xsl:value-of select="$val" />
	</xsl:otherwise>
    </xsl:choose>
  </EntrySize>
  <xsl:value-of select='$nl' />
   
  <Facility>
    <xsl:value-of select="facility" />
  </Facility>
  <xsl:value-of select='$nl' />
   
  <Instrument>
    <xsl:value-of select="instrument" />
  </Instrument>
  <xsl:value-of select='$nl' />
       
  <xsl:variable name="cap" select="capability[@standardID=$capstd]" />
  <MaxSR>
    <xsl:value-of select="$cap/maxQueryRegionSize/long" />
  </MaxSR>
  <xsl:value-of select='$nl' />
 
  <MaxRecords>
    <xsl:value-of select="$cap/maxRecords" />
  </MaxRecords>
  <xsl:value-of select='$nl' />

  <Publisher>
    <xsl:value-of select="curation/publisher" />
  </Publisher>
  <xsl:value-of select='$nl' />
  
  <ReferenceURL>
    <xsl:value-of select="content/referenceURL" />
  </ReferenceURL>
  <xsl:value-of select='$nl' />
   
  <ServiceType>
    <xsl:value-of select="$capname" />
  </ServiceType>
  <xsl:value-of select='$nl' />
 
  <ServiceURL>
    <xsl:value-of select="$cap/interface[@role='std']/accessURL" />
  </ServiceURL>
  <xsl:value-of select='$nl' />
  
  <xsl:for-each select="content/subject">
    <Subject>
       <xsl:value-of select="." />
    </Subject>
    <xsl:value-of select='$nl' />
  </xsl:for-each>
  
  <xsl:for-each select="content/type">
    <Type>
      <xsl:value-of select="." />
    </Type>
    <xsl:value-of select='$nl' />
  </xsl:for-each>
  
  <validationLevel>
    <xsl:value-of select="validationLevel" />
  </validationLevel>
  <xsl:value-of select='$nl' />
        
</Resource>  
<xsl:value-of select='$nl' />

  </xsl:template>
  
  <xsl:template name="error">
      No resources in file.
  </xsl:template>
  
  
</xsl:stylesheet>
