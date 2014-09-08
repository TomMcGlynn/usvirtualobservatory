<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" 
   xmlns:v1="http://vizier.u-strasbg.fr/VOTable"
   xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
   xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"
   xmlns:v4="http://www.ivoa.net/xml/VOTable/v1.2" 
   exclude-result-prefixes="vo v1 v2 v3 v4"
   >

	<xsl:strip-space elements="TABLEDATA vo:TABLEDATA v1:TABLEDATA v2:TABLEDATA v3:TABLEDATA v4:TABLEDATA"/>

	<xsl:template match="TABLEDATA/TR">
		<xsl:variable name="vovid" select="position()"/>
		<xsl:element name="TR">
			<xsl:attribute name="vovid">
				<xsl:value-of select="$vovid"/> 
			</xsl:attribute>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="vo:TABLEDATA/vo:TR">
		<xsl:variable name="vovid" select="position()"/>
		<xsl:element name="vo:TR">
			<xsl:attribute name="vovid">
				<xsl:value-of select="$vovid"/> 
			</xsl:attribute>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="v1:TABLEDATA/v1:TR">
		<xsl:variable name="vovid" select="position()"/>
		<xsl:element name="v1:TR">
			<xsl:attribute name="vovid">
				<xsl:value-of select="$vovid"/> 
			</xsl:attribute>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="v2:TABLEDATA/v2:TR">
		<xsl:variable name="vovid" select="position()"/>
		<xsl:element name="v2:TR">
			<xsl:attribute name="vovid">
				<xsl:value-of select="$vovid"/> 
			</xsl:attribute>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="v3:TABLEDATA/v3:TR">
		<xsl:variable name="vovid" select="position()"/>
		<xsl:element name="v3:TR">
			<xsl:attribute name="vovid">
				<xsl:value-of select="$vovid"/> 
			</xsl:attribute>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<xsl:template match="v4:TABLEDATA/v4:TR">
		<xsl:variable name="vovid" select="position()"/>
		<xsl:element name="v4:TR">
			<xsl:attribute name="vovid">
				<xsl:value-of select="$vovid"/> 
			</xsl:attribute>
			<xsl:copy-of select="@*"/>
			<xsl:apply-templates/>
		</xsl:element>
	</xsl:template>
	
	<!-- standard copy template -->
	<xsl:template match="@*|node()">
		<xsl:copy>
			<xsl:apply-templates select="@*"/>
			<xsl:apply-templates/>
		</xsl:copy>
	</xsl:template>	
</xsl:stylesheet>

