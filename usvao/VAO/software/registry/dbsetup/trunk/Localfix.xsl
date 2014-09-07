<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:vr="http://www.ivoa.net/xml/VOResource/v1.0" 
                xmlns:vs="http://www.ivoa.net/xml/VODataService/v1.1" 
                xmlns:stc="http://www.ivoa.net/xml/STC/stc-v1.30.xsd" 
                xmlns:vs2="http://www.ivoa.net/xml/VODataService/v1.0" 
                xmlns:xlink="http://www.w3.org/1999/xlink" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns=""
                exclude-result-prefixes="#all" 
                version="2.0">

   <!-- 
     -  Stylesheet to convert VOResource records from VODataService v1.0
     -   to VODataService v1.1
     -->
   <xsl:output method="xml" encoding="UTF-8" indent="yes" />
   <xsl:variable name="autoIndent" select="'  '"/>

   <xsl:preserve-space elements="*"/>

   <!--
     -  If set, the updated atribute will be set to this value
     -->
   <xsl:param name="today"/>

   <!--
     -  If set, the output document will have a root element of this
     -  name and a namespace given by $resourceNS and it will contain
     -  the VOResource metadata.
     -
     -  the default setting will produce records
     -  that can be inserted directly into a Harvest response record.
     -->
   <xsl:param name="resourceElement">Resource</xsl:param>

   <!--
     -  If resourceName is set (with an non-empty value), the output 
     -  document will have a root element of $resourceName and a 
     -  this namespace.  It will contain the VOResource metadata; all 
     -  other wrapping elements from the input will be filtered out. 
     -->
   <xsl:param name="resourceNS">http://www.ivoa.net/xml/RegistryInterface/v1.0</xsl:param>

   <!--
     -  The namespace to assign to the VOResource root element in the output
     -  document.  This defaults to the value of $resourceNS (which 
     -  defaults to the IVOA RegistryInterface namespace).
     -->
   <xsl:param name="newResourceNS" select="$resourceNS"/>

   <!-- 
     -  The prefix to assign to the namespace for the output root element.
     -  The default is ri.
     -->
   <xsl:param name="newResourcePrefix">ri</xsl:param>

   <!--
     -  The name to give to the VOResource root element in the output
     -  document.  This defaults to the value of $resourceName (which 
     -  defaults to "Resource").
     -->
   <xsl:param name="newResourceElement" 
              select="concat($newResourcePrefix,':',$resourceElement)"/>

   <!--
     -  If true, insert carriage returns and indentation to produce a neatly 
     -  formatted output.  If false, any spacing between tags in the source
     -  document will be preserved.  
     -->
   <xsl:param name="prettyPrint" select="true()"/>

   <!--
     -  the per-level indentation.  Set this to a sequence of spaces when
     -  pretty printing is turned on.
     -->
   <xsl:param name="indent">
      <xsl:text>  </xsl:text>
      <!--
      <xsl:for-each select="/*/*[2]">
         <xsl:call-template name="getindent"/>
      </xsl:for-each>
        -->
   </xsl:param>

   <xsl:param name="stc">http://www.ivoa.net/xml/STC/stc-v1.30.xsd</xsl:param>

   <xsl:variable name="cr"><xsl:text>
</xsl:text></xsl:variable>

   <xsl:variable name="istep">
      <xsl:if test="$prettyPrint">
         <xsl:value-of select="$indent"/>
      </xsl:if>
   </xsl:variable>
   <xsl:variable name="isp">
      <xsl:value-of select="$cr"/>
   </xsl:variable>

   <!-- ==========================================================
     -  General templates
     -  ========================================================== -->

   <xsl:template match="/">
      <xsl:apply-templates select="*">
         <xsl:with-param name="sp">
            <xsl:if test="$prettyPrint">
              <xsl:value-of select="$cr"/>
            </xsl:if>
         </xsl:with-param>
         <xsl:with-param name="step">
            <xsl:if test="$prettyPrint">
              <xsl:value-of select="$indent"/>
            </xsl:if>
         </xsl:with-param>
      </xsl:apply-templates>
   </xsl:template>

   <xsl:template match="*">
      <xsl:param name="sp"/>
      <xsl:param name="step"/>
      <xsl:param name="pfx" select="'#'"/>

      <xsl:choose>
         <xsl:when test="descendant::text()[normalize-space(.)!=''] or 
                         @*[normalize-space(.)!=''] or 
                         descendant::*[namespace-uri()=$stc] or 
                         namespace-uri()=$stc">
            <!-- we have some values enclosed; pass on this element -->
            <xsl:apply-templates select="." mode="pass">
               <xsl:with-param name="sp" select="$sp"/>
               <xsl:with-param name="step" select="$step"/>
               <xsl:with-param name="pfx" select="$pfx"/>
            </xsl:apply-templates>
         </xsl:when>

         <!-- if there are no values, do not emit -->
         <xsl:otherwise></xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  remove various empty attributes
     -->
   <xsl:template match="@ivo-id[normalize-space(.)='' or normalize-space(.)='null']"/>
   <xsl:template match="@standardID[normalize-space(.)='' or normalize-space(.)='null']"/>
   <xsl:template match="interface/@role[normalize-space(.)='' or normalize-space(.)='null']"/>
   <xsl:template match="interface/@version[normalize-space(.)='' or normalize-space(.)='null']"/>

   <!--
     -  set empty accessURL/@use values
     -->
   <xsl:template 
        match="interface[contains(@xsi:type,':WebBrowser')]/accessURL/@use[normalize-space(.)='']">
      <xsl:attribute name="use">full</xsl:attribute>
   </xsl:template>

   <!--
     -  get case right on value for waveband
     -->
   <xsl:template match="waveband[.!='UV' and .!='EUV' and not(matches(normalize-space(.),'^[A-Z][a-z]+'))]/text()">
      <xsl:value-of select="upper-case(substring(.,1,1))"/>
      <xsl:value-of select="lower-case(substring(.,2))"/>
   </xsl:template>

   <!--
     -  Visible -> Optical
     -->
   <xsl:template match="waveband[.='Visible']/text()">
      <xsl:text>Optical</xsl:text>
   </xsl:template>

   <!--
     -  Ultraviolet -> UV
     -->
   <xsl:template match="waveband[.='Ultraviolet']/text()">
      <xsl:text>UV</xsl:text>
   </xsl:template>

   <!--
     -  @xsi:type="vg:DataService" -> @xsi:type="vs:DataService"
     -->
   <xsl:template match="@xsi:type[normalize-space(.)='vg:DataService']">
      <xsl:attribute name="xsi:type">
         <xsl:text>vs:DataService</xsl:text>
      </xsl:attribute>
   </xsl:template>

   <!--
     -  provide prefix for Position1D
     -->
   <xsl:template match="Position1D">
      <stc:Position1D>
         <xsl:apply-templates select="*|text()" /> 
      </stc:Position1D>
   </xsl:template>
   <xsl:template match="Position1D/Size">
      <stc:Size>
         <xsl:apply-templates select="@*" />
         <xsl:apply-templates select="*|text()" /> 
      </stc:Size>
   </xsl:template>

   <!--
     -  fill in a default value for complianceLevel
     -->
   <xsl:template match="complianceLevel[normalize-space(.)='']">
      <xsl:param name="sp"/>
      <xsl:param name="step"/>
      <xsl:param name="pfx" select="'#'"/>

      <xsl:value-of select="$sp"/>
      <complianceLevel>minimal</complianceLevel>
   </xsl:template>

   <!--
     -  get case right on value for imageServiceType
     -->
   <xsl:template match="imageServiceType[not(matches(normalize-space(.),'^[A-Z][a-z]+'))]/text()">
      <xsl:value-of select="upper-case(substring(.,1,1))"/>
      <xsl:value-of select="lower-case(substring(.,2))"/>
   </xsl:template>

   <!--
     -  correct the order of capability/validationLevel
     -->
   <xsl:template match="capability[validationLevel[preceding-sibling::*]]">
      <xsl:param name="sp"/>
      <xsl:param name="step"/>
      <xsl:param name="pfx" select="'#'"/>

      <xsl:variable name="newp">
         <xsl:call-template name="newprefixes">
            <xsl:with-param name="pfx" select="$pfx"/>
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="fp" select="concat($pfx,substring($newp,1))"/>

      <xsl:value-of select="$sp"/>

      <xsl:copy>
         <xsl:apply-templates select="@*" />

         <xsl:apply-templates select="validationLevel">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="child::node()[local-name()!='validationLevel']">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:if test="$prettyPrint and contains(text()[1],$cr)">
           <xsl:value-of select="$sp"/>
         </xsl:if>
      </xsl:copy>
   </xsl:template>

   <!--
     -  correct the order of coverage/footprint
     -->
   <xsl:template match="coverage[waveband[following-sibling::footprint[normalize-space(.)!=''] and normalize-space(.)!='']]">
      <xsl:param name="sp"/>
      <xsl:param name="step"/>
      <xsl:param name="pfx" select="'#'"/>

      <xsl:variable name="newp">
         <xsl:call-template name="newprefixes">
            <xsl:with-param name="pfx" select="$pfx"/>
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="fp" select="concat($pfx,substring($newp,1))"/>

      <xsl:value-of select="$sp"/>

      <xsl:copy>
         <xsl:apply-templates select="@*" />

         <xsl:apply-templates select="footprint|footprint/preceding-sibling::*[local-name()!='waveband']">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="waveband">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="footprint/following-sibling::*[local-name()!='waveband' and local-name()!='footprint']">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:if test="$prettyPrint and contains(text()[1],$cr)">
           <xsl:value-of select="$sp"/>
         </xsl:if>
      </xsl:copy>

   </xsl:template>

   <!--
     -  correct the order of creator
     -  replaced with general curation handler
     -
   <xsl:template match="curation[creator[preceding-sibling::*[local-name()!='publisher']]]">
      <xsl:param name="sp"/>
      <xsl:param name="step"/>
      <xsl:param name="pfx" select="'#'"/>

      <xsl:variable name="newp">
         <xsl:call-template name="newprefixes">
            <xsl:with-param name="pfx" select="$pfx"/>
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="fp" select="concat($pfx,substring($newp,1))"/>

      <xsl:value-of select="$sp"/>

      <xsl:copy>
         <xsl:apply-templates select="@*" />

         <xsl:apply-templates select="publisher">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="creator">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="*[local-name()!='publisher' and local-name()!='creator']">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:if test="$prettyPrint and contains(text()[1],$cr)">
           <xsl:value-of select="$sp"/>
         </xsl:if>
      </xsl:copy>

   </xsl:template>
     -->


   <!--
     -  get the curation ordering right
     -->
   <xsl:template match="curation">
      <xsl:param name="sp"/>
      <xsl:param name="step"/>
      <xsl:param name="pfx" select="'#'"/>

      <xsl:variable name="newp">
         <xsl:call-template name="newprefixes">
            <xsl:with-param name="pfx" select="$pfx"/>
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="fp" select="concat($pfx,substring($newp,1))"/>

      <xsl:value-of select="$sp"/>
      <xsl:copy>

         <xsl:apply-templates select="publisher">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="creator">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="contributor">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="date">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="version">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="contact">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

      </xsl:copy>
      
   </xsl:template>

   <!--
     -  get the content ordering right
     -->
   <xsl:template match="content">
      <xsl:param name="sp"/>
      <xsl:param name="step"/>
      <xsl:param name="pfx" select="'#'"/>

      <xsl:variable name="newp">
         <xsl:call-template name="newprefixes">
            <xsl:with-param name="pfx" select="$pfx"/>
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="fp" select="concat($pfx,substring($newp,1))"/>

      <xsl:value-of select="$sp"/>
      <xsl:copy>

         <xsl:apply-templates select="subject">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="description">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="source">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="referenceURL">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="type">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="contentLevel">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="relationship">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

      </xsl:copy>
      
   </xsl:template>

   <!--
     -  correct the order of coverage
     -->
   <xsl:template match="/*[capability[preceding-sibling::coverage[descendant::text()[normalize-space(.)!=''] or 
                         descendant::*/@* or 
                         descendant::*[namespace-uri()=$stc]]] or 
                           rights[preceding-sibling::capability]]">
      <xsl:param name="sp"/>
      <xsl:param name="step"/>
      <xsl:param name="pfx" select="'#'"/>

      <xsl:variable name="newp">
         <xsl:call-template name="newprefixes">
            <xsl:with-param name="pfx" select="$pfx"/>
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="fp" select="concat($pfx,substring($newp,1))"/>

      <xsl:value-of select="$sp"/>

      <xsl:copy>
         <xsl:apply-templates select="@*" />

         <xsl:apply-templates select="content|content/preceding-sibling::*|
                                      content/preceding-sibling::text()">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="rights">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="capability">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="coverage">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:apply-templates select="capability/following-sibling::*[local-name()!='coverage' and local-name()!='capability' and local-name()!='rights']">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:if test="$prettyPrint and contains(text()[1],$cr)">
           <xsl:value-of select="$sp"/>
         </xsl:if>
      </xsl:copy>

   </xsl:template>

   <!--
     -  Copy elements unchanged by default (apart from spacing)
     -->
   <xsl:template match="*" priority="-1" mode="pass">
      <xsl:param name="sp"/>
      <xsl:param name="step"/>
      <xsl:param name="pfx" select="'#'"/>

      <xsl:variable name="newp">
         <xsl:call-template name="newprefixes">
            <xsl:with-param name="pfx" select="$pfx"/>
         </xsl:call-template>
      </xsl:variable>
      <xsl:variable name="fp" select="concat($pfx,substring($newp,1))"/>

      <xsl:value-of select="$sp"/>

      <xsl:copy>
         <xsl:apply-templates select="@*" />

         <xsl:apply-templates select="child::node()">
            <xsl:with-param name="sp" select="concat($sp,$step)"/>
            <xsl:with-param name="step" select="$step"/>
            <xsl:with-param name="pfx" select="$fp"/>
         </xsl:apply-templates>

         <xsl:if test="$prettyPrint and contains(text()[1],$cr)">
           <xsl:value-of select="$sp"/>
         </xsl:if>
      </xsl:copy>

   </xsl:template>

   <xsl:template match="@*" priority="-1">
      <xsl:copy/>
   </xsl:template>

   <!--
     -  template for handling ignorable whitespace.  This version will
     -    shave off all but the last carriage return
     -->
   <xsl:template match="text()" priority="-1" mode="trim">
      <xsl:if test="not($prettyPrint)">
         <xsl:choose>
            <xsl:when test="contains(.,$cr)">
               <xsl:value-of select="$cr"/>
               <xsl:call-template name="afterLastCR">
                  <xsl:with-param name="text" select="."/>
               </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
               <xsl:copy/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>
   </xsl:template>

   <xsl:template match="*" mode="fudgeForDepth">
      <xsl:if test="parent::element()">
         <xsl:value-of select="$autoIndent"/>
         <xsl:apply-templates select=".." mode="fudgeForDepth"/>
      </xsl:if>
   </xsl:template>

   <!--
     -  template for handling ignorable whitespace
     -->
   <xsl:template match="text()" priority="-1">
      <xsl:param name="sp"/>
      <xsl:param name="step"/>

      <xsl:variable name="fdge">
         <xsl:apply-templates select=".." mode="fudgeForDepth"/>
      </xsl:variable>
      <xsl:variable name="trimmed" select="normalize-space(.)"/>
      <xsl:variable name="subsp" select="concat($sp,$fdge,$step)"/>
      <xsl:variable name="lastsp" 
        select="substring($subsp,1,string-length($subsp)-string-length($step)-1)"/>

      <xsl:choose>
         <xsl:when test="not($prettyPrint) or 
                         (string-length($trimmed) &gt; 0 and 
                          not(contains(.,$cr)))">
            <xsl:copy/>
         </xsl:when>
         <xsl:when test="string-length($trimmed) &gt; 0">
            <xsl:for-each select="tokenize(.,$cr)">
               <xsl:variable name="trmd" select="normalize-space(.)"/>
               <xsl:choose>
                 <xsl:when test="string-length($trmd)=0 and 
                                 (position()=last() or position()=1)">
                 </xsl:when>
                 <xsl:when test="string-length($trmd)=0">
                   <xsl:value-of select="$cr"/>
                 </xsl:when>
                 <xsl:otherwise>
                   <xsl:value-of select="$subsp"/>
                   <xsl:value-of select="$trmd"/>
                 </xsl:otherwise>
               </xsl:choose>
            </xsl:for-each>
            <xsl:value-of select="$lastsp"/>
         </xsl:when>
      </xsl:choose>
   </xsl:template>

<!-- for debugging:  uncomment this template to override the default
  -  handling of ignorable whitespace
   <xsl:template match="text()" priority="-0.8">
      <xsl:variable name="trimmed" select="normalize-space(.)"/>
      <xsl:if test="not($prettyPrint) or string-length($trimmed) &gt; 0">
         <xsl:text>[</xsl:text>
         <xsl:copy/>
         <xsl:text>]</xsl:text>
      </xsl:if>
   </xsl:template>
  -->

   <!--
     -  template for handling ignorable whitespace.  This version will
     -    shave off all but the last carriage return
     -->
   <xsl:template match="text()" priority="-1" mode="trim">
      <xsl:if test="not($prettyPrint)">
         <xsl:choose>
            <xsl:when test="contains(.,$cr)">
               <xsl:value-of select="$cr"/>
               <xsl:call-template name="afterLastCR">
                  <xsl:with-param name="text" select="."/>
               </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
               <xsl:copy/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>
   </xsl:template>

   <!-- ==========================================================
     -  Utility templates
     -  ========================================================== -->

   <xsl:template name="newprefixes">
      <xsl:param name="pfx" select="'#'"/>
      <xsl:param name="inscope">
         <xsl:value-of select="in-scope-prefixes(.)"/>
      </xsl:param>
      <xsl:param name="npfx" select="'#'"/>

      <xsl:variable name="firstpref">
         <xsl:choose>
            <xsl:when test="contains($inscope,' ')">
              <xsl:value-of select="substring-before($inscope,' ')"/>
            </xsl:when>
            <xsl:when test="string-length($inscope) &gt; 0">
              <xsl:value-of select="$inscope"/>
            </xsl:when>
         </xsl:choose>
      </xsl:variable>
      <xsl:variable name="restpref">
         <xsl:if test="contains($inscope,' ')">
           <xsl:value-of select="substring-after($inscope,' ')"/>
         </xsl:if>
      </xsl:variable>

      <xsl:choose>
         <xsl:when test="string-length($firstpref)=0">
            <xsl:value-of select="$npfx"/>
         </xsl:when>
         <xsl:when test="not(contains($pfx,concat('#',$firstpref,'#'))) and
                         not(contains($npfx,concat('#',$firstpref,'#')))">
            <xsl:call-template name="newprefixes">
               <xsl:with-param name="pfx" select="$pfx"/>
               <xsl:with-param name="npfx" 
                               select="concat($npfx,$firstpref,'#')"/>
               <xsl:with-param name="inscope" select="$restpref"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:call-template name="newprefixes">
               <xsl:with-param name="pfx" select="$pfx"/>
               <xsl:with-param name="npfx" select="$npfx"/>
               <xsl:with-param name="inscope" select="$restpref"/>
            </xsl:call-template>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  convert the first character to a lower case
     -  @param in  the string to convert
     -->
   <xsl:template name="uncapitalize">
      <xsl:param name="in"/>
      <xsl:value-of select="translate(substring($in,1,1),
                                      'ABCDEFGHIJKLMNOPQRSTUVWXYZ',
                                      'abcdefghijklmnopqrstuvwxyz')"/>
      <xsl:value-of select="substring($in,2)"/>
   </xsl:template>

   <!--
     -  determine the indentation preceding the context element
     -->
   <xsl:template name="getindent">
      <xsl:variable name="prevsp">
         <xsl:for-each select="preceding-sibling::text()">
            <xsl:if test="position()=last()">
               <xsl:value-of select="."/>
            </xsl:if>
         </xsl:for-each>
      </xsl:variable>

      <xsl:choose>
         <xsl:when test="contains($prevsp,$cr)">
            <xsl:call-template name="afterLastCR">
               <xsl:with-param name="text" select="$prevsp"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:when test="$prevsp">
            <xsl:value-of select="$prevsp"/>
         </xsl:when>
         <xsl:otherwise><xsl:text>    </xsl:text></xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  return the text that appears after the last carriage return
     -  in the input text
     -  @param text  the input text to process
     -->
   <xsl:template name="afterLastCR">
      <xsl:param name="text"/>
      <xsl:choose>
         <xsl:when test="contains($text,$cr)">
            <xsl:call-template name="afterLastCR">
               <xsl:with-param name="text" select="substring-after($text,$cr)"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="$text"/></xsl:otherwise>
      </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
