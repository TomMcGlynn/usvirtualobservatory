<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" 
   xmlns:v1="http://vizier.u-strasbg.fr/VOTable"
   xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
   xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"
   exclude-result-prefixes="vo v1 v2 v3"
   >

<xsl:output method="html" />

<!-- Sort VOTable by column sortOrder and write a page of rows in of HTML -->

<!-- Input parameters -->

<xsl:param name="sortOrder">ascending</xsl:param>
<xsl:param name="sortColumn" />
<xsl:param name="selectedRows" />
<xsl:param name="selectRowUCD">ID_MAIN</xsl:param>
<xsl:param name="page">1</xsl:param>
<xsl:param name="pageLength">20</xsl:param>
<xsl:param name="maxColumns">11</xsl:param>
<xsl:param name="columnOrder"/>

<xsl:param name="decPrecision">10</xsl:param>
<xsl:param name="raPrecision">100</xsl:param>
<xsl:param name="sexSeparator">:</xsl:param>

<xsl:param name="fullTable" />

<!-- Filter parameters -->
<xsl:param name="filterText"></xsl:param>
<xsl:param name="filterTypes"></xsl:param>
<xsl:param name="filterForm">filterForm</xsl:param>
<xsl:param name="filterCallback">filterByColumn</xsl:param>
<xsl:param name="filterReset">resetFilter</xsl:param>
<xsl:param name="filterRow">filterRow</xsl:param>

<!-- Javascript callback functions (also settable as parameters) -->

<xsl:param name="sortCallback">rd.sort</xsl:param>
<xsl:param name="setMaxColumnsCallback">setMaxColumns</xsl:param>
<xsl:param name="resetColumnOrderCallback">resetColumnOrder</xsl:param>
<xsl:param name="setPageLength">rd.setPageLength</xsl:param>
<xsl:param name="selectRowCallback">selectRow</xsl:param>
<xsl:param name="clearSelectionCallback">clearSelection</xsl:param>

<xsl:variable name="lc" select="'abcdefghijklmnopqrstuvwxyz'" />
<xsl:variable name="uc" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

<!-- Computed variables -->

<xsl:variable name="fieldlist" select="/VOTABLE/RESOURCE/TABLE/FIELD|/vo:VOTABLE/vo:RESOURCE/vo:TABLE/vo:FIELD|/v1:VOTABLE/v1:RESOURCE/v1:TABLE/v1:FIELD|/v2:VOTABLE/v2:RESOURCE/v2:TABLE/v2:FIELD|/v3:VOTABLE/v3:RESOURCE/v3:TABLE/v3:FIELD"/>

<xsl:variable name="paramlist" select="/VOTABLE/RESOURCE/PARAM|/vo:VOTABLE/vo:RESOURCE/vo:PARAM|/v1:VOTABLE/v1:RESOURCE/v1:PARAM|/v2:VOTABLE/v2:RESOURCE/v2:PARAM|/v3:VOTABLE/v3:RESOURCE/v3:PARAM"/>

<xsl:variable name="sortname" select="translate($sortColumn,$lc,$uc)"/>
<xsl:variable name="useDescription" select="name($fieldlist/*)='DESCRIPTION'"/>
<xsl:variable name="totalCount" select="$paramlist[@name='VOV:TotalCount']/@value" />

<xsl:variable name="sortColumnNum">
   <xsl:if test="$sortColumn != ''">
      <xsl:call-template name="getColumnByName">
         <xsl:with-param name="value" select="$sortColumn"/>
      </xsl:call-template>
   </xsl:if>
</xsl:variable>

<xsl:variable name="datatype">
   <xsl:choose>
      <xsl:when test="$sortColumnNum=''">text</xsl:when>
      <xsl:otherwise>
         <xsl:for-each select="$fieldlist[position()=$sortColumnNum]">
            <xsl:choose>
               <xsl:when test="not(@arraysize) and (@datatype='float' or @datatype='double'
                  or @datatype='int' or @datatype='long' or @datatype='short'
                  or @datatype='unsignedByte' or @datatype='bit')">number</xsl:when>
               <xsl:otherwise>text</xsl:otherwise>
            </xsl:choose>
         </xsl:for-each>
      </xsl:otherwise>
   </xsl:choose>
</xsl:variable>

<xsl:variable name="raColumnNum">
   <xsl:call-template name="getColumnByUCDs">
      <xsl:with-param name="value" select="'|pos.eq.ra;meta.main|POS_EQ_RA_MAIN|'"/>
      <xsl:with-param name="datatype" select="'|float|double|'"/>
   </xsl:call-template>
</xsl:variable>

<xsl:variable name="decColumnNum">
   <xsl:call-template name="getColumnByUCDs">
      <xsl:with-param name="value" select="'|pos.eq.dec;meta.main|POS_EQ_DEC_MAIN|'"/>
      <xsl:with-param name="datatype" select="'|float|double|'"/>
   </xsl:call-template>
</xsl:variable>

<xsl:variable name="urlColumnNum">
   <xsl:call-template name="getColumnByUCD">
      <xsl:with-param name="value" select="'VOX:Image_AccessReference'"/>
   </xsl:call-template>
</xsl:variable>

<xsl:variable name="formatColumnNum">
   <xsl:call-template name="getColumnByUCD">
      <xsl:with-param name="value" select="'VOX:Image_Format'"/>
   </xsl:call-template>
</xsl:variable>

<xsl:variable name="selectColumnNum">
   <xsl:call-template name="getColumnByUCD">
      <xsl:with-param name="value" select="$selectRowUCD"/>
   </xsl:call-template>
</xsl:variable>

<xsl:template name="getColumnByUCD">
<!-- THIS ASSUMED THAT THE COLUMN EXISTS! -->
<!-- WHEN IT DOESN'T, SAFARI IS UNHAPPY! -->
   <xsl:param name="value"/>
   <xsl:variable name='temp_column'>
      <xsl:for-each select="$fieldlist">
         <xsl:if test="@ucd = $value">
            <xsl:value-of select="position()"/>
         </xsl:if>
      </xsl:for-each>
   </xsl:variable>
   <xsl:choose>
      <xsl:when test="$temp_column != ''">
         <xsl:value-of select="$temp_column"/>
      </xsl:when>
      <xsl:otherwise>-1</xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:template name="getColumnByUCDs">
   <xsl:param name="value"/>
   <xsl:param name="datatype"/>
   <xsl:for-each select="$fieldlist">
      <xsl:if test="contains($value, concat('|',@ucd,'|')) and
         (not($datatype) or contains($datatype,concat('|',@datatype,'|')))">
         <xsl:value-of select="position()"/>
      </xsl:if>
   </xsl:for-each>
</xsl:template>

<xsl:template name="getColumnByName">
   <xsl:param name="value"/>
   <xsl:variable name="tvalue" select="translate($value,$lc,$uc)"/>
   <xsl:for-each select="$fieldlist">
      <xsl:variable name="ID"><xsl:call-template name="getID"/></xsl:variable>
      <xsl:if test="translate($ID,$lc,$uc) = $tvalue">
         <xsl:value-of select="position()"/>
      </xsl:if>
   </xsl:for-each>
</xsl:template>

<!-- ID is primary FIELD identifier (fall back to name if ID is not available) -->

<xsl:template name="getID">
   <xsl:choose>
      <xsl:when test="@ID">
         <xsl:value-of select="@ID"/>
      </xsl:when>
      <xsl:otherwise>
         <xsl:value-of select="@name"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<!-- name is primary FIELD label (fall back to ID if name is not available) -->

<xsl:template name="getName">
   <xsl:choose>
      <xsl:when test="@name">
         <xsl:value-of select="@name"/>
      </xsl:when>
      <xsl:otherwise>
         <xsl:value-of select="@ID"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:variable name="nrows" select="count(/VOTABLE/RESOURCE/TABLE/DATA/TABLEDATA/TR|/vo:VOTABLE/vo:RESOURCE/vo:TABLE/vo:DATA/vo:TABLEDATA/vo:TR|/v1:VOTABLE/v1:RESOURCE/v1:TABLE/v1:DATA/v1:TABLEDATA/v1:TR|/v2:VOTABLE/v2:RESOURCE/v2:TABLE/v2:DATA/v2:TABLEDATA/v2:TR|/v3:VOTABLE/v3:RESOURCE/v3:TABLE/v3:DATA/v3:TABLEDATA/v3:TR)"/>

<xsl:variable name="ncols" select="count($fieldlist)"/>
<xsl:variable name="npages" select="ceiling($nrows div $pageLength)"/>

<xsl:variable name="pageStart">
   <xsl:choose>
      <xsl:when test="$nrows=0"><xsl:value-of select="0"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="number($pageLength)*(number($page)-1)+1"/></xsl:otherwise>
   </xsl:choose>
</xsl:variable>

<xsl:variable name="pageEnd">
   <xsl:choose>
      <xsl:when test="number($pageLength)+number($pageStart)-1 &gt; $nrows"><xsl:value-of select="$nrows"/></xsl:when>
      <xsl:otherwise><xsl:value-of select="number($pageLength)+number($pageStart)-1"/></xsl:otherwise>
   </xsl:choose>
</xsl:variable>

<!-- process the VOTable -->

<xsl:template name="start" match="/">
   <xsl:variable name="votable" select="VOTABLE|vo:VOTABLE|v1:VOTABLE|v2:VOTABLE|v3:VOTABLE" />
   <xsl:for-each select="$votable">
      <xsl:call-template name="votable"/>
   </xsl:for-each>
   <xsl:if test="count($votable)=0">
      <xsl:call-template name="error"/>
   </xsl:if>
</xsl:template>

<!-- error template is called when root VOTABLE node is not found -->

<xsl:template name="error">
   <xsl:variable name="root" select="name(*)"/>
   <xsl:variable name="ns1" select="namespace-uri(*)"/>
   <xsl:variable name="ns">
      <xsl:if test="$ns1"> {<xsl:value-of select="$ns1"/>} </xsl:if>
   </xsl:variable>
   <h2>Error: Input is not a standard VOTable</h2>
   <p>Root node is <i> <xsl:value-of select="$ns"/> </i> <b> <xsl:value-of select="$root"/> </b></p>
   <p>Should be <b> VOTABLE </b> or <i> {http://www.ivoa.net/xml/VOTable/v1.1} </i> <b> VOTABLE </b></p>
</xsl:template>

<xsl:template name="votable">
   <xsl:for-each select="INFO|vo:INFO|v1:INFO|v2:INFO|v3:INFO">
      <xsl:call-template name="info"/>
   </xsl:for-each>
   <xsl:for-each select="RESOURCE|vo:RESOURCE|v1:RESOURCE|v2:RESOURCE|v3:RESOURCE">
      <xsl:call-template name="resource"/>
   </xsl:for-each>
</xsl:template>

<!-- Handle VOTable error return -->

<xsl:template name="info">
   <xsl:if test="@name='QUERY_STATUS' and @value='ERROR'">
      <pre><h2><xsl:value-of select="."/></h2></pre>
   </xsl:if>
</xsl:template>

<xsl:template name="resource">
   <div>
      <xsl:for-each select="TABLE|vo:TABLE|v1:TABLE|v2:TABLE|v3:TABLE">
         <xsl:call-template name="buttons">
            <xsl:with-param name="location" select="'top'"/>
         </xsl:call-template>
         <div class="searchnote">
            Click column heading to sort list - Click rows to select
            <span class="bbox" onclick="{$clearSelectionCallback}();">Reset&#160;selection</span>
            <br />
            Text boxes under columns select matching rows
            <span class="bbox" onclick="return {$filterCallback}(document.{$filterForm});">Apply Filter</span>
            <span class="bbox" onclick="return {$filterReset}(document.{$filterForm});">Clear Filter</span>
            <br />
         </div>
         <!-- wrap entire table in a form for filtering -->
         <form method="get" name="{$filterForm}" id="{$filterForm}"
            onsubmit="return {$filterCallback}(this);"
            onreset="return {$filterReset}(this);" action="">
            <div style="display:none">
               <!-- hide the submit & reset buttons (where should they go?) -->
               <input type="submit" class="submit" name=".submit"
                  value="Filter"
                  title="Enter values for one or more columns in boxes" />
               <input type="reset" class="reset" name=".reset"
                  value="Clear"
                  title="Clear column filter values" />
            </div>
            <table class="data">
               <xsl:call-template name="columnSetting"/>
               <thead>
                  <xsl:call-template name="header">
                     <xsl:with-param name="location" select="'top'" />
                  </xsl:call-template>
               </thead>
               <tbody>
                  <xsl:choose>
                     <xsl:when test="$nrows=0">
                        <tr>
                           <td colspan="{$maxColumns}">
                              <xsl:choose>
                                 <xsl:when test="$totalCount">
                                    <h2>No results remain after filtering</h2>
                                 </xsl:when>
                                 <xsl:otherwise>
                                    <h2>No results found</h2>
                                 </xsl:otherwise>
                              </xsl:choose>
                           </td>
                        </tr>
                     </xsl:when>
                     <xsl:otherwise>
                        <xsl:apply-templates select="DATA/TABLEDATA|vo:DATA/vo:TABLEDATA|v1:DATA/v1:TABLEDATA|v2:DATA/v2:TABLEDATA|v3:DATA/v3:TABLEDATA" />
                     </xsl:otherwise>
                  </xsl:choose>
               </tbody>
               <!-- header and buttons repeat at bottom of table -->
               <tfoot>
                  <xsl:call-template name="header">
                     <xsl:with-param name="location" select="'bottom'" />
                  </xsl:call-template>
               </tfoot>
            </table>
         </form>
         <xsl:call-template name="buttons">
            <xsl:with-param name="location" select="'bottom'"/>
         </xsl:call-template>
      </xsl:for-each>
      <xsl:call-template name="fieldsparams" />
   </div>
</xsl:template>

<!--
   Code gets replicated here for efficiency in selecting different namespaces.
   I've abstracted what I can.  Is there a better way to code this?
-->

<xsl:template match="DATA/TABLEDATA">
   <xsl:for-each select="TR">
      <xsl:sort select="TD[position()=$sortColumnNum]/@val" order="{$sortOrder}" data-type="{$datatype}"/>
      <xsl:sort select="TD[position()=$sortColumnNum]" order="{$sortOrder}" data-type="{$datatype}"/>
      <xsl:if test="not (position() &lt; $pageStart or position() &gt; $pageEnd)">
         <xsl:call-template name="processIncludedRow">
            <xsl:with-param name="rowNum" select="position()" />
            <xsl:with-param name="TDlist" select="TD" />
         </xsl:call-template>
      </xsl:if>
   </xsl:for-each>
</xsl:template>

<xsl:template match="vo:DATA/vo:TABLEDATA">
   <xsl:for-each select="vo:TR">
      <xsl:sort select="vo:TD[position()=$sortColumnNum]/@val" order="{$sortOrder}" data-type="{$datatype}"/>
      <xsl:sort select="vo:TD[position()=$sortColumnNum]" order="{$sortOrder}" data-type="{$datatype}"/>
      <xsl:if test="not (position() &lt; $pageStart or position() &gt; $pageEnd)">
         <xsl:call-template name="processIncludedRow">
            <xsl:with-param name="rowNum" select="position()" />
            <xsl:with-param name="TDlist" select="vo:TD" />
         </xsl:call-template>
      </xsl:if>
   </xsl:for-each>
</xsl:template>

<xsl:template match="v1:DATA/v1:TABLEDATA">
   <xsl:for-each select="v1:TR">
      <xsl:sort select="v1:TD[position()=$sortColumnNum]/@val" order="{$sortOrder}" data-type="{$datatype}"/>
      <xsl:sort select="v1:TD[position()=$sortColumnNum]" order="{$sortOrder}" data-type="{$datatype}"/>
      <xsl:if test="not (position() &lt; $pageStart or position() &gt; $pageEnd)">
         <xsl:call-template name="processIncludedRow">
            <xsl:with-param name="rowNum" select="position()" />
            <xsl:with-param name="TDlist" select="v1:TD" />
         </xsl:call-template>
      </xsl:if>
   </xsl:for-each>
</xsl:template>

<xsl:template match="v2:DATA/v2:TABLEDATA">
   <xsl:for-each select="v2:TR">
      <xsl:sort select="v2:TD[position()=$sortColumnNum]/@val" order="{$sortOrder}" data-type="{$datatype}"/>
      <xsl:sort select="v2:TD[position()=$sortColumnNum]" order="{$sortOrder}" data-type="{$datatype}"/>
      <xsl:if test="not (position() &lt; $pageStart or position() &gt; $pageEnd)">
         <xsl:call-template name="processIncludedRow">
            <xsl:with-param name="rowNum" select="position()" />
            <xsl:with-param name="TDlist" select="v2:TD" />
         </xsl:call-template>
      </xsl:if>
   </xsl:for-each>
</xsl:template>

<xsl:template match="v3:DATA/v3:TABLEDATA">
   <xsl:for-each select="v3:TR">
      <xsl:sort select="v3:TD[position()=$sortColumnNum]/@val" order="{$sortOrder}" data-type="{$datatype}"/>
      <xsl:sort select="v3:TD[position()=$sortColumnNum]" order="{$sortOrder}" data-type="{$datatype}"/>
      <xsl:if test="not (position() &lt; $pageStart or position() &gt; $pageEnd)">
         <xsl:call-template name="processIncludedRow">
            <xsl:with-param name="rowNum" select="position()" />
            <xsl:with-param name="TDlist" select="v3:TD" />
         </xsl:call-template>
      </xsl:if>
   </xsl:for-each>
</xsl:template>

<xsl:template name="processIncludedRow">
   <xsl:param name="rowNum" />
   <xsl:param name="TDlist" />
   <xsl:variable name="selector" select="string($TDlist[position()=$selectColumnNum])"/>
   <tr onclick="{$selectRowCallback}(this,'{$selector}',event)">
      <xsl:attribute name="class">
         <xsl:call-template name="isSelected">
            <xsl:with-param name="selector" select="$selector" />
         </xsl:call-template>
         <xsl:choose>
            <xsl:when test="($rowNum mod 2) = 0">even</xsl:when>
            <xsl:otherwise>odd</xsl:otherwise>
         </xsl:choose>
      </xsl:attribute>
      <xsl:variable name="isSelected">
         <xsl:call-template name="isSelected">
            <xsl:with-param name="selector" select="$selector" />
         </xsl:call-template>
      </xsl:variable>
      <xsl:call-template name="processRow">
         <xsl:with-param name="TDlist" select="$TDlist" />
         <xsl:with-param name="format" select="(TD|vo:TD|v1:TD|v2:TD|v3:TD)[position()=$formatColumnNum]" />
         <xsl:with-param name="selection" select="$isSelected" />
      </xsl:call-template>
   </tr>
</xsl:template>

<!-- create tables describing FIELDs and PARAMs -->

<xsl:template name="fieldsparams">
   <xsl:for-each select="TABLE|vo:TABLE|v1:TABLE|v2:TABLE|v3:TABLE">
      <table><tbody><tr>
         <td class="fieldparam">
            <xsl:call-template name="fieldstable"/>
         </td>
         <td class="fieldparam">
            <xsl:call-template name="paramstable"/>
         </td>
      </tr></tbody></table>
   </xsl:for-each>
</xsl:template>

<xsl:template name="fieldstable">
   <h2>Columns</h2>
   <span class="bbox rightbutton" onclick="{$resetColumnOrderCallback}();" title="Restore original column order">Reset&#160;column&#160;order</span>
   <table class="fields" id="fields">
      <col />
      <col />
      <col />
      <xsl:if test="$useDescription">
         <col width="400" />
      </xsl:if>
      <thead><tr>
         <th>Name</th>
         <th>Unit</th>
         <th>Datatype</th>
         <xsl:if test="$useDescription">
            <th>Description</th>
         </xsl:if>
      </tr></thead>
      <tbody>
         <xsl:choose>
            <xsl:when test="$columnOrder">
               <xsl:call-template name="fieldIter">
                  <xsl:with-param name="count" select="1" />
                  <xsl:with-param name="colnums" select="$columnOrder" />
               </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
               <xsl:for-each select="$fieldlist"> 
                  <xsl:variable name="row" select="position()"/>
                  <xsl:call-template name="fieldrow">
                     <xsl:with-param name="row" select="$row" />
                     <xsl:with-param name="posit" select="$row" />
                  </xsl:call-template>
               </xsl:for-each> 
            </xsl:otherwise>
         </xsl:choose>
      </tbody>
   </table>
</xsl:template>

<xsl:template name="paramstable">
   <xsl:if test="count($paramlist) &gt; 0">
      <h2>Search Parameters</h2>
      <table class="parameters">
         <thead><tr>
            <th>Name</th>
            <th>Value</th>
            <th>Unit</th>
         </tr></thead>
         <tbody>
            <xsl:for-each select="$paramlist">
               <tr>
                  <td> <xsl:value-of select="@name"/> </td>
                  <td> <xsl:value-of select="@value"/> </td>
                  <td> <xsl:value-of select="@unit"/> </td>
               </tr>
            </xsl:for-each>
         </tbody>
      </table>
   </xsl:if>
</xsl:template>

<!-- recursive template to loop over fields in columnOrder -->

<xsl:template name="fieldIter">
   <xsl:param name="count" />
   <xsl:param name="colnums" />
   <xsl:if test="$colnums">
      <xsl:variable name="posit" select="number(substring-before($colnums,','))" />
      <xsl:for-each select="$fieldlist[position()=$posit]"> 
         <xsl:call-template name="fieldrow">
            <xsl:with-param name="row" select="$count" />
            <xsl:with-param name="posit" select="$posit" />
         </xsl:call-template>
      </xsl:for-each> 
      <xsl:call-template name="fieldIter">
         <xsl:with-param name="count" select="1+$count" />
         <xsl:with-param name="colnums" select="substring-after($colnums,',')" />
      </xsl:call-template>
   </xsl:if>
</xsl:template>

<xsl:template name="fieldrow">
   <xsl:param name="row" />
   <xsl:param name="posit" />
   <tr id="fieldrow_{$posit}">
      <xsl:attribute name="class">
         <xsl:choose>
            <xsl:when test="($row mod 2) = 0">even</xsl:when>
            <xsl:otherwise>odd</xsl:otherwise>
         </xsl:choose>
      </xsl:attribute>
      <td>
         <xsl:call-template name="getName"/>
      </td>
      <td>
         <xsl:value-of select="@unit"/>
      </td>
      <td>
         <xsl:value-of select="@datatype"/>
         <xsl:if test="@arraysize">
            <xsl:value-of select="concat('[',@arraysize,']')"/>
         </xsl:if>
      </td>
      <xsl:if test="$useDescription">
         <td>
            <xsl:value-of select="DESCRIPTION|vo:DESCRIPTION|v1:DESCRIPTION|v2:DESCRIPTION|v3:DESCRIPTION"/>
         </td>
      </xsl:if>
   </tr>
<!--
<td><xsl:value-of select="$row"/>:<xsl:value-of select="$ncols"/>:<xsl:value-of select="$maxColumns"/></td>
"Hidden" bar did not show initially when $ncols < $maxColumns ...
<xsl:if test="$row=$maxColumns">
-->
   <xsl:if test="$row=$maxColumns or ( $row=$ncols and $maxColumns &gt; $ncols )">
      <tr class="separator">
         <td colspan="5" align="center">Columns below are hidden - Drag to change</td>
      </tr>
   </xsl:if>
</xsl:template>

<!-- all the page buttons -->

<xsl:template name="buttons">
   <xsl:param name="location"/>
   <div class="buttons {$location}">
      <div class="pagelabel">
         <xsl:if test="$fullTable='no'">Partial</xsl:if>
         Results <b><xsl:value-of select="$pageStart"/>-<xsl:value-of select="$pageEnd"/></b>
         <xsl:if test="$npages != 1 or $totalCount">
            of <b>
               <xsl:value-of select="$nrows"/>
               <xsl:if test="$fullTable='no'">+</xsl:if>
            </b>
         </xsl:if>
         <xsl:if test="$totalCount">
            (<b><xsl:value-of select="$totalCount"/></b> before filtering)
         </xsl:if>
         <xsl:if test="$sortColumnNum != ''">
            sorted by <xsl:value-of select="$sortColumn"/>
         </xsl:if>
      </div>
      <xsl:if test="$npages != 1">
         <div class="pagebuttons">
            <xsl:call-template name="onePage">
               <xsl:with-param name="value" select="number($page)-1"/>
               <xsl:with-param name="label" select="'Previous'"/>
               <xsl:with-param name="class" select="'rev'"/>
            </xsl:call-template>
            <xsl:choose>
               <xsl:when test="$npages &lt; 12">
                  <xsl:call-template name="pageRun">
                     <xsl:with-param name="start" select="1"/>
                     <xsl:with-param name="end" select="$npages"/>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="number($page) &lt; 7">
                  <xsl:call-template name="pageRun">
                     <xsl:with-param name="start" select="1"/>
                     <xsl:with-param name="end" select="9"/>
                  </xsl:call-template>
                  &#8230;
                  <xsl:call-template name="onePage">
                     <xsl:with-param name="value" select="$npages"/>
                  </xsl:call-template>
               </xsl:when>
               <xsl:when test="number($page)+6 &gt; $npages">
                  <xsl:call-template name="onePage">
                     <xsl:with-param name="value" select="1"/>
                  </xsl:call-template>
                  &#8230;
                  <xsl:call-template name="pageRun">
                     <xsl:with-param name="start" select="number($npages)-8"/>
                     <xsl:with-param name="end" select="$npages"/>
                  </xsl:call-template>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:call-template name="onePage">
                     <xsl:with-param name="value" select="1"/>
                  </xsl:call-template>
                  &#8230;
                  <xsl:call-template name="pageRun">
                     <xsl:with-param name="start" select="number($page)-3"/>
                     <xsl:with-param name="end" select="number($page)+3"/>
                  </xsl:call-template>
                  &#8230;
                  <xsl:call-template name="onePage">
                     <xsl:with-param name="value" select="$npages"/>
                  </xsl:call-template>
               </xsl:otherwise>
            </xsl:choose>
            <xsl:call-template name="onePage">
               <xsl:with-param name="value" select="number($page)+1"/>
               <xsl:with-param name="label" select="'Next'"/>
               <xsl:with-param name="class" select="'fwd'"/>
            </xsl:call-template>
         </div>
      </xsl:if>
      <xsl:call-template name="pageLengthControl">
         <xsl:with-param name="location" select="$location"/>
      </xsl:call-template>
   </div>
</xsl:template>

<xsl:template name="onePage">
   <xsl:param name="value"/>
   <xsl:param name="label"/>
   <xsl:param name="class"/>
   <xsl:variable name="plabel">
      <xsl:choose>
         <xsl:when test="$label=''"><xsl:value-of select="$value"/></xsl:when>
         <xsl:otherwise><xsl:value-of select="$label"/></xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <xsl:text> </xsl:text>
   <xsl:choose>
      <xsl:when test="$value &lt; 1 or $value &gt; $npages">
         <span class="button {$class} inactive"><xsl:value-of select="$plabel"/></span>
      </xsl:when>
      <xsl:when test="$page=$value">
         <b><xsl:value-of select="$plabel"/></b>
      </xsl:when>
      <xsl:otherwise>
         <a href="#" onclick="return {$sortCallback}(undefined,undefined,{$value})">
            <span class="button {$class}">
               <xsl:value-of select="$plabel"/>
            </span>
         </a>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<xsl:template name="pageRun">
   <xsl:param name="start"/>
   <xsl:param name="end"/>
   <xsl:call-template name="onePage">
      <xsl:with-param name="value" select="$start"/>
   </xsl:call-template>
   <xsl:if test="$start &lt; $end">
      <xsl:call-template name="pageRun">
         <xsl:with-param name="start" select="number($start)+1" />
         <xsl:with-param name="end" select="$end" />
      </xsl:call-template>
   </xsl:if>
</xsl:template>

<xsl:template name="pageLengthControl">
   <xsl:param name="location"/>
   <div class="pageLengthControl">
      Show
      <select name="pagesize-{$location}" onchange="{$setPageLength}(this.value)">
         <option value="10">
            <xsl:if test="number($pageLength)=10"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
            10
         </option>
         <option value="20">
            <xsl:if test="number($pageLength)=20"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
            20
         </option>
         <option value="50">
            <xsl:if test="number($pageLength)=50"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
            50
         </option>
         <option value="100">
            <xsl:if test="number($pageLength)=100"><xsl:attribute name="selected">selected</xsl:attribute></xsl:if>
            100
         </option>
      </select>
      results per page
   </div>
</xsl:template>


<!-- template setting column properties can be overridden by importing stylesheet -->

<xsl:template name="columnSetting" />

<!-- prefix-* templates do nothing but serve as place holders to be overriden -->
<xsl:template name="prefix-header" />
<xsl:template name="prefix-column" />
<xsl:template name="prefix-filter" />

<!-- column headers come from VOTable FIELDS -->

<xsl:template name="header">
   <xsl:param name="location" />
   <tr>
      <xsl:call-template name="prefix-header">
         <xsl:with-param name="location" select="$location" />
      </xsl:call-template>
      <xsl:choose>
         <xsl:when test="$columnOrder">
            <xsl:call-template name="headerIter">
               <xsl:with-param name="count" select="1" />
               <xsl:with-param name="colnums" select="$columnOrder" />
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:for-each select="$fieldlist"> 
               <xsl:variable name="posit" select="position()"/>
               <xsl:if test="$posit &lt;= $maxColumns">
                  <xsl:call-template name="columnheader">
                     <xsl:with-param name="posit" select="$posit"/>
                  </xsl:call-template>
               </xsl:if>
            </xsl:for-each> 
         </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="$ncols &gt; 1 and $maxColumns &gt; 1">
         <th onclick="{$setMaxColumnsCallback}({$maxColumns - 1})" title="Click to show fewer columns">&#171;</th>
      </xsl:if>
      <xsl:if test="$ncols &gt; $maxColumns">
         <th onclick="{$setMaxColumnsCallback}({$maxColumns + 1})" title="Click to show more columns">&#187;</th>
      </xsl:if>
   </tr>
   <xsl:if test="$location='top'">
      <tr name="{$filterRow}">
         <xsl:call-template name="prefix-filter" />
         <xsl:choose>
            <xsl:when test="$columnOrder">
               <xsl:call-template name="filterIter">
                  <xsl:with-param name="count" select="1" />
                  <xsl:with-param name="colnums" select="$columnOrder" />
               </xsl:call-template>
            </xsl:when>
            <xsl:otherwise>
               <xsl:for-each select="$fieldlist"> 
                  <xsl:variable name="posit" select="position()"/>
                  <xsl:if test="$posit &lt;= $maxColumns">
                     <xsl:call-template name="filterbox">
                        <xsl:with-param name="posit" select="$posit"/>
                     </xsl:call-template>
                  </xsl:if>
               </xsl:for-each> 
            </xsl:otherwise>
         </xsl:choose>
      </tr>
   </xsl:if>
</xsl:template>

<!-- recursive template to loop over fields in columnOrder -->

<xsl:template name="headerIter">
   <xsl:param name="count" />
   <xsl:param name="colnums" />
   <xsl:if test="$colnums and $count &lt;= $maxColumns">
      <xsl:variable name="posit" select="number(substring-before($colnums,','))" />
      <xsl:for-each select="$fieldlist[position()=$posit]"> 
         <xsl:call-template name="columnheader">
            <xsl:with-param name="posit" select="$posit" />
         </xsl:call-template>
      </xsl:for-each> 
      <xsl:call-template name="headerIter">
         <xsl:with-param name="count" select="1+$count" />
         <xsl:with-param name="colnums" select="substring-after($colnums,',')" />
      </xsl:call-template>
   </xsl:if>
</xsl:template>

<xsl:template name="columnheader">
   <xsl:param name="posit"/>
   <xsl:variable name="ID"><xsl:call-template name="getID"/></xsl:variable>
   <xsl:variable name="name"><xsl:call-template name="getName"/></xsl:variable>
   <xsl:choose>
      <xsl:when test="$posit = $urlColumnNum">
         <th class="unsortable"><xsl:value-of select="$name"/></th>
      </xsl:when>
      <xsl:otherwise>
         <th onclick="{$sortCallback}('{$ID}')">
         <xsl:attribute name="title">
            <xsl:variable name="descr"
               select="DESCRIPTION|vo:DESCRIPTION|v1:DESCRIPTION|v2:DESCRIPTION|v3:DESCRIPTION"/>
               <xsl:choose>
                  <xsl:when test="$descr">
                     <xsl:value-of select="concat($descr,' (click to sort)')" />
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="concat('Click to sort by ',$name)" />
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:attribute>
            <xsl:if test="translate($ID,$lc,$uc)=$sortname">
               <xsl:attribute name="class"><xsl:value-of select="$sortOrder"/></xsl:attribute>
            </xsl:if>
            <xsl:value-of select="$name"/>
         </th>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<!-- recursive template to loop over fields in columnOrder -->

<xsl:template name="filterIter">
   <xsl:param name="count" />
   <xsl:param name="colnums" />
   <xsl:if test="$colnums and $count &lt;= $maxColumns">
      <xsl:variable name="posit" select="number(substring-before($colnums,','))" />
      <xsl:for-each select="$fieldlist[position()=$posit]"> 
         <xsl:call-template name="filterbox">
            <xsl:with-param name="posit" select="$posit" />
         </xsl:call-template>
      </xsl:for-each> 
      <xsl:call-template name="filterIter">
         <xsl:with-param name="count" select="1+$count" />
         <xsl:with-param name="colnums" select="substring-after($colnums,',')" />
      </xsl:call-template>
   </xsl:if>
</xsl:template>

<xsl:template name="filterbox">
   <xsl:param name="posit" />
   <td>
      <xsl:if test="$posit != $urlColumnNum and (@datatype='char' or not(@arraysize)  or @arraysize=1)">
         <xsl:variable name="isChar" select="@datatype='char' or @datatype='string'"/>
         <input type="hidden" name="vovfilter{$posit}_type" value="{$isChar}" />
         <input class="filter" type="text" name="vovfilter{$posit}">
            <xsl:attribute name="title">
               <xsl:choose>
                  <xsl:when test="$isChar">String: abc (exact match) or *ab*c* , ! to negate</xsl:when>
                  <xsl:otherwise>Number: 10 or >=10 or 10..20 for a range , ! to negate</xsl:otherwise>
               </xsl:choose>
            </xsl:attribute>
            <xsl:attribute name="value">
               <xsl:variable name="filterSep" select="concat('|',$posit,':')" />
               <xsl:if test="contains($filterText,$filterSep)" >
                  <xsl:value-of select="substring-before(substring-after($filterText,$filterSep),'|')" />
               </xsl:if>
            </xsl:attribute>
         </input>
      </xsl:if>
   </td>
</xsl:template>

<xsl:template name="processRow">
   <xsl:param name="TDlist"/>
   <xsl:param name="format"/>
   <xsl:param name="selection"/>
   <xsl:call-template name="prefix-column">
      <xsl:with-param name="index"        select="position()"   />
      <xsl:with-param name="format"       select="$format"      />
      <xsl:with-param name="isSelected"   select="$selection"   />
      <xsl:with-param name="urlColumnNum" select="$urlColumnNum"/>
   </xsl:call-template>
   <xsl:choose>
      <xsl:when test="$columnOrder">
         <xsl:call-template name="columnIter">
            <xsl:with-param name="count" select="1" />
            <xsl:with-param name="colnums" select="$columnOrder" />
            <xsl:with-param name="TDlist" select="$TDlist" />
         </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:for-each select="$TDlist">
            <xsl:variable name="posit" select="position()"/>
            <xsl:if test="$posit &lt;= $maxColumns">
               <xsl:call-template name="processColumn">
                  <xsl:with-param name="TDlist" select="$TDlist" />
                  <xsl:with-param name="posit" select="$posit" />
               </xsl:call-template>
            </xsl:if>
         </xsl:for-each>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<!-- recursive template to loop over columns in columnOrder -->

<xsl:template name="columnIter">
   <xsl:param name="count" />
   <xsl:param name="colnums" />
   <xsl:param name="TDlist" />
   <xsl:if test="$colnums and $count &lt;= $maxColumns">
      <xsl:variable name="posit" select="number(substring-before($colnums,','))" />
      <xsl:for-each select="$TDlist[position()=$posit]"> 
         <xsl:call-template name="processColumn">
            <xsl:with-param name="TDlist" select="$TDlist" />
            <xsl:with-param name="posit" select="$posit" />
         </xsl:call-template>
      </xsl:for-each>
      <xsl:call-template name="columnIter">
         <xsl:with-param name="count" select="1+$count" />
         <xsl:with-param name="colnums" select="substring-after($colnums,',')" />
         <xsl:with-param name="TDlist" select="$TDlist" />
      </xsl:call-template>
   </xsl:if>
</xsl:template>

<xsl:template name="processColumn">
   <xsl:param name="TDlist"/>
   <xsl:param name="posit"/>
   <xsl:choose>
      <xsl:when test="$posit = $urlColumnNum">
         <xsl:call-template name="processURL">
            <xsl:with-param name="TDlist" select="$TDlist"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:when test="$posit = $decColumnNum">
         <xsl:call-template name="processSex">
            <xsl:with-param name="precision" select="$decPrecision" />
            <xsl:with-param name="scale"	 select="'1'" />
         </xsl:call-template>
      </xsl:when>
      <xsl:when test="$posit = $raColumnNum">
         <xsl:call-template name="processSex">
            <xsl:with-param name="precision" select="$raPrecision" />
            <xsl:with-param name="scale"	 select="'15'" />
         </xsl:call-template>
      </xsl:when>
   <xsl:otherwise>
      <td>
         <!-- Trim long columns (need to make this show full text if clicked)  -->
         <xsl:choose>
            <xsl:when test="substring(.,101)">
               <xsl:value-of select="concat(substring(.,1,97),'...')"/>
            </xsl:when>
<!--
added the arraysize check to make them show up on 
the same line when there was an array
some people may prefer the other way
-->
            <xsl:when test="contains('|float|int|double|',concat('|',$fieldlist[position()=$posit]/@datatype,'|')) and not($fieldlist[position()=$posit]/@arraysize)">
               <xsl:value-of select="." />
            </xsl:when>
            <xsl:otherwise>
               <!-- replace spaces with non-breaking spaces -->
               <xsl:call-template name="replace" />
            </xsl:otherwise>
         </xsl:choose>
      </td>
   </xsl:otherwise>
</xsl:choose>
</xsl:template>

<xsl:template name="replace">
   <xsl:param name="text-string"  select="."/>
   <xsl:param name="find-word"    select="' '"/>
   <xsl:param name="replace-with" select="'&#160;'"/>
   <xsl:choose>
      <xsl:when test="contains($text-string,$find-word)">
         <xsl:call-template name="replace">
            <xsl:with-param name="text-string" select="concat(substring-before($text-string,$find-word),$replace-with,substring-after($text-string,$find-word))"/>
            <xsl:with-param name="find-word" select="$find-word"/>
            <xsl:with-param name="replace-with" select="$replace-with"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:value-of select="$text-string"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>


<xsl:template name="processURL">
   <xsl:param name="TDlist"/>
   <xsl:variable name="format" select="$TDlist[position()=$formatColumnNum]"/>
   <xsl:variable name="href" select="normalize-space(.)"/>
   <xsl:variable name="sformat" select="translate(substring-after($format,'/'),$lc,$uc)"/>
   <xsl:variable name="label">
      <xsl:choose>
         <xsl:when test="$sformat"><xsl:value-of select="$sformat"/></xsl:when>
         <xsl:otherwise>Link</xsl:otherwise>
      </xsl:choose>
   </xsl:variable>
   <td><a href="{$href}"><xsl:value-of select="$label"/></a></td>
</xsl:template>

<!-- Convert to sexagesimal format dd:mm:ss -->

<xsl:template name="processSex">
   <xsl:param name="precision" >10</xsl:param>
   <xsl:param name="scale">1</xsl:param>
<!--
   <xsl:variable name="original" select="." />
Some tables have a leading + which causes problems, so remove it ...
-->
   <xsl:variable name="original" select="translate(.,'+','')" />
   <xsl:choose>
      <xsl:when test = "string-length(normalize-space($original)) &gt; 0" >
         <xsl:variable name="numb"	   select="number($original)"  />
         <xsl:variable name="absnumb" 
            select="round((1-2*($numb &lt; 0))*$numb*3600*$precision div $scale + 0.5)"	  />

         <xsl:variable name="degr"
            select="floor($absnumb div (3600*$precision))" />

         <xsl:variable name="mn"
            select="floor(($absnumb - $degr*3600*$precision) div (60*$precision))" />
         <xsl:variable name="sc"
            select="($absnumb - $precision*(3600*$degr + 60*$mn)) div $precision" />
         <td>
            <xsl:if test="$numb &lt; 0">-</xsl:if>
            <xsl:value-of select="concat(format-number($degr,'00'), $sexSeparator, format-number($mn,'00'), $sexSeparator, format-number($sc, '00.0##'))" />
         </td>
      </xsl:when>
      <xsl:otherwise>
         <td> --- </td>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>

<!-- translate Num from scientific notation and multiply by Factor-->

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

<!-- encode URLs (from url-encode.xsl) -->

<!-- ISO-8859-1 based URL-encoding demo
Written by Mike J. Brown, mike@skew.org.
Updated 2002-05-20.

No license; use freely, but credit me if reproducing in print.

Also see http://skew.org/xml/misc/URI-i18n/ for a discussion of
non-ASCII characters in URIs.
-->

<!-- Characters we'll support.
We could add control chars 0-31 and 127-159, but we won't. -->

<xsl:variable name="ascii"> !"#$%&amp;'()*+,-./0123456789:;&lt;=&gt;?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[\]^_`abcdefghijklmnopqrstuvwxyz{|}~</xsl:variable>
<xsl:variable name="latin1">&#160;&#161;&#162;&#163;&#164;&#165;&#166;&#167;&#168;&#169;&#170;&#171;&#172;&#173;&#174;&#175;&#176;&#177;&#178;&#179;&#180;&#181;&#182;&#183;&#184;&#185;&#186;&#187;&#188;&#189;&#190;&#191;&#192;&#193;&#194;&#195;&#196;&#197;&#198;&#199;&#200;&#201;&#202;&#203;&#204;&#205;&#206;&#207;&#208;&#209;&#210;&#211;&#212;&#213;&#214;&#215;&#216;&#217;&#218;&#219;&#220;&#221;&#222;&#223;&#224;&#225;&#226;&#227;&#228;&#229;&#230;&#231;&#232;&#233;&#234;&#235;&#236;&#237;&#238;&#239;&#240;&#241;&#242;&#243;&#244;&#245;&#246;&#247;&#248;&#249;&#250;&#251;&#252;&#253;&#254;&#255;</xsl:variable>

<!-- Characters that usually don't need to be escaped -->
<xsl:variable name="safe">!'()*-.0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz~</xsl:variable>

<xsl:variable name="hex" >0123456789ABCDEF</xsl:variable>

<xsl:template name="url-encode">
   <xsl:param name="str"/>   
   <xsl:if test="$str">
      <xsl:variable name="first-char" select="substring($str,1,1)"/>
      <xsl:choose>
         <xsl:when test="contains($safe,$first-char)">
            <xsl:value-of select="$first-char"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:variable name="codepoint">
               <xsl:choose>
                  <xsl:when test="contains($ascii,$first-char)">
                     <xsl:value-of select="string-length(substring-before($ascii,$first-char)) + 32"/>
                  </xsl:when>
                  <xsl:when test="contains($latin1,$first-char)">
                     <xsl:value-of select="string-length(substring-before($latin1,$first-char)) + 160"/>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:message terminate="no">Warning: string contains a character that is out of range! Substituting "?".</xsl:message>
                     <xsl:text>63</xsl:text>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:variable>
            <xsl:variable name="hex-digit1" select="substring($hex,floor($codepoint div 16) + 1,1)"/>
            <xsl:variable name="hex-digit2" select="substring($hex,$codepoint mod 16 + 1,1)"/>
            <xsl:value-of select="concat('%',$hex-digit1,$hex-digit2)"/>
         </xsl:otherwise>
      </xsl:choose>
      <xsl:if test="string-length($str) &gt; 1">
         <xsl:call-template name="url-encode">
            <xsl:with-param name="str" select="substring($str,2)"/>
         </xsl:call-template>
      </xsl:if>
   </xsl:if>
</xsl:template>


<!--
   Returns $selectedvalue if the selector is in the comma-delimited 
   list of selectedRows.
   Stupid Xpath 1.0 does not have the $*(#@ ends-with function, so have to
   check that by hand.
-->

<xsl:variable name="selectedvalue">selectedimage </xsl:variable>

<xsl:template name="isSelected">
   <xsl:param name="selector"/>
   <xsl:if test="$selectedRows">
      <xsl:choose>
         <xsl:when test="$selector = $selectedRows or contains($selectedRows,concat(',',$selector,',')) or starts-with($selectedRows,concat($selector,','))">
            <xsl:value-of select="$selectedvalue"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:call-template name="endswithSelected">
               <xsl:with-param name="selector" select="concat(',',$selector)"/>
               <xsl:with-param name="sparam" select="$selectedRows"/>
            </xsl:call-template>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:if>
</xsl:template>

<xsl:template name="endswithSelected">
   <xsl:param name="selector"/>
   <xsl:param name="sparam"/>
   <xsl:if test="contains($sparam,$selector)">
      <xsl:variable name="tail" select="substring-after($sparam,$selector)"/>
      <xsl:choose>
         <xsl:when test="$tail">
            <xsl:call-template name="endswithSelected">
               <xsl:with-param name="selector" select="$selector"/>
               <xsl:with-param name="sparam" select="$tail"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="$selectedvalue"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:if>
</xsl:template>

</xsl:stylesheet>
