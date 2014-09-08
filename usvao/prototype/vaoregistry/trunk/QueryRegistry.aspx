<%@ Page language="c#" EnableSessionState="true" AutoEventWireup="false" Inherits="registry.QueryRegistry" %>
<!-- ORIGINAL before SkyHeader code used.
<%
Server.Execute("top.aspx?message=Query the Registry");
%>
-->
<html>
<HEAD>
<!--  BEGINNING of SkyHeader,  had to be included directly to this file because there
	  is a conflict with the Request object in the code behind this page.  Paramaters
	  can not be extracted with the Server.Execute 
// fill this with your details 
	string Title = "The Virtual Astronomical Observatory (VAO)";
	string author ="Gretchen Greene";
	string email ="greene@stsci.edu";
	string cvsRevision = "$Revision: 1.21 $";

	string path = "";

	string bgcolor = "#FF0000";
	string displayTitle = "asdf";
	string selected = "home";
	
	string Parameters = "message="	+	Title	+	"&"	+	"author="	+	author	+
		"&"	+	"email="	+	email	+	"&"	+	"cvsRevision=" + cvsRevision.Replace(":"," ")  +
		"&path=" + path + "&selected=" + selected +
		"&bgcolor=" + bgcolor + "&displayTitle=" +displayTitle;				

// will need to fix path if we have sub dirs ..

Server.Execute("web/SkyHeader.aspx" + "?" + Parameters);	  
-->
	  
<%
	string url = Request.Url.GetLeftPart(System.UriPartial.Authority)+Request.ApplicationPath+"/";
	string path = "";
	
	string message = "NVO STScI Registry Query";
	
	string leftMenu = Request.Params["leftMenu"];
	
	string bgcolor = "#FF0000";
	string displayTitle = "";	
	string select = "query";

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
           <td width="144" height="131" valign="top" bgcolor="#557399"><img src="<%=url%>web/images/graphic.1.1.gif" width="144" height="131"></td>
           <td width="115" valign="top"><img src="<%=url%>web/images/graphic.1.2.gif" width="115" height="107"></td>
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
                 </td>
               </tr>
             </table>
           </td>
<!-- blank table cell that follows is there only to fix gap problem in Firefox -->
           <td width=600 ></td>
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
                       <td width="93%"><a href="http://usvao.org/"><span class="style6">VAO Home</span></a></td>
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

			<form id="QueryRegistry" method="post" runat="server">
			<input type="hidden" name="startRes" value=-1/>
			<input type="hidden" name="groupFlag" value="true"/>
			<table>
				<%if (!advanced){%>
				<tr><td colspan=2>Enter word(s) separated by spaces (e.g cool star)</td></tr>
				<TR>
					
					<TH class="left" >Text Search</TH>
					<TD class="left">
						<input name="keywords" value="<%=keywords%>" size="50"/>
					</td>
					<td><input type="submit" value="GO!"/></td><td><table><tr>
						<td class="menu"><a class="menu" href="QueryRegistry.aspx?advanced=true&startRes=-1" >Advanced Search</a></td>
					</tr></table>  </td>
				</TR>			
				<TR>
					<TD class="left" ></TD>
					<TH class="left" >
						<asp:RadioButtonList id="rblANDOR" runat="server" RepeatDirection="Horizontal">
							<asp:ListItem Value="AND" Selected="True">AND</asp:ListItem>
							<asp:ListItem Value="OR Keywords">OR Keywords</asp:ListItem>
						</asp:RadioButtonList></TH>
					<td class="left"></td>
					
				</TR>
				
				<%} else {%>
				<tr>
					<TD class="left" style="WIDTH: 105px"></TD>
					<TD class="left">
						<P>Please enter your SQL predicate here using <a href="ListColumns.aspx" target="cols">VO MetaData </a>e.g.<br>
							ResourceType like 'CONE' and contentlevel like '%research%'
						</P>
					</TD>
				</tr>
				<tr>
					<TH class="left" style="WIDTH: 105px">
						<P>
							Custom Predicate</P>
						<P>-OR-</P>
					</TH>
					<td class="left"><textarea id="sql" rows="4" cols="80" runat="server"> ResourceType like 'CONE' and contentlevel like '%research%' </textarea>
					</td>
				</tr>
				<TR>
					<TH class="left" style="WIDTH: 105px">
						Choose</TH>
					<TD class="left"><asp:dropdownlist id="ddSQLList" runat="server" AutoPostBack="True" Height="37px" Width="664px">
							<asp:ListItem Value="Select ">Select Predicate from List</asp:ListItem>
							<asp:ListItem Value="ResourceType like '%CONE%' and subject like '%AGN%'">ResourceType like '%CONE%' and subject like '%AGN%'</asp:ListItem>
							<asp:ListItem Value="ResourceType like '%CONE%' and (subject like '%Quasar%' or subject like '%AGN%')">ResourceType like '%CONE%' and (subject like '%Quasar%'or sublect like '%AGN%')</asp:ListItem>
							<asp:ListItem Value="ResourceType like '%SkyNode%'">ResourceType like '%SkyNode%'</asp:ListItem>
							<asp:ListItem Value="ResourceType like '%SIAP%'">ResourceType like '%SIAP%'</asp:ListItem>
							<asp:ListItem Value="ResourceType like '%CONE%' and harvestedFrom like '%heasarc%'">ResourceType like '%CONE%' and harvestedFrom like '%heasarc%'</asp:ListItem>
							<asp:ListItem Value="Title like '%2MASS%'">Title like '%2MASS%'</asp:ListItem>
						</asp:dropdownlist></TD>
				</TR>
				<tr>
					<TD class="left" ></TD>
					<td class="left"><table width="100%"><tr>
						<td class="left"><asp:button id="Button1" runat="server" Text="Execute Query"></asp:button>
						</td>
					<td class="right"><table><tr>
						<td class="menu"><a class="menu" href="QueryRegistry.aspx?startRes=-1" >Text Search</a></td>
					</tr></table></td>
					</tr></table></td>				
				</tr>
				
				<%}%>
			</table>

			<% if (null != ds) {	
				System.Data.DataRow dr= null;	
			%>
			
			<% if (null != groupDs) { 
			    if (groupDs.Tables[0].Rows.Count ==1) {
			    %>
			    <b>Only one ResourceType <a title="<%=groupDs.Tables[0].Rows[0][2]%>" ><i> (<%=groupDs.Tables[0].Rows[0][0]%>)</i></a> found.</b>

			    <%
			    } else {
			    %>
			    <b>ResourceType(count) - click to go to this subset - mouseover for more info.</b>
			<table  border="1"  bordercolor="#6BA5D7" cellspacing="2" cellpadding="2">
				<tr >
				    <%
					for( int r =0; r < groupDs.Tables[0].Rows.Count; r++) {
					dr = groupDs.Tables[0].Rows[r];
					%>
					<td >
						<a class ="small" title="<%=dr[2]%>" 
						href="QueryRegistry.aspx?startres=-1&<%=advanced?"&advanced=true":""%>&sql=<%=Server.UrlEncode(predicate)%> and tag like '<%=dr[0]%>'">
						<%=dr[0]%><br>(<%=dr[1]%>)
						</a>
					</td>
					<%}%>
				</tr>
			</table>	

			<%}}%>
			<br>		
			<table><tr>
			<th class="filled"><%=totalRes%> resources. </th> 
		
			<td class="menusmall"><%if (startRes > 0){%><a class="menusmall" href="QueryRegistry.aspx?startRes=<%=prevRes%><%=advanced?"&advanced=true":""%>">Prev<<</a><%}%></td>
			<td>Showing <%=startRes+1%> to <%=endRes%>.</td> 
			<td class="menusmall"><%if (endRes < totalRes){%><a class="menusmall" href="QueryRegistry.aspx?startRes=<%=endRes%><%=advanced?"&advanced=true":""%>">>>Next</a><%}%></td>
			</tr></table>

			<table border="1" class="filled" bordercolor="#6BA5D7" width ="800">
			<tr  ><th class="filled"> Actions </th><th class="filledcen" width=600>Title / Description</th>
			<th class="filled" width="20%">Subject</th><th class="filled" width="10%">ResourceType</th></tr>
			<%  for (int r =startRes; r < endRes; r++){
				try{
					dr = ds.Tables[0].Rows[r];
				}
				catch(Exception e){

				throw new Exception("totalRes: " + totalRes + " startRes: " + startRes + " endRes: " + endRes);
				}
				
			%>	
		
			<tr><td valign="top"><table >
					<tr><th class="menusmall"><a href="UpdateRegistry.aspx?SearchIdentifier=<%=Server.UrlEncode((string)dr["Identifier"])%>&ro=t" class="menusmall">View</a></th> 
					</tr><tr><th class="menusmall"><a target ="xml" href="registry.asmx/QueryVOResource?predicate=identifier%3D%27<%=Server.UrlEncode((string)dr["Identifier"])%>%27" class="menusmall">XML</a></th> 
					</tr><tr><th class="menusmall"><a href="UpdateRegistry.aspx?SearchIdentifier=<%=Server.UrlEncode((string)dr["Identifier"])%>" class="menusmall">Edit</a></th> 
					</tr>
					<tr><th class="menusmall"><a href="UpdateRegistry.aspx?InsertMode=true&SearchIdentifier=<%=Server.UrlEncode((string)dr["Identifier"])%>" class="menusmall">Copy</a></th> </tr>
				<%  String st = (String)dr["Tag"];
					if (st.ToUpper().StartsWith ("CONE") ||
							st.StartsWith ("SIA") || st.StartsWith ("SSA")) {%>
					<tr><th class="menusmall"><a href="voForm.aspx?ResourceType=<%=st%>&serviceUrl=<%=Server.UrlEncode((string)dr["serviceURL"])%>" class="menusmall">Try It!</a></th> </tr>
				<%} // form link %>
					</table>
				</td>
				<td class="left"> <a href="UpdateRegistry.aspx?SearchIdentifier=<%=Server.UrlEncode((string)dr["Identifier"])%>&ro=t"><%=dr["Title"]%><b>(<%=dr["ShortName"]%>)</b></a>
						<br><font size="-2"><%=dr["content/description"]%></font></td>
				<td class="left"> <font size="-2"><%= (((string)dr["content/Subject"]).IndexOf(' ') <0)? ((string)dr["content/Subject"]).Replace(",",", ") : dr["content/Subject"] %><font size="-1"></td>
				<td class="left"> <font size="-2"><%=dr["Tag"]%></font></td>
				
			</tr>
			<%	}	%>
			
			</table>
			<table><tr>
			<td>Found <%=totalRes%> resources. </td> 
			<td class="menusmall"><%if (startRes > 0){%><a class="menusmall" href="QueryRegistry.aspx?startRes=<%=prevRes%><%=advanced?"&advanced=true":""%>">Prev<<</a><%}%></td>
			<td>Showing <%=startRes+1%> to <%=endRes%>.</td> 
			<td class="menusmall"><%if (endRes < totalRes){%><a class="menusmall" href="QueryRegistry.aspx?startRes=<%=endRes%><%=advanced?"&advanced=true":""%>">>>Next</a><%}%></td>
			</tr></table>

			<%}%>
		</form>

<!-- close all the tables and cells  - put in footer> !-->
             </td>
           </tr>
         </table>
	   </td>
	 </tr>
	 <tr>
	   <td bgcolor="#557399" height="30"><img src="<%=url%>/web/images/spacer.gif"></td>
	   <td colspan=2 align=right valign=bottom>
	     <table width="325" border="0" cellspacing="0" cellpadding="0">
	       <tr>
		     <td bgcolor="#FFFFFF" align=right><img src="<%=url%>/web/images/footercurve.gif"></td>
	         <td bgcolor="#8BBAE2" align=center><a href="<%=url%>/search.aspx" class="searchbar">Google</a></td>
	         <td bgcolor="#8BBAE2" align=center><a href="<%=url%>/contact.aspx" class="searchbar">Contact Us</a></td>
	         <td bgcolor="#8BBAE2" align=center><a href="mailto:feedback@us-vo.org?subject=VAO Directory Feedback" class="searchbar">Feedback</a></td>
	       </tr>
	     </table>
	   </td>
	 </tr>
	 <tr>
	   <td border="0" colspan=3 bgcolor="#000033" align=left>
	     <table border="0" width="100%" bgcolor="#000033">
	       <tr>
              <td width align="left"><img src="<%=url%>/web/images/spacer.gif" width="16" height="1"></td>
              <td width="48"><a href="http://www.nsf.gov/"><img border="0" src="<%=url%>/web/images/nsflogo.gif" width="48" height="48"></a></td>
              <td width="17"><img src="<%=url%>/web/images/spacer.gif" width="17" height="1"></td>
              <td width="72"><a href="http://www.ivoa.net"><img border="0" src="<%=url%>/web/images/ivoalogo.jpg" width="72" height="39"></a></td>
              <td width="10"><img src="<%=url%>/web/images/spacer.gif" width="10" height="1"></td>
              <td width><img src="<%=url%>/web/images/footerline.gif" width="1" height="55"></td>
              <td width="14"><img src="<%=url%>/web/images/spacer.gif" width="14" height="1"></td>
              <td width="530">
                <div class="tiny"><font color="#FFFFFF">
                Sponsored by the National Science Foundation under Cooperative 
                Agreement AST0122449 with<br>The Johns Hopkins University. 
                Developed in collaboration with the International Virtual Observatory Alliance.<br><br>
				</font></div></td>
              <td width><img src="../web/images/spacer.gif" width="1" height="84"></td>
	       </tr>
	     </table>
	   </td>
	 </tr>
   </table>


</td>
</tr>
</table>

</body>
</html>
				

