var activecount = 0;
var logoimg = undefined;
var logostatic = "@IMG@/newHLA_logo.gif";
var logoanim = "@IMG@/newHLA_logo_anim.gif";

var relayURL    = "/vo/view/proxy.pl";  // URL that can be used as retrieval proxy.
//var relayURL    = "/cgi-bin/vo/squery/relay.sh";  // URL that can be used as retrieval proxy.

function activateLoading(loadCallback) {
	// turn on loading indicator
	// keeps track of number of requests so that indicator is
	// only deactivated when all requests are complete
	if (!logoimg) {
		var logo = document.getElementById("logo");
		if (logo) {
			logoimg = logo.getElementsByTagName("img");
			if (logoimg) logoimg = logoimg[0];
		}
	}
	activecount += 1;
	if (activecount == 1) {
		if (logoimg) {
			if (loadCallback) {
				logoimg.onload = loadCallback;
			} else {
				logoimg.onload = undefined;
			}
			logoimg.src = logoanim;
		}
		var body = document.lastChild.lastChild;
		if (body) body.className = "busy";
	} else if (loadCallback) {
		loadCallback();
	}
}

function deactivateLoading(loadCallback) {
	// turn off loading indicator
	activecount -= 1;
	if (activecount <= 0) activecount = 0;
	if (activecount == 0) {
		if (logoimg) {
			if (loadCallback) {
				logoimg.onload = loadCallback;
			} else {
				logoimg.onload = undefined;
			}
			logoimg.src = logostatic;
		}
		var body = document.lastChild.lastChild;
		if (body) {
			// keep both of these so it works on IE too (maybe)
			body.removeAttribute("class");
			body.removeAttribute("className");
		}
	} else if (loadCallback) {
		loadCallback();
	}
}

function makeRequest(http_request, url, parameters, doneCallback, 
	processingCallback, requestName, errmsg,
	post ) {

/*
alert( "http_request: "+http_request+"\n"+
"url: "+url+"\n"+
"parameters: "+parameters+"\n"+
"post: "+post+"\n"+
"");
*/

	// general-purpose XMLHttpRequest
	if (http_request.overrideMimeType) {
		http_request.overrideMimeType('text/xml');
	}
	errmsg = errmsg || alert;

	http_request.onreadystatechange = function() {
		if (http_request.readyState == 3) {
			if (processingCallback) {
				processingCallback(http_request.responseText);
			}
		} else if (http_request.readyState == 4) {
			try {
				var status = http_request.status;
			} catch(e) {
				// This apparently happens when the request was aborted,
				// so simply return
				return;
			}
			if (status == 200) {
				// do final update
				var xmldata = http_request.responseXML;
				if (xmldata && xmldata.documentElement && xmldata.documentElement.nodeName != 'parsererror') {
					doneCallback(xmldata);
				} else {

/*	No rows in table ? */

					errmsg("Response from '"+url+parameters+"' is not XML?");
				}
			} else {
/* Failed to load VOTable: Error 500: Internal Server Error */
				errmsg("Error "+status+": "+http_request.statusText);
			}
		}
	};
	try {
		var fullURL = url+parameters;
		if (!post) {
			try {
				http_request.open('GET', fullURL, true);
			} catch (e) {
				// See if we can query using the relay.
				fullURL = relayURL+"?"+encodeURIComponent(fullURL);
				http_request.open('GET', fullURL, true);
			}
			http_request.send(null);
		} else {
			http_request.open('POST', fullURL, true);
			http_request.send(post);
		}  
	} catch (exceptionId) {
		errmsg("Error on open: "+exceptionId);
	}
}

// ----------------- begin FSM definitions -----------------

// Finite-state machines for hlaview user interface

// FSM: Finite-state machine base class

// Parameters:
// id = string naming this request (just for messages)
// parameters = additional parameters for the object

function FSM(id, parameters) {
	this.id = id;
	for (var parameter in parameters) this[parameter] = parameters[parameter];
	this.currentState = this.initialState;
}

FSM.prototype = {

	currentState: null,

	initialState: "Inactive",

	handleEvent: function(event) {
		var actionTransitionFunction = this.actionTransitionFunctions[this.currentState][event.type];
		if (!actionTransitionFunction) actionTransitionFunction = this.unexpectedEvent;
		var nextState = actionTransitionFunction.call(this, event);
		if (!nextState) nextState = this.currentState;

//		debug("<br/>"+this.id + "(" + this.parameters + "): '"
//				+ event.type + "' event caused transition from '"
//				+ this.currentState + "' state to '"
//				+ nextState + "' state");

		if (!this.actionTransitionFunctions[nextState]) nextState = this.undefinedState(event, nextState);
		this.currentState = nextState;
	},

	unexpectedEvent: function(event) {
		this.cancelRequest();
		alert("FSM handled unexpected event '" + event.type +
			"' in state '" + this.currentState +
			"' for id='" + this.id +
			"' running browser " + window.navigator.userAgent);
		return this.initialState;
	},

	undefinedState: function(event, state) {
		this.cancelRequest();
		alert("FSM transitioned to undefined state '" + state +
			"' from state '" + this.currentState +
			"' due to event '" + event.type +
			"' for id='" + this.id +
			"' running browser " + window.navigator.userAgent);
		return this.initialState;
	},

	// doActionTransition is used when one function takes exactly the same
	// actions as another function in the table

	doActionTransition: function(anotherState, anotherEventType, event) {
		 return this.actionTransitionFunctions[anotherState][anotherEventType].call(this,event);
	}
};

// FSMLoader: Finite-state machine loader for XMLHttpRequest
// Used for XML, XSL, name resolver

// Parameters:
// url = base url for loading (suitable for concatenating with parameters)
// notifyCallback = function called on successful load
// errorCallback = function called on error
// id = string naming this request (used for messages)

// Methods:
// makeRequest(parameters): Start new XMLHttpRequest
// cancelRequest(): Cancel current request

function FSMLoader(id, url, notifyCallback, errorCallback, processingCallback ) {
//function FSMLoader(id, url, notifyCallback, processingCallback, errorCallback) {
	this.base = FSM;
	this.base(id, {url: url, notifyCallback: notifyCallback,
				processingCallback: processingCallback,
				errorCallback: errorCallback});
	this.cache = {};			// cache of previous results
	this.parameters = null;		// parameters for active request
	this.activeRequest = null;	// active XMLHttpRequest object
	this.post           = null; // content for post request
}

FSMLoader.prototype = new FSM;

// state table:
// States: Inactive, Loading
// Events: Get, Update, Loaded, Cancel, Error

FSMLoader.prototype.actionTransitionFunctions = {
	Inactive: {
		Get: function(event) {
			this.parameters = event.parameters || "";
			var xmldata = this.cache[this.parameters];
			if (xmldata) {
				// this value already is cached
				if (typeof(xmldata) == "string") {
					// error message is cached
					return this.doActionTransition("Loading", "Error", {errmsg: xmldata});
				} else {
					activateLoading();
					return this.doActionTransition("Loading", "Loaded", {xmldata: xmldata});
				}
			}
			var self = this;
			var doneCallback = function(xmldata) {
				self.handleEvent({type: "Loaded", xmldata: xmldata});
			};
			var updCallback = function(text) {
				self.handleEvent({type: "Update", text: text});
			}
			var errorCallback = function(errmsg) {
				self.handleEvent({type: "Error", errmsg: errmsg});
			};
			// use a zero-delay timer to send the request asynchronously
			this.activeRequest = new XMLHttpRequest();
			setTimeout(function() {
					activateLoading();
					makeRequest(self.activeRequest, self.url, self.parameters,
						doneCallback, updCallback, self.id, errorCallback, self.post);
				}, 0);
			return "Loading";
		},
		// Allow for possibility of race conditions that generate these events
		// in the wrong state.  Presumably the events are already being handled
		// so they are ignored.
		Update: function(event) { return this.currentState; },
		Loaded: function(event) { return this.currentState; },
		Cancel: function(event) { return this.currentState; },
		Error: function(event) { return this.currentState; }
	},

	Loading: {
		Get: function(event) {
			var newparameters = event.parameters || "";
			if (this.parameters == newparameters) {
				// just continue loading
				return this.currentState;
			} else {
				this.cancelRequest();
				return this.doActionTransition("Inactive", "Get", event);
			}
		},
		Update: function(event) {
			if (this.processingCallback != null) {
				this.processingCallback(event.text);
			}
			return this.currentState;
		},
		Loaded: function(event) {

//			debug("<br/>"+this.id + "(" + this.parameters + "): 'Loaded' event with xmldata &lt;"+
//					event.xmldata.firstChild.nodeName+"&gt; calling "+
//					(''+this.notifyCallback).substring(0,30));

			deactivateLoading();
			this.cache[this.parameters] = event.xmldata;
			this.activeRequest = null;
			this.parameters = null;
			// using synchronous call here to ensure update happens
			this.notifyCallback(event.xmldata);
			return "Inactive";
		},
		Cancel: function(event) {

			this.cancelRequest();
			return "Inactive";
		},
		Error: function(event) {
			deactivateLoading();
			// Cache the error message
			this.cache[this.parameters] = event.errmsg;
			this.activeRequest = null;
			this.parameters = null;
			// This causes an error in Safari 3 Beta?
			// var self = this;
			// setTimeout(function() { self.errorCallback(event.errmsg); }, 0);
			if (this.errorCallback) {
				this.errorCallback(event.errmsg);
			} else {
				alert(event.errmsg);
			}
			return "Inactive";
		}
	}
};

FSMLoader.prototype.setPost = function(input) {
	this.post = input;
}

// Start a new request

FSMLoader.prototype.makeRequest = function(parameters) {
	this.handleEvent({type: "Get", parameters: parameters});
};

// Cancel current request

FSMLoader.prototype.cancelRequest = function() {
	deactivateLoading();
	this.activeRequest.abort();
};


// ----------------- end of FSM definitions -----------------

