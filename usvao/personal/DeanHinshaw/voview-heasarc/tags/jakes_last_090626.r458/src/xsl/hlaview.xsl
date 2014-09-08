<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" 
	xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" 
	xmlns:v1="http://vizier.u-strasbg.fr/VOTable"
	xmlns:v2="http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd"
	xmlns:v3="http://www.ivoa.net/xml/VOTable/v1.0"
	exclude-result-prefixes="vo v1 v2 v3"
>

<xsl:import href="voview.xsl"/>

<!-- Input parameters -->

<xsl:param name="maxColumns">15</xsl:param>
<xsl:param name="displayCallback">display</xsl:param>

<!-- Computed variables -->

<xsl:variable name="zoom">
	<xsl:for-each select="$paramlist">
		<xsl:if test="translate(@name,$lc,$uc) = 'INPUT:ZOOM'">
			<xsl:value-of select="@value" />
		</xsl:if>
	</xsl:for-each>
</xsl:variable>

<xsl:variable name="proj">
	<xsl:for-each select="$paramlist">
		<xsl:if test="@ucd = 'VOX:WCS_CoordProjection'">
			<xsl:call-template name="url-encode">
				<xsl:with-param name="str" select="@value"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:for-each>
</xsl:variable>

<xsl:variable name="refframe">
	<xsl:for-each select="$paramlist">
		<xsl:if test="@ucd = 'VOX:STC_CoordRefFrame'">
			<xsl:call-template name="url-encode">
				<xsl:with-param name="str" select="@value"/>
			</xsl:call-template>
		</xsl:if>
	</xsl:for-each>
</xsl:variable>

<xsl:variable name="exptimeColumnNum">
	<xsl:call-template name="getColumnByUCD">
		<xsl:with-param name="value" select="'EXPOSURE_TIME'"/>
	</xsl:call-template>
</xsl:variable>

<xsl:variable name="naxisColumnNum">
	<xsl:call-template name="getColumnByUCD">
		<xsl:with-param name="value" select="'VOX:Image_Naxis'"/>
	</xsl:call-template>
</xsl:variable>

<xsl:variable name="crpixColumnNum">
	<xsl:call-template name="getColumnByUCD">
		<xsl:with-param name="value" select="'VOX:WCS_CoordRefPixel'"/>
	</xsl:call-template>
</xsl:variable>

<xsl:variable name="crvalColumnNum">
	<xsl:call-template name="getColumnByUCD">
		<xsl:with-param name="value" select="'VOX:WCS_CoordRefValue'"/>
	</xsl:call-template>
</xsl:variable>

<xsl:variable name="cdmatrixColumnNum">
	<xsl:call-template name="getColumnByUCD">
		<xsl:with-param name="value" select="'VOX:WCS_CDMatrix'"/>
	</xsl:call-template>
</xsl:variable>

<xsl:variable name="targetColumnNum">
	<xsl:call-template name="getColumnByUCD">
		<xsl:with-param name="value" select="'TARGET_NAME'"/>
	</xsl:call-template>
</xsl:variable>

<xsl:variable name="titleColumnNum">
	<xsl:call-template name="getColumnByUCD">
		<xsl:with-param name="value" select="'VOX:Image_Title'"/>
	</xsl:call-template>
</xsl:variable>

<xsl:variable name="detectorColumnNum">
	<xsl:call-template name="getColumnByUCD">
		<xsl:with-param name="value" select="'INST_ID'"/>
	</xsl:call-template>
</xsl:variable>

<xsl:variable name="apertureColumnNum">
	<xsl:call-template name="getColumnByUCD">
		<xsl:with-param name="value" select="'INST_APERT'"/>
	</xsl:call-template>
</xsl:variable>

<xsl:variable name="spectral_eltColumnNum">
	<xsl:call-template name="getColumnByUCD">
		<xsl:with-param name="value" select="'MAIN_FILTER'"/>
	</xsl:call-template>
</xsl:variable>

<xsl:variable name="filenameColumnNum">
	<xsl:call-template name="getColumnByName">
		<xsl:with-param name="value" select="'filename'"/>
	</xsl:call-template>
</xsl:variable>

<xsl:variable name="propidColumnNum">
	<xsl:call-template name="getColumnByName">
		<xsl:with-param name="value" select="'PropID'"/>
	</xsl:call-template>
</xsl:variable>

<!-- special column spacing info -->

<xsl:template name="columnSetting">
	<xsl:for-each select="$fieldlist"> 
		<xsl:if test="position() &lt;= $maxColumns">
			<xsl:choose>
				<xsl:when test="position() = $urlColumnNum">
					<col/><col/>
				</xsl:when>
				<xsl:when test="position() = $spectral_eltColumnNum">
					<col width="130"/>
				</xsl:when>
				<xsl:when test="position() = $titleColumnNum or position() = $filenameColumnNum">
					<col width="150"/>
				</xsl:when>
				<xsl:otherwise>
					<col/>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:for-each> 
	<xsl:if test="$ncols &gt; 1 and $maxColumns &gt; 1">
		<col/>
	</xsl:if>
	<xsl:if test="$ncols &gt; $maxColumns">
		<col/>
	</xsl:if>
</xsl:template>

<xsl:template name="columnheader">
	<xsl:param name="posit"/>
	<xsl:variable name="ID"><xsl:call-template name="getID"/></xsl:variable>
	<xsl:variable name="name"><xsl:call-template name="getName"/></xsl:variable>
	<xsl:choose>
		<xsl:when test="$posit = $urlColumnNum">
			<th class="unsortable" title="Start interactive display">Display</th>
			<th class="unsortable" title="Download image">Download</th>
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

<xsl:template name="filterbox">
	<xsl:param name="posit" />
		<xsl:choose>
			<xsl:when test="$posit = $urlColumnNum">
				<td></td>
				<td></td>
			</xsl:when>
			<xsl:when test="@datatype='char' or not(@arraysize)  or @arraysize=1">
				<td>
				<input type="hidden" name="vovfilter{$posit}_type" value="{@datatype = 'char'}" />
				<input type="text" name="vovfilter{$posit}">
					<xsl:attribute name="title">
						<xsl:choose>
							<xsl:when test="@datatype='char'">String: abc (exact match) or *ab*c* , ! to negate</xsl:when>
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
				</td>
			</xsl:when>
			<xsl:otherwise>
				<td></td>
			</xsl:otherwise>
		</xsl:choose>
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
		<xsl:when test="$posit = $spectral_eltColumnNum or $posit = $titleColumnNum">
			<td class="wrappable">
				<xsl:call-template name="MakeBreakable">
					<xsl:with-param name="value" select="."/>
					<xsl:with-param name="breakchar">/</xsl:with-param>
				</xsl:call-template>
			</td>
		</xsl:when>
		<xsl:when test="$posit = $filenameColumnNum">
			<td class="wrappable">
				<xsl:call-template name="MakeBreakable">
					<xsl:with-param name="value" select="."/>
					<xsl:with-param name="breakchar">,</xsl:with-param>
				</xsl:call-template>
			</td>
		</xsl:when>
		<xsl:when test="$posit = $exptimeColumnNum">
			<td>
			<xsl:if test=".!=''">
				<xsl:value-of select="format-number(.,'####0.##')"/>
			</xsl:if>
			</td>
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
		<xsl:when test="$posit = $propidColumnNum">
			<xsl:variable name="propid" select="normalize-space(.)" />
			<td>
			<xsl:if test="$propid != ''">
				<a href="http://archive.stsci.edu/cgi-bin/proposal_search?mission=hst&amp;id={$propid}" target="_blank">
					<xsl:value-of select="$propid"/>
				</a>
			</xsl:if>
			</td>
		</xsl:when>
		<xsl:otherwise>
			<td>
				<!-- Trim long columns (need to make this show full text if clicked)  -->
				<xsl:choose>
					<xsl:when test="substring(.,101)">
						<xsl:value-of select="concat(substring(.,1,97),'...')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="."/>
					</xsl:otherwise>
				</xsl:choose>
			</td>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

<xsl:template name="processURL">
	<xsl:param name="TDlist"/>
	<xsl:variable name="naxis" select="$TDlist[position()=$naxisColumnNum]"/>
	<xsl:variable name="format" select="$TDlist[position()=$formatColumnNum]"/>
	<xsl:variable name="filename">
		<xsl:call-template name="url-encode">
			<xsl:with-param name="str" select="$TDlist[position()=$filenameColumnNum]"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="stitle">
		<xsl:call-template name="url-encode">
			<xsl:with-param name="str" select="$TDlist[position()=$titleColumnNum]"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="starget">
		<xsl:call-template name="url-encode">
			<xsl:with-param name="str" select="$TDlist[position()=$targetColumnNum]"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="sdetector">
		<xsl:call-template name="url-encode">
			<xsl:with-param name="str" select="$TDlist[position()=$detectorColumnNum]"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="saperture">
		<xsl:call-template name="url-encode">
			<xsl:with-param name="str" select="$TDlist[position()=$apertureColumnNum]"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="scrpix">
		<xsl:call-template name="url-encode">
			<xsl:with-param name="str" select="$TDlist[position()=$crpixColumnNum]"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="scrval">
		<xsl:call-template name="url-encode">
			<xsl:with-param name="str" select="$TDlist[position()=$crvalColumnNum]"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="scdmatrix">
		<xsl:call-template name="url-encode">
			<xsl:with-param name="str" select="$TDlist[position()=$cdmatrixColumnNum]"/>
		</xsl:call-template>
	</xsl:variable>
	<xsl:variable name="href" select="normalize-space(.)"/>
	<xsl:variable name="fileref">
		<xsl:choose>
			<xsl:when test="$filename">
				<xsl:value-of select="$filename"/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="$href"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:variable>
			<xsl:variable name="sformat" select="translate(substring-after($format,'/'),$lc,$uc)"/>
	<xsl:choose>
		<xsl:when test="$sformat='DADS' or $sformat='TAR'">
			<td title="Show MAST preview"><a href="/cgi-bin/display?image={$filename}&amp;size={$naxis}&amp;izoom={$zoom}&amp;title={$stitle}%20{$starget}&amp;detector={$sdetector}&amp;aperture={$saperture}&amp;crpix={$scrpix}&amp;crval={$scrval}&amp;cdmatrix={$scdmatrix}&amp;proj={$proj}&amp;refframe={$refframe}" target="display" onclick="return {$displayCallback}(this.href, this.target)">MAST Preview</a></td>
		</xsl:when>
		<xsl:otherwise>
			<td title="Show interactive image display"><a href="/cgi-bin/display?image={$filename}&amp;size={$naxis}&amp;izoom={$zoom}&amp;title={$stitle}%20{$starget}&amp;detector={$sdetector}&amp;aperture={$saperture}&amp;crpix={$scrpix}&amp;crval={$scrval}&amp;cdmatrix={$scdmatrix}&amp;proj={$proj}&amp;refframe={$refframe}" target="display" onclick="return {$displayCallback}(this.href, this.target)">Display</a></td>
		</xsl:otherwise>
	</xsl:choose>

	<xsl:choose>
		<xsl:when test="$sformat='DADS'">
			<td title="Open DADS download page"><a href="{$href}" target="_blank" onclick="javascript: pageTracker._trackPageview('/download/{$filename}'); "><xsl:value-of select="$sformat"/></a></td>
		</xsl:when>
		<xsl:otherwise>
			<td title="Download {$sformat} file"><a href="{$href}" onClick="javascript: pageTracker._trackPageview('/download/{$filename}'); "><xsl:value-of select="$sformat"/></a></td>
		</xsl:otherwise>
	</xsl:choose>

</xsl:template>

<!-- don't show parameters table - use the space for instructions instead -->

<xsl:template name="paramstable">
<h2>Instructions</h2>
<table class="parameters">
<thead><tr><th></th></tr></thead>
<!-- tbody><tr class="odd"><td -->
<tbody><tr class="even"><td>
To change the order of the columns, drag rows in the table to the left.
To change which columns are shown, drag them above or below the gray
dividing bar, or drag the dividing bar itself.  Add or remove one column
at at time using the arrow links (&#171; &#187;) at the end of the column
headings in the table above.  See the
<a href="hla_help.html" target="_blank">help</a> and
<a href="hla_faq.html" target="_blank">FAQ</a>
for more information on sorting and filtering the inventory table.
</td></tr></tbody>
<thead><tr><th></th></tr></thead>
</table>
</xsl:template>

<!--
  Replace slashes in spectral_elt name with slash + <wbr></wbr>
  to make line-breaking work better in long spectral_elt names.
  This particular combination is supposed to work in most
  browsers (including Firefox, Safari, IE6).  The zero-length
  space that ought to work is less well supported.
-->

<xsl:template name="MakeBreakable">
	<xsl:param name="value"/>
	<xsl:param name="breakchar">/</xsl:param>
	<xsl:choose>
		<xsl:when test="contains($value,$breakchar)">
			<xsl:value-of select="concat(substring-before($value,$breakchar),$breakchar)"/><wbr></wbr>
			<xsl:call-template name="MakeBreakable">
				<xsl:with-param name="value" select="substring-after($value,$breakchar)"/>
				<xsl:with-param name="breakchar" select="$breakchar"/>
			</xsl:call-template>
		</xsl:when>
		<xsl:otherwise>
			<xsl:value-of select="$value"/>
		</xsl:otherwise>
	</xsl:choose>
</xsl:template>

</xsl:stylesheet>
