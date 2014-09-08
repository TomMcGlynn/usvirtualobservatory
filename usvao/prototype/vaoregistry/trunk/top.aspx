<%@ Page language="c#"  %>
<%
	
	string message = Request.Params["message"];
	string url = Request.ApplicationPath+"/";
	if (null == message)  message = "VAO Directory";
		string leftMenu = Request.Params["leftMenu"];
		
	string selectedTab = Request.Params["selected"];
//	Response.Write("Header Selected: " + selectedTab);

%>
<html>
<head>
<link href="<%=url%>styles.css" rel="stylesheet">
<style>
	.nvotitle	{ color:white; font-size:14pt; background-color:#24386d; text-align:center; font-weight:1000; }
	.nvobar		{ color:white; font-size: 9pt; background-color:#6ba5d7; text-align:left; }
</style>
</head>

<body  topmargin=0 leftmargin=0>
<table border="0" cellpadding="2" cellspacing="2" width="800">
  <tr bgcolor="#ffffff">
	<td valign="top"><a href="http://www.us-vo.org"><img 
		border="0" align="bottom" src="<%=url%>NVO_100pixels.jpg" width="104" height="45"  alt="NVO"></a></td>
	<td width="800">
		<table border="0" cellpadding="0" cellspacing="2" width="100%">
		<tr><td class="nvotitle" colspan="8"><img src="<%=url%>registry-title.gif" width="180" height="24"></td></tr>
		<tr>
		<td class="menu">
		<a class="menu" target="_parent" href="<%=url%>index.aspx"> Home </a></td>
		<td class="menu">
		<a class="menu" target="_parent" href="<%=url%>Summary.aspx"> Contents </a></td>
		<td class="menu">
		<a class="menu" target="_parent" href="<%=url%>QueryRegistry.aspx?startRes=-1"> Search </a></td>
		<td class="menu">
		<a class="menu" target="_parent" href="<%=url%>Publish.aspx"> Publish </a></td>
		<td class="menu">
		<a class="menu" target="_parent" href="<%=url%>DeleteEntry.aspx"> Delete </a></td>
		<td class="menu">
		<a class="menu" target="_parent" href="<%=url%>help/help.aspx"> Help/Develop </a></td>
		<td class="menu">
		<a class="menu" target="_parent" href="<%=url%>FAQ.aspx"> FAQ </a></td>						

		</tr>
		</table>
	</td>
  </tr>
  <tr>
	<td class="nvobar" colspan="2">&nbsp;&nbsp;&nbsp;Virtual Astronomical Observatory : <%=message%></td>
  </tr>
</table>

<%if (null != leftMenu) { 
	// pull in left menu if there is one
%>
<script type="text/javascript">
var ns = (navigator.appName.indexOf("Netscape") != -1);
var d = document;
var px = document.layers ? "" : "px";
function JSFX_FloatDiv(id, sx, sy)
{
	var el=d.getElementById?d.getElementById(id):d.all?d.all[id]:d.layers[id];
	window[id + "_obj"] = el;
	if(d.layers)el.style=el;
	el.cx = el.sx = sx;el.cy = el.sy = sy;
	el.sP=function(x,y){this.style.left=x+px;this.style.top=y+px;};
	el.flt=function()
	{
		var pX, pY;
		pX = (this.sx >= 0) ? 0 : ns ? innerWidth : 
		document.documentElement && document.documentElement.clientWidth ? 
		document.documentElement.clientWidth : document.body.clientWidth;
		pY = ns ? pageYOffset : document.documentElement && document.documentElement.scrollTop ? 
		document.documentElement.scrollTop : document.body.scrollTop;
		if(this.sy<0) 
		pY += ns ? innerHeight : document.documentElement && document.documentElement.clientHeight ? 
		document.documentElement.clientHeight : document.body.clientHeight;
		this.cx += (pX + this.sx - this.cx)/8;this.cy += (pY + this.sy - this.cy)/8;
		this.sP(this.cx, this.cy);
		setTimeout(this.id + "_obj.flt()", 40);
	}
	return el;
}
</script>
<table width="800">
<tr>
<td>
<table width=120><tr><td>
<div id="divTopLeft"     style="position:absolute" >
<%
Server.Execute(leftMenu);
%>
</div>
<script>JSFX_FloatDiv("divTopLeft",  10,   170).flt();</script>
</td></tr></table>

</td>
<td>

<%}
%>	
