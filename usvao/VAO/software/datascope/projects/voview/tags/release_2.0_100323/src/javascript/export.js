function export_to(where) {
   switch(where){
      case 'ascii':     return renderAscii();
      case 'xml':       return renderXML();
      case 'sqdev':     return sendTo('http://heasarcdev.gsfc.nasa.gov/cgi-bin/vo/squery/query.pl');
      case 'sq':        return sendTo('@SimpleQuery@');
      case 'vim':       return sendTo('@VIM@', true);
      case 'inventory': return sendTo('@Inventory@');
   }
}

// it seems that bouncing the votable off the server is the only
// way to really make it a legitimate xml file
// may want to do this with the text version as well?
function renderXML() {
   var form = document.getElementById('outputform');
   form.action = "votable2xml.pl";
   try {
      var votable = removeValAttribute(rd.filter.getDocument());
      var str = new XMLSerializer().serializeToString(votable);
//      document.getElementById("sources").value = str;
         form.elements['sources'].value = str;
   } catch (e) {
      alert("Exception:"+e);
   }
   form.submit();
   return false;
}

// doesn't render correctly on all browsers
function renderXML_JS() {
   try {
      var votable = removeValAttribute(rd.filter.getDocument());
      top.twin = window.open(null);   // opens new tab when given no options
//      top.twin.document.open('Content-type: text/xml\nContent-disposition: attachment; filename=test.xml\n\n');
      top.twin.document.open('Content-type: text/xml');
      top.twin.document.title = "VO Table (Cut-n-Paste the page source)";
      top.twin.document.writeln(new XMLSerializer().serializeToString(votable));
      top.twin.document.close();
      top.twin.focus();
   } catch (e) {
      alert("Exception creating XML:"+e);
   }
}

function renderAscii() {
   var xsltString = "<?xml version='1.0' encoding='UTF-8'?>" +
      "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'" +
      "   xmlns:vo='http://www.ivoa.net/xml/VOTable/v1.1' " +
      "   xmlns:v1='http://vizier.u-strasbg.fr/VOTable'" +
      "   xmlns:v2='http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd'" +
      "   xmlns:v3='http://www.ivoa.net/xml/VOTable/v1.0'" +
      "   exclude-result-prefixes='vo v1 v2 v3'" +
      "   version='1.0' >" +
      "<xsl:output method='text' />" +
      "<xsl:template match='/' >" +
      " <xsl:for-each select='//FIELD|//vo:FIELD|//v1:FIELD|v2:FIELD|v3:FIELD'>" +
      "  <xsl:choose>" +
      "   <xsl:when test='@name'>" +
      "    <xsl:value-of select='@name' /><xsl:value-of select='string(\"|\")' />" +
      "   </xsl:when>" +
      "   <xsl:when test='@id'>" +
      "    <xsl:value-of select='@id' /><xsl:value-of select='string(\"|\")' />" +
      "   </xsl:when>" +
      "   <xsl:when test='@ID'>" +
      "    <xsl:value-of select='@ID' /><xsl:value-of select='string(\"|\")' />" +
      "   </xsl:when>" +
      "   <xsl:otherwise>" +
      "    <xsl:value-of select='position()' /><xsl:value-of select='string(\"|\")' />" +
      "   </xsl:otherwise>" +
      "  </xsl:choose>" +
      " </xsl:for-each>" +
      " <xsl:value-of select='string(\"&#xA;\")' />" +
      " <xsl:value-of select='string(\"&#xA;\")' />" +
      " <xsl:for-each select='//TR|//vo:TR|//v1:TR|//v2:TR|//v3:TR'>" +
      "  <xsl:for-each select='TD|vo:TD|v1:TD|v2:TD|v3:TD'>" +
      "   <xsl:value-of select='.' /><xsl:value-of select='string(\"|\")' />" +
      "  </xsl:for-each>" +
      "  <xsl:value-of select='string(\"&#xA;\")' />" +
      " </xsl:for-each>" +
      "</xsl:template>" +
      "</xsl:stylesheet>";

   try {
      var xsltp   = new XSLTProcessor();
      var xsltDom     = (new DOMParser()).parseFromString(xsltString, "text/xml");  
      xsltp.importStylesheet(xsltDom);
      var newDoc     = xsltp.transformToDocument(rd.filter.getDocument());
      if (top.twin) {
         top.twin.close();
      }
      top.twin = window.open(null);
      var str = new XMLSerializer().serializeToString(newDoc);
      top.twin.document.write("<html><head><title>ASCII table</title></head><body><pre>"+str+"</pre></body></html>");
      top.twin.document.close();
      top.twin.focus();
   } catch  (e) {
      alert("Exception creating ASCII Table:"+e);
   }
}

function sendTo(url, noRadius) {
   var form = document.getElementById('outputform');
   form.action = url;
   form.target = Math.random();   // create a random target for window to be opened
   var votable = removeValAttribute(rd.filter.getDocument());              
   var str = new XMLSerializer().serializeToString(votable);
   form.elements['sources'].value = str.replace(/\n/g,'').replace(/\'/g,"&apos;");
//   document.getElementById("sources").value = str.replace(/\n/g,'').replace(/\'/g,"&apos;");
   if (form.elements['radius']) {
       if (noRadius) {
           form.elements['radius'].value = null;
       } else {
           form.elements['radius'].value = 10;
       }
   }
   
//   for (var el in form.elements) {
//       alert("Element is:"+el);
//       alert("Element:"+el.name+" -> " +el.value);
//   }
//   alert("sources is:"+form.elements['sources'].value);
   form.submit();
}

function removeValAttribute(votable) {
   var xsltString = "<?xml version='1.0' encoding='UTF-8'?>" +
      "<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'" +
      "   xmlns:vo='http://www.ivoa.net/xml/VOTable/v1.1' " +
      "   xmlns:v1='http://vizier.u-strasbg.fr/VOTable'" +
      "   xmlns:v2='http://vizier.u-strasbg.fr/xml/VOTable-1.1.xsd'" +
      "   xmlns:v3='http://www.ivoa.net/xml/VOTable/v1.0'" +
      "   exclude-result-prefixes='vo v1 v2 v3'" +
      "   version='1.0' >" +
      "<xsl:output method='xml' />" +
      "<xsl:template match='TD|vo:TD|v1:TD|v2:TD|v3:TD'>" +
      "   <xsl:copy><xsl:value-of select='.' /></xsl:copy>" +
      "</xsl:template>" +
      "<xsl:template match='@*|node()'>" +
      "<xsl:copy><xsl:apply-templates select='@*|node()'/></xsl:copy>" +
      "</xsl:template>" +
      "</xsl:stylesheet>";

   try {
      var xsltp   = new XSLTProcessor();
      var xsltDom = (new DOMParser()).parseFromString(xsltString, "text/xml");  
      xsltp.importStylesheet(xsltDom);
      var new_votable = xsltp.transformToDocument(votable);
   } catch (e) {
      alert("Exception removing val attribute:"+e);
   }
   return new_votable;
}
