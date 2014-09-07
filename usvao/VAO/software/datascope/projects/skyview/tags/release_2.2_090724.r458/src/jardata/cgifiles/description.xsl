<?xml version="1.0"?>

<xsl:stylesheet 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                version="1.0">

    <xsl:template match="/Survey">
        <HTML>
	  <xsl:apply-templates select="Name" />
	  <xsl:apply-templates select="ShortName" />
	  <xsl:apply-templates select="Description" />
	  <xsl:apply-templates select="MetaTable" />
	</HTML>
    </xsl:template>
    
    <xsl:template match="ShortName">
       <B>Short name[s] used to specify survey:</B>
       <xsl:value-of select="." disable-output-escaping="yes" />
    </xsl:template>
    
    <xsl:template match="Name">
       <H2> <xsl:value-of select="." disable-output-escaping="yes"/> </H2>
    </xsl:template>
    
    <xsl:template match="Description">
       <P/><B> Description </B><BR/>
       <xsl:value-of select="." disable-output-escaping="yes"/>
       <P/>
    </xsl:template>
    
    <xsl:template match="MetaTable">
    
        <TABLE>
        <xsl:for-each select="*">
	   <TR>
           <TH align="right"><xsl:value-of select="name()" /></TH>
           <TD><xsl:value-of select="." disable-output-escaping="yes"/></TD>
	   </TR>
	</xsl:for-each>
	</TABLE>
	
    </xsl:template>
        
</xsl:stylesheet>
