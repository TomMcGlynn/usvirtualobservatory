<%@ Page language="jscript" AutoEventWireup="false" %>


<%
  var gselect = Request.Params["gselect"];
  if (null == gselect) gselect = Session["gselect"];
  var gutter = Request.Params["gutter"];

  if (null==gutter) gutter=Session["gutter"] 
  if (null==gutter) Response.Write("No menu specified ")
  else
  makeGutter(gselect,gutter);
%>

<%
	function mkG(level,b,c) {
		if (level==0) 
			mkG_L0(b,c);
		else if (level==1)
			mkG_L1(b,c);
		else if (level==2)
			mkG_L2(b,c);
	}

	function mkG_href(b,c) {
	    var url = ConfigurationSettings.AppSettings["url"];
	    if (null == url) url=Request.Url.GetLeftPart(System.UriPartial.Authority)+Request.ApplicationPath+"/";
//		var link = (b[2].match("http:"))?b[2]:url+b[2];
		var link = (b[2].match(":"))?b[2]:url+b[2];
		return "<a href='"+link+"' class="+c+">"+b[1]+"</a>";
	}

	function mkG_L0(b,c) {
		var s = "<tr><td class="+c+">";
		s += mkG_href(b,c)+"</td></tr>\n";
		s += "<tr><td width='10' height='5' class='left'></td><td class='left' colspan='2'></td></tr>\n";
		var img = "";
    s += "<tr><td colspan='3'>" + img + "</td></td></tr>\n";		    		
    s += "<tr><td width='10' height='5'></td><td colspan='2'></td></tr>\n";
		Response.Write(s);		
	}

	function mkG_L1(b,c) {
		var s = "<tr><td class="+c+">&nbsp;";
		s += mkG_href(b,c)+"</td></tr>\n";
		Response.Write(s);		
	}

	function mkG_L2(b,c) {
		var s = "<tr><td class="+c+">&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;-&nbsp;";
		s += mkG_href(b,c)+"</td></tr>\n";
		Response.Write(s);		
	}

	function makeGutter(n,g) {


		var a,b,i,level,m,f;
		var a = g.split(';');


		Response.Write("<table border=0 cellspacing=1 cellpadding=1 width='120'>\n");
		for(i=0;i<a.length-1;i++) {
			b = a[i].split(',');	
			m = b[0];
			f = Math.floor(m);
			if (f==m) level = (m==0?0:1);
			else level = (f==Math.floor(n))?2:-1;
			//Response.Write("<tr><td class="+c+">"+m+','+f+"</td><td class="+c+">"+level+"</td><td>"+n+"</td></tr>");
			if (m==n)mkG(level,b,'hi');
			else mkG(level,b,'lo');
		}
		Response.Write("</table>\n");
		
	}

%>

