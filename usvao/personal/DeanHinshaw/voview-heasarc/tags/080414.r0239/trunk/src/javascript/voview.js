// VOTable viewer
// R. White, 2007 October 25

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

// XPath functions

function selectSingleNode(doc, xpath) {
	if (document.evaluate) {
		// Mozilla version
		var result = document.evaluate(xpath, doc, null, XPathResult.FIRST_ORDERED_NODE_TYPE, null).singleNodeValue;
	} else if (doc.selectSingleNode) {
		// IE version
		result = doc.selectSingleNode(xpath);
	}
	return result;
}

function selectNodes(doc, xpath) {
	if (document.evaluate) {
		// Mozilla version
		var result = document.evaluate(xpath, doc, null, XPathResult.ANY_TYPE, null);
	} else if (doc.selectSingleNode) {
		// IE version
		result = doc.selectNodes(xpath);
		// fake the iterateNext method [XXX untested XXX]
		var nextIndex=0;
		result.iterateNext = function() {
			if (nextIndex < this.length) {
				nextIndex = nextIndex+1;
				return this[nextIndex-1];
			} else {
				return null;
			}
		};
	}
	return result;
}

// pack form parameters into a GET string

function getFormPars(formname) {
	if (typeof(formname) == "string") {
		var form = document.forms[formname];
	} else {
		form = formname;
	}
	if (form == undefined) {
		// allow non-existent form
		return "";
	}
	var parlist = [];
	for (var i=0; i<form.elements.length; i++) {
		var el = form.elements[i];
		if (el.tagName == "INPUT") {
			var value = encodeURIComponent(el.value);
			if (el.type == "text" || el.type == "hidden") {
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
				if (el.type == "checkbox") {
					if (value) {
						el.checked = true;
					} else {
						el.checked = false;
					}
				} else {
					// text or hidden element
					el.value = value;
				}
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

function readdata(search_params) {

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
			this.title.innerHTML = this.titletext;
			var label = this.searchparam.value || this.url || this.filename;
			if ( label )
				this.title.innerHTML += " ( <i>" + label + "</i> ) ";
			if ( this.exporter )
				this.title.innerHTML += this.exporter;
		} else {
			this.title.innerHTML = this.titletext + innerHTML;
		}
	};

	this.setWindowTitle = function() {
		// set window title to include name
		if (this.queryTitle) {
			window.document.title = "VOTable Viewer (" + this.queryTitle + ")";
		} else if (this.url) {
			window.document.title = "VOTable Viewer (" + this.url + ")";
		} else if (this.filename) {
			window.document.title = "VOTable Viewer (" + this.filename + ")";
		} else {
			window.document.title = "VOTable Viewer";
		}
	};

	this.clearPageInfo = function() {
		this.sortColumn = undefined;
		this.maxColumns = undefined;
		this.columnOrder = undefined;
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
		this.selectedRows = [];

		this.pageLength = 20;
		this.clearPageInfo();
		this.sortToggle = true;

		if (this.form) this.form.reset();

		// set id="selected" for the default view
		var el = document.getElementById("selected");
		if (el) el.removeAttribute("id");
		el = document.getElementById(this.defaultView);
		while (el && el.tagName != "LI") el= el.parentNode;
		if (el) el.id = "selected";
	};

	this.loadData = function() {
		var params = this.searchparam.value || this.url;

		if ( this.stream != null && this.stream.value.length > 0 ) {
			//	First time through (or after clearform)
			this.errorMessage("Searching...");
			// clear filter with a new search
			if (! this.restoringState) this.filter.clearXSL();
			this.filter.setBaseDocument(null);
			// clear list of selected rows with new search
			this.clearSelection();

			// save parameters for last search
			this.setTitle();
			this.xml = undefined;
			// reset all sort/page info for new searches
			this.clearPageInfo();
			this.params = "Stream";	//	needs set or xmlLoaded won't do anything.

			// Load XML
			this.xmlLoaded((new DOMParser()).parseFromString(this.stream.value, "text/xml"));
		} else if (params != this.params) {
			//	First time through (or after clearform)
			this.errorMessage("Searching...");
			// clear filter with a new search
			if (! this.restoringState) this.filter.clearXSL();
			this.filter.setBaseDocument(null);
			// clear list of selected rows with new search
			this.clearSelection();

			// save parameters for last search
			this.params = params;
			this.setTitle();
			this.xml = undefined;
			// reset all sort/page info for new searches
			this.clearPageInfo();
			// Load XML
			this.VOloader.makeRequest(params);
		} else if (! this.xml) {
//	Don't know exactly when this would be true?
			// Parameters are set but XML is not
			this.errorMessage("Searching...");
			this.VOloader.makeRequest(params);
		}

		//XXX Need this?  Or move to setView?
		// Call sort immediately if XML & XSL already exist or if they are not needed
		this.sortToggle = false;
		if ((!this.xsltfile) ||
			(this.xml != undefined &&
			 this.xslt != undefined)) {
			this.sort();
		}
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
			if (this.columnOrder) {
				this.myXslProc.setParameter(null, "columnOrder", (this.columnOrder.join(","))+",");
			} else {
				if (this.myXslProc.removeParameter) {
					this.myXslProc.removeParameter(null, "columnOrder");
				} else {
					this.myXslProc.setParameter(null, "columnOrder", null);
				}
			}
		}
	};

	this.valueXslLoaded = function(data) {
		me.valueXslProc.importStylesheet(data);
		me.valueXslImported = true;
		if (me.xml) me.addValueAttribute();
	};

	this.addValueAttribute = function() {
		// I think that if votable is small and loads fast, this doesn't work
		if ( me.valueXslImported && !me.valueAttributeAdded ) {
			me.valueAttributeAdded = true;
			tmpDoc = me.valueXslProc.transformToDocument(me.xml);
			me.xml = tmpDoc; // not really needed as is only really used as a flag (which is already set)
			me.filter.setBaseDocument(tmpDoc);// required
			if ( me.xslt) me.sort();
		}
	}

	this.xslLoaded = function(data) {
		me.xslt = data;
		if (me.valueAttributeAdded) me.sort();
//		if (me.xml) me.sort();
	};

	this.getXML = function() {
		return me.filter.getDocument();
	};

	this.xmlLoaded = function(data) {
		// If params is null, back button was presumably used to
		// return to a blank page.  Simply ignore the XML data in
		// that case.
		if (me.params) {
			me.xml = data;
			me.filter.setBaseDocument(data);
			if (me.valueXslImported) me.addValueAttribute();
//			if (me.xslt) me.sort();
		}
	};

	this.setMaxColumns = function(maxcolumns) {
		if (maxcolumns != me.maxColumns) {
			this.maxColumns = maxcolumns;
			this.sortToggle = false;
			this.sort();
		}
	};

	this.setColumnOrder = function(maxcolumns, order) {
		if (maxcolumns == me.maxColumns) {
			if (this.columnOrder) {
				if (this.columnOrder.length == order.length) {
					// just return if the order is unchanged
					var equals = true;
					for (var i=0; i<order.length; i++) {
						if (order[i] != this.columnOrder[i]) {
							equals = false;
							break;
						}
					}
					if (equals) return;
				}
			} else if (!order) {
				// both old and new order are undefined (default)
				return;
			}
		}
		this.columnOrder = order;
		this.maxColumns = maxcolumns;
		this.sortToggle = false;
		this.sort();
	};

	this.clearSelection = function() {
		if (me.selectedRows.length > 0) {
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

	this.setPageLength = function(pageLength) {
		// change number of rows per page
		if ((!pageLength) || me.pageLength == pageLength) return;
		var start = me.pageLength*(me.page-1);
		me.pageLength = pageLength;
		me.sort(undefined, undefined, Math.floor(start/pageLength)+1);
	};

	this.sort = function(sortColumn, sortOrder, newpage) {
		if (me.xml == undefined || me.xslt == undefined) return false;

		if (me.valueXslImported && !me.valueAttributeAdded) me.addValueAttribute();

		if (newpage != undefined) me.page = newpage;
		// sort direction gets toggled only if the page does not change
		var pchanged = newpage != undefined;

		if (!sortColumn) {
			sortColumn = me.sortColumn || "";
		}
		if (!sortOrder) {
			if (me.sortToggle && sortColumn == me.sortColumn && (! pchanged)) {
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

		// do the transform
		me.myXslProc.setParameter(null, "sortOrder", sortOrder);
		me.myXslProc.setParameter(null, "sortColumn", sortColumn);
		me.myXslProc.setParameter(null, "page", ""+me.page);
		me.myXslProc.setParameter(null, "pageLength", ""+me.pageLength);
		if (me.selectedRows) {
			me.myXslProc.setParameter(null, "selectedRows", me.selectedRows.join(","));
		}
		// set extra XSLT parameters
		for (var p in me.xslParams) {
			me.myXslProc.setParameter(null, p, me.xslParams[p]);
		}
		me.setViewParams();

		// create the HTML table and insert into document
		Sarissa.updateContentFromNode(me.getXML(), me.output, me.myXslProc,
			function() {
				// make the fields table draggable if it exists
				var ftable = document.getElementById('fields');
				if (ftable) {
					var tablednd = new TableDnD.TableDnD();
					tablednd.onDrop = setColumnOrder;
					tablednd.init(ftable);
				}
				if (search_params.updateCallback) {
					search_params.updateCallback();
				}
			});
		return false;
	};

	this.errorMessage = function(msg) {
		var p = document.createElement('p');
		p.innerHTML = msg;
		me.clearOutput(p);
		return false;
	};

	this.noXSLTMessage = function() {
		me.errorMessage("Sorry, your browser does not support XSLT -- try Firefox, Safari (version 3), Mozilla (version > 1.3), Internet Explorer, or other compatible browsers.");
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

/*
	// From older version but potentially useful in the future ?

	this.xmlFinished = function(xml) {
		me.fullTable = true;
		me.xmlLoaded(xml);
	}

	this.updProc = function(text) {
		try {
			var pos = text.lastIndexOf("</TR>");
			if (pos > 0  && !me.xml) {
				var tinydoc = text.substring(0, pos)+
					"</TR></TABLEDATA></DATA></TABLE></RESOURCE></VOTABLE>";
				var tinyXML = Sarissa.getDomDocument();
				tinyXML     = (new DOMParser()).parseFromString(tinydoc, "text/xml");
				var rows    = tinyXML.getElementsByTagName("TR");
				if (rows.length > 20) {
					me.fullTable = false;
					me.xmlLoaded(tinyXML);
				} 
			}    
		} catch (e) {
			alert("Exception:"+e);
		}
	}
*/


	// *** State initialization ***

	this.defaultView = "Table";
	this.sortOrder = {};
	if ( ! search_params ) {
		alert("No search_params given to readdata");
		return;
	}
	this.searchparam = "";	// needed for simple query usage
	if ( search_params.form ) {
		this.form = document.forms[search_params.form];
		if (! this.form) {
			alert("Form "+search_params.form+" not found");
		} else {
			this.searchparam = this.form[search_params.searchparam];
			if (! this.searchparam) alert("Parameter " +
				search_params.form + "." + search_params.searchparam + " not found");
		}
		this.stream = this.form[search_params.stream];
	} else if ( search_params.url ) {
		this.url = search_params.url;
	} else if ( search_params.stream ) {
		this.stream = search_params.stream;
	} else {
		alert("You must specify a form, url or stream!");
		return;
	}
	this.filename = search_params.filename || "none";
	if (! search_params.output) {
		alert("You must specify an output DOM element!");
		return;
	}
	this.titletext = search_params.titletext || "";
	this.exporter = search_params.exporter || "";
	this.queryTitle = search_params.queryTitle || "";

	this.xsltfile = undefined;
	this.xslt = undefined;
	this.filter = new XSLTFilter(null, this);
	this.xslBase = undefined;

	// mapping from view choices to XSLT files
	// only one view, but leave this in to allow XSLT switching

	var xsltdir = search_params.xsltdir || "";
	if (Sarissa._SARISSA_IS_SAFARI) {
		// work around bug in Safari that makes xsl:import fail
		this.view2xslt = {
				"Table": xsltdir + "voview-safari.xsl"
				};
	} else {
		this.view2xslt = {
				"Table": xsltdir + "voview.xsl"
				};
	}

	// output has two parts, a title and a div for the XSL output
	while (search_params.output.hasChildNodes()) {
		search_params.output.removeChild(search_params.output.firstChild);
	}

	
	ctitle = document.createElement("center");
	this.title = document.createElement("h3");
	this.title.setAttribute('id','title');
	ctitle.appendChild(this.title);
	// start with a blank line
	this.title.innerHTML = this.titletext || '&nbsp;';
	search_params.output.appendChild(ctitle);

	this.output = document.createElement("div");
	search_params.output.appendChild(this.output);

	// Finish the initialization using clearForm
	this.clearForm();

	// create the event-handling loaders
	this.VOloader = new FSMLoader("data", "", this.xmlLoaded, this.loadingError, null );
//	this.VOloader = new FSMLoader("data", "", this.xmlLoaded, this.updProc, this.loadingError);
// JAKE
	if (search_params.postInput) {
		this.VOloader.setPost(search_params.postInput);
	}
	this.XSLloader = new FSMLoader("xsl", "", this.xslLoaded, this.loadingError, null );

	this.valueXslImported = false;
	this.valueAttributeAdded  = false;
	this.valueXslProc = new XSLTProcessor();
	this.valueXSLloader = new FSMLoader("valueXsl", "", this.valueXslLoaded, this.loadingError, null );
	this.valueXSLloader.makeRequest("@XSL@/addValue.xsl");
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
	var changed = rd.filter.filterByColumn(form);
	if (changed) {
		rd.sortToggle = false;
		rd.page = 1;
		rd.sort();
	}
	return false;
}

function resetFilter(form) {
	var changed = rd.filter.clear(form);
	if (changed) {
		rd.sortToggle = false;
		rd.page = 1;
		rd.sort();
	}
	return false;
}

function setMaxColumns(maxcolumns) {
	rd.setMaxColumns(maxcolumns);
}

function setColumnOrder(table, row) {
	// determine the column order from the table
	var rows = table.tBodies[0].rows;
	var maxcolumns = rows.length-1;
	var order = [];
	for (var i=0; i<rows.length; i++) {
		var classname = rows[i].className || "";
		if (classname.indexOf("separator") >= 0) {
			maxcolumns = i;
		} else {
			// ID for row is 'fieldrow_<number>'
			var f = rows[i].id.split('_');
			order.push(parseInt(f[f.length-1],10));
		}
	}
	rd.setColumnOrder(maxcolumns, order);
}

function resetColumnOrder() {
	rd.setColumnOrder();
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
