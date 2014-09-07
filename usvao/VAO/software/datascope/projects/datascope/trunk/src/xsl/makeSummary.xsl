<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1"
	xmlns:uws="http://www.ivoa.net/xml/UWS/v1.0/UWS.xsd"
	exclude-result-prefixes="vo uws">
	
	<xsl:output method="xml" indent="yes" omit-xml-declaration="no" standalone="no"/>

<!-- 	<xsl:strip-space elements="*"/> -->
<!-- 	<xsl:strip-space elements="vo:TABLEDATA"/> -->
		
	<xsl:variable name="rows" select="/vo:VOTABLE/vo:RESOURCE/vo:TABLE/vo:DATA/vo:TABLEDATA/vo:TR"/>
	<xsl:variable name="fields" select="/vo:VOTABLE/vo:RESOURCE/vo:TABLE/vo:FIELD"/>
			
	<xsl:variable name="statusField">
		<xsl:for-each select="$fields">
			<xsl:if test="@name='serviceStatus' or @ID='serviceStatus'">
				<xsl:value-of select="position()"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:variable>
	
	<xsl:variable name="hitsField">
		<xsl:for-each select="$fields">
			<xsl:if test="@name='hits' or @ID='hits'">
				<xsl:value-of select="position()"/>
			</xsl:if>
		</xsl:for-each>
	</xsl:variable>
	

	<xsl:template name="numberComplete">
		<xsl:param name="rowNodes"/>

		<xsl:choose>
			<xsl:when test="count($rowNodes)=1">
				<xsl:choose>
					<xsl:when test="$rowNodes='COMPLETED' or $rowNodes='FILTERED' or $rowNodes='ERROR' or $rowNodes='NOTINVOKED'">
						<xsl:value-of select="1"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="0"/>						
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			
			<xsl:when test="count($rowNodes) &gt; 0">
				<xsl:variable name="halfCount" select="floor(count($rowNodes) div 2)"/>
				<xsl:variable name="sum1">
					<xsl:call-template name="numberComplete">
						<xsl:with-param name="rowNodes" select="$rowNodes[position() &lt;= $halfCount]"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="sum2">
					<xsl:call-template name="numberComplete">
						<xsl:with-param name="rowNodes" select="$rowNodes[position() &gt; $halfCount]"/>
					</xsl:call-template>
				</xsl:variable>
<!-- 				<xsl:message> -->
<!-- 					numberComplete: halfCount, sum1, sum2: -->
<!-- 					<xsl:value-of select="$halfCount"/>, -->
<!-- 					<xsl:value-of select="$sum1"/>, -->
<!-- 					<xsl:value-of select="$sum2"/>, -->
<!-- 				</xsl:message> -->
				<xsl:value-of select="$sum1+$sum2"/>
			</xsl:when>
			
			<xsl:otherwise>
<!-- 				<xsl:message> -->
<!-- 					numberComplete: Returning Zero, -->
<!-- 				</xsl:message> -->
				<xsl:value-of select="0"/>			
			</xsl:otherwise>
		</xsl:choose>		
	</xsl:template>
	

	<xsl:template name="numberOfHits">
		<xsl:param name="rowNodes"/>
				
		<xsl:choose>
			<xsl:when test="count($rowNodes)=1">
<!-- 				<xsl:message> -->
<!-- 					Returning rowNodes: -->
<!-- 					<xsl:value-of select="$rowNodes"/>, -->
<!-- 				</xsl:message> -->
				<xsl:value-of select="$rowNodes"/>
			</xsl:when>
			
			<xsl:when test="count($rowNodes) &gt; 0">
				<xsl:variable name="halfCount" select="floor(count($rowNodes) div 2)"/>
				<xsl:variable name="sum1">
					<xsl:call-template name="numberOfHits">
						<xsl:with-param name="rowNodes" select="$rowNodes[position() &lt;= $halfCount]"/>
					</xsl:call-template>
				</xsl:variable>
				<xsl:variable name="sum2">
					<xsl:call-template name="numberOfHits">
						<xsl:with-param name="rowNodes" select="$rowNodes[position() &gt; $halfCount]"/>
					</xsl:call-template>
				</xsl:variable>
<!-- 				<xsl:message> -->
<!-- 					numberOfHits: halfCount, sum1, sum2: -->
<!-- 					<xsl:value-of select="$halfCount"/>, -->
<!-- 					<xsl:value-of select="$sum1"/>, -->
<!-- 					<xsl:value-of select="$sum2"/>, -->
<!-- 				</xsl:message> -->
				<xsl:value-of select="$sum1+$sum2"/>
			</xsl:when>
			
			<xsl:otherwise>
<!-- 				<xsl:message> -->
<!-- 					numberOfHits: Returning Zero, -->
<!-- 				</xsl:message> -->
				<xsl:value-of select="0"/>			
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	
	
	<xsl:template match="/">
		<uws:job>
			<uws:result id="numberOfHits">
				<xsl:call-template name="numberOfHits">
					<xsl:with-param name="rowNodes" select="$rows/vo:TD[position()=$hitsField]"/>
				</xsl:call-template>
			</uws:result>
		
			<xsl:variable name="completed">
				<xsl:call-template name="numberComplete">
					<xsl:with-param name="rowNodes" select="$rows/vo:TD[position()=$statusField]"/>
				</xsl:call-template>
			</xsl:variable>
		
			<uws:result id="percentComplete">
				<xsl:value-of select="$completed div count($rows)"/>
			</uws:result>
		</uws:job>
	</xsl:template>
	

</xsl:stylesheet>