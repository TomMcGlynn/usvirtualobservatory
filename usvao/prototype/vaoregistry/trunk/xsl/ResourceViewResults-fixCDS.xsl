<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns:vs="http://www.ivoa.net/xml/VODataService/v1.0" 
                xmlns:stc="http://www.ivoa.net/xml/STC/stc-v1.30.xsd" 
                xmlns:vr="http://www.ivoa.net/xml/VOResource/v1.0" 
                xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0" 
                xmlns:vot="http://www.ivoa.net/xml/VOTable/v1.1" 
                xmlns="http://www.ivoa.net/xml/VOTable/v1.1" 
                version="1.0">

   <xsl:template match="/">
      <xsl:apply-templates select="ri:VOResources" />
   </xsl:template>

   <xsl:template match="ri:VOResources" xml:space="preserve">
<VOTABLE xmlns="http://www.ivoa.net/xml/VOTable/v1.1">
   <DESCRIPTION>Registry Search Results</DESCRIPTION>
   <RESOURCE name="Search Results">
      <TABLE name="results">
         <FIELD ID="tags" name="categories" datatype="char" arraysize="*"/>
         <FIELD ID="shortName" name="shortName" datatype="char" arraysize="*"/>
         <FIELD ID="title" name="title" datatype="char" arraysize="*"/>
         <FIELD ID="description" name="description" datatype="char" arraysize="*"/>
         <FIELD ID="publisher" name="publisher" datatype="char" arraysize="*"/>
         <FIELD ID="waveband" name="waveband" datatype="char" arraysize="*"/>
         <FIELD ID="identifier" name="identifier" datatype="char" arraysize="*" ucd="ID_MAIN"/>
         <FIELD ID="updated" name="descriptionUpdated" datatype="char" arraysize="*"/>
         <FIELD ID="subject" name="subject" datatype="char" arraysize="*"/>
         <FIELD ID="type" name="type" datatype="char" arraysize="*"/>
         <FIELD ID="contentLevel" name="contentLevel" datatype="char" arraysize="*"/>
         <FIELD ID="regionOfRegard" name="typicalRegionSize" datatype="int" unit="arcsec"/>
         <FIELD ID="version" name="version" datatype="char" arraysize="*"/>
         <FIELD ID="capabilityName" name="capabilityName" datatype="char" arraysize="*"/>
         <FIELD ID="capabilityClass" name="capabilityClass" datatype="char" arraysize="*"/>
         <FIELD ID="capabilityStandardID" name="capabilityStandardID" datatype="char" arraysize="*"/>
         <FIELD ID="capabilityValidationLevel" name="capabilityValidationLevel" datatype="char" arraysize="*"/>
         <FIELD ID="interfaceClass" name="interfaceClass" datatype="char" arraysize="*"/>
         <FIELD ID="interfaceVersion" name="interfaceVersion" datatype="char" arraysize="*"/>
         <FIELD ID="interfaceRole" name="interfaceRole" datatype="char" arraysize="*"/>
         <FIELD ID="accessURL" name="accessURL" datatype="char" arraysize="*"/>
         <FIELD ID="maxRadius" name="maxSearchRadius" datatype="float"/>
         <FIELD ID="maxRecords" name="maxRecords" datatype="int"/>
         <FIELD ID="publisherID" name="publisherIdentifier" datatype="char" arraysize="*"/>
         <FIELD ID="referenceURL" name="referenceURL" datatype="char" arraysize="*"/>
         <GROUP ID="interfaces" name="interfaces">
            <DESCRIPTION>
               These columns represent a sub-table of interfaces.  Each column
               has the same number of array values, one for each available 
               interface, and in the same order. The values are delimited by 
               # characters (and extra # appear at the beginning and end of the
               array).
            </DESCRIPTION>
            <FIELDref ref="capabilityName" />
            <FIELDref ref="capabilityClass" />
            <FIELDref ref="capabilityStandardID" />
            <FIELDref ref="capabilityValidationLevel" />
            <FIELDref ref="interfaceClass" />
            <FIELDref ref="interfaceVersion" />
            <FIELDref ref="interfaceRole" />
            <FIELDref ref="accessURL" />
            <FIELDref ref="maxRadius" />
            <FIELDref ref="maxRecords" />
         </GROUP>
         <DATA>
            <TABLEDATA>
               <xsl:apply-templates select="ri:Resource" />
            </TABLEDATA>
         </DATA>
      </TABLE>
   </RESOURCE>

</VOTABLE>
   </xsl:template>

   <xsl:template match="ri:Resource">
      <TR><xsl:text>
</xsl:text>
         <xsl:text>   </xsl:text>
         <TD><xsl:apply-templates select="." mode="gettag"/></TD><xsl:text>
</xsl:text>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="shortName" />
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="title" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/description" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="curation/publisher" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="coverage/waveband" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="identifier" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="@updated" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/subject" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/type" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/contentLevel" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="coverage/stc:STCResourceProfile/stc:AstroCoords/stc:Position1D/stc:Size" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="curation/version" />   
         </xsl:call-template>

         <!-- capabilityName -->
         <xsl:variable name="caps" select="capability"/>
         <xsl:call-template name="capabilityName">
            <xsl:with-param name="caps" select="$caps"/>
         </xsl:call-template>

         <!-- capabilityClass -->
         <xsl:call-template name="capabilityArrayData">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="att">xsi:type</xsl:with-param>
            <xsl:with-param name="forstd" select="true()"/>
         </xsl:call-template>

         <!-- capabilityStandardID -->
         <xsl:call-template name="capabilityArrayData">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="att">standardID</xsl:with-param>
            <xsl:with-param name="forstd" select="true()"/>
         </xsl:call-template>

         <xsl:call-template name="capabilityArrayData">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="el">validationLevel</xsl:with-param>
            <xsl:with-param name="forstd" select="true()"/>
         </xsl:call-template>

         <xsl:call-template name="interfaceArrayData">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="att" select="'xsi:type'"/>
         </xsl:call-template>
         <xsl:call-template name="interfaceArrayData">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="att" select="'version'"/>
         </xsl:call-template>
         <xsl:call-template name="interfaceArrayData">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="att" select="'role'"/>
         </xsl:call-template>
         <xsl:call-template name="interfaceArrayData">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="el" select="'accessURL'"/>
         </xsl:call-template>

         <xsl:call-template name="capabilityArrayData">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="el">maxSearchRadius</xsl:with-param>
         </xsl:call-template>

         <xsl:call-template name="capabilityArrayData">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="el">maxRecords</xsl:with-param>
         </xsl:call-template>

         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="curation/publisher/@ivo-id" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/referenceURL" />   
         </xsl:call-template>
      </TR><xsl:text>
</xsl:text>
   </xsl:template>

  <xsl:template name="valOrNull">
      <xsl:param name="val"/>
      <xsl:param name="asarray" select="false()"/>
      <xsl:param name="removeScope" select="false()"/>
      <xsl:variable name="count" select="count($val)"/>

      <xsl:text>   </xsl:text>
      <TD>
         <xsl:choose>
            <xsl:when test="$asarray">
               <xsl:if test="count($val)>0">
                  <xsl:text>#</xsl:text>
               </xsl:if>
               <xsl:for-each select="$val">
                  <xsl:value-of select="normalize-space(.)"/>
                  <xsl:text>#</xsl:text>
               </xsl:for-each>
            </xsl:when>
           <xsl:otherwise>
             <xsl:variable name="withscope">
               <xsl:value-of select="normalize-space($val)"/>
             </xsl:variable>
             <xsl:choose>
               <xsl:when test="$removeScope and contains($withscope, ':')">
                 <xsl:value-of select="substring-after($withscope, ':')"/>
               </xsl:when>
               <xsl:otherwise>
                 <xsl:value-of select="$withscope"/>
               </xsl:otherwise>
             </xsl:choose>
           </xsl:otherwise>
         </xsl:choose>
      </TD><xsl:text>
</xsl:text>
   </xsl:template>

   <xsl:template match="*[identifier]" mode="gettag">
      <xsl:variable name="rxsitype" select="substring-after(@xsi:type,':')"/>
                    
      <xsl:text>#</xsl:text>
      <xsl:choose>
         <xsl:when test="$rxsitype='Registry'">
            <xsl:if test="capability[substring-after(@xsi:type,':')='Search']">
               <xsl:text>Searchable</xsl:text>
            </xsl:if>
            <xsl:if test="capability[substring-after(@xsi:type,':')='Search'] and capability[substring-after(@xsi:type,':')='Harvest']">
               <xsl:text> </xsl:text>
            </xsl:if>
            <xsl:if test="capability[substring-after(@xsi:type,':')='Harvest']">
               <xsl:text>Publishing</xsl:text>
            </xsl:if>
            <xsl:text> Registry#</xsl:text>
         </xsl:when>
         <xsl:when test="capability">
           <xsl:for-each select="capability">
            <xsl:variable name="cxsitype" select="substring-after(@xsi:type,':')"/>
            <xsl:choose>
               <xsl:when test="$cxsitype='ConeSearch' or $cxsitype='OpenSkyNode'">
                  <xsl:text>Catalog</xsl:text>
               </xsl:when>
               <xsl:when test="$cxsitype='SimpleImageAccess'">
                   <xsl:text>Images</xsl:text>
               </xsl:when>
               <xsl:when test="$cxsitype='SimpleSpectralAccess'">
                   <xsl:text>Spectra</xsl:text>
               </xsl:when>
               <xsl:when test="$cxsitype='Search' or $cxsitype='Harvest'">
                   <xsl:text>Registry</xsl:text>
               </xsl:when>
              <xsl:when test="interface">
                <xsl:for-each select="interface">
                  <xsl:variable name="ixsitype" select="substring-after(@xsi:type,':')"/>
                  <xsl:choose>
                    <!--handled above, if this is a well-formed record-->
                    <xsl:when test="$ixsitype='ConeSearch' or $ixsitype='OpenSkyNode'">
                      <xsl:text></xsl:text>
                    </xsl:when>
                    <xsl:when test="$ixsitype='ParamHTTP'">
                      <xsl:text>HTTP Request</xsl:text>
                    </xsl:when>
                    <xsl:when test="$ixsitype='WebBrowser'">
                      <xsl:text>Web Page</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                      <xsl:text>Custom Service</xsl:text>
                    </xsl:otherwise>
                  </xsl:choose>
                </xsl:for-each>
              </xsl:when>
               <xsl:otherwise>
                  <xsl:text>Custom Service</xsl:text>
               </xsl:otherwise>
            </xsl:choose>
            <xsl:text>#</xsl:text>
           </xsl:for-each>
         </xsl:when>
         <xsl:when test="$rxsitype='DataCollection'">
            <xsl:text>Data Collection#</xsl:text>
         </xsl:when>
         <xsl:when test="$rxsitype='Organisation'">
            <xsl:text>Organisation#</xsl:text>
         </xsl:when>
         <xsl:when test="contains($rxsitype,'Standard') or $rxsitype='Authority'">
            <xsl:text>VO Support#</xsl:text>
         </xsl:when>
         <xsl:when test="not(@xsi:type)">
            <xsl:text>Generic Resource</xsl:text>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="$rxsitype"/>#</xsl:otherwise>
      </xsl:choose>
   </xsl:template>  

   <xsl:template match="capability" mode="capperif">
      <xsl:param name="val"/>
      <xsl:param name="forstd" select="true()"/>

      <xsl:choose>
         <xsl:when test="count(interface)>1">
            <xsl:for-each select="interface">
               <xsl:if test="not($forstd) or @role='std'">
                  <xsl:value-of select="normalize-space($val)"/>
               </xsl:if>
               <xsl:text>#</xsl:text>
            </xsl:for-each>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="normalize-space($val)"/>
            <xsl:text>#</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template name="interfaceArrayData">
      <xsl:param name="caps"/>
      <xsl:param name="att"/>
      <xsl:param name="el"/>

      <xsl:text>   </xsl:text>
      <TD>
         <xsl:if test="count($caps) > 0"><xsl:text>#</xsl:text></xsl:if>
         <xsl:for-each select="$caps">
            <xsl:choose>
               <xsl:when test="interface">
                  <xsl:for-each select="interface">
                    <xsl:variable name="val">
                       <xsl:choose>
                         <xsl:when test="$att!=''">
                            <xsl:value-of select="attribute::node()[name()=$att]"/>
                         </xsl:when>
                         <xsl:when test="$el!=''">
                            <xsl:value-of select="child::node()[name()=$el]/self::node()[1]"/>
                         </xsl:when>
                       </xsl:choose>
                    </xsl:variable>

                    <xsl:choose>
                       <xsl:when test="($att!='' and $att='xsi:type') or 
                                       $el='xsi:type'">
                          <xsl:value-of select="substring-after($val,':')"/>
                       </xsl:when>
                       <xsl:otherwise>
                          <xsl:value-of select="$val"/>
                       </xsl:otherwise>
                    </xsl:choose>

                    <xsl:text>#</xsl:text>
                  </xsl:for-each>
               </xsl:when>
               <xsl:otherwise><xsl:text>#</xsl:text></xsl:otherwise>
            </xsl:choose>
         </xsl:for-each>
      </TD><xsl:text>
</xsl:text>

   </xsl:template>

   <xsl:template name="capabilityArrayData">
      <xsl:param name="caps"/>
      <xsl:param name="att"/>
      <xsl:param name="el"/>
      <xsl:param name="forstd" select="false()"/>

      <xsl:text>   </xsl:text>
      <TD>
         <xsl:if test="count($caps) > 0"><xsl:text>#</xsl:text></xsl:if>
         <xsl:for-each select="$caps">
            <xsl:variable name="val0">
               <xsl:choose>
                  <xsl:when test="$att!=''">
                     <xsl:value-of select="attribute::node()[name()=$att]"/>
                  </xsl:when>
                  <xsl:when test="$el!=''">
                     <xsl:value-of select="child::node()[name()=$el]/self::node()[1]"/>
                  </xsl:when>
               </xsl:choose>
            </xsl:variable>
            <xsl:variable name="val">
               <xsl:choose>
                  <xsl:when test="$el='maxSearchRadius' and $val0=''">
                     <xsl:value-of select="maxSR"/>
                  </xsl:when>
                  <xsl:otherwise><xsl:value-of select="$val0"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            <xsl:variable name="use">
               <xsl:choose>
                  <xsl:when test="($att!='' and $att='xsi:type') or 
                                  $el='xsi:type'">
                     <xsl:value-of select="substring-after($val,':')"/>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="$val"/>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            <xsl:apply-templates select="." mode="capperif">
               <xsl:with-param name="val" select="$use"/>
               <xsl:with-param name="forstd" select="$forstd"/>
            </xsl:apply-templates>
         </xsl:for-each>
      </TD><xsl:text>
</xsl:text>
      
   </xsl:template>

   <xsl:template name="capabilityName">
      <xsl:param name="caps"/>

      <xsl:text>   </xsl:text>
      <TD>
         <xsl:if test="count($caps) > 0"><xsl:text>#</xsl:text></xsl:if>
         <xsl:for-each select="$caps">
            <xsl:apply-templates select="." mode="capperif">
               <xsl:with-param name="val">
                 <xsl:apply-templates select="." mode="capName"/>
               </xsl:with-param>
               <xsl:with-param name="forstd" select="false()"/>
            </xsl:apply-templates>
         </xsl:for-each>
      </TD><xsl:text>
</xsl:text>
   </xsl:template>

   <xsl:template match="capability" mode="capName">
      <xsl:choose>
<!--
         <xsl:when test="@vx:name">
            <xsl:value-of select="@vx:name"/>
         </xsl:when>
  -->
         <xsl:when test="attribute::node()[local-name()='name']">
            <xsl:value-of select="attribute::node()[local-name()='name']"/>
         </xsl:when>
         <xsl:when test="contains(description,'Name: ')">
            <xsl:call-template name="firstword">
               <xsl:with-param name="in"
                               select="substring-after(description, 'Name: ')"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:variable name="thisid" select="generate-id()"/>
            <xsl:for-each select="../capability">
               <xsl:if test="generate-id(.)=$thisid">
                  <xsl:value-of select="position()"/>
               </xsl:if>
            </xsl:for-each>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template name="firstword">
      <xsl:param name="in"/>
      <xsl:param name="delim" select="' '"/>

      <xsl:variable name="use" select="normalize-space($in)"/>

      <xsl:choose>
         <xsl:when test="contains($in, $delim)">
            <xsl:value-of select="substring-before($in,$delim)"/>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="$in"/></xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <xsl:template 
        match="ri:Resource[starts-with(identifier,'ivo://CDS.VizieR/') and
                           contains(@xsi:type,':CatalogService')]">
      <TR><xsl:text>
</xsl:text>
         <xsl:text>   </xsl:text>
         <TD><xsl:apply-templates select="." mode="gettag"/></TD><xsl:text>
</xsl:text>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="shortName" />
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="title" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/description" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="curation/publisher" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="coverage/waveband" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="identifier" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="@updated" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/subject" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/type" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/contentLevel" />   
            <xsl:with-param name="asarray" select="true()"/>
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="coverage/stc:STCResourceProfile/stc:AstroCoords/stc:Position1D/stc:Size" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="curation/version" />   
         </xsl:call-template>

         <xsl:variable name="caps" select="capability"/>

         <!-- capabilityName -->
         <xsl:call-template name="capabilityNameCDS">
            <xsl:with-param name="caps" select="$caps"/>
         </xsl:call-template>

         <!-- capabilityClass -->
         <xsl:call-template name="capabilityArrayDataCDS">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="att">xsi:type</xsl:with-param>
            <xsl:with-param name="forstd" select="true()"/>
         </xsl:call-template>

         <!-- capabilityStandardID -->
         <xsl:call-template name="capabilityArrayDataCDS">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="att">standardID</xsl:with-param>
            <xsl:with-param name="forstd" select="true()"/>
         </xsl:call-template>

         <xsl:call-template name="capabilityArrayDataCDS">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="el">validationLevel</xsl:with-param>
            <xsl:with-param name="forstd" select="true()"/>
         </xsl:call-template>

         <xsl:call-template name="interfaceArrayDataCDS">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="att" select="'xsi:type'"/>
         </xsl:call-template>
         <xsl:call-template name="interfaceArrayDataCDS">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="att" select="'version'"/>
         </xsl:call-template>
         <xsl:call-template name="interfaceArrayDataCDS">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="att" select="'role'"/>
         </xsl:call-template>
         <xsl:call-template name="interfaceArrayDataCDS">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="el" select="'accessURL'"/>
         </xsl:call-template>

         <xsl:call-template name="capabilityArrayDataCDS">
            <xsl:with-param name="caps" select="$caps"/>
            <xsl:with-param name="el">maxSearchRadius</xsl:with-param>
         </xsl:call-template>

        <xsl:call-template name="capabilityArrayDataCDS">
          <xsl:with-param name="caps" select="$caps"/>
          <xsl:with-param name="el">maxRecords</xsl:with-param>
        </xsl:call-template>

         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="curation/publisher/@ivo-id" />   
         </xsl:call-template>
         <xsl:call-template name="valOrNull">
            <xsl:with-param name="val" select="content/referenceURL" />   
         </xsl:call-template>
      </TR><xsl:text>
</xsl:text>
   </xsl:template>

   <xsl:template name="capabilityArrayDataCDS">
      <xsl:param name="caps"/>
      <xsl:param name="att"/>
      <xsl:param name="el"/>
      <xsl:param name="forstd" select="false()"/>

      <xsl:text>   </xsl:text>
      <TD>
         <xsl:if test="count($caps) > 0"><xsl:text>#</xsl:text></xsl:if>
         <xsl:for-each select="$caps">
            <xsl:variable name="val0">
               <xsl:choose>
                  <xsl:when test="$att!=''">
                     <xsl:value-of select="attribute::node()[name()=$att]"/>
                  </xsl:when>
                  <xsl:when test="$el!=''">
                     <xsl:value-of select="child::node()[name()=$el]/self::node()[1]"/>
                  </xsl:when>
               </xsl:choose>
            </xsl:variable>
            <xsl:variable name="val">
               <xsl:choose>
                  <xsl:when test="$el='maxSearchRadius' and $val0=''">
                     <xsl:value-of select="maxSR"/>
                  </xsl:when>
                  <xsl:otherwise><xsl:value-of select="$val0"/></xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            <xsl:variable name="use">
               <xsl:choose>
                  <xsl:when test="($att!='' and $att='xsi:type') or 
                                  $el='xsi:type'">
                     <xsl:value-of select="substring-after($val,':')"/>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="$val"/>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            
            <xsl:choose>
               <xsl:when test="../table[column[contains(ucd,'pos.eq.ra')] and
                                        column[contains(ucd,'pos.eq.dec')]] and 
                               contains(@xsi:type,':ConeSearch')">
                  <xsl:for-each select="../table[column[contains(ucd,'pos.eq.ra')]
                                          and column[contains(ucd,'pos.eq.dec')]]">
                     <xsl:value-of select="$use"/>
                     <xsl:text>#</xsl:text>
                  </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:apply-templates select="." mode="capperif">
                     <xsl:with-param name="val" select="$use"/>
                     <xsl:with-param name="forstd" select="$forstd"/>
                  </xsl:apply-templates>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:for-each>
      </TD><xsl:text>
</xsl:text>
      
   </xsl:template>

   <xsl:template name="interfaceArrayDataCDS">
      <xsl:param name="caps"/>
      <xsl:param name="att"/>
      <xsl:param name="el"/>

      <xsl:text>   </xsl:text>
      <TD>
         <xsl:if test="count($caps) > 0"><xsl:text>#</xsl:text></xsl:if>
         <xsl:for-each select="$caps">
            <xsl:choose>
               <xsl:when test="interface">
                  <xsl:for-each select="interface">
                    <xsl:variable name="val">
                       <xsl:choose>
                         <xsl:when test="$att!=''">
                            <xsl:value-of select="attribute::node()[name()=$att]"/>
                         </xsl:when>
                         <xsl:when test="$el!=''">
                            <xsl:value-of select="child::node()[name()=$el]/self::node()[1]"/>
                         </xsl:when>
                       </xsl:choose>
                    </xsl:variable>

                    <xsl:variable name="use">
                       <xsl:choose>
                          <xsl:when test="($att!='' and $att='xsi:type') or 
                                          $el='xsi:type'">
                             <xsl:value-of 
                             select="substring-after(normalize-space($val),':')"/>
                          </xsl:when>
                          <xsl:otherwise>
                             <xsl:value-of select="normalize-space($val)"/>
                          </xsl:otherwise>
                       </xsl:choose>
                    </xsl:variable>

                    <xsl:choose>
                       <xsl:when 
                          test="../../table[column[contains(ucd,'pos.eq.ra')] and
                                            column[contains(ucd,'pos.eq.dec')]] and
                                contains(../@xsi:type,':ConeSearch')">
                          <xsl:for-each 
                              select="../../table[column[contains(ucd,'pos.eq.ra')]
                                      and column[contains(ucd,'pos.eq.dec')]]">
                             <xsl:value-of select="$use"/>
                             <xsl:if test="$el='accessURL'">
                               <xsl:text>/</xsl:text>
                               <xsl:call-template name="getTableName">
                                 <xsl:with-param name="id" select="../identifier"/>
                                 <xsl:with-param name="fullname" select="name"/>
                                </xsl:call-template>
                             </xsl:if>
                             <xsl:text>#</xsl:text>
                          </xsl:for-each>
                       </xsl:when>
                       <xsl:otherwise>
                          <xsl:value-of select="normalize-space($use)"/>
                          <xsl:text>#</xsl:text>
                       </xsl:otherwise>
                    </xsl:choose>

                  </xsl:for-each>
               </xsl:when>
               <xsl:otherwise><xsl:text>#</xsl:text></xsl:otherwise>
            </xsl:choose>
         </xsl:for-each>
      </TD><xsl:text>
</xsl:text>

   </xsl:template>

   <xsl:template name="capabilityNameCDS">
      <xsl:param name="caps"/>

      <xsl:text>   </xsl:text>
      <TD>
         <xsl:if test="count($caps) > 0"><xsl:text>#</xsl:text></xsl:if>
         <xsl:for-each select="$caps">
            <xsl:choose>
               <xsl:when test="contains(@xsi:type,':ConeSearch') and 
                               ../table[column[contains(ucd,'pos.eq.ra')] and
                                        column[contains(ucd,'pos.eq.dec')]]">
                  <xsl:for-each select="../table[column[contains(ucd,'pos.eq.ra')]
                                          and column[contains(ucd,'pos.eq.dec')]]">
                     <xsl:call-template name="getTableName">
                        <xsl:with-param name="id" select="../identifier"/>
                        <xsl:with-param name="fullname" select="name"/>
                     </xsl:call-template>
                     <xsl:text>#</xsl:text>
                  </xsl:for-each>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:apply-templates select="." mode="capperif">
                     <xsl:with-param name="val">
                       <xsl:apply-templates select="." mode="capName"/>
                     </xsl:with-param>
                     <xsl:with-param name="forstd" select="false()"/>
                  </xsl:apply-templates>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:for-each>
      </TD><xsl:text>
</xsl:text>
   </xsl:template>

   <xsl:template name="getTableName">
      <xsl:param name="id"/>
      <xsl:param name="fullname"/>
      <xsl:param name="resid" 
                 select="substring-after(substring-after($id,'ivo://'),'/')"/>
      <xsl:variable name="prefix" select="concat($resid,'/')"/>

      <xsl:choose>
         <xsl:when test="starts-with($fullname,$prefix)">
            <xsl:value-of select="substring-after($fullname,$prefix)"/>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="$fullname"/></xsl:otherwise>
      </xsl:choose>
   </xsl:template>

</xsl:stylesheet>
