<xsl:stylesheet version = '1.0'
     xmlns:xsl='http://www.w3.org/1999/XSL/Transform'>

<xsl:template match="*|/"><xsl:apply-templates/></xsl:template>

<xsl:template match="text()|@*"><xsl:value-of select="."/></xsl:template>


<xsl:template match="INFO">
<p> INFO <xsl:value-of select="."/> </p>
</xsl:template>

<xsl:template match="VOTABLE">
 <html>
  <head>
  <LINK rel="stylesheet" href="http://voservices.net/registry/styles.css"/>
  </head>

<table>
  <tr>
    <td><a class="plain" href="http://www.us-vo.org">
	<img src="./images/VAO_logo_100.png"/>
      </a> </td>
    <td>
      <h1> VAO Directory </h1>
    </td>
  </tr>
  <tr BGCOLOR="#6BA5D7">
    <td colspan="2" height="4"><font size="-4"> </font></td>
  </tr>
</table>

  <h2>
         <xsl:value-of select="DESCRIPTION"/>
  </h2>
   <xsl:apply-templates select="RESOURCE"/>
 </html>
</xsl:template>

<xsl:template match="RESOURCE">
  <xsl:apply-templates select="INFO"/>
  <h2> Parameters </h2>
  <table >
    <xsl:apply-templates select="PARAM"/>
  </table>
  <xsl:apply-templates select="TABLE"/>
</xsl:template>

<xsl:template match="FIELD">
   <th><table>
	<tr><th><xsl:value-of select="@ID"/></th></tr>
	<tr><th><xsl:value-of select="@ucd"/></th></tr>
   </table></th>
</xsl:template>

<xsl:template match="INFO">
	<p><b><xsl:value-of select="@name"/> : <xsl:value-of select="@value"/> : </b>
	<xsl:value-of select="."/></p>
</xsl:template>

<xsl:template match="PARAM">
   <tr>
	<td><b><xsl:value-of select="@name"/></b></td>
	<td><xsl:value-of select="DESCRIPTION"/></td>
        <td><xsl:value-of select="@value"/></td>
   </tr>
</xsl:template>

<xsl:template match="TABLE">
  <table border="1">
	<tr>
		<xsl:apply-templates select="FIELD"/>
	</tr>
	<xsl:apply-templates select="DATA/TABLEDATA"/>

   </table>
</xsl:template>

<xsl:template match="TABLEDATA">
	<xsl:apply-templates select="TR"/>
</xsl:template>

<xsl:template match="TR">
	<tr><xsl:apply-templates select="TD"/></tr>
</xsl:template>

<xsl:template match="TD">
	<td><xsl:value-of select="."/></td>
</xsl:template>

</xsl:stylesheet>