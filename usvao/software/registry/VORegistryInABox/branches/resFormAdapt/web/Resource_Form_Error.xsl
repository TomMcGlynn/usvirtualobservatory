<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0"
                version="1.0">

   <xsl:output method="html" encoding="UTF-8" />

   <xsl:include href="Resource_Form_Site.xsl"/>

   <xsl:template match="/">
     <xsl:apply-templates select="." mode="site">
        <xsl:with-param name="title">Error: Resource Registration</xsl:with-param>
     </xsl:apply-templates>
   </xsl:template>

   <xsl:template match="/" mode="appbody">
<h1>Error Occurred While Processing Your Request</h1>

Unfortunately, while handling your request, this service encountered a problem:
<blockquote>
<strong><em><xsl:value-of select="Error"/></em></strong>
</blockquote>

You might try your request again; if the problem persists, please
contact us at feedback@us-vo.org.

   </xsl:template>

</xsl:stylesheet>
