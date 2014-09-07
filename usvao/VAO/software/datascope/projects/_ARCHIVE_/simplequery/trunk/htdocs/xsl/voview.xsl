<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
    xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" 
    xmlns:v1="http://vizier.u-strasbg.fr/VOTable"
    xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
    xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"
    xmlns:d="powers of 10"
    xsl:exclude-result-prefixes="vo v1 v2 v3"
>

<xsl:output method="html" />

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

<!-- Sort VOTable by column sortOrder and write a page of rows in of HTML -->

<!-- Input parameters -->

<xsl:param name="sortOrder">ascending</xsl:param>
<xsl:param name="sortColumn" />
<xsl:param name="selectedRows" />
<xsl:param name="selectRowUCD">ID_MAIN</xsl:param>
<xsl:param name="page">1</xsl:param>
<xsl:param name="pageLength">20</xsl:param>
<xsl:param name="maxColumns">11</xsl:param>

<xsl:param name="decPrecision">10</xsl:param>
<xsl:param name="raPrecision">100</xsl:param>
<xsl:param name="sexSeparator">:</xsl:param>

<xsl:param name="fullTable"  />

<!-- Filter parameters -->
<xsl:param name="filterText"></xsl:param>
<xsl:param name="filterTypes"></xsl:param>
<xsl:param name="filterCallBack">filterByColumn</xsl:param>
<xsl:param name="filterUpdCallback">updateFilter</xsl:param>

<!-- Javascript callback functions (also settable as parameters) -->

<xsl:param name="sortCallback">rd.sort</xsl:param>
<xsl:param name="showColumnCallback">rd.showColumns</xsl:param>
<xsl:param name="setPageLength">rd.setPageLength</xsl:param>
<xsl:param name="selectRowCallback">selectRow</xsl:param>
<xsl:param name="clearSelectionCallback">clearSelection</xsl:param>

<xsl:variable name="lc" select="'abcdefghijklmnopqrstuvwxyz'" />
<xsl:variable name="uc" select="'ABCDEFGHIJKLMNOPQRSTUVWXYZ'" />

<!-- Computed variables -->

<xsl:variable name="fieldlist" select="//FIELD|//vo:FIELD|//v1:FIELD|//v2:FIELD|//v3:FIELD"/>
<xsl:variable name="paramlist" select="//PARAM|//vo:PARAM|//v1:PARAM|//v2:PARAM|//v3:PARAM"/>

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

<xsl:variable name="urlColumnNum">
    <xsl:call-template name="getColumnByUCDs">
        <xsl:with-param name="value" select="'|VOX:Image_AccessReference|Data_link|'"/>
    </xsl:call-template>
</xsl:variable>

<xsl:variable name="raColumnNum">
    <xsl:call-template name="getColumnByUCDs">
        <xsl:with-param name="value" select="'|pos.eq.ra;meta.main|POS_EQ_RA_MAIN|'"/>
    </xsl:call-template>
</xsl:variable>

<xsl:variable name="decColumnNum">
    <xsl:call-template name="getColumnByUCDs">
        <xsl:with-param name="value" select="'|pos.eq.dec;meta.main|POS_EQ_DEC_MAIN|'"/>
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

<xsl:template name="getColumnByUCDs">
    <xsl:param name="value"/>
    <xsl:for-each select="$fieldlist">
        <xsl:if test="contains(translate($value, $lc, $uc), concat('|',translate(@ucd,$lc,$uc),'|'))">
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

<xsl:variable name="nrows" select="count(//TABLEDATA/TR|//vo:TABLEDATA/vo:TR|//v1:TABLEDATA/v1:TR|//v2:TABLEDATA/v2:TR|//v3:TABLEDATA/v3:TR)"/>
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

<xsl:template name="start"  match="/">
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
        <h2><xsl:value-of select="."/></h2>
    </xsl:if>
</xsl:template>

<xsl:template name="resource">
    <div>
        <xsl:choose>
        <xsl:when test="$nrows=0">
            <h2>No rows in VOTable</h2>
        </xsl:when>
        <xsl:otherwise>
            <xsl:for-each select="TABLE|vo:TABLE|v1:TABLE|v2:TABLE|v3:TABLE">
                <xsl:call-template name="buttons">
                    <xsl:with-param name="location" select="'top'"/>
                </xsl:call-template>
                <div class="searchnote">
                    Click column headings to sort - Uses boxes to filter
                    <span class="bbox" onclick="{$clearSelectionCallback}();">Reset</span>
                </div>
                <table class="data">
                    <thead>
                        <xsl:call-template name="header">
			   <xsl:with-param name="location" select="'top'" />
			</xsl:call-template>
                    </thead>
                    <tbody>
                    <xsl:for-each select="DATA/TABLEDATA|vo:DATA/vo:TABLEDATA|v1:DATA/v1:TABLEDATA|v2:DATA/v2:TABLEDATA|v3:DATA/v3:TABLEDATA">
                        <xsl:for-each select="TR|vo:TR|v1:TR|v2:TR|v3:TR">
                            <xsl:sort select="(TD|vo:TD|v1:TD|v2:TD|v3:TD)[position()=$sortColumnNum]" order="{$sortOrder}" data-type="{$datatype}"/>
                            <xsl:if test="not (position() &lt; $pageStart or position() &gt; $pageEnd)">
                                <xsl:variable name="selector" select="string((TD|vo:TD|v1:TD|v2:TD|v3:TD)[position()=$selectColumnNum])"/>
                                <tr onclick="{$selectRowCallback}(this,'{$selector}',event)">
				    <xsl:variable name="isSelected">
                                        <xsl:call-template name="isSelected">
                                            <xsl:with-param name="selector" select="$selector" />
                                        </xsl:call-template>
				    </xsl:variable>
                                    <xsl:attribute name="class">
				        <xsl:value-of select="$isSelected" />
                                        <xsl:choose>
                                            <xsl:when test="(position() mod 2) = 0">even</xsl:when>
                                            <xsl:otherwise>odd</xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:attribute>
                                    <xsl:call-template name="processRow">
                                        <xsl:with-param name="format" select="(TD|vo:TD|v1:TD|v2:TD|v3:TD)[position()=$formatColumnNum]" />
					<xsl:with-param name="selection" select="$isSelected" />
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
    <xsl:for-each select="TABLE|vo:TABLE|v1:TABLE|v2:TABLE|v3:TABLE">
        <table><tbody>
        <tr><td class="fieldparam">
        <h2>Columns</h2>
        <table class="fields">
            <thead>
	      <tr class="even">
                <th>Name</th>
	        <th colspan="4">Description</th>
            </tr>
	    <tr>
		<th>Constraint</th>
                <th>ID</th>
                <th>Unit</th>
                <th>Datatype</th>
                <th>UCD</th>
                <!--
                <th>precision</th>
                <th>width</th>
                <th>ref</th>
                <th>type</th>
                -->
	    </tr></thead>
            <tbody>
            <xsl:for-each select="$fieldlist"> 
	        <xsl:variable name="posit" select="position()" />
<!--                <tr onclick="{$showColumnCallback}({$posit})" title="Click to show only columns above this"> -->
                    <tr class="odd">
                    <td> <xsl:value-of select="@name"/> </td>
		    <td colspan="4">
		      <xsl:value-of select="DESCRIPTION" />
		    </td>
                </tr>
		<tr class="even">
		    <td>
		       <xsl:choose>
		           <xsl:when test="position() &gt; $maxColumns">
		               <xsl:call-template name="filterbox" />
			   </xsl:when>
			   <xsl:otherwise>
			       <span title="Use filter box on top of table">---</span>
			   </xsl:otherwise>
		       </xsl:choose>
		    </td>
                    <td> <xsl:value-of select="@ID"/> </td>
                    <td> <xsl:value-of select="@unit"/> </td>
                    <td> 
		    <xsl:value-of select="@datatype" /> 
		    <xsl:if test="@arraysize" >
                        <xsl:value-of select="'['"        />
		        <xsl:value-of select="@arraysize" />
                        <xsl:value-of select="']'"        />
		    </xsl:if></td>
                    <td> <xsl:value-of select="@ucd"/> </td>
                    <!--
                    <td> <xsl:value-of select="@precision"/> </td>
                    <td> <xsl:value-of select="@width"/> </td>
                    <td> <xsl:value-of select="@ref"/> </td>
                    <td> <xsl:value-of select="@type"/> </td>
                    -->
		</tr>
                <xsl:if test="$posit=$maxColumns and $maxColumns!=$ncols">
                    <tr class="separator"><td colspan="10" align="center">The fields below are hidden</td></tr>
                </xsl:if>
            </xsl:for-each> 
            </tbody>
        </table>
        </td>
        <td class="fieldparam">
            <xsl:if test="count($paramlist) &gt; 0">
                <h2>Table Parameters</h2>
                <table class="parameters">
                <thead><tr>
                    <th>Name</th>
		    <th>ID  </th>
                    <th>Value</th>
                    <th>Unit</th>
                </tr></thead>
                <tbody>
                <xsl:for-each select="$paramlist">
                    <tr>
                        <td> <xsl:value-of select="@name"/> </td>
			<td> <xsl:value-of select="@ID" />  </td>
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
    <div class="buttons {$location}">
        <div class="pagelabel">
	    <xsl:if test="$fullTable='no'">
	       Partial 
	    </xsl:if>
            Results <b><xsl:value-of select="$pageStart"/>-<xsl:value-of select="$pageEnd"/></b>
            <xsl:if test="$npages != 1">
                of <b><xsl:value-of select="$nrows"/>
	    <xsl:if test="$fullTable='no'">+</xsl:if></b>
            </xsl:if>
            <xsl:if test="$sortColumnNum != ''">
                (sorted by <xsl:value-of select="$sortColumn"/>)
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

<!-- prefix-* templates do nothing 
     but serve as place holders to be overriden -->
<xsl:template name="prefix-header" />>
<xsl:template name="prefix-column" />
<xsl:template name="prefix-filter" />

<!-- column headers come from VOTable FIELDS -->
<xsl:template name="header">
    <xsl:param name="location" />
    <xsl:variable name="ztop" select="'top'" />
    
    <tr>
        <xsl:call-template name="prefix-header">
	   <xsl:with-param name="location" select="$location" />
	</xsl:call-template>
        <xsl:variable name="sortname" select="translate($sortColumn,$lc,$uc)"/>
        <xsl:for-each select="$fieldlist"> 
            <xsl:if test="position() &lt;= $maxColumns">
                <xsl:variable name="ID"><xsl:call-template name="getID"/></xsl:variable>
                <xsl:variable name="name"><xsl:call-template name="getName"/></xsl:variable>
		<xsl:variable name="posit" select="position()" />
                <xsl:choose>
                    <xsl:when test="position() = $urlColumnNum">
                        <th class="unsortable"><xsl:value-of select="$name"/>
		        </th>
                    </xsl:when>
                    <xsl:otherwise>
                        <th onclick="{$sortCallback}('{$ID}')" title="Click to sort by {$name}">
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
	    <xsl:choose>
	        <xsl:when test="$ncols &lt; $maxColumns" >
                    <th onclick="{$showColumnCallback}({$ncols - 1})" title="Click to show fewer columns">&#171;</th>
		</xsl:when>
	        <xsl:otherwise>
                    <th onclick="{$showColumnCallback}({$maxColumns - 1})" title="Click to show fewer columns">&#171;</th>
		</xsl:otherwise>
	    </xsl:choose>
        </xsl:if>
        <xsl:if test="$ncols &gt; $maxColumns">
            <th onclick="{$showColumnCallback}({$maxColumns + 1})" title="Click to show more columns">&#187;</th>
        </xsl:if>
    </tr>
    <xsl:if test="$location=$ztop" >
    <tr>
    <xsl:call-template name="prefix-filter" />
    <xsl:for-each select="$fieldlist"> 
        <xsl:if test="position() &lt;= $maxColumns">
            <td>
                <xsl:call-template name="filterbox" />
            </td>
        </xsl:if>
    </xsl:for-each>
    </tr>
    </xsl:if>

</xsl:template>

<xsl:template name="filterbox">
    <xsl:variable name="posit" select="position()" />
    <xsl:variable name="par3">
       <xsl:choose>
           <xsl:when test="$posit=$raColumnNum">,15</xsl:when>
	   <xsl:when test="$posit=$decColumnNum">,1</xsl:when>
	   <xsl:otherwise></xsl:otherwise>
       </xsl:choose>
    </xsl:variable>
    
    <xsl:if test=" (@datatype='char' and not($posit=$urlColumnNum) )or not(@arraysize)  or @arraysize=1">
    
        <span>
            <xsl:attribute name="onmouseout">
	        <xsl:value-of select="$filterUpdCallback" />
	        <xsl:value-of select="'('"        />
	        <xsl:value-of select="$posit" />
	        <xsl:value-of select="','"        />
	        <xsl:value-of select="@datatype = 'char'" />
		<xsl:value-of select="$par3" />
	        <xsl:value-of select="')'"        />
	    </xsl:attribute>
            <input size="8" id="vovfilter{$posit}">
	        <xsl:attribute name="title">
	            <xsl:choose>
	                <xsl:when test="@datatype='char'" >
Character: Matches (including * wildcards)
		        </xsl:when>
		        <xsl:otherwise>
Numeric: E.g., 10 or &gt;=10 or 10..20 
		        </xsl:otherwise>
	           </xsl:choose>
	        </xsl:attribute>
                <xsl:attribute name="value">
	            <xsl:if test="contains($filterText, concat('|',$posit,':'))" >
	                <xsl:value-of select="substring-before(substring-after($filterText,concat('|',$posit,':')),'|')" />
	            </xsl:if>
	        </xsl:attribute>
	    </input>
       </span>
   </xsl:if>
</xsl:template>

<xsl:template name="processRow">
    <xsl:param name="format"/>
    <xsl:param name="selection"/>
    
    <xsl:call-template name="prefix-column">
	<xsl:with-param name="index" select="position()" />
	<xsl:with-param name="format" select="$format"   />
	<xsl:with-param name="isSelected" select="$selection"/>
    </xsl:call-template>

    <xsl:for-each select="TD|vo:TD|v1:TD|v2:TD|v3:TD">
        <xsl:if test="position() &lt;= $maxColumns">
            <xsl:choose>
                <xsl:when test="position() = $urlColumnNum">
                    <xsl:call-template name="processURL">
                        <xsl:with-param name="format" select="$format"/>
                    </xsl:call-template>
                </xsl:when>
		<xsl:when test="position() = $decColumnNum">
		    <xsl:call-template name="processSex">
		        <xsl:with-param name="precision" select="$decPrecision" />
			<xsl:with-param name="scale"     select="'1'" />
		    </xsl:call-template>
		</xsl:when>
		<xsl:when test="position() = $raColumnNum">
		    <xsl:call-template name="processSex">
		        <xsl:with-param name="precision" select="$raPrecision" />
			<xsl:with-param name="scale"     select="'15'" />
		    </xsl:call-template>
		</xsl:when>
                <xsl:otherwise>
                    <td>
                    <xsl:value-of select="."/>
                    </td>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:if>
    </xsl:for-each>
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

<xsl:template name="processSex">
    <xsl:param name="precision" >10</xsl:param>
    <xsl:param name="scale">1</xsl:param>
    <xsl:variable name="original">
        <xsl:call-template name="SciNum">
	    <xsl:with-param name="num" select='.' />
	</xsl:call-template>
    </xsl:variable>
    <xsl:choose>
        <xsl:when test = "string-length(normalize-space($original)) &gt; 0" >
	  <xsl:variable name="numb"    select="number($original)"  />
          <xsl:variable name="absnumb" 
	    select="round((1-2*($numb &lt; 0))*$numb*3600*$precision div $scale + 0.5)"   />

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
<!--
  Returns "selectedimage" if the selector is in the comma-delimited 
  list of selectedRows.
  Stupid Xpath 1.0 does not have the $*(#@ ends-with function, so have to
  check that by hand.
-->

<xsl:template name="isSelected">
    <xsl:param name="selector"/>
    <xsl:if test="$selectedRows">
        <xsl:choose>
            <xsl:when test="$selector = $selectedRows or contains($selectedRows,concat(',',$selector,',')) or starts-with($selectedRows,concat($selector,','))">selectedimage </xsl:when>
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
            <xsl:otherwise>selectedimage </xsl:otherwise>
        </xsl:choose>
    </xsl:if>
</xsl:template>

<xsl:template name="SciNum">
    <xsl:param name="num" />
        <!-- Get rid of +.  They cause problems is xslt1.0. -->
	<xsl:variable name="input" select="translate($num, 'E+', 'e')" />
	<xsl:choose>
	    <xsl:when test="contains($num, 'e')" >
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
