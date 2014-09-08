<%@ Page language="c#" %>
<%@ Import Namespace="System.Configuration"%>
<%@ Import Namespace="System.IO"%>

<html>
<HEAD>

<%
	string url = Request.Url.GetLeftPart(System.UriPartial.Authority)+Request.ApplicationPath+"/";
	string path = Request.Params["path"];
	//Response.Write("<h1>"+url+"</h1>");
	
	string message = Request.Params["message"];
	if (null == message)  message = "NVO Education WebSite";
	
	string leftMenu = Request.Params["leftMenu"];
	
	string bgcolor = Request.Params["bgcolor"];
	string displayTitle = Request.Params["displayTitle"];	
	string select = Request.Params["selected"];	
	//Response.Write("Header displayTitle: " + displayTitle + " Selected: " + select);
	
%>	
<!--  <LINK REL="SHORTCUT ICON" HREF="http://test.virtualobservatory.org/scnvo.ico">-->
  <LINK REL="SHORTCUT ICON" HREF="<%=url%>/scnvo.ico">

	<title><%=message%></title>

	<LINK href="<%=url%>web/styles.css" rel="stylesheet">


</HEAD>

<body bgcolor="#1C376C">

<a name="top"></a>

<!-- outermosst table exists to keep all the text on a white background, above the blue background of the rest of the page -->
<table width="760" border="0" cellspacing="0" cellpadding="0" bgcolor="#FFFFFF">
  <tr>
    <td>
       <!-- beginning of stuff at top of page -->
       <table width="760" border="0" cellspacing="0" cellpadding="0">
         <tr>
           <td width="144" height="131" valign="top" bgcolor="#557399"><img src="<%=url%>web/images/graphic.1.1.gif"></td>
           <td width="115" valign="top"><img src="<%=url%>web/images/graphic.1.2.gif"></td>
           <td width="501" height="99" valign="top" bgcolor="#FFFFFF">
             <!--<table width="100%" border="0" cellspacing="0" cellpadding="0" bgcolor="#FFFFFF">-->
             <table width="501" border="0" cellspacing="0" cellpadding="0" bgcolor="#FFFFFF">  
               <tr>
  	             <td height="69" align="right" valign="bottom" background="<%=url%>web/images/graphic.1.3.1.jpg">
		           <table width="501" border="0" cellspacing="0" cellpadding="0">
  		             <tr>
		  	           <td width="376"><img src="<%=url%>web/images/spacer.gif" width="371" height="1"></td>
  	    		       <td width="125">
				         <table width="125" border="0" cellspacing="0" cellpadding="0">
  				           <tr>
  				             <td><span class="style4">STScI/JHU <br>Registry </span></td>
  				           </tr>
				           <tr>
				             <td><img src="<%=url%>web/images/spacer.gif" width="2" height="4"></td>
				           </tr>
				         </table>
				       </td>
			         </tr>
			         <tr>
			           <td colspan="2" align="right">
			           
			           <!-- beginning of navigation bar -->
			           <!-- width for controlling tab spacing at top of header -->
         	             <table width="376" border="0" cellspacing="0" cellpadding="0">
				           <tr>		           
				             <td width="146">
  				               <table border="0" cellspacing="0" cellpadding="0">
				                 <tr>
				                   <td width="5" height="20"><img src="<%if (select=="home") {Response.Write(url+"web/images/left.tabcurve.on.gif");} else {Response.Write(url+"web/images/left.tabcurve.off.gif");}%>" width="5" height="20"></td>
				                   <td width="78" align="center" class="style2" background="<%if (select=="home") {Response.Write(url+"web/images/tab.on.gif");} else {Response.Write(url+"web/images/tab.off.gif");}%>">
				                     <table border="0" cellspacing="0" cellpadding="0">
				                       <tr>
				                         <td width="10" height="1"><img src="<%=url%>web/images/spacer.gif" width="10" height="1"></td>
				                         <td><a href="<%=url%>index.aspx" class="navibutton">HOME</a></td>
				                         <td width="10" height="1"><img src="<%=url%>web/images/spacer.gif" width="10" height="1"></td>				                         
				                       </tr>
				                     </table>
				                   </td>
				                   <td width="5" height="20"><img src="<%if (select=="home") {Response.Write(url+"web/images/right.tabcurve.on.gif");} else {Response.Write(url+"web/images/right.tabcurve.off.gif");}%>" width="5" height="20"></td>
							     </tr>
							   </table>
							 </td>
							 
				             <td width="202">
  				               <table border="0" cellspacing="0" cellpadding="0">
				                 <tr>
				                   <td width="5" height="20"><img src="<%if (select=="query") {Response.Write(url+"web/images/left.tabcurve.on.gif");} else {Response.Write(url+"web/images/left.tabcurve.off.gif");}%>" width="5" height="20"></td>
				                   <td width="78" align="center" class="style2" background="<%if (select=="query") {Response.Write(url+"web/images/tab.on.gif");} else {Response.Write(url+"web/images/tab.off.gif");}%>">
				                     <table border="0" cellspacing="0" cellpadding="0">
				                       <tr>
				                         <td width="10" height="1"><img src="<%=url%>web/images/spacer.gif" width="10" height="1"></td>
				                         <td><a href="<%=url%>queryregistry.aspx?startRes=-1" span class="navibutton">QUERY</a></td>
				                         <td width="10" height="1"><img src="<%=url%>web/images/spacer.gif" width="10" height="1"></td>				                         
				                       </tr>
				                     </table>
				                   </td>
				                   <td width="5" height="20"><img src="<%if (select=="query") {Response.Write(url+"web/images/right.tabcurve.on.gif");} else {Response.Write(url+"web/images/right.tabcurve.off.gif");}%>" width="5" height="20"></td>
							     </tr>
							   </table>
							 </td>
							 
				             <td width="167">
  				               <table border="0" cellspacing="0" cellpadding="0">
				                 <tr>
				                   <td width="5" height="20"><img src="<%if (select=="publish") {Response.Write(url+"web/images/left.tabcurve.on.gif");} else {Response.Write(url+"web/images/left.tabcurve.off.gif");}%>" width="5" height="20"></td>
				                   <td width="78" align="center" class="style2" background="<%if (select=="publish") {Response.Write(url+"web/images/tab.on.gif");} else {Response.Write(url+"web/images/tab.off.gif");}%>">
				                     <table border="0" cellspacing="0" cellpadding="0">
				                       <tr>
				                         <td width="10" height="1"><img src="<%=url%>web/images/spacer.gif" width="10" height="1"></td>
				                         <td><a href="<%=url%>publish.aspx" class="navibutton">PUBLISH</a></td>
				                         <td width="10" height="1"><img src="<%=url%>web/images/spacer.gif" width="10" height="1"></td>				                         
				                       </tr>
				                     </table>
				                   </td>
				                   <td width="5" height="20"><img src="<%if (select=="publish") {Response.Write(url+"web/images/right.tabcurve.on.gif");} else {Response.Write(url+"web/images/right.tabcurve.off.gif");}%>" width="5" height="20"></td>
							     </tr>
							   </table>
							 </td>
							 
				             <td width="239">
  				               <table border="0" cellspacing="0" cellpadding="0">
				                 <tr>
				                   <td width="5" height="20"><img src="<%if (select=="develop") {Response.Write(url+"web/images/left.tabcurve.on.gif");} else {Response.Write(url+"web/images/left.tabcurve.off.gif");}%>" width="5" height="20"></td>
				                   <td width="78" align="center" class="style2" background="<%if (select=="develop") {Response.Write(url+"web/images/tab.on.gif");} else {Response.Write(url+"web/images/tab.off.gif");}%>">
				                     <table border="0" cellspacing="0" cellpadding="0">
				                       <tr>
				                         <td width="10" height="1"><img src="<%=url%>web/images/spacer.gif" width="10" height="1"></td>
				                         <td><a href="<%=url%>develop.aspx" class="navibutton">DEVELOPER</a></td>
				                         <td width="10" height="1"><img src="<%=url%>web/images/spacer.gif" width="10" height="1"></td>				                         
				                       </tr>
				                     </table>
				                   </td>
				                   <td width="5" height="20"><img src="<%if (select=="develop") {Response.Write(url+"web/images/right.tabcurve.on.gif");} else {Response.Write(url+"web/images/right.tabcurve.off.gif");}%>" width="5" height="20"></td>
							     </tr>
							   </table>
							 </td>
							 
				             <td width="208">
  				               <table border="0" cellspacing="0" cellpadding="0">
				                 <tr>
				                   <td width="5" height="20"><img src="<%if (select=="contents") {Response.Write(url+"web/images/left.tabcurve.on.gif");} else {Response.Write(url+"web/images/left.tabcurve.off.gif");}%>" width="5" height="20"></td>
				                   <td width="78" align="center" class="style2" background="<%if (select=="contents") {Response.Write(url+"web/images/tab.on.gif");} else {Response.Write(url+"web/images/tab.off.gif");}%>">
				                     <table border="0" cellspacing="0" cellpadding="0">
				                       <tr>
				                         <td width="10" height="1"><img src="<%=url%>web/images/spacer.gif" width="10" height="1"></td>
				                         <td><a href="<%=url%>summary.aspx/" class="navibutton">CONTENTS</a></td>
				                         <td width="10" height="1"><img src="<%=url%>web/images/spacer.gif" width="10" height="1"></td>				                         
				                       </tr>
				                     </table>
				                   </td>
				                   <td width="5" height="20"><img src="<%if (select=="contents") {Response.Write(url+"web/images/right.tabcurve.on.gif");} else {Response.Write(url+"web/images/right.tabcurve.off.gif");}%>" width="5" height="20"></td>
							     </tr>
							   </table>
							 </td>
							 
							 
					       </tr>
				         </table>
				         <!-- end of navigation bar -->
				         
				       </td>
			         </tr>
  	               </table>
                 </td>
               </tr>
		       <tr>
		         <td height="5" valign="top">
		           <table width="501" border="0" cellspacing="0" cellpadding="0">
			         <tr>
			           <td bgcolor="f2c839"><img src="<%=url%>web/images/spacer.gif" width="3" height="3"></td>
			         </tr>
			         <tr>
			           <td bgcolor="010133"><img src="<%=url%>web/images/spacer.gif" width="1" height="1"></td>
			         </tr>
   	               </table>
                 </td>
               </tr>
               <tr>
                 <td height="55" valign=middle>
                   <!--<span class="title"><%=displayTitle%></span>-->
                 </td>
               </tr>
             </table>
           </td>
<!-- blank table cell that follows is there only to fix gap problem in Firefox -->
			<td width = 600 ></td>
         </tr>


<!-- end of stuff at top of page -->

   <!-- this table holds the left panel and the main panel with all the content -->
          <!-- this table holds the links on the left side of the page -->
   	     <tr> 
           <td width="144" bgcolor="#557399" valign="top">
    	     <table width="100%"  border="0" cellspacing="0" cellpadding="0">
           	   <tr>
                 <td><img src="<%=url%>web/images/spacer.gif" width="1" height="20"></td>
		       </tr>
		       <tr>
		  	     <td>
   			       <table width="100%"  border="0" cellspacing="0" cellpadding="0">
			         <tr>
                       <td width="7%"><img src="../web/images/spacer.gif" width="20" height="1"></td>
                       <td width="93%"><a href="http://us-vo.org/"><span class="style6">NVO Home</span></a></td>
                     </tr>
                   </table>
                 </td>
               </tr>
               <tr>
                 <td><img src="../web/images/spacer.gif" width="1" height="10"></td>
               </tr>
               <tr>
		       <tr>
		  	     <td>
   			       <table width="100%"  border="0" cellspacing="0" cellpadding="0">
			         <tr>
                       <td width="7%"><img src="../web/images/spacer.gif" width="20" height="1"></td>
<!--                       <td width="93%"><a href="<%=url+path+"faq.aspx"%>"><span class="style6">FAQ</span></a></td>-->
                       <td width="93%"><a href="<%=url%>faq.aspx"><span class="style6">FAQ</span></a></td>
                     </tr>
                   </table>
                 </td>
               </tr>
               <tr>
                 <td><img src="../web/images/spacer.gif" width="1" height="10"></td>
               </tr>
               <tr>
				 <td align="left">
                   <table width="100%"  border="0" cellspacing="0" cellpadding="0">
                     <tr>
                       <td width="7%"><img src="../web/images/spacer.gif" width="20" height="1"></td>
                       <td width="93%"><a href="projects.aspx"><span class="style6">Projects</span></a></td>
                     </tr>
                   </table>
                 </td>
               </tr>
               <tr>
                 <td align="left"><img src="../web/images/spacer.gif" width="1" height="10"></td>
               </tr>
               <tr>
                 <td align="left">
                   <table width="100%"  border="0" cellspacing="0" cellpadding="0">
                     <tr>
                       <td width="7%"><img src="../web/images/spacer.gif" width="20" height="1"></td>
                       <td width="93%"><a href="http://www.ivoa.net/twiki/bin/view/IVOA/IvoaResReg"><span class="style6">IVOA WG</span></a></td>
                     </tr>
                   </table>
                 </td>
               </tr>
               <tr>
                 <td align="left"><img src="../web/images/spacer.gif" width="1" height="10"></td>
               </tr>
               <tr>
                 <td align="left">
                   <table width="100%"  border="0" cellspacing="0" cellpadding="0">
                     <tr>
                       <td width="7%"><img src="../web/images/spacer.gif" width="20" height="1"></td>
                       <td width="93%"><a href="<%=url%>help.aspx"><span class="style6">Help</span></a></td>
                     </tr>
                   </table>
                </td>
               </tr>
             </table>
           </td>

<!-- end of left menu -->

    <td colspan=3 width="616" bgcolor="#FFFFFF" valign="top">
       <!-- Page will go in this table !-->
	   <table border="0" cellspacing="0" cellpadding="6">
	     <tr>
	       <td>
<!-- main table gets closed in SkyFooter.aspx -->