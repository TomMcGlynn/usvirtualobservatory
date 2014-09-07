<%@ Page language="c#" %>
<%@ Import Namespace="System.Configuration"%>
<%@ Import Namespace="System.IO"%>

<html xmlns="http://www.w3.org/1999/xhtml"> 
<head>
<title>VAO Directory</title>
<link href="http://www.us-vo.org/app_templates/usvo_template.css" type="text/css" rel="stylesheet"/>
<style type="text/css">
    a:visited { color: #6ba5d7; }

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
</script>

</head>
<body>
<table width="100%" border="0" cellpadding="0" cellspacing="0">
<tr>
    <td width="112" height="32" align="center" valign="top"><a
href="http://www.usvao.org" class="nvolink" target="_top"><img
        src="./images/VAO_logo_100.png" border="0"/></a><span class="nvolink"><a
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
        <td class="navlink"><a href="http://vao.stsci.edu/publishing">Publish</a></td>
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
