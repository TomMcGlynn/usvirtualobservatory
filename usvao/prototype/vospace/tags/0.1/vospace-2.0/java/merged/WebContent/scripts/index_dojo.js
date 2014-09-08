var rootUrl = document.location.href.slice(0,document.location.href.lastIndexOf('/')+1);
var transfersUrl = rootUrl+"rest/transfers/";
var nodesUrl = rootUrl+"rest/nodes/";
var nodesStoreUrl = rootUrl+"rest/nodesjson/";
var oauthUrl = rootUrl+"authorize?";

var space = "edu.jhu";

var treeStore, treeModel, treeWidget, treeMenu, clickedNode;
var tasksStore, tasksGrid, gridNode;

var transferDirection; // remembers the chosen transfer direction in transfer menu of a tree item
var transferType; // remembers the chosen transfer type in transfer menu of a tree item

var obsCredentials={};

dojo.addOnLoad(initQueryStores);
dojo.addOnLoad(oauthStep1);

function initQueryStores() {
	dojo.provide("my.QueryReadStore");
    dojo.declare("my.QueryReadStore", dojox.data.QueryReadStore, {
    	close: function(/*dojo.data.api.Request || keywordArgs || null */ request){
    	    this._lastServerRequest = null;
    	    this._itemsByIdentity = null;
    	    this._items = null;
    	},
    
		_fetchItems: function(request, fetchHandler, errorHandler){
			var serverQuery = request.serverQuery || request.query || {};
			//Need to add start and count
			if(!this.doClientPaging){
				serverQuery.start = request.start || 0;
				// Count might not be sent if not given.
				if(request.count){
					serverQuery.count = request.count;
				}
			}
			if(!this.doClientSorting && request.sort){
				var sortInfo = [];
				dojo.forEach(request.sort, function(sort){
					if(sort && sort.attribute){
						sortInfo.push((sort.descending ? "-" : "") + sort.attribute);
					}
				});
				serverQuery.sort = sortInfo.join(',');
			}
			// Compare the last query and the current query by simply json-encoding them,
			// so we dont have to do any deep object compare ... is there some dojo.areObjectsEqual()???
			if(this.doClientPaging && this._lastServerQuery !== null &&
				dojo.toJson(serverQuery) == dojo.toJson(this._lastServerQuery)
				){
				this._numRows = (this._numRows === -1) ? this._items.length : this._numRows;
				fetchHandler(this._items, request, this._numRows);
			}else{

				var xhrFunc;
				
				switch(this.requestMethod.toLowerCase()){
					case 'post': xhrFunc = dojo.xhrPost;
					case 'put': xhrFunc = dojo.xhrPut;
					case 'get': xhrFunc = dojo.xhrGet;
					case 'delete': xhrFunc = dojo.xhrDelete;
					default: xhrFunc = dojo.xhrGet;
				}
				
				var xhrHandler = xhrFunc(dojox.io.OAuth.sign(this.requestMethod.toUpperCase(),{url:this.url, handleAs:"json-comment-optional", content:serverQuery}, getOAuthInfo()));
				
				xhrHandler.addCallback(dojo.hitch(this, function(data){
					this._xhrFetchHandler(data, request, fetchHandler, errorHandler);
				}));
				xhrHandler.addErrback(function(error){
					errorHandler(error, request);
				});
				// Generate the hash using the time in milliseconds and a randon number.
				// Since Math.randon() returns something like: 0.23453463, we just remove the "0."
				// probably just for esthetic reasons :-).
				this.lastRequestHash = new Date().getTime()+"-"+String(Math.random()).substring(2);
				this._lastServerQuery = dojo.mixin({}, serverQuery);
			}
		}
    });

	dojo.provide("my.CsvStore");
    dojo.declare("my.CsvStore", dojox.data.CsvStore, {
    	// The dojo.data.api.Read.fetch() function is implemented as
    	// a mixin from dojo.data.util.simpleFetch.
    	// That mixin requires us to define _fetchItems().
    	_fetchItems: function(	/* Object */ keywordArgs, 
    							/* Function */ findCallback, 
    							/* Function */ errorCallback){
    		// summary: 
    		//		See dojo.data.util.simpleFetch.fetch()
    		// tags:
    		//		protected
    		var self = this;
    		var filter = function(requestArgs, arrayOfAllItems){
    			var items = null;
    			if(requestArgs.query){
    				var key, value;
    				items = [];
    				var ignoreCase = requestArgs.queryOptions ? requestArgs.queryOptions.ignoreCase : false; 

    				//See if there are any string values that can be regexp parsed first to avoid multiple regexp gens on the
    				//same value for each item examined.  Much more efficient.
    				var regexpList = {};
    				for(key in requestArgs.query){
    					value = requestArgs.query[key];
    					if(typeof value === "string"){
    						regexpList[key] = dojo.data.util.filter.patternToRegExp(value, ignoreCase);
    					}
    				}

    				for(var i = 0; i < arrayOfAllItems.length; ++i){
    					var match = true;
    					var candidateItem = arrayOfAllItems[i];
    					for(key in requestArgs.query){
    						value = requestArgs.query[key];
    						if(!self._containsValue(candidateItem, key, value, regexpList[key])){
    							match = false;
    						}
    					}
    					if(match){
    						items.push(candidateItem);
    					}
    				}
    			}else{
    				// We want a copy to pass back in case the parent wishes to sort the array.  We shouldn't allow resort 
    				// of the internal list so that multiple callers can get lists and sort without affecting each other.
    				items = arrayOfAllItems.slice(0,arrayOfAllItems.length); 
    				
    			}
    			findCallback(items, requestArgs);
    		};

    		if(this._loadFinished){
    			filter(keywordArgs, this._arrayOfAllItems);
    		}else{
    			if(this.url !== ""){
    				//If fetches come in before the loading has finished, but while
    				//a load is in progress, we have to defer the fetching to be 
    				//invoked in the callback.
    				if(this._loadInProgress){
    					this._queuedFetches.push({args: keywordArgs, filter: filter});
    				}else{
    					this._loadInProgress = true;
    					var getArgs = {
    							url: self.url, 
    							handleAs: "text",
    							preventCache: self.urlPreventCache
    						};
    					//var getHandler = dojo.xhrGet(getArgs);
    					// Dimm
    					var getHandler = dojo.xhrGet(dojox.io.OAuth.sign("GET", getArgs, getOAuthInfo()));

    					getHandler.addCallback(function(data){
    						try{
    							self._processData(data);
    							filter(keywordArgs, self._arrayOfAllItems);
    							self._handleQueuedFetches();
    						}catch(e){
    							errorCallback(e, keywordArgs);
    						}
    					});
    					getHandler.addErrback(function(error){
    						self._loadInProgress = false;
    						if(errorCallback){
    							errorCallback(error, keywordArgs);
    						}else{
    							throw error;
    						}
    					});
    					//Wire up the cancel to abort of the request
    					//This call cancel on the deferred if it hasn't been called
    					//yet and then will chain to the simple abort of the
    					//simpleFetch keywordArgs
    					var oldAbort = null;
    					if(keywordArgs.abort){
    						oldAbort = keywordArgs.abort;
    					}
    					keywordArgs.abort = function(){
    						var df = getHandler;
    						if(df && df.fired === -1){
    							df.cancel();
    							df = null;
    						}
    						if(oldAbort){
    							oldAbort.call(keywordArgs);
    						}
    					};
    				}
    			}else if(this._csvData){
    				try{
    					this._processData(this._csvData);
    					this._csvData = null;
    					filter(keywordArgs, this._arrayOfAllItems);
    				}catch(e){
    					errorCallback(e, keywordArgs);
    				}
    			}else{
    				var error = new Error(this.declaredClass + ": No CSV source data was provided as either URL or String data input.");
    				if(errorCallback){
    					errorCallback(error, keywordArgs);
    				}else{
    					throw error;
    				}
    			}
    		}
    	}
    });

    dojo.provide("my.ForestStoreModel");
    dojo.declare("my.ForestStoreModel", dijit.tree.ForestStoreModel, {
		getChildren: function(parentItem, complete_cb, error_cb) {
			if (parentItem.root == true) {
				// get top level nodes for this plugin id 
				id = '';
			}
			else {
				id = this.store.getValue(parentItem, 'id');
			}
			this.store.fetch({ query: {path:id}, 
							   onComplete: complete_cb, 
							   onError: error_cb});
 
			// Call superclasses' getChildren
			return this.inherited(arguments);
		}

    });

    dojo.provide("my.Tree");
    dojo.declare("my.Tree", dijit.Tree, {
		_onExpandoClick: function(message){
			var node = message.node;
			node.state = "UNCHECKED";
			return this.inherited(arguments);
		}
    });

}

function makeTree() {
    treeStore = new my.QueryReadStore({
    	url: nodesStoreUrl
    });
    
    treeModel = new my.ForestStoreModel({
    	store:treeStore,
        query: {
        	path:''
        },
        rootId: '',
        rootLabel: 'pha.jhu.edu',
        childrenAttrs: ["children"]
    });
    
    treeWidget = new my.Tree({
        model: treeModel
    },
    dojo.byId("treeOne"));
    
    treeMenu = new dijit.Menu({ 
    	targetNodeIds: ["treeOne"]
    }); 
    treeMenu.addChild(new dijit.MenuItem({ 
    	label: "Node metadata", 
    	id: "NodeDef",
    	disabled:false, 
    	onClick:function(e) {
    		readNodeXml(clickedNode.i.id);
    	} 
    })); 
    treeMenu.addChild(new dijit.MenuItem({ 
    	label: "Create new node",
    	id: "NodeCreate",
    	disabled:false, 
    	onClick:function(e) {
    		var newNodeDialog = new dijit.Dialog({
    			title: "Create new node",
    			style: "background-color:white;",
    			id : "newNodeDialog",
    			content: res("resources/newNodeDialog.html"),
    			execute:function(arguments) {
    				createNode(arguments);
    				dijit.popup.close(this);
    				this.destroyRecursive(false);
    			},
    			onCancel: function() {
    				dijit.popup.close(this);
    				this.destroyRecursive(false);
    			}
    		});
    		dijit.byId("newNodeContent").set("value", formatXml(createNewNodeXml("ContainerNode")));
    		newNodeDialog.show();
    	} 
    }));
    treeMenu.addChild(new dijit.MenuItem({ 
    	label: "Delete node",
    	id: "NodeDelete",
    	disabled:false, 
    	onClick:function(e) {
    		deleteNode(clickedNode.i.id);
    	} 
    }));

    var pTransfersMenu = new dijit.Menu();
    pTransfersMenu.addChild(new dijit.MenuItem({ 
    	label: "Pull From VO",
    	disabled:false, 
    	onClick:function(e) {
    		pullFromVo(clickedNode.i.id);
    	} 
    }));
    pTransfersMenu.addChild(new dijit.MenuItem({ 
    	label: "Push To VO", 
    	disabled:false, 
    	onClick:function(e) {
			pushToVo(clickedNode.i.id);
    	} 
    }));
    pTransfersMenu.addChild(new dijit.MenuSeparator());
    pTransfersMenu.addChild(new dijit.MenuItem({ 
    	label: "Pull To VO from another VO",
    	disabled:false, 
    	onClick:function(e) {
    		pullToVoFromVo(clickedNode.i.id);
    	} 
    }));
    pTransfersMenu.addChild(new dijit.MenuItem({ 
    	label: "Push From VO to another VO", 
    	disabled:false, 
    	onClick:function(e) {
			pushFromVoToVo(clickedNode.i.id);
    	} 
    }));
    pTransfersMenu.addChild(new dijit.MenuSeparator());
    pTransfersMenu.addChild(new dijit.MenuItem({ 
    	label: "Pull To VO from URL",
    	disabled:false, 
    	onClick:function(e) {
    		pullToVo(clickedNode.i.id);
    	} 
    }));
    pTransfersMenu.addChild(new dijit.MenuItem({ 
    	label: "Push From VO to URL", 
    	disabled:false, 
    	onClick:function(e) {
			pushFromVo(clickedNode.i.id);
    	} 
    }));
    treeMenu.addChild(new dijit.PopupMenuItem({
        label: "Transfers",
        popup: pTransfersMenu
    }));
    

    dojo.connect(treeMenu, "_openMyself", this, function(e) {
        var tn = dijit.getEnclosingWidget(e.target);
        clickedNode = tn.item;
    	var menuItems = treeMenu.getChildren();
        if(clickedNode.i == undefined) { // root node
			menuItems[0].set("disabled","true");
			menuItems[1].set("disabled",null);
			menuItems[2].set("disabled","true");
			menuItems[3].set("disabled","true");
        } else {
            if(clickedNode.i.type == "CONTAINER_NODE"){
    			menuItems[0].set("disabled",null);
    			menuItems[1].set("disabled",null);
    			menuItems[2].set("disabled",null);
    			menuItems[3].set("disabled",null);
            } else {
    			menuItems[0].set("disabled",null);
    			menuItems[1].set("disabled","true");
    			menuItems[2].set("disabled",null);
    			menuItems[3].set("disabled",null);
            }
        }
    });
    
    treeMenu.startup();

}

//Reloads the tasks widget
function reloadTasks() {
	if(tasksStore){
		tasksStore.close();
	}
	
	tasksStore = new my.CsvStore({
        url: transfersUrl
    });
	tasksStore.fetch();
	tasksGrid.setStore(tasksStore);
}

//Initialises the tasks widget
function makeTasks() {
	tasksStore = new my.CsvStore({
        url: transfersUrl
    });
	tasksStore.fetch();
	
	window["tasksGrid"] = dijit.byId("tasksGrid");

    //menu
    window["tasksMenu"] = dijit.byId("tasksMenu");
	tasksMenu.bindDomNode(tasksGrid.domNode);
	
	// prevent grid methods from killing the context menu event by implementing our own handler
	tasksGrid.onCellContextMenu = function(e) {
		gridNode = e.cellNode;
	};
	tasksGrid.onHeaderContextMenu = function(e) {
		gridNode = e.cellNode;
	};
    
	tasksGrid.setStore(tasksStore);
    tasksGrid.startup();
}

//Shows a dialog with job endpoint
function getJobEndpoint(){
	if(gridNode){
		var jobQueryPath = gridNode.parentNode.children[2].innerHTML;
		dojo.xhrGet(dojox.io.OAuth.sign("GET", {
	        url: transfersUrl+jobQueryPath,
	        handleAs: "xml",
	        sync: false,
	        load: function(data) {
	        	var infoWindow = new dijit.Dialog({
	        	    title: "Job details",
	        	    style: "background-color:white;z-index:5;position:relative;",
	        	    id : "IndoWindow",
	        	    onCancel: function() {
		        	    dijit.popup.close(this);
		        	    this.destroyRecursive(false);
	        	    }
	        	  });
	        	var infoContent = "<p>Endpoint url: <pre>"+selectSingleNode(data.documentElement, "//vos:protocolEndpoint/text()", {vos: "http://www.ivoa.net/xml/VOSpace/v2.0"}).nodeValue+"</pre></p>\n";
	        	infoContent += "<p>Endpoint protocol: <pre>"+selectSingleNode(data.documentElement, "//vos:protocolEndpoint/../@uri", {vos: "http://www.ivoa.net/xml/VOSpace/v2.0"}).nodeValue+"</pre></p>";
	        	infoWindow.set("content",infoContent);
	        	infoWindow.show();
	        },
	        error: function(error, data) {
	            alert(error+"\n"+data.xhr.responseText);
	        }
	    },getOAuthInfo()));
		
	}
}


//Shows a dialog with job Error
function getJobError(){
	if(gridNode){
		var jobQueryPath = gridNode.parentNode.children[2].innerHTML+"/error";
		dojo.xhrGet(dojox.io.OAuth.sign("GET", {
	        url: transfersUrl+jobQueryPath,
	        handleAs: "text",
	        sync: false,
	        load: function(data) {
	        	var infoWindow = new dijit.Dialog({
	        	    title: "Job error",
	        	    style: "background-color:white;z-index:5;position:relative;",
	        	    id : "IndoWindow",
	        	    onCancel: function() {
		        	    dijit.popup.close(this);
		        	    this.destroyRecursive(false);
	        	    }
	        	  });
	        	if(data == "")
	        		data = "-- Nothing found --";
	        	var infoContent = "<p><pre>"+data+"</pre></p>\n";
	        	infoWindow.set("content",infoContent);
	        	infoWindow.show();
	        },
	        error: function(error, data) {
	            alert(error+"\n"+data);
	        }
	    },getOAuthInfo()));
		
	}
}

// Shows a dialog with Job ID
function getJobId(){
	if(gridNode){
    	var infoWindow = new dijit.Dialog({
    	    title: "Job details",
    	    style: "background-color:white;z-index:5;position:relative;",
    	    id : "IndoWindow",
    	    onCancel: function() {
        	    dijit.popup.close(this);
        	    this.destroyRecursive(false);
    	    }
    	  });
    	var infoContent = "<p>Job ID: <pre>"+gridNode.parentNode.children[2].innerHTML+"</pre></p>\n";
    	infoWindow.set("content",infoContent);
    	infoWindow.show();
		
	}
}

//Download the file (pullFromVO job)
function getJobResultData(){
	if(gridNode){
		var jobQueryPath = gridNode.parentNode.children[2].innerHTML+"/results";

		dojo.xhrGet(dojox.io.OAuth.sign("GET", {
	        url: transfersUrl+jobQueryPath,
	        handleAs: "xml",
	        sync: false,
	        load: function(data) {
	        	var detailsUrl = selectSingleNode(data.documentElement, "ns0:result/@ns1:href", {ns0: "http://www.ivoa.net/xml/UWS/v1.0", ns1: "http://www.w3.org/1999/xlink" } ).nodeValue;
        		dojo.xhrGet(dojox.io.OAuth.sign("GET", {
        	        url: detailsUrl,
        	        handleAs: "xml",
        	        sync: false,
        	        load: function(data) {
        	    	   var dataUrl = selectSingleNode(data.documentElement, "protocol[@uri = 'ivo://ivoa.net/vospace/core#httpget']/endpoint/text()", {} ).nodeValue;
        	    	   location.href = dataUrl;
        	        },
        	        error: function(error, data) {
        	            alert(error+"\n"+data.xhr.responseText);
        	        }
        	    },getOAuthInfo()));
	        },
	        error: function(error, data) {
	            alert(error+"\n"+data.xhr.responseText);
	        }
	    },getOAuthInfo()));
	}
}

function readNodeXml(url){
    dojo.xhrGet(dojox.io.OAuth.sign("GET", {
        url: nodesUrl+url,
        handleAs: "text",
        sync: false,
        load: function(data) {
    		var editNodeDialog = new dijit.Dialog({
    			title: url,
    			style: "background-color:white, width: 500px",
    			id : "editNodeDialog",
    			onCancel: function() {
    				dijit.popup.close(this);
    				this.destroyRecursive(false);
    			}
    		});
        	var editMetaDiv = dojo.create("div",{id: "metaDiv"});
        	
    		var metaEditBox = new dijit.InlineEditBox({
    			editor:"dijit.form.Textarea", 
    			autoSave:false, 
    			value: data,
    			id: "nodeXmlContent",
    			style: "width: 500px",
    			width: "500px",
    			onChange: function(value) {
    				setNode(value);
    			}
    		}, editMetaDiv);
    		
    		editNodeDialog.attr("content", metaEditBox.domNode);
    		editNodeDialog.show();
       	
        },
        error: function(error, data) {
            alert(error+"\n"+data.xhr.responseText);
        }
    },getOAuthInfo()));
}

function createNode(formData) {
	var nodeName = formData.nodeName;
	var url = "";
	if(undefined != clickedNode.i) // not root
		url = clickedNode.i.id+"/";
	
	var newNodeTemplate = formData.nodeContent;
	
    dojo.xhrPut(dojox.io.OAuth.sign("PUT", {
		url: nodesUrl+url+nodeName,
		putData: newNodeTemplate,
		handleAs: "text",
		preventCache: true,
        sync: false,
		load: function(data){
        },
        error: function(error, data) {
            alert(error+"\n"+data.xhr.responseText);
        }
    }, getOAuthInfo()));
}

function setNode(data) {
	var url = "";
	if(undefined != clickedNode.i) // not root
		url = clickedNode.i.id+"/";

	var nodeDlg = dijit.byId("editNodeDialog");
	nodeDlg.set("title", url+" updating...");
	
    dojo.xhrPost(dojox.io.OAuth.sign("POST", {
		url: nodesUrl+url,
		postData: data,
		handleAs: "text",
        sync: false,
		load: function(data){
            	//nodeDlg.set("title", url+" updated.");
            	//dijit.byId("nodeXmlContent").set("value", data);
    	    dijit.popup.close(nodeDlg);
    	    nodeDlg.destroyRecursive(false);
        },
        error: function(error, data) {
            alert(error+"\n"+data.xhr.responseText);
        }
    }, getOAuthInfo()));
}

function deleteNode(url) {
    dojo.xhrDelete(dojox.io.OAuth.sign("DELETE", {
        url: nodesUrl+url,
        handleAs: "text",
        sync: false,
        load: function(data){
        	var infoWindow = new dijit.Dialog({
        	    title: "Deleted",
        	    style: "background-color:white;",
        	    id : "IndoWindow",
        	    onCancel: function() {
	        	    dijit.popup.close(this);
	        	    this.destroyRecursive(false);
        	    }
        	  });
        	var infoContent = "<p>The node "+url+" was successfully deleted.</p>\n";
        	infoWindow.set("content",infoContent);
        	infoWindow.show();
        },
        error: function(error, data) {
            alert(error+"\n"+data.xhr.responseText);
        }
    }, getOAuthInfo()));

}

//*************************************************
// Transfer functions
//*************************************************

function pullFromVo(path) {
	var data = createPullFromVoJob(path);
    dojo.xhrPost(dojox.io.OAuth.sign("POST", {
		url: transfersUrl,
		postData: data,
		handleAs: "text",
        sync: false,
		load: function(data){
			reloadTasks();
        },
        error: function(error, data) {
            alert(error+"\n"+data.xhr.responseText);
        }
    }, getOAuthInfo()));	
}

function pushToVo(path) {
	var data = createPushToVoJob(path);
	dojo.xhrPost(dojox.io.OAuth.sign("POST", {
		url: transfersUrl,
		postData: data,
		handleAs: "text",
        sync: false,
		load: function(data){
				reloadTasks();
				//openFileUploadWindow(transfersUrl);
        },
        error: function(error, data) {
            alert(error+"\n"+data.xhr.responseText);
        }
    },getOAuthInfo()));
}

function pushFromVoToVo(path) {
	transferDirection = "push";
	transferType = "httpput";
	var extVoDlg = new dijit.Dialog({
        title: "Please choose the external VO to use.",
        style: "width: 300px",
        href: "outerVoDialog_.html",
        id: "extVoDlg",
        onCancel: function() {
        	this.destroyDescendants(false);
    		this.destroyRecursive(false);
        }
    });
	extVoDlg.show();
}

function pullToVoFromVo(path) {
	transferDirection = "pull";
	transferType = "httpget";
	var extVoDlg = new dijit.Dialog({
        title: "Please choose the external VO to use.",
        style: "width: 300px",
        href: "outerVoDialog_.html",
        id: "extVoDlg",
        onCancel: function() {
        	this.destroyDescendants(false);
    		this.destroyRecursive(false);
        }
    });
	extVoDlg.show();
}

function pushFromVo(path) {
	transferDirection = "push";
	transferType = "httpput";
	var extVoDlg = new dijit.Dialog({
        title: "Please enter the URL to use.",
        style: "width: 400px",
        href: "transferDialog.html",
        id: "extVoDlg",
        onCancel: function() {
        	this.destroyDescendants(false);
    		this.destroyRecursive(false);
        }
    });
	extVoDlg.show();
}

function pullToVo(path) {
	transferDirection = "pull";
	transferType = "httpget";
	var extVoDlg = new dijit.Dialog({
        title: "Please enter the URL to use.",
        style: "width: 400px",
        href: "transferDialog.html",
        id: "extVoDlg",
        onCancel: function() {
        	this.destroyDescendants(false);
    		this.destroyRecursive(false);
        }
    });
	extVoDlg.show();
}

//*************************************************
// End Transfer functions
//*************************************************


function getCapabilities() {
    dojo.xhrGet(dojox.io.OAuth.sign("GET", {
        url: rootUrl+"rest/protocols",
        handleAs: "text",
        sync: false,
        load: function(data) {
        	alert(data);
        },
        error: function(error, data) {
            alert(error+"\n"+data.xhr.responseText);
        }
    }, getOAuthInfo()));	
}

/* Returns a contents of a file */
function res(url){
    var string;
    dojo.xhrGet({
      url: url,
      sync: true,
      load: function(data){
        string = data;
      }
    });
    return string;
}

/* Replaces a text with an image in the grid component */
function formatJobDirection(value){
	switch(value){
		case 'PULLFROMVOSPACE':
			return "<img src='images/PullFrom.png' title='PullFromVoSpace' alt='PullFromVoSpace' height='32'/>";
		case 'PULLTOVOSPACE':
			return "<img src='images/PullTo.png' title='PullToVoSpace' alt='PullToVoSpace' height='32'/>";
		case 'PUSHFROMVOSPACE':
			return "<img src='images/PushFrom.png' title='PushFromVoSpace' alt='PushFromVoSpace' height='32'/>";
		case 'PUSHTOVOSPACE':
			return "<img src='images/PushTo.png' title='PushToVoSpace' alt='PushToVoSpace' height='32'/>";
	}
}

/* Replaces a text with an image in the grid component */
function formatJobState(value){
	switch(value){
		case 'PENDING':
			return "<img src='images/submited.png' title='PENDING' alt='PENDING' height='32'/>";
		case 'RUN':
			return "<img src='images/start.png' title='RUN' alt='RUN' height='32'/>";
		case 'COMPLETED':
			return "<img src='images/finished.jpg' title='COMPLETED' alt='COMPLETED' height='32'/>";
		case 'ERROR':
			return "<img src='images/error.png' title='ERROR' alt='ERROR' height='32'/>";
	}
}