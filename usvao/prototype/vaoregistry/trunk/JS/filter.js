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

    var currentDoc = baseDocument;
    this.constraints = {'userIDs': [],
                        'userConstraints': [],
                        'userTypes': [],
                        'filterText': "",
                        'userText': ""};
                                              
    var me = this;

    this.getDocument = function() {
        var filterText = xslProc.getParameter(null,"filterText") || ""; 
        if (this.constraints.filterText != filterText) {        
            // update document if cached version is out of sync
            var filterTypes = xslProc.getParameter(null,"filterTypes");
            this.filterByColumn({"filterText": filterText, "filterTypes": filterTypes});
        }
        return currentDoc;
    };

    this.filterByColumn = function(form) {
        // filter columns using current form values
        // returns true if results have changed

        var ff = getConstraints(form);
        if (ff.userIDs.length == 0) {
            // No filtering, so just use the original data.
            if (! xslProc.getParameter(null, "filterText")) {
                // no change in filter settings
                return false;
            } else {
                xslProc.setParameter(null, "filterText", "");
                xslProc.setParameter(null, "filterTypes", "");
                xslProc.setParameter(null, "userText", "");
                currentDoc = baseDocument;
                return true;
            }

        } else {
            var oldfilter = xslProc.getParameter(null, "filterText") || "";
            if (ff.filterText == oldfilter) {
                // no change in filter settings
                return false;
            }
            
            xslProc.setParameter(null, "filterText", ff.filterText);
            xslProc.setParameter(null, "filterTypes", ff.userTypes.join(","));
            xslProc.setParameter(null, "userText", ff.userText);

            var newDoc;
            var xsltString;
            try {
                xsltString = xslt(ff.userIDs, ff.userConstraints, ff.userTypes);
//              alert("xsltString is:\n"+xsltString);
//var s = xsltString.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/\'/g,'&apos;').replace(/"/g,'&quot;');
//debug('<pre>' + s + '</pre>');

                // Get an XSL processor
                var xsltp      = new XSLTProcessor();

                // Not sure if the following statement is required.
                // Just copied it -- maybe it does some global initializations.
                var xsltDom    = Sarissa.getDomDocument();
                xsltDom        = (new DOMParser()).parseFromString(xsltString, "text/xml");
                xsltp.importStylesheet(xsltDom);

                // This does the transformation
                newDoc = xsltp.transformToDocument(baseDocument);
                var rows = newDoc.getElementsByTagName("TR");
                if (rows.length == 0) {
                    alert("No rows in result. Table is not updated.\n\n"+
                          "This can also result from a malformed query, e.g.,"+
                          "inequalities or ranges in a character column.");
//                  var ser = new XMLSerializer();
//                  var str = ser.serializeToString(newDoc);
//                  alert("New doc is:"+str);
                    return false;
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
                return false;
            }
            currentDoc = newDoc;
        }
        return true;
    };

    this.clear = function(form) {

        var changed =  clearConstraints(form);
        if (changed) {
            me.clearXSL();
        }
        currentDoc = baseDocument;
        return changed;
    };

    this.clearXSL = function() {
        xslProc.setParameter(null, "filterText", "");
        xslProc.setParameter(null, "filterTypes", "");
        xslProc.setParameter(null, "userText", "");
    };

    // local functions and variables

    function getConstraints(form) {
        // Extract filtering parameters from form
        // form can be either an HTML DOM <form> element or a dictionary
        // containing the filterText and filterTypes parameters
        var userIDs = [];
        var userTypes = [];
        var userConstraints = [];
        var userTextConstraints = [];
        if (! form) {
            // use current constraints if the form is not available
            return me.constraints;
        } else if (form.elements) {
            for (var j=0; j<form.elements.length; j++) {
                var el = form.elements[j];
                if (el.tagName == "INPUT" && el.type == "text") {
                    var constraint = el.value;
                    if (constraint) {
                        // field number is in trailing digits
                        var i = parseInt(el.name.replace(/.*[^0-9]/,""));
                        userIDs.push(i);
                        userTextConstraints.push(constraint);
                        //add wildcard-surround unless user has entered wildcards of their own
                        //or user is surrounding text with " or ' quotes.
                        //I'm sure we could add some finesse on the interpretations of user input here.
                        if( el.title.match("Character column") && 
                            constraint.length > 0 && constraint.indexOf('*') == -1  && 
                            constraint.indexOf('\"') == -1 && constraint.indexOf('\'') == -1) {
                            userConstraints.push( "*" + constraint + "*" );
                        }
                        else if( el.title.match("Character column") && 
                            constraint.length > 0 && constraint.indexOf('*') == -1  && 
                            ( (constraint.indexOf('\"') == 0 && constraint.lastIndexOf('\"') == constraint.length - 1) ||
                              (constraint.indexOf('\'') == 0 && constraint.lastIndexOf('\'') == constraint.length - 1) ) ){
                            userConstraints.push( constraint.substring(1,constraint.length - 1) );
                        }
                        else {
                            userConstraints.push(constraint);
                        }
                        var v = form.elements[el.name+"_type"].value;
                        if (trim(v.toLowerCase()) == "false") {
                            v = false;
                        } else {
                            v = true;
                        }
                        userTypes.push(v);
                    }
                }
            }
            if (userIDs.length > 0) {
                var filterText = new Array(userIDs.length);
                var userText = new Array(userIDs.length);
                for (var i=0; i<userIDs.length; i++) {
                    filterText[i] = userIDs[i] + ":" + userConstraints[i];
                    userText[i] = userIDs[i] + ":" + userTextConstraints[i];
                }
                filterText = "|"+filterText.join("|")+"|";
                userText = "|" + userText.join("|") + "|";
            } else {
                filterText = "";
                userText = "";
            }
        } else {
            // Dictionary parameter
            var filterText = form.filterText;
            if (filterText) {
                var fields = filterText.substring(1,filterText.length-1).split('\|');
                var types = form.filterTypes.split(',');
                for (var i=0; i<fields.length; i += 1) {
                    var index = fields[i].indexOf(':');
                    if (index > 0) {
                        userIDs.push(parseInt(fields[i].substring(0,index)));
                        userConstraints.push(fields[i].substring(index+1));
                        userTypes.push(types[i]=='true');
                    }
                }
            }
        }
        me.constraints = {'userIDs': userIDs,
                        'userConstraints': userConstraints,
                        'userTypes': userTypes,
                        'filterText': filterText,
                        'userText': userText};
        return me.constraints;
    }

    function clearConstraints(form) {
        // Clear filtering parameters in form
        // Returns true if any values are changed
        var changed = false;
        if (form) {
            for (var j=0; j<form.elements.length; j++) {
                var el = form.elements[j];
                if (el.tagName == "INPUT" && el.type == "text" && el.value != "") {
                    el.value = null;
                    changed = true;
                }
            }
        }
        if (me.constraints) {
            me.constraints = {'userIDs': [],
                            'userConstraints': [],
                            'userTypes': [],
                            'filterText': "",
                            'userText': ""};
            changed = true;
        }
        return changed;
    }
}
