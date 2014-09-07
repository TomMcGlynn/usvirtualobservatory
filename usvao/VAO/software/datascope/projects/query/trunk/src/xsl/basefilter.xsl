<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:d="namespace for powers of 10"
   xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" 
   xmlns:v1="http://vizier.u-strasbg.fr/VOTable"
   xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
   xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"
   exclude-result-prefixes="vo v1 v2 v3"
   version="1.0" >

<!-- Start with 10^-30 -->
<d:p>0.000000000000000000000000000001</d:p>
<d:p>0.00000000000000000000000000001</d:p>
<d:p>0.0000000000000000000000000001</d:p>
<d:p>0.000000000000000000000000001</d:p>
<d:p>0.00000000000000000000000001</d:p>
<d:p>0.0000000000000000000000001</d:p>
<d:p>0.000000000000000000000001</d:p>
<d:p>0.00000000000000000000001</d:p>
<d:p>0.0000000000000000000001</d:p>
<d:p>0.000000000000000000001</d:p>
<d:p>0.00000000000000000001</d:p>
<d:p>0.0000000000000000001</d:p>
<d:p>0.000000000000000001</d:p>
<d:p>0.00000000000000001</d:p>
<d:p>0.0000000000000001</d:p>
<d:p>0.000000000000001</d:p>
<d:p>0.00000000000001</d:p>
<d:p>0.0000000000001</d:p>
<d:p>0.000000000001</d:p>
<d:p>0.00000000001</d:p>
<d:p>0.0000000001</d:p>
<d:p>0.000000001</d:p>
<d:p>0.00000001</d:p>
<d:p>0.0000001</d:p>
<d:p>0.000001</d:p>
<d:p>0.00001</d:p>
<d:p>0.0001</d:p>
<d:p>0.001</d:p>
<d:p>0.01</d:p>
<d:p>0.1</d:p>
<d:p>1</d:p>
<d:p>10</d:p>
<d:p>100</d:p>
<d:p>1000</d:p>
<d:p>10000</d:p>
<d:p>100000</d:p>
<d:p>1000000</d:p>
<d:p>10000000</d:p>
<d:p>100000000</d:p>
<d:p>1000000000</d:p>
<d:p>10000000000</d:p>
<d:p>100000000000</d:p>
<d:p>1000000000000</d:p>
<d:p>10000000000000</d:p>
<d:p>100000000000000</d:p>
<d:p>1000000000000000</d:p>
<d:p>10000000000000000</d:p>
<d:p>100000000000000000</d:p>
<d:p>1000000000000000000</d:p>
<d:p>10000000000000000000</d:p>
<d:p>100000000000000000000</d:p>
<d:p>1000000000000000000000</d:p>
<d:p>10000000000000000000000</d:p>
<d:p>100000000000000000000000</d:p>
<d:p>1000000000000000000000000</d:p>
<d:p>10000000000000000000000000</d:p>
<d:p>100000000000000000000000000</d:p>
<d:p>1000000000000000000000000000</d:p>
<d:p>10000000000000000000000000000</d:p>
<d:p>100000000000000000000000000000</d:p>
<d:p>1000000000000000000000000000000</d:p>
<d:p>10000000000000000000000000000000</d:p>
<d:p>100000000000000000000000000000000</d:p>
<d:p>1000000000000000000000000000000000</d:p>
<d:p>10000000000000000000000000000000000</d:p>
<d:p>100000000000000000000000000000000000</d:p>
<d:p>1000000000000000000000000000000000000</d:p>
<d:p>10000000000000000000000000000000000000</d:p>
<d:p>100000000000000000000000000000000000000</d:p>
<d:p>1000000000000000000000000000000000000000</d:p>
<d:p>10000000000000000000000000000000000000000</d:p>
<d:p>100000000000000000000000000000000000000000</d:p>
<d:p>1000000000000000000000000000000000000000000</d:p>
<d:p>10000000000000000000000000000000000000000000</d:p>
<d:p>100000000000000000000000000000000000000000000</d:p>
<d:p>1000000000000000000000000000000000000000000000</d:p>
<d:p>10000000000000000000000000000000000000000000000</d:p>
<d:p>100000000000000000000000000000000000000000000000</d:p>
<d:p>1000000000000000000000000000000000000000000000000</d:p>
<d:p>10000000000000000000000000000000000000000000000000</d:p>

<xsl:variable name="pows" select="document('')/*/d:p" />
    
<xsl:apply-templates select = "/" />
    
<!-- This placeholder template indicates
whether a given row sould be included in the output.
-->
<xsl:template name="rowMatch">
	<xsl:param name="fields" />
	1
</xsl:template>
    
<xsl:template match="TABLEDATA|vo:TABLEDATA|v1:TABLEDATA|v2:TABLEDATA|v3:TABLEDATA">
	<xsl:copy>
		<xsl:for-each select="TR|vo:TR|v1:TR|v2:Tr|v3:TR">
			<!-- define this here so that inheriting code needn't
					worry about name spaces.
			-->
			<xsl:variable name="fields" select="(TD|vo:TD|v1:TD|v2:TD|v3:TD)" />
			<xsl:variable name="include">
				<xsl:call-template name="rowMatch">
					<xsl:with-param name="fields" select="$fields" />
				</xsl:call-template>
			</xsl:variable>
			<xsl:if test="$include &gt; 0">
				<xsl:copy>
					<xsl:apply-templates select="@*|node()" />
				</xsl:copy>
			</xsl:if>
		</xsl:for-each>
	</xsl:copy>
</xsl:template>

<xsl:template match="@*|node()">
	<xsl:copy>
		<xsl:apply-templates select="@*|node()"/>
	</xsl:copy>
</xsl:template>
    
<xsl:template name="SciNum">
	<xsl:param name="num" />
	<!-- Get rid of +.  They cause problems is xslt1.0. -->
	<xsl:variable name="input" select="translate($num, 'E+', 'e')" />
		<xsl:choose>
			<xsl:when test="contains($num, 'e')" >
				<xsl:variable name="man" select="substring-before($input,'e')" />
				<xsl:variable name="exp" select="substring-after($input,'e')" />
				<xsl:value-of select="$man * $pows[$exp + 31]" />
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$num" />
			</xsl:otherwise>
	</xsl:choose>
</xsl:template>
    
</xsl:stylesheet>
