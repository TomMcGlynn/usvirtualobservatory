<%@ Import Namespace="System.Web" %>
<%@ Page language="c#" CodeBehind="search.aspx.cs" AutoEventWireup="false" Inherits="registry.search" %>
<% // fill this with your details 
	string Title = "NVO - Search virtualobservatory.org";
	string author ="Gretchen Greene";
	string email ="greene@stsci.edu";
	string cvsRevision = "$Revision: 1.2 $";

	string path = "";
	
	string bgcolor = "#FF0000";
	string displayTitle = "";
	string selected = "home";
	
	string Parameters = "message="	+	Title	+	"&"	+	"author="	+	author	+
		"&"	+	"email="	+	email	+	"&"	+	"cvsRevision=" + cvsRevision.Replace(":"," ")  +
		"&path=" + path + "&selected=" + selected +
		"&bgcolor=" + bgcolor + "&displayTitle=" +displayTitle;				
%>


<% // will need to fix path if we have sub dirs .. 
	Server.Execute("web/SkyHeader.aspx" + "?" + Parameters);
%>
<h1>Search</h1>

<p>Use the form below to have Google search the virtualobservatory.org website and The VAO Explorer, which is 
hosted on the same site. </p>

<!-- SiteSearch Google -->
<FORM method=GET action="http://www.google.com/search">
<input type=hidden name=ie value=UTF-8>
<input type=hidden name=oe value=UTF-8>
<TABLE bgcolor="#FFFFFF" border=0><tr><td>
<A HREF="http://www.google.com/">
<IMG SRC="http://www.google.com/logos/Logo_40wht.gif" 
border="0" ALT="Google"></A>
</td>
<td>
<INPUT TYPE=text name=q size=31 maxlength=255 value="">
<INPUT type=submit name=btnG VALUE="Google Search"><br><br>
<span class=med>
<input type=hidden name=domains value="http://nvo.stsci.edu/voregistry"><input type=radio name=sitesearch value=""> WWW <input type=radio name=sitesearch value="http://nvo.stsci.edu/voregistry" checked> STScI VO Registry
</span>
</td></tr></TABLE>
</FORM>
<!-- SiteSearch Google -->


<!-- EXAMPLE Below for Making SITEMAP
<h1>Sitemap</h1>

<table width=500>
  <tr>
     <td width="20%" align=left><p><a href="default.aspx">Home</a></p></td>
     <td width="20%" align=center><p><a href="whatis.aspx">What is NVO?</a></p></td>
     <td width="15%" align=center><p><a href="faq.aspx">FAQ</a></p></td>
     <td width="15%" align=center><p><a href="toolkit.aspx">Toolkit</a></p></td>
     <td width="15%" align=center><p><a href="partners.aspx">Partners</a></p></td>
     <td width="15%" align=center><p><a href="help.aspx">Help</a></p></td>
  </tr>
  <tr>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>                    
  </tr>
  <tr>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>                    
  </tr>  
  <tr>
     <td align=left><p><a href="students/">Students</a></p></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>                    
  </tr>
  <tr>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>                    
  </tr>
  <tr>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>                    
  </tr>  
  <tr>
     <td align=left><p><a href="teachers/">Teachers</a></p></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
  </tr>
  <tr>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>                    
  </tr>
  <tr>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>                    
  </tr>  
  <tr>
     <td align=left colspan=2><p><a href="informalsci/">Informal Science Educators</a></p></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
  </tr>
  <tr>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>                    
  </tr>
  <tr>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>                    
  </tr>    
  <tr>
     <td align=left colspan=2><p><a href="amateurs/">Amateur Astronomers</a></p></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
  </tr>
  <tr>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>                    
  </tr>
  <tr>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>                    
  </tr>    
  <tr>
     <td align=left><p><a href="search.aspx">Search</a></p></td>
     <td colspan=2><p><a href="contact.aspx">Contact Us</a></p></td>
     <td></td>
     <td></td>
     <td></td>
     <td></td>
  </tr>
  
</table>-->


<% // will need to fix path if we have sub dirs .. 
	Server.Execute("web/SkyFooter.aspx" + "?" + Parameters);
%>

