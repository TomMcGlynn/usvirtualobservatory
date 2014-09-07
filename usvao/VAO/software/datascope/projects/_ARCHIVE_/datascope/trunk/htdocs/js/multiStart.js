// VOTable viewer
// R. White, 2007 August 1

// define global variables (called on load)
// this initialization needs to be executed after the page elements are defined

function addArg(key, value) {
    return '&'+encodeURIComponent(key)+"="+encodeURIComponent(value);
}

window.onload = function() {

    StateManager = EXANIMO.managers.StateManager;

    var output = document.getElementById("output");
    var url    = "sq.sh";
    
    var args = "IVOID="+PIVOID;
    if (PRadius) {
        args += addArg("RADIUS", PRadius);
    }
    if (Psources) {
        args += addArg("sources", Psources);
    }
    if (PsourcesURL) {
        args += addArg("sourcesURL", PsourcesURL);
    }
    if (PRequestID) {
        args += addArg("RequestID", PRequestID);
    }
    rd = new readdata(output, url, args);
    
    StateManager.initialize();
    StateManager.onstaterevisit = rd.restoreState;
    if (document.forms["searchForm"]["query_string"].value) {
        if (docBase) {
	   rd.setXSLBase(docBase+"/xsl");
	}
        rd.setView();
    }
};

