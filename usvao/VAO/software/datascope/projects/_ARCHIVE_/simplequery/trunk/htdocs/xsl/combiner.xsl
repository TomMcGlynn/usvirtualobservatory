<?xml version="1.0" encoding="UTF-8"?>
<!-- This stylesheet concatenates a series of
     homogeneous VOTables which are in a structure like:
     
     <VOTABLES>
       <VOTABLE_ENTRY><VOTABLE>...</VOTABLE>
       <VOTABLE_ENTRY><VOTABLE>...</VOTABLE>
       ...
     </VOTABLES>
     
     into a single VOTABLE where the first column
     of the VOTable is added with an index
     that indicates which original VOTABLE a given
     row's data derives from.
     
     This allows stream processing of a
     request to Mike Fitzpatrick's vodata program that
     includes multiple input positions.
     
     Extraneous information is stripped from the VOTable,
     and only FIELD and TR data is retained.
  -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" xsl:exclude-result-prefixes="vo">
<xsl:output method="html" />

<xsl:template match="/" >
    <xsl:call-template name="master" />
</xsl:template>
    
<xsl:template match="VOTABLES" name="master">
    <VOTABLE><RESOURCE><TABLE>
    <FIELD name="rec_id" type="int">
    <DESCRIPTION>Index of input position that gave this result </DESCRIPTION>
    </FIELD>
    <xsl:for-each select="//VOTABLE_ENTRY" >
       <xsl:if test="position()=1">
           <xsl:call-template name="copy-fields" />
       </xsl:if>
    </xsl:for-each>
    <DATA><TABLEDATA>
    <xsl:for-each select="//VOTABLE_ENTRY" >
       <xsl:call-template name="entry">
           <xsl:with-param name="index" select="@index" />
       </xsl:call-template>
    </xsl:for-each>
    </TABLEDATA></DATA>
    </TABLE></RESOURCE></VOTABLE>
</xsl:template>

<xsl:template match="VOTABLE_ENTRY" name="entry">
    <xsl:param name="index" />
    <xsl:for-each select=".//TR|.//vo:TR">
        <TR>
	<TD><xsl:value-of select="$index" /></TD>
        <xsl:for-each select="TD|vo:TD">
           <TD><xsl:value-of select="."/></TD>
        </xsl:for-each>
        </TR>
    </xsl:for-each>
</xsl:template>

<xsl:template name="copy-fields" >
    <xsl:for-each select=".//FIELD|.//vo:FIELD">
        <xsl:copy-of select="." />
    </xsl:for-each>
</xsl:template>

</xsl:stylesheet>
