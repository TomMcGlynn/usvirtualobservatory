<%@ Page Language="c#" AutoEventWireup="false" %>
<%
Server.Execute("top.aspx?message=Harvest SOAP");
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN" >
<HTML>
	<HEAD>
		<title>Registry Overview</title>
		<LINK href="styles.css" rel="stylesheet">
		<LINK REL="shortcut icon" HREF="scnvo.ico">
	</HEAD>

<!--iframe frameborder="no" height="120" src="top.aspx?Harvest SOAP" width="100%" scrolling="no">
</iframe-->

<form id="harvest" action="SOAPHarvester/Harvest.asmx/HarvestSOAPReg">
<Table>
<tr><th class="left">From URL ASMX:</th><td class="left"><input size="80" name="fromUrlAsmx" value = "http://skyservice/devel/registry.asmx" ></td> </tr>
<tr><th class="left">To URL ASMX:</th><td class="left"> <input  size="80" name="toUrlAsmx" value="http://localhost/registry/registry.asmx" ></td> </tr>
<tr><th class="left">Password:</th><td class="left"> <input type="password" name="password"></td> </tr>
<tr><td></td><td class="left"><input type="Submit" Value="Harvest"</td></tr>
</tr>
