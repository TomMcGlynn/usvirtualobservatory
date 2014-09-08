<%@ Page Language="C#" AutoEventWireup="true" CodeBehind="HarvestTable.aspx.cs" Inherits="ReportPages.HarvestTable" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">

<html xmlns="http://www.w3.org/1999/xhtml" >
<head id="Head1" runat="server">
    <title>Untitled Page</title>
    <link href="http://www.us-vo.org/usvo3.css" type="text/css" rel="stylesheet" />
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
</head>
<body>

<table width="100%" border="0" cellpadding="0" cellspacing="0">
<tr>
    <td width="112" height="32" align="center" valign="top"><a
href="http://www.us-vo.org" class="nvolink" target="_top"><img
        src="http://www.us-vo.org/images/NVO_100pixels.jpg" border="0"/></a><span class="nvolink"><a
href="http://www.us-vo.org/" target="_top">National Virtual Observatory</a></span></td>
    <td width="50" align="center" valign="middle"><img src="images/registry50.png" alt="ICON" width="50" height="50"></td>
    <td valign="top"><table  width="100%" border="0" cellpadding="0" cellspacing="0">
      <tr>
        <td width="2" height="30" bgcolor="white"></td>
<td width="678" height="39" align="center"
          valign="middle"
          background="images/stars.jpg"
          bgcolor="#CFE5FC"  class="nvoapptitle" style="background-repeat: repeat-y;"><span class="nvoapptitle" style="background-repeat: repeat-y;">NVO Registry</span></td>
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
        <td class="navlink"><a href="http://www.us-vo.org/">NVO Home</a></td>
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

<table>
<tr>
<td width="56"></td>
<td>
<br />
<p>Below you will find the most recent results from STScI's automated registry harvesting. This page is
automatically updated from the harvester's logging. The list of registries included comes from the <a href="http://rofr.ncsa.uiuc.edu/cgi-bin/rofrhello.py">Registry of Registries</a>
and is manually updated. If you maintain a listed registry and require selective 
harvesting of individual records or other administrative functionality, use the
 <a href="../registryadmin.asmx">Registry Administration</a> web service.</p>
<br /><br />
    <form id="form1" runat="server">
        <asp:Table ID="HarvesterTable" cellpadding="2" GridLines="Both" runat="server" />
        <br /><br />
        <asp:Label ID="TotalCount" runat="server" Text=""></asp:Label>
        <br />
        <asp:Label ID="HarvestedCount" runat="server"></asp:Label>
        <br /><br />
        <asp:Label ID="TotalResources" runat="server"></asp:Label>
    </form>
    <br />
   </td>
   <td width="60"></td> 
   </tr>
   </table>

<hr align="left" noshade=""/>
    <table width="100%"  border="0" align="center" cellpadding="4" cellspacing="0">
  <tr align="center" valign="top">
    
    <td width="16%" valign="top"><div align="center" class="style10"><a href="http://www.nsf.gov"><img src="http://www.us-vo.org/images/nsf_logo.gif" alt="NSF HOME" width="50" height="50" border="0"/></a><a href="http://www.nasa.gov"><img src="http://www.us-vo.org/images/nasa_logo_sm.gif" alt="NASA HOME" width="50" height="47" border="0"/></a></div></td>
    <td width="76%"><div align="center">
        <p class="style10"> Developed with the support of the <a href="http://www.nsf.gov">National Science Foundation</a> <br/>

          under Cooperative Agreement AST0122449 with the Johns Hopkins University <br/>
          The NVO is a member of the <a href="http://www.ivoa.net">International Virtual Observatory Alliance</a></p>
        <p class="style10">This NVO Application is hosted by the <a href="http://www.stsci.edu">Space Telescope Science Institute</a></p>
    </div></td>
    <td width="8%"><div align="center"><span class="tiny">Member<br/>
    </span><a href="http://www.ivoa.net"><img src="images/ivoa_small.jpg" alt="ivoa logo" width="68" height="39" border="0" align="top"/></a></div></td>

    <td width="8%"><span class="nvolink"><span class="tiny"><a href="contactus.aspx">Meet the Developers</a></span><br/>
    <img src="http://www.us-vo.org/images/bee_hammer.gif" alt="MEET THE DEVELOPERS" width="50" border="0"/></span></td>
  </tr>
</table>
    
</body>
</html>
