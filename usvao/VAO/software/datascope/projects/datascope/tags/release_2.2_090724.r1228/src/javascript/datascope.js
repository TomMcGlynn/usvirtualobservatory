var statusArray = new Array();
var timeStart   = new Date().getTime();
var metadata    = null;
var NODATA      = 0;
var DATA        = 1;
var ERROR       = 2;
var REMAINING   = 3;

var statLabels = [
                  "<a href='javascript: void 0' onclick='return chng(\"matches\")'>Data found</a>",
                  "<a href='javascript: void 0' onclick='return chng(\"nonmatches\")'>No data </a>",
                  "<a href='javascript: void 0' onclick='return chng(\"errors\")'>Errors</a>",
                  "<a href='javascript: void 0' onclick='return chng(\"processing\")'>Waiting</a>"];


var HTTPHost    = "http://heasarc.gsfc.nasa.gov";
var CGIBase     = "/cgi-bin/vo/datascope/";

var errorText  = null;
var errorCount = 0;

var nonmatchText  = null;
var nonmatchCount = 0;

var errorText  = null;
var errorCount = 0;

var selDiv     = null;

var processingText  = null;
var processingCount = 0;

var summaryText = null;

var matchText  = null;
var matchCount = 0;

var cacheDate  = null;

var viewedChecks;
var allFitsSelected = false;


var helpText   = null;

var dataText   = null;
var lastDataID = null;

var lastText   = null;

var dataTextArray  = null;
var dataTextOffset = 0;
var dataMeta       = null;

var snLoc      = DMfields["ShortName"];
var titleLoc   = DMfields["Title"];
var pubLoc     = DMfields["Publisher"];
var typeLoc    = DMfields["Type"];
var stypeLoc   = DMfields["ServiceType"];
var subjLoc    = DMfields["Subject"];
var covspecLoc = DMfields["CoverageSpectral"];
var indexLoc   = DMfields["index"];
var nodeLoc    = DMfields["node"];
var idLoc      = DMfields["Identifier"];
var facLoc     = DMfields["Facility"];

var selFiles    = new Array();
var fileCount    = 0;
var selCounts   = new Array();
var selResource = new Array();
var resCount    = 0;
var parseTree;


var specialServ = new Array();
specialServ["ADS"]          = 1;
specialServ["NED(sources)"] = 1;
specialServ["Simbad"]       = 1;
var currentTable;

var dWin = null;

var autoUpdates = true;

var Tucd1 = -1;

var encCacheDir;

var currentNode;


function metadesc(index) {
	var str = "<a title='Display full metadata and status' target=extern href='$#' onclick='return showMeta("+index+")'>";
	return str;
}

function showMeta(index) {
	var str = "metadisp.pl?cache="+encCacheDir+"&id="+index;
	var stat = statusArray[index];
	var has  = selCounts[index];
	var chk  = selResource[index];
	if (has == null) {
		has = 0;
	}
	if (chk == null) {
		chk = 0;
	}
	var nodeStat = chk+","+has+","+stat[3]+","+stat[1];
	str += "&status="+encodeURIComponent(nodeStat) + ">";
	return nw(str, "extern");
}

function initialize() {
	window.focus();
	tpSet('resources', 'tabs', 'summary', 'matches', 'data', 'nonmatches', 
			'processing', 'errors', 'help');
	encCacheDir = encodeURIComponent(cacheDir);
	selDiv = document.getElementById("selDiv");
	initialize1();
}
function initialize1() {
	setCount();
}

function s (number) {
	if (number == 1) {
		return "";
	} else {
		return "s";
	}
}

function setCount() {

	var url   = cacheDir + "current.status" + "?zz="+encodeURIComponent(new Date().getTime());
	var req   = getRequest();
	req.open("GET", url, false);
	req.send(null);

	var text  = req.responseText;

	if (req.status == 404) {
		var delta = new Date().getTime() - timeStart;
		if (delta > 30000) {
			setText("status", "<strong>Status:</strong> Cache not initializing properly.");
			return
		} else {
			setText("status", "<strong>Status:</strong> Cache initializing.");
			window.setTimeout("initialize1()", 300);
			return
		}
	}

	if (lastText != null) {
		if (text.length == lastText.length && text == lastText) {
			setTimeout("setCount()", 2000);
			return
		}
	}
	lastText = text;

	var lines = text.split("\n");

	if (lines[lines.length-1].length == 0) {
		lines.pop();
	}

	if (cacheDate == null) {
		req = getRequest();
		req.open("GET", "date.pl?cache="+encodeURIComponent(cacheDir), false);
		req.send(null);
		cacheDate = req.responseText;
		if (cacheDate.length > 50) {
			cacheDate = "unknown";
		}
	}


	var error     = 0;
	var done      = 0;
	var hasData   = 0;
	var noData    = 0;
	var hits      = 0;
	var remaining = 0;
	var zc        = 0;

	for (var i=0; i<lines.length; i += 1) {
		var status;
		var cnts = "";
		var msg  = "";
		if (lines[i].length == 0) {
			status = REMAINING;
			remaining += 1;
		} else {
			var    flds = lines[i].split('|');
			if (flds[0].length == 0) {
				status = NODATA;
				msg    = flds[1];
				noData+= 1;
			} else if (flds[0] == -1) {
				status = ERROR;
				msg    = flds[1];
				error += 1;
			} else {
				status  = DATA;
				cnts    = flds[0];
				flds    = cnts.split(",");
				hits   += Math.floor((flds[0]));
				hasData+= 1;
			}
		}
		statusArray[i] = [i, cnts, msg, status];
	}

	var fraction = 100*(lines.length-remaining)/lines.length;
	fraction = parseInt(fraction);
	var bar = new Colorbar(500,5);
	bar.setColors(["green", "violet", "red", "black"]);
	bar.setElements(100);
	bar.setLabels(statLabels);
	bar.setCounts([hasData, noData, error, remaining]);

	var text = "<table><tr><td>"+bar.getBar()+"</td><td>"+fraction+"% complete</td></tr></table>";

	if (metadata == null) {
		setText("status", "<strong>Status:</strong> Downloading metadata");
		if (!getMetadata()) {
			setText("status", "<string>Status:</strong> Error in metadata download. Ending query.");
			return;
		}
	} 

	if (summaryText == null || error!= errorCount || nonmatchCount != noData  ||
			hasData != matchCount) {
		setSummaryText();
	}

	if (errorText == null || error != errorCount) {
		setErrorText(error);
	}
	if (processingText == null || remaining != processingCount) {
		setProcessingText(remaining);
	}
	if (nonmatchText == null || nonmatchCount != noData) {
		setNonmatchText(noData);
	}

	if (matchText == null || hasData != matchCount) {
		setMatchText(hasData);
	}

	if (helpText == null) {
		setHelpText();
	}

	if (dataText == null) {
		setDataText();
	}

	var now = new Date().getTime();
	text += "<table width='80%'><tr><td align=left>Position:"+userTarget+"</td><td>Resources/hits: "+ lines.length+"/"+hits+"</td>";
	if (cacheDate != null) {
		var odate = new Date(cacheDate);
		var delta = (now-odate.getTime())/ 3600;
		delta     = Math.round(delta);
		delta    /= 1000;
		text     += "<td>Cache age:"+delta+" hours</td>";
	}
	if (remaining > 0) {
		var delta = (now-timeStart)/1000;
		if (delta > 300) {
			autoUpdates = false;
		}
		if (autoUpdates) {
			text += "<td align=right><a title='Disable updates' href='javascript: void setAutoUpd(false)'>Stop updates</a></td>";
		} else {
			text += "<td align=right><a title='Refresh results' href='javascript: void setCount()'>Update</a>/" +
			"<a title='Enable updates' href='javascript: void setAutoUpd(true)'>Resume updates</a></td>";
		}
	}
	text += "</tr></table>";
	setText("status", text);
	if (remaining > 0 && autoUpdates) {
		setTimeout("setCount()", 2000);
	}
}

function setHelpText() {
	var url   = "/vo/datascope/helpInc.html";
	var req   = getRequest();
	req.open("GET", url, false);
	req.send(null);
	if (req.status == 404) {
		helpText = "Help text unavailable";
	} else {
		helpText = req.responseText;
	}

	setText("help", helpText);
}

function setSummaryText() {

	var textTimeStart   = new Date().getTime();

	var text = "<h3> Summary of Request and Selections</h3>";
	text += "<Table><tr><td><table border><tr><th colspan=2> Request parameters </th></tr>";
	text +=    "<tr><td colspan=2><b>Target:</b> " + userTarget + "</td></tr>";
	text +=    "<tr><td class=right>"+sexagesimal(userRA/15, 8)+"</td><td>"+sexagesimal(userDec,7)+"</td></tr>";
	text +=    "<tr><td class=right>"+userRA+"</td><td>"+userDec+"</td></tr>";
	text +=    "<tr><th class=right>Size:</th><td>"+userSize+"</td></tr>";
	if (userErrorCircle > 0) {
		text += "<tr><th class=right>Error radius:</th><td>"+userErrorCircle+"</td></tr>";
	}
	if (userSkipDataCache) {
		text += "<tr><td colspan=2> All known resources re-queried </td></tr>";
	}
	if (userSkipRegCache) {
		text += "<tr><td colspan=2> Registry re-queried to find resources </td></tr>";
	}

	imageUrl = cacheDir+"/DssImg.jpg";
	var req  = getRequest();
	req.open("GET", imageUrl, false);
	req.send(null);
	if( req.status != 200 ){
		imageUrl += "?" + textTimeStart;
	}

	text += "</table></td><td rowspan=3><br><img alt='SkyView Image' src="+imageUrl+"><br>DSS1 Optical Image of Requested Region (from <a title='Image source' href=http://skyview.gsfc.nasa.gov><i>SkyView</i></a>)</td><tr><td>";
	text += "<div id=selDiv>";
	text += getSelections();
	text += "</div></td></tr><tr><td>";

	text += "<form><table><tr><th colspan=2> Analysis Options </th></tr>";
	text += "<tr><td><button onclick=\"return analyze('Aladin')\"> Aladin Applet </button><td><td><button onclick=\"return analyze('AlScript')\">Aladin script</button></td>";
//	text += "<tr><td colspan=2><button onclick=\"return analyze('OASIS')\"> OASIS
//	Applet </button></td></tr>"
	text += "<tr><td colspan=2><button onclick=\"return analyze('TAR')\"> Save as tar </button></td></tr>";
	text += "</table></td></tr></table></form>";

	text += "<form id=ALFORM name=ALFORM action=http://aladin.u-strasbg.fr/java/nph-aladin.pl method=POST target=extern>"+
	"<input type=hidden name=frame value=launching>"+
	"<input type=hidden name='Start Aladin' value='Start Aladin session'>"+
	"<input type=hidden id=ALCONT type=text name=script></form>";
	text += "<form id=ASC name=ASC action=ascript.pl method=POST>"+
	"<input type=hidden id=ASCONT type=text name=script></form>";
	text += "<form id=TAR name=TFORM action=tar.pl method=POST>"+
	"<input type=hidden id=TCONT type=text name=selections>"+
	"<input type=hidden name=cache value='"+cacheDir+"'</form>";
	text += "<form id=OFORM name=OFORM action=oasis.pl method=POST target=extern>"+
	"<input type=hidden id=OCONT type=text name=selections>"+
	"<input type=hidden name=cache value='"+cacheDir+"'</form>";


	setText("summary", text);
	if (selDiv == null) {
		selDiv = document.getElementById("selDiv");
	}
}

function analyze(service) {
	if (fileCount + resCount == 0) {
		alert("You have not selected any resources for analysis.\n\n"+
		"Please select resources and data in the Resources and Data panels.");
		return false;
	}
	if (service == "Aladin") {
		doAladin();
	} else if (service == "AlScript") {
		doScript();
	} else if(service == "TAR") {
		doService("TCONT");
	} else if (service == "OASIS") {
		doService("OCONT");
	}
	return false;
}



function doService(element) {
	var content="";
	var index;
	var sep = "";
	for (index in selFiles) {
		if (selFiles[index] == null) {
			continue;
		}
		var flds = index.split('-');
		var sn =   metadata[flds[0]][snLoc];
		content += sep + sn+","+index + ","+ selFiles[index];
		sep=";";
	}
	for(index in selResource) {
		if (selResource[index] == null) {
			continue;
		}
		var sn = metadata[index][snLoc];
		content += sep + sn + ","+index;
		sep = ";";
	}
	var inp = document.getElementById(element);
	var form = inp.form;
	inp.value = content;
	form.submit();
}

function getSelections() {
	var hasRes = (resCount + fileCount)  > 0;
	var text;
	if (hasRes) {
		text = "<table><tr><th colspan=3>Resources selected</th></tr>";
		if (resCount > 0) {
			text += "<tr><td></td><td>"+resCount+" catalog"+s(resCount)+" and/or SIAP service"+s(resCount)+"(XML)</td></tr>";
		}
		if (fileCount > 0) {
			text += "<tr><td></td><td>"+fileCount+" FITS file"+s(fileCount)+"</td></tr>";
		}
		text += "<tr><td></td><td><a 'Remove all selections' href='javascript: void clearSelections()'>Clear Selections</a></td></tr>";
		text += "</table>";

	} else {
		text = "No resources currently selected.<br>"+
		"When you check tables and individual data files, you can<br> "+
		"download them in a single tar file or send them to Aladin<br> "+
		"from here.";
	}
	return text;
}

function clearSelections () {
	selFiles    = new Array();
	selResource = new Array();
	selCounts   = new Array();
	fileCount   = 0;
	resCount    = 0;
	if (selDiv != null) {
		selDiv.innerHTML = getSelections();
	}
	var el;
	for (el in viewedChecks) {
		viewedChecks[el].checked = false;
	}
	setMatchText(matchCount);
}



function setAutoUpd(flag) {
	if (flag) {
		timeStart = new Date().getTime();
	}
	autoUpdates = flag;

	if (flag) {
		setCount();
	}
}

function setMatchText(count) {
	var text;       
	if (count == 0) {
		text = "<h3> Matching Resources </h3>No resources with entries in the specified region have been found so far.";
		setText("matches", text);
	} else {

		if (parseTree == null) {
			parseTree = getParseTree();
		}
		mArr = getStatusArray(DATA);
		for (var i=0; i<mArr.length; i += 1) {
			m = mArr[i];
			if (metadata[m][nodeLoc] == null) {
				parseTree.match(metadata[m]);
			}
		}
		matchTreePrint(parseTree);
	}
	matchCount = count;
}

function matchTreePrint(parseTree) {
	var text = "<h3> Matching Resources </h3>" +
	"These resources had data in the specified region.<br>"+
	"Click on the <br><dl>"+
	" <dd> <i> checkbox </i> to select the data for download or analysis."+
	" <dd> <i> name </i> to view the catalog data and select files."+
	" <dd> <i> ? </i> to see the metadata for the resource.</dl> "+
	"When the number after the name is given as <i>nn/mm</i> you have "+
	"selected <i>nn</i> of the <i>mm</i> files indexed in that resource. "+
	"Click on the resource name to select files within such resources."+
	"<br>Download selected resources from the Summary tab.";


	text += "<table border><tr>";
	text += parseTree.print();
	text += "</table>";
	text += "<hr> Selected resources are shown on the Summary tab.";
	setText("matches", text);
}

function doAladin() {

	var content="";
	var index;
	for(index in selFiles) {
		if (selFiles[index] == null) {
			continue;
		}
		var flds = index.split('-');
		if (flds.length == 2) {

			var sn  = metadata[flds[0]][snLoc];
			var snc = validFileName(sn);
			var url = HTTPHost + CGIBase + "rf.pl?format=fits&sn="+snc;
			url     += "&id="+flds[0]+"&col="+selFiles[index];
			url     += "&index="+flds[1];
			url     += "&cache="+cacheDir;
			content += "get local("+url+","+snc+"."+flds[1]+");";
		}
	}
	for(index in selResource) {
		if (selResource[index] == null) {
			continue;
		}
		var sn = metadata[index][snLoc];
		var snc = validFileName(sn);
		content += "get local("+HTTPHost +
		cacheDir + snc + "." + index + ".xml" + 
		"," + snc + ");";
	}
	var inp = document.getElementById("ALCONT");
	var form = inp.form;
	inp.value = content;
	form.submit();
}

function validFileName(sn) {
	return sn.replace(/\W/g,"_");
}

function doScript() {

	var content="";
	var index;
	for(index in selFiles) {
		if (selFiles[index] == null) {
			continue;
		}
		var flds = index.split('-');
		if (flds.length == 2) {

			var sn =   metadata[flds[0]][snLoc];
			var url =  HTTPHost + CGIBase + "rf.pl?format=fits&sn="+encodeURIComponent(sn);
			url     += "&id="+flds[0]+"&col="+selFiles[index];
			url     += "&index="+flds[1];
			url     += "&cache="+cacheDir;
			content += "get local("+url+","+sn+"."+flds[1]+");";
		}
	}
	for(index in selResource) {
		if (selResource[index] == null) {
			continue;
		}
		var sn = metadata[index][snLoc];
		var snc = sn.replace(/\W/g,"_");
		content += "get local("+HTTPHost+cacheDir + snc + "." + index + ".xml" + 
		"," + sn + ");";
	}
	var inp = document.getElementById("ASCONT");
	var form = inp.form;
	inp.value = content;
	form.submit();
}

function matchEntry(meta) {

	m = meta[indexLoc];

	var xsel = statusArray[m][1];
	var cnts = xsel.split(",");
	var sel = null;

	if (meta[stypeLoc].match(/SIA/i) ) {
		if (cnts[1] > 0) {
			sel = cnts[1];
			if (selCounts[m] == null) {
				sel = "0/"+sel;
			} else {
				sel = selCounts[m] + "/"+ sel;
			}
		}
	}

	if (sel == null) {
		sel = cnts[0];
	}

	var checked = "";
	if (selResource[m]  != null ) {
		checked = " checked";
	}
	var xtitle = meta[titleLoc];
	xtitle = xtitle.replace('"', "'");
	xtitle = ' title="'+xtitle+' ('+meta[pubLoc]+')" ';
	var text = "<td id=xb"+m+"> "+
	"<input title='Select resource' type=checkbox onclick='return setCheckbox(this, "+m+")'" +checked+">"+
	"<a href='javascript: void renderNode(" + m + ")'"+xtitle + ">" + meta[snLoc]+"</a>"+
	" ("+sel+") "+
	metadesc(m)+"&nbsp;?</a>" + "</td>";
	return text;
}

function setCheckbox(chk, m) {
	if (selResource[m] == null) {
		chk.defaultChecked = true;
		resCount += 1;
		selResource[m] = true;
	} else {
		resCount -= 1;
		chk.defaultChecked = false;
		selResource[m] = null;
	}
	if (selDiv != null) {
		selDiv.innerHTML = getSelections();
	}
	return true;
}

function setErrorText(count) {
	var text = "<h3> Query errors </h3";

	var href = "target=extern href=metadisp.pl?cache="+ encodeURIComponent(cacheDir);

	if (count == 0) {
		text + "No errors detected";
	} else {

		var errArr = getStatusArray(ERROR);
		text += "Queries to the following services failed. An indication of the reason for the error is given.<p><p>"+
		"<table><tr> <th class=left>Short Name</th><th class=left>Service Type</th><th class=left>Publisher</th></tr><tr><td></td><th  colspan=2 class=left>Title  </th></tr>"+
		"<tr><td> </td><th colspan=2 class=left>Error</th></tr><tr><td colspan=3><hr><hr></td></tr>";

		for (var i=0; i<errArr.length; i += 1) {
			e = errArr[i];
			text += "<tr><td>"+metadesc(e) + metadata[e][snLoc] + "</td><td>" +
			getType(e)+"</td><td >" +
			metadata[e][pubLoc] + "</td></tr><tr><td></td><td colspan=2>"+
			metadata[e][titleLoc] + "</td></tr><tr><td> </td><td colspan=2>" +
			statusArray[e][2] + "</td></tr><tr><td colspan=3><hr></td></tr>";
		}
		text += "</table>";
	}
	errorText = text;
	setText("errors", text);
	errorCount = count;
}

function stringCmp (a, b) {
	a = metadata[a][snLoc];
	b = metadata[b][snLoc];
	if (a < b) {
		return -1;
	} else if (a > b) {
		return 1;
	} else {
		return 0;
	}
}


function getStatusArray(status) {

	var arr = new Array();
	for (var i=0; i<statusArray.length; i += 1) {
		if (statusArray[i][3] == status) {
			arr.push(i);
		}
	}

	arr.sort(stringCmp);
	return arr;
}

function setProcessingText(count) {

	var href = "target=extern href=metadisp.pl?cache="+ encodeURIComponent(cacheDir);
	if (count == 0) {
		text = "<H3> Unprocessed Resources </h3>All resources processed.";
	} else {
		var procArr = getStatusArray(REMAINING);
		text= "<h3> Unprocessed Resources</h3>"+
		"Queries to these resources have not yet responded. "+
		"Note that resources at given site are normally queued to be "+
		"processed sequentially so that DataScope does not flood a given "+
		"site with multiple requests.<p>"+
		"<table><tr> <th class=left>Short Name</th><th class=left>Service Type</th><th class=left>Publisher</th><th class=left>Title</th></tr>";

		for (var i=0; i<procArr.length; i += 1) {
			p = procArr[i];
			text += "<tr><td>"+metadesc(p) + metadata[p][snLoc] + "</a></td><td>" + 
			getType(p)+"</td><td>"+
			metadata[p][pubLoc] + "</td><td>"+
			metadata[p][titleLoc] + "</td></tr>";
		}
		text += "</table>";
	}
	processingText = text;

	setText("processing", text);
	processingCount = count;
}

function setNonmatchText(count) {

	var href = "target=extern href=metadisp.pl?cache="+ encodeURIComponent(cacheDir);
	if (count == 0) {
		text = "No non-matching resources found";
	} else {
		var procArr = getStatusArray(NODATA);
		text= "<h3>Non-Matching Resources</h3>"+
		"Queries of these resources completed successfully, but no results were found in the requested region."+
		"<table><tr> <th class=left>Short Name</th><th> Resource Type</th><th class=left>Publisher</th><th class=left>Title</th></tr>";

		for (var i=0; i<procArr.length; i += 1) {
			var p      = procArr[i];
			var filter = "";
			if (statusArray[p][2].match(/filter/i)) {
				filter = "<sup>*</sup>";
			}
			text += "<tr><td>"+metadesc(p) + metadata[p][snLoc] + "</a>"+filter+"</td>"+
			"<td>" + getType(p) + "</td>"+ 
			"<td>"+  metadata[p][pubLoc] + "</td><td>"+
			metadata[p][titleLoc] + "</td></tr>";
		}
		text += "</table>";
		text += "<sup>*</sup> This resource was not actually queried.  The host institution "+
		" provides a query filter which indicated that this resource had no data at the "+
		" specified location.";
	}
	nonmatchesText = text;
	setText("nonmatches", text);
	nonmatchCount = count;
}

function getMetadata() {

	var url = cacheDir+"/metadata.stem?"+new Date().getTime();
	var req = getRequest();
	req.open("GET", url, false);
	req.send(null);
	if (req.status == 404) {
		return
	}
	var text  = req.responseText;
	var lines = text.split("\n");

	metadata = new Array();

	for (var i=0; i<lines.length; i += 1) {
		if (lines[i].length > 0) {
			metadata[i] = lines[i].split("|");
			metadata[i][metadata[i].length] = i;
		}
	}

	setText("matches", "Metadata resources downloaded");

	if (metadata.length != statusArray.length) {
		alert("Error: Metadata mismatch\n"+
				"The number of resources described in the metadata ("+metadata.length+")"+
				" is not the same as the number of resources for which we have gotten status ("+
				statusArray.length+").  You may wish to re-query DataScope.");
		return false;
	}

	return true;
}


function setText(id, text) {
	var elem = document.getElementById(id);
	if (elem == null) {  // Check hidden panels.
		elem = tpGetPane(id);
	}
	if (elem) {
		elem.innerHTML = text;
	}
}

function setDataText() {
	dataText = "<h3> Table viewing panel </h3><p> You have not yet selected any resources. "+
	"This panel shows the results "+
	"from the last selected resource.  Select the Resources tab and click on the "+
	"name of the resource you are interested in.  That will pop up this panel with "+
	"the table from the resource you selected.";

	setText("data", dataText);
}

function renderNode(res_id) {

	if (lastDataID != null && lastDataID == res_id) {
		tpShow("resources", "data");
		return
	}

	lastDataID = res_id;

	var meta = metadata[res_id];
	var url  = "render.pl?sn="+encodeURIComponent(metadata[res_id][snLoc])+"&id="+res_id+"&cache="+cacheDir;

	var req   = getRequest();
	req.open("GET", url, false);
	req.send(null);

	var text  = req.responseText;

	// Assume text begins with a JavaScript block.
	if (text == null || text.indexOf("<SCRIPT") != 0) {
		var vtext = "<h3>Error querying resource</h3>";
		if (req.status != 200) {
			vtext += " Request status is "+req.status;
		} else {
			vtext += " Unexpected output follows <hr>"+text;
		}
		setText("data", vtext);
		tpShow("resources", "data");
		dataText = vtext;
		return;
	} 
	var start = text.indexOf(">") + 2;
	var end = text.indexOf("</SCRIPT>");
	var js  = text.substring(start, end);
	eval(js);

	dataTextArray = text.split("</TABLE>");
	if (dataTextArray[dataTextArray.length-1].indexOf("<TABLE") < 0) {
		dataTextArray.pop();
	}
	dataMeta = meta;
	currentNode = document.getElementById("xb"+res_id);
	allFitsSelected = false;
	displayBlock(res_id,0);
}

function displayBlock(res_id, offset) {

	var text = dataTextArray[offset]+"</table>";
	var links;
	if (dataTextArray.length < 2) {
		links = "";
	} else {
		if (offset > 0) {
			links =  "<a title='Go to beginning' href='javascript: void displayBlock("+res_id+",0)'>&lt;&lt;First</a> ";
			links += "<a title='Go back one block' href='javascript: void displayBlock("+res_id+","+(offset-1)+")'>&lt;Prev</a> ";
		} else {
			links = "&lt;&lt;First &lt;Prev";
		}
		var row = 25*offset + 1;
		var re  = row+24;
		if (offset == dataTextArray.length-1) {
			re = row + text.split(/<tr/i).length-3;
		}
		links += "| "+row +"-"+re + " |";
		if (offset < dataTextArray.length-1) {
			links += "<a title='Go forward one block' href='javascript: void displayBlock("+res_id+","+(offset+1)+")'>Next&gt;</a>";
			links += "<a title='Go to end' href='javascript: void displayBlock("+res_id+","+(dataTextArray.length-1)+")'> Last&gt;&gt;</a>";
		} else {
			links += "Next&gt; Last&gt;&gt;";
		}
	}
	var meta = dataMeta;
	var sn  = meta[snLoc];
	sn      = sn.replace(/\W/g,"_");
	var pURL    = cacheDir + sn + "."+ res_id + ".xml";
	var xmlURL  = HTTPHost + pURL;
	var errorcirc = "";
	if (userErrorCircle != null && userErrorCircle > 0) {
		errorcirc = "&errorcircle="+userErrorCircle;
	}
	dataText = "<h3> Data for "+meta[titleLoc]+" </h3>" +
	"Quick Links: "+
	"<a title='Data as pipe-delimited ASCII' target=extern href='simple.pl?sn="+sn+"&id="+res_id+"&cache="+encCacheDir+"' onClick='return nw(this.href, this.target)'>ASCII </a> | "+
	metadesc(res_id)+"MetaData</a> | "+
	"<a title='Returned XML file' target=extern href="+pURL+" onClick='return nw(this.href, this.target)'>XML</a> | "+
//	VOStat needs to be fixed...
//	"<a title='External statistics service' target=extern
//	href=http://astrostatistics.psu.edu/cgi-bin/VOStatBeta1/stats1.cgi/load?filetype=votable&uploaded_url="+xmlURL+"
//	onClick='return nw(this.href, this.target)'>VOStat</a> | "+
	"<a title='VO Plotting applet' target=extern href=/cgi-bin/vo/voplot/loadjvt2.pl?suffix="+pURL+" onClick='return nw(this.href, this.target)'>VOPlot</a> | "+
	"<a title='Display positions on DSS image of region' target=extern href=overlay.pl?cache="+cacheDir+"&sn="+encodeURIComponent(sn)+"&id="+res_id+errorcirc+" onClick='return nw(this.href, this.target)'>Overlay</a>";

	var isSIA = false;
	if (meta[stypeLoc].match(/SIA/i)) {
		isSIA = true;
		dataText += "<br>All checked files may be downloaded or sent to Aladin from the Summary tab";
	}

	dataText += "<p>"+links +"<br>";
	dataText += text;
	dataText += "<br>"+links +"<p>";

	setText("data", dataText);



	var tuc = Tucd1.split(",");
	var ira   = -1;
	var idec  = -1;
	var iform = -1;
	var iurl  = -1;

	for (i=0; i<tuc.length; i += 1) {
		var lc = tuc[i].toLowerCase();
		if (lc.match("(pos_eq_ra_main|pos.eq.ra;meta.main)")) {
			ira   = i;
		} else if (lc.match("(pos_eq_dec_main|pos.eq.dec;meta.main)")) {
			idec  = i;
		} else if (lc == "vox:image_format") {
			iform = i;
		} else if (lc == "vox:image_accessreference" || tuc[i] == "data_link") {
			iurl  = i;
		}
	}

	tpShow("resources", "data");

	var row= 1 + 25*offset;

	if (isSIA) {
		if (iurl < 0) {
			alert("No AccessReference specified in the metadata.  Unable to reference data");
			isSIA = false;
		} else {
			iurl += 1;
		}
		if (iform < 0) {
			alert("No ImageFormat column specified.  All data assumed to be FITS");
		} else {
			iform += 1;
		}
	}
	if (isSIA && iurl >= 0 && iform >= 0) {
		var elem = document.getElementById("t1-"+offset+"-0-0");
		var checked = "";
		if (allFitsSelected) {
			checked = " checked ";
		}
		elem.innerHTML = "<input type=checkbox id=ck0 onClick='return setAllFits(this,"+offset+ "," + res_id + "," + iform + "," + iurl + ")' > All";
	}

	var shortName = meta[snLoc];
	var special   = (specialServ[shortName] != null);
	if (meta[stypeLoc] == "CONE"  &&  meta[typeLoc] == "Archive"  && !meta[pubLoc].match(/Spitzer/)) {
		if (meta[pubLoc].match(/HEASARC/)) {
			shortName = "HEASARC";
			currentTable = meta[idLoc];
			currentTable = currentTable.substring(currentTable.lastIndexOf("/")+1);
			special = true;
		}  else if (meta[pubLoc].match(/Space Tel/)) {
			shortName = "MAST";
			currentTable = meta[facLoc].toLowerCase();
			special = true;
		}
	}

	viewedChecks = new Array();

	if (ira >= 0 && idec >= 0) {
		ira  += 1;
		idec += 1;

		while (true) {
			var id = "t1-" + row + "-"+ira;
			var el = document.getElementById(id);
			if (el == null) {
				break;
			}
			var ra  = sexagesimal(el.innerHTML/15, 7);
			if (!ra.match(/NaN/)) {
				el.innerHTML = ra;
			}

			id = "t1-" + row + "-"+idec;
			el = document.getElementById(id);
			if (el == null) {
				continue;
			}
			var dec =  sexagesimal(el.innerHTML, 7);
			if (!dec.match(/NaN/) ) {
				el.innerHTML = dec;
			}

			if (isSIA && iform >= 0 && iurl >= 0) {
				var isFits = false;
				var form;
				if (iform < 0) {
					isFits = true;
				} else {
					id = "t1-" + row + "-"+iform;
					form  = document.getElementById(id).innerHTML;
					isFits = form.match(/FITS/i);
				}
				id = "t1-" + row + "-"+iurl;

				var entry;
				var node = document.getElementById("t1-" + row + "-"+iurl);
				var xurl = node.innerHTML;
				node.innerHTML = "<a title='Remote data file' target=extern href='"+xurl+"' onClick='return nw(this.href, this.target)'>Link </a>";


				if (isFits) {
					entry = row+". "+"<input id=ck"+row+" type=checkbox  "+ 
					fileSelected(res_id,row)+
					" onClick='return flipFile(this, "+res_id+","+row+","+iurl+")'>" +
					"<a title='View FITS file' href=fv.pl?sn="+sn+"&col="+iurl+"&id=" + res_id + "&index=" + row + "&cache="+encodeURIComponent(cacheDir)+errorcirc+
					" onClick='return nw(this.href, this.target)' onClick='return nw(this.href, this.target)'>View</a>|"+
					"<a title='View FOV overlay on DSS background' href=fovw.pl?sn="+sn+"&col="+iurl+"&id=" + res_id + "&index=" + row + "&cache="+encodeURIComponent(cacheDir)+errorcirc+
					" onClick='return nw(this.href, this.target)'>FOV</a>";

				} else {
					id = "t1-" + row + "-"+iurl;
					entry = row+". <a title='View quicklook image/data' href=rf.pl?sn="+sn+"&col="+iurl+"&id=" + res_id + "&index=" + row + "&cache="+encodeURIComponent(cacheDir)+"&format="+form+
					" onClick='return nw(this.href, this.target)'>View</a>";
				}
				id = "t1-" + row + "-0";
				document.getElementById(id).innerHTML = entry;
				if (isFits) {
					var el = document.getElementById("ck"+row);
					viewedChecks[row] = el;
				}
			}

			if (special) {
				renderSpecial(shortName, row);
			}

			row += 1;
		}
	}
}

function setAllFits(chk, offset, res, iform, iurl) {


	allFitsSelected = !allFitsSelected;
	var xelem = document.getElementById("ck0");
	xelem.defaultChecked = allFitsSelected;

	for (var block=0; block < dataTextArray.length; block += 1) {
		var rows = dataTextArray[block].split(/<\/TR>/i);
		for (var i=0; i<rows.length; i += 1) {
			var flds = rows[i].split(/<\/TD>/i);
			if (flds.length >= iform && flds[iform].match(/FITS/i)) {
				var xr = i + 25*block;
				if (block != 0) {
					xr += 1;  // First block has extra header.
				}
				var id = res+"-"+xr;
				if (allFitsSelected && !selFiles[id]) {
					addFile(id, res, iurl);
				} else if (!allFitsSelected && selFiles[id]) {
					delFile(id, res);
				}
			}
		}
	}
	displayBlock(res, offset);

	xelem = document.getElementById("ck0");
	xelem.checked = allFitsSelected;

	currentNode.innerHTML = matchEntry(metadata[res]);
	if (selDiv != null) {
		selDiv.innerHTML = getSelections();
	}
	return true;
}

function nw(url, target) {
	var move = false;
	var xs   = 500;
	var ys   = 500;
	if (dWin != null) {
		if (dWin.closed) {
			dWin = null;
		}
	}
	if (dWin == null) {
		features = 'resizable,title="DataScope features",toolbar,status,width=500,height=500,scrollbars';
		dWin = window.open(url, target, features);
	} else {
		dWin.location.href = url;
	}
	if (dWin.focus) {
		dWin.focus();
	}
	return false;
}

function delFile(id, res) {
	fileCount    -= 1;
	selFiles[id]  = null;
	selCounts[res] -= 1;
}

function addFile(id, res, icol) {
	selFiles[id]  = icol;
	fileCount    += 1;
	if (selCounts[res] == null) {
		selCounts[res] = 1;
	} else {
		selCounts[res] += 1;
	}
}

function flipFile(chk, res, row, icol) {
	var id = res+"-"+row;

	if (selFiles[id] != null) {
		chk.defaultChecked = false;
		delFile(id, res);

	} else {
		chk.defaultChecked = true;
		addFile(id, res, icol);
	}
	currentNode.innerHTML = matchEntry(metadata[res]);
	if (selDiv != null) {
		selDiv.innerHTML = getSelections();
	}
	return true;
}

function fileSelected(res, row) {
	if (selFiles[res+"-"+row] == null) {
		return "";
	} else {
		return "checked";
	}
}    

//Try to support IE.
function getRequest() {

	var req = false;
	try {
		req = new ActiveXObject("Msxml2.XMLHTTP");
	} catch (e) {
		req = false;
		try {
			req = new ActiveXObject("Microsoft.XMLHTTP");
		} catch (ex) {
			req = new XMLHttpRequest();
		}
	}
	return req;
}

function renderSpecial(service, row) {

	if (service == "HEASARC") {
		var node = document.getElementById("t1-"+row+"-1");
		var text = node.innerHTML;
		node = document.getElementById("t1-"+row+"-0");
		node.innerHTML =  "<a title='Link to HEASARC archive' href=http://heasarc.gsfc.nasa.gov/db-perl/W3Browse/w3hdprods.pl?files=Preview&Target=heasarc_"+
		currentTable+"|||_unique_id="+text+"||  onClick='return nw(this.href, this.target)'>Data</a>";

	} else if (service == "MAST") {
		var node = document.getElementById("t1-"+row+"-1");
		var text = node.innerHTML;
		node = document.getElementById("t1-"+row+"-0");
		node.innerHTML = "<a title='Link to MAST archive' href=http://archive.stsci.edu/cgi-bin/mastpreview?mission=" +
		currentTable + "&dataid=" + text + " onClick='return nw(this.href, this.target)'>Data</a>";

	} else if (service == "ADS") {
		var node = document.getElementById("t1-"+row+"-1");
		var text = node.innerHTML;
		node.innerHTML = "<a title='Link to ADS reference' href=http://adsabs.harvard.edu/cgi-bin/nph-bib_query?bibcode="+text+" onClick='return nw(this.href, this.target)'>" +text+"</a>";
	} else if (service == "NED(sources)") {
		var node = document.getElementById("t1-"+row+"-2");
		var text = node.innerHTML;
		var ztext= encodeURIComponent(text.replace(/^\*/, ""));
		node.innerHTML = "<a title='Link to NED details' href='http://nedwww.ipac.caltech.edu/cgi-bin/nph-objsearch?objname="+ztext+"&extend=no'  onClick='return nw(this.href, this.target)'>"+text+"</a>";
	} else if (service == "Simbad") {
		var node = document.getElementById("t1-"+row+"-3");
		var text = node.innerHTML;
		var zText = encodeURIComponent(text);
		node.innerHTML = "<a title='Link to Simbad details' href='http://simbad.u-strasbg.fr/sim-id.pl?protcol=html&Ident="+zText+"&Frame3=G' onClick='return nw(this.href, this.target)'>"+text+"</a>";
	}
}

function chng(tab) {
	tpShow("resources", tab);
	return false;
}

function getType(index) {
	var res = "";
	if (metadata[index][stypeLoc].match("SIA")) {
		res = "Images/"+metadata[index][covspecLoc];
	} else if (metadata[index][typeLoc].match("Archive")) {
		res = "Observations/"+metadata[index][covspecLoc];
	} else {
		res = "Objects/"+metadata[index][subjLoc];
	}
	return res.replace(/,/g, ", ");
}


