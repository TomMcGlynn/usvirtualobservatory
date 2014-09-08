<?xml version="1.0"?>
<!--
  -  Provide a reduced-jargon display of resource metadata appropriate for 
  -  end-users.  
  - 
  -  It is intended that this template be included by a wrapper stylessheet
  -  (e.g. nvobrowse.xsl) which is responsible for providing the header and 
  -  footer for the output HTML page.  This stylesheet specifically provides 
  -  templates that support the core VOResource metadata as well as the 
  -  VODataService extension.  More specific extensions are handled by 
  -  companion stylesheets (which should also be included by the wrapper 
  -  stylesheet).
  -
  -  This stylesheet also provides the following named templates intended to 
  -  be called by the wrapper stylesheet:
  -   doccss:  the <link> and <style> markup that provides CSS rules
  -   docjs:   the <script> markup that provides JavaScript code
  --> 
<xsl:stylesheet xmlns:ri="http://www.ivoa.net/xml/RegistryInterface/v1.0" 
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xmlns:xlink="http://www.w3.org/1999/xlink"
                xmlns:stc="http://www.ivoa.net/xml/STC/stc-v1.30.xsd"
                exclude-result-prefixes="stc ri xlink"
                version="1.0">

   <!--
     -  set this to true() if you want the resource description to be enclosed
     -  in a highlighted box (or false() otherwise).  
     -->
   <xsl:param name="highlightDescription" select="true()"/>

   <xsl:param name="getRecordSrvc">
      <xsl:text>getRecord.aspx?id=</xsl:text>
   </xsl:param>

   <xsl:variable name="LC">abcdefghijklmnopqrstuvwxyz</xsl:variable>
   <xsl:variable name="UC">ABCDEFGHIJKLMNOPQRSTUVWXYZ</xsl:variable>

   <xsl:variable name="SimpleQueryURL">http://heasarc.gsfc.nasa.gov/vo/squery/?IVOID=</xsl:variable>

   <!--
     -  The CSS macros used by the resulting HTML output.  Be sure to 
     -  call this template while formatting the header of the document.
     -->
   <xsl:template name="doccss" xml:space="preserve">
<link rel="stylesheet" type="text/css" href="js/nvotip.css"/>
<style type="text/css">
  .tiny		{FONT-SIZE: 7pt;}
  .tinylink	{FONT-SIZE: 7pt; COLOR:#aaaaff;}
  .navlink	{MARGIN-TOP: 0px; MARGIN-BOTTOM: 1px; FONT-SIZE:9pt; 
                 BACKGROUND-COLOR:#6ba5d7; text-align: center}
  .navlink a	{TEXT-DECORATION:none;COLOR:#FFFFFF;}
  .navlink a:hover { COLOR: #99FFCC; }
  .helplink	{MARGIN-TOP: 0px; MARGIN-BOTTOM: 1px; FONT-SIZE:9pt; 
                 BACKGROUND-COLOR:#24386d; text-align: center}
  .helplink a	{TEXT-DECORATION:none;COLOR:#FFFFFF;}
  .helplink a:hover { COLOR: #99FFCC; }
  .nvolink	{MARGIN-TOP: 0px; MARGIN-BOTTOM: 1px; FONT-SIZE:9pt; 
                 PADDING-LEFT: 2px;
                 PADDING-RIGHT: 2px; }
  .nvolink a	{TEXT-DECORATION:none;COLOR:#6ba5d7;}
  .nvolink a:hover { COLOR: #99FFCC; }
  .section	{MARGIN-TOP: 2px; MARGIN-BOTTOM: 2px; BACKGROUND-COLOR:#DDDDDD;}
  .nvoapptitle  { color: #243a6d; 
                  font-weight: bolder; font-size: 14pt;
                  text-align: center; margin-left: 2px; margin-right: 2px; }
  p 		{MARGIN-TOP: 0px; MARGIN-BOTTOM: 8px;}
  .th           {font-weight:800; background-color:#DDDDEF;}
  .td           {background-color:#EEEEEE;}
  p,tr,td,dt,dd {FONT-WEIGHT: normal; FONT-SIZE: 9pt; FONT-STYLE: normal; }
  .resclass     {font-size: 14pt; font-weight: 700; COLOR: #003366;}
  .restitle     {font-size: 18pt; font-weight: 700; COLOR: #003366;
                 margin-bottom: 12pt;}
  .abstract     {font-size: 12pt; font-weight: 700; margin-bottom: 0pt; 
                 COLOR: #003366;}
  .showhide     {font-size: 80%; font-size: 80%; padding-left: 3px; 
                 display: none;}
  .collapse     {margin-top: 10px;}
  .collapsetitle {font-size: 12pt; font-weight: 700; COLOR: #003366; 
                  padding-left: 5px; }
  .show         {font-size: 100%; font-style: italic; padding-left: 5px;}
  .hide         {font-size: 100%; font-style: italic; padding-left: 5px;}
  .moretitle    {font-size: 12pt; font-weight: 700; margin-bottom: 0pt; 
                 COLOR: #003366;}
  .morebut      {width: 20px; font-size: 80%; vertical-align: middle;}
  .morehelp     {color: green; font-style: italic;}
  .vordesc      {margin-top: 0pt; margin-bottom: 0pt;}
</style>
   </xsl:template>

   <!--
     -  The javascript used by the resulting HTML output.  Be sure to 
     -  call this template while formatting the header of the document.
     -->
   <xsl:template name="docjs" xml:space="preserve">
<script type="text/javascript" src="js/prototype.js"></script>
<script type="text/javascript" src="js/prototip.js"></script>
<script type='text/javascript'>
var SHOWHIDE_PARENT_NAME = "showhide";
var SHOWHIDE_PARENT_TYPE = "span";
var SHOWHIDE_CHILD_TYPE = "span";
var SHOWCHOICE_CLASS = "show"
var HIDECHOICE_CLASS = "hide"

var MORE_PARENT_NAME = "more";
var MORE_PARENT_TYPE = "table";
var MORE_BUTTON_TYPE = "td";
var MORE_BUTTON_NAME = "morebut";
var MORE_SECT_TYPE = "div";
var MORE_SECT_NAME = "moresect";

var SHOWHIDE_EXPAND = "[+]";
var SHOWHIDE_SHRINK = "[-]";

init = function() {
    if(document.getElementById &amp;&amp; document.createTextNode) {
        var entries = document.getElementsByTagName(SHOWHIDE_PARENT_TYPE);

        for(var i=0;i&lt;entries.length;i++) {
            if (entries[i].className==SHOWHIDE_PARENT_NAME) {
                assignShowHide(entries[i]);
                entries[i].style.display = 'inline';
            }
        }

        entries = document.getElementsByTagName(MORE_PARENT_TYPE);
        for(var i=0;i&lt;entries.length;i++) {
            if (entries[i].className==MORE_PARENT_NAME) {
                assignMore(entries[i]);
            }
        }
    }

    if ($('whatsResource')) {
        new Tip('whatsResource',
                "A resource is usually a <strong>data collection</strong> or <strong>service</strong> available to VO, but it can also refer to other pieces of the VO like <strong>organizations, standards,</strong> or <strong>coordinate systems</strong>.  In general, it's anything with an IVOA identifier that can play some role in the VO web.  And if it's a VO Resource, you can find it in this registry.",
                {title: "What's a Resource?", className: 'nvotip', showOn: 'click',
                 hideOn: 'click', closeButton: true, offset: { x: 0, y: 10 },
                 hook: { target: 'bottomLeft', tip: 'topRight' } } );
    }
}

assignShowHide = function (div) {
    var button = document.createElement('a');
    button.style.cursor='pointer';
    button.setAttribute('expand', SHOWHIDE_EXPAND);
    button.setAttribute('shrink', SHOWHIDE_SHRINK);
    button.setAttribute('state', -1);
    button.innerHTML='dsds';
    div.insertBefore(button, div.getElementsByTagName(SHOWHIDE_CHILD_TYPE)[0]);

    button.onclick=function(){
        var state = -(1*this.getAttribute('state'));
        this.setAttribute('state', state);
        choices = this.parentNode.getElementsByTagName(SHOWHIDE_CHILD_TYPE);
        for(var i=0; i &lt; choices.length; i++) {
           if (choices[i].className == SHOWCHOICE_CLASS) {
               choices[i].style.display = (state == 1) ? 'none' : 'inline';
           }
           else {
               choices[i].style.display = (state == 1) ? 'inline' : 'none';
           }
        }
        this.innerHTML = this.getAttribute(state==1?'expand':'shrink');
    };                   
    button.onclick();
}

assignMore = function (div) {
    var button = document.createElement('a');
    button.style.cursor='pointer';
    button.setAttribute('expand', SHOWHIDE_EXPAND);
    button.setAttribute('shrink', SHOWHIDE_SHRINK);
    button.setAttribute('state', 1);
    button.innerHTML='[=]';

    var buttonparent = null;
    var nodes = div.getElementsByTagName(MORE_BUTTON_TYPE);
    for(var i=0; i &lt; nodes.length; i++) {
        if (nodes[i].className == MORE_BUTTON_NAME) {
            buttonparent = nodes[i];
            break;
        }
    }
/*
    if (nodes.length == 0) {
       alert("no " + MORE_BUTTON_TYPE + " tag in " + div.innerHTML);
    }
    else if (buttonparent == null) {
       alert("no " + MORE_BUTTON_NAME + " class in " + div.innerHTML);
    }
 */
    buttonparent.appendChild(button);

    button.onclick=function(){
        var state = -(1*this.getAttribute('state'));
        this.setAttribute('state', state);

        var more = this.parentNode;
        while (more.className != 'more' &amp;&amp; more.parentNode != null) { 
            more = more.parentNode; 
        }
        if (more.className == 'more') {
            var nodes = more.getElementsByTagName(MORE_SECT_TYPE);
            for(var i=0; i &lt; nodes.length; i++) {
               if (nodes[i].className == MORE_SECT_NAME) more = nodes[i];
            }
            more.style.display = (state == 1) ? 'inline' : 'none';
            this.innerHTML = this.getAttribute(state==1?'shrink':'expand');
        }
    }
    button.onclick();
}

window.onload=init;
</script>
   </xsl:template>

   <!-- 
     -  the main template.   This will produce a complete but plain and 
     -  unadorned HTML document.  Override this to provide a standard header 
     -  and footer, calling the template matching ri:Resource in the effective 
     -  body of the document.  
     --> 
   <xsl:template match="/">

      <xsl:for-each select="." xml:space="preserve">
<html>
<head>
<title><xsl:value-of select="//ri:Resource/shortname|//resource/shortname"/>: Resource Record Summary</title>
<link href="http://www.us-vo.org/usvo3.css" type="text/css" rel="stylesheet" />
<xsl:call-template name="doccss"/>
<xsl:call-template name="docjs"/>
</head>
<body>

         <xsl:apply-templates select="//ri:Resource|//resource" />

</body>
</html>
      </xsl:for-each>
   </xsl:template>

   <!--
     -  create the view of a Resource.  
     -->
   <xsl:template match="ri:Resource|resource">
      <xsl:variable name="resclass">
         <xsl:apply-templates select="." mode="getResourceClass"/>
      </xsl:variable>

      <xsl:for-each select=".">
        <table border="0" cellpadding="0" cellspacing="0" width="100%">
          <tr>
            <td valign="top" rowspan="2">
              <p class="restitle">
                <span class="resclass">
                  <xsl:value-of select="$resclass"/>:
                </span>
                <br />
                <xsl:value-of select="title"/>
              </p>
            </td>
            <td align="center">
              <xsl:apply-templates select="curation" mode="showlogo"/>
            </td>
          </tr>
        </table>
        <!--table cellpadding="5" frame="box"-->

        <xsl:apply-templates select="." mode="digest"/> <p />

        <xsl:call-template name="showDescription">
           <xsl:with-param name="highlight" select="$highlightDescription"/>
        </xsl:call-template>

        <h3>More About this Resource</h3>
<xsl:apply-templates select="." mode="moreAbout"/>

<xsl:if test="capability">

<h3>Available Service Interfaces</h3>

<xsl:apply-templates select="capability" />

</xsl:if>

      </xsl:for-each>
   </xsl:template>

   <!--
     -  display a "digest" summary of the resource via a selection of the 
     -  metadata that is considered most important to see at the top of the 
     -  page.  This does not include the resource description.  This can 
     -  be overridden for a specific xsi:type of resource, but it should 
     -  call the Resource template with mode="digestCore" to get core 
     -  metadata.  
     -->
   <xsl:template match="ri:Resource|resource" mode="digest">
      <xsl:apply-templates select="." mode="digestCore"/>
   </xsl:template>

   <!--
     -  display a rendering of the core digest metadata.  This
     -  information is formatted into a two-column data.  The default
     -  contents of the two columns is handled by templates digest1
     -  and digest2, respectively.
     -->
   <xsl:template match="ri:Resource|resource" mode="digestCore">
      <table width="100%" cellspacing="0" cellpadding="0">
        <tr align="top">
          <td valign="top" rowspan="2">
              <xsl:apply-templates select="." mode="digest1"/>
          </td>
          <td valign="top">
              <xsl:apply-templates select="." mode="digest2"/>
          </td>
        </tr>
        <tr>
          <td valign="middle">
              <a href="{$getRecordSrvc}{normalize-space(identifier)}&amp;format=xml">Get XML</a>
          </td>
        </tr>
      </table>
   </xsl:template>
   <!-- 
     -  display a "digest" summary of the second set of selected core resource 
     -  metadata.  This set is rendered in the right side of the digest
     -  table formatted by the digestCore template.  This set includes 
     -  @status.
     -->
   <xsl:template match="ri:Resource|resource" mode="digest2">
      <strong>Status: </strong> <xsl:value-of select="@status"/> <br />
      <strong>Registered: </strong>
      <xsl:call-template name="fmtdate">
        <xsl:with-param name="date" select="@created"/>
      </xsl:call-template> <br/>
   </xsl:template>

   <!-- 
     -  display a "digest" summary of the first set of selected core resource 
     -  metadata.  This set is rendered in the left side of the digest
     -  table formatted by the digestCore template.  This set includes 
     -  shortName, identifier, curation/publisher, and content/referenceURL.  
     -->
   <xsl:template match="ri:Resource|resource" mode="digest1">

<strong>Short name: </strong>  <xsl:choose>
         <xsl:when test="normalize-space(shortName)=''">
            <em>none specified</em>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="shortName"/></xsl:otherwise>
      </xsl:choose> <br />
<strong>IVOA Identifier: </strong>  <xsl:value-of select="identifier"/><br />
<strong>Publisher: </strong>
<xsl:choose>
   <xsl:when test="curation/publisher/@ivo-id">
<a href="{$getRecordSrvc}{normalize-space(curation/publisher/@ivo-id)}"><xsl:value-of select="curation/publisher"/></a>
   </xsl:when>
   <xsl:otherwise><xsl:value-of select="curation/publisher"/></xsl:otherwise>
</xsl:choose>
<xsl:if test="curation/publisher/@ivo-id">
<span class="showhide">
   <span class="show"><xsl:value-of select="curation/publisher/@ivo-id"/></span>
   <span class="hide">[Pub. ID]</span>
</span>
 </xsl:if>
 <br />
 <strong>More Info: </strong> <a href="{normalize-space(content/referenceURL)}"><xsl:value-of select="content/referenceURL"/></a> <br/>
<xsl:call-template name="showValidationLevel" />

    </xsl:template>

    <!--
      -  display the resource description.  This assumes the description is 
      -  accessible via content/description.
      -  @param highlight   if true, highlight the description in a box
      -  @param bgcolor     when highlight is true, this will be used as the 
      -                        background color
      -->
    <xsl:template name="showDescription">
      <xsl:param name="highlight" select="false()"/>
      <xsl:param name="bgcolor">#eeeeff</xsl:param>

      <xsl:choose>
         <xsl:when test="boolean($highlight)">

<table bgcolor="{$bgcolor}" cellpadding="5" frame="box" width="100%">
  <tr>
    <td>
      <p class="abstract">Description</p>
      <br />
      <p style="text-indent: 20px;">
        <xsl:choose>
          <xsl:when test="normalize-space(content/description)=''">
            <em>None provided.</em>
          </xsl:when>
          <xsl:otherwise>
            <xsl:value-of select="content/description"/>
          </xsl:otherwise>
        </xsl:choose>
      </p>
    </td>
  </tr>
</table>      

         </xsl:when>
         <xsl:otherwise>

<p class="abstract">Description</p>
<p style="text-indent: 20px;">
<xsl:choose>
   <xsl:when test="content/description">
      <xsl:value-of select="content/description"/>
   </xsl:when>
   <xsl:otherwise><em>None provided.</em></xsl:otherwise>
</xsl:choose>
</p>

         </xsl:otherwise>
      </xsl:choose>
    </xsl:template>
    

    <!--
      -  produce a short description of the type of resource
      -  appropriate for the end user
      -->
    <xsl:template match="ri:Resource|resource" mode="getResourceClass">

       <xsl:variable name="hasImageCap">
          <xsl:copy-of 
          select="boolean(capability[contains(@xsi:type,':SimpleImageAccess')])"/>
       </xsl:variable>

       <xsl:variable name="hasCatCap">
          <xsl:copy-of 
               select="boolean(capability[contains(@xsi:type,':ConeSearch') or
                                          contains(@xsi:type,':SkyNode')])"/>
       </xsl:variable>

       <xsl:choose>
          <xsl:when test="$hasImageCap='true' and $hasCatCap='true'">
             <xsl:text>Image and Catalog Service</xsl:text>
          </xsl:when>
          <xsl:when test="$hasImageCap='true'">
             <xsl:text>Image Service</xsl:text>
          </xsl:when>
          <xsl:when test="substring-after(@xsi:type,':') = 'CatalogService' or 
                          $hasCatCap='true'">
             <xsl:text>Catalog Service</xsl:text>
          </xsl:when>
          <xsl:when test="substring-after(@xsi:type,':') = 'DataService'">
             <xsl:text>Data Service</xsl:text>
          </xsl:when>
          <xsl:when test="substring-after(@xsi:type,':') = 'DataCollection' and 
                          normalize-space(content/type)='Archive'">
             <xsl:text>Data Archive</xsl:text>
          </xsl:when>
          <xsl:when test="substring-after(@xsi:type,':') = 'DataCollection' and 
                          normalize-space(content/type)='Catalog'">
             <xsl:text>Catalog</xsl:text>
          </xsl:when>
          <xsl:when test="substring-after(@xsi:type,':') = 'DataCollection'">
             <xsl:text>Data Collection</xsl:text>
          </xsl:when>
          <xsl:when test="substring-after(@xsi:type,':') = 'Organisation'">
             <xsl:text>Organization</xsl:text>
          </xsl:when>
          <xsl:when test="substring-after(@xsi:type,':') = 'Registry'">
             <xsl:text>VO Registry</xsl:text>
          </xsl:when>
          <xsl:when test="substring-after(@xsi:type,':') = 'Authority'">
             <xsl:text>VO Infrastructure (Naming Authority)</xsl:text>
          </xsl:when>
          <xsl:when test="substring-after(@xsi:type,':') = 'Standard'
                          or substring-after(@xsi:type,':') = 'ServiceStandard'">
             <xsl:text>VO Infrastructure (Standard)</xsl:text>
          </xsl:when>
          <xsl:when test="substring-after(@xsi:type,':') = 'Service'">
             <xsl:text>Service</xsl:text>
          </xsl:when>
          <xsl:when test="substring-after(@xsi:type,':') = 'Resource' or
                          not(@xsi:type)">
             <xsl:text>Generic Resource</xsl:text>
          </xsl:when>
          <xsl:otherwise>
             <xsl:text>Specialized Resource (</xsl:text>
             <xsl:choose>
               <xsl:when test="contains(@xsi:type,':')">
                  <xsl:value-of select="substring-after(@xsi:type,':')"/>
               </xsl:when>
               <xsl:otherwise><xsl:value-of select="@xsi:type"/></xsl:otherwise>
             </xsl:choose>
             <xsl:text>)</xsl:text>
          </xsl:otherwise>
       </xsl:choose>
    </xsl:template>

    <!--
      -  select and show a creator logo
      -->
    <xsl:template match="curation" mode="showlogo" xml:space="default">
       <xsl:param name="urlshowable" select="true()"/>

       <xsl:for-each select="creator/logo[normalize-space(.)!='']">
          <!-- select the first one found -->
          <xsl:if test="position()=1">
             <xsl:text>   </xsl:text>
             <xsl:comment> Logo for Resource: </xsl:comment><xsl:text>
    </xsl:text>
             <a href="{normalize-space(../content/referenceURL)}"><xsl:text>
       </xsl:text>
                <img src="{normalize-space(.)}" border="0" /><xsl:text>
    </xsl:text>
             </a><xsl:text>
 </xsl:text>

             <xsl:if test="$urlshowable">
 <br /><span class="showhide">
    <span class="show"><xsl:value-of select="."/></span>
    <span class="hide">[Logo URL]</span>
 </span>
             </xsl:if>

          </xsl:if>
       </xsl:for-each>
    </xsl:template>

    <!--
      -  render the validation level.  If the validation level has not 
      -  been specified, the display indicates this.  The implementation
      -  assumes that if the validationLevel has been specified, then
      -  it will be accessible via the XPath, validationLevel.  
      -->
    <xsl:template name="showValidationLevel">
       <xsl:choose>
          <xsl:when test="validationLevel">
            <strong>VO Compliance: </strong> <xsl:apply-templates select="validationLevel" />
          </xsl:when>
          <xsl:otherwise>
             <!--<em>not specified</em>-->
          </xsl:otherwise>
       </xsl:choose>
    </xsl:template>

    <!--
      -  format a validation level
      -->
    <xsl:template match="validationLevel" xml:space="default">
       <xsl:text>Level </xsl:text>
       <xsl:value-of select="normalize-space(.)"/>
       <xsl:text>: </xsl:text>

       <xsl:choose>
          <xsl:when test="number(.)=0">
             <xsl:text>This description appears to be invalid.</xsl:text>
          </xsl:when>
          <xsl:when test="number(.)=1">
             <xsl:text>This is a valid resource description.</xsl:text>
          </xsl:when>
          <xsl:when test="number(.)=2">
             <xsl:text>This is a VO-compliant resource.</xsl:text>
          </xsl:when>
          <xsl:when test="number(.)=3">
             <xsl:text>This is a well-described and VO-compliant resource.</xsl:text>
          </xsl:when>
          <xsl:when test="number(.)=4">
             <xsl:text>This is a high-quality, VO-compliant resource.</xsl:text>
          </xsl:when>
       </xsl:choose>
    </xsl:template>

    <!--
      -  Display detailed renderings of the resource metadata for the section
      -  "More About this Resource"
      -->
    <xsl:template match="ri:Resource|resource" mode="moreAbout">
       <xsl:apply-templates select="." mode="moreAboutCore"/>
    </xsl:template>

    <!--
      -  Display detailed renderings of the resource metadata for the section
      -  "More About this Resource".  This is the default rendering of the 
      -  core resource metadata.
      -->
    <xsl:template match="ri:Resource|resource" mode="moreAboutCore">
 <table class="more" border="0" cellpadding="0" cellspacing="0" width="100%"><tr>
 <td class="morebut"></td>
 <td class="moretitle">About the Resource Providers</td></tr>
 <tr><td></td><td>
 <p class="morehelp">
 This section describes who is responsible for this resource</p>

 <div class="moresect">
 <p style="font-size: 100%;">
 <!-- <font size="120%"> -->
 <strong>Publisher: </strong>
 <xsl:choose>
    <xsl:when test="curation/publisher/@ivo-id">
 <a href="{$getRecordSrvc}{normalize-space(curation/publisher/@ivo-id)}"><xsl:value-of select="curation/publisher"/></a>
    </xsl:when>
    <xsl:otherwise><xsl:value-of select="curation/publisher"/></xsl:otherwise>
 </xsl:choose>
 <!-- </font>  -->
 <xsl:if test="curation/publisher/@ivo-id">
 <span class="showhide">
    <span class="show"><xsl:value-of select="curation/publisher/@ivo-id"/></span>
    <span class="hide">[Pub. ID]</span>
 </span>
 </xsl:if>
 </p>

 <p>
 <table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr><td valign="top" width="35%">
 <xsl:apply-templates select="curation" mode="creator"/>
 </td><td valign="top">
 <xsl:apply-templates select="curation" mode="contributor"/>
 </td></tr>
 </table>
 </p>

 <xsl:apply-templates select="curation" mode="contact"/>
 </div>
 </td></tr></table>

 <table class="more" border="0" cellpadding="0" cellspacing="0" width="100%"><tr>
 <td class="morebut"></td>
 <td class="moretitle">Status of This Resource</td></tr>
 <tr><td></td><td>
 <p class="morehelp">This section provides some status information: the
   resource version, availability, and relevant dates.</p>
 <div class="moresect">
 <table border="0" cellpadding="0" cellspacing="0" width="100%">
 <tr><td valign="top">
    <xsl:apply-templates select="curation" mode="version" /><br />
    <xsl:apply-templates select="."        mode="status" />
    <xsl:apply-templates select="."        mode="rights"/>
 </td><td valign="top">
    <xsl:apply-templates select="curation" mode="date"/><br />
 </td></tr>
 </table>

 <p>
 <strong>This resource was registered on: </strong>
 <xsl:call-template name="fmtdate">
   <xsl:with-param name="date" select="@created"/>
 </xsl:call-template> <br/>
 <strong>This resource description was last updated on: </strong>
 <xsl:call-template name="fmtdate">
   <xsl:with-param name="date" select="@updated"/>
 </xsl:call-template> <br/>
 </p>

 </div>
 </td></tr></table>

 <table class="more" border="0" cellpadding="0" cellspacing="0" width="100%"><tr>
 <td class="morebut"></td>
 <td class="moretitle">What This Resource is About</td></tr>
 <tr><td></td><td>
 <p class="morehelp">This section describes what the resource is, what it contains, and how it might be relevant.</p>
 <div class="moresect">
 <xsl:apply-templates select="." mode="contentSummary"/>
 <xsl:if test="accessURL">
 <p>
 This data is accessible over the Web at 
 <a href="{normalize-space(accessURL)}"><xsl:value-of select="accessURL"/></a>.
 </p>
 </xsl:if>

 <table border="0" cellpadding="0" cellspacing="0" width="100%">
   <tr>
     <td valign="top" width="50%">
 <xsl:apply-templates select="content" mode="type"/>
 <xsl:apply-templates select="." mode="format"/>
     </td>
     <td valign="top">
 <xsl:apply-templates select="content" mode="subject"/>
 <xsl:apply-templates select="." mode="facility"/>
     </td>
   </tr>
   <tr>
     <td colspan="2" valign="top">
 <xsl:apply-templates select="content" mode="contentLevel"/>
     </td>
   </tr>
   <tr>
     <td valign="top">
 <xsl:apply-templates select="content" mode="referenceURL"/>
     </td>
     <td valign="top">
 <xsl:apply-templates select="content" mode="source"/>
     </td>
   </tr>
 <xsl:if test="content/relationship">  <tr>
     <td valign="top" colspan="2">
 <xsl:apply-templates select="content" mode="relationship"/>
     </td>
   </tr></xsl:if>
 </table><p />

 </div>
 </td></tr></table>

<!--
 <xsl:if test="catalog">
 <table class="more" border="0" cellpadding="0" cellspacing="0" width="100%"><tr>
 <td class="morebut"></td>
 <td class="moretitle">Catalog/Tabular Data Information</td></tr>
 <tr><td></td><td>
 <p class="morehelp">This section describes what the tabular data available from this resource.</p>
 <div class="moresect">

 </div>
 </td></tr></table>
 </xsl:if>
-->

 <xsl:if test="coverage">
 <table class="more" border="0" cellpadding="0" cellspacing="0" width="100%"><tr>
 <td class="morebut"></td>
 <td class="moretitle">Data Coverage Information</td></tr>
 <tr><td></td><td>
 <p class="morehelp">This section describes the data's coverage over the sky, frequency, and time.</p>
 <div class="moresect">
 <xsl:apply-templates select="coverage" />
 </div>
 </td></tr></table>
 </xsl:if>

       <xsl:apply-templates select="." mode="moreAboutExtended"/>

    </xsl:template>

    <!--
      -  render the data coverage information
      -->
    <xsl:template match="coverage">
      <p>
      <xsl:apply-templates select="stc:STCResourceProfile" mode="systems" />
      </p>
      <p>
      <xsl:apply-templates select="stc:STCResourceProfile" mode="skycoverage" />
      <xsl:if test="stc:STCResourceProfile/stc:AstroCoords/stdPosition1D/stc:Resolution">
        <strong>Spatial Resultion:</strong>
        <xsl:for-each select="stc:STCResourceProfile/stc:AstroCoords/stc:Position1D/stc:Resolution">
          <xsl:if test="position()!=last()">, </xsl:if>
          <xsl:apply-templates select="." />
          <xsl:text> </xsl:text>
          <xsl:choose>
            <xsl:when test="@pos_unit">
              <xsl:value-of select="@pos_unit"/>
            </xsl:when>
            <xsl:when test="../@unit">
              <xsl:value-of select="../@unit"/>
            </xsl:when>
            <xsl:otherwise><i>(unspecified units)</i></xsl:otherwise>
          </xsl:choose>
        </xsl:for-each> <br />
      </xsl:if>
      <xsl:if test="stc:STCResourceProfile/stc:AstroCoords/stc:Position1D/stc:Size">
        <xsl:variable name="s">
          <xsl:if test="count(stc:STCResourceProfile/stc:AstroCoords/stc:Position1D/stc:Size) > 1">s</xsl:if>
        </xsl:variable>
        <strong>Typical Size Scale<xsl:value-of select="$s"/> (Region of Regard): </strong>
        <xsl:for-each select="stc:STCResourceProfile/stc:AstroCoords/stc:Position1D/stc:Size">
          <xsl:if test="position()=last()">, </xsl:if>
          <xsl:value-of select="."/>
          <xsl:text> </xsl:text>
          <xsl:choose>
            <xsl:when test="@pos_unit">
              <xsl:value-of select="@pos_unit"/>
            </xsl:when>
            <xsl:when test="../@unit">
              <xsl:value-of select="../@unit"/>
            </xsl:when>
            <xsl:otherwise><i> (unspecified units)</i></xsl:otherwise>
          </xsl:choose>
        </xsl:for-each>
      </xsl:if>
      </p>
      <p>
      <xsl:apply-templates select="stc:STCResourceProfile" mode="speccoverage" />
      <xsl:if test="waveband">
        <strong>Wavebands covered:</strong>
        <ul>
          <xsl:for-each select="waveband">
            <li><xsl:value-of select="."/></li>
          </xsl:for-each>
        </ul>
      </xsl:if>
      </p>
      <p>
      <xsl:apply-templates select="stc:STCResourceProfile" mode="timecoverage" />
      </p>
    </xsl:template>

    <!--
      -  render the coordinate system descriptions
      -->
    <xsl:template match="stc:STCResourceProfile" mode="systems">
      <xsl:choose>
        <xsl:when test="count(*[contains(local-name(),'System')])=1">
          <strong>Reference Coordinate System: </strong>
          <xsl:for-each select="*[contains(local-name(),'System')]">
             <xsl:call-template name="coordsys" />
          </xsl:for-each>
        </xsl:when>
        <xsl:otherwise>
          <strong>Referenced Coordinate Systems: </strong><br />
          <ul>
            <xsl:for-each select="*[contains(local-name(),'System')]">
              <li> <xsl:call-template name="coordsys"/> </li>
            </xsl:for-each>
          </ul>
        </xsl:otherwise>
      </xsl:choose>
    </xsl:template>

    <!--
      -  render a coordinate system briefly
      -  @context an STC coordsys element
      -->
    <xsl:template name="coordsys">
      <xsl:choose>
        <xsl:when test="starts-with(@xlink:href, 'ivo://') and 
                        contains(@xlink:href,'#')">
          <!-- registered, pre-defined coordinate system -->
          <a href="http://www.ivoa.net/Documents/latest/STC.html">
            <xsl:value-of 
                 select="substring-after(normalize-space(@xlink:href),'#')"/>
          </a>
          <span style="visibility: hidden;">XX</span>
          <span class="showhide">
            <span class="show">
              <xsl:value-of select="normalize-space(@xlink:href)"/>
            </span>
            <span class="hide">[Res. ID]</span>
          </span>

        </xsl:when>

        <xsl:when test="starts-with(@xlink:href, 'ivo://')">

          <!-- registered, pre-defined coordinate system -->
          <a href="{$getRecordSrvc}{normalize-space(@xlink:href)}">
            <xsl:value-of select="normalize-space(@xlink:href)"/>
          </a>
          <span style="visibility: hidden;">XX</span>
          <span class="showhide">
            <span class="show">
              <xsl:value-of select="normalize-space(@xlink:href)"/>
            </span>
            <span class="hide">[Res. ID]</span>
          </span>
        </xsl:when>

        <xsl:when test="@xlink:href">
          <xsl:text>Unrecognized, pre-defined system (</xsl:text>
          <a href="{@xlink:href}">more info</a>
          <xsl:text>)</xsl:text>
          <span style="visibility: hidden;">XX</span>
          <span class="showhide">
            <span class="show">
              <xsl:value-of select="normalize-space(@xlink:href)"/>
            </span>
            <span class="hide">[URL]</span>
          </span>
        </xsl:when>

        <xsl:otherwise>
          <xsl:text>Custom system (See </xsl:text>
          <a href="{$getRecordSrvc}{//identifier[1]}&amp;format=xml">XML description</a>
          <xsl:text>)</xsl:text>
        </xsl:otherwise>

      </xsl:choose>
    </xsl:template>

    <!--
      -  render the sky coverage
      -->
    <xsl:template match="stc:STCResourceProfile" mode="skycoverage">
      <xsl:for-each select="stc:AstroCoordArea">
        <strong>Sky Coverage: Regions covered: </strong>
        <ul>
          <xsl:apply-templates 
               select="stc:AllSky|stc:Circle|stc:Union|stc:Intersection" 
               mode="region"/>
        </ul>
      </xsl:for-each>
    </xsl:template>

    <!--
      -  render a region: AllSky
      -->
    <xsl:template match="stc:AllSky" mode="region">
      <li> <strong>All-sky: </strong>
           <xsl:text>The data from this resource is distributed </xsl:text>
           <xsl:text>over the entire sky.</xsl:text> </li>
    </xsl:template>
    
    <!--
      -  render a region: Union
      -->
    <xsl:template match="stc:Union" mode="region">
      <xsl:apply-templates select="*" mode="region"/>
    </xsl:template>

    <!--
      -  render a region: CircleRegion
      -->
    <xsl:template match="stc:Circle" mode="region">
      <xsl:param name="sysid">
        <xsl:for-each select="id(@coord_system_id)">
          <xsl:call-template name="coordsys"/>
        </xsl:for-each>
      </xsl:param>

      <li> <strong>Circle: </strong>
           <xsl:text>Centered at: </xsl:text>
           <xsl:call-template name="tosegdec">
             <xsl:with-param name="ang" select="stc:Center/stc:C1"/>
             <xsl:with-param name="unit">
               <xsl:choose>
                 <xsl:when test="stc:Center/stc:C1/@pos_unit">
                   <xsl:value-of select="stc:Center/stc:C1/@pos_unit"/>
                 </xsl:when>
                 <xsl:otherwise>
                   <xsl:value-of select="stc:Center/@unit"/>
                 </xsl:otherwise>
               </xsl:choose>
             </xsl:with-param>
             <xsl:with-param name="hms" select="$sysid='UTC-FK5-TOPO'"/>
           </xsl:call-template> 
           <xsl:text> </xsl:text>
           <xsl:call-template name="tosegdec">
             <xsl:with-param name="ang" select="stc:Center/stc:C2"/>
             <xsl:with-param name="unit">
               <xsl:choose>
                 <xsl:when test="stc:Center/stc:C2/@pos_unit">
                   <xsl:value-of select="stc:Center/stc:C2/@pos_unit"/>
                 </xsl:when>
                 <xsl:otherwise>
                   <xsl:value-of select="stc:Center/@unit"/>
                 </xsl:otherwise>
               </xsl:choose>
             </xsl:with-param>
           </xsl:call-template> 
           <xsl:text>; Radius: </xsl:text>
           <xsl:value-of select="stc:Radius"/>
           <xsl:text> </xsl:text>
           <xsl:choose>
             <xsl:when test="stc:Radius/@pos_unit">
               <xsl:value-of select="stc:Radius/@pos_unit"/>
             </xsl:when>
             <xsl:otherwise><xsl:value-of select="@unit"/></xsl:otherwise>
           </xsl:choose>
           <xsl:value-of select="@pos_unit"/>
      </li>
    </xsl:template>

    <xsl:template name="tosegdec">
      <xsl:param name="ang"/>
      <xsl:param name="unit">deg</xsl:param>
      <xsl:param name="hms" select="false()"/>

      <xsl:variable name="label">
        <xsl:choose>
          <xsl:when test="$hms">h</xsl:when>
          <xsl:otherwise>d</xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="deg">
        <xsl:choose>
          <xsl:when test="$unit='rad'">
            <xsl:value-of select="$ang * 180.0 div 3.141592654"/>
          </xsl:when>
          <xsl:when test="$unit='h' and not(boolean($hms))">
            <xsl:value-of select="$ang * 15.0"/>
          </xsl:when>
          <xsl:when test="$unit='arcmin'">
            <xsl:value-of select="$ang div 60.0"/>
          </xsl:when>
          <xsl:when test="$unit='arcsec'">
            <xsl:value-of select="$ang div 3600.0"/>
          </xsl:when>
          <xsl:otherwise><xsl:value-of select="$ang"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="sign">
        <xsl:choose>
          <xsl:when test="$deg &lt; 0"><xsl:value-of select="-1"/></xsl:when>
          <xsl:otherwise><xsl:value-of select="1"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="angle">
        <xsl:choose>
          <xsl:when test="$unit != 'h' and boolean($hms)">
            <xsl:value-of select="$sign * $deg div 15.0"/>
          </xsl:when>
          <xsl:otherwise><xsl:value-of select="$sign * $deg"/></xsl:otherwise>
        </xsl:choose>
      </xsl:variable>

      <xsl:variable name="d" select="floor($angle)"/>
      <xsl:variable name="m" select="floor(($angle - $d) * 60.0)"/>
      <xsl:variable name="s" select="($angle -$d - ($m div 60.0)) * 3600.0"/>

      <!-- leading +/- -->
      <xsl:choose>
        <xsl:when test="$sign &lt; 0">
          <xsl:value-of select="'-'"/>
        </xsl:when>
        <xsl:when test="not(boolean($hms))">
          <xsl:value-of select="'+'"/>
        </xsl:when>
      </xsl:choose>

      <xsl:value-of select="$d"/>
      <xsl:value-of select="$label"/>
      <xsl:text> </xsl:text>
      <xsl:value-of select="$m"/>
      <xsl:text>m </xsl:text>
      <xsl:value-of select="round($s * 100.0) div 100.0"/>
      <xsl:text>s</xsl:text>
      
    </xsl:template>
    
    <!--
      -  render a region: unrecognized
      -->
    <xsl:template match="*" mode="region" priority="-1">
      <li> <strong>Complex region: </strong>
           <xsl:text>See the </xsl:text>
           <a href="{$getRecordSrvc}{//identifier[1]}&amp;format=xml">XML description</a>
           <xsl:text> for details.</xsl:text> </li>
    </xsl:template>

    <!--
      -  render the spectral coverage
      -->
    <xsl:template match="stc:STCResourceProfile" mode="speccoverage">
      <xsl:if test="stc:AstroCoordArea/stc:SpectralInterval">
        <strong>Spectral Coverage:</strong>
        <ul>
          <xsl:apply-templates select="stc:AstroCoordArea/stc:SpectralInterval"/>
        </ul>
      </xsl:if>
    </xsl:template>

    <xsl:template match="stc:SpectralInterval">
      <li> <xsl:value-of select="stc:LoLimit"/> <xsl:text> - </xsl:text>
           <xsl:value-of select="stc:HiLimit"/> <xsl:text> </xsl:text>
           <xsl:value-of select="@unit"/>
           </li>
    </xsl:template>

    <!--
      -  render the time coverage
      -->
    <xsl:template match="stc:STCResourceProfile" mode="timecoverage">
      <xsl:if test="stc:AstroCoordArea/stc:TimeInterval">
        <strong>Temporal Coverage:</strong>
        <ul>
          <xsl:for-each select="stc:AstroCoordArea/stc:TimeInterval">
            <li>
              <xsl:choose>
                <xsl:when test="stc:StartTime and stc:StopTime">
                  <xsl:value-of select="stc:StartTime/*"/>
                  <xsl:text> - </xsl:text>
                  <xsl:value-of select="stc:StopTime/*"/>
                </xsl:when>
                <xsl:when test="stc:StartTime">
                  <xsl:text>From </xsl:text>
                  <xsl:value-of select="stc:StartTime/*"/>
                </xsl:when>
                <xsl:when test="stc:StopTime">
                  <xsl:text>Until </xsl:text>
                  <xsl:value-of select="stc:StopTime/*"/>
                </xsl:when>
                <xsl:otherwise>
                  <xsl:text>Complex time coverage (see </xsl:text>
                  <a href="{$getRecordSrvc}{//identifier[1]}&amp;format=xml">XML description</a>
                  <xsl:text>)</xsl:text>
                </xsl:otherwise>
              </xsl:choose>
            </li>
          </xsl:for-each>
        </ul>
      </xsl:if>
    </xsl:template>

   <!--
     -  Display detailed renderings of the extended metadata for the section
     -  "More About this Resource".  This template is intended to be overridden
     -  for specific xsi:types of resources (e.g. vg:Registry)
     -->
   <xsl:template match="ri:Resource|resource" mode="moreAboutExtended"/>
   
   <!--
     -  format 0, 1, or more creator elements
     -->
   <xsl:template match="curation" mode="creator" xml:space="default">
      <xsl:choose>
         <xsl:when test="count(creator) &gt; 1">
            <!-- a list of Creators -->
            <xsl:text>   </xsl:text>
            <dl><xsl:text>
      </xsl:text>
               <dt> <strong>Creators: </strong>  </dt> <xsl:text>
      </xsl:text>
               <dd> 
                  <xsl:for-each select="creator">
                     <xsl:choose>
                        <xsl:when test="name/@ivo-id">
                           <a href="{getRecordSrvc}{normalize-space(name/@ivo-id)}">
                              <xsl:value-of select="name"/>
                           </a>  
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:value-of select="name"/>
                        </xsl:otherwise>
                     </xsl:choose>
                     <xsl:if test="name/@ivo-id">
                        <span class="showhide">
                           <span class="show"><xsl:value-of 
                                 select="name/@ivo-id"/></span>
                           <span class="hide">[Creator ID]</span>
                        </span>
                     </xsl:if>
                  </xsl:for-each>
               </dd><xsl:text>
   </xsl:text>
            </dl>
         </xsl:when>

         <xsl:otherwise>
            <!-- Just one (or zero) Creator -->
            <xsl:text>   </xsl:text>
            <strong>Creator: </strong> 
            <xsl:for-each select="creator">
               <xsl:choose>
                  <xsl:when test="name/@ivo-id">
                     <a href="{getRecordSrvc}{normalize-space(name/@ivo-id)}">
                        <xsl:value-of select="name"/>
                     </a>
                     <xsl:if test="name/@ivo-id">
                        <span class="showhide">
                           <span class="show"><xsl:value-of 
                                 select="name/@ivo-id"/></span>
                           <span class="hide">[Creator ID]</span>
                        </span>
                     </xsl:if>
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="name"/>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:for-each>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  format 0, 1, or more contributor elements
     -->
   <xsl:template match="curation" mode="contributor" xml:space="default">
      <xsl:choose>
         <xsl:when test="count(contributor) &gt; 1">
            <!-- a list of Contributors -->
            <xsl:text>   </xsl:text>
            <dl><xsl:text>
      </xsl:text>
               <dt> <strong>Contributors: </strong>  </dt> <xsl:text>
      </xsl:text>
               <dd> 
                  <xsl:for-each select="contributor">
                     <xsl:choose>
                        <xsl:when test="@ivo-id">
                           <a href="{getRecordSrvc}{normalize-space(@ivo-id)}">
                              <xsl:value-of select="."/>
                           </a>  
                        </xsl:when>
                        <xsl:otherwise>
                           <xsl:value-of select="."/>
                        </xsl:otherwise>
                     </xsl:choose>
                     <xsl:if test="@ivo-id">
                        <span class="showhide">
                           <span class="show"><xsl:value-of 
                                 select="@ivo-id"/></span>
                           <span class="hide">[Contrib. ID]</span>
                        </span>
                     </xsl:if>
                     <xsl:if test="position()!=last()">
                        <br />
                     </xsl:if>
                  </xsl:for-each>
               </dd><xsl:text>
   </xsl:text>
            </dl>
         </xsl:when>

         <xsl:otherwise>
            <!-- Just one (or zero) Contributor -->
            <xsl:text>   </xsl:text>
            <strong>Contributor: </strong> 
            <xsl:for-each select="contributor">
               <xsl:choose>
                  <xsl:when test="@ivo-id">
                     <a href="{getRecordSrvc}{normalize-space(@ivo-id)}">
                        <xsl:value-of select="."/>
                     </a>
                     <span class="showhide">
                        <span class="show"><xsl:value-of 
                              select="@ivo-id"/></span>
                        <span class="hide">[Contrib. ID]</span>
                     </span>              
                  </xsl:when>
                  <xsl:otherwise>
                     <xsl:value-of select="."/>
                  </xsl:otherwise>
               </xsl:choose>
            </xsl:for-each>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  format 0, 1, or more contact elements
     -->
   <xsl:template match="curation" mode="contact" xml:space="default">
      <p><xsl:text>
</xsl:text>
      <strong>Contact Information: </strong><xsl:text>
</xsl:text>
      <table border="0" cellpadding="0" cellspacing="0" width="100%"><xsl:text>
  </xsl:text>
        <tr><xsl:text>
    </xsl:text>
          <td><span style="visibility: hidden" >X</span></td><xsl:text>
    </xsl:text>

          <xsl:for-each select="contact">
             <td valign="top"><xsl:text>
      </xsl:text>
               <xsl:apply-templates select="name" mode="contact"/>
               <xsl:apply-templates select="email" />
               <xsl:apply-templates select="address" />
               <xsl:apply-templates select="phone" />
             </td><xsl:text>
</xsl:text>
          </xsl:for-each>
          <xsl:text>  </xsl:text>
        </tr></table><xsl:text>
</xsl:text></p>
   </xsl:template>

   <!--
     -  format a contact name
     -->
   <xsl:template match="name" mode="contact" xml:space="default">
      <xsl:choose>
         <xsl:when test="@ivo-id">
            <a href="{getRecordSrvc}{normalize-space(@ivo-id)}">
               <xsl:value-of select="."/>
            </a>
            <span class="showhide">
               <span class="show"><xsl:value-of 
                     select="@ivo-id"/></span>
               <span class="hide">[Contact. ID]</span>
            </span>              
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="."/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  format a contact email address (substituting out @)
     -->
   <xsl:template match="email">
      <br /><xsl:text>
      </xsl:text>
      <xsl:text>Email: </xsl:text>
      <xsl:choose>
         <xsl:when test="contains(.,'@')">
            <xsl:value-of select="substring-before(.,'@')"/>
            <i><xsl:text> at </xsl:text></i>
            <xsl:value-of select="substring-after(.,'@')"/>
         </xsl:when>
         <xsl:otherwise><xsl:value-of select="email"/></xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  format a contact address
     -->
   <xsl:template match="address">
      <br /><xsl:text>
      </xsl:text>
      <xsl:text>Address: </xsl:text>
      <xsl:call-template name="breakbydelim">
         <xsl:with-param name="text"><xsl:value-of select="."/></xsl:with-param>
         <xsl:with-param name="pre" select="'      '"/>
      </xsl:call-template>
   </xsl:template>

   <!--
     -  format elements in a comma-delimited list each on a separate line
     -->
   <xsl:template name="breakbydelim">
      <xsl:param name="text"/>
      <xsl:param name="delim">,</xsl:param>
      <xsl:param name="pre"/>

      <xsl:choose>
        <xsl:when test="contains($text,',')">
           <xsl:value-of select="substring-before($text,',')"/>
           <br />
           <xsl:text>
</xsl:text>
           <xsl:value-of select="$pre"/>

           <xsl:call-template name="breakbydelim">
              <xsl:with-param name="text" select="substring-after($text,',')"/>
              <xsl:with-param name="delim" select="$delim"/>
              <xsl:with-param name="pre" select="$pre"/>
           </xsl:call-template>
        </xsl:when>
        <xsl:otherwise>
           <xsl:value-of select="$text"/>
        </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  format a contact phone number
     -->
   <xsl:template match="phone">
      <br /><xsl:text>
      </xsl:text>
      <xsl:text>Phone: </xsl:text>
      <xsl:value-of select="."/>
      <br /><xsl:text>
      </xsl:text>
   </xsl:template>

   <!--
     -  format a resource version
     -->
   <xsl:template match="curation" mode="version" xml:space="default">
      <xsl:text>   </xsl:text>
      <strong>Version: </strong>
      <xsl:choose>
         <xsl:when test="version">
            <xsl:value-of select="version"/>
         </xsl:when>
         <xsl:otherwise>n/a</xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  format the resource status
     -->
   <xsl:template match="*[@status]" mode="status">
      <xsl:text>   </xsl:text>
      <strong>Availability: </strong>
      
      <xsl:choose>
         <xsl:when test="@status='active'">
            <xsl:text>This is an active resource.</xsl:text>
         </xsl:when>
         <xsl:when test="@status='inactive'">
            <xsl:text>This is an </xsl:text>
            <i>inactive</i>
            <xsl:text> resource.</xsl:text>
         </xsl:when>
         <xsl:when test="@status='deleted'">
            <i>This resource has been deleted.</i>
         </xsl:when>
         <xsl:otherwise>
            <xsl:value-of select="@status"/>
         </xsl:otherwise>
      </xsl:choose>
   </xsl:template>

   <!--
     -  format the dates associated with a resource.  This does not 
     -  include the resource record's created and updated attributes.
     -->
   <xsl:template match="curation" mode="date" xml:space="default">
      <xsl:if test="date">
         <strong>Relevant dates for this Resource: </strong> 
         <ul style="margin-top: 0pt; padding-left: 20pt;"><xsl:text>
</xsl:text>
            <xsl:for-each select="date">
               <xsl:text>   </xsl:text>
               <xsl:variable name="date">
                 <xsl:call-template name="fmtdate">
                   <xsl:with-param name="date" select="."/>
                   <xsl:with-param name="showtime">cond</xsl:with-param>
                 </xsl:call-template>
               </xsl:variable>

               <li>
               <xsl:choose>
                 <xsl:when test="@role='representative' or not(@role)">
                   <xsl:text>Representative: </xsl:text>
                   <xsl:value-of select="$date"/>
                 </xsl:when>
                 <xsl:otherwise>
                   <xsl:call-template name="capitalize">
                     <xsl:with-param name="text" select="@role"/>
                   </xsl:call-template>
                   <xsl:text>: </xsl:text>
                   <xsl:value-of select="$date"/>
                 </xsl:otherwise>
               </xsl:choose> 
               <xsl:text> </xsl:text></li><xsl:text>
</xsl:text>
            </xsl:for-each>
            <xsl:text>  </xsl:text></ul><xsl:text>
</xsl:text>
         </xsl:if>
   </xsl:template>

   <!--
     -  format an ISO.... date
     -  @param date      the date to format
     -  @param showtime  a directive indicating whether the time should be 
     -                   shown.  The following values are recognized; any other
     -                   value will cause the time to be displayed.  Mimimum 
     -                   match is supported.  
     -                   @value no             do not show the time
     -                   @value conditionally  do not show the time if it is 
     -                                           set to 00:00:00*
     -     
     -->
   <xsl:template name="fmtdate">
      <xsl:param name="date"/>
      <xsl:param name="showtime">yes</xsl:param>

      <xsl:variable name="year" select="substring-before($date,'-')"/>
      <xsl:variable name="mon" 
                    select="substring-before(substring-after($date,'-'),'-')"/>
      <xsl:variable name="ym" select="concat($year,'-',$mon,'-')"/>
      <xsl:variable name="dy" select="substring-after($date,$ym)"/>

      <xsl:variable name="day">
         <xsl:choose>
            <xsl:when test="contains($dy,'T')">
               <xsl:value-of select="substring-before($dy,'T')"/>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select="$dy"/></xsl:otherwise>
         </xsl:choose>
      </xsl:variable>
      
      <xsl:variable name="month"
                    select="concat(translate(substring($mon,1,1),$LC,$UC),
                                   translate(substring($mon,2),$UC,$LC))"/>

      <xsl:value-of select="$year"/><xsl:text> </xsl:text>
      <xsl:call-template name="toMonthName">
        <xsl:with-param name="num" select="$mon"/>
      </xsl:call-template><xsl:text> </xsl:text>
      <xsl:value-of select="$day"/>

      <xsl:choose> 
         <xsl:when test="starts-with('no',$showtime)"/>
         <xsl:when test="starts-with('conditionally',$showtime)">
           <xsl:variable name="time">
              <xsl:value-of select="substring-after($date,'T')"/>
           </xsl:variable>
           <xsl:if test="$time != '' and $time != '00:00:00'">
              <xsl:text> </xsl:text>
              <xsl:value-of select="$time"/>
              <br />
           </xsl:if>
         </xsl:when>
         <xsl:otherwise>
           <xsl:text> </xsl:text>
           <xsl:value-of select="substring-after($date,'T')"/>
         </xsl:otherwise>
      </xsl:choose>

   </xsl:template>

   <!--
     -  Ensure the first character is capitalized
     -->
   <xsl:template name="capitalize">
      <xsl:param name="text"/>
      <xsl:value-of select="concat(translate(substring($text,1,1),$LC,$UC),
                                   substring($text,2))"/>
   </xsl:template>

   <!--
     -  convert a month number to a string
     -->
   <xsl:template name="toMonthName">
     <xsl:param name="num"/>
     <xsl:choose>
       <xsl:when test="number($num)=1">Jan</xsl:when>
       <xsl:when test="number($num)=2">Feb</xsl:when>
       <xsl:when test="number($num)=3">Mar</xsl:when>
       <xsl:when test="number($num)=4">Apr</xsl:when>
       <xsl:when test="number($num)=5">May</xsl:when>
       <xsl:when test="number($num)=6">Jun</xsl:when>
       <xsl:when test="number($num)=7">Jul</xsl:when>
       <xsl:when test="number($num)=8">Aug</xsl:when>
       <xsl:when test="number($num)=9">Sep</xsl:when>
       <xsl:when test="number($num)=10">Oct</xsl:when>
       <xsl:when test="number($num)=11">Nov</xsl:when>
       <xsl:when test="number($num)=12">Dec</xsl:when>
       <xsl:otherwise>???</xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <!--
     -  render table information
     -->
   <xsl:template match="ri:Resource|resource" mode="table">
      <xsl:if test="table">
         <xsl:text>This resource returns information in a table.</xsl:text>
      </xsl:if>
   </xsl:template>

   <!--
     -  Display the resource extension type along with 
     -  a general statement about what the type implies about the resource 
     -  contents.  This statement is sometimes made more specific by 
     -  incorporating other information from the resource record.
     -->
   <xsl:template match="ri:Resource|resource" mode="contentSummary" 
                 xml:space="default">
     <xsl:variable name="resext" select="substring-after(@xsi:type,':')"/>
     <xsl:text>   </xsl:text>
     <dl><xsl:text>
     </xsl:text>
       <dt> <strong>Resource Class: </strong> 
            <xsl:value-of select="$resext"/> </dt><xsl:text>
     </xsl:text>
       <dd> <i>
         <xsl:choose>
           <xsl:when test="$resext='Organisation'">
             <xsl:variable name="noun">
               <xsl:choose>
                 <xsl:when test="content/type='Project'">
                   <xsl:text>a project or research team</xsl:text>
                 </xsl:when>
                 <xsl:otherwise>
                   <xsl:text>an organization, institute, or academic department</xsl:text>
                 </xsl:otherwise>
               </xsl:choose>
             </xsl:variable>

             <xsl:text>This resource represents </xsl:text>
             <xsl:value-of select="$noun"/>
             <xsl:text>that provides and/or uses VO data, services, or applications.</xsl:text>
           </xsl:when>

           <xsl:when test="$resext='Service'">
             <xsl:text>
       This resource respresent a service that may be used in a VO application. 
       Often, a service of this general class provides storage or specialized 
       analysis capabilities (as opposed to access to data).  
             </xsl:text>
           </xsl:when>

           <xsl:when test="$resext='DataService'">
             <xsl:variable name="spec">
               <xsl:apply-templates select="." mode="kindOfServiceData"/>
             </xsl:variable>

             <xsl:text>
       This resource is a service that provides access to data</xsl:text>  
             <xsl:if test="$spec != ''">
               <xsl:text>(specifically, </xsl:text>
               <xsl:value-of select="$spec"/>
               <xsl:text>)</xsl:text>
             </xsl:if>
             <xsl:text>.</xsl:text>
           </xsl:when>

           <xsl:when test="$resext='CatalogService' and 
                           contains(capability/@xsi:type,'Simple')">
             <xsl:variable name="spec">
               <xsl:apply-templates select="." mode="kindOfServiceData"/>
             </xsl:variable>

             <xsl:text>
       This resource is a service that provides access to data</xsl:text>  
             <xsl:if test="$spec != ''">
               <xsl:text> (specifically, </xsl:text>
               <xsl:value-of select="$spec"/>
               <xsl:text>)</xsl:text>
             </xsl:if>
             <xsl:text>.
       With this type of service, you can search for data by issuing a 
       query, and information about available data is returned as a table.  
             </xsl:text>
           </xsl:when>

           <xsl:when test="$resext='CatalogService'">
             <xsl:text>
       This resource is a service that provides access to catalog data.
       You can extract data from the catalog by issuing a query, and the 
       matching data is returned as a table.  
             </xsl:text>  
           </xsl:when>

           <xsl:when test="$resext='DataCollection'">
             <xsl:variable name="collection">
               <xsl:choose>
                 <xsl:when test="content/type='Archive'">
                   <xsl:text>a data archive</xsl:text>
                 </xsl:when>
                 <xsl:when test="content/type='Catalog'">
                   <xsl:text>a catalog</xsl:text>
                 </xsl:when>
                 <xsl:otherwise>a collection of data</xsl:otherwise>
               </xsl:choose>
             </xsl:variable>
             <xsl:text>
       This resource represents </xsl:text>
             <xsl:value-of select="$collection"/>
             <xsl:text>
             </xsl:text>
           </xsl:when>

           <xsl:when test="$resext='StandardSTC'">
             <xsl:text>
       This resource describes a set of standard space-time coordinate systems.
       The definitions it contains allows applications to describe coordinates
       and regions in terms of these common systems in a short-hand notation.
             </xsl:text>
           </xsl:when>

           <xsl:when test="$resext='Registry'">
             <xsl:text>
       A VO Registry is a resource that connects publishers and users.  The
       publishing interface allows publishers to register new resources to 
       the VO, while the search interface allows users to find them.  </xsl:text>
             <xsl:choose>
               <xsl:when test="contains(capability/@xsi:type,'Harvest') and
                               not(contains(capability/@xsi:type,'Search'))">
                 <xsl:text>
       This registry only supports publishing.
                 </xsl:text>
               </xsl:when>
               <xsl:when test="contains(capability/@xsi:type,'Search') and
                               not(contains(capability/@xsi:type,'Harvest'))">
                 <xsl:text>
       This registry only supports searching.
                 </xsl:text>
               </xsl:when>
             </xsl:choose>
           </xsl:when>

           <xsl:when test="$resext='Authority'">
             <xsl:text>
       This resource represents its publisher's authority to publish new 
       resources and assign them IVOA identifiers.  In particular, it records
       the publisher's ownership of identifiers that begin with 
       </xsl:text>
             <xsl:value-of select="identifier"/>
             <xsl:text>.</xsl:text>
           </xsl:when>

           <xsl:when test="$resext='Standard' or $resext='StandardService'">
             <xsl:variable name="spectype">
               <xsl:choose>
                 <xsl:when test="starts-with(identifier,'ivo://ivoa.net')">
                   <xsl:text>an IVOA standard</xsl:text>
                 </xsl:when>
                 <xsl:otherwise>
                   <xsl:text>a local (non-IVOA) standard</xsl:text>
                 </xsl:otherwise>
               </xsl:choose>
             </xsl:variable>

             <xsl:text>
       This resource represents </xsl:text>
             <xsl:value-of select="$spectype"/>
             <xsl:text> specification.  This specification can be found at
       </xsl:text>
             <a href="{normalize-space(content/referenceURL)}">
               <xsl:value-of select="content/referenceURL"/>
             </a>
             <xsl:text>.
             </xsl:text>
           </xsl:when>

           <xsl:when test="$resext='Resource' or $resext=''">
             <xsl:text>
       This generic resource class is used when the resource does not fall into
       any of the currently defined classes.  
             </xsl:text>
           </xsl:when>

         </xsl:choose>
       </i></dd><xsl:text>
   </xsl:text></dl><xsl:text>
</xsl:text>
   </xsl:template>

   <!--
     -  Provide a phrase indicating the kind of data provided by the resource.
     -  This is used by the contentsSummary template.
     -->
   <xsl:template match="ri:Resource|resource" mode="kindOfServiceData">
     <xsl:variable name="datatypes">
       <xsl:text>#</xsl:text>
       <xsl:if test="contains(capability/@xsi:type,'SimpleImageAccess')">
         <xsl:text>images#</xsl:text>
       </xsl:if>
       <xsl:if test="contains(capability/@xsi:type,'SimpleSpectralAccess')">
         <xsl:text>spectra#</xsl:text>
       </xsl:if>
       <xsl:if test="contains(capability/@xsi:type,'ConeSearch') or 
                     contains(capability/@xsi:type,'SkyNode') or 
                     contains(capability/@xsi:type,'TableAccess')">
         <xsl:text>catalogs#</xsl:text>
       </xsl:if>
     </xsl:variable>

     <xsl:call-template name="proseList">
       <xsl:with-param name="list" select="substring-after($datatypes,'#')"/>
     </xsl:call-template>
   </xsl:template>
   <xsl:template name="proseList">
     <xsl:param name="list"/>
     <xsl:param name="next" select="substring-before($list,'#')"/>
     <xsl:param name="rest" select="substring-after($list,'#')"/>
     <xsl:param name="comma">
       <xsl:if test="contains($rest,'#')">,</xsl:if>
     </xsl:param>

     <xsl:value-of select="$next"/>

     <xsl:if test="$rest!=''">
       <xsl:if test="$comma!=''">
         <xsl:value-of select="$comma"/>
         <xsl:text> </xsl:text>
       </xsl:if>
       <xsl:if test="not(contains($rest,'#'))">
         <xsl:text> and </xsl:text>
       </xsl:if>

       <xsl:call-template name="proseList">
         <xsl:with-param name="list" select="$rest"/>
         <xsl:with-param name="comma" select="$comma"/>
       </xsl:call-template>
     </xsl:if>
   </xsl:template>

   <xsl:template match="content" mode="subject" xml:space="default">
     
     <xsl:text>  </xsl:text>
     <strong>Subject keywords: </strong>

     <xsl:choose>

       <xsl:when test="subject">
         <xsl:text>
   </xsl:text>
         <ul><xsl:text>
   </xsl:text>
           <xsl:for-each select="subject">
             <xsl:text>  </xsl:text>
             <li> <xsl:value-of select="."/> </li><xsl:text>
   </xsl:text>
           </xsl:for-each>
         </ul>
       </xsl:when>

       <xsl:otherwise>
         <i>none provided</i>
       </xsl:otherwise>

     </xsl:choose><xsl:text>
</xsl:text>
   </xsl:template>

   <xsl:template match="content" mode="type" xml:space="default">
     
     <xsl:text>  </xsl:text>
     <strong>Resource type keywords: </strong>

     <xsl:choose>

       <xsl:when test="type">
         <xsl:text>
   </xsl:text>
         <ul><xsl:text>
   </xsl:text>
           <xsl:for-each select="type">
             <xsl:text>  </xsl:text>
             <li> <xsl:value-of select="."/> </li><xsl:text>
   </xsl:text>
           </xsl:for-each>
         </ul>
       </xsl:when>

       <xsl:otherwise>
         <i>none provided</i>
       </xsl:otherwise>

     </xsl:choose><xsl:text>
</xsl:text>
   </xsl:template>

   <xsl:template match="content" mode="contentLevel" xml:space="default">
     
     <xsl:text>  </xsl:text>
     <strong>Intended audience or use: </strong>

     <xsl:choose>

       <xsl:when test="contentLevel">
         <xsl:text>
   </xsl:text>
         <ul><xsl:text>
   </xsl:text>
           <xsl:for-each select="contentLevel">
             <xsl:text>  </xsl:text>
             <li> <xsl:value-of select="."/>: 
               <i> 
                 <xsl:apply-templates select="." mode="LevelDef"/>
               </i>
             </li><xsl:text>
   </xsl:text>
           </xsl:for-each>
         </ul>
       </xsl:when>

       <xsl:otherwise>
         <i>none specified</i>
       </xsl:otherwise>

     </xsl:choose><xsl:text>
</xsl:text>
   </xsl:template>

   <xsl:template match="contentLevel" mode="LevelDef">
     <xsl:param name="lev" select="normalize-space(.)"/>

     <xsl:choose>
       <xsl:when test="$lev='General'">
         <xsl:text>
     This resource provides information appropriate for all users.
         </xsl:text>
       </xsl:when>

       <xsl:when test="$lev='Elementary Education'">
         <xsl:text>
     This resource provides information appropriate for use in elementary
     education (e.g. approximate ages 6-11)
         </xsl:text>
       </xsl:when>

       <xsl:when test="$lev='Middle School Education'">
         <xsl:text>
     This resource provides information appropriate for use in middle
     school education (e.g. approximate ages 11-14)
         </xsl:text>
       </xsl:when>

       <xsl:when test="$lev='Secondary Education'">
         <xsl:text>
     This resource provides information appropriate for use in elementary
     education (e.g. approximate ages 14-18).
         </xsl:text>
       </xsl:when>

       <xsl:when test="$lev='Community College'">
         <xsl:text>
     This resource provides information appropriate for use in 
     community/junior college or early university education.
         </xsl:text>
       </xsl:when>

       <xsl:when test="$lev='University'">
         <xsl:text>
     This resource provides information appropriate for use in university 
     education.
         </xsl:text>
       </xsl:when>

       <xsl:when test="$lev='Research'">
         <xsl:text>
     This resource provides information appropriate for supporting scientific 
     research.
         </xsl:text>
       </xsl:when>

       <xsl:when test="$lev='Amateur'">
         <xsl:text>
     This resource provides information of interest to amateur astronomers.
         </xsl:text>
       </xsl:when>

       <xsl:when test="$lev='Informal Education'">
         <xsl:text>
     This resource provides information appropriate for education
     at museums, planetariums, and other centers of informal learning.
         </xsl:text>
       </xsl:when>

     </xsl:choose>
   </xsl:template>

   <!--
     -  render the source bibliographic reference
     -  TODO:  link to ads when appropriate.
     -->
   <xsl:template match="content" mode="source">
     <xsl:if test="source">
       <strong>Literature Reference: </strong>
       <xsl:value-of select="source"/>
     </xsl:if>
   </xsl:template>

   <!--
     -  render the reference URL
     -->
   <xsl:template match="content" mode="referenceURL" xml:space="default">
     <strong>More Info: </strong>
     <xsl:choose>
       <xsl:when test="referenceURL">
         <a href="{normalize-space(referenceURL)}">
           <xsl:value-of select="referenceURL"/></a>
       </xsl:when>
       <xsl:otherwise><i>none provided</i></xsl:otherwise>
     </xsl:choose>
   </xsl:template>

   <!--
     -  Provide information about access rights
     -->
   <xsl:template match="ri:Resource|resource" mode="rights" xml:space="default">
     <xsl:variable name="reshas">
       <xsl:choose>
         <xsl:when test="type='Archive'">
           <xsl:text>archive contains</xsl:text>
         </xsl:when>
         <xsl:when test="contains(@xsi:type,'DataCollection')">
           <xsl:text>collection contains</xsl:text>
         </xsl:when>
         <xsl:when test="contains(@xsi:type,'Service')">
           <xsl:text>service provides</xsl:text>
         </xsl:when>
       </xsl:choose>
     </xsl:variable>

     <xsl:variable name="apphas">
       <xsl:value-of select="substring-before($reshas,' ')"/>
       <xsl:text> apparently </xsl:text>
       <xsl:value-of select="substring-after($reshas,' ')"/>
     </xsl:variable>

     <xsl:choose>
       <xsl:when test="rights">
         <ul type="circle" style="margin-top: 0pt; padding-top: 0pt"><xsl:text>
     </xsl:text>
           <xsl:choose>
             <xsl:when test="rights='public' and not(rights!='public')">
               <xsl:text>  </xsl:text>
               <li> This <xsl:value-of select="$reshas"/> only public data. </li>
               <xsl:text>
     </xsl:text>
             </xsl:when>
             <xsl:when test="rights='proprietary' and rights!='public'">
               <xsl:text>  </xsl:text>
               <li> This <xsl:value-of select="$reshas"/> only proprietary data. </li>
               <xsl:text>
     </xsl:text>
               <xsl:if test="rights='secure'">
                 <xsl:text>  </xsl:text>
                 <li> Some access may require authentication </li><xsl:text>
     </xsl:text>
               </xsl:if>
             </xsl:when>
             <xsl:otherwise>
               <xsl:for-each select="rights">
                 <xsl:choose>
                   <xsl:when test=".='public'">
                     <xsl:text>  </xsl:text>
                     <li> This <xsl:value-of select="$reshas"/> 
                          some publically available data </li><xsl:text>
     </xsl:text>
                   </xsl:when>
                   <xsl:when test=".='proprietary'">
                     <xsl:text>  </xsl:text>
                     <li> This <xsl:value-of select="$reshas"/> 
                          some proprietary data </li><xsl:text>
     </xsl:text>
                   </xsl:when>
                   <xsl:when test=".='secure'">
                     <li> Some access may require authentication. </li><xsl:text>
     </xsl:text>
                   </xsl:when>
                 </xsl:choose>
               </xsl:for-each>
             </xsl:otherwise>
           </xsl:choose>
         </ul><xsl:text>
</xsl:text>
       </xsl:when>
       
       <xsl:when test="contains(@xsi:type,'DataCollection') or 
                       contains(@xsi:type,'DataService') or 
                       contains(@xsi:type,'CatalogService')">
         <ul type="circle" style="margin-top: 0pt; padding-top: 0pt">
           <li> This <xsl:value-of select="$apphas"/> only public data </li>
           <xsl:text>
     </xsl:text>
         </ul><xsl:text>
</xsl:text>
       </xsl:when>

     </xsl:choose>
   </xsl:template>

   <!--
     -  list the associated facilities and instruments
     -->
   <xsl:template match="ri:Resource|resource" mode="facility" 
                 xml:space="default">
     <xsl:variable name="reshas">
       <xsl:choose>
         <xsl:when test="type='Archive'">
           <xsl:text>archive contains</xsl:text>
         </xsl:when>
         <xsl:when test="contains(@xsi:type,'DataCollection')">
           <xsl:text>collection contains</xsl:text>
         </xsl:when>
         <xsl:when test="contains(@xsi:type,'Service')">
           <xsl:text>service provides</xsl:text>
         </xsl:when>
       </xsl:choose>
     </xsl:variable>

     <xsl:if test="facility or instrument">
       <xsl:choose>
         <xsl:when test="$reshas!=''">
           <strong>This <xsl:value-of select="$reshas"/> data from: </strong>
         </xsl:when>
         <xsl:otherwise>
           <strong>Associated observatories/telescopes/instruments: </strong>
         </xsl:otherwise>
       </xsl:choose>
       <ul><xsl:text>
     </xsl:text>
         <xsl:for-each select="facility|instrument">
           <xsl:text>  </xsl:text>
           <li> <xsl:value-of select="local-name()"/>
             <xsl:text>: </xsl:text>
             <xsl:value-of select="."/>
             <xsl:if test="@ivo-id">
               <span class="showhide">
                  <span class="show"><xsl:value-of 
                        select="@ivo-id"/></span>
                  <span class="hide">[Res. ID]</span>
               </span>
             </xsl:if></li><xsl:text>
     </xsl:text>
         </xsl:for-each>
       </ul><xsl:text>
</xsl:text>
     </xsl:if>
   </xsl:template>

   <!--
     -  list any supported formats
     -->
   <xsl:template match="ri:Resource|resource" mode="format" 
                 xml:space="default">
     <xsl:if test="format">
       <strong>Available Formats: </strong><xsl:text>
     </xsl:text>
       <ul>
         <xsl:for-each select="format">
           <xsl:text>  </xsl:text>
           <li> <xsl:value-of select="."/> </li><xsl:text>
     </xsl:text>
         </xsl:for-each>
       </ul>
     </xsl:if>
   </xsl:template>

   <!--
     -  List the related resources
     -->
   <xsl:template match="content" mode="relationship" xml:space="default">
     <xsl:if test="relationship">
       <p style="margin-top: 5pt; margin-bottom: 0pt;">
       <strong>Related Resources: </strong>
       <dl style="margin-top: 0pt">
         <xsl:if test="normalize-space(relationship/relationshipType) =
                       'served-by'">
           <dt> Services that provide access to data in this resource: </dt>
           <xsl:apply-templates 
                 select="relationship[
                            normalize-space(relationshipType)='served-by']"/>
         </xsl:if>
         <xsl:if test="normalize-space(relationship/relationshipType) =
                       'service-for'">
           <dt> This is a service for accessing data from: </dt>
           <xsl:apply-templates 
                 select="relationship[
                            normalize-space(relationshipType)='service-for']"/>
         </xsl:if>
         <xsl:if test="normalize-space(relationship/relationshipType) =
                       'mirror-of'">
           <dt> This resource is a mirror of: </dt>
           <xsl:apply-templates 
                 select="relationship[
                            normalize-space(relationshipType)='mirror-of']"/>
         </xsl:if>
         <xsl:if test="normalize-space(relationship/relationshipType) =
                       'derived-from'">
           <dt> This resource is derived from: </dt>
           <xsl:apply-templates 
                 select="relationship[
                            normalize-space(relationshipType)='derived-from']"/>
         </xsl:if>

         <xsl:variable name="others">
           <xsl:for-each select="relationship">
             <xsl:variable name="type" 
                           select="normalize-space(relationshipType)"/>
             <xsl:if test="$type != 'derived-from' and $type != 'mirror-of' and
                           $type != 'service-for' and $type != 'served-by'">
               <xsl:apply-templates select="."/>
             </xsl:if>
           </xsl:for-each>
         </xsl:variable>
         <xsl:if test="$others!=''">
           <dt> Other Related Resources </dt>
           <xsl:copy-of select="$others"/>
         </xsl:if>
       </dl>
       </p>
     </xsl:if>
   </xsl:template>

   <!--
     -  List a single related resource
     -->
   <xsl:template match="relationship" xml:space="default">
     <xsl:param name="type" select="normalize-space(relationshipType)"/>

     <dd> 
       <xsl:choose>
         <xsl:when test="relatedResource/@ivo-id">
           <a href="{$getRecordSrvc}{normalize-space(relatedResource/@ivo-id)}">
             <xsl:value-of select="normalize-space(relatedResource)"/></a>
           <xsl:if test="$type != 'derived-from' and $type != 'mirror-of' and
                         $type != 'service-for' and $type != 'served-by' and 
                         $type != 'related-to'">
             <xsl:text>(</xsl:text>
             <xsl:value-of select="relationshipType"/>
             <xsl:text>)</xsl:text>
           </xsl:if><xsl:text>
  </xsl:text>
           <span class="showhide"><xsl:text>
    </xsl:text>
             <span class="show"><xsl:value-of 
                   select="relatedResource/@ivo-id"/></span><xsl:text>
    </xsl:text>
             <span class="hide">[Res. ID]</span><xsl:text>
  </xsl:text>
           </span>
         </xsl:when>
         <xsl:otherwise>
           <xsl:value-of select="relatedResource"/>
           <xsl:if test="$type != 'derived-from' and $type != 'mirror-of' and
                         $type != 'service-for' and $type != 'served-by' and 
                         $type != 'related-to'">
             <xsl:text>(</xsl:text>
             <xsl:value-of select="relationshipType"/>
             <xsl:text>)</xsl:text>
           </xsl:if>
         </xsl:otherwise>
       </xsl:choose>
     </dd> 
   </xsl:template>

   <!--
     -  summarize a generic or otherwise unrecognized capability 
     -->
   <xsl:template match="capability">
<table class="more" border="0" cellpadding="0" cellspacing="0" width="100%"><tr>
<td class="morebut"></td>
<td class="moretitle">Custom Service</td></tr>
<tr><td></td><td>
<p class="morehelp">This is service that does not comply with any IVOA standard but instead provides access to special capabilities specific to this resource.</p>
<div class="moresect">
<xsl:call-template name="showValidationLevel" /><br/>

<xsl:if test="description">
<strong>Description: </strong>
<blockquote class="vordesc"><xsl:value-of select="description"/></blockquote>
</xsl:if>

<strong>Available endpoints for this service interface: </strong>
<ul>
<xsl:apply-templates select="interface" />
</ul>

</div>
</td></tr></table>

   </xsl:template>

   <!--
     -  summarize a standard "Simple" service 
     -->
   <xsl:template match="capability" mode="simpleCapability">
      <xsl:param name="name">Simple IVOA Service</xsl:param>
      <xsl:param name="desc"/>

<table class="more" border="0" cellpadding="0" cellspacing="0" width="100%"><tr>
<td class="morebut"></td>
<td class="moretitle"><xsl:value-of select="$name"/>
<span style="visibility: hidden">XX</span>
  <xsl:if test="not($name = 'Simple Spectral Access')">
<font size="-1"><a href="{$SimpleQueryURL}{normalize-space(../identifier)}&amp;type=cone"><i>Search Me</i></a></font>
  </xsl:if>
</td></tr>
<tr><td></td><td>
<p class="morehelp"><xsl:value-of select="$desc"/></p>
<div class="moresect">
<xsl:call-template name="showValidationLevel" /><br/>

<xsl:if test="description">
<strong>Description: </strong>
<blockquote class="vordesc"><xsl:value-of select="description"/></blockquote>
</xsl:if>

<strong>Available endpoints for the standard interface: </strong>
<ul><xsl:for-each select="interface[@role='std']">
   <li> 
     <xsl:for-each select="accessURL[1]">
       <i>
       <xsl:choose>
          <xsl:when test="@use='full'">
             <a href="{normalize-space(.)}"><xsl:value-of select="."/></a>
          </xsl:when>
          <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
       </xsl:choose>
       </i>
       <xsl:choose>
          <xsl:when test="../@version">
             <span style="visibility: hidden">X</span>
             <xsl:text>(version </xsl:text>
             <xsl:value-of select="../@version"/>
             <xsl:text> compatible)</xsl:text>
          </xsl:when>
          <xsl:when test="../../interface[@role='std']/@version">
             <span style="visibility: hidden">X</span>
             <xsl:text>(assuming version 1.0 compatible)</xsl:text>
          </xsl:when>
       </xsl:choose>
     </xsl:for-each>
     <xsl:for-each select="accessURL[position()>1]">
       <br /><xsl:text>Alternate: </xsl:text><i>
       <xsl:choose>
          <xsl:when test="@use='full'">
             <a href="{normalize-space(.)}"><xsl:value-of select="."/></a>
          </xsl:when>
          <xsl:otherwise><xsl:value-of select="."/></xsl:otherwise>
       </xsl:choose>
       </i>
     </xsl:for-each>
   </li> 
</xsl:for-each></ul>

<xsl:if test="interface[@role!='std']">
<strong>Additional non-standard <xsl:value-of select="$name"/> interfaces: </strong>
<ul>
   <xsl:apply-templates select="interface[@role!='std']" />
</ul>
</xsl:if>

  <xsl:if test="interface[not(@role)]">
    <strong>
      Additional non-standard <xsl:value-of select="$name"/> interfaces:
    </strong>
    <ul>
      <xsl:apply-templates select="interface[not(@role)]" />
    </ul>
  </xsl:if>

<xsl:apply-templates select="." mode="extendedCapability"/>

</div>
</td></tr></table>

   </xsl:template>
  
  <xsl:template match="capability" mode="complexCapability">
    <xsl:param name="name">IVOA Service</xsl:param>
    <xsl:param name="desc"/>

    <table class="more" border="0" cellpadding="0" cellspacing="0" width="100%">
      <tr>
        <td class="morebut"></td>
        <td class="moretitle">
          <xsl:value-of select="$name"/>
          <span style="visibility: hidden">XX</span>
        </td>
      </tr>
      <tr>
        <td></td>
        <td>
          <p class="morehelp">
            <xsl:value-of select="$desc"/>
          </p>
          <div class="moresect">
            <xsl:call-template name="showValidationLevel" />
            <br/>

            <xsl:if test="description">
              <strong>Description: </strong>
              <blockquote class="vordesc">
                <xsl:value-of select="description"/>
              </blockquote>
            </xsl:if>

            <strong>Available endpoints for the standard interface: </strong>
            <ul>
              <xsl:for-each select="interface[@role='std']">
                <li>
                  <xsl:for-each select="accessURL[1]">
                    <i>
                      <xsl:choose>
                        <xsl:when test="@use='full'">
                          <a href="{normalize-space(.)}">
                            <xsl:value-of select="."/>
                          </a>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="."/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </i>
                    <xsl:choose>
                      <xsl:when test="../@version">
                        <span style="visibility: hidden">X</span>
                        <xsl:text>(version </xsl:text>
                        <xsl:value-of select="../@version"/>
                        <xsl:text> compatible)</xsl:text>
                      </xsl:when>
                      <xsl:when test="../../interface[@role='std']/@version">
                        <span style="visibility: hidden">X</span>
                        <xsl:text>(assuming version 1.0 compatible)</xsl:text>
                      </xsl:when>
                    </xsl:choose>
                  </xsl:for-each>
                  <xsl:for-each select="accessURL[position()>1]">
                    <br />
                    <xsl:text>Alternate: </xsl:text>
                    <i>
                      <xsl:choose>
                        <xsl:when test="@use='full'">
                          <a href="{normalize-space(.)}">
                            <xsl:value-of select="."/>
                          </a>
                        </xsl:when>
                        <xsl:otherwise>
                          <xsl:value-of select="."/>
                        </xsl:otherwise>
                      </xsl:choose>
                    </i>
                  </xsl:for-each>
                </li>
              </xsl:for-each>
            </ul>

            <xsl:if test="interface[@role!='std']">
              <strong>
                Additional non-standard <xsl:value-of select="$name"/> interfaces:
              </strong>
              <ul>
                <xsl:apply-templates select="interface[@role!='std']" />
              </ul>
            </xsl:if>

            <xsl:if test="interface[not(@role)]">
              <strong>
                Additional non-standard <xsl:value-of select="$name"/> interfaces:
              </strong>
              <ul>
                <xsl:apply-templates select="interface[not(@role)]" />
              </ul>
            </xsl:if>

            <xsl:apply-templates select="." mode="extendedCapability"/>

          </div>
        </td>
      </tr>
    </table>

  </xsl:template>



  <!--
     -  the template for rendering a capability's extended metadata.  This
     -  default implementation does nothing.  It can be overridden, however,
     -  for specific types of capabilities.
     -->
   <xsl:template match="capability" mode="extendedCapability"/>

   <!--
     -  render an unrecognized interface.  (Recognized interfaces have there 
     -  own templates.)
     -->
   <xsl:template match="interface">
      <xsl:param name="type">
         <xsl:choose>
            <xsl:when test="contains(@xsi:type,':')">
               <xsl:value-of select="substring-after(@xsi:type,':')"/>
            </xsl:when>
            <xsl:when test="not(@xsi:type)">unspecified</xsl:when>
            <xsl:otherwise><xsl:value-of select="@xsi:type"/></xsl:otherwise>
         </xsl:choose>
      </xsl:param>

      <li>
         <strong>Type: </strong>
         <xsl:value-of select="$type"/> <br />
         <xsl:if test="@version">
           <strong>Supported Version: </strong>
           <xsl:value-of select="@version"/> <br />
         </xsl:if>
         <xsl:if test="@role">
           <strong>Role: </strong>
           <xsl:value-of select="@role"/> <br />
         </xsl:if>
         <i><xsl:value-of select="accessURL[1]"/></i>
         <xsl:for-each select="accessURL[position() > 1]">
            <br /><xsl:text>Alternate: </xsl:text>
            <i><xsl:value-of select="."/></i>
         </xsl:for-each>
      </li>
   </xsl:template>

   <!--
     -  Describe a WebBrowser interface
     -->
   <xsl:template match="interface[@xsi:type='WebBrowser' or 
                                  contains(@xsi:type,':WebBrowser')]">
      <li> <strong>Interactive web page: </strong> 
           <i>
           <a href="{normalize-space(accessURL[1])}"><xsl:value-of select="normalize-space(accessURL[1])"/></a>
           </i>
           <xsl:for-each select="accessURL[position() > 1]">
              <br /><xsl:text>Alternate: </xsl:text>
              <i>
              <a href="{normalize-space(.)}">
              <xsl:value-of select="normalize-space(.)"/></a>
              </i>
           </xsl:for-each>
      </li>
   </xsl:template>

   <!--
     -  Describe a WebBrowser interface
     -->
   <xsl:template match="interface[@xsi:type='WebService' or 
                                  contains(@xsi:type,':WebService')]">
      <li> <strong>SOAP-based Web Service: </strong> 
           <i>
           <xsl:value-of select="accessURL[1]"/>
           </i>
           <span style="visibility: hidden">X</span>
           <xsl:choose>
              <xsl:when test="wsdlURL">
                 <xsl:text> (</xsl:text>
                 <a href="{normalize-space(wsdlURL[1])}">WSDL</a>
                 <xsl:text>)</xsl:text>
              </xsl:when>
              <xsl:otherwise>
                 <xsl:text> (</xsl:text>
                 <a href="{normalize-space(accessURL[1])}?wsdl">WSDL</a>
                 <xsl:text>)</xsl:text>
              </xsl:otherwise>
           </xsl:choose>
           <xsl:for-each select="accessURL[position() > 1]">
              <br /><xsl:text>Alternate: </xsl:text>
              <i><xsl:value-of select="normalize-space(.)"/></i>
           </xsl:for-each>
      </li>
   </xsl:template>

   <!--
     -  Describe a WebBrowser interface
     -->
   <xsl:template match="interface[@xsi:type='ParamHTTP' or 
                                  contains(@xsi:type,':ParamHTTP')]">
      <xsl:variable name="sublist" select="boolean(@role or @version or param)"/>

      <li> <strong>URL-based interface: </strong> 
           <i>
           <xsl:value-of select="normalize-space(accessURL[1])"/>
           </i>
           <xsl:for-each select="accessURL[position() > 1]">
              <br /><xsl:text>Alternate: </xsl:text>
              <i>
              <a href="{normalize-space(.)}">
              <xsl:value-of select="normalize-space(.)"/></a>
              </i>
           </xsl:for-each>

           <xsl:if test="boolean($sublist)">
              <ul>
                <xsl:if test="@version">
                  <li> <strong>Supported Version: </strong>
                  <xsl:value-of select="@version"/> </li>
                </xsl:if>
                <xsl:if test="@role">
                  <li> <strong>Role: </strong>
                  <xsl:value-of select="@role"/> </li>
                </xsl:if>
                <xsl:if test="param">
                  <li> <strong>This interface supports the following 
                       arguments: </strong>
                       <dl><xsl:apply-templates select="param" mode="list"/></dl>
                  </li>
                </xsl:if>
              </ul>              
           </xsl:if>
      </li>
   </xsl:template>

   <xsl:template match="param" mode="list">
      <dt><code><xsl:value-of select="name"/></code>
          <xsl:if test="@use">
             <xsl:text> [</xsl:text>
             <xsl:value-of select="@use"/>
             <xsl:text>]</xsl:text>
          </xsl:if>
      </dt>
      <dd>
          <xsl:value-of select="description"/>
          <xsl:if test="dataType">
             <br /> <strong>Value Type: </strong>
             <xsl:value-of select="dataType"/>
          </xsl:if>
          <xsl:if test="unit">
             <br /> <strong>Unit: </strong>
             <xsl:value-of select="unit"/>
          </xsl:if>
          <xsl:if test="ucd">
             <br /> <strong>Unified Content Descriptor (UCD): </strong>
             <xsl:value-of select="ucd"/>
          </xsl:if>
          <xsl:if test="utype">
             <br /> <strong>Model Type Descriptor (Utype): </strong>
             <xsl:value-of select="utype"/>
          </xsl:if>
      </dd>
   </xsl:template>

</xsl:stylesheet>
