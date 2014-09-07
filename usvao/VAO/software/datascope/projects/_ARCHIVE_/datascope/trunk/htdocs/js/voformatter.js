//  output      -- Document element in which data is to be written.

function readdata(output, baseURL, postInput) {

    // Note all initialization is at the end (after the methods are defined)

    var me            = this;
    var fullTable     = true;
    var stateEnabled  = true;
    
    var selectAdd     = null;
    var selectDel     = null;
    
    
    // Internal fields:
    //     me           -- an alias for this
    //     queryTitle   -- Not settable, but used to update window title
    //     title        -- a title to be used in the output
    //     view         -- a name for the current XSLT transformation
    //     params       -- The name of the URL used in the query (the value of searchform[searchparam])
    //     xml          -- the XML document downloaded
    //     page         -- the current page number (starting at 1)
    //     sortColumn   -- the current sort column
    //     maxColumns   -- the current maximum number of columns to display
    //     defaultView  -- the name of the default XSLT transformation
    //     selectedRows -- the array of selected rows
    //     pageLength   -- the maximum number of rows to display in a page
    //     sortToggle   -- should the next rendering change the sort order?
    //     form         -- the form element in which the url will be found.
    //     xsltfile     -- the file associated with the current view
    //     filter       -- the filter that applies row criteria to the input data
    //     restoringState -- Currently restoring state?
    //     myXslProc    -- the XSLT processor that generates HTML from XML
    //     voLoader     -- An FSMLoader that loads the document XML
    //     XSLLoader    -- An FSMLoader that loads the XSLT XML
    //     sortOrder    -- sort in ascending or descending order?
    
    // Published methods
    //
    // clearOutput(el)     -- Clear a node of all descendents
    // setTitle(innerHTML) -- Set the title area of the rendered document
    // setWindowTitle()    -- Set the title of the document/window
    // clearPageInfo()     -- Reset column dispaly and page selections to default.
    // clearForm()         -- Reset the page selection form
    // loadData()          -- Load (if needed) the XML source document requested
    // checkXSL()          -- Load (if needed) the XSL transformation document for the current view
    // saveState()         -- Save the current state to the session history.
    // restoreState(id)    -- Use a recovered state to restore to an earlier configuration.
    // clearState()        -- Return to the default state
    // setView(id)         -- Set the view to be used in rendering XML
    // setViewParams()     -- Set up special values needed for a given view.
    // xslLoaded()         -- Call-back when XSL document is loaded.
    // xmlLoaded()         -- Call-back when XML source document is loaded. 
    // getXML()            -- Get the current, possibly filtered, XML document
    // showColumns(n)      -- Set the number of columns to be displayed.
    // clearSelection()    -- Clear selections and filters
    // setSelection(str)   -- Set the selected rows.
    // extendSelection(str) - Add to the selected rows.
    // selectRow(elem,id)  -- Call-back for row selection
    // setPageLength(maxr) -- Set the maximum number of rows to be rendered
    // sort(col,dir,page)  -- Render the given page sorting by the given column.
    //                        Usually called without arguments
    // errorMessage(msg)   -- Display error
    // noXsltMessage()     -- Indicate browser does not support XSLT
    // loadingError(msg)   -- Error call-back when error in loading.
    // dataError(msg)      -- Error call-back for XSL processing.
    // xslError(msg)       -- Error call-back for XSL loading.

    this.clearOutput = function(el) {
        while (this.output.hasChildNodes()) {
            this.output.removeChild(this.output.firstChild);
        }
        if (el) {
            this.output.appendChild(el);
        }
    };

    this.setTitle = function(innerHTML) {
        return;
        // set title in the output section
        if (innerHTML == undefined) {
            this.title.innerHTML = "<i>" + baseURL + "</i>";
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
        this.maxColumns = undefined;
        this.page       = 1;
    };

    this.clearForm = function() {
        // reset the form and restore most things to default state

        // start with a blank line and empty display
        this.setTitle("&nbsp;");
        this.clearOutput();

        this.view         = this.defaultView;
        this.xml          = undefined;
        this.params       = undefined;
	if (this.preSelects) {
	    this.selectedRows = this.preSelects;
	} else {
            this.selectedRows = [];
	}

        this.pageLength   = 20;
        this.clearPageInfo();
        this.sortToggle   = true;

        // set id="selected" for the default view
	
        var el = document.getElementById("selected");
        if (el) el.removeAttribute("id");
        el     = document.getElementById(this.defaultView);
	
        while (el && el.tagName != "LI") {
	    el= el.parentNode;
	}
	
        if (el) el.id = "selected";
    };

    this.loadData = function() {
        var params = baseURL;
        if (params != this.params) {
            this.errorMessage("Searching...");
            // clear list of selected rows with new search
            if (this.filter) {
                this.filter.clearXSL();
            }
            this.filter = undefined;
            this.clearSelection();

            // save parameters for last search
            this.params = params;
            this.setTitle();
            this.xml    = undefined;
	    
            // reset all sort/page info for new searches
            // don't reset sort/page info if requested by restoreState
	    
            if (!this.restoringState) {
	        this.clearPageInfo();
	    }
	    
            // Load XML
            this.VOloader.makeRequest(params);
	    
        } else if (! this.xml) {
	
            // Parameters are set but XML is not
            this.errorMessage("Searching...");
            this.VOloader.makeRequest(params);
	    
        }

        //XXX Need this?  Or move to setView?
        // Call sort immediately if XML & XSL already exist or if they are not needed
        this.sortToggle = false;
        if (
            (!this.xsltfile) || 
            (this.xml  != undefined && 
             this.xslt != undefined)) {
	     
            if (this.filter) {
                this.filter.filter();
            }
	    
            this.sort();
        }
        this.restoringState = false;
    };
    
    this.setXSLBase = function(dir) {
        this.xslBase = dir;
    }

    this.checkXSL = function() {
        // clear saved XSL if it is not what we need
        this.view    = this.view || this.defaultView;
        var xsltfile = this.view2xslt[this.view];
	if (this.xslBase) {
	    xsltfile = this.xslBase + "/"+ xsltfile;
	}
	
        if (xsltfile != this.xsltfile) {
	
            this.xsltfile  = xsltfile;
            this.xslt      = undefined;
            this.myXslProc = undefined;
	    
            if (this.filter) {
                this.filter.clearXSL();
            }
	    
            this.filter    = undefined;
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

    this.stateEnable = function(flag) {
       stateEnabled = flag;
    }
   
    this.saveState = function() {
    
        if (!stateEnabled) {
	    return;
	}
    
    
        // Save current state
        // Changed to use keywords rather than
        // positional params.  Adds a few chars
        // to the size but should be clearer
        // and more extensible.  Nothing
        // special about 2 char keys...
	
        if (this.sortColumn) {
            var sortOrder = this.sortOrder[this.sortColumn];
        } else {
            sortOrder = '';
        }
        var pars = ""

        // vw  -> Current view
        // sc  -> Sort column
        // so  -> Sort order
        // pg  -> Page
        // pl  -> Page length
        // mc  -> Max columns
        // pa  -> Form parameters
        // fi  -> Filter text
        // fy  -> Filter column types
        
        var state = 'vw='+this.view + 
                   '|sc='+encodeURIComponent(this.sortColumn || '') +
                   '|so=' + (sortOrder || '') + 
                   '|pg=' + (this.page || 1) + 
                   '|pl=' + this.pageLength + 
                   '|mc=' + (this.maxColumns || '') + 
                   '|pa=' + encodeURIComponent(pars);
		
        if (me.myXslProc) {
	
            var fields = me.myXslProc.getParameter(null, "filterText");
            var types  = me.myXslProc.getParameter(null, "filterTypes");
            
            if (fields && fields.length > 0) {
                state +="|fi="+encodeURIComponent(fields);
                state +="|ty="+encodeURIComponent(types);
            }
        }

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
            var settings = new Object();
            var field;
	    
            for (field in state) {
                var fld = decodeURIComponent(state[field]);
                var key;
                var val = null;
                // Split doesn't stop splitting at limit
                // so using split as we would in Perl doesn't work.
                var index = fld.indexOf('=');
                if (index > 0) {
                    key = fld.substring(0,index);
                val = fld.substring(index+1);
                }  else {
                    key = fld;
                }
                settings[key] = val;
            }
            
            var newview = settings['vw'];
            me.sortColumn = settings['sc'] || undefined;
            if (me.sortColumn) {
                me.sortOrder[me.sortColumn] = settings['so'];
                // don't toggle the sort order on first call
                me.sortToggle = false;
            }
	    
            me.page       = parseInt(settings['pg'] || 1, 10);
	    
            me.pageLength = parseInt(settings['pl'] || 20, 10);
	    
            me.maxColumns = settings['mc'] || undefined;
	    
            if (me.maxColumns) me.maxColumns = parseInt(me.maxColumns, 10);
	    
            if (settings['pa']) {
                setFormPars(me.form, settings['pa']);
            }
            
            if (settings['fi']) {
	        if (!me.myXslProc) {
		    if (me.xslt) {
		        me.myXslProc = new XSLTProcessor();
                        me.myXslProc.importStylesheet(me.xslt);
		    }
		}
		if (me.myXslProc) {
                    me.myXslProc.setParameter(null, "filterText",  settings['fi']);
                    me.myXslProc.setParameter(null, "filterTypes", settings['ty']);
		}
            }
            
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
        while (el && el.tagName != "LI") {
	    el= el.parentNode;
	}
	
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
                this.myXslProc.removeParameter(null, "maxColumns");
            }
	    
        } else if (this.view == "Sia") {
	   this.myXslProc.setParameter(null, "selectRowUCD", "VOX:Image_AccessReference");
        }
	
	if (this.specView && this.specView[this.view]) {
	    for (param in this.specView[this.view]) {
	        this.myXslProc.setParameter(null, param, this.specView[this.view][param]);
	    }
	}
    };
    
    this.updateViewParams = function(view, name, value) {
        if (!this.specView) {
	    this.specView = new Object();
	}
	if (!this.specView[view]) {
	    this.specView[view] = new Object();
	}
	this.specView[view][name] = value;
    };

    this.xslLoaded = function(data) {
        me.xslt = data;
        if (me.xml) me.sort();
    };
    
    this.getXML = function() {
         return me.filter.getDocument();
    }
    
    this.xmlLoaded = function(data) {
        // If params is null, back button was presumably used to
        // return to a blank page.  Simply ignore the XML data in
        // that case.
        if (me.params) {
	    if (me.xml != data) {
                me.xml    = data;
	        me.filter = null;
	    }

            if (me.xslt) {
	        me.sort();
	    }
        }
    };

    this.showColumns = function(columns) {
        me.maxColumns = columns;
        me.sortToggle = false;
        me.sort();
    };

    this.clear = function() {
        this.clearSelection();
	this.clearFilter();
    }
    
    this.clearSelection = function() {
	
	if (me.preSelects) {
	    me.selectedRows = me.preSelects;
	} else {
            me.selectedRows = [];
	}
    }
    
    this.clearFilter = function() {
        if (me.filter) {
            if (me.filter.clear()) {
                me.sort();
            }
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

    this.selectRow = function(el, dataset) {
        var cclass = el.className;
	var boxes = el.getElementsByTagName("input");
	if (boxes.length > 0) {
            for (var i=0, f; i < me.selectedRows.length; i++) {
                if (me.selectedRows[i] == dataset) {
                    // second click disables selection
                    me.selectedRows.splice(i,1);
                    if (cclass) {
                        el.className = removeSubstring(cclass,"selectedimage");
                    }
		    boxes[0].checked = false;
		    if (this.selectDel) {
		        this.selectDel(dataset);
		    }
                    return;
                }
            }
	    boxes[0].checked=true;
	
            // not in current selection, so add this to selection
            me.selectedRows.push(dataset);
            me.selectedRows.sort();
	    if (this.selectAdd) {
	        this.selectAdd(dataset);
	    }
            if (cclass) {
                el.className = cclass + " selectedimage"; 
            } else {
                el.className = "selectedimage"; 
            }
	}
    };
    
    this.setSelectedRows = function(rows) {
        me.preSelects = rows;
    }
    
    this.setSelectCallBack = function(add, del) {
        this.selectAdd = add;
	this.selectDel = del;
    }

    this.setPageLength = function(pageLength) {
        // change number of rows per page
        if ((!pageLength) || me.pageLength == pageLength) return;
        var start = me.pageLength*(me.page-1);
        me.pageLength = pageLength;
        me.sort(undefined, undefined, Math.floor(start/pageLength)+1);
    };

    this.sort = function(sortColumn, sortOrder, newpage) {
        if (me.xml == undefined || me.xslt == undefined) return false;

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

        if (! me.myXslProc) {
            // Mozilla/IE XSLT processing using Sarissa
            if (!window.XSLTProcessor) return me.noXSLTMessage();
            
            me.myXslProc = new XSLTProcessor(); 
	    if (me.filter) { 
	        me.filter.clearXSL(); 
	    } 
	    
	    me.filter = new XSLTFilter(me.xml, me.myXslProc);
            
            if ((!me.myXslProc) || (!me.myXslProc.importStylesheet))
                return me.noXSLTMessage();
            // attach the stylesheet; the required format is a DOM object, and not a string
            me.myXslProc.importStylesheet(me.xslt);
        }
        if (!me.filter) {
            me.filter = new XSLTFilter(me.xml, me.myXslProc);
        }
        
        if (me.filter.getValid()) {
            me.saveState();

            // do the transform
            me.myXslProc.setParameter(null, "sortOrder", sortOrder);
            me.myXslProc.setParameter(null, "sortColumn", sortColumn);
            me.myXslProc.setParameter(null, "page", ""+me.page);
            me.myXslProc.setParameter(null, "pageLength", ""+me.pageLength);
	    if (me.fullTable) {
	        me.myXslProc.setParameter(null, "fullTable", "yes");
	    } else {
	        me.myXslProc.setParameter(null, "fullTable", "no");
	    }
	     
            if (me.selectedRows) {
                me.myXslProc.setParameter(null, "selectedRows", me.selectedRows.join(","));
            }
            me.setViewParams();

            // create the HTML table and insert into document
            var finishedHTML = me.myXslProc.transformToFragment(me.filter.getDocument(), document);
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
        }
        return false;
    };
    
    this.getSelections = function() {
        return selectedRows;
    }

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
    
    // *** State initialization ***

    this.defaultView = "Table";
    this.sortOrder = {};

    this.xsltfile = undefined;
    this.xslt     = undefined;
    this.filter   = undefined;
    this.xslBase  = undefined;

    // mapping from view choices to XSLT files
    // only one view, but leave this in to allow XSLT switching

    this.view2xslt = {
        "Table":   "voview.xsl",
	"Sia":     "Sia.xsl",
	"Heasarc": "Heasarc.xsl",
	"Mast":    "Mast.xsl",
	"NED":     "NED.xsl",
	"Simbad":  "Simbad.xsl",
	"ADS":     "ADS.xsl"
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
    this.VOloader =  new FSMLoader("data", "", this.xmlFinished, this.updProc, this.loadingError);
    if (postInput) {
        this.VOloader.setPost(postInput);
    }
    this.XSLloader = new FSMLoader("xsl",  "", this.xslLoaded, null,         this.loadingError);
}

function removeSubstring(s,
                         sub) {
    var flist = s.split(' ');
    var glist = [];
    for (var i=0, f; f = flist[i]; i++) {
        if (f && f != sub) {
            glist.push(f);
        }
    }
    return glist.join(' ');
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
                // text or hidden element
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
    rd.clear();
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

// Update the filter constraints
function updateFilter(id, type) {
    
    if (rd.filter.update(id, type)) {
        rd.filter.filter();
        rd.sortToggle = false;
        rd.sort();
    }
}

function clearAll() {
    if (confirm("Clear form and results?")) {
        // Reset form and clear saved state
        rd.clearForm();
        rd.clearState();
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

function renderXML() {
    try {
        var str = new XMLSerializer().serializeToString(rd.filter.getDocument());
        elem1 = document.getElementById("outputsources");
        elem2 = document.getElementById("outputform");
    
        document.getElementById("outputsources").value = str;
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
        var xsltp      = new XSLTProcessor();
        var xsltDom    = Sarissa.getDomDocument();
        xsltDom        = (new DOMParser()).parseFromString(xsltString, "text/xml");  
        xsltp.importStylesheet(xsltDom);
        var newDoc     = xsltp.transformToFragment(rd.filter.getDocument(), document);
	if (top.twin) {
	    top.twin.close();
	}
	top.twin = window.open(null, "results", "WIDTH=450,HEIGHT=300,resizable,scrollbars,toolbar,menubar,status");
        var str = new XMLSerializer().serializeToString(newDoc);
	top.twin.document.write("<head><title>ASCII table</title></head><pre>"+str+"</pre>");
	top.twin.document.close();
	top.twin.focus();
	
    } catch  (e) {
        alert("Exception creating ASCII Table:"+e);
    }
    
}
