<%@ Page Language="C#" AutoEventWireup="true" CodeFile="keywordsearch.aspx.cs" Inherits="keywordsearch" Debug="true" validateRequest="false"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml"> 
<head>
<title>VAO Directory Keyword Search</title>
<link href="http://www.us-vo.org/app_templates/usvo_template.css" type="text/css" rel="stylesheet"/>
<style type="text/css">
   .section	{MARGIN-TOP: 2px; MARGIN-BOTTOM: 2px; BACKGROUND-COLOR:#DDDDDD;}
   .searchbox	{WIDTH: 85px;}
  .nvoapptitle  { color: #243a6d; 
                  font-weight: bolder; font-size: 14pt;
                  text-align: center; margin-left: 2px; margin-right: 2px; }
  p 		{MARGIN-TOP: 0px; MARGIN-BOTTOM: 0px;}

  #header	{POSITION: absolute; TOP:  0px; LEFT:  2px;}
  #search	{POSITION: absolute; TOP: 26px; LEFT:648px; WIDTH:160PX;}
  #navibar	{MARGIN-TOP: 0px;POSITION: absolute; TOP: 96px; LEFT:154px; 
                 WIDTH: 450px; PADDING:0px }
  #menubar	{POSITION: absolute; TOP: 96px; LEFT:  0px; WIDTH: 132px; 
                 PADDING:4px; BACKGROUND-COLOR:#EEEEEE;}
  			 
  #features	{POSITION: absolute; TOP:128px; LEFT:646px; WIDTH: 132px;}
  #main		{POSITION: absolute; TOP:128px; LEFT:153px; WIDTH: 641px; 
                 PADDING:8px;}
  .th           {font-weight:800; background-color:#DDDDEF;}
  .td           {background-color:#EEEEEE;}
  p,tr,td,dt,dd {FONT-WEIGHT: normal; FONT-SIZE: 9pt; FONT-STYLE: normal; }
</style>
  	<script type="text/javascript" src="./js/sarissa.js"></script>
    <script type="text/javascript" src="./js/statemanager.js"></script>
	<script type="text/javascript" src="./js/regview.js"></script>
  	<script type="text/javascript" src="./js/filter.js"></script>
  	<script type="text/javascript" src="./js/fsm.js"></script>
  	<script type="text/javascript" src="./js/query.js"></script>
    <link rel="stylesheet" type="text/css" href="./js/regview.css"/>
    <link rel="stylesheet" type="text/css" href="./js/styles.css"/>
<style type="text/css">
h1,h2,h3,h4,h5,h6,p, body, tr, td, ul, li {FONT-FAMILY: arial,helvetica,sans-serif}

UNKNOWN {
	BACKGROUND-COLOR: white; MARGIN: 0.12in; WORD-SPACING: 1em; COLOR: black; LETTER-SPACING: 0.1em
}
H1 {
	FONT-WEIGHT: 700; FONT-SIZE: 18pt; COLOR: #003366; FONT-STYLE: normal; 
}
H1.custom {
	FONT-WEIGHT: normal; FONT-SIZE: 36pt; FONT-STYLE: normal; 
}
H1.custom2 {
	FONT-WEIGHT: normal; FONT-SIZE: 42pt; FONT-STYLE: normal; 
}
H1.custom3 {
	FONT-WEIGHT: normal; FONT-SIZE: 6pt; FONT-STYLE: normal; 
}
H2 {
	FONT-WEIGHT: 700; FONT-SIZE: 16pt; COLOR: #003366; FONT-STYLE: normal; 
}
H3 {
	FONT-WEIGHT: 700; FONT-SIZE: 14pt; COLOR: #003366; FONT-STYLE: normal; 
}
H4 {
	FONT-WEIGHT: 700; FONT-SIZE: 12pt; COLOR: #003366; FONT-STYLE: normal; 
}
H5 {
	FONT-WEIGHT: 700; FONT-SIZE: 10pt; COLOR: #003366; FONT-STYLE: normal; 
}
H6 {
	FONT-WEIGHT: 700; FONT-SIZE: 8pt; COLOR: #003366; FONT-STYLE: normal; 
}
DIV {
	FONT-WEIGHT: normal; FONT-SIZE: 10pt; FONT-STYLE: normal; 
}
SPAN {
	FONT-WEIGHT: normal; FONT-SIZE: 10pt; FONT-STYLE: normal; 
}
P,TR, TD {
	FONT-WEIGHT: normal; FONT-SIZE: 10pt; FONT-STYLE: normal; 
}
HR {
	COLOR: #ffcc00
}

LI {
	MARGIN-TOP:0px;  MARGIN-BOTTOM:0px; 
}
UL, P {
	MARGIN-TOP:4px;  MARGIN-BOTTOM:4px;
}
H3, H4 {
	MARGIN-TOP:8px;  MARGIN-BOTTOM:6px;
}
  .tiny		{FONT-SIZE: 7pt;}
  .tinylink	{FONT-SIZE: 7pt; COLOR:#aaaaff;}
  .navlink	{MARGIN-TOP: 0px; MARGIN-BOTTOM: 1px; FONT-SIZE:9pt; 
                 BACKGROUND-COLOR:#6ba5d7; text-align: center}
  .navlink A	{ TEXT-DECORATION:none;COLOR:#FFFFFF;}
  .navlink A:hover { BACKGROUND-COLOR:#6ba5d7; TEXT-DECORATION:none; COLOR: #99FFCC; }
  .helplink	{MARGIN-TOP: 0px; MARGIN-BOTTOM: 1px; FONT-SIZE:9pt; 
                 BACKGROUND-COLOR:#24386d; text-align: center}
  .helplink A	{TEXT-DECORATION:none;COLOR:#FFFFFF;}
  .helplink A:hover { COLOR: #99FFCC; }
  .nvolink	{MARGIN-TOP: 0px; MARGIN-BOTTOM: 1px; FONT-SIZE:9pt; 
                 PADDING-LEFT: 2px;
                 PADDING-RIGHT: 2px; }
  .nvolink A	{TEXT-DECORATION:none;COLOR:#6ba5d7;}
  .nvolink A:hover { COLOR: #99FFCC; }
  .nvolinktiny A {FONT-SIZE:7PT;} 
  .section	{MARGIN-TOP: 2px; MARGIN-BOTTOM: 2px; BACKGROUND-COLOR:#DDDDDD;}
   .searchbox	{WIDTH: 85px;}
  .nvoapptitle  { color: #24386d; 
                  font-weight: bolder; font-size: 14pt;
                  text-align: center; margin-left: 2px; margin-right: 2px; }
  p 		{MARGIN-TOP: 0px; MARGIN-BOTTOM: 0px;}

  #header	{POSITION: absolute; TOP:  0px; LEFT:  2px;}
  #search	{POSITION: absolute; TOP: 26px; LEFT:648px; WIDTH:160PX;}
  #navibar	{MARGIN-TOP: 0px;POSITION: absolute; TOP: 96px; LEFT:154px; 
                 WIDTH: 450px; PADDING:0px }
  #menubar	{POSITION: absolute; TOP: 96px; LEFT:  0px; WIDTH: 132px; 
                 PADDING:4px; BACKGROUND-COLOR:#EEEEEE;}
  			 
  #features	{POSITION: absolute; TOP:128px; LEFT:646px; WIDTH: 132px;}
  #main		{POSITION: absolute; TOP:128px; LEFT:153px; WIDTH: 641px; 
                 PADDING:8px;}
  .th           {font-weight:800; background-color:#DDDDEF;}
  .td           {background-color:#EEEEEE;}
  P,TR,TD,DT,DD {FONT-WEIGHT: normal; FONT-SIZE: 9pt; FONT-STYLE: normal; }
  </style>
</head>
<body><table width="100%" border="0" cellpadding="0" cellspacing="0">
<tr>
    <td width="112" height="32" align="center" valign="top"><a
href="http://www.us-vo.org" class="nvolink" target="_top"><img
        src=".images/VAO_logo_100.png" border="0"/></a><span class="nvolink"><a
href="http://www.usvao.org/" target="_top">Virtual Astronomical Observatory</a></span></td>
    <td width="50" align="center" valign="middle"><img src="images/Directory50.png" alt="ICON" width="50" height="50"></td>
    <td valign="top"><table  width="100%" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td width="2" height="30" bgcolor="white"></td>
<td width="678" height="39" align="center"
          valign="middle"
          background="images/stars.jpg"
          bgcolor="#CFE5FC"  class="nvoapptitle" style="background-repeat: repeat-y;"><span class="nvoapptitle" style="background-repeat: repeat-y;">VAO Directory</span></td>
        <td bgcolor="white" width="2"></td>
      </tr>
      <tr>
        <td bgcolor="white" width="2"></td>
        <td bgcolor="white" width="2"></td>
      </tr>
      <tr>
        <td align="center" valign="top" colspan="3"><table cellspacing="2" cellpadding="0" border="0" width="100%"
                style="margin: 0pt;">
          <tr>
             <!-- the local links -->
        <td class="navlink"><a href="http://www.usvao.org/">VAO Home</a></td>
        <td class="navlink"><a href="keywordsearch.aspx">Search</a></td>
        <td class="navlink"><a href="xpublish.aspx">Publish</a></td>
        <td class="navlink"><a href="riws.aspx">Developers</a></td>
        <td class="navlink"><a href="helpnew.aspx">Help</a></td>
        <td class="helplink"><a href="http://www.us-vo.org/feedback/">Contact Us</a></td>
           </tr>
         </table>
         </td>
       </tr>
    </table>
    </td>
    <td width="140" align="center" valign="top">
      <!-- local logo and link -->
      <span class="tiny">Hosted By</span><br/><a href="http://www.stsci.edu"><img height="54"
         src="images/hst.gif"
         alt="STScI Home" border="0"/></a>
      <br />
      <span class="nvolink"><span class="tiny"><a
            href="http://www.stsci.edu">Space Telescope<br/> Science 
      Institute</a> </span></span>
    </td>
   </tr>

</table>

<!-- =======================================================================
  -  Page Content -->
<!--  -  ======================================================================= -->

<!--hr noshade="noshade" /-->
<table width="100%" border="0">
    <tr><td width="112"></td><td width="50"></td><td>
    
        <h3>Find Astronomical Data Resources</h3></td></tr>
            <tr><td width="112"></td><td width="50"></td><td>
            <br /><p>
                Enter terms in the text box that describe the type of data you are looking for.&nbsp;
                Results will show catalogs and data collections that have these terms as part of
                their descriptions.&nbsp;</p><br />
</td><td></td></tr> 
    <tr><td width="112"></td><td width="50"></td><td>
    
            <form id="outputform" method="post" action="savexml.aspx" style="margin: 0pt; display: inline;">
                <input id="save" type="hidden" name="save" />
                <input id="format" type="hidden" name="format" />
            </form>
            <form id="SaveResourceURLForm" method="post" action="" style="margin: 0pt; display: inline;">
                <input id="resourceListForURL" type="hidden" name="resourceListForURL" />
                <input id="resourceListFilename" type="hidden" name="resourceListFilename" />
            </form> 
                                                                   
            <form method="get" name="searchForm" onsubmit="return rd.setView();" action="">   
                <input name="query_string" id="sterm" size="50" maxlength="500" value="" type="text"/>
	            <br/>
	            <input type="submit" class="submit" name=".submit" value="Search" />
	            <input type="reset" class="reset" name=".reset" value="Reset" onclick="return rd.clearState();" />&nbsp;&nbsp;<a href="advancedsearch.aspx">Advanced</a>
	            <div class="searchnote">
		            Examples: 
		            <a onclick="return insertTerm(this);">quasar</a>, 
		            <a onclick="return insertTerm(this);">AGN</a>, 
		            <a onclick="return insertTerm(this);">binary stars</a>, 
		            <a onclick="return insertTerm(this);">Chandra</a>, 
		            <a onclick="return insertTerm(this);">GALEX</a>, 
		            <a onclick="return insertTerm(this);">far ultraviolet</a>
	            </div>
	            <div >
		            Use <a href="http://heasarc.gsfc.nasa.gov/cgi-bin/vo/datascope/init.pl"> DataScope </a> to search and view data for a particular object or position.
	            </div>
	            <div >
		            Use the <a href="http://irsa.ipac.caltech.edu/cgi-bin/VOInventory/nph-voInventory"> Inventory </a>  to find catalogs having entries for a given position or list of positions.
	            </div>
            </form>
            
            <form id="Interop" method="post" action="" style="margin: 0pt; display: inline;" runat="server">
                <table width="100%"><tr><td align="right">
                <input type="hidden" id="sources" name="sources" value="" />
                <input type="hidden" id="sourcesURL" name="sourcesURL" value="" />
                <input type="hidden" id="RunID" name="RunID" value="" />
                <input type="hidden" id="referralURL" name="referralURL" value="" />
                <input type="hidden" id="resources" name="resources" value="" />
                <input type="hidden" id="resourcesURL" name="resourcesURL" value="" />
                <input type="hidden" id="toolName" name="toolName" value="findResources" /> 
                <input type="hidden" id="benchID" name="benchID" value="" />
                </td><td></td></tr></table>
            </form> 
    </td><td width="140"></td></tr> 
</table>
<div id="output">
Results will appear here.
</div>

<br />

<!-- =======================================================================
  -  End Page Content
  -  ======================================================================= -->

<hr align="left" noshade=""/>
    <table width="100%"  border="0" align="center" cellpadding="4" cellspacing="0">
  <tr align="center" valign="top">
    
    <td width="16%" valign="top"><div align="center" class="style10"><a href="http://www.nsf.gov"><img src="http://www.us-vo.org/images/nsf_logo.gif" alt="NSF HOME" width="50" height="50" border="0"/></a><a href="http://www.nasa.gov"><img src="http://www.us-vo.org/images/nasa_logo_sm.gif" alt="NASA HOME" width="50" height="47" border="0"/></a></div></td>
    <td width="76%"><div align="center">
        <p class="style10"> Developed with the support of the <a href="http://www.nsf.gov">National Science Foundation</a> <br/>

          under Cooperative Agreement AST0122449 with the Johns Hopkins University <br/>
          The VAO is a member of the <a href="http://www.ivoa.net">International Virtual Observatory Alliance</a></p>
        <p class="style10">This VAO Application is hosted by the <a href="http://www.stsci.edu">Space Telescope Science Institute</a></p>
    </div></td>
    <td width="8%"><div align="center"><span class="tiny">Member<br/>
    </span><a href="http://www.ivoa.net"><img src="images/ivoa_small.jpg" alt="ivoa logo" width="68" height="39" border="0" align="top"/></a></div></td>

    <td width="8%"><span class="nvolink"><span class="tiny"><a href="contactus.aspx">Meet the Developers</a></span><br/>
    <img src="http://www.us-vo.org/images/bee_hammer.gif" alt="MEET THE DEVELOPERS" width="50" border="0"/></span></td>
  </tr>
</table>
</body> </html>
