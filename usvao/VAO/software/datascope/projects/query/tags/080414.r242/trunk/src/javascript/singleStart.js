// VOTable viewer
// R. White, 2007 August 1

// define global variables (called on load)
// this initialization needs to be executed after the page elements are defined
window.onload = function() {
	StateManager = EXANIMO.managers.StateManager;
	var output = document.getElementById("output");
	var rd = new readdata({
		queryTitle: "sq.sh", 
		output: output, 
		url: url, 
		updateCallback: vo_ready 
	});

	// redefine some functions
	rd.setTitle = function(innerHTML) {}
	rd.errorMessage = function(msg) {
		if ( msg.match("Searching") ) {
			var div = document.createElement('div');
			div.innerHTML = "<center><h1>"+msg+"</h1><br/><img src='@IMG@/PleaseWait.gif' /></center>";
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
    
	if (docBase) rd.setXSLBase(docBase+"/xsl");
	if (url) {
		rd.setView();
	} else {
		alert("No URL specified for display");
	}
};
