// ---- This all needs to go somewhere else in another file... ----
function renderXML() {
	try {
		var votable = removeValAttribute(rd.filter.getDocument());		
		var str = new XMLSerializer().serializeToString(votable);
		document.getElementById("sources").value = str;
		document.getElementById("outputform").submit();
	} catch (e) {
		alert("Exception:"+e);
	}
	return false;
}

function renderAscii() {
	// Need to get rid of reference to docBase...
	var xsltString=
		'<?xml version="1.0" encoding="UTF-8"?>\n'+
		'<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" '+
		'xmlns:vo="http://www.ivoa.net/xml/VOTable/v1.1" xsl:exclude-result-prefixes="vo" '+
		'version="1.0">\n'+
		'<xsl:include href="'+docBase+'/xsl/ascii.xsl" />\n'+
		'</xsl:stylesheet>'

	try {
		var xsltp   = new XSLTProcessor();
		var xsltDom = (new DOMParser()).parseFromString(xsltString, "text/xml");

		xsltp.importStylesheet(xsltDom);
		var newDoc     = xsltp.transformToFragment(rd.filter.getDocument(), document);
		if (top.twin) {
			top.twin.close();
		}
		top.twin = window.open(null, "results", "WIDTH=450,HEIGHT=300,resizable,scrollbars,toolbar,menubar,status");
		var str = new XMLSerializer().serializeToString(newDoc);
		top.twin.document.write("<html><head><title>ASCII table</title></head><body><pre>"+str+"</pre></body></html>");
		top.twin.document.close();
		top.twin.focus();
	} catch  (e) {
		alert("Exception creating ASCII Table:"+e);
	}
}

function voLink(url) {
	var elem = document.getElementById('outputform');
	var votable = removeValAttribute(rd.filter.getDocument());		
	var str = new XMLSerializer().serializeToString(votable);
//	var str = new XMLSerializer().serializeToString(rd.filter.getDocument());
	document.getElementById("sources").value = str;
	elem.action = url;
	elem.submit();
}

function sendTo(url) {
	var form = document.getElementById('outputform');
	form.action = url;
	var votable = removeValAttribute(rd.filter.getDocument());		
	var str = new XMLSerializer().serializeToString(votable);
//	var str = new XMLSerializer().serializeToString(rd.filter.getDocument());
	document.getElementById("sources").value = str.replace(/\n/g,'').replace(/\'/g,"&apos;");

	var i1 = document.createElement("input");
	i1.type='hidden';
	i1.name='findResources';
	i1.value='1';
	form.appendChild(i1);
	var i2 = document.createElement("input");
	i2.type='hidden';
	i2.name='radius';
	i2.value='15';
	form.appendChild(i2);
	document.getElementById("units").value = 'arcmin';

	if ( document.getElementById('toolName') ) {
		document.getElementById('toolName').value='sources';
	} else {
		var inp = document.createElement("input");
		inp.type='hidden';
		inp.name='toolName';
		inp.value='sources';
		form.appendChild(inp);
	}
	form.submit();
}

function nullFunc() {
    // Do nothing (as a callback perhaps)
    return false;
}
function removeValAttribute(votable) {
	var xsltString = "<?xml version='1.0' encoding='UTF-8'?>" +
		"<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'" +
		"   xmlns:vo='http://www.ivoa.net/xml/VOTable/v1.1' " +
		"   xmlns:v1='http://vizier.u-strasbg.fr/VOTable'" +
		"   xmlns:v2='http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd'" +
		"   xmlns:v3='http://www.ivoa.net/xml/VOTable/v1.0'" +
		"   xsl:exclude-result-prefixes='vo v1 v2 v3'" +
		"   version='1.0' >" +
		"<xsl:output method='xml' />" +
		"<xsl:template match='TD|vo:TD|v1:TD|v2:TD|v3:TD'>" +
		"	<xsl:copy>" +
		"		<xsl:value-of select='.' />" +
		"	</xsl:copy>" +
		"</xsl:template>" +
		"<xsl:template match='@*|node()'>" +
		"	<xsl:copy>" +
		"		<xsl:apply-templates select='@*|node()'/>" +
		"	</xsl:copy>" +
		"</xsl:template>" +
		"</xsl:stylesheet>";

	try {
		var xsltp   = new XSLTProcessor();
		var xsltDom = (new DOMParser()).parseFromString(xsltString, "text/xml");  
		xsltp.importStylesheet(xsltDom);
		var new_votable = xsltp.transformToDocument(votable);
	} catch (e) {
		alert("Exception:"+e);
	}
	return new_votable;
}
