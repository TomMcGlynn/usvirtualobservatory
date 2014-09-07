<%@ Page language="c#" CodeBehind="riws.aspx.cs" AutoEventWireup="false" %>
<%@ Import Namespace="System.Web" %>
<%
	string Title = "STScI/JHU VO Registry Web Services";
	string author ="Gretchen Greene";
	string email ="greene@stsci.edu";
	string cvsRevision = "$Revision: 1.1 $";
	string cvsTag = "$Name:  $";
	
	string path = "";

	string bgcolor = "#FF0000";
	string displayTitle = "pubpage";
	string selected = "home";
	
	string Parameters = "message="	+	Title	+	"&"	+	"author="	+	author	+
		"&"	+	"email="	+	email	+	"&"	+	"cvsRevision=" + cvsRevision.Replace(":"," ")  +
		"&path=" + path + "&selected=" + selected +
		"&bgcolor=" + bgcolor + "&displayTitle=" +displayTitle;				


Server.Execute("web/SkyHeader2.aspx" + "?" + Parameters);
%>

<!-- =======================================================================
  -  Page Content -->
<!--  -  ======================================================================= -->
        <table width="100%"><tr><td width="112"></td>
        <td>
        
    <h2>Directory Interface XML Web Services</h2>
<p align="left">
        Below you can find the link to the latest Directory Interface services to perform all operations
        programmatically. These services provide programmatic access to the functionality
        on this web site with more to come soon:</p>
    <p>
        &nbsp;</p><br />
<!--    <p align="left"> -->
        <table id="tblRIWS" border="0" cellpadding="0" cellspacing="15" width="80%">
            <tr>
                <td width="120">
                    <p align="left">
                        <strong>Service</strong></p>
                </td>
                <td style="width: 523px">
                    <p align="center">
                        <strong>Brief Description of Services</strong></p>
                </td>
                <td style="width: 157px">
                    <p align="center">
                        <strong>Home</strong></p>
                </td>
                <td style="width: 179px">
                    <p align="center">
                        <strong>WSDL</strong></p>
                </td>
            </tr>
            <tr>
                <td valign="top" style="height: 16px; width: 300px;">
                    <p align="left">
                        VOTable-based Directory Interface
                    </p>
                </td>
                <td width="523" valign="top">
                    <p align="left">
                        Provides basic access to Directory resource metadata in VOTable format.
                    </p>
                </td>
                <td style="width: 157px; height: 16px;" valign="top">
                    <p align="center">
                            <asp:HyperLink ID="lnkNVORegInt" runat="server" NavigateUrl="NVORegInt.asmx">NVORegInt.asmx</asp:HyperLink>
                    </p>
                </td>
                <td style="width: 179px; height: 16px;" valign="top">
                    <p align="center">
                        <asp:HyperLink ID="lnkNvoRegIntWSDL" runat="server" NavigateUrl="NVORegInt.asmx?WSDL">wsdl</asp:HyperLink>
                    </p>
                </td>
            </tr>     
            <tr>
                <td valign="top" style="height: 16px">
                    <p align="left">
                        SOAP-based OAI Interface</p>
                </td>
                <td style="width: 523px; height: 16px;" valign="top">
                    Open Archives Initiatives service</td>
                <td style="width: 157px; height: 16px;" valign="top">
                    <p align="center">
                        <a href="STOAI.asmx">STOAI.asmx</a></p>
                </td>
                <td style="width: 179px; height: 16px;" valign="top">
                    <p align="center">
                        <a href="STOAI.asmx?WSDL">wsdl</a></p>
                </td>
            </tr>      
             <tr>
                <td valign="top" style="height: 16px">
                    <p align="left">
                        Standard OAI Interface</p>
                </td>
                <td style="width: 523px; height: 16px;" valign="top">
                    Open Archives Initiatives standard services</td>
                <td style="width: 157px; height: 16px;" valign="top">
                    <p align="center">
                        <a href="OAI.aspx">OAI.aspx</a></p>
                </td>
                <td style="width: 179px; height: 16px;" valign="top">
                    <p align="center">
                        </p>
                </td>
            </tr>
            <tr>
                <td valign="top" style="height: 16px">
                    <p align="left">
                        Standard Search Interface</p>
                </td>
                <td style="width: 523px; height: 16px;" valign="top">
                    Registry Standard SOAP / ADQL Search</td>
                <td style="width: 157px; height: 16px;" valign="top">
                    <p align="center">
                        <a href="RIStandardService.asmx">RIStandardService.asmx</a></p>
                </td>
                <td style="width: 179px; height: 16px;" valign="top">
                    <p align="center">
                        <a href="RIStandardService.asmx?WSDL">wsdl</a></p>
                </td>
            </tr>
        </table>
        
          <h2>Directory Administration and Harvesting</h2>
    <ul><li>Directory <a href="RegistryDBQuery.asmx">Local Administrative</a> web service. 
    This service can be used for direct DB query individual records and other 
    administrative functionality. Caution: this service is not supported for external interfaces.</li>
    <li>Directory <a href="HarvestAdmin.asmx">Harvester Administration</a> web service. 
    This service can be used to find the source registries of resources and harvest individual resources or registries outside of the normal automated process.</li>
    </ul>
    <br />
  
        
    <h2 style="text-align: left">
        Documentation for Services</h2>
    <ul>
        <li><a href="http://www.us-vo.org/news/story.cfm?ID=32"><span style="color: #000000">The VAO Book, published as an ASP Conference Series</span></a></li>
        <li><a href="http://www.ivoa.net/Documents/"><span style="color: #000000">IVOA Documents and Standards</span></a></li>
        <li><a href="http://www.us-vo.org/summer-school/2006/proceedings/"><span style="color: #000000">
            Summer School 2006 proceedings</span></a></li>
        <li><a href="http://www.us-vo.org/summer-school/2005/proceedings/"><span style="color: #000000">
            Summer School 2005 proceedings</span></a></li>
        <li><a href="http://www.us-vo.org/summer-school/2004/proceedings/"><span style="color: #000000">
            Summer School 2004 proceedings</span></a></li>

    </ul><br />
    
    <h2 style="text-align: left">
        Web Services General Resources</h2>
    <ul>
        <li><a href="http://msdn.microsoft.com/webservices/">http://msdn.microsoft.com/webservices/</a>
        </li>
        <li><a href="http://java.sun.com/webservices/">http://java.sun.com/webservices/</a>
        </li>
        <li><a href="http://www.mono-project.com/">http://www.mono-project.com/</a> </li>
    </ul>
     
    <!-- =======================================================================
  -  End Page Content
  -  ======================================================================= -->

</td><td width="147"></td></tr></table>

<%
//Server.Execute("bot.aspx");
	Server.Execute("web/SkyFooter2.aspx" + "?" + Parameters);
%>
