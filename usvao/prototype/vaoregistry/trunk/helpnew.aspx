<%@ Page language="c#" AutoEventWireup="false" %>
<%@ Import Namespace="System.Web" %>
<%
	string Title = "STScI/JHU VO VAO Directory Help Page";
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

<table width="100%">
<tr><td width="112"></td>
<td>

<h3>Index</h3>
<ul>
<li<a href="#overview">Overview</a></li>
<li><a href="#faq">Search FAQ</a>
    <ul>
    <li><a href="#whatenter">What can I enter in the search box?</a></li>
    <li><a href="#howsort">How do I sort my results?</a></li>
    <li><a href="#displayhide">How do I display or hide more information about the returned resources?</a></li>
    <li><a href="#filter">Too many resources were returned. How do I further filter my results?</a></li>
    <li><a href="#format">In what formats can I save my results?</a></li>
    <li><a href="#howsave">How do I save my results?</a></li>
    <li><a href="#howsend">How do I send my results to other applications within the Portal?</a></li>
    </ul>
</li>
<li><a href="#register">Registering a Resource in the Directory</a></li>
<li><a href="#standards">Resource Standards</a></li>
</ul>
<br /><br /><br />

<h3 id="overview">Overview</h3>
The VAO Directory is an interface for finding data collections and catalogs by 
searching their descriptions. The Directory search interface queries an underlying 'registry', or 
database of descriptive metadata about collections and catalogs. The underlying VAO Directory has access to
the metadata of every registry in the IVOA as well as locally managed information.  
The web portal has been developed to provide direct search access for the scientific, student, 
or casual user, and programmatic search capabilities are also available for interoperability with other research tools. 
A publishing interface is also offered for the local management of information about a user's own
data collections and catalogs; registering the resource's descriptive metadata with this interface allows the catalogs
 to be immediately searched by the directory and other tools within The VAO Portal.

<br /><br /><br /><H3 class="Section1" id="faq">Search FAQ</H3>

<h5 id="whatenter">What can I enter in the search box?</h5>
<li>Enter any text in the "Find Astronomical Data Resources" search box to retrieve all
resources in the directory which contain your text. You may enter a partial title, description,
identifier, or any data element in the resource as described below. You do not need to
surround multiple-word queries with quotes. For example, <i>far ultraviolet</i> will return
all resources containing those words together.</li>

<h5 id="howsort">How do I sort my results?</h5>
<li>Sort results by column by clicking on the column header. You can toggle the sort order (ascending or descending) 
by clicking on the same column header again.</li>

<h5 id="displayhide">How do I display or hide more information about the returned resources?</h5>
<li>To hide or display columns of resource data, select a cut-off column from the list below your results.
Alternatively, the left and right arrow column headers will subtract and add columns one at a time in your results display.</li>

<h5 id="filter">Too many resources were returned. How do I further filter my results?</h5>
<li>Filter results by column by entering text and wildcard characters in the text box under the column header
and clicking on "Apply Filter". For example, when finding resources with optical-waveband data (by searching
on <i>Optical</i>), you can filter for data from HEASARC by entering <i>HEASARC</i> in the "publisher" 
text box, and clicking "Apply Filter". Filtering is case-insensitive.</li>

<li>By default, your term for filtering will return partial matches as if your term were surrounded by wildcard (*) characters.
To require an exact match, surround your filtering term with double or single quotes. Thus, to only return records with GALEX as the full
short name, you could enter <i>"GALEX"</i> or <i>'GALEX'</i> in the filtering text box under the column for "short name". If you include any wildcards
(*) in your filtering term, the filter will assume you otherwise require an exact match in the text. Therefore <i>GALEX*</i> would return
all results that begin with GALEX</li>
<li>To clear all of your current search filtering terms, click "Clear Filter".</li>

<h5 id="format">In what formats can I save my results?</h5>
<li>Resource lists can be saved as .CSV files, or as XML VOTables. Each resource will be included
in its entirety, even if certain columns are not displayed on the webpage.</li>

<h5 id="howsave">How do I save my results?</h5>
<li>You can save your results in .CSV or XML VOTable format by selecting your save options from the drop-down to the left above
the table of your results, and then hitting the "save" button next to it. There exist options for saving all results, filtered results,
or specifically selected results. You can select specific results to be saved by marking the checkbox in the rightmost 
column of the results you wish to save. Resources on multiple pages of results can be selected by this method. To 
then save your selected results, use "Save Selected as CSV" or "Save Selected as VOTable". </li>

<h5 id="howsend">How do I send my results to other applications within the Portal?</h5>
<li>Some other NVO projects, notably VIM, Datascope, and Inventory, can accept results sent by the directory search page. To send your
results to one of these projects, select it from the drop-down marked "Next, Send Results to", to the right above the table of your results and click the "Send" button.
If you have specifically selected resources, only those selected will be sent. Otherwise, if you have filtered your results, only the filtered ones will
be sent. If you have neither filtered or selected your results, all results will be sent. From VIM, you can see that catalogs in your results are automatically added to your catalog cache.
</li>

<br /><br /><br /><H3 class="Section1" id="register">Registering a Resource in the Directory </H3>
You can <A href="xpublish.aspx">publish</A> your resources locally to the STScI/JHU registry (the underlying database) and your data 
will be automatically circulated to the other VO  repositories and immediately available for searching via the Directory.<br /><br />
<li>
	<A href="http://us-vo.org/pubs/files/PublishHowTo.html">Overview of publishing to 
		the VO</A>
</li>


<br /><br /><br /><H3 class="Section1" id="standards">Resource Standards</H3>
<LI>
	<a href="http://www.ivoa.net/Documents/REC/ResMetadata/RM-20040426.html">(RM) 
		Resource Metadata for the Virtual Observatory Version 1.01</a></LI>
		<LI>
			<A href="http://www.ivoa.net/Documents/PR/ReR/VOResource-20061107.html">VOResource v1.02 
		and its Standard Extensions</A></li>


			

    <!-- =======================================================================
  -  End Page Content
  -  ======================================================================= -->
</td><td width="147"></td></tr></table>

<%
	Server.Execute("web/SkyFooter2.aspx" + "?" + Parameters);
%>
