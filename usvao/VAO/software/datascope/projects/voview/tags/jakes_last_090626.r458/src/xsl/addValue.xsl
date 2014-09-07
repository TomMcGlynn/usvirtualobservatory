<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" 
   xmlns:v1="http://vizier.u-strasbg.fr/VOTable"
   xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
   xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"
   xmlns:d="powers of 10"
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



<xsl:output method="xml" />
<xsl:variable name="fieldlist" select="//FIELD|//vo:FIELD|//v1:FIELD|//v2:FIELD|//v3:FIELD"/>

<xsl:template match="TABLEDATA|vo:TABLEDATA|v1:TABLEDATA|v2:TABLEDATA|v3:TABLEDATA">
   <xsl:copy><!-- copy the TABLEDATA element -->
      <xsl:for-each select="TR|vo:TR|v1:TR|v2:TR|v3:TR">
         <xsl:copy><!-- copy the TR element -->
            <xsl:for-each select="TD|vo:TD|v1:TD|v2:TD|v3:TD">
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
<!--
   This just wasn't working properly.  Small numbers would be returned as 0;
                           <xsl:call-template name="Scientific">
                              <xsl:with-param name="Num" select="." />
                           </xsl:call-template>
-->
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

<xsl:template match="@*|node()">
   <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
   </xsl:copy>
</xsl:template>

<xsl:template name="Scientific">
   <xsl:param name="Num"/>
   <xsl:param name="Factor">1</xsl:param>
   <xsl:variable name="Esplit">
      <xsl:choose>
         <xsl:when test="contains($Num,'E')">E</xsl:when>
         <xsl:otherwise>e</xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:variable name="result">
      <xsl:choose>
         <xsl:when test="not(contains($Num,$Esplit))">
            <xsl:value-of select="$Num"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:variable name="m" select="substring-before($Num,$Esplit)"/>
            <xsl:variable name="e" select="substring-after($Num,$Esplit)"/>
            <xsl:choose>
               <xsl:when test="substring($e,1,1)='+'">
                  <xsl:call-template name="Scientific_Helper">
                     <xsl:with-param name="m" select="$m"/>
                     <xsl:with-param name="e" select="substring($e,2)"/>
                  </xsl:call-template>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:call-template name="Scientific_Helper">
                     <xsl:with-param name="m" select="$m"/>
                     <xsl:with-param name="e" select="$e"/>
                  </xsl:call-template>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:value-of select="format-number(number($result)*number($Factor),'#0.#####')"/>
</xsl:template>

<xsl:template name="Scientific_Helper">
   <xsl:param name="m"/>
   <xsl:param name="e"/>
   <xsl:choose>
      <xsl:when test="not(number($e)) or $e = 0">
         <xsl:value-of select="$m"/>
      </xsl:when>
      <xsl:when test="$e &gt; 0">
         <xsl:variable name="factor">1<xsl:call-template name="Nzeros">
               <xsl:with-param name="N" select="$e"/>
            </xsl:call-template>
         </xsl:variable>
         <xsl:value-of select="$m * $factor"/>
      </xsl:when>
      <xsl:otherwise>
         <xsl:variable name="factor">1<xsl:call-template name="Nzeros">
               <xsl:with-param name="N" select="-$e"/>
            </xsl:call-template>
         </xsl:variable>
         <xsl:value-of select="$m div $factor"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<!--
Make a string of e zeros using divide-and-conquer recursion.
Should be called only with N > 0;  returns incorrect value if N<=0
-->

<xsl:template name="Nzeros">
   <xsl:param name="N"/>
   <xsl:choose>
      <xsl:when test="$N &lt; 2">0</xsl:when>
      <xsl:otherwise>
         <xsl:variable name="Nhalf" select="floor($N div 2)"/>
         <xsl:variable name="shalf">
            <xsl:call-template name="Nzeros">
               <xsl:with-param name="N" select="$Nhalf"/>
            </xsl:call-template>
         </xsl:variable>
         <xsl:choose>
            <xsl:when test="2*$Nhalf = $N">
               <xsl:value-of select="concat($shalf,$shalf)"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="concat($shalf,$shalf,'0')"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:otherwise>
   </xsl:choose>
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
         <xsl:value-of select="$num" />
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

</xsl:stylesheet>
