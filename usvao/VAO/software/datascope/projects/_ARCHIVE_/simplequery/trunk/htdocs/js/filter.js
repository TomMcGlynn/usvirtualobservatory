// Get and apply an XSLT filter to the rows of a table.
// The filter values are stored in the XSLT Processors parameters
// The filter text is of the form:
//   |id1:test1|id2:test2|...|
// where idN is the column number (1-indexed) and testN is the filter.
// The leading pipe allows easy identification of the |id: syntax
// for all elements including the first.

// TAM 2007-9-11

function XSLTFilter(baseDocument, xslProc, xslBase) {

    this.filter      = filterByColumn;
    this.xslBaseDir  = xslBase;
    this.update      = filterTextUpd;
    this.clear       = filterClear;
    this.getDocument = getCurrentDoc;
    this.clearXSL    = clearXSLParam;
    var currentDoc   = baseDocument;
    this.getValid    = getValidFlag;
    
    this.types       = new Array();
    this.scales      = new Array();
    
    var  lastValid   = true; 
    
    function getCurrentDoc() {
        return currentDoc;
    }
    
    function getValidFlag() {
        var oldValid = lastValid;
        lastValid = true;
	return oldValid;
    }
    
    function filterByColumn() {

        // Get the filter data.
        var fields = xslProc.getParameter(null, "filterText");
	if (!fields) {
	    fields = "";
	}
    
        // Are there any filter fields?
        if (fields && fields.length > 0) {
            fields = fields.substring(1); // Get rid of leading and trailing pipes.
	    fields = fields.substring(0, fields.length-1);
        }
	
        // Split the fields
        fields = fields.split('\|');
    
        var userConstraints = new Array();
        var userIDs         = new Array();
	var userTypes       = new Array();
	var userScales      = new Array();
    
        for (var i=0; i<fields.length; i += 1) {
	    var index = fields[i].indexOf(':');
	    if (index > 0) {
	        var constr = fields[i].substring(index+1);
		var id     = fields[i].substring(0, index);
	        userConstraints.push(constr);
	        userIDs.push(id);
		userTypes.push(this.types[id]);
		userScales.push(this.scales[id]);
	    }
        }
    
        // No filtering, so just use the original data.
        if (userIDs.length == 0) {
	    currentDoc = baseDocument;
	
        } else {
	    var newDoc;
	    var xsltString;
	    try {
                xsltString = xslt(this.xslBaseDir, userIDs, userConstraints, userTypes, userScales);
//	        alert("xsltString is:\n"+xsltString);

                // Get an XSL processor
	        var xsltp      = new XSLTProcessor();
	
	        // Not sure if the following statement is required.
	        // Just copied it -- maybe it does some global initializations.
	        var xsltDom    = Sarissa.getDomDocument();
                xsltDom        = (new DOMParser()).parseFromString(xsltString, "text/xml");  
	        xsltp.importStylesheet(xsltDom);
	
	        // This does the transformation
	        newDoc     = xsltp.transformToDocument(baseDocument);
//	        var rows   = newDoc.getElementsByTagName("TR");
//		alert("Rows are:"+rows.length);
//	        if (rows.length == 0) {
//	            alert("No rows in result. Table is not updated.\n\n"+
//		          "This can also result from a malformed query, e.g.,"+
//			  "inequalities or ranges in a character column.");
//		    var ser = new XMLSerializer();
//		    var str = ser.serializeToString(newDoc);
//		    alert("New doc is:"+str);
//		    return;
//	        }
	    } catch (e) {
	        alert("Error filtering data.\n\n"+
	          "For numeric columns use >,>=,=,<,<= or range:\n"+
	          "   e.g., >30  or  30..50\n\n"+
	          "Character fields support only matches:\n"+
	          "   e.g., Zwicky  or  3C*273\n");
	        return;
	    }
	    currentDoc = newDoc;
	}
    }


    // This function saves any filtering constraints as parameters
    // to the XSLT processor.  They are needed there to make
    // sure that the constraints boxes are written properly, but
    // this is also where the filtering function finds them.
    //
    //  num   -- The one-based index of the column.
    //  type  -- true means character, false means numeric\
    function filterTextUpd(num, type, scale) {
    
        this.types[num] = type;
	if (scale) {
	    this.scales[num] = scale;
	} else {
	    this.scales[num] = 0;
	}

        var filterText  = "|";
        var oldText  = xslProc.getParameter(null, "filterText");
    
        var index    = 1;
        // Build the current filter state from the current constraints.
        while (  (elem=document.getElementById("vovfilter"+index)) != null) {
            if (elem.value  && elem.value.length > 0) {
	        filterText += index+":"+elem.value+"|";
	    }
            index += 1;
        }

        if ( (filterText.length > 1  && oldText != filterText)  ||
             (filterText.length == 1 && oldText && oldText.length > 1)
             ) {
            // The filters have changed so we want to re-filter.
	
            if (filterText.length > 1) {
	        // There are filters.
	        xslProc.setParameter(null, "filterText", filterText);
	    
            } else {
	        // User has removed filters.
                xslProc.setParameter(null, "filterText", "");
	    }
	    return true;
	}
	return false;
    }

    function filterClear() {

        var i      = 1;
        var change = false;
    
        while ( elem=document.getElementById("vovfilter"+i) ) {
            if (elem.value && elem.value.length > 0) {
	        elem.value = null;
	        change = true;
	    }
	    i += 1
        }
    
        if (change) {
	    clearXSLParam();
	}
	currentDoc = baseDocument;
	return change;
    }
    
    function clearXSLParam() {
        xslProc.setParameter(null, "filterText", "");
    }
}
