// VOTable viewer
// R. White, 2007 August 1

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

function addArg(key, value) {
	return '&'+encodeURIComponent(key)+"="+encodeURIComponent(value);
}

window.onload = function() {
	StateManager = EXANIMO.managers.StateManager;

	var output = document.getElementById("output");
	var url    = "sq.pl";
	var args = "IVOID="+PIVOID;
	if (PRadius)     args += addArg("RADIUS", PRadius);
	if (Punits)      args += addArg("units", Punits);
	if (Psources)    args += addArg("sources", Psources);
	if (PsourcesURL) args += addArg("sourcesURL", PsourcesURL);
	if (PRequestID)  args += addArg("RequestID", PRequestID);
	if (PviewURL)    args += addArg("viewURL", PviewURL);
	if (PviewLocal)  args += addArg("viewLocal", PviewLocal);
	if (Pverbosity)  args += addArg("VERBOSITY", Pverbosity);
	if (Plimit)      args += addArg("limit", Plimit);

	rd = new readdata({
		output:output, 
		url:url, 
		postInput:args, 
		updateCallback: vo_ready 
	});

	// redefine some functions
	rd.setTitle = function(innerHTML) {}
	rd.errorMessage = function(msg) {
		if ( msg.match("Searching") ) {
			var div = document.createElement('div');
			div.innerHTML = "<center><h1>"+msg+"</h1><br/><img src='@IMG_PATH@PleaseWait.gif' /></center>";
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
	rd.setView();
};
