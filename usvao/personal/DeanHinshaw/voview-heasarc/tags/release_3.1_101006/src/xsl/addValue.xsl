<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" 
   xmlns:v1="http://vizier.u-strasbg.fr/VOTable"
   xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
   xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"
   xmlns:v4="http://www.ivoa.net/xml/VOTable/v1.2" 
   exclude-result-prefixes="vo v1 v2 v3 v4"
   xmlns:d="powers_of_10"
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



<xsl:output method="xml" />
<xsl:variable name="fieldlist" select="//FIELD|//vo:FIELD|//v1:FIELD|//v2:FIELD|//v3:FIELD|//v4:FIELD"/>

<xsl:template match="TABLEDATA|vo:TABLEDATA|v1:TABLEDATA|v2:TABLEDATA|v3:TABLEDATA|v4:TABLEDATA">
   <xsl:copy><!-- copy the TABLEDATA element -->
      <xsl:for-each select="TR|vo:TR|v1:TR|v2:TR|v3:TR|v4:TR">
         <xsl:copy><!-- copy the TR element -->
            <xsl:for-each select="TD|vo:TD|v1:TD|v2:TD|v3:TD|v4:TD">
               <xsl:variable name="posit" select="position()"/>
               <xsl:choose>
                  <!-- currently, only the doubles show up as exponential notation, but will probably need to add others here -->
<!--
                  <xsl:when test="$fieldlist[position()=$posit]/@datatype='double'">
-->
                  <xsl:when test="contains('|float|double|',concat('|',$fieldlist[position()=$posit]/@datatype,'|'))">
                     <xsl:copy >
                        <xsl:attribute name='val'>
                           <xsl:call-template name="SciNum">
                              <xsl:with-param name="num" select="." />
                           </xsl:call-template>
                        </xsl:attribute><!-- apparently must do attribute first -->
                        <xsl:value-of select="." />
                     </xsl:copy>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:copy><!-- copy just the TD element -->
                        <xsl:value-of select="." />
                     </xsl:copy>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:for-each>
         </xsl:copy>
         <xsl:value-of select="string('&#xA;')" />
      </xsl:for-each>
   </xsl:copy>
</xsl:template>

<!-- standard copy template -->
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
      <xsl:when test="contains($input, 'e')" >
         <xsl:variable name="man" select="substring-before($input,'e')" />
         <xsl:variable name="exp" select="substring-after($input,'e')" />
         <!-- Offset depends on what we included above -->
         <xsl:value-of select="$man * $pows[$exp + 31]" />
      </xsl:when>
      <xsl:otherwise>
         <xsl:value-of select="translate($num, '+', '')" />
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

</xsl:stylesheet>
