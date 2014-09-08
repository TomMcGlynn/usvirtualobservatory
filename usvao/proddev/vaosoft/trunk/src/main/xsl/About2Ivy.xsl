<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!--
  -  This stylesheet is used to convert an XML-ized about.properties file
  -  into an ivy.xml file.  
  -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                version="1.0">

   <xsl:output method="xml" encoding="UTF-8" indent="no"
               omit-xml-declaration="no" />

   <!--
     -  the product version
     -->
   <xsl:param name="version"/>

   <!--
     -  the product organization (should default to usvao)
     -->
   <xsl:param name="org">usvao</xsl:param>

   <!--
     -  the product status
     -->
   <xsl:param name="status">release</xsl:param>

   <!--
     -  the default configuration
     -->
   <xsl:param name="defaultConf">null</xsl:param>

   <xsl:template match="/">
     <xsl:apply-templates select="*" />
   </xsl:template>

   <xsl:template match="about">

     <xsl:variable name="vers">
       <xsl:choose>
         <xsl:when test="$version!=''">
            <xsl:value-of select="$version"/>
         </xsl:when>
         <xsl:when test="version">
            <xsl:value-of select="version"/>
         </xsl:when>
         <xsl:otherwise>unset</xsl:otherwise>
       </xsl:choose>
     </xsl:variable>

     <xsl:text>
</xsl:text>
     <ivy-module version="2.0">
       <xsl:text>
  </xsl:text>
       <info organisation="{$org}" module="{name}" status="{$status}" 
             revision="{$vers}"><xsl:text>
    </xsl:text>
         <description><xsl:text>
      </xsl:text>
           <xsl:value-of select="description"/><xsl:text>
    </xsl:text>
         </description><xsl:text>
  </xsl:text>
       </info>
       <xsl:text>

  </xsl:text>

       <configurations default="{$defaultConf}"><xsl:text>

    </xsl:text>
          <xsl:comment> standard configurations </xsl:comment><xsl:text>
    </xsl:text>
          <xsl:apply-templates select="dep/for" mode="stdConfs"/>

          <xsl:text>
    </xsl:text>
          <xsl:comment> dependency configurations specific to this product </xsl:comment><xsl:text>
    </xsl:text>
          <xsl:apply-templates select="dep/for" mode="specConfs"/>

          <xsl:text>
    </xsl:text>
          <xsl:comment> artifact configurations </xsl:comment><xsl:text>
    </xsl:text>
          <xsl:apply-templates select="art/for" mode="confs"/>


          <xsl:text>
  </xsl:text>
       </configurations>
       <xsl:text>

  </xsl:text>
       <publications><xsl:text>

    </xsl:text>
          <xsl:apply-templates select="art/for" mode="artifacts"/><xsl:text>
  </xsl:text>
       </publications>

       <xsl:text>

  </xsl:text>

       <dependencies><xsl:text>

    </xsl:text>

         <xsl:apply-templates select="dep/for" mode="dependencies"/>

         <xsl:text>
  </xsl:text>
       </dependencies><xsl:text>

</xsl:text>
     </ivy-module><xsl:text>
</xsl:text>
   </xsl:template>

   <xsl:key name="specificConfs" match="/about/dep/for/*/*" 
            use="concat(local-name(),'-',.)" />
   <xsl:key name="standardConfs" match="/about/dep/for/*" 
            use="local-name()" />
   <xsl:key name="artifactConfs" match="/about/art/for/*" 
            use="local-name()" />

   <!--
     -  create the list of standard <conf> nodes needed by the product
     -->
   <xsl:template match="dep/for" mode="stdConfs">
     <xsl:call-template name="stdConf">
       <xsl:with-param name="confname">null</xsl:with-param>
     </xsl:call-template>
     <xsl:call-template name="stdConf">
       <xsl:with-param name="confname">uberinstall</xsl:with-param>
     </xsl:call-template>

     <xsl:for-each select="*[generate-id()=generate-id(key('standardConfs',local-name())[1])]">
       <xsl:sort select="local-name()"/>
       <xsl:variable name="stdconf" select="local-name()"/>
       <xsl:variable name="extends">
         <xsl:call-template name="confExtends"/>
       </xsl:variable>

       <xsl:call-template name="stdConf">
         <xsl:with-param name="confname" select="$stdconf"/>
         <xsl:with-param name="extends" select="$extends"/>
       </xsl:call-template>
     </xsl:for-each>
   </xsl:template>

   <!--
     -  Create a <conf> element for a standard configuration.  If a description
     -  has been defined for it, it will be included.
     -  @param confname   the name of the configuration
     -  @param extends    the configurations it extends; if empty it will not
     -                      be included.
     -->
   <xsl:template name="stdConf">
     <xsl:param name="confname"/>
     <xsl:param name="extends"/>

     <xsl:element name="conf">
        <xsl:attribute name="name">
          <xsl:value-of select="$confname"/>
        </xsl:attribute>
        <xsl:if test="/about/conf/description/*[local-name()=$confname]">
          <xsl:attribute name="description">
            <xsl:value-of select="/about/conf/description/*[local-name()=$confname]"/>
          </xsl:attribute>
        </xsl:if>
        <xsl:if test="string-length($extends) &gt; 0">
          <xsl:attribute name="extends">
            <xsl:value-of select="$extends"/>
          </xsl:attribute>
        </xsl:if>
     </xsl:element><xsl:text>
    </xsl:text>
   </xsl:template>

   <!--
     -  create the value for the extends attribute that defines the 
     -  product-specific meaning of the standard configurations.  
     --> 
   <xsl:template name="confExtends">
     <xsl:param name="appendto"/>
     <xsl:param name="i" select="1"/>

     <xsl:choose>
       <xsl:when test="$i &lt;= count(*)">
         <xsl:variable name="addon">
           <xsl:choose>
             <xsl:when test="local-name(*[$i])='_extends'">
               <xsl:value-of select="*[$i]"/>
             </xsl:when>
             <xsl:when test="not(starts-with(local-name(*[$i]),'_'))">
               <xsl:value-of select="local-name(*[$i])"/>
               <xsl:text>-</xsl:text>
               <xsl:value-of select="*[$i]"/>
             </xsl:when>
           </xsl:choose>
         </xsl:variable>

         <xsl:variable name="out">
           <xsl:choose>
             <xsl:when test="string-length($addon) > 0">
               <xsl:if test="string-length($appendto) &gt; 0">
                 <xsl:value-of select="$appendto"/>
                 <xsl:text>,</xsl:text>
               </xsl:if>
               <xsl:value-of select="$addon"/>
             </xsl:when>
             <xsl:otherwise>
               <xsl:value-of select="$appendto"/>
             </xsl:otherwise>
           </xsl:choose>
         </xsl:variable>

         <xsl:call-template name="confExtends">
           <xsl:with-param name="appendto" select="$out"/>
           <xsl:with-param name="i" select="$i + 1"/>
         </xsl:call-template>
       </xsl:when>
       <xsl:otherwise>
         <xsl:value-of select="$appendto"/>
       </xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <!--
     -  create the list of product-specifc <conf> nodes
     -->
   <xsl:template match="dep/for" mode="specConfs">
     <xsl:for-each select="*/*[not(starts-with(local-name(),'_')) and 
                               generate-id()=generate-id(key('specificConfs',concat(local-name(),'-',.))[1])]">
       <xsl:sort select="concat(local-name(),'-',.)"/>
       <xsl:variable name="dep" select="local-name()"/>
       <xsl:call-template name="specificConf">
         <xsl:with-param name="depprod" select="$dep"/>
         <xsl:with-param name="artifactType" select="."/>
       </xsl:call-template>
     </xsl:for-each>
   </xsl:template>

   <!--
     -  create a product-specific  <conf> node 
     -  @param depprod       the dependent product name
     -  @param artifactType  the needed artifactType for the product
     -->
   <xsl:template name="specificConf">
     <xsl:param name="depprod"/>
     <xsl:param name="artifactType"/>

     <xsl:variable name="name">
       <xsl:call-template name="depDashArt">
         <xsl:with-param name="depprod" select="$depprod"/>
         <xsl:with-param name="artifactType" select="$artifactType"/>
       </xsl:call-template>
     </xsl:variable>

     <conf name="{$name}" /><xsl:text>
    </xsl:text>
   </xsl:template>

   <!--
     -  create a common-delimited combinations of dependent product name
     -  and artifact type, where each combination is delimited by a dash.
     -  @param depprod       the dependent product name
     -  @param artifactType  a comma-separated list of artifact types to 
     -                         pair with the product name
     -->
   <xsl:template name="depDashArt">
     <xsl:param name="depprod"/>
     <xsl:param name="artifactType"/>
     <xsl:param name="appendto"/>

     <xsl:variable name="combo">
       <xsl:value-of select="$depprod"/>
       <xsl:text>-</xsl:text>
       <xsl:choose>
         <xsl:when test="contains($artifactType,',')">
           <xsl:value-of 
                select="normalize-space(substring-before($artifactType,','))"/>
         </xsl:when>
         <xsl:otherwise>
           <xsl:value-of select="$artifactType"/>
         </xsl:otherwise>
       </xsl:choose>
     </xsl:variable>

     <xsl:variable name="pre">
       <xsl:if test="string-length($appendto) > 0">
         <xsl:value-of select="$appendto"/>                 
         <xsl:text>,</xsl:text>
       </xsl:if>
     </xsl:variable>

     <xsl:choose>

       <xsl:when test="contains($artifactType,',')">
         <xsl:call-template name="depDashArt">
           <xsl:with-param name="depprod" select="$depprod"/>
           <xsl:with-param name="artifactType" 
                select="normalize-space(substring-after($artifactType,','))"/>
           <xsl:with-param name="appendto">
             <xsl:value-of select="$pre"/>
             <xsl:value-of select="$combo"/>
           </xsl:with-param>
         </xsl:call-template>      
       </xsl:when>

       <xsl:otherwise>
         <xsl:value-of select="$pre"/>
         <xsl:value-of select="$combo"/>
       </xsl:otherwise>

     </xsl:choose>
   </xsl:template>

   <xsl:template match="dep/for" mode="dependencies">

     <xsl:for-each select="*/*[not(starts-with(local-name(),'_')) and 
                               generate-id()=generate-id(key('specificConfs',concat(local-name(),'-',.))[1])]">
       <xsl:sort select="concat(local-name(),'-',.)"/>

       <xsl:variable name="prodname" select="local-name()"/>

       <xsl:variable name="specart">
         <xsl:call-template name="depDashArt">
           <xsl:with-param name="depprod" select="$prodname"/>
           <xsl:with-param name="artifactType" select="."/>
         </xsl:call-template>
       </xsl:variable>

       <xsl:variable name="depdata" 
                     select="/about/dep/product/*[local-name()=$prodname]"/>

       <xsl:variable name="deporg">
         <xsl:call-template name="listElement">
           <xsl:with-param name="list" select="$depdata"/>
           <xsl:with-param name="i" select="1"/>
         </xsl:call-template>
       </xsl:variable>
       <xsl:variable name="depname">
         <xsl:call-template name="listElement">
           <xsl:with-param name="list" select="$depdata"/>
           <xsl:with-param name="i" select="2"/>
         </xsl:call-template>
       </xsl:variable>
       <xsl:variable name="depver">
         <xsl:call-template name="listElement">
           <xsl:with-param name="list" select="$depdata"/>
           <xsl:with-param name="i" select="3"/>
         </xsl:call-template>
       </xsl:variable>

       <dependency conf="{$specart}->{.}" name="{$depname}" 
                   org="{$deporg}" rev="{$depver}" /><xsl:text>
    </xsl:text>

     </xsl:for-each>

   </xsl:template>

   <!-- 
     -  figure out the configuration to recursively follow for a dependency
     -  products
     -->
   <xsl:template name="recurseDep">
     <xsl:param name="depprod"/>
     <xsl:param name="artifactType"/>

     <xsl:choose>
       <xsl:when test="/about/dep/mappingRule/*[local-name()=$depprod]/*[local-name()=$artifactType]">
         <xsl:value-of select="/about/dep/mappingRule/*[local-name()=$depprod]/*[local-name()=$artifactType]"/>
       </xsl:when>
       <xsl:when 
            test="/about/dep/defaultMappingRule/*[local-name()=$artifactType]">
         <xsl:value-of select="/about/dep/defaultMappingRule/*[local-name()=$artifactType]"/>
       </xsl:when>
       <xsl:otherwise><xsl:value-of select="$artifactType"/></xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <!--
     - select the i-th element in a list
     - @param list    the list to parse the element from
     - @param i       the 1-based index of the desired element
     - @param delim   the character that delimits the list elements
     -->
   <xsl:template name="listElement">
     <xsl:param name="list"/>
     <xsl:param name="i"/>
     <xsl:param name="delim" select="' '"/>

     <xsl:variable name="array" select="normalize-space($list)"/>

     <xsl:variable name="first">
       <xsl:choose>
         <xsl:when test="contains($array,$delim)">
           <xsl:value-of select="substring-before($array,$delim)"/>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="$array"/></xsl:otherwise>
       </xsl:choose>
     </xsl:variable>
     <xsl:variable name="rest">
       <xsl:if test="contains($array,$delim)">
         <xsl:value-of select="substring-after($array,$delim)"/>
       </xsl:if>
     </xsl:variable>

     <xsl:choose>
       <xsl:when test="$i=1">
         <xsl:value-of select="$first"/>
       </xsl:when>
       <xsl:when test="string-length($rest) &gt; 0">
         <xsl:call-template name="listElement">
           <xsl:with-param name="list" select="$rest"/>
           <xsl:with-param name="i" select="$i - 1"/>
           <xsl:with-param name="delim" select="$delim"/>
         </xsl:call-template>
       </xsl:when>
     </xsl:choose>
   </xsl:template>

   <xsl:template match="art/for" mode="confs">

     <xsl:for-each select="*[generate-id()=generate-id(key('artifactConfs',local-name())[1])]">
        <xsl:variable name="confname" select="local-name()"/>

        <xsl:if test="not(/about/dep/for/*[local-name()=$confname])">
          <xsl:call-template name="stdConf">
            <xsl:with-param name="confname" select="$confname"/>
          </xsl:call-template>
        </xsl:if>
     </xsl:for-each>
   </xsl:template>

   <xsl:template match="art/for" mode="artifacts">
     <xsl:variable name="defName" select="/about/art/defaultName"/>

     <xsl:for-each select="*">
       <xsl:variable name="specArtName">
         <xsl:call-template name="listElement">
           <xsl:with-param name="list" select="."/>
           <xsl:with-param name="i" select="3"/>
         </xsl:call-template>
       </xsl:variable>

       <xsl:variable name="artType">
         <xsl:call-template name="listElement">
           <xsl:with-param name="list" select="."/>
           <xsl:with-param name="i" select="1"/>
         </xsl:call-template>
       </xsl:variable>

       <xsl:variable name="artExt">
         <xsl:call-template name="listElement">
           <xsl:with-param name="list" select="."/>
           <xsl:with-param name="i" select="2"/>
         </xsl:call-template>
       </xsl:variable>

       <xsl:variable name="artName">
         <xsl:choose>
           <xsl:when test="string-length($specArtName)=0">
             <xsl:value-of select="$defName"/>
           </xsl:when>
           <xsl:otherwise><xsl:value-of select="$specArtName"/></xsl:otherwise>
         </xsl:choose>
       </xsl:variable>

       <artifact name="{$artName}" type="{$artType}" ext="{$artExt}" conf="{local-name()}"/><xsl:text>
    </xsl:text>

     </xsl:for-each>
   </xsl:template>

</xsl:stylesheet>
