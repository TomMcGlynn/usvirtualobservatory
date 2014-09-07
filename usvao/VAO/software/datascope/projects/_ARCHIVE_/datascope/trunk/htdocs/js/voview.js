// VOTable viewer
// R. White, 2007 August 1

// define global variables (called on load)
// this initialization needs to be executed after the page elements are defined
window.onload = function() {

    StateManager = EXANIMO.managers.StateManager;

    var output = document.getElementById("output");
    var url = document.forms["searchForm"]["query_string"].value;
    
    rd = new readdata(output, url);
    StateManager.initialize();
    StateManager.onstaterevisit = rd.restoreState;
    if (document.forms["searchForm"]["query_string"].value) {
        rd.setView();
    }
};
