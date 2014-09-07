// Get and apply an XSLT filter to the rows of a table.
// The filter values are stored in the XSLT Processors parameters
// The filter text is of the form:
//   |id1:test1|id2:test2|...|
// where idN is the column number (1-indexed) and testN is the filter.
// The leading pipe allows easy identification of the |id: syntax
// for all elements including the first.

// The types are stored a sequence of 'true/false' values separated
// by commas.  True indicates a character column.
// TAM 2007-9-11

function XSLTFilter(baseDocument, xslProc) {

    this.filter      = filterByColumn;
    this.update      = filterTextUpd;
    this.clear       = filterClear;
    this.getDocument = getCurrentDoc;
    this.clearXSL    = clearXSLParam;
    var currentDoc   = baseDocument;
    this.getValid    = getValidFlag;
    
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
        var types  = xslProc.getParameter(null, "filterTypes");
    
        // Are there any filter fields?
        if (fields.length > 0) {
            fields = fields.substring(1); // Get rid of leading and trailing pipes.
	    fields = fields.substring(0, fields.length-1);
        }
	
        // Split the fields
        fields = fields.split('\|');
        types  = types.split(',');
    
        var userConstraints = new Array();
        var userIDs         = new Array();
        var userTypes       = new Array();
    
        for (var i=0; i<fields.length; i += 1) {
	    var index = fields[i].indexOf(':');
	    if (index > 0) {
	        userConstraints.push(fields[i].substring(index+1));
	        userIDs.push(fields[i].substring(0,index));
	        userTypes.push(types[i]=='true');
	    }
        }
    
        // No filtering, so just use the original data.
        if (userIDs.length == 0) {
	    currentDoc = baseDocument;
	
        } else {
	    var newDoc;
	    var xsltString;
	    try {
                xsltString = xslt(userIDs, userConstraints, userTypes);
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
	        var rows       = newDoc.getElementsByTagName("TR");
	        if (rows.length == 0) {
	            alert("No rows in result. Table is not updated.\n\n"+
		          "This can also result from a malformed query, e.g.,"+
			  "inequalities or ranges in a character column.");
//		    var ser = new XMLSerializer();
//		    var str = ser.serializeToString(newDoc);
//		    alert("New doc is:"+str);
		    return;
	        }
	    } catch (e) {
	        alert("Error in filtering.  Invalid syntax on field criteria?\n\n"+
	          "For numeric columns use >,>=,=,<,<= or range.\n"+
	          "   >30  or  30..50\n"+
	          "   The = operator is optional."+
	          "Character fields support only matchs which may"+
	          "include wildcards (*).\n"+
	          "   Zwicky    or    3C*273\n"+
	          "If no wildcards are specified use =xxx to force\n"+
	          "an exact match.  Otherwise all rows matching at\n"+
	          "the beginning will match (i.e., '3C' matches '3C273'\n\n"+
	           e);
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
    function filterTextUpd(num, type) {

        var filterText  = "|";
        var filterTypes = "";
    
        // Get any old information first.  We especially
        // need the types of old columns since we only
        // get that when that column is modified, but
        // we'll need it later when some other column is modified.
    
        var oldText  = xslProc.getParameter(null, "filterText");
        var oldTypes = xslProc.getParameter(null, "filterTypes");
    
        // Get all the type information that we currently have.
        var typeArray = new Array();
        if (oldText && oldText.length > 0) {
            oldFields = oldText.split("\|");
            oldTypes  = oldTypes.split(",");
            for (var i=0; i<oldTypes.length; i += 1) {
                var data = oldFields[i+1].split(':')
                typeArray[Number(data[0])] = oldTypes[i];
	    }
        }
    
        // Update the type array for the column that we have
        // just modified.
        typeArray[num] = type;
        var index      = 1;
    
        // Build the current filter state from the current constraints.
        while (  (elem=document.getElementById("vovfilter"+index)) != null) {
            if (elem.value  && elem.value.length > 0) {
	        filterText += index+":"+elem.value+"|";
	        if (filterTypes) {
	            filterTypes += ",";
	        }
	        filterTypes += typeArray[index];
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
	        xslProc.setParameter(null, "filterTypes", filterTypes);
	    
            } else {
	        // User has removed filters.
	    
                xslProc.setParameter(null, "filterText", "");
	        xslProc.setParameter(null, "filterTypes", "");
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
	xslProc.setParameter(null, "filterTypes", "");
    }
}
    
