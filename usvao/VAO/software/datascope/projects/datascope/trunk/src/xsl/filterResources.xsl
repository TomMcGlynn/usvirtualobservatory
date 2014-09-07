<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1"
	exclude-result-prefixes="vo">

<!-- 	<xsl:strip-space elements="*"/> -->
	<xsl:strip-space elements="vo:TABLEDATA"/>

<!-- 	<xsl:param name="resources">ivo://ned.ipac/Image,ivo://vopdc.obspm/gepi/vopsat,ivo://eso.org/dss</xsl:param> -->
	<xsl:param name="resources"/>
		
	<xsl:variable name="identifierField">
		<xsl:for-each select="/vo:VOTABLE/vo:RESOURCE/vo:TABLE/vo:FIELD">
			<xsl:if test="@ID='identifier' or @name='identifier'">
				<xsl:value-of select="position()"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:variable>
	
	<xsl:template match="vo:TABLE">
		<xsl:copy>
			<xsl:apply-templates select="node()"/>
		</xsl:copy>
	</xsl:template>

	<xsl:template match="vo:TABLEDATA/vo:TR">
		<xsl:variable name="nodeValue" select="vo:TD[position()=$identifierField]"/>
<!-- 		<xsl:message>Value of nodeValue is #<xsl:value-of select="$nodeValue"/>#</xsl:message> -->
		<xsl:variable name="matchesResource">
			<xsl:call-template name="testResources">
				<xsl:with-param name="nodeValue" select="$nodeValue"/>
				<xsl:with-param name="resourceList" select="$resources"/>
			</xsl:call-template>
		</xsl:variable>
<!-- 		<xsl:message>Value of matchesResource is <xsl:value-of select="$matchesResource"/></xsl:message> -->
		<xsl:if test="$matchesResource='true'">
 			<xsl:copy>
				<xsl:apply-templates select="@*|node()"/>
			</xsl:copy>		
		</xsl:if>
	</xsl:template>
	
	<xsl:template name="testResources">
		<xsl:param name="nodeValue"/>
		<xsl:param name="resourceList"/>
		<xsl:variable name="first" select="substring-before($resourceList, ',')"/>
    	<xsl:variable name="rest" select="substring-after($resourceList, ',')"/>
<!-- 		<xsl:message>Value of first, rest is #<xsl:value-of select="$first"/>#<xsl:value-of select="$rest"/>#</xsl:message> -->
<!-- 		<xsl:message>Value of resourceList is #<xsl:value-of select="$resourceList"/>#</xsl:message> -->
    	<xsl:choose>
			<xsl:when test="$first and contains($nodeValue, $first)">true</xsl:when>
			<xsl:when test="not($rest) and contains($nodeValue, $resourceList)">true</xsl:when>
			<xsl:otherwise>
    			<xsl:if test="$rest">
	    			<xsl:call-template name="testResources">
						<xsl:with-param name="nodeValue" select="$nodeValue"/>
						<xsl:with-param name="resourceList" select="$rest"/>
       				</xsl:call-template>
	    		</xsl:if>
    		</xsl:otherwise>
    	</xsl:choose>
	</xsl:template>
	

    <xsl:template match="@*|node()">
<!-- 		<xsl:message>In generic template, node -->
<!-- 			<xsl:value-of select="name()"/> -->
<!-- 		</xsl:message> -->
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>
    
    <xsl:template name="start" match="/">
        <xsl:copy>
            <xsl:apply-templates/>
        </xsl:copy>
    </xsl:template>

</xsl:stylesheet>