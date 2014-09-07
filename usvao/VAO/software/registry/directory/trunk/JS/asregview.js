// VOTable viewer
// R. White, 2007 October 25

//  GRG Modified:
//  (1) loadData function for using registry VOTkeyword webservice
//  (2) view2xslt for single voview.xsl assignment,  no switching

function changeExampleText(){
    var sql = document.getElementById('sql');
    sql.value = document.getElementById('ddExamples').value;
}

function clearExampleDropDown(){
    var dropdown = document.getElementById('ddExamples');
    dropdown.value = "";
}

function getTextContent(el) {
	var txt = el.textContent;
	if (txt != undefined) {
		return txt;
	} else {
		return getTCRecurs(el);
	}
}

function getTCRecurs(el) {
	// recursive method to get text content of an element
	// used only if the textContent attribute is not defined (e.g., in Safari)
	var x = el.childNodes;
	var txt = '';
	for (var i=0, node; node=x[i]; i++) {
		if (3 == node.nodeType) {
			txt += node.data;
		} else if (1 == node.nodeType) {
			txt += getTCRecurs(node);
		}
	}
	return txt;
}

function getElementsByClass(searchClass,node,tag) {
	var classElements = new Array();
	if (node == undefined) node = document;
	if (tag == undefined) tag = '*';
	var els = node.getElementsByTagName(tag);
	var elsLen = els.length;
	var pattern = new RegExp("(^|\\s)"+searchClass+"(\\s|$)");
	for (i = 0, j = 0; i < elsLen; i++) {
		if (pattern.test(els[i].className) ) {
			classElements[j] = els[i];
			j++;
		}
	}
	return classElements;
}

// remove a blank-delimited string sub from string s
// if sub occurs multiple times, all are removed
// also normalizes the string by removing blanks

function removeSubstring(s,sub) {
	var flist = s.split(' ');
	var glist = [];
	for (var i=0, f; f = flist[i]; i++) {
		if (f && f != sub) {
			glist.push(f);
		}
	}
	return glist.join(' ');
}

// Validates that a string contains only valid numbers.
// Returns true if valid, otherwise false.

function validateNumeric(strValue) {
	var objRegExp  =  /^\s*(([-+]?\d\d*\.\d*$)|([-+]?\d\d*$)|([-+]?\.\d\d*))\s*$/;
	return objRegExp.test(strValue);
}

// pack form parameters into a GET string

function getFormPars(formname) {
	if (typeof(formname) == "string") {
		var form = document.forms[formname];
	} else {
		form = formname;
	}
	var parlist = [];
	for (var i=0; i<form.elements.length; i++) {
		var el = form.elements[i];
		if (el.tagName == "INPUT" || el.tagName == "TEXTAREA") {
			var value = encodeURIComponent(el.value);
			if (el.type == "text" || el.type == "hidden" || el.type == "textarea") {
				parlist.push(el.name + "=" + value);
			} else if (el.type == "checkbox") {
				if (el.checked) {
					parlist.push(el.name + "=" + value);
				} else {
					parlist.push(el.name + "=");
				}
			} else if (el.type == "radio") {
				if (el.checked) {
					parlist.push(el.name + "=" + value);
				}
			}
		} else if (el.tagName == "SELECT") {
			parlist.push(el.name + "=" + encodeURIComponent(el.options[el.selectedIndex].value));
		}
	}
	return parlist.join("&");
}

// extract form parameters from a GET string and set form values

function setFormPars(formname,getstr) {
	if (typeof(formname) == "string") {
		var form = document.forms[formname];
	} else {
		form = formname;
	}
	var parlist = getstr.split("&");
	for (var i=0; i<parlist.length; i++) {
		var f = parlist[i].split("=");
		if (f.length < 2) {
			var name = parlist[i];
			var value = "";
		} else {
			// don't know if embedded '=' can happen, but might as well handle it
			name = f.shift();
			value = decodeURIComponent(f.join("="));
		}
		var el = form[name];
		if (el != undefined) {
			if (el.tagName == "INPUT") {
				    el.value = value;
			} else if (el.tagName == "SELECT") {
				for (var j=0; j < el.options.length; j++) {
					var option = el.options[j];
					if (option.value == value) {
						option.selected = true;
					} else {
						option.selected = false;
					}
				}
			} else if (el.length > 0) {
				// radio buttons
				for (j=0; j < el.length; j++) {
					if (el[j].value == value) {
						el[j].checked = true;
					} else {
						el[j].checked = false;
					}
				}
			}
		}
	}
}

// pack hash table (dictionary) values into a URI-encoded string

function encodeHash(dict) {
	var s = [];
	for (var p in dict) {
		s.push(p + '=' + dict[p]);
	}
	return encodeURIComponent(s.join("$"));
}

// unpack hash table from URI-encoded string

function decodeHash(value) {
	var s = decodeURIComponent(value).split("$");
	var dict = {};
	for (var i=0; i<s.length; i++) {
		var p = s[i];
		var f = p.split("=");
		if (f.length == 1) {
			if (p) dict[p] = undefined;
		} else if (f.length == 2) {
			dict[f[0]] = f[1];
		} else {
			var field = f.shift();
			dict[field] = f.join("=");
		}
	}
	return dict;
}

function readdata(output, searchform, searchparam, capparam, titleparam, shortnameparam, idparam, pubparam, subparam, waveparam) {

	// Note all initialization is at the end (after the methods are defined)

	var me = this;

	this.clearOutput = function(el) {
		while (this.output.hasChildNodes()) {
			this.output.removeChild(this.output.firstChild);
		}
		if (el) {
			this.output.appendChild(el);
		}
	};

	this.setTitle = function(innerHTML) {
		// set title in the output section
		if (innerHTML == undefined) {
			this.title.innerHTML = "<i> Advanced Search </i>";
		} else {
			this.title.innerHTML = innerHTML;
		}
	};

	this.setWindowTitle = function() {
		// set window title to include name
		if (this.queryTitle) {
			window.document.title = "VOTable Viewer (" + this.queryTitle + ")";
		} else {
			window.document.title = "VOTable Viewer";
		}
	};

	this.clearPageInfo = function() {
		this.sortColumn = undefined;
		//this.maxColumns = undefined;
		//This was the original default. IE clears before first display.
		this.maxColumns = 7;
		this.page = 1;
	};

	this.clearForm = function() {
		// reset the form and restore most things to default state

		// start with a blank line and empty display
		this.setTitle("&nbsp;");
		this.clearOutput();

		this.view = this.defaultView;
		this.xml = undefined;
		// extra XSLT parameters
		this.xslParams = {};
		this.params = undefined;
		this.cparams = undefined;
		this.tparams = undefined;
		this.shparams = undefined;
		this.iparams = undefined;
		this.pparams = undefined;
		this.sparams = undefined;
		this.wparams = undefined;

		this.selectedRows = [];

		this.pageLength = 20;
		this.clearPageInfo();
		this.sortToggle = true;
		this.SaveOption = "AllAsCSV";
		this.SendOption = "";

		this.form.reset();
		// set id="selected" for the default view
		var el = document.getElementById("selected");
		if (el) el.removeAttribute("id");
		el = document.getElementById(this.defaultView);
		while (el && el.tagName != "LI") el= el.parentNode;
		if (el) el.id = "selected";
	};

	this.loadData = function() {
		var searchparams    = this.searchparams.value;
		var capparams = this.capparams.value;
		var titleparams = this.titleparams.value;
		var shortnameparams = this.shortnameparams.value;
		var idparams = this.idparams.value;
		var pubparams = this.pubparams.value;
		var subparams = this.subparams.value;
		var waveparams = this.waveparams.value;
		
		if( searchparams == "" && capparams == "" && titleparams == "" && idparams == ""
		    && pubparams == "" && subparams == "" && waveparams == "" && shortnameparams == "")
		    return;
		if (searchparams != this.searchparams || capparams != this.capparams || 
		    titleparams != this.titleparams || pubparams != this.pubparams ||
		    idparams != this.idparams || subparams != this.subparams ||
		    waveparams != this.waveparams || shortnameparams != this.shortnameparams ) {
			this.errorMessage("Searching...");
			if (this.filter) {
				// clear filter with a new search
				this.filter.clearXSL();
				this.filter = undefined;
			}
			// clear list of selected rows with new search
			this.clearSelection();

			// save parameters for last search
			this.params  =  this.searchparams.value;
			this.cparams =  this.capparams.value;
			this.tparams =  this.titleparams.value;
			this.shparams = this.shortnameparams.value;
			this.iparams =  this.idparams.value;
			this.pparams =  this.pubparams.value;
			this.sparams =  this.subparams.value;
			this.wparams =  this.waveparams.value;
			
			this.setTitle();
			this.xml = undefined;
			// reset all sort/page info for new searches
			//this.clearPageInfo();
			
            var fullurl =  "/newdirectory/NVORegInt.asmx/VOTAdvancedSearch?capability=" + 
            encodeURIComponent(capparams) + "&predicate=" + encodeURIComponent(searchparams) + 
            "&title=" + encodeURIComponent(titleparams) + "&shortName=" + encodeURIComponent(shortnameparams) + 
            "&identifier=" + encodeURIComponent(idparams) + 
            "&publisher=" + encodeURIComponent(pubparams) + "&subject=" + encodeURIComponent(subparams) + 
            "&waveband=" + encodeURIComponent(waveparams);
			
            // Load XML
            this.VOloader.makeRequest(fullurl);
            
		} else if (! this.xml) {
			// Parameters are set but XML is not
			this.errorMessage("Searching...");
						
            var fullurl =  "/newdirectory/NVORegInt.asmx/VOTAdvancedSearch?capability=" + 
            encodeURIComponent(capparams) + "&predicate=" + encodeURIComponent(searchparams) + 
            "&title=" + encodeURIComponent(titleparams) + "&shortName=" + encodeURIComponent(shortnameparams) +
            "&identifier=" + encodeURIComponent(idparams) + 
            "&publisher=" + encodeURIComponent(pubparams) + "&subject=" + encodeURIComponent(subparams) + 
            "&waveband=" + encodeURIComponent(waveparams);
            
            // Load XML
            this.VOloader.makeRequest(fullurl);
		}

		//XXX Need this?  Or move to setView?
		// Call sort immediately if XML & XSL already exist or if they are not needed
		this.sortToggle = false;
		if ((!this.xsltfile) ||
			(this.xml != undefined &&
			 this.xslt != undefined)) {
			this.sort();
			 //this.clearPageInfo();
		}
		this.restoringState = false;
	};

	this.setXSLBase = function(dir) {
		this.xslBase = dir;
	};

	this.checkXSL = function() {
		// clear saved XSL if it is not what we need
		this.view = this.view || this.defaultView;
		var xsltfile = this.view2xslt[this.view];
		if (this.xslBase) {
			xsltfile = this.xslBase + "/" + xsltfile;
		}
		if (xsltfile != this.xsltfile) {
			this.xsltfile = xsltfile;
			this.xslt = undefined;
			this.myXslProc = undefined;
		}
	};

	this.loadXSL = function() {
		this.checkXSL(); // Not needed?
		if (this.xsltfile && !this.xslt) {
			// don't toggle the sort order on next call
			this.sortToggle = false;
			this.XSLloader.makeRequest(this.xsltfile);
		}
	};

	this.getParameter = function(namespace, name) {
		// get XSLT parameter
		return me.xslParams[name];
	};

	this.setParameter = function(namespace, name, value) {
		// set XSLT parameter
		me.xslParams[name] = value;
	};

	this.saveState = function() {
		// Save current state
		if (this.sortColumn) {
			var sortOrder = this.sortOrder[this.sortColumn];
		} else {
			sortOrder = '';
		}
		var pars = getFormPars(this.form);

		var state = this.view + '|' +
					encodeHash(this.xslParams) + '|' +
					pars;
		StateManager.setState(state);
		
		// change window title just after state change so it shows up correctly
		// in page history
		// This works in Safari and Firefox2 but seems random in Firefox1.5.
		// Still seems like the right approach though.
		this.setWindowTitle();
	};

	this.restoreState = function(e) {
		// Restore current state
		// Called on a state change
		var state = e.id;
		me.restoringState = true;
		if (state == StateManager.defaultStateID) {
			// reset to default state
			me.clearForm();
		} else {
			state = state.split('|');
			var newview = state[0];
			// don't toggle the sort order on first call
			me.sortToggle = false;
			if (state[2]) setFormPars(me.form, state[2]);

			filterByColumn(decodeHash(state[1]));
			
			// set id="selected" for the currently selected view
			var el = document.getElementById(newview);
			if (el) {
				me.setView(el);
			} else {
				me.setView(newview);
			}
		}
		me.restoringState = false;
	};

	this.clearState = function() {
		// Clear saved state
		StateManager.setState(StateManager.defaultStateID);
		return true;
	};

	this.setView = function(current) {
		var oldview = me.view;
		if (!current) {
			me.view = me.view || me.defaultView;
			current = document.getElementById(me.view);
		} else if (typeof(current) == "string") {
			// current gives the name of the new view
			me.view = current || me.view || me.defaultView;
			current = document.getElementById(me.view);
		} else {
			// current is an HTML element whose id is
			// the name of the new view
			me.view = current.id || me.defaultView;
		}

		// reset the currently selected element
		var el = document.getElementById("selected");
		if (el) el.removeAttribute("id");

		// set id="selected" for the currently selected view
		el = current;
		while (el && el.tagName != "LI") el= el.parentNode;
		if (el) el.id = "selected";

		// Finally, do the search and load the XSL (if necessary) and
		// display the results
		me.loadData();
		me.loadXSL();
		return false;
	};

	this.setViewParams = function() {
		// set additional parameters specific to the current view
		if (this.view == "Table") {
			if (this.maxColumns) {
				this.myXslProc.setParameter(null, "maxColumns", ""+this.maxColumns);
			} else {
				if (this.myXslProc.removeParameter) {
					this.myXslProc.removeParameter(null, "maxColumns");
				} else {
					// IE doesn't have removeParameter
					this.myXslProc.setParameter(null, "maxColumns", null);
				}
			}
		}
	};

	this.xslLoaded = function(data) {
		me.xslt = data;
		if (me.xml) me.sort();
	};

	this.getXML = function() {
		if (me.filter) {
			return me.filter.getDocument();
		} else {
			return me.xml;
		}
	};

	this.xmlLoaded = function(data) {
		// If params is null, back button was presumably used to
		// return to a blank page.  Simply ignore the XML data in
		// that case.
		if (me.params || me.cparams || me.tparams || me.shparams || me.iparams ||
		    me.pparams || me.sparams || me.wparams) {
			me.xml = data;
			me.filter = null;
			if (me.xslt) me.sort();
		}
	};

	this.showColumns = function(columns) {
		me.maxColumns = columns;
		me.sortToggle = false;
		me.sort();
	};
	
    function ConvertVOTableResultsToCSV(data) {
        var newString = "";
        
        //Header data.
        var startField = data.indexOf("<FIELD ID", 0);
        var startData = data.indexOf("<DATA>", 0);
        var lastField = data.lastIndexOf("<FIELD ID", startData);
                
        var textIndex;
        while( startField > -1 && startField < startData)
        {
            textIndex = data.indexOf("\"", startField) + 1;
            newString = newString + data.substring(textIndex, data.indexOf("\"", textIndex));
            
            if( startField == lastField )
            {
                newString = newString + "\n";
            }
            else
            {
                newString = newString + ",";
            }
            startField = data.indexOf("<FIELD ID", textIndex);
        }
        
        //Now iterate over the table data.
        var startTD = data.indexOf("<TD", data.indexOf("<TABLEDATA", startData));
        var endTR = data.indexOf("</TR>", startTD);
        var endTD;
        
        var tempString;
        while( startTD > -1 )
        {
            //if it is not an empty column, copy the data
            //To play it safe, quote-wrap all data columns.
            if( startTD != data.indexOf("<TD />", startTD) &&
                startTD != data.indexOf("<TD/>", startTD))
            {
                endTD = data.indexOf("</TD>", startTD);
                tempString = data.substring( startTD + 4, endTD).replace(/\"/g, "\"\"");            
                newString = newString + "\"" + tempString + "\"";
            }

            startTD = data.indexOf("<TD", startTD + 1); 
            if( startTD < endTR )
            {
                newString = newString + ",";
            }
            else
            {
                newString = newString + "\n";
                endTR = data.indexOf("</TR>", endTR + 1);
            }
        }
    
        return( newString );
    }
    
    function RemoveUnselectedRowsFromVOTable(data, selectedRows) {
        var startRow = data.indexOf("<TR", data.indexOf("<TABLEDATA", 0)); 
        var endTable = data.indexOf("</TABLEDATA", 0);
        var newString = data.substring(0, startRow);
        var foundAny = false;
        
        while( startRow > -1 ) {
            var startID = data.indexOf("<TD>ivo://", startRow) + 4;
            var endID = data.indexOf("</TD>", startID);
            var idtext = data.substring(startID, endID);  
            var nextRow = data.indexOf("<TR", startRow + 1 );
            
            //if this ID is in the selected list, copy the row to newstring
            var found = false;
            for( var i = 0, selector; i < selectedRows.length; i++ ) {
                selector = selectedRows[i];
                if( selector == idtext ) {
                    found = true;
                    foundAny = true;
                    break;
                }
            }
            if( found == true ) {
                if( nextRow > -1 )
                    newString = newString + data.substring(startRow, nextRow);
                else
                    newString = newString + data.substring(startRow, endTable);
            }
            startRow = nextRow;  
        }
        
        if( foundAny == false )
            return "";
        
        newString = newString + data.substring(endTable);
        return newString;
    }
    
    var request;
    function getRequestObject() {
        if (window.ActiveXObject) {
            return(new ActiveXObject("Microsoft.XMLHTTP"));
        } else if (window.XMLHttpRequest) {
            return(new XMLHttpRequest());
        } else {
            return(null);
        }
    }
    
    function sendResourceURL(content, actiontarget) {
    
        //we have a Too-large VO table. Make a list of identifiers out of it
        var identifiers = "";
        var whatsLeft = content;
        var currentIndex = 0;
        var currentEnd = 0;
        while( whatsLeft.length > 0 )
        {
            currentIndex = whatsLeft.indexOf(">ivo://");
            if( currentIndex == -1 )
                break;
            currentIndex += 7; //skip the tag and ivo://
            currentEnd = whatsLeft.indexOf("</", currentIndex);
            identifiers = identifiers + whatsLeft.substring(currentIndex, currentEnd) + "|";
            whatsLeft = whatsLeft.substring(currentEnd);
        }
        identifiers = identifiers.substring(0, identifiers.length -1);
    
        request = getRequestObject();
        request.onreadystatechange = handleResponse;
        request.open("POST", "savexml.aspx", true);
        request.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
        request.setRequestHeader("Connection", "close");
        request.send("resourceList=" + identifiers);
        
        //set this now while we know what the actiontarget is.
        document.getElementById("Interop").action = actiontarget;
        
    }
    function handleResponse() {
        if (request.readyState == 4) {
            document.getElementById("resourcesURL").value = request.responseText;
            document.getElementById("Interop").submit();     
        }
    }

    this.PostToNVOPage = function(nrows)
    {
        //are we posting this back, or posting this to a new target....
        var actiontarget = document.referrer;
        if( this.SendOption.length > 1 )
        {
             actiontarget = this.SendOption;
        }
        else
        {
           return;
        }

    
        var outputTable;
        var done = false;
        
            //first try selected.
            if( me.selectedRows.length > 0 ) 
            {
                var str = (new XMLSerializer).serializeToString(rd.filter.getDocument()); 
                outputTable = RemoveUnselectedRowsFromVOTable(str, me.selectedRows); 
                if( outputTable.length > 0 ) {
                    outputTable = outputTable.replace("encoding=\"UTF-16\"", "");   
                    done = true;
                }
            }      
            //then try just filtered.
            if ( done == false )
            {
                outputTable = (new XMLSerializer).serializeToString(rd.filter.getDocument());  
                if( outputTable.length > 0 )
                {
                    outputTable = outputTable.replace("encoding=\"UTF-16\"", "");   
                    done = true;
                }
            }
             
            //then try sending everything
            if( done == false )
            {                     
                outputTable = (new XMLSerializer).serializeToString(me.xml);    
            }
            
            //arbitrary cutoff to save locally and return url.
            //todo why wasn't .length working?
            if( outputTable[14336] != null ) //14k. inventory looks for 16k total message.
            {
                sendResourceURL(outputTable, actiontarget);
            }
            else
            {
                document.getElementById("Interop").action = actiontarget;
                document.getElementById("resources").value = outputTable;
                document.getElementById("Interop").submit();     
            }
    }    

    this.saveResults = function() {
        var format = me.SaveOption;
        if( format == 'AllAsCSV' )
        {
            var str = (new XMLSerializer).serializeToString(me.xml);    
            var newString = ConvertVOTableResultsToCSV(str);   
            document.getElementById("save").value = newString;
            document.getElementById("format").value = "csv";
            document.getElementById("outputform").submit();     
        }
        else if( format == 'AllAsXML' ) {
            var str = (new XMLSerializer).serializeToString(me.xml);        
            document.getElementById("save").value = str;
            document.getElementById("format").value = "xml";
            document.getElementById("outputform").submit();     
        }
        else if( format == 'SelectedAsCSV' ) {
            if( me.selectedRows.length > 0 ) {
                var str = (new XMLSerializer).serializeToString(rd.filter.getDocument()); 
                str = RemoveUnselectedRowsFromVOTable(str, me.selectedRows);
                if( str.length == 0 ) {
                    alert("No results selected using current filter.");
                    return;
                }

                str = ConvertVOTableResultsToCSV(str);   
                document.getElementById("save").value = str;
                document.getElementById("format").value = "csv";
                document.getElementById("outputform").submit(); 
            }
            else {
                alert("No results selected.");
            }
        }
        else if( format == 'SelectedAsXML' ) {
            if( me.selectedRows.length > 0 ) {
                var str = (new XMLSerializer).serializeToString(rd.filter.getDocument()); 
                str = RemoveUnselectedRowsFromVOTable(str, me.selectedRows); 
                if( str.length == 0 ) {
                    alert("No results selected using current filter.");
                    return;
                }

                str = str.replace("encoding=\"UTF-16\"", "");   
                document.getElementById("save").value = str;
                document.getElementById("format").value = "xml";
                document.getElementById("outputform").submit();
                }
            else {
                alert("No results selected.");
            }
        }
        else if( format == 'FilteredAsCSV' ) {
            var str = (new XMLSerializer).serializeToString(rd.filter.getDocument());    
            var newString = ConvertVOTableResultsToCSV(str);   
            document.getElementById("save").value = newString;
            document.getElementById("format").value = "csv";
            document.getElementById("outputform").submit();     
        }
        else if( format == 'FilteredAsXML' ) {
            var str = (new XMLSerializer).serializeToString(rd.filter.getDocument());  
            var newString = str.replace("encoding=\"UTF-16\"", "");   
            document.getElementById("save").value = newString;
            document.getElementById("format").value = "xml";
            document.getElementById("outputform").submit();     
        }
    };
        
	this.clearSelection = function() {
		if (me.selectedRows.length > 0) {
			while( me.selectedRows.length > 0 ) {
			    var name = me.selectedRows.pop();
			    var el = document.getElementById(name);
			    if( el != null ) //it may be on another page
			    {
			        var cclass = el.className;
				    if (cclass) {
					    el.className = removeSubstring(cclass,"selectedimage");
				    }
				    el = document.getElementById( "cb-" + name );
				    el.checked = false;
				}
			}
		    //clear the array.	
			me.selectedRows = [];
		}
		return true;
	};

	this.setSelection = function(selectors) {
		// set selection from a list or a comma-separated string of selectors
		this.clearSelection();
		return this.extendSelection(selectors);
	};

	this.extendSelection = function(selectors) {
		// extend current selection from a list or comma-separated string of selectors
		if (! selectors) return true;
		if (selectors.split) {
			// looks like a string
			me.selectedRows = me.selectedRows.concat(selectors.split(","));
		} else if (selectors.length) {
			// looks like a list
			me.selectedRows = me.selectedRows.concat(selectors);
		}
		// remove any duplicate selectors from the selection
		var uniq = [];
		var dict = {};
		for (var i=0, selector; i < me.selectedRows.length; i++) {
			selector = me.selectedRows[i];
			if (dict[selector] == undefined) {
				dict[selector] = 1;
				uniq.push(selector);
			}
		}
		// keep selectors in sorted order
		uniq.sort();
		me.selectedRows = uniq;
		return true;
	};

	this.selectRow = function(el,dataset) {
		var cclass = el.className;
		for (var i=0, f; i < me.selectedRows.length; i++) {
			if (me.selectedRows[i] == dataset) {
				// second click disables selection
				me.selectedRows.splice(i,1);
				if (cclass) {
					el.className = removeSubstring(cclass,"selectedimage");
				}
				return;
			}
		}
		// not in current selection, so add this to selection
		me.selectedRows.push(dataset);
		me.selectedRows.sort();
		if (cclass) {
			el.className = cclass + " selectedimage"; 
		} else {
			el.className = "selectedimage"; 
		}
	};

    this.setSaveOption = function(value) 
    {
        if((!value) || me.SaveOption == value) return;
            me.SaveOption = value;
    }
    
    this.setSendOption = function(value) 
    {
        if((!value) || me.SendOption == value) return;
            me.SendOption = value;
    } 
   
	this.setPageLength = function(pageLength) {
		// change number of rows per page
		if ((!pageLength) || me.pageLength == pageLength) return;
		var start = me.pageLength*(me.page-1);
		me.pageLength = pageLength;
		me.sort(undefined, undefined, Math.floor(start/pageLength)+1);
	};

	this.ShowFullDescID = function( id ) {
	    var properId = id.id;
	    if ((!properId) || me.showDescID == properId) return;
	    me.showDescID = properId;
	    me.sort(undefined, undefined, undefined, true);
	};
	
	this.HideFullDescID = function() {
	    me.showDescID = "";
	    me.sort(undefined, undefined, undefined, true);
	};
	
	this.GetReferrer = function () {
        return document.referrer;
    }

	

	this.sort = function(sortColumn, sortOrder, newpage, avoidtoggle) {
		if (me.xml == undefined || me.xslt == undefined) return false;

		if (newpage != undefined) me.page = newpage;
		// sort direction gets toggled only if the page does not change
		var pchanged = newpage != undefined;

		if (sortColumn == undefined) {
			sortColumn = me.sortColumn || "";
		}
		if (sortOrder == undefined) {
			if ( avoidtoggle == undefined && me.sortToggle && sortColumn == me.sortColumn && (! pchanged)) {
				// toggle sort order
				if (me.sortOrder[sortColumn] == "ascending") {
					sortOrder = "descending";
				} else {
					sortOrder = "ascending";
				}
			} else {
				// restore previous sort order or use default
				sortOrder = me.sortOrder[sortColumn] || "ascending";
			}
		}
		
		me.sortColumn = sortColumn;
		me.sortOrder[sortColumn] = sortOrder;
        me.sortToggle = true;

		// save state so back button works
		me.saveState();

		if (! me.myXslProc) {
			// Mozilla/IE XSLT processing using Sarissa
			if (!window.XSLTProcessor) return me.noXSLTMessage();

			me.myXslProc = new XSLTProcessor();
			if ((!me.myXslProc) || (!me.myXslProc.importStylesheet))
				return me.noXSLTMessage();
			// attach the stylesheet; the required format is a DOM object, and not a string
			me.myXslProc.importStylesheet(me.xslt);
		}
		if (!me.filter) {
			me.filter = new XSLTFilter(me.xml, me);
		}

		// do the transform
		me.myXslProc.setParameter(null, "sortOrder", sortOrder);
		me.myXslProc.setParameter(null, "sortColumn", sortColumn);
		me.myXslProc.setParameter(null, "page", ""+me.page);
		me.myXslProc.setParameter(null, "pageLength", ""+me.pageLength);
		me.myXslProc.setParameter(null, "showDescID", ""+me.showDescID);
		if (me.selectedRows) {
			me.myXslProc.setParameter(null, "selectedRows", me.selectedRows.join(","));
		}
		
//		if( document.referrer != null && document.referrer.length > 0 ) {
//		    me.myXslProc.setParameter(null, "referrer", document.referrer);
//		}
	    if( document.getElementById("referralURL").value != null ) 
	    {
		    me.myXslProc.setParameter(null, "referrer", document.getElementById("referralURL").value);
		}

		
		// set extra XSLT parameters
		for (var p in me.xslParams) {
			me.myXslProc.setParameter(null, p, me.xslParams[p]);
		}
		me.setViewParams();

		// create the HTML table and insert into document
		var finishedHTML = me.myXslProc.transformToFragment(me.getXML(), document);
		me.clearOutput();
		try {
			me.output.appendChild(document.adoptNode(finishedHTML));
		} catch (e) {
			try {
				me.output.appendChild(document.importNode(finishedHTML,true));
			} catch (e) {
				me.output.appendChild(finishedHTML);
			}
		}
		return false;
	};

	this.errorMessage = function(msg) {
		var p = document.createElement('p');
		p.innerHTML = msg;
		me.clearOutput(p);
		return false;
	};

	this.noXSLTMessage = function() {
		me.errorMessage("Sorry, your browser does not support XSLT -- try Firefox, Mozilla (version > 1.3), Safari 3 beta, Internet Explorer, or other compatible browsers.");
		return false;
	};

	this.loadingError = function(errmsg) {
		if (this.id == "data") {
			var label = "VOTable";
		} else {
			label = "XSL for "+this.view+" view";
		}
		msg = "Failed to load "+label+":\n"+errmsg;
		me.errorMessage(msg);
		me.saveState();
	};

	this.dataError = function(errmsg) {
		msg = "Failed to load VOTable:\n"+errmsg;
		me.errorMessage(msg);
		me.saveState();
	};

	this.xslError = function(errmsg) {
		msg = "Failed to load XSL for "+this.view+" view:\n"+errmsg;
		me.errorMessage(msg);
		me.saveState();
	};

	// *** State initialization ***

	this.defaultView = "Table";
	this.sortOrder = {};

	searchform = searchform || "searchForm";
	this.form = document.forms[searchform];
	if (! this.form) alert("Form "+searchform+" not found");
	    this.searchparams = this.form[searchparam];
	this.capparams = this.form[capparam];
	this.titleparams = this.form[titleparam];
	this.idparams = this.form[idparam];
	this.pubparams = this.form[pubparam];
	this.subparams = this.form[subparam];
	this.waveparams = this.form[waveparam];
	this.shortnameparams = this.form[shortnameparam];
	//tdower todo - what's up with these?

	this.xsltfile = undefined;
	this.xslt = undefined;
	this.filter = undefined;
	this.xslBase = undefined;
	this.SaveOption = "AllAsCSV";
	this.SendOption = "";

	// mapping from view choices to XSLT files
	// only one view, but leave this in to allow XSLT switching

	this.view2xslt = {
			"Table": "./js/regview.xsl"
			};

	// output has three parts, a title, a div for the XSL output and an (invisible) iframe
	while (output.hasChildNodes()) {
		output.removeChild(output.firstChild);
	}
	this.title = document.createElement("h3");
	// start with a blank line
	this.title.innerHTML = '&nbsp;';
	output.appendChild(this.title);

	this.output = document.createElement("div");
	output.appendChild(this.output);

	// Finish the initialization using clearForm
	this.clearForm();

	// create the event-handling loaders
	this.VOloader = new FSMLoader("data", "", this.xmlLoaded, this.loadingError);
	this.XSLloader = new FSMLoader("xsl", "", this.xslLoaded, this.loadingError);
}

// functions used in XSL-generated code
function trim(str) {
	return str.replace(/^\s*(\S*(\s+\S+)*)\s*$/, "$1");
}

// other javascript

function getRadioValue(button) {
	// get the value for a radio button input
	for (var i=0, option; option = button[i]; i++) {
		if (option.checked) {
			return option.value;
		}
	}
	return undefined;
}

// callbacks for selection list

function selectRow(el,dataset,event) {
	var ev = event || window.event;
	// don't select when links are clicked
	if (ev && ev.target && ev.target.tagName.toLowerCase() == "a") return;
	//XXX maybe this should be ev.srcElement for IE?
	rd.selectRow(el,dataset);
}

function selectRowFromCheckbox(el,dataset,event) {
	var ev = event || window.event;
	
	while (el && el.tagName != "TR") el= el.parentNode;
	var row = el;
	
	// don't select when links are clicked
	if (ev && ev.target && ev.target.tagName.toLowerCase() == "a") return;
	//XXX maybe this should be ev.srcElement for IE?
	rd.selectRow(row,dataset);
}

function clearSelection() {
	rd.clearSelection();
}

function setSelection(selectors) {
	rd.setSelection(selectors);
}

function extendSelection(selectors) {
	rd.extendSelection(selectors);
}

// insert a term into a search box

function insertTerm(el) {
	var sbox = document.getElementById('sterm');
	
	var s = el.href;
	if( !s ) {
	    s = trim(el.innerHTML);
	}
	
	if (s && sbox) {
		rd.queryTitle = trim(el.innerHTML);
		sbox.value = s;
		sbox.focus();
	}
	return false;
}

function clearAll() {
	if (confirm("Clear form and results?")) {
		// Reset form and clear saved state
		rd.clearForm();
		rd.clearState();
	}
	return false;
}

// filtering hooks

function filterByColumn(form) {
	if (rd.filter) {
		var changed = rd.filter.filterByColumn(form);
		if (changed) {
			rd.sortToggle = false;
			rd.page = 1;
			rd.sort();
		}
	}
	return false;
}

function resetFilter(form) {
	if (rd.filter) {
		var changed = rd.filter.clear(form);
		if (changed) {
			rd.sortToggle = false;
			rd.page = 1;
			rd.sort();
		}
	}
	return false;
}


// write debug output to div at top of page
function debug(innerHTML,clear) {
	var el = document.getElementById("debug");
	if (!el) {
		el = document.createElement("div");
		el.id = "debug";
		el.style.fontSize = "80%";
		el.innerHTML = '<a href="#" onclick="return debug(null,true)">Clear</a>';
		document.body.insertBefore(el, document.body.firstChild);
	}
	if (clear) {
		el.innerHTML = '<a href="#" onclick="return debug(null,true)">Clear</a>';
	} else {
		el.innerHTML += " "+innerHTML;
	}
	return false;
}

// define global variables (called on load)
// this initialization needs to be executed after the page elements are defined

window.onload = function() {
	StateManager = EXANIMO.managers.StateManager;

	var output = document.getElementById("output");
	rd = new readdata(output, "searchForm", "sql", "ddCapList", "inputTitle", "inputShortname", "inputIdentifier", "inputPublisher", "inputSubject", "inputWaveband");
	StateManager.initialize();
	StateManager.onstaterevisit = rd.restoreState;
};
