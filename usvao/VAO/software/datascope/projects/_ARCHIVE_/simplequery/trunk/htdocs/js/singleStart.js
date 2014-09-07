// VOTable viewer
// R. White, 2007 August 1

// define global variables (called on load)
// this initialization needs to be executed after the page elements are defined
window.onload = function() {

    StateManager = EXANIMO.managers.StateManager;

    var output = document.getElementById("output");
    
    // url should be specified by input program in JavaScript include    
    rd = new readdata(output, url);
    
    StateManager.initialize();
    StateManager.onstaterevisit = rd.restoreState;
    
    if (docBase) {
        rd.setXSLBase(docBase+"/xsl");
    }
    if (url) {
        rd.setView();
    } else {
        alert("No URL specified for display");
    }
};
