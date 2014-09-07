// R. White, 2007 August 1

var candidateIVOID;
var candidateIVOIDIndex;

if (! Sarissa._SARISSA_IS_SAFARI) {

    var originalState = window.location.href.split('#');
    if (originalState.length > 1) {
        var baseLocation = originalState[0];
        originalState = originalState.slice(1).join("#");
        // Save desired state in a cookie
        // We're using a default date so that coookie gets deleted when browser exits
        // (but it should get erased right away after the page loads)
        createCookie("squeryReloadState", originalState);
        window.location.href = baseLocation;
    }
}


// define global variables (called on load)
// this initialization needs to be executed after the page elements are defined
window.onload = function() {

    StateManager = EXANIMO.managers.StateManager;
    var output   = document.getElementById("output");
    rd           = new readdata(
      {
        queryTitle: "sq.sh", 
        output: output, 
        url: url, 
        updateCallback: vo_ready
      }
    );
    
    // Look for errors of the form:
    //  <INFO/PARAM name="Error" value="Error text" />
    //       or
    //  <INFO/PARAM name="QUERY_STATUS" value="notOK">ErrorText</..>
  
    function checkTagsForError(list) {
    
        for (var i=0; i<list.length; i += 1) {
	    var tag   = list[i];
	    var att   = tag.attributes;
	    var natt  = att.getNamedItem("name");
	    if (natt != null) {
	        var name = natt.value;
		if (name != null) {
		    name = name.toLowerCase();
		}
	        if (name == "error") {
	            var val  = att.getNamedItem("value");
		    if (val != null) {
		        return val.value;
		    }
		} else if (name == "query_status") {
		    var val = att.getNamedItem("value");
		    if (val != null) {
		        if (val != "OK" && val != "ok") {
			    if (tag.childNodes.length > 0) {
			        return tag.childNodes[0].data;
			    }
			}
		    }
		}
	    }
	}
	return null;
    }
    
    rd.printErrorDoc = function(node, xmlDoc) {
                var itags = xmlDoc.getElementsByTagName("INFO");
                var ptags = xmlDoc.getElementsByTagName("PARAM");
		var msg = null;
		if (itags.length > 0) {
		    msg = checkTagsForError(itags);
		}
		if (msg == null && ptags.length > 0) {
		    msg = checkTagsForError(ptags);
		}
		
		if (msg) {
		    msg = "<b>Query returned error:</b><br>"+msg;
		} else {
		    msg ="<b>Error in query.</b><br>"+
		         "No error indication seen.<br>"
		         "If this was a multi-table VizieR dataset you may wish to "+
			 "retry with a different table since the one you selected does "+
			 "not seem to be queryable by position.";
		}
	        node.innerHTML = msg;
	    };

    // redefine some functions
    rd.setTitle = function(innerHTML) {}
    rd.errorMessage = function(msg) {
        if ( msg.match("Searching") ) {
            var div = document.createElement('div');
            div.innerHTML = "<center><h1>"+msg+"</h1><br/><img src='@URL_PATH@@IMG_PATH@PleaseWait.gif' /></center>";
            rd.clearOutput(div);
        } else {
            var p = document.createElement('p');
            p.innerHTML = msg;
            rd.clearOutput(p);
        }
        return false;
    }

    StateManager.initialize();
    StateManager.onstaterevisit = rd.restoreState;

    originalState = readCookie("squeryReloadState");
    if (originalState) {
        // restore the original state using the cookie value
        eraseCookie("squeryReloadState");
        rd.restoreState({id: originalState});
    }
    
    if (docBase) {
        rd.setXSLBase(docBase+"/xsl");
    }
    
    if (url) {
        rd.setView();
    } else {
        alert("No URL specified for display");
    }
};
