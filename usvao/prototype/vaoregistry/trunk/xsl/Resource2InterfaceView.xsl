<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns:v="http://www.ivoa.net/xml/VOTable/v1.1" 
                xmlns="http://www.ivoa.net/xml/VOTable/v1.1" 
                version="1.0">

   <xsl:param name="instep" select="'  '"/>

   <xsl:template match="/">
      <xsl:apply-templates select="v:VOTABLE" />
   </xsl:template>

   <xsl:template match="@*|*">
      <xsl:copy>
         <xsl:apply-templates select="@*|*|child::text()" />
      </xsl:copy>
   </xsl:template>

   <xsl:template match="v:TABLE[@name='results']">
      <xsl:copy>
         <xsl:apply-templates select="@*" />

         <!-- replace the capabilityName FIELD with a resourceID field -->
         <xsl:apply-templates 
              select="child::node()[following-sibling::v:FIELD[@name='capabilityName']]" />
         <xsl:for-each select="v:FIELD[@name='capabilityName']">
            <xsl:copy>
               <xsl:apply-templates 
                    select="@*[local-name()!='ID' and local-name()!='name']" />
               <xsl:attribute name="name">
                  <xsl:text>resourceID</xsl:text>
               </xsl:attribute>
               <xsl:attribute name="ID">
                  <xsl:text>resourceID</xsl:text>
               </xsl:attribute>
            </xsl:copy>
         </xsl:for-each>

         <xsl:apply-templates 
              select="child::node()[preceding-sibling::v:FIELD[@name='capabilityName']]" />

      </xsl:copy>
   </xsl:template>

   <!--
     -  drop the interfaces GROUP
     -->
   <xsl:template match="v:GROUP[@name='interfaces']"/>

   <xsl:template match="v:TABLEDATA[ancestor::v:TABLE[@name='results']]">

      <xsl:copy>
         <xsl:apply-templates select="@*" />
         <xsl:apply-templates select="child::node()" mode="split">
            <xsl:with-param name="tagpos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">categories</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="idpos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">identifier</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="snpos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">shortName</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="namepos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">capabilityName</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="classpos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">capabilityClass</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="stdidpos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">capabilityStandardID</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="valpos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">capabilityValidationLevel</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="iclasspos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">interfaceClass</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="iverpos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">interfaceVersion</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="irolepos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">interfaceRole</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="urlpos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">accessURL</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="maxsrpos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">maxSearchRadius</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
            <xsl:with-param name="maxrecpos">
               <xsl:call-template name="getFieldPos">
                  <xsl:with-param name="name">maxRecords</xsl:with-param>
               </xsl:call-template>
            </xsl:with-param>
         </xsl:apply-templates>
      </xsl:copy>
   </xsl:template>

   <xsl:template match="v:TR" mode="split">
      <xsl:param name="tagpos" select="0"/>
      <xsl:param name="idpos" select="0"/>
      <xsl:param name="snpos" select="0"/>
      <xsl:param name="namepos" select="0"/>
      <xsl:param name="classpos" select="0"/>
      <xsl:param name="stdidpos" select="0"/>
      <xsl:param name="valpos" select="0"/>
      <xsl:param name="iclasspos" select="0"/>
      <xsl:param name="iverpos" select="0"/>
      <xsl:param name="irolepos" select="0"/>
      <xsl:param name="urlpos" select="0"/>
      <xsl:param name="maxsrpos" select="0"/>
      <xsl:param name="maxrecpos" select="0"/>
      <xsl:param name="ntr" select="-1"/>
      <xsl:param name="itr" select="0"/>

      <xsl:choose>
         <xsl:when test="$ntr &lt; 0">
            <!-- set up for recursion -->
            <xsl:variable name="n">
               <xsl:call-template name="countPounds">
                  <xsl:with-param name="in" 
                       select="substring-after(v:TD[number($namepos)],'#')"/>
               </xsl:call-template>
            </xsl:variable>

            <xsl:apply-templates select="." mode="split">
               <xsl:with-param name="itr" select="1"/>
               <xsl:with-param name="ntr" select="$n"/>
               <xsl:with-param name="tagpos" select="$tagpos"/>
               <xsl:with-param name="idpos" select="$idpos"/>
               <xsl:with-param name="snpos" select="$snpos"/>
               <xsl:with-param name="namepos" select="$namepos"/>
               <xsl:with-param name="classpos" select="$classpos"/>
               <xsl:with-param name="stdidpos" select="$stdidpos"/>
               <xsl:with-param name="valpos" select="$valpos"/>
               <xsl:with-param name="iclasspos" select="$iclasspos"/>
               <xsl:with-param name="iverpos" select="$iverpos"/>
               <xsl:with-param name="irolepos" select="$irolepos"/>
               <xsl:with-param name="urlpos" select="$urlpos"/>
               <xsl:with-param name="maxsrpos" select="$maxsrpos"/>
               <xsl:with-param name="maxrecpos" select="$maxrecpos"/>
            </xsl:apply-templates>
         </xsl:when>

         <xsl:otherwise>
            <!-- do a row and recurse -->
            <xsl:copy>
               <xsl:apply-templates select="@*" />
               <xsl:for-each select="v:TD">
                  <xsl:apply-templates select="preceding-sibling::node()[1]/self::text()" />
                  <xsl:apply-templates select="." mode="split">
                     <xsl:with-param name="tdpos" select="position()"/>
                     <xsl:with-param name="itr" select="$itr"/>
                     <xsl:with-param name="ntr" select="$ntr"/>
                     <xsl:with-param name="tagpos" select="$tagpos"/>
                     <xsl:with-param name="idpos" select="$idpos"/>
                     <xsl:with-param name="snpos" select="$snpos"/>
                     <xsl:with-param name="namepos" select="$namepos"/>
                     <xsl:with-param name="classpos" select="$classpos"/>
                     <xsl:with-param name="stdidpos" select="$stdidpos"/>
                     <xsl:with-param name="valpos" select="$valpos"/>
                     <xsl:with-param name="iclasspos" select="$iclasspos"/>
                     <xsl:with-param name="iverpos" select="$iverpos"/>
                     <xsl:with-param name="irolepos" select="$irolepos"/>
                     <xsl:with-param name="urlpos" select="$urlpos"/>
                     <xsl:with-param name="maxsrpos" select="$maxsrpos"/>
                     <xsl:with-param name="maxrecpos" select="$maxrecpos"/>
                  </xsl:apply-templates>
               </xsl:for-each>
               <xsl:apply-templates select="child::node()[position()=last()]/self::text()" />
            </xsl:copy>

            <xsl:if test="$itr &lt; $ntr">
               <xsl:text>
</xsl:text>
               <xsl:apply-templates select="." mode="split">
                  <xsl:with-param name="itr" select="$itr+1"/>
                  <xsl:with-param name="ntr" select="$ntr"/>
                  <xsl:with-param name="tagpos" select="$tagpos"/>
                  <xsl:with-param name="idpos" select="$idpos"/>
                  <xsl:with-param name="snpos" select="$snpos"/>
                  <xsl:with-param name="namepos" select="$namepos"/>
                  <xsl:with-param name="classpos" select="$classpos"/>
                  <xsl:with-param name="stdidpos" select="$stdidpos"/>
                  <xsl:with-param name="valpos" select="$valpos"/>
                  <xsl:with-param name="iclasspos" select="$iclasspos"/>
                  <xsl:with-param name="iverpos" select="$iverpos"/>
                  <xsl:with-param name="irolepos" select="$irolepos"/>
                  <xsl:with-param name="urlpos" select="$urlpos"/>
                  <xsl:with-param name="maxsrpos" select="$maxsrpos"/>
                  <xsl:with-param name="maxrecpos" select="$maxrecpos"/>
               </xsl:apply-templates>
            </xsl:if>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template name="getFieldPos">
     <xsl:param name="name"/>

     <xsl:variable name="pos">
        <xsl:for-each select="ancestor::v:TABLE[1]/v:FIELD">
           <xsl:if test="@name=$name">
              <xsl:copy-of select="position()"/>
           </xsl:if>
        </xsl:for-each>
     </xsl:variable>

     <xsl:choose>
        <xsl:when test="$pos=''">
           <xsl:copy-of select="0"/>
        </xsl:when>
        <xsl:otherwise>
           <xsl:copy-of select="number($pos)"/>
        </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <xsl:template name="countPounds">
      <xsl:param name="in"/>
      <xsl:param name="n" select="0"/>
      <xsl:choose>
         <xsl:when test="contains($in,'#')">
            <xsl:call-template name="countPounds">
               <xsl:with-param name="in" select="substring-after($in,'#')"/>
               <xsl:with-param name="n" select="$n+1"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:copy-of select="$n"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>   

   <xsl:template match="v:TD" mode="split">
      <xsl:param name="tagpos" select="0"/>
      <xsl:param name="idpos" select="0"/>
      <xsl:param name="snpos" select="0"/>
      <xsl:param name="namepos" select="0"/>
      <xsl:param name="classpos" select="0"/>
      <xsl:param name="stdidpos" select="0"/>
      <xsl:param name="valpos" select="0"/>
      <xsl:param name="iclasspos" select="0"/>
      <xsl:param name="iverpos" select="0"/>
      <xsl:param name="irolepos" select="0"/>
      <xsl:param name="urlpos" select="0"/>
      <xsl:param name="maxsrpos" select="0"/>
      <xsl:param name="maxrecpos" select="0"/>
      <xsl:param name="tdpos" select="-1"/>
      <xsl:param name="itr" select="0"/>

      <xsl:copy>
         <xsl:choose>
            <xsl:when test="$tdpos=$tagpos">
               <xsl:apply-templates select=".." mode="selectTag">
                  <xsl:with-param name="tagpos" select="$tagpos"/>
                  <xsl:with-param name="idpos" select="$idpos"/>
                  <xsl:with-param name="snpos" select="$snpos"/>
                  <xsl:with-param name="namepos" select="$namepos"/>
                  <xsl:with-param name="classpos" select="$classpos"/>
                  <xsl:with-param name="stdidpos" select="$stdidpos"/>
                  <xsl:with-param name="valpos" select="$valpos"/>
                  <xsl:with-param name="iclasspos" select="$iclasspos"/>
                  <xsl:with-param name="iverpos" select="$iverpos"/>
                  <xsl:with-param name="irolepos" select="$irolepos"/>
                  <xsl:with-param name="urlpos" select="$urlpos"/>
                  <xsl:with-param name="maxsrpos" select="$maxsrpos"/>
                  <xsl:with-param name="maxrecpos" select="$maxrecpos"/>
                  <xsl:with-param name="itr" select="$itr"/>
               </xsl:apply-templates>
            </xsl:when>

            <xsl:when test="$tdpos=$idpos">
               <xsl:value-of select="."/>
               <xsl:if test="../v:TD[number($namepos)]!='' and
                             ../v:TD[number($namepos)]!='#'">
                  <xsl:text>#</xsl:text>
                  <xsl:call-template name="getArrayElem">
                     <xsl:with-param name="in" 
                        select="substring-after(../v:TD[number($namepos)],'#')"/>
                     <xsl:with-param name="which" select="$itr"/>
                  </xsl:call-template>
               </xsl:if>
            </xsl:when>

            <xsl:when test="$tdpos=$snpos">
               <xsl:variable name="nname">
                 <xsl:call-template name="countPounds">
                    <xsl:with-param name="in" 
                        select="substring-after(../v:TD[number($namepos)],'#')"/>
                 </xsl:call-template>
               </xsl:variable>
               <xsl:value-of select="."/>
               <xsl:if test="number($nname) > 1">
                  <xsl:text> [</xsl:text>
                  <xsl:call-template name="getArrayElem">
                     <xsl:with-param name="in" 
                        select="substring-after(../v:TD[number($namepos)],'#')"/>
                     <xsl:with-param name="which" select="$itr"/>
                  </xsl:call-template>
                  <xsl:text>]</xsl:text>
               </xsl:if>
            </xsl:when>

            <xsl:when test="$tdpos=$namepos">
               <xsl:value-of select="../v:TD[number($idpos)]"/>
            </xsl:when>

            <xsl:when test="$tdpos=$classpos or $tdpos=$stdidpos  or 
                            $tdpos=$valpos   or $tdpos=$iclasspos or 
                            $tdpos=$iverpos  or $tdpos=$irolepos  or 
                            $tdpos=$urlpos   or $tdpos=$maxsrpos  or 
                            $tdpos=$maxrecpos">
               <xsl:call-template name="getArrayElem">
                  <xsl:with-param name="in" select="substring-after(.,'#')"/>
                  <xsl:with-param name="which" select="$itr"/>
               </xsl:call-template>
            </xsl:when>

            <xsl:otherwise>
               <xsl:value-of select="."/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:copy>
   </xsl:template>

   <!--
     -  select out the correct tag for desired interface
     -->
   <xsl:template match="v:TR" mode="selectTag">
      <xsl:param name="tagpos" select="0"/>
      <xsl:param name="idpos" select="0"/>
      <xsl:param name="snpos" select="0"/>
      <xsl:param name="namepos" select="0"/>
      <xsl:param name="classpos" select="0"/>
      <xsl:param name="stdidpos" select="0"/>
      <xsl:param name="valpos" select="0"/>
      <xsl:param name="iclasspos" select="0"/>
      <xsl:param name="iverpos" select="0"/>
      <xsl:param name="irolepos" select="0"/>
      <xsl:param name="urlpos" select="0"/>
      <xsl:param name="maxsrpos" select="0"/>
      <xsl:param name="maxrecpos" select="0"/>
      <xsl:param name="itr" select="0"/>

      <xsl:variable name="tags" select="v:TD[number($tagpos)]"/>
      <xsl:variable name="ntags">
         <xsl:call-template name="countPounds">
            <xsl:with-param name="in" select="substring-after($tags,'#')"/>
         </xsl:call-template>
      </xsl:variable>

      <xsl:choose>
         <xsl:when test="$ntags=1">
            <xsl:value-of 
                 select="substring-before(substring-after($tags,'#'),'#')"/>
         </xsl:when>
         <xsl:otherwise>

            <xsl:variable name="capclass">
               <xsl:call-template name="getArrayElem">
                  <xsl:with-param name="in" select="substring-after(v:TD[number($classpos)],'#')"/>
                  <xsl:with-param name="which" select="number($itr)"/>
               </xsl:call-template>
            </xsl:variable>

            <xsl:variable name="iclass">
               <xsl:call-template name="getArrayElem">
                  <xsl:with-param name="in" select="substring-after(v:TD[number($iclasspos)],'#')"/>
                  <xsl:with-param name="which" select="number($itr)"/>
               </xsl:call-template>
            </xsl:variable>

            <xsl:variable name="tag">
               <xsl:choose>
                  <xsl:when test="$capclass='ConeSearch' or $capclass='SkyNode'">
                     <xsl:text>Catalog</xsl:text>
                  </xsl:when>
                  <xsl:when test="$capclass='SimpleImageAccess'">
                     <xsl:text>Images</xsl:text>
                  </xsl:when>
                  <xsl:when test="$capclass='SimpleSpectralAccess'">
                     <xsl:text>Spectra</xsl:text>
                  </xsl:when>
                  <xsl:when test="$iclass='WebBrowser'">
                     <xsl:text>Web Page</xsl:text>
                  </xsl:when>
                  <xsl:when test="$iclass='ParamHTTP'">
                     <xsl:text>HTTP Request</xsl:text>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="$iclass"/>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:variable>

            <xsl:value-of select="$tag"/>

         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>
   

   <!--
     -  select the i-th value from a #-delimited array of values.  An empty 
     -  string is returned if input does not contian enough values.  Any leading
     -  #-signs should have already been striped from the input value before
     -  calling.
     -  @param delim   the array delimiter (default: #)
     -  @param in      the input array
     -  @param which   the number of array position to grab
     -  @param i       the position of the first element in array
     -->
   <xsl:template name="getArrayElem">
       <xsl:param name="delim" select="'#'"/>
       <xsl:param name="in" select="$delim"/>
       <xsl:param name="which" select="0"/>
       <xsl:param name="i" select="1"/>

       <xsl:variable name="nxt" select="substring-before($in, $delim)"/>

       <xsl:choose>
          <xsl:when test="$i = $which">
             <xsl:choose>
                <xsl:when test="contains($in,$delim)">
                   <xsl:value-of select="substring-before($in,$delim)"/>
                </xsl:when>
                <xsl:otherwise>
                   <xsl:value-of select="$in"/>
                </xsl:otherwise>
             </xsl:choose>
          </xsl:when>

          <xsl:when test="$i &lt; $which">
             <xsl:call-template name="getArrayElem">
                <xsl:with-param name="delim" select="$delim"/>
                <xsl:with-param name="in" select="substring-after($in,$delim)"/>
                <xsl:with-param name="which" select="$which"/>
                <xsl:with-param name="i" select="$i+1"/>
             </xsl:call-template>
          </xsl:when>
       </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
