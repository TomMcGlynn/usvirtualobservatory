<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" exclude-result-prefixes="vo">

<!-- GRG - removed xsl: from the exclude-result-prefixes-->
<!-- GRG processRow,  added TD for Search ME -->
<!-- GRG header Row,  added TH for the QUERY/SEARCH ME -->
<!-- GRG add in blank TD for first column empty filter row -->
<!-- GRG added vars before processrows:  TDList, capabilityVal -->

<xsl:output method="html" />

<!-- Sort VOTable by column sortOrder and write a page of rows in of HTML -->

<!-- Input parameters -->

<xsl:param name="sortOrder">ascending</xsl:param>
<xsl:param name="sortColumn" />
<xsl:param name="selectedRows" />
<xsl:param name="selectRowUCD">ID_MAIN</xsl:param>
<xsl:param name="page">1</xsl:param>
<xsl:param name="pageLength">20</xsl:param>
<xsl:param name="maxColumns">7</xsl:param>
  <xsl:param name="showDescID" />
<xsl:param name="referrer">keywordsearch.aspx</xsl:param>

  <xsl:param name="decPrecision">10</xsl:param>
<xsl:param name="raPrecision">100</xsl:param>
<xsl:param name="sexSeparator">:</xsl:param>

<xsl:param name="fullTable" />
	
<!-- Filter parameters -->
<xsl:param name="filterText"></xsl:param>
  <xsl:param name="userText"></xsl:param>
<xsl:param name="filterTypes"></xsl:param>
<xsl:param name="filterForm">filterForm</xsl:param>
<xsl:param name="filterCallback">filterByColumn</xsl:param>
<xsl:param name="filterReset">resetFilter</xsl:param>
<xsl:param name="filterRow">filterRow</xsl:param>

<!-- Javascript callback functions (also settable as parameters) -->

<xsl:param name="sortCallback">rd.sort</xsl:param>
<xsl:param name="showColumnCallback">rd.showColumns</xsl:param>
<xsl:param name="setPageLength">rd.setPageLength</xsl:param>
<xsl:param name="setSaveOption">rd.setSaveOption</xsl:param>
<xsl:param name="setSendOption">rd.setSendOption</xsl:param>
<xsl:param name="selectRowCallback">selectRow</xsl:param>
<xsl:param name="selectRowCallbackFromCheckbox">selectRowFromCheckbox</xsl:param>
<xsl:param name="clearSelectionCallback">clearSelection</xsl:param>
<xsl:param name="showDescIDCallback">rd.ShowFullDescID</xsl:param>
<xsl:param name="getReferrer">rd.GetReferrer</xsl:param>
<xsl:param name="PostToNVOPage">rd.PostToNVOPage</xsl:param>
<xsl:param name="hideDescIDCallback">rd.HideFullDescID</xsl:param>

  <xsl:variable name="lc" select="'abcdefghijklmnopqrstuvwxyz'" />
<xsl:variable name="uc" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

<!-- Registry Specific Variables -->

<xsl:variable name="SimpleQueryURL" select="'http://heasarc.gsfc.nasa.gov/vo/squery/?IVOID='" />

<xsl:param name="importantColsShown">#tags#shortName#title#description#publisher#</xsl:param>
<xsl:param name="colsSkipped">##</xsl:param>

<xsl:variable name="columnIndex">
	<xsl:call-template name="indexImportantColumns"/>
</xsl:variable>

<!--
  -  the service that will return a VOResource record given its identifier
  -->
<xsl:param name="getRecordSrvc">
   <xsl:text>getRecord.aspx?id=</xsl:text>
</xsl:param>

<!-- Computed variables -->

<xsl:variable name="fieldlist" select="//FIELD|//vo:FIELD"/>
<xsl:variable name="paramlist" select="//PARAM|//vo:PARAM"/>

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


<!-- Registry Specific Computer Variables -->	
<xsl:variable name="titlePos">
    <xsl:call-template name="getColumnByName">
        <xsl:with-param name="value" select="'title'"/>
    </xsl:call-template>
</xsl:variable>

<xsl:variable name="shortNamePos">
    <xsl:call-template name="getColumnByName">
        <xsl:with-param name="value" select="'shortName'"/>
    </xsl:call-template>
</xsl:variable>
 
<xsl:variable name="tagsPos">
    <xsl:call-template name="getColumnByName">
        <xsl:with-param name="value" select="'tags'"/>
    </xsl:call-template>
</xsl:variable>
  
<xsl:variable name="descriptionPos">
    <xsl:call-template name="getColumnByName">
        <xsl:with-param name="value" select="'description'"/>
    </xsl:call-template>
</xsl:variable>

<xsl:variable name="publisherPos">
    <xsl:call-template name="getColumnByName">
        <xsl:with-param name="value" select="'publisher'"/>
    </xsl:call-template>
</xsl:variable>
   
<xsl:variable name="pubIDPos">
    <xsl:call-template name="getColumnByName">
        <xsl:with-param name="value" select="'publisherID'"/>
    </xsl:call-template>
</xsl:variable>

<xsl:variable name="identifierPos">
    <xsl:call-template name="getColumnByName">
        <xsl:with-param name="value" select="'identifier'"/>
    </xsl:call-template>
</xsl:variable>

<xsl:variable name="referenceURLPos">
    <xsl:call-template name="getColumnByName">
        <xsl:with-param name="value" select="'referenceURL'"/>
    </xsl:call-template>
</xsl:variable>

<xsl:variable name="capabilityVal">
    <xsl:call-template name="getColumnByName">
      <xsl:with-param name="value" select="'capabilityClass'"/>
    </xsl:call-template>
</xsl:variable>

<!-- Generic Computed Variables-->
	
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
    <xsl:param name="value"/>
    <xsl:for-each select="$fieldlist">
        <xsl:if test="@ucd = $value">
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


<xsl:variable name="nrows" select="count(//TABLEDATA/TR|//vo:TABLEDATA/vo:TR)"/>
<xsl:variable name="ncols" select="count($fieldlist)"/>
<xsl:variable name="npages" select="ceiling($nrows div $pageLength)"/>

<xsl:variable name="pageStart" select="number($pageLength)*(number($page)-1)+1"/>

<xsl:variable name="pageEnd">
    <xsl:choose>
        <xsl:when test="number($pageLength)+number($pageStart)-1 &gt; $nrows"><xsl:value-of select="$nrows"/></xsl:when>
        <xsl:otherwise><xsl:value-of select="number($pageLength)+number($pageStart)-1"/></xsl:otherwise>
    </xsl:choose>
</xsl:variable>

<!-- process the VOTable -->

<xsl:template match="/">
    <xsl:variable name="votable" select="VOTABLE|vo:VOTABLE" />

  <xsl:if test="count($votable) > 0">
          <xsl:call-template name="saveResultsButtons" />
  </xsl:if>


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
    <xsl:for-each select="INFO|vo:INFO">
        <xsl:call-template name="info"/>
    </xsl:for-each>
    <xsl:for-each select="RESOURCE|vo:RESOURCE">
        <xsl:call-template name="resource"/>
    </xsl:for-each>
</xsl:template>

<!-- Handle VOTable error return -->

<xsl:template name="info">
    <xsl:if test="@name='QUERY_STATUS' and @value='ERROR'">
        <h2><xsl:value-of select="."/></h2>
    </xsl:if>
</xsl:template>

<xsl:template name="resource">
    <div>
        <xsl:choose>
        <xsl:when test="$nrows=0">
            <h2>No results in directory.  Please check your spelling, or try using a more general term.</h2>
        </xsl:when>
        <xsl:otherwise>
            <xsl:for-each select="TABLE|vo:TABLE">
                <xsl:call-template name="buttons">
                    <xsl:with-param name="location" select="'top'"/>
                </xsl:call-template>
                <div class="searchnote">
                    Click column heading to sort list - Click checkbox to select
                    <span class="bbox" onclick="{$clearSelectionCallback}();">Reset selection</span>
					<br /> <br />
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
					<col/>
					<col/>
					<col/>
					<col/>
					<col/>
                    <thead>
						<xsl:call-template name="header">
							<xsl:with-param name="location" select="'top'" />
						</xsl:call-template>
                    </thead>
                    <tbody>
                    <xsl:for-each select="DATA/TABLEDATA|vo:DATA/vo:TABLEDATA">
                        <xsl:for-each select="TR|vo:TR">
                            <xsl:sort select="(TD|vo:TD)[position()=$sortColumnNum]" order="{$sortOrder}" data-type="{$datatype}"/>
                            <xsl:if test="not (position() &lt; $pageStart or position() &gt; $pageEnd)">
                               <xsl:variable name="selector" select="string((TD|vo:TD)[position()=$selectColumnNum])"/>
                                <xsl:variable name="oddeven">
                                   <xsl:choose>
                                      <xsl:when test="(position() mod 2) = 0">
                                         <xsl:text>even</xsl:text>
                                      </xsl:when>
                                      <xsl:otherwise>odd</xsl:otherwise>
                                   </xsl:choose>
                                </xsl:variable>
                                <xsl:variable name="ident" select="(TD|vo:TD)[position()=$identifierPos]"/>
                                <tr id="{$ident}">
                                    <xsl:attribute name="class">
                                        <xsl:call-template name="isSelected">
                                            <xsl:with-param name="selector" select="$selector" />
                                        </xsl:call-template>
                                        <xsl:value-of select="$oddeven"/>
                                    </xsl:attribute><xsl:text>
</xsl:text>

                                    <xsl:call-template name="processRow">
                                        <xsl:with-param name="format" select="(TD|vo:TD)[position()=$formatColumnNum]" />
                                        <xsl:with-param name="selector" select="$selector" />
                                    </xsl:call-template>

                                </tr>
                            </xsl:if>
                        </xsl:for-each>
                    </xsl:for-each>
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
        </xsl:otherwise>
        </xsl:choose>
    </div>
</xsl:template>

<!-- create tables describing FIELDs and PARAMs -->

<xsl:template name="fieldsparams">
	<xsl:variable name="useDescription" select="name($fieldlist/*)='DESCRIPTION'"/>
  <xsl:variable name="ident" select="(TD|vo:TD)[position()=$identifierPos]" />
  <xsl:for-each select="TABLE|vo:TABLE">
        <table><tbody>
        <tr><td class="fieldparam">
        <h2>Columns</h2>
        <table class="fields">
			<col />
			<col />
			<col />
			<xsl:if test="$useDescription">
				<col width="400" />
			</xsl:if>
            <thead><tr>
				<th>Name</th>
				<th>Unit</th>
				<!--th>Datatype</th-->
				<xsl:if test="$useDescription">
					<th class="desc">Description</th>
				</xsl:if>
                <!--
                <th>precision</th>
                <th>width</th>
                <th>ref</th>
                <th>type</th>
                -->
            </tr></thead>
            <tbody>
            <xsl:for-each select="$fieldlist"> 
                <tr onclick="{$showColumnCallback}({position()})" title="Click to show only columns above this">
					<xsl:attribute name="class">
						<xsl:choose>
							<xsl:when test="(position() mod 2) = 0">even</xsl:when>
							<xsl:otherwise>odd</xsl:otherwise>
						</xsl:choose>
					</xsl:attribute>
					<td> <xsl:call-template name="getName"/> </td>
                    <td> <xsl:value-of select="@unit"/> </td>
					<xsl:if test="$useDescription">
            <td class="desc">
              <xsl:call-template name="fmtDesc">
                <xsl:with-param name="text" select="(TD|vo:TD)[position()=$descriptionPos]"/>
                <xsl:with-param name="ident"
                                select="$ident" />
                <xsl:with-param name="showDescID" select="$showDescID" />
                </xsl:call-template>
              <xsl:text>
   </xsl:text>
            </td>
            <xsl:text>
</xsl:text>
          </xsl:if>
					<!--
                    <td> <xsl:value-of select="@precision"/> </td>
                    <td> <xsl:value-of select="@width"/> </td>
                    <td> <xsl:value-of select="@ref"/> </td>
                    <td> <xsl:value-of select="@type"/> </td>
                    -->
                </tr>
                <xsl:if test="position()=$maxColumns and $maxColumns!=$ncols">
                    <tr class="separator"><td colspan="10" align="center">The fields below are hidden</td></tr>
                </xsl:if>
            </xsl:for-each> 
            </tbody>
        </table>
        </td>
        <td class="fieldparam">
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
        </td>
        </tr></tbody></table>
    </xsl:for-each>
</xsl:template>

<!-- all the page buttons -->
<xsl:template name="buttons">
    <xsl:param name="location"/>
	<xsl:variable name="totalCount" select="$paramlist[@name='VOV:TotalCount']/@value" />
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

  <xsl:template name="saveResultsButtons">
    <div class="saveResultsButtons">
      <table width="100%">
      <tr>
        <td valign="top" align="left" width="70%">
          <select name="saveoptions" onchange="{$setSaveOption}(this.value)">
            <option value="AllAsCSV">
              Save All Results as CSV
            </option>
            <option value="AllAsXML">
              Save All Results as VOTable
            </option>
            <option value="SelectedAsCSV">
              Save Selected Results as CSV
            </option>
            <option value="SelectedAsXML">
              Save Selected Results as VOTable
            </option>
            <option value="FilteredAsCSV">
              Save Filtered Results as CSV
            </option>
            <option value="FilteredAsXML">
              Save Filtered Results as VOTable
            </option>
          </select>
          <button onclick="return rd.saveResults()" title="SaveCurrentOption" name="SaveCurrentOption">
            Save
          </button>
        </td>
        <td valign="top" align="right">
          <xsl:call-template  name="backbuttons"/>
        </td>
      </tr>
      </table>
    </div>
  </xsl:template>

  <xsl:template name="backbuttons">
    <xsl:choose>
      <xsl:when test="contains($referrer, 'caltech')">
        Next, Send Results to
        <select name="SendOptions" onchange="{$setSendOption}(this.value)">
          <option value=".">
          </option>
          <option value="{$referrer}">
            VIM
          </option>
          <option value="http://heasarc.gsfc.nasa.gov/vo/">
            DataScope
          </option>
          <option value="http://irsa.ipac.caltech.edu/cgi-bin/VOInventory/nph-voInventory">
            Inventory
          </option>
        </select><button onclick="{$PostToNVOPage}({$nrows})" title="PostTo" name="PostTo">Send</button>
      </xsl:when>
      <xsl:otherwise>
        Next, Send Results to
        <select name="SendOptions" onchange="{$setSendOption}(this.value)">
          <option value=".">
            </option>
            <option value="http://envoy5.cacr.caltech.edu:8888/">
              VIM
            </option>
            <option value="http://heasarc.gsfc.nasa.gov/vo/">
              DataScope
            </option>
            <option value="http://irsa.ipac.caltech.edu/cgi-bin/VOInventory/nph-voInventory">
              Inventory
            </option>
          </select><button onclick="{$PostToNVOPage}({$nrows})" title="PostTo" name="PostTo">Send</button>
       </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
<!-- column headers come from VOTable FIELDS -->



<xsl:template match="FIELD|vo:FIELD" mode="THforColumn">
   <xsl:param name="sortname"/>
   <xsl:param name="name"/>
   <xsl:param name="ID"/>
   <xsl:variable name="useid">

      <xsl:choose>
         <xsl:when test="$ID=''">
            <xsl:call-template name="getID"/>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="$ID"/></xsl:otherwise>
      </xsl:choose>

   </xsl:variable>
   <xsl:variable name="usename">
      <xsl:choose>
         <xsl:when test="$name=''">
            <xsl:call-template name="getName"/>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="$name"/></xsl:otherwise>
      </xsl:choose>

   </xsl:variable>

   <xsl:text>   </xsl:text>
   <th onclick="{$sortCallback}('{$useid}')" title="Click to sort by {$usename}"
       align="left">

       <xsl:choose>
          <xsl:when test="$sortname='unsortable'">
             <xsl:attribute name="class">
                <xsl:value-of select="'unsortable'"/>
             </xsl:attribute>
          </xsl:when>

          <xsl:when test="translate($ID,$lc,$uc)=$sortname">

             <xsl:attribute name="class">

                <xsl:value-of select="$sortOrder"/>

             </xsl:attribute>

          </xsl:when>

       </xsl:choose>

       <xsl:value-of select="$usename"/>
   </th><xsl:text>
</xsl:text>
</xsl:template>

<xsl:template name="filterbox">
	<xsl:variable name="posit" select="position()-1" />
	<xsl:if test="@datatype='char' or not(@arraysize)  or @arraysize=1">
		<input type="hidden" name="vovfilter{$posit}_type" value="{@datatype = 'char'}" />
    <input type="hidden" name="vovfilter{$posit}_usertext" value="{$userText}"></input>
		<input type="text" name="vovfilter{$posit}">
			<xsl:attribute name="title">
				<xsl:choose>
					<xsl:when test="@datatype='char'" >
						Character column: Only plain character matches (including * wildcards) supported.
					</xsl:when>
					<xsl:otherwise>
						Numeric column: 10 or >=10 or 10..20 for a range
					</xsl:otherwise>
			   </xsl:choose>
			</xsl:attribute>
			<xsl:attribute name="value">
				<xsl:variable name="filterSep" select="concat('|',$posit,':')" />
      <xsl:if test="contains($userText,$filterSep)" >
        <xsl:value-of select="substring-before(substring-after($userText,$filterSep),'|')" />
      </xsl:if>
			</xsl:attribute>
		</input>
	</xsl:if>
</xsl:template>

<!-- column headers come from VOTable FIELDS -->

<xsl:template name="header">
	<xsl:param name="location" />
	
    <tr>
        <xsl:variable name="sortname" select="translate($sortColumn,$lc,$uc)"/>
      <th class="selwidth">select</th>
      <th class="midwidth">browse / query</th>
		<xsl:for-each select="$fieldlist"> 
			<xsl:if test="position() &lt;= $maxColumns">
				<xsl:variable name="ID"><xsl:call-template name="getID"/></xsl:variable>
				<xsl:variable name="name"><xsl:call-template name="getName"/></xsl:variable>
				<xsl:choose>
					<xsl:when test="position() = $urlColumnNum">
						<th class="unsortable"><xsl:value-of select="$name"/></th>
					</xsl:when>
          <xsl:when test="position() = $descriptionPos">
            <th class="desc" onclick="{$sortCallback}('{$ID}')">
              <xsl:attribute name="title">
                <xsl:variable name="descr"
									select="DESCRIPTION|vo:DESCRIPTION"/>
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
                <xsl:attribute name="class">
                  <xsl:value-of select="concat($sortOrder, ' midwidth')"/>
                </xsl:attribute>
              </xsl:if>
              <xsl:value-of select="$name"/>
            </th>
          </xsl:when>
					<xsl:otherwise>
						<th class="midwidth" onclick="{$sortCallback}('{$ID}')">
							<xsl:attribute name="title">
								<xsl:variable name="descr"
									select="DESCRIPTION|vo:DESCRIPTION"/>
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
			</xsl:if>
		</xsl:for-each> 
        <xsl:if test="$ncols &gt; 1 and $maxColumns &gt; 1">
				<th class="smallwidth" onclick="{$showColumnCallback}({$maxColumns - 1})" title="Click to show fewer columns">&#171;</th>
        </xsl:if>
        <xsl:if test="$ncols &gt; $maxColumns">
			<th class="smallwidth" onclick="{$showColumnCallback}({$maxColumns + 1})" title="Click to show more columns">&#187;</th>
		</xsl:if>
	</tr>
	<xsl:if test="$location='top'">
		<tr name="{$filterRow}">
			<td></td>
			<xsl:for-each select="$fieldlist">
				<!--xsl:if test="position() &lt;= $maxColumns"-->
        <xsl:if test="position() &lt;= $maxColumns+1">
          <xsl:choose>
            <!--xsl:when test="position() = $urlColumnNum"-->
            <xsl:when test="position() &lt; 2">
              <td class="smallwidth"></td>
            </xsl:when>
            <xsl:otherwise>
              <td>
                <xsl:call-template name="filterbox" />
              </td>
            </xsl:otherwise>
          </xsl:choose>
        </xsl:if>
			</xsl:for-each>
    </tr>
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

<xsl:template name="isSelectedCheckbox">
  <xsl:param name="ident"/>
  <xsl:param name="selector"/>
  <xsl:choose>
    <xsl:when test="$selectedRows">
        <xsl:choose>
          <xsl:when test="$selector = $selectedRows or contains($selectedRows,concat(',',$selector,',')) or starts-with($selectedRows,concat($selector,','))">
            <input id="cb-{$ident}" type="checkbox" name="cb-{$ident}" value="cb-{$ident}" onClick="{$selectRowCallbackFromCheckbox}(this,'{$ident}',event)" checked="true"/>
            <label for="cb-{$ident}"> </label>
          </xsl:when>
          <xsl:otherwise>
            <xsl:call-template name="endswithSelectedCheckbox">
              <xsl:with-param name="ident" select="$ident"/>
              <xsl:with-param name="selector" select="concat(',',$selector)"/>
              <xsl:with-param name="sparam" select="$selectedRows"/>
            </xsl:call-template>
          </xsl:otherwise>
        </xsl:choose>
      </xsl:when>
    <xsl:otherwise>
      <input id="cb-{$ident}" type="checkbox" name="cb-{$ident}" value="cb-{$ident}" onClick="{$selectRowCallbackFromCheckbox}(this,'{$ident}',event)" />
      <label for="cb-{$ident}"></label>
    </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <xsl:template name="endswithSelectedCheckbox">
    <xsl:param name="ident" />
    <xsl:param name="selector"/>
    <xsl:param name="sparam"/>
    <xsl:choose>
    <xsl:when test="contains($sparam,$selector)">
      <xsl:variable name="tail" select="substring-after($sparam,$selector)"/>
      <xsl:choose>
        <xsl:when test="$tail">
          <xsl:call-template name="endswithSelectedCheckbox">
            <xsl:with-param name="ident" select="$ident"/>
            <xsl:with-param name="selector" select="$selector"/>
            <xsl:with-param name="sparam" select="$tail"/>
          </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
          <input id="cb-{$ident}" type="checkbox" name="cb-{$ident}" value="cb-{$ident}" onClick="{$selectRowCallbackFromCheckbox}(this,'{$ident}',event)" checked="true"/>
          <label for="cb-{$ident}"></label>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:when>
      <xsl:otherwise>
        <input id="cb-{$ident}" type="checkbox" name="cb-{$ident}" value="cb-{$ident}" onClick="{$selectRowCallbackFromCheckbox}(this,'{$ident}',event)" />
        <label for="cb-{$ident}"></label>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>
  
  <!-- 
  -  render a single row of the table.
  -->
  <xsl:template name="processRow">

    <xsl:param name="format"/>
    <xsl:param name="selector"/>

    <xsl:variable name="ident" select="(TD|vo:TD)[position()=$identifierPos]"/>
    <xsl:variable name="refURL" select="(TD|vo:TD)[position()=$referenceURLPos]"/>
    <xsl:variable name="tags" select="(TD|vo:TD)[position()=$tagsPos]"/>
    <xsl:variable name="cap" select="(TD|vo:TD)[position()=$capabilityVal]"/>
    <xsl:variable name="desc" select="(TD|vo:TD)[position()=$descriptionPos]" />
    <xsl:variable name="srvcType">
      <xsl:choose>
        <xsl:when test="contains($cap,'SimpleImageAccess')">sia</xsl:when>
        <xsl:when test="contains($cap,'ConeSearch')">cone</xsl:when>
      </xsl:choose>
    </xsl:variable>
    <xsl:variable name="numcaps" select="string-length((TD|vo:TD)[position()=$tagsPos])-string-length(translate((TD|vo:TD)[position()=$tagsPos],'#','')) - 1"/>
    <td class="selwidth" valign="center">
      <xsl:call-template name="isSelectedCheckbox">
        <xsl:with-param name="ident" select="$ident" />
        <xsl:with-param name="selector" select="$selector" />
        <!--input id="cb-{$ident}" type="checkbox" name="cb-{$ident}" value="cb-{$ident}" onClick="{$selectRowCallbackFromCheckbox}(this,'{$ident}',event)" checked="true"/>
      <label for="cb-{$ident}">Select</label-->
      </xsl:call-template>
    </td>
    <td class="midwidth" valign="top">
      <xsl:text />
      <a href="{$getRecordSrvc}{$ident}">Full Record</a>
      <br />
      <xsl:text />
      <a href="{$refURL}">More Info</a>
      <xsl:text />
      <xsl:if test="$srvcType != '' and $numcaps = 1">
        <br />
        <xsl:text />
        <a href="{$SimpleQueryURL}{$ident}&amp;type={$srvcType}">Search Me</a>
        <xsl:text />
      </xsl:if>
    </td>
    <xsl:text />

    <xsl:variable name="nImportantCols">
      <xsl:call-template name="countImportantColumns"/>
    </xsl:variable>

    <xsl:call-template name="importantPartOfData">
      <xsl:with-param name="maxCols" select="number($maxColumns)"/>
      <xsl:with-param name="ncols" select="1"/> <!--starting column-->
    </xsl:call-template>

    <xsl:call-template name="restOfData">
      <xsl:with-param name="maxCols" select="number($maxColumns)"/>
      <xsl:with-param name="ncols" select="number($nImportantCols)+1"/>
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="processURL">
	<xsl:param name="format"/>
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

<!--
  -  count the number of columns in the list of important columns
  -->

<xsl:template name="countImportantColumns">
   <xsl:param name="importantNames" 
              select="substring-after($importantColsShown,'#')"/>
   <xsl:param name="ncols" select="0"/>

   <xsl:variable name="restOfList" 
                select="normalize-space(substring-after($importantNames,'#'))"/>
   <xsl:choose>
      <xsl:when test="string-length($restOfList) &gt; 0">
         <xsl:call-template name="countImportantColumns">
            <xsl:with-param name="importantNames" select="$restOfList"/>
            <xsl:with-param name="ncols" select="number($ncols)+1"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:value-of select="number($ncols)+1"/>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>


<!--
  -  render the rest of the (unimportant) Header columns
  -->
<xsl:template name="restOfHeader">
   <xsl:param name="importantNames"/>
   <xsl:param name="maxCols" select='1'/>
   <xsl:param name="ncols" select='0'/>
   <xsl:param name="colNum" select='1'/>
   <xsl:param name="sortname"/>

   <xsl:if test="$ncols &lt; $maxCols">
      <xsl:for-each select="(//FIELD|//vo:FIELD)[position()=number($colNum)]">
         <xsl:variable name="ID">
            <xsl:call-template name="getID"/>
         </xsl:variable>
         <xsl:variable name="name">
            <xsl:call-template name="getName"/>
         </xsl:variable>

         <xsl:variable name="nxtNcols">
            <xsl:choose>
               <xsl:when test="contains($importantNames,concat('#',$ID,'#'))">
                  <xsl:copy-of select="$ncols"/>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:copy-of select="number($ncols)+1"/>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:variable>

         <xsl:if test="not(contains($importantNames, concat('#',$ID,'#')))">
            <xsl:apply-templates mode="THforColumn" 
                                 select="(//FIELD|//vo:FIELD)[1]">
               <xsl:with-param name="name" select="$name"/>
               <xsl:with-param name="sortname" select="$sortname"/>
               <xsl:with-param name="ID" select="$ID"/>
            </xsl:apply-templates>
         </xsl:if>

         <xsl:call-template name="restOfHeader">
            <xsl:with-param name="importantNames" select="$importantNames"/>
            <xsl:with-param name="maxCols" select="$maxCols"/>
            <xsl:with-param name="ncols" select="number($nxtNcols)"/>
            <xsl:with-param name="colNum" select="number($colNum)+1"/>
            <xsl:with-param name="sortname" select="$sortname"/>
         </xsl:call-template>
      </xsl:for-each>
   </xsl:if>
</xsl:template>




<!--
  -  create an index of the ordered columns
  -->
<xsl:template name="indexImportantColumns">
   <xsl:param name="importantNames" 
              select="substring-after($importantColsShown,'#')"/>

   <xsl:variable name="col" 
        select="normalize-space(substring-before($importantNames,'#'))"/>

   <xsl:choose>
      <xsl:when test="$col!=''">
         <xsl:text>#</xsl:text>
         <xsl:for-each select="//FIELD|//vo:FIELD">
            <xsl:if test="@ID=$col">
               <xsl:value-of select="position()"/>
            </xsl:if>
         </xsl:for-each>

        
         <xsl:call-template name="indexImportantColumns">
            <xsl:with-param name="importantNames"
                            select="substring-after($importantNames,'#')"/>
         </xsl:call-template>
      </xsl:when>
      <xsl:otherwise>
         <xsl:text>#</xsl:text>
      </xsl:otherwise>
   </xsl:choose>
</xsl:template>


<!--
  -  print the ordered data columns
  -->

<xsl:template name="importantPartOfData">
   <xsl:param name="importantColNums" 
              select="substring-after($columnIndex,'#')"/>
   <xsl:param name="maxCols" select='1'/>
   <xsl:param name="ncols" select='0'/>

   <xsl:variable name="nc" select="number($ncols)"/>
   <xsl:variable name="ident" select="(TD|vo:TD)[position()=$identifierPos]"/>

  <!-- tags -->
   <xsl:if test="$nc &lt;= $maxCols">
      <xsl:call-template name="TDforColumn">
         <xsl:with-param name="tdpos" select="$tagsPos"/>
      </xsl:call-template>
   </xsl:if>

   <!-- short name -->
   <xsl:if test="$nc + 1 &lt;= $maxCols">
      <xsl:call-template name="TDforColumn">
         <xsl:with-param name="tdpos" select="$shortNamePos"/>
      </xsl:call-template>
   </xsl:if>

   <!-- title -->
   <xsl:if test="$nc + 2 &lt;= $maxCols">
      <xsl:text />
      <td class="title">
         <!--xsl:if test="$nc + 3 &lt; $maxCols">
            <xsl:attribute name="colspan">2</xsl:attribute>
         </xsl:if-->
         <xsl:value-of select="(TD|vo:TD)[position()=$titlePos]"/>
      </td><xsl:text>
</xsl:text>
   </xsl:if>
  
  <!--description-->
  <xsl:if test="$nc + 3 &lt;= $maxCols">
    <td class="desc">
      <xsl:text />
        <xsl:call-template name="fmtDesc">
          <xsl:with-param name="text" select="(TD|vo:TD)[position()=$descriptionPos]"/>
          <xsl:with-param name="ident" select="$ident" />
          <xsl:with-param name="showDescID" select="$showDescID" />
        </xsl:call-template>
        <!--br />
        <a href="{$getRecordSrvc}{$ident}"> <xsl:value-of select="$ident"/> </a-->
        <xsl:text />
      </td>
  </xsl:if>

   <!-- publisher -->
   <xsl:if test="$nc + 4 &lt;= $maxCols">
      <xsl:text>   </xsl:text>
      <td class="wrappable">
         <xsl:choose>
            <xsl:when test="normalize-space((TD|vo:TD)[position()=$pubIDPos])">
               <a href="{$getRecordSrvc}{normalize-space((TD|vo:TD)[position()=$pubIDPos])}"><xsl:value-of select="(TD|vo:TD)[position()=$publisherPos]"/></a>
            </xsl:when>
            <xsl:otherwise>
               <xsl:value-of select="(TD|vo:TD)[position()=$publisherPos]"/>
            </xsl:otherwise>
         </xsl:choose>
      </td><xsl:text>
</xsl:text>
   </xsl:if>
</xsl:template>


<!--
  -  split an array value
  -->

<xsl:template name="splitArray">
   <xsl:param name="val" select="'#'"/>

   <xsl:variable name="nxt" select="substring-before($val,'#')"/>
   <xsl:variable name="rest" select="substring-after($val,'#')"/>

   <xsl:if test="$nxt!=''">
      <xsl:value-of select="$nxt"/> 
      <xsl:if test="$rest!=''"><br /><xsl:text> </xsl:text></xsl:if>
   </xsl:if>

   <xsl:if test="$rest!=''">
      <xsl:call-template name="splitArray">
         <xsl:with-param name="val" select="$rest"/>
      </xsl:call-template>
   </xsl:if>
</xsl:template>


<!--
  -  render a cell of data
  -->
<xsl:template name="TDforColumn">
   <xsl:param name="tdpos"/>
   <xsl:param name="datatype">
      <xsl:choose>
         <xsl:when test="$tdpos!=''">
            <xsl:value-of select="(//FIELD|//vo:FIELD)[position()=$tdpos]"/>
         </xsl:when>
         <xsl:otherwise>int</xsl:otherwise>
      </xsl:choose>
   </xsl:param>
   <xsl:param name="val">
      <xsl:if test="$tdpos!=''">
         <xsl:value-of select="(TD|vo:TD)[position()=$tdpos]"/>
      </xsl:if>
   </xsl:param>
   <xsl:param name="rowspan">1</xsl:param>

   <xsl:text>   </xsl:text>
   <td rowspan="{$rowspan}">
      <xsl:if test="$datatype='char'">
         <xsl:attribute name="class">center</xsl:attribute>
      </xsl:if>
      <xsl:choose>
         <xsl:when test="starts-with($val,'http://')">
            <a href="{$val}"><xsl:value-of select="$val"/></a>
         </xsl:when>
         <xsl:when test="starts-with($val,'ivo://')">
            <a href="{$getRecordSrvc}{$val}"><xsl:value-of select="$val"/></a>
         </xsl:when>
         <xsl:when test="starts-with($val,'#')">
            <xsl:call-template name="splitArray">
               <xsl:with-param name="val" select="substring-after($val,'#')"/>
            </xsl:call-template>
         </xsl:when>
         <xsl:otherwise>
            <xsl:copy-of select="$val"/>
         </xsl:otherwise>
      </xsl:choose>
   </td><xsl:text>
</xsl:text>


</xsl:template>


<!--
  -  render the rest of the (unimportant) data columns
  -->
<xsl:template name="restOfData">
   <xsl:param name="importantColNums" select="$columnIndex"/>
   <xsl:param name="maxCols" select='1'/>
   <xsl:param name="ncols" select='0'/>
   <xsl:param name="colNum" select='1'/>

   <xsl:if test="$ncols &lt;= $maxCols">
      <xsl:variable name="nxtNcols">
         <xsl:choose>
            <xsl:when test="contains($importantColNums, concat('#',$colNum,'#'))">
               <xsl:copy-of select="$ncols"/>
            </xsl:when>
            <xsl:otherwise>
               <xsl:copy-of select="number($ncols)+1"/>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:if test="not(contains($importantColNums, concat('#',$colNum,'#')))">
         <xsl:call-template name="TDforColumn">
            <xsl:with-param name="tdpos" select="$colNum"/>
         </xsl:call-template>
      </xsl:if>

      <xsl:call-template name="restOfData">
         <xsl:with-param name="importantColNums" select="$importantColNums"/>
         <xsl:with-param name="maxCols" select="$maxCols"/>
         <xsl:with-param name="ncols" select="number($nxtNcols)"/>
         <xsl:with-param name="colNum" select="number($colNum)+1"/>
      </xsl:call-template>
   </xsl:if>

</xsl:template>




<!--
  -  format a description text, limiting its length
  -->

<xsl:template name="fmtDesc">
   <xsl:param name="text"/>
   <xsl:param name="width" select="60"/>
   <xsl:param name="pre" select="'      '"/>
   <xsl:param name="lim" select="210" />
   <xsl:param name="ident"/>
   <xsl:param name="showDescID"/>

   <xsl:variable name="cuttext">
      <xsl:choose>
         <xsl:when test="string-length($text) &gt; $lim and $ident != $showDescID">
            <xsl:variable name="cutsp">
               <xsl:call-template name="indexOfLast">
                 <xsl:with-param name="text" select="substring($text,1,$lim)"/>
               </xsl:call-template>
            </xsl:variable>
            <xsl:variable name="cut">
               <xsl:choose>
                  <xsl:when test="$cutsp > string-length($text)">
                     <xsl:value-of select="$lim"/>
                  </xsl:when>
                  <xsl:otherwise><xsl:value-of select="$cutsp"/></xsl:otherwise>
               </xsl:choose>      
            </xsl:variable>
            <xsl:value-of select="substring($text, 1, $cut)"/>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="$text"/></xsl:otherwise>
      </xsl:choose>
   </xsl:variable>

   <xsl:call-template name="fmttxt">
      <xsl:with-param name="text" select="$cuttext"/>
      <xsl:with-param name="pre" select="$pre"/>
      <xsl:with-param name="width" select="$width"/>
   </xsl:call-template>

   <xsl:if test="string-length($text) &gt; $lim and $ident != $showDescID">
      <xsl:text>... </xsl:text>
    <i>
        <span style='color:blue'>
          <a onclick="return {$showDescIDCallback}(this)" id="{$ident}">(more)</a>
        </span>
      </i>
   </xsl:if>
  <xsl:if test="string-length($text) &gt; $lim and $ident = $showDescID">
    <i>
      <span style='color:blue'>
        <a onclick="return {$hideDescIDCallback}()" id="{$ident}">(less)</a>
      </span>
    </i>
  </xsl:if>

</xsl:template>

  <!-- 
    -  format a long string into multiple lines
    -  @param text     the input string
    -  @param width    the maxmimum length of each line
    -  @param pre      an optional string to prepend to each line.
    -->

  <xsl:template name="fmttxt">
     <xsl:param name="text"/>
     <xsl:param name="width" select="60"/>
     <xsl:param name="pre"/>    
     <xsl:choose>
        <xsl:when test="string-length($text) &gt; $width">

           <!-- input is longer than one line.  First, lop off and print 
                the first line.  -->
           <xsl:variable name="cutpoint">
              <xsl:call-template name="indexOfLast">
                 <xsl:with-param name="text" 
                                 select="substring($text, 1, $width)"/>
              </xsl:call-template>
           </xsl:variable>

           <xsl:value-of select="$pre"/>
           <xsl:value-of select="substring($text, 1, number($cutpoint)-1)"/>
           <xsl:text> </xsl:text> <br /> <xsl:text> 
</xsl:text>


           <xsl:if test="number($cutpoint) &lt; string-length($text)">
           <!-- now recurse on the remaining text -->
              <xsl:call-template name="fmttxt">
                 <xsl:with-param name="text" 
                                 select="substring($text,number($cutpoint)+1)"/>
                 <xsl:with-param name="width" select="$width"/>
                 <xsl:with-param name="pre" select="$pre"/>
              </xsl:call-template>
           </xsl:if>
        </xsl:when>


        <xsl:otherwise>
           <!-- input line is less than max width, so just print it -->
           <xsl:value-of select="$pre"/>
           <xsl:value-of select="$text"/>
           <xsl:text>
</xsl:text>       
        </xsl:otherwise>
     </xsl:choose>
  </xsl:template>


  <!--
    -  return the index of the last occurance of a substring.  If the 
    -  pattern does not occur, the length of the string plus one is returned.
    -  @param text    the input string to search
    -  @param pat     the substring to search for
    -->
  <xsl:template name="indexOfLast">
     <xsl:param name="text"/>
     <xsl:param name="pat" select="' '"/>
     <xsl:param name="sum" select="0"/>
     <xsl:param name="patlen" select="0"/>

     <xsl:choose>
        <xsl:when test="contains($text,$pat)">
           <xsl:variable name="pre" select="substring-before($text, $pat)"/>
           <xsl:variable name="post" select="substring-after($text, $pat)"/>
           <xsl:variable name="newsum" 
                select="$sum + number($patlen) + string-length($pre)"/>
           <xsl:call-template name="indexOfLast">
              <xsl:with-param name="text" select="$post"/>
              <xsl:with-param name="pat" select="$pat"/>
              <xsl:with-param name="sum" select="$newsum"/>
              <xsl:with-param name="patlen" select="string-length($pat)"/>
           </xsl:call-template>
        </xsl:when>
        <xsl:when test="$sum=0">
           <xsl:value-of select="string-length($text)+1"/>
        </xsl:when>
        <xsl:otherwise>
           <xsl:value-of select="$sum+1"/>
        </xsl:otherwise>
     </xsl:choose>
  </xsl:template>



</xsl:stylesheet>
