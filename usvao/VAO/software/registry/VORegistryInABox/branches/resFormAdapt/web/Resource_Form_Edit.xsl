<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" 
                xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0"
                xmlns:stc="http://www.ivoa.net/xml/STC/stc-v1.30.xsd"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:rbx="http://nvo.ncsa.uiuc.edu/xml/VORegInABox/v1.0"
                version="1.0">

   <xsl:output method="html" encoding="UTF-8" />

   <xsl:include href="Resource_Form_Site.xsl"/>

   <!-- 
     -  the desired output resource class.  In the case of non-standard 
     -  services, this may not be entirely known beyond 'vr:Service' until 
     -  the user enters, say, data coverage.  
     -->
   <xsl:variable name="xsitype">
      <xsl:choose>
         <xsl:when test="/*/@rbx:resource-type">
            <xsl:value-of select="/*/@rbx:resource-type"/>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="/*/@xsi:type"/></xsl:otherwise>
      </xsl:choose>
   </xsl:variable>

   <xsl:variable name="formOp">
      <xsl:choose>
         <xsl:when test="/*/@rbx:form-type = 'NewOrg'">
            <xsl:text>   Next   </xsl:text>
         </xsl:when>
         <xsl:when test="/*/@rbx:resource-type">
            <xsl:text>   Add Resource   </xsl:text>
         </xsl:when>
         <xsl:otherwise>
            <xsl:text> Submit Changes</xsl:text>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>

   <xsl:variable name="doc_dir">/vopub</xsl:variable> 

   <xsl:variable name="srcAuthid" select="normalize-space(substring-before(concat(substring-after(/*/identifier,'ivo://'),'/'),'/'))"/>

   <xsl:variable name="authid">
      <xsl:choose>
         <xsl:when test="/*/@rbx:resource-type='vg:Authority'"/>
         <xsl:otherwise>
            <xsl:value-of select="$srcAuthid"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:variable>

   <xsl:variable name="resourceKey" 
                 select="normalize-space(substring-after(substring-after(/*/identifier,'ivo://'),'/'))"/>

   <xsl:template match="/">
     <xsl:apply-templates select="." mode="site">
        <xsl:with-param name="title">Edit Resource Form</xsl:with-param>
     </xsl:apply-templates>
   </xsl:template>

   <xsl:template match="/" mode="appbody">
      <script type="text/javascript" src="/vopub/Resource_Form.js" ></script>
      <xsl:apply-templates select="ri:Resource" />
   </xsl:template>

   <xsl:template match="ri:Resource" xml:space="preserve">

      <xsl:if test="@rbx:form-type = 'NewOrg'">
<h1>Register a New Publisher </h1>

<xsl:if test="@rbx:tryout"><h2><font color="red"><em>***Test Registry - 
Data Will Not Be Published***</em></font></h2></xsl:if>
   <xsl:if test="@rbx:problems">
      <xsl:call-template name="probIntro">
         <xsl:with-param name="probs" select="concat(@rbx:problems,'#')"/>
      </xsl:call-template>
   </xsl:if>
<p>
Use this form to describe your organisation, which will serve as the 
publisher (or Naming Authority) for your resources. When this form is 
submitted, it will create two resource descriptions in your workspace: 
an "Organisation" and an "Authority." 
</p>

<p>
Fill in appropriate fields for this site and then click on the "Next" 
Button to continue. The "Reset" button will empty all the fields.
</p>

      </xsl:if>
      <xsl:if test="@rbx:form-type != 'NewOrg' and @rbx:resource-type">
<h1>New Resource Entry Form: <br />
<xsl:value-of select="@rbx:user-type"/></h1>

<xsl:if test="@rbx:tryout"><h2><font color="red"><em>***Test Registry - 
Data Will Not Be Published***</em></font></h2></xsl:if>

         <xsl:if test="@rbx:problems">
            <xsl:call-template name="probIntro">
               <xsl:with-param name="probs" select="concat(@rbx:problems,'#')"/>
            </xsl:call-template>
         </xsl:if>
         <xsl:if test="not(@rbx:problems)">
            <xsl:call-template name="newIntro"/>
         </xsl:if>
      </xsl:if>
      <xsl:if test="@rbx:form-type != 'NewOrg' and not(@rbx:resource-type)">
<h1>Edit Resource Form</h1>

<xsl:if test="@rbx:tryout"><h2><font color="red"><em>***Test Registry - 
Data Will Not Be Published***</em></font></h2></xsl:if>

         <xsl:if test="@rbx:problems">
            <xsl:call-template name="probIntro">
               <xsl:with-param name="probs" select="concat(@rbx:problems,'#')"/>
            </xsl:call-template>
         </xsl:if>
         <xsl:if test="not(@rbx:problems)">
            <xsl:call-template name="editIntro"/>
         </xsl:if>
      </xsl:if>

<p>
Not all inputs need to be filled in.  Any non-required information
that does not apply to your resource can be left blank.  The relative
importance of an input is given by the following labels:
</p>
<table border="0" cellspacing="8" cellpadding="4">
  <tr>

    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td>Required</td>
  </tr>
  <tr>
    <td bgcolor="#6ba5d7" valign="top"><font color="black">Rec.</font></td>
    <td>Recommended</td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td>Optional</td>
  </tr>
</table> 

<p>
To learn more about what an input item means, click on its highlighted 
name to access <a href="/vopub/formhelp.html#nvohelp" target="nvohelp">help</a> on 
that item.  
</p>

<form method="post" action="/cgi-bin/vopub/Resource_Form.cgi" enctype="multipart/form-data">
    <table border="0" width="100%" cellspacing="8" cellpadding="4">

    <xsl:if test="not(@rbx:resource-type)">
  <tr>
    <td colspan="3"><font size="5"><b>Resource Type: <xsl:value-of select="@rbx:user-type"/></b></font></td>
  </tr>

  <tr>
    <td colspan="3">
      <font size="5"><b>Identifier: <xsl:value-of select="identifier"/></b></font>
      <input type="hidden" name="authorityId" value="{$authid}" />
      <input type="hidden" name="resourceKey" value="{$resourceKey}" />
    </td>
  </tr>
       </xsl:if>
       <xsl:if test="@rbx:resource-type">
          <xsl:if test="@rbx:form-type='Authority' or @rbx:form-type='NewOrg'">
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#AuthorityID" target="nvohelp">Authority ID:</a>     
    </td>
    <td>
      <input type="text" name="authorityId" size="60"  value="{$authid}"/>

	<br />
	<font color="green"><em>
        A globally unique namespace that you will control; 
        <br /> e.g. "adil.ncsa" or "ncsa.adil" or "ncsa"
        </em></font> 
    </td>
  </tr>
          </xsl:if>
          <xsl:if test="@rbx:form-type!='Authority' and @rbx:form-type!='NewOrg'">
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#AuthorityID" target="nvohelp">Authority ID:</a>     
    </td>
    <td>
        <xsl:call-template name="selectRes">
           <xsl:with-param name="name" select="''"/>
           <xsl:with-param name="inputName">authorityId</xsl:with-param>
           <xsl:with-param name="label">Authority ID</xsl:with-param>
           <xsl:with-param name="select" select="$authid"/>
           <xsl:with-param name="resources" select="/*/@rbx:authids"/>
           <xsl:with-param name="asauths" select="true()"/>
        </xsl:call-template>
	<br />
	<font color="green" style="font-size: 10pt"><em>
      A globally unique namespace controlled by a single naming authority
    <br />

	  Select the previously registered Authority.</em></font>
    </td>
  </tr>
          </xsl:if>

          <xsl:if test="@rbx:form-type!='Authority'">
           <tr>
           <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
           <td valign="top" width="25%">
           <a href="/vopub/formhelp.html#ResourceKey"
               target="nvohelp">Resource Key:</a>     </td>
           <td>
           <input type="text" name="resourceKey" size="60"  value="{$resourceKey}"/>

	       <br />
	       <font color="green" style="font-size: 10pt"><em>
            A localized name for a resource that is unique within the namespace
            of the authority ID. 
           </em></font> 
           </td>
        </tr>
          </xsl:if>

     </xsl:if>
  <tr>
    <td colspan="3" bgcolor="#eeeeee">General Information:</td>
  </tr>
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#Title" target="nvohelp">Title:</a>   
    </td>

    <td>
      <input type="text" name="title" size="60"  value="{title}" /> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#ShortName" target="nvohelp">Short Name:</a>

    </td>
    <td>
      <input type="text" name="sname" size="16"  value="{shortName}" />
	<br />
	<font color="green" style="font-size: 10pt"><em>
	A short name (16 characters or less) that can be used to identify this resource in a <br />
        compact display of many resources
        </em></font> 
    </td>
  </tr>

  <xsl:apply-templates select="curation" />
  <xsl:apply-templates select="content" />

  <xsl:if test="$xsitype!='vr:Resource'  and $xsitype!='vg:Registry' and
                $xsitype!='vg:Authority' and $xsitype!='vr:Service'">
   <tr>
     <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
     <td valign="top" width="25%">
     <a href="/vopub/formhelp.html#Facility" target="nvohelp">Facility:</a>
     </td>

     <td>
        <xsl:call-template name="selectRes">
           <xsl:with-param name="name" select="facility[1]"/>
           <xsl:with-param name="inputName">facilityId</xsl:with-param>
           <xsl:with-param name="label">Facility</xsl:with-param>
           <xsl:with-param name="select" select="facility[1]/@ivo-id"/>
        </xsl:call-template>
 	<br />

 	<em><font color="green" style="font-size: 10pt">Select the previously registered facility, 
        <b> or fill in the </b></font> <b style="font-size: 10pt"> Facility's Name </b> 
        <font color="green" style="font-size: 10pt"><b>below:</b></font></em>  <br />
        <input type="text" name="facility" size="60"  value="{normalize-space(facility[1])}"/> 

     </td>
   </tr>

   <tr>
     <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
     <td valign="top" width="25%">
     <a href="/vopub/formhelp.html#Instrument" target="nvohelp">Instrument:</a>
     </td>

     <td>
        <xsl:call-template name="selectRes">
           <xsl:with-param name="name" select="instrument[1]"/>
           <xsl:with-param name="inputName">instrumentId</xsl:with-param>
           <xsl:with-param name="label">Instrument</xsl:with-param>
           <xsl:with-param name="select" select="instrument[1]/@ivo-id"/>
        </xsl:call-template>
 	<br />

 	<em><font color="green" style="font-size: 10pt">Select the previously registered instrument, 
        <b> or fill in the </b></font> <b style="font-size: 10pt"> Instrument's Name </b> 
        <font color="green" style="font-size: 10pt"><b>below:</b></font></em>  <br />
        <input type="text" name="instrument" size="60"  value="{normalize-space(instrument[1])}"/> 

     </td>
   </tr>

  </xsl:if>

   <tr>
     <td colspan="3" bgcolor="#eeeeee">Related Resource Information:</td>
   </tr>

   <xsl:apply-templates select="content" mode="relationship"/>

   <xsl:if test="$xsitype='vs:DataCollection'">
  <tr>
    <td colspan="3" bgcolor="#eeeeee">Resource Access Description:</td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#Formats" target="nvohelp">Formats:</a>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        one MIME format type per line; e.g. "image/fits"
        </em></font> 
    </td>
    <td>
        <textarea name="format" cols="30" rows="4">
           <xsl:for-each select="format">
              <xsl:value-of select="."/><xsl:text>
</xsl:text>
           </xsl:for-each>
        </textarea>
    </td>
  </tr>

  <tr>

    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#Rights" target="nvohelp">Rights:</a> 
        <br />
	<font color="green" style="font-size: 10pt"><em>
        select all that apply.
        </em></font> 
    </td>
    <td>
      <select name="rights" multiple="multiple"> 
         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">public</xsl:with-param>
            <xsl:with-param name="select" select="rights='public'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">secure</xsl:with-param>
            <xsl:with-param name="select" select="rights='secure'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">proprietary</xsl:with-param>
            <xsl:with-param name="select" select="rights='proprietary'"/>
         </xsl:call-template>
      </select>

    </td>
  </tr>

   </xsl:if> 

   <xsl:if test="$xsitype='vs:DataService' or $xsitype='vs:CatalogService' or 
                 @rbx:form-type='DataCollection'"> 

  <tr>
    <td colspan="3" bgcolor="#eeeeee">Data Coverage:</td>
  </tr>

      <xsl:apply-templates select="." mode="spatialCoverage"/>
      <xsl:apply-templates select="." mode="spectralCoverage"/>
      <xsl:apply-templates select="." mode="temporalCoverage"/>
   </xsl:if>

   <xsl:if test="$xsitype!='vs:DataService' and $xsitype!='vs:CatalogService' and
                 (@rbx:form-type='Service' or @rbx:form-type='CGIService' or
                  @rbx:form-type='WebService' or 
                  @rbx:form-type='BrowserBasedService')">
      <xsl:variable name="whodoes">
         <xsl:if test="@rbx:form-type='DataCollection'">collection includes</xsl:if>
         <xsl:if test="@rbx:form-type!='DataCollection'">service works with or produces</xsl:if>
      </xsl:variable>

      <tr>
          <td colspan="3" bgcolor="#eeeeee">Data Coverage:</td>
      </tr>
      <tr>
         <td bgcolor="#eeeeee" valign="top"><font color="white">Opt.</font></td>
         <td><input type="submit" name="ftype" value="Add Coverage"/></td>
         <td>Click Here if this <xsl:value-of select="$whodoes"/> data that 
             covers a particular area of the sky (including the whole sky) and/or
             a range in frequency or  time. </td>
      </tr>
   </xsl:if>

   <xsl:if test="contains(@rbx:form-type,'Service') or 
                 @rbx:form-type='ConeSearch' or  @rbx:form-type='SkyNode'">
      <xsl:apply-templates select="." mode="capability"/>
   </xsl:if>
</table>

        <input type="hidden" name="inputfname" value="{@rbx:src}" />
	<input type="hidden" name="srcAuthorityID" value="{$srcAuthid}" />
	<input type="hidden" name="srcResourceKey" value="{$resourceKey}" />
	<input type="hidden" name="defset" value="{@rbx:form-type}" />
	<input type="hidden" name="uname" value="rplante" />
   <xsl:if test="$xsitype='vg:Authority'">
	<input type="hidden" name="resourceKey" value="" />
	<input type="hidden" name="type" value="Other" />
	<input type="hidden" name="contentLevel" value="General" />
	<input type="hidden" name="subject" value="virtual observatory" />
	<input type="hidden" name="contributor" value="" />
	<input type="hidden" name="version" value="" />
	<input type="hidden" name="source" value="" />
   </xsl:if>
   <xsl:if test="@rbx:tryout!=''">
        <input type="hidden" name="tryout" value="{@rbx:tryout}"/>
   </xsl:if>
        <input type="hidden" name="target-ftype" value="{@rbx:target-op}"/>
	<input type="submit" name="ftype" value="{$formOp}" />
	<input type="reset" value="Reset Form" />
	<input type="submit" name="ftype" value="Cancel" />

</form>

   </xsl:template>

   <xsl:template name="editIntro" xml:space="preserve">
<p>
This page enables you to edit a resource description that has already
been registered.  Change the inputs below as necessary and then hit the 
<strong>Submit Changes</strong> button.  Click the <strong>Reset</strong> 
button to return to the original values. Use your Browser's  <!-- ' -->
<strong>Back</strong> button to cancel the edit.
</p>
   </xsl:template>

   <xsl:template name="newIntro" xml:space="preserve">
<p>
Use this page to <strong>add</strong> a new resource description.  The 
default values are those of one of your previously registered resources.  
Change only those values that are different; then click the 
<strong>Add Resource</strong> button at the bottom of the form.  Click 
the <strong>Reset</strong> button to return to the default values. Use your
Browser's <strong>Back</strong> button to cancel the add.
</p> <!-- ' -->
   </xsl:template>

   <xsl:template name="probIntro">
      <xsl:param name="probs">#</xsl:param>

<h3>Problems Were Encountered</h3>
<p>
The following problems kept us from saving your changes:
</p>

<font color="#d15d5d"><strong>
<ul>
      <xsl:call-template name="listprobs">
         <xsl:with-param name="problems" select="concat($probs,'#')"/>
      </xsl:call-template>
</ul>
</strong></font>

<p>
Please fix these problems and re-submit.
</p>
   </xsl:template>

   <xsl:template name="listprobs">
      <xsl:param name="problems">#</xsl:param>

      <xsl:variable name="nxt" select="substring-before($problems,'#')"/>
      <xsl:variable name="rest" select="substring-after($problems,'#')"/>

      <xsl:if test="normalize-space($nxt) != ''">
  <li> <xsl:value-of select="$nxt"/> </li>
      </xsl:if>

      <xsl:if test="normalize-space($nxt) != ''">
         <xsl:call-template name="listprobs">
            <xsl:with-param name="problems" select="$rest"/>
         </xsl:call-template>
      </xsl:if>

   </xsl:template>

   <xsl:template name="selectRes">
      <xsl:param name="inputName"/>
      <xsl:param name="label">??</xsl:param>
      <xsl:param name="name"/>
      <xsl:param name="select"/>
      <xsl:param name="resources" select="/*/@rbx:resources"/>
      <xsl:param name="asauths" select="false()"/>

      <xsl:variable name="options">
         <xsl:call-template name="resourceOptions">
            <xsl:with-param name="opts">
               <xsl:value-of select="normalize-space($resources)"/>
               <xsl:text>#</xsl:text>
            </xsl:with-param>
            <xsl:with-param name="match" select="$select"/>
            <xsl:with-param name="asauths" select="$asauths"/>
         </xsl:call-template>
      </xsl:variable>

      <select name="{$inputName}">
         <xsl:choose>
            <xsl:when test="contains($options,'selected')">
                <option value="">Select a registered <xsl:value-of select="$label"/></option>
                <option value="">[None]</option>
            </xsl:when>
            <xsl:when test="$select != ''">
                <option value="">Select a registered <xsl:value-of select="$label"/></option>
                <option value="">[None]</option>
                <option value="{$select}" selected="selected"><xsl:value-of select="$select"/></option>
            </xsl:when>
            <xsl:when test="$name!=''">
                <option value="">Select a registered <xsl:value-of select="$label"/></option>
                <option value="" selected="selected">[None]</option>
            </xsl:when>
            <xsl:otherwise>
                <option value="" selected="selected">Select a registered <xsl:value-of select="$label"/></option>
                <option value="">[None]</option>
            </xsl:otherwise>
         </xsl:choose>
         <xsl:copy-of select="$options"/>
      </select>

   </xsl:template>

   <xsl:template name="resourceOptions">
      <xsl:param name="opts"/>
      <xsl:param name="match"/>
      <xsl:param name="asauths" select="false()"/>

      <xsl:variable name="option" 
                    select="normalize-space(substring-before($opts,'#'))"/>
      <xsl:variable name="rest" 
                    select="normalize-space(substring-after($opts,'#'))"/>

      <xsl:if test="$option != ''">
         <xsl:variable name="id">
            <xsl:choose>
               <xsl:when test="$asauths">
                  <xsl:value-of 
                       select="normalize-space(substring-before($option,'('))"/>
               </xsl:when>
               <xsl:otherwise>
                  <xsl:value-of select="substring-before(substring-after($option,'('),')')"/>
               </xsl:otherwise>
            </xsl:choose>
         </xsl:variable>

         <xsl:choose>
            <xsl:when test="$match = $id">
              <option value="{$id}" selected="selected"><xsl:value-of select="$option"/></option>
            </xsl:when>
            <xsl:otherwise>
              <option value="{$id}"><xsl:value-of select="$option"/></option>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>

      <xsl:if test="$rest != ''">
         <xsl:call-template name="resourceOptions">
            <xsl:with-param name="opts" select="$rest"/>
            <xsl:with-param name="match" select="$match"/>
            <xsl:with-param name="asauths" select="$asauths"/>
         </xsl:call-template>
      </xsl:if>
   </xsl:template>

   <xsl:template match="curation">

  <tr>
    <td colspan="3" bgcolor="#eeeeee">Curation Information:</td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
       <a href="/vopub/formhelp.html#Publisher" 
          target="nvohelp">Publisher</a>   
    </td>

    <td>
        <xsl:call-template name="selectRes">
           <xsl:with-param name="name" select="publisher[1]"/>
           <xsl:with-param name="inputName">publisherId</xsl:with-param>
           <xsl:with-param name="label">Publisher</xsl:with-param>
           <xsl:with-param name="select" select="publisher[1]/@ivo-id"/>
           <xsl:with-param name="resources" select="/*/@rbx:publishers"/>
        </xsl:call-template>
	<br />
	<em><font color="green" style="font-size: 10pt">Select the previously registered
        publisher, 
        <b>or fill in the </b></font> <b style="font-size: 10pt">Publisher's Title </b> 

        <font color="green" style="font-size: 10pt"> <b>below:</b></font></em>  <br />
        <input type="text" name="pub_title" size="60"  value="{normalize-space(publisher)}" /> 
    </td>
  </tr>

  <tr>
    <td colspan="2" bgcolor="#eeeeee">Creator Information:</td>
  </tr>

  <xsl:choose>
     <xsl:when test="creator">
        <xsl:for-each select="creator">
           <xsl:call-template name="makeCreator">
              <xsl:with-param name="name" select="name"/>
           </xsl:call-template>
        </xsl:for-each>
     </xsl:when>
     <xsl:otherwise>
        <xsl:call-template name="makeCreator"/>
     </xsl:otherwise>
  </xsl:choose>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#Date" target="nvohelp">Creation Date:</a>
    </td>
    <td>
        <input type="text" name="date" size="20"  value="{date[1]}" />
	<br />

	<font color="green" style="font-size: 10pt"><em>
        (Example: 1984, 1990-07, 2001-04-25)
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#CreatorLogo" target="nvohelp">URL to Creator Logo:</a>
    </td>
    <td>
        <input type="text" name="logo" size="60"  value="{creator[1]/logo}" />
    </td>
  </tr>

  <tr>
    <td colspan="2" bgcolor="#eeeeee">                  </td>
  </tr>

  <xsl:if test="$xsitype != 'vg:Authority'">
    <xsl:choose>
       <xsl:when test="contributor">
          <xsl:for-each select="contributor">
             <xsl:call-template name="makeContributor">
                <xsl:with-param name="name" select="."/>
             </xsl:call-template>
          </xsl:for-each>
       </xsl:when>
       <xsl:otherwise>
          <xsl:call-template name="makeContributor"/>
       </xsl:otherwise>
    </xsl:choose>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#Version" target="nvohelp">Version:</a>
    </td>
    <td>
        <input type="text" name="version" size="60"  value="{version}" />
    </td>
  </tr>
  </xsl:if>

  <tr>
    <td colspan="2" bgcolor="#eeeeee">Contact Information:</td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>

    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#ContactName" target="nvohelp">Contact Name:</a>     
    </td>
    <td>
        <input type="text" name="contact" size="60"  value="{contact/name}" />
    </td>
  </tr>

  <tr>
    <td bgcolor="#6ba5d7" valign="top"><font color="black">Rec.</font></td>

    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#ContactEmail" target="nvohelp">Contact Email:</a>     
    </td>
    <td>
        <input type="text" name="contactEmail" size="60"  value="{contact/email}"/>
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>

    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#ContactAddress" target="nvohelp">Contact Address:</a>     
    </td>
    <td>
        <textarea name="contactAddress" cols="60" rows="3"><xsl:value-of select="contact/address"/></textarea>
    </td>
  </tr>

  <tr>

    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#ContactTelephone" target="nvohelp">Contact Telephone:</a>     
    </td>
    <td>
        <input type="text" name="contactTelephone" size="60"  value="{contact/telephone}"/>
    </td>
   </tr>

   

   </xsl:template>

   <xsl:template name="makeContributor">
      <xsl:param name="name"/>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#Contributor" target="nvohelp">Contributor:</a>
    </td>
    <td>
        <input type="text" name="contributor" size="60"  value="{$name}" />
    </td>

  </tr>
   </xsl:template>

   <xsl:template name="makeCreator">
      <xsl:param name="name"/>
  <tr>

    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#CreatorName" target="nvohelp">Creator Name:</a>     
    </td>
    <td>
        <input type="text" name="creator" size="60"  value="{$name}" />
    </td>
  </tr>
   </xsl:template>

   <xsl:template match="publisher"  mode="ivo-id">
        <select name="publisherId">
          <xsl:choose>
             <xsl:when test="contains(/*/@rbx:publishers,' selected')">
                <option value="">Select a registered Publisher</option>
                <option value="">[None]</option>
             </xsl:when>
             <xsl:when test="@ivo-id != ''">
                <option value="">Select a registered Publisher</option>
                <option value="">[None]</option>
                <option value="{@ivo-id}" selected="selected">
                   <xsl:value-of select="@ivo-id"/></option>
             </xsl:when>
             <xsl:when test="/*/@rbx:publishers = ''">
                <option value="">Select a registered Publisher</option>
                <option value="" selected="selected">[None]</option>
             </xsl:when>
             <xsl:otherwise>
                <option value="" selected="selected">Select a registered Publisher</option>
                <option value="">[None]</option>
             </xsl:otherwise>
          </xsl:choose>
          <xsl:call-template name="publisherOptions">
             <xsl:with-param name="opts">
                <xsl:value-of select="normalize-space(/*/@rbx:publishers)"/>
                <xsl:text>#</xsl:text>
             </xsl:with-param>
             <xsl:with-param name="match" select="@ivo-id"/>
          </xsl:call-template>
        </select>
   </xsl:template>

   <xsl:template name="publisherOptions">
      <xsl:param name="opts"/>
      <xsl:param name="match"/>

      <xsl:variable name="option" 
                    select="normalize-space(substring-before($opts,'#'))"/>
      <xsl:variable name="rest" 
                    select="normalize-space(substring-after($opts,'#'))"/>

      <xsl:variable name="pub">
         <xsl:choose>
            <xsl:when test="contains($option, ' selected')">
               <xsl:value-of select="substring-before($option,' selected')"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="$option"/></xsl:otherwise>
         </xsl:choose>
      </xsl:variable>

      <xsl:if test="$option != ''">
         <xsl:variable name="id" 
              select="substring-before(substring-after($option,'('),')')" />
         <xsl:choose>
            <xsl:when test="contains($option, ' selected') or $match = $id">
              <option value="{$pub}" selected="selected"><xsl:value-of select="$pub"/></option>
            </xsl:when>
            <xsl:otherwise>
              <option value="{$pub}"><xsl:value-of select="$pub"/></option>
            </xsl:otherwise>
         </xsl:choose>
      </xsl:if>

      <xsl:if test="$rest != ''">
         <xsl:call-template name="publisherOptions">
            <xsl:with-param name="opts" select="$rest"/>
            <xsl:with-param name="match" select="$match"/>
         </xsl:call-template>
      </xsl:if>
   </xsl:template>

   <!--
     -  render the content part of form
     -->
   <xsl:template match="content">

  <tr>
    <td colspan="3" bgcolor="#eeeeee">Content Information:</td>
  </tr>

  <xsl:if test="$xsitype != 'vg:Authority'">
  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#Subject" target="nvohelp">Subject:</a> 
        <br />

	<font color="green" style="font-size: 10pt"><em>
        one subject per line.
        </em></font> 
    </td>
    <td>
        <textarea name="subject" cols="60" rows="4">
           <xsl:for-each select="subject">
              <xsl:value-of select="."/><xsl:text>
</xsl:text>
           </xsl:for-each>
        </textarea>
    </td>
  </tr>
  </xsl:if>

 <tr>

    <xsl:variable name="desctext">
       <xsl:if test="$xsitype='vg:Authority' and /*/@rbx:resource-type">
          <xsl:text>This Naming Authority is used to identify resources from
</xsl:text> <xsl:value-of select="normalize-space(../curation/publisher)"/> 
          <xsl:text>....</xsl:text>
       </xsl:if>
       <xsl:if test="$xsitype!='vg:Authority' or not(/*/@rbx:resource-type)">
          <xsl:value-of select="normalize-space(description)"/>
       </xsl:if>
    </xsl:variable>

    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#Description" target="nvohelp">Description:</a>     
    </td>
    <td>
        <textarea name="description" cols="60" rows="6"><xsl:value-of select="$desctext"/></textarea>
    </td>
  </tr>

  <xsl:if test="$xsitype!='vg:Authority'">
  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#Source" target="nvohelp">Source:</a>
    </td>
    <td>
        <input type="text" name="source" size="60"  value="{source}" />
	<br />

	<font color="green" style="font-size: 10pt"><em>
        an ADS bibcode for the article that this resource is derived from.
        </em></font> 
    </td>
  </tr>
  </xsl:if>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#ReferenceURL" target="nvohelp">Reference URL:</a>     
    </td>

    <td>
        <input type="text" name="refURL" size="60"  value="{referenceURL}"/>
	<br />
	<font color="green" style="font-size: 10pt"><em>
	A URL to a human-readable document giving more information
        about this resource.
        </em></font> 
    </td>
  </tr>

  <xsl:if test="$xsitype != 'vg:Authority'">
  <tr>

    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#Type" target="nvohelp">Type:</a>     
        <br />
	<font color="green" style="font-size: 10pt"><em>
        select all that apply.
        </em></font> 
    </td>
    <td>
      <xsl:apply-templates select="." mode="typeOpts"/>
    </td>
  </tr>

  <tr>
    <td bgcolor="#6ba5d7" valign="top"><font color="black">Rec.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#ContentLevel" target="nvohelp">Content Level:</a>     
        <br />
	<font color="green" style="font-size: 10pt"><em>

        select all that apply.
        </em></font> 
    </td>
    <td>
      <xsl:apply-templates select="." mode="contentLevelOpts"/>
    </td>
  </tr>
     </xsl:if>
   </xsl:template>

   <!--
     -  render input for related resource
     -->
   <xsl:template match="content" mode="relationship">

   <tr>
     <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>

     <td valign="top" width="25%">
 <a href="/vopub/formhelp.html#Relationship" target="nvohelp">Relationship:</a>     
         <br />
 	<font color="green" style="font-size: 10pt"><em>
         The relationship of this resource to another resource.
         </em></font> 
     </td>
     <td>
       <select name="relation" size="4">
         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">mirror-of</xsl:with-param>
            <xsl:with-param name="select" select="relationship[1]/relationshipType='mirror-of'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">service-for</xsl:with-param>
            <xsl:with-param name="select" select="relationship[1]/relationshipType='service-for'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">derived-from</xsl:with-param>
            <xsl:with-param name="select" select="relationship[1]/relationshipType='derived-from'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">related-to</xsl:with-param>
            <xsl:with-param name="select" select="relationship[1]/relationshipType='related-to'"/>
         </xsl:call-template>

       </select>

     </td>
   </tr>
 
   <tr>

     <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
     <td valign="top" width="25%">
 <a href="/vopub/formhelp.html#RelatedResourceID" target="nvohelp">Related Resource:</a>     
     </td>
     <td>

        <xsl:call-template name="selectRes">
           <xsl:with-param name="name" select="relationship[1]/relatedResource"/>
           <xsl:with-param name="inputName">relatedResourceId</xsl:with-param>
           <xsl:with-param name="label">Resource</xsl:with-param>
           <xsl:with-param name="select" select="relationship[1]/relatedResource/@ivo-id"/>
        </xsl:call-template>
      	<br />

        <em><font color="green" style="font-size: 10pt">Select the previously registered resource, 
        <b>or fill in the </b></font> <b style="font-size: 10pt">Resource's Name </b> <!-- ' -->
        <font color="green" style="font-size: 10pt"><b>below:</b></font></em>  <br />
        <input type="text" name="rel_title" size="60"  value="{relationship[1]/relatedResource}"/> 

     </td>
   </tr>
   
   </xsl:template>

   <xsl:template match="content" mode="contentLevelOpts">
      <select name="contentLevel" size="7" multiple="multiple">

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">General</xsl:with-param>
            <xsl:with-param name="select" select="type='General'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Elementary Education</xsl:with-param>
            <xsl:with-param name="select" select="contentLevel='Elementary Education'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Middle School Education</xsl:with-param>
            <xsl:with-param name="select" select="contentLevel='Middle School Education'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Secondary Education</xsl:with-param>
            <xsl:with-param name="select" select="contentLevel='Secondary Education'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Community College</xsl:with-param>
            <xsl:with-param name="select" select="contentLevel='Community College'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">University</xsl:with-param>
            <xsl:with-param name="select" select="contentLevel='University'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Research</xsl:with-param>
            <xsl:with-param name="select" select="contentLevel='Research'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Amateur</xsl:with-param>
            <xsl:with-param name="select" select="contentLevel='Amateur'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Informal Education</xsl:with-param>
            <xsl:with-param name="select" select="contentLevel='Informal Education'"/>
         </xsl:call-template>
      </select>

   </xsl:template>

   <xsl:template match="content" mode="typeOpts">

      <select name="type" size="7" multiple="multiple"><xsl:text>
</xsl:text>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Other</xsl:with-param>
            <xsl:with-param name="select" select="type='Other'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Organisation</xsl:with-param>
            <xsl:with-param name="select" 
                 select="type='Organisation' or /*/@rbx:form-type='NewOrg'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Archive</xsl:with-param>
            <xsl:with-param name="select" select="type='Archive'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Catalog</xsl:with-param>
            <xsl:with-param name="select" select="type='Catalog'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Survey</xsl:with-param>
            <xsl:with-param name="select" select="type='Survey'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Simulation</xsl:with-param>
            <xsl:with-param name="select" select="type='Simulation'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Bibliography</xsl:with-param>
            <xsl:with-param name="select" select="type='Bibliography'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Journal</xsl:with-param>
            <xsl:with-param name="select" select="type='Journal'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Library</xsl:with-param>
            <xsl:with-param name="select" select="type='Library'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Transformation</xsl:with-param>
            <xsl:with-param name="select" select="type='Transformation'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Education</xsl:with-param>
            <xsl:with-param name="select" select="type='Education'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Outreach</xsl:with-param>
            <xsl:with-param name="select" select="type='Outreach'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">EPOResource</xsl:with-param>
            <xsl:with-param name="select" select="type='EPOResource'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Animation</xsl:with-param>
            <xsl:with-param name="select" select="type='Animation'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Artwork</xsl:with-param>
            <xsl:with-param name="select" select="type='Artwork'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Background</xsl:with-param>
            <xsl:with-param name="select" select="type='Background'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">BasicData</xsl:with-param>
            <xsl:with-param name="select" select="type='BasicData'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Historical</xsl:with-param>
            <xsl:with-param name="select" select="type='Historical'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Photographic</xsl:with-param>
            <xsl:with-param name="select" select="type='Photographic'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Press</xsl:with-param>
            <xsl:with-param name="select" select="type='Press'"/>
         </xsl:call-template>

      </select>

   </xsl:template>

   <xsl:template name="selectOpt">
      <xsl:param name="opt">??</xsl:param>
      <xsl:param name="select"/>

         <option value="{$opt}">
           <xsl:if test="$select">
             <xsl:attribute name="selected">1</xsl:attribute>
           </xsl:if>
           <xsl:value-of select="$opt"/>
         </option><xsl:text>
</xsl:text>
      
   </xsl:template>

   <xsl:template match="ri:Resource" mode="spatialCoverage">
      <xsl:variable name="csys" 
      select="coverage/stc:STCResourceProfile/stc:AstroCoordSystem/@xlink:href"/>

  <tr>
    <td colspan="2" bgcolor="#eeeeee">Spatial Information</td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>

    <td valign="top" width="25%">
       <a href="/vopub/formhelp.html#SpatialCoverage" 
          target="nvohelp">Spatial Coverage:</a> </td>
    <td> 
	<font color="green" style="font-size: 10pt"><em>
        Select one of the options for Spatial Coverage and fill in
        relevant fields. </em></font> 

        <dl>
           <dt> <input type="radio" name="region" value="AllSky">
                   <xsl:if test="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:AllSky">
                     <xsl:attribute name="checked">checked</xsl:attribute>
                   </xsl:if>
                </input>
                <xsl:text>All Sky</xsl:text>
                <br />
	        <font color="green" style="font-size: 10pt"><em>
                Coverage is the entire sky.
                </em></font> <p/> </dt>

           <dt> <input type="radio" name="region" value="CircleRegion">
                   <xsl:if test="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:Circle">
                     <xsl:attribute name="checked">checked</xsl:attribute>
                   </xsl:if>
                </input>
                <xsl:text>Circle Region</xsl:text>
                <br />
	        <font color="green" style="font-size: 10pt"><em>
                  A central position and angular radius.
                </em></font> </dt>

           <dd> 
             <a href="/vopub/formhelp.html#CoordinateFrame" 
                target="nvohelp">Coordinate Frame:</a>

             <select name="circleRegionFrame">
                 <xsl:text>   </xsl:text>
                 <xsl:call-template name="selectOpt">
                    <xsl:with-param name="opt">ICRS</xsl:with-param>
                    <xsl:with-param name="select" 
                                    select="contains($csys, '#UTC-ICRS-TOPO')"/>
                 </xsl:call-template>

                 <xsl:text>   </xsl:text>
                 <xsl:call-template name="selectOpt">
                    <xsl:with-param name="opt">FK4</xsl:with-param>
                    <xsl:with-param name="select" 
                                    select="contains($csys, '#UTC-FK4-TOPO')"/>
                 </xsl:call-template>

                 <xsl:text>   </xsl:text>
                 <xsl:call-template name="selectOpt">
                    <xsl:with-param name="opt">FK5</xsl:with-param>
                    <xsl:with-param name="select" 
                                    select="contains($csys, '#UTC-FK5-TOPO')"/>
                 </xsl:call-template>

                 <xsl:text>   </xsl:text>
                 <xsl:call-template name="selectOpt">
                    <xsl:with-param name="opt">Galactic (II)</xsl:with-param>
                    <xsl:with-param name="select" 
                                    select="contains($csys, '#UTC-GALII-TOPO')"/>
                 </xsl:call-template>
             </select>  <br />

             Longitude:  
             <input type="text" name="region_long" size="15"  value="{coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:Circle/stc:Center/stc:C1}" />
             <span style="visibility: hidden">XXXXX</span> Latitude: 
             <input type="text" name="region_lat" size="15" value="{coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:Circle/stc:Center/stc:C2}" />

             <br />
             
             Radius (degrees):
             <input type="text" name="region_rad" size="15"  value="{coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:Circle/stc:Radius}" />
             <p /></dd>

           <dt> <input type="radio" name="region" value="CoordRange" >
                   <xsl:if test="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:Position2VecInterval/stc:LoLimit2Vec">
                     <xsl:attribute name="checked">checked</xsl:attribute>
                   </xsl:if>
                </input>
                <xsl:text>Coordinate Range</xsl:text>
                <br />
	        <font color="green" style="font-size: 10pt"><em>
                  A range of longitude and latitude (in degrees). 
                </em></font>

                <br />  </dt>

           <dd>
             <a href="/vopub/formhelp.html#CoordinateFrame" 
                target="nvohelp">Coordinate Frame:</a>
             <select name="rangeRegionFrame">
                 <xsl:text>   </xsl:text>
                 <xsl:call-template name="selectOpt">
                    <xsl:with-param name="opt">ICRS</xsl:with-param>
                    <xsl:with-param name="select" 
                                    select="contains($csys, '#UTC-ICRS-TOPO')"/>
                 </xsl:call-template>

                 <xsl:text>   </xsl:text>
                 <xsl:call-template name="selectOpt">
                    <xsl:with-param name="opt">FK4</xsl:with-param>
                    <xsl:with-param name="select" 
                                    select="contains($csys, '#UTC-FK4-TOPO')"/>
                 </xsl:call-template>

                 <xsl:text>   </xsl:text>
                 <xsl:call-template name="selectOpt">
                    <xsl:with-param name="opt">FK5</xsl:with-param>
                    <xsl:with-param name="select" 
                                    select="contains($csys, '#UTC-FK5-TOPO')"/>
                 </xsl:call-template>

                 <xsl:text>   </xsl:text>
                 <xsl:call-template name="selectOpt">
                    <xsl:with-param name="opt">Galactic (II)</xsl:with-param>
                    <xsl:with-param name="select" 
                                    select="contains($csys, '#UTC-GALII-TOPO')"/>
                 </xsl:call-template>
             </select>  <br />


      Longitude:   Min:  
      <input type="text" name="range_long_min" size="15"  value="{coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:Position2VecInterval/stc:LoLimit2Vec/stc:C1}"/>
      <span style="visibility: hidden">XXXXX</span> Max: 
      <input type="text" name="range_long_max" size="15"  value="{coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:Position2VecInterval/stc:HiLimit2Vec/stc:C1}"/>
      <br />

      Latitude:   Min:  
      <input type="text" name="range_lat_min" size="15"  value="{coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:Position2VecInterval/stc:LoLimit2Vec/stc:C2}"/>
      <span style="visibility: hidden">XXXXX</span> Max: 
      <input type="text" name="range_lat_max" size="15"  value="{coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:Position2VecInterval/stc:HiLimit2Vec/stc:C2}"/>
      </dd>
        </dl>
    </td>
    </tr>

    <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top">
<a href="/vopub/formhelp.html#SpatialResolution" target="nvohelp">Spatial Resolution:</a></td>
    <td valign="top">

      <input type="text" name="spatial_res" size="15"  value="{normalize-space(coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Position1D/stc:Resolution)}"/>
        <span style="visibility: hidden">XX</span>
	<font color="green" style="font-size: 10pt"><em>
        The spatial (angular) resolution that is typical of the observations
of interest, in decimal degrees.
        </em></font> 
    </td>
  </tr>
    <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>

    <td valign="top">
<a href="/vopub/formhelp.html#RegionofRegard" target="nvohelp">Region of Regard:</a></td>
    <td valign="top">
      <input type="text" name="region_regard" size="15"  value="{normalize-space(coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Position1D/stc:Size)}" />
        <span style="visibility: hidden">XX</span>
	<font color="green" style="font-size: 10pt"><em>
        The intrinsic size scale, given in arcseconds, associated with data
items contained in a resource.
        </em></font> 
    </td>
  </tr>
      
   </xsl:template>

   <xsl:template match="ri:Resource" mode="spectralCoverage">
  <tr>
    <td colspan="2" bgcolor="#eeeeee">Spectral Information</td>
  </tr>

  <tr>
    <td bgcolor="#6ba5d7" valign="top"><font color="black">Rec.</font></td>
    <td valign="top" width="25%">
       <a href="/vopub/formhelp.html#Waveband" 
          target="nvohelp">Waveband:</a>
        <br />
	<font color="green" style="font-size: 10pt"><em>
        select all that apply.
        </em></font> 
    </td>
    <td>
        <select name="waveband" size="5" multiple="multiple">

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">Radio</xsl:with-param>
              <xsl:with-param name="select" select="coverage/waveband='Radio'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">Millimeter</xsl:with-param>
              <xsl:with-param name="select" select="coverage/waveband='Millimeter'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">Infrared</xsl:with-param>
              <xsl:with-param name="select" select="coverage/waveband='Infrared'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">Optical</xsl:with-param>
              <xsl:with-param name="select" select="coverage/waveband='Optical'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">UV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/waveband='UV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">EUV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/waveband='EUV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">X-ray</xsl:with-param>
              <xsl:with-param name="select" select="coverage/waveband='X-ray'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">Gamma-ray</xsl:with-param>
              <xsl:with-param name="select" select="coverage/waveband='Gamma-ray'"/>
           </xsl:call-template>
        </select>

    </td>
  </tr>
  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td>
       <a href="/vopub/formhelp.html#WavelengthRange" 
          target="nvohelp">Wavelength Range:</a>
    </td>
    <td>

        Min:  <input type="text" name="wave_min" size="25"  value="{coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/stc:LoLimit}"/>
        <select name="wave_min_units" size="1" >
           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">GeV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='GeV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">MeV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='MeV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">keV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='keV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">eV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='eV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">Angstroms</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='Angstroms'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">microns</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='microns'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">mm</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='mm'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">meters</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='m' or not(coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval)"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">Hz</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='Hz'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">MHz</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='MHz'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">GHz</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='GHz'"/>
           </xsl:call-template>
        </select>

        <br />
        Max: <input type="text" name="wave_max" size="25"  value="{coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/stc:HiLimit}"/>
        <select name="wave_max_units" size="1" >
           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">GeV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='GeV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">MeV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='MeV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">keV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='keV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">eV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='eV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">Angstroms</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='Angstroms'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">microns</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='microns'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">mm</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='mm'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">meters</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='m' or not(coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval)"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">Hz</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='Hz'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">MHz</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='MHz'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">GHz</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:SpectralInterval/@unit='GHz'"/>
           </xsl:call-template>
        </select>

    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td colspan="2">
<a href="/vopub/formhelp.html#SpectralResolution" target="nvohelp">Spectral Resolution:</a>
      <input type="text" name="spec_res" size="15"  value="{coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/stc:Resolution}"/>
        <select name="spec_res_units" size="1" >
           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">GeV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/@unit='GeV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">MeV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/@unit='MeV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">keV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/@unit='keV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">eV</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/@unit='eV'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">Angstroms</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/@unit='Angstroms'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">microns</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/@unit='microns'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">mm</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/@unit='mm'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">meters</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/@unit='m' or not(coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral)"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">Hz</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/@unit='Hz'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">MHz</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/@unit='MHz'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">GHz</xsl:with-param>
              <xsl:with-param name="select" select="coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Spectral/@unit='GHz'"/>
           </xsl:call-template>
        </select>
    </td>

  </tr>
      
   </xsl:template>

   <xsl:template match="ri:Resource" mode="temporalCoverage">

  <tr>
    <td colspan="2" bgcolor="#eeeeee">Time Information</td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">

       <a href="/vopub/formhelp.html#Temporal" 
          target="nvohelp">Temporal Coverage:</a>
    </td>
    <td>
      Start:  
      <input type="text" name="temporal_start" size="15"  value="{substring-before(coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:TimeInterval/stc:StartTime/stc:ISOTime,'T')}"/>
      <span style="visibility: hidden">XXXXX</span> End: 
      <input type="text" name="temporal_end" size="15"    value="{substring-before(coverage/stc:STCResourceProfile/stc:AstroCoordArea[1]/stc:TimeInterval/stc:StopTime/stc:ISOTime,'T')}"/>
        <br />

	<font color="green" style="font-size: 10pt"><em>
        (Example: 1984, 1990-07, 2001-04-25) 
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td colspan="2">
<a href="/vopub/formhelp.html#TemporalResolution" target="nvohelp">Temporal Resolution (seconds):</a>

      <input type="text" name="temporal_res" size="15"  value="{coverage/stc:STCResourceProfile/stc:AstroCoords[1]/stc:Time/stc:Resolution}"/>
    </td>
  </tr>

   </xsl:template>

   <xsl:template match="ri:Resource" mode="capability">
      <xsl:variable name="type" select="@rbx:form-type"/>

  <tr>
    <td colspan="3" bgcolor="#eeeeee">Service Capabilities and Interface:</td>
  </tr>

      <xsl:choose>
         <xsl:when test="$type='SIAService'">
            <xsl:apply-templates select="." mode="sia"/>
         </xsl:when>
         <xsl:when test="$type='SSAService'">
            <xsl:apply-templates select="." mode="ssa"/>
         </xsl:when>
         <xsl:when test="$type='ConeSearch'">
            <xsl:apply-templates select="." mode="scs"/>
         </xsl:when>
         <xsl:when test="$type='SkyNode'">
            <xsl:apply-templates select="." mode="sn"/>
         </xsl:when>
         <xsl:when test="$type='CGIService'">
            <xsl:apply-templates select="." mode="cgi"/>
         </xsl:when>
         <xsl:when test="$type='WebService'">
            <xsl:apply-templates select="." mode="soap"/>
         </xsl:when>
         <xsl:when test="$type='BrowserBasedService'">
            <xsl:apply-templates select="." mode="genservice"/>
         </xsl:when>
         <xsl:otherwise>
            <xsl:apply-templates select="." mode="genservice"/>
         </xsl:otherwise>
      </xsl:choose>      
   </xsl:template>

   <xsl:template match="ri:Resource" mode="sia">

      <xsl:apply-templates select="." mode="baseurl">
         <xsl:with-param name="label">SIA image</xsl:with-param>
      </xsl:apply-templates>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#ImageServiceType" target="nvohelp">Image Service Type:</a>     
    </td>
    <td>
      <select name="imServType" size="1" >
         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">cutout</xsl:with-param>
            <xsl:with-param name="select" select="capability/imageServiceType='cutout'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">mosaic</xsl:with-param>
            <xsl:with-param name="select" select="capability/imageServiceType='mosaic'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">atlas</xsl:with-param>
            <xsl:with-param name="select" select="capability/imageServiceType='atlas'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">pointed</xsl:with-param>
            <xsl:with-param name="select" select="capability/imageServiceType='pointed'"/>
         </xsl:call-template>
      </select>
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#MaxRegionSize" target="nvohelp">Maximum Query Region Size:</a>
    </td>
    <td>
        Longitude:  
        <input type="text" name="maxRegSize_long" size="15" value="{capability/maxQueryRegionSize/long}"/>
        <span style="visibility: hidden">XXXXX</span>  Latitude:  
        <input type="text" name="maxRegSize_lat" size="15" value="{capability/maxQueryRegionSize/lat}"/>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        Largest queryable region in decimal degrees; enter "360.0" and "180.0"
        if there is no limit.  
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#MaxImExt" target="nvohelp">Maximum Image Extent:</a>
    </td>
    <td>
        Longitude: 
        <input type="text" name="maxImExt_long" size="20" value="{capability/maxImageExtent/long}"/>
        <span style="visibility: hidden">XXXXX</span>  Latitude:  
        <input type="text" name="maxImExt_lat" size="20" value="{capability/maxImageExtent/lat}"/>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        Largest image that can be returned in decimal degrees; enter "360.0" 
        and "180.0" if there is no limit.
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#MaxImSize" target="nvohelp">Maximum Image Size:</a>     
    </td>
    <td>
        Longitude: 
        <input type="text" name="maxImSize_long" size="20" value="{capability/maxImageSize/long}"/>
        <span style="visibility: hidden">XXXXX</span>  Latitude:  
        <input type="text" name="maxImSize_lat" size="20" value="{capability/maxImageSize/lat}"/>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        Size of largest image that can be returned in integer pixels
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#MaxFSize" target="nvohelp">Maximum File Size:</a>     
    </td>
    <td>
        <input type="text" name="maxFSize" size="20"  value="{capability/maxFileSize}"/>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        Filesize of largest image that can be returned in bytes
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#MaxRec" target="nvohelp">Limit on Number of Returned Records:</a>
    </td>
    <td>
        <input type="text" name="maxRec" size="20"  value="{capability/maxRecords}"/>
    </td>
  </tr>

   </xsl:template>

   <xsl:template match="ri:Resource" mode="baseurl">
      <xsl:param name="label">service</xsl:param>
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#BaseURL" target="nvohelp">Base URL:</a>
    </td>
    <td>
        <input type="text" name="ifaceURL" size="60"  value="{normalize-space(capability[1]/interface[1]/accessURL[1])}"/>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        The base URL to use for <xsl:value-of select="$label"/> queries.
        </em></font> 
    </td>
  </tr>
   </xsl:template>

   <xsl:template match="ri:Resource" mode="ssa">
      <xsl:apply-templates select="." mode="baseurl">
         <xsl:with-param name="label">SSA spectra</xsl:with-param>
      </xsl:apply-templates>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#SSAversion" target="nvohelp">SSA Version:</a>
    </td>
    <td>
        <font color="green" style="font-size: 10pt"><em>Indicate which version of the SSA standard 
        your service is compliant with: </em></font> <br />
        <input type="radio" name="ssaVersion" value="1.0">
           <xsl:if test="capability/@xsi:type='ssa:SimpleSpectralAccess'">
              <xsl:attribute name="checked">checked</xsl:attribute>
           </xsl:if>
        </input> version 1.0
        <span style="visibility: hidden">XXXXX</span>  
        <input type="radio" name="ssaVersion" value="pre1.0">
           <xsl:if test="capability/@xsi:type='ssa:ProtoSpectralAccess'">
              <xsl:attribute name="checked">checked</xsl:attribute>
           </xsl:if>
        </input> pre-version 1.0
    </td>        
  </tr>
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#complianceLevel" target="nvohelp">Compliance Level:</a>     
    </td>
    <td>
      <select name="complianceLevel" size="1" >
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">full</xsl:with-param>
            <xsl:with-param name="select" select="capability/complianceLevel='full'"/>
         </xsl:call-template>

         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">minimal</xsl:with-param>
            <xsl:with-param name="select" select="capability/complianceLevel='minimal'"/>
         </xsl:call-template>

         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">query</xsl:with-param>
            <xsl:with-param name="select" select="capability/complianceLevel='query'"/>
         </xsl:call-template>
      </select> <br/>
      <font color="green" style="font-size: 10pt"><strong>full</strong><em>=supports all features;</em> 
      <span style="visibility: hidden">XX</span> 
      <strong>minimal</strong><em>=supports only required features;</em> <br /> 
      <strong>query</strong><em>=does not support compliant return formats</em>
      </font>
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#dataSource" target="nvohelp">Type of source data:</a>     
        <br />
	<font color="green" style="font-size: 10pt"><em>
        select all that apply.  
        </em></font> 
    </td>
    <td>
        <select name="dataSource" size="5" multiple="multiple">
           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">survey</xsl:with-param>
              <xsl:with-param name="select" select="capability/dataSource='survey'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">pointed</xsl:with-param>
              <xsl:with-param name="select" select="capability/dataSource='pointed'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">custom</xsl:with-param>
              <xsl:with-param name="select" select="capability/dataSource='custom'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">theory</xsl:with-param>
              <xsl:with-param name="select" select="capability/dataSource='theory'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">artificial</xsl:with-param>
              <xsl:with-param name="select" select="capability/dataSource='artificial'"/>
           </xsl:call-template>
        </select>      
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
        <a href="{$doc_dir}/formhelp.html#creationType" target="nvohelp">Method for creating spectra:</a>     
        <br />
	<font color="green" style="font-size: 10pt"><em>
        select all that apply.  
        </em></font> 
    </td>
    <td>
        <select name="creationType" size="6" multiple="multiple">
           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">archival</xsl:with-param>
              <xsl:with-param name="select" select="capability/creationType='archival'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">cutout</xsl:with-param>
              <xsl:with-param name="select" select="capability/creationType='cutout'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">filtered</xsl:with-param>
              <xsl:with-param name="select" select="capability/creationType='filtered'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">mosaic</xsl:with-param>
              <xsl:with-param name="select" select="capability/creationType='mosaic'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">projection</xsl:with-param>
              <xsl:with-param name="select" select="capability/creationType='projection'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">spectralExtraction</xsl:with-param>
              <xsl:with-param name="select" select="capability/creationType='spectralExtraction'"/>
           </xsl:call-template>
        </select>      
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#MaxSR" target="nvohelp">Maximum Search Radius:</a>
    </td>
    <td>
        <input type="text" name="maxSR" size="20"  value="{capability/maxSearchRadius}"/>
        <br />
	<font color="green" style="font-size: 10pt"><em>
	Largest search radius, in degrees. 
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#MaxRec" target="nvohelp">Maximum Number of Records Returned:</a>
    </td>

    <td>
        Hard Limit: <span style="visibility: hidden">XXXX</span>
        <input type="text" name="maxRec" size="20" value="{capability/maxRecords}"/>
        <br/>
        Default Limit<font color="green" style="font-size: 10pt">*</font><span style="visibility: hidden">XX</span>
        <input type="text" name="defMaxRec" size="20" value="{capability/defaultMaxRecords}"/>
        <span style="visibility: hidden">XX</span>
        <font color="green" style="font-size: 10pt">*when not specified by user</font>
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#MaxAper" target="nvohelp">Maximum Aperature Size:</a>
    </td>
    <td>
        <input type="text" name="maxAperture" size="20"  value="{capability/maxAperture}"/>
        <br />
	<font color="green" style="font-size: 10pt"><em>
	Largest aperature diameter radius supported in query, given in degrees. 
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#suppFrame" target="nvohelp">Supported Coordinate Frame:</a>
    </td>
    <td>
        <input type="text" name="supportedFrame" size="20"  value="{capability/supportedFrame}"/>
        <br />
	<font color="green" style="font-size: 10pt"><em>
	(e.g. </em><strong>ICRS</strong>, <strong>GALACTIC</strong>;
        <em> see <a href="/vopub/formhelp.html#suppFrame" 
        target="nvohelp">help</a> for full list of recognized frames.
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#MaxFile" target="nvohelp">Maximum File Size:</a>
    </td>
    <td>
        <input type="text" name="maxFileSize" size="20"  value="{capability/maxFileSize}"/>
        <br />
	<font color="green" style="font-size: 10pt"><em>
	Largest output dataset file size in kilobytes.
        </em></font> 
    </td>
  </tr>

   </xsl:template>

   <xsl:template match="ri:Resource" mode="scs">

      <xsl:apply-templates select="." mode="baseurl">
         <xsl:with-param name="label">Cone Search</xsl:with-param>
      </xsl:apply-templates>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#MaxSR" target="nvohelp">Maximum Search Radius:</a>
    </td>
    <td>
        <input type="text" name="maxSR" size="20"  value="{capability/maxSR}"/>
        <br />
	<font color="green" style="font-size: 10pt"><em>
	Largest search radius, in degrees. 
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#MaxRec" target="nvohelp">Maximum Number of Records Returned:</a>
    </td>

    <td>
        <input type="text" name="maxRec" size="20" value="{capability/maxRecords}"/>
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#Verbosity" target="nvohelp">Verbosity:</a>
    </td>
    <td colspan="3">
       <input type="checkbox" name="verbosity" value="Verbosity">
          <xsl:if test="capability/verbosity='true'">
             <xsl:attribute name="checked">checked</xsl:attribute>
          </xsl:if>
       </input>
       <br />
       <font color="green" style="font-size: 10pt"><em>
          Click here if the service supports the VERB keyword
       </em></font> 
    </td>
  </tr>
   
   </xsl:template>

   <xsl:template match="ri:Resource" mode="cgi">
      <xsl:apply-templates select="." mode="baseurl">
         <xsl:with-param name="label">Service</xsl:with-param>
      </xsl:apply-templates>

      <tr>
         <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
         <td valign="top" width="25%">
<a href="/vopub/formhelp.html#QueryType" target="nvohelp">Query Type:</a>
         </td>
         <td colspan="3" align="left" valign="top">
           <table border="0" width="75%" cellpadding="1" cellspacing="1">
              <tr>
                <td colspan="3"><font color="green" style="font-size: 10pt"><em>
                Which kind of CGI query does this service accept?
                </em></font></td>
              </tr>
              <tr>
                 <td><input type="checkbox" value="GET" name="queryType">
                        <xsl:if test="not(/*/@rbx:resource-type) and 
                                      capability/interface/queryType!='POST'">
                           <xsl:attribute name="checked">checked</xsl:attribute>
                        </xsl:if>
                     </input> GET </td>
                 <td><input type="checkbox" value="POST" name="queryType">
                        <xsl:if test="not(/*/@rbx:resource-type) and 
                                      capability/interface/queryType!='GET'">
                           <xsl:attribute name="checked">checked</xsl:attribute>
                        </xsl:if>
                     </input> POST </td>
              </tr>
           </table>
         </td>
      </tr>

      <tr>
         <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
         <td valign="top">Output Type:</td>
         <td colspan="3"> 
            <table noborder="noborder" width="100%" cellpadding="1" cellspacing="8">
              <tr>
                <td>
                   <input type="radio" name="output" 
                          value="application/xml+votable">
                      <xsl:if test="capability/interface/resultType = 
                                    'application/xml+votable'">
                         <xsl:attribute name="checked">checked</xsl:attribute>
                      </xsl:if>
                   </input>VOTable 
                </td>
                <td> 
                   <input type="radio" name="output" value="application/csv">
                      <xsl:if test="capability/interface/resultType = 
                                    'application/csv'">
                         <xsl:attribute name="checked">checked</xsl:attribute>
                      </xsl:if>
                   </input>CSV Table 
                </td>
                <td> 
                   <input type="radio" name="output" value="text/html">
                      <xsl:if test="capability/interface/resultType = 
                                    'text/html'">
                         <xsl:attribute name="checked">checked</xsl:attribute>
                      </xsl:if>
                   </input>HTML Table 
                </td>
              </tr>
              <tr>
                 <td> 
                    <input type="radio" name="output" value="Other">
                      <xsl:if test="capability/interface/resultType != 
                                    'application/csv' and 
                                    capability/interface/resultType != 
                                    'application/xml+votable' and 
                                    capability/interface/resultType != 
                                    'text/html' and 
                                    capability/interface/resultType != ''">
                         <xsl:attribute name="checked">checked</xsl:attribute>
                      </xsl:if>
                   </input>Other:
                 </td>
              </tr>
              <tr>
                 <td valign="top" width="25%">
                   <a href="/vopub/formhelp.html#OutputMime" target="nvohelp">Output Mime Type:</a>
                 </td>
                 <td colspan="2">
                   <input type="text" name="outputMime" size="60"  
                          value="{capability/interface/resultType}"/>
                 </td>
              </tr>
              <tr>
                <td></td>
                <td align="left" width="25%" colspan="2">
                   <input type="checkbox" name="catalogservice" value="yes">
                      <xsl:if test="$xsitype='vs:CatalogService'">
                         <xsl:attribute name="checked">checked</xsl:attribute>
                      </xsl:if>
                   </input>
                   Is this tabular output?
                </td>
              </tr>
           </table>
         </td>
      </tr>

      <tr>
         <td colspan="2" bgcolor="#eeeeee">Service Parameters:</td>
      </tr>

      <xsl:apply-templates select="." mode="ServiceParams">
         <xsl:with-param name="numParam">
            <xsl:choose>
               <xsl:when test="number(@rbx:numParam) > 
                                          count(capability/interface/param)">
                  <xsl:value-of select="number(@rbx:numParam)"/>
               </xsl:when>
               <xsl:when test="count(capability/interface/param) > 0">
                  <xsl:value-of select="count(capability/interface/param)"/>
               </xsl:when>
               <xsl:otherwise><xsl:value-of select="3"/></xsl:otherwise>
            </xsl:choose>
         </xsl:with-param>
      </xsl:apply-templates>

   </xsl:template>

   <xsl:template match="ri:Resource" mode="ServiceParams">
      <xsl:param name="numParam" select="3"/>

      <xsl:apply-templates select="." mode="Param">
         <xsl:with-param name="numParam" select="$numParam"/>
      </xsl:apply-templates>

   <tr>
      <td></td>
      <td><input type="text" name="addparam" size="5" /></td>
      <td>If you need to add more parameters, type in the number to add.
          <br />Then press the Add Param Button.</td>
   </tr>
   <tr>
      <td/>
      <td><input type="hidden" name="numParam" value="{$numParam}" />
          <input type="submit" name="ftype" value="Add Param" />
      </td>
   </tr>

   </xsl:template>

   <xsl:template match="ri:Resource" mode="Param">
      <xsl:param name="numParam" select="3"/>
      <xsl:param name="i" select="1"/>

      <xsl:if test="$numParam > 0">

  <tr>
    <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
    <td valign="top" width="75%">
       <a href="/vopub/formhelp.html#Parameter" 
          target="nvohelp">Parameter:</a>
    </td>
  </tr>
  <tr>
    <td></td>
    <td colspan="2"> 
        <table cellpadding="0" border="0">

        <tr>
        <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
        <td>Name:  <input type="text" name="param_name" size="20" value="{capability/interface/param[$i]/name}"/></td>
        <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
        <td>Data Type:
        <select name="param_dataType" size="1" >

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">string</xsl:with-param>
              <xsl:with-param name="select" select="capability/interface/param[$i]/dataType='string'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">boolean</xsl:with-param>
              <xsl:with-param name="select" select="capability/interface/param[$i]/dataType='boolean'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">integer</xsl:with-param>
              <xsl:with-param name="select" select="capability/interface/param[$i]/dataType='integer'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">char</xsl:with-param>
              <xsl:with-param name="select" select="capability/interface/param[$i]/dataType='char'"/>
           </xsl:call-template>

           <xsl:text>   </xsl:text>
           <xsl:call-template name="selectOpt">
              <xsl:with-param name="opt">real</xsl:with-param>
              <xsl:with-param name="select" select="capability/interface/param[$i]/dataType='real'"/>
           </xsl:call-template>
        </select>
        </td>
        </tr>
        <tr>
        <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
        <td>
        Unit:<input type="text" name="param_unit" size="10" value = "{capability/interface/param[$i]/unit}"/>
        </td>
        <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>

        <td>
          <a href="http://vizier.u-strasbg.fr/UCD/old/ucds_from_kw/"
             target="nvohelp">UCD:</a>
          <input type="text" name="param_ucd" size="10" value="{capability/interface/param[$i]/ucd}"/>
        </td>
        </tr>
        <tr>
        <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
        <td colspan="4">Description: <input type="text" name="param_desc" size="60" value="{capability/interface/param[$i]/description}"/></td>

        </tr>
        <tr>
        <td bgcolor="#eeeeee" valign="top"><font color="black">Opt.</font></td>
        <td>
         <input type="checkbox" name="param_req" value="{$i}">
            <xsl:if test="capability/interface/param[$i]/@use='required'">
               <xsl:attribute name="checked">checked</xsl:attribute>
            </xsl:if>
         </input>Required Parameter?
        </td>
        </tr>
        </table>
    </td>

  </tr>

         <xsl:if test="$numParam > 1">
            <xsl:apply-templates select="." mode="Param">
               <xsl:with-param name="numParam" select="$numParam - 1"/>
               <xsl:with-param name="i" select="$i + 1"/>
            </xsl:apply-templates>
         </xsl:if>
      </xsl:if>
   </xsl:template>

   <xsl:template match="ri:Resource" mode="soap">
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#ServiceURL" target="nvohelp">Service URL:</a>
    </td>
    <td>
        <input type="text" name="ifaceURL" size="60"  value="{normalize-space(capability[1]/interface[1]/accessURL[1])}"/>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        The URL endpoint for the SOAP-based service
        </em></font> 
    </td>
  </tr>
   </xsl:template>

   <xsl:template match="ri:Resource" mode="genservice">
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#ServiceURL" target="nvohelp">Access URL:</a>
    </td>
    <td>
        <input type="text" name="ifaceURL" size="60"  value="{normalize-space(capability[1]/interface[1]/accessURL[1])}"/>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        The URL of the web page hosting the service
        </em></font> 
    </td>
  </tr>
   </xsl:template>

   <xsl:template match="ri:Resource" mode="sn">
  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#ServiceURL" target="nvohelp">Service URL:</a>
    </td>
    <td>
        <input type="text" name="ifaceURL" size="60"  value="{normalize-space(capability[1]/interface[1]/accessURL[1])}"/>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        The URL endpoint for the SOAP-based Sky Node service
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#SNCompliance" target="nvohelp">Compliance Level:</a>     
    </td>
    <td>
      <select name="snCompliance" size="1" >
         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Full</xsl:with-param>
            <xsl:with-param name="select" select="capability/compliance='Full'"/>
         </xsl:call-template>

         <xsl:text>   </xsl:text>
         <xsl:call-template name="selectOpt">
            <xsl:with-param name="opt">Basic</xsl:with-param>
            <xsl:with-param name="select" select="capability/compliance='Basic'"/>
         </xsl:call-template>
      </select>
      <span style="visibility: hidden">XX</span> 
      <font color="green" style="font-size: 10pt"><strong>Basic</strong><em> supports all features
      except upload/cross-correlation;</em> <br/>
      <strong>Full</strong><em> supports all features.</em>
      </font>
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#ServerLoc" target="nvohelp">Server Location:</a>
    </td>
    <td>
        Longitude:  
        <input type="text" name="loc_long" size="15" value="{capability/longitude}"/>
        <span style="visibility: hidden">XXXXX</span>  Latitude:  
        <input type="text" name="loc_lat" size="15" value="{capability/latitude}"/>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        Geographical location of your server, in degrees
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="/vopub/formhelp.html#MaxRec" target="nvohelp">Maximum Number of Records Returned:</a>
    </td>

    <td>
        <input type="text" name="maxRec" size="20" value="{capability/maxRecords}"/>
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#primaryTable" target="nvohelp">Primary Table:</a>     
    </td>
    <td>
        <input type="text" name="primaryTable" size="20"  value="{capability/primaryTable}"/>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        The name of the table containing the primary key (usually the rows 
        represents objects in the sky).  
        </em></font> 
    </td>
  </tr>

  <tr>
    <td bgcolor="#d15d5d" valign="top"><font color="white">Req.</font></td>
    <td valign="top" width="25%">
<a href="{$doc_dir}/formhelp.html#primaryKey" target="nvohelp">Primary Key:</a>     
    </td>
    <td>
        <input type="text" name="primaryKey" size="20"  value="{capability/primaryKey}"/>
        <br />
        <font color="green" style="font-size: 10pt"><em>
        The name of the column that represents the primary key in the primary
        table.  
        </em></font> 
    </td>
  </tr>

   </xsl:template>

</xsl:stylesheet>
