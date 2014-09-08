var panel1 = null, panel2 = null;

var vospaces;

//url: document.location.href.slice(0,document.location.href.lastIndexOf('/')+1)+"rest/1/regions/info",
var rootPrefix = "http://dimm.pha.jhu.edu:8081";

dojo.addOnLoad(function() {
    require(["dojo/parser", "dojo/dom", "my/FilePanel", "my/SyncManager", "my/VosyncReadStore"], function(parser, dom, FilePanel, SyncManager, VosyncReadStore){

    	var loginFunc = function(data){
        	vospaces = data;
        	
        	for(var i in vospaces) {
        		var vospace = vospaces[i];
        		if(vospace.default) {
		        	login(vospace, null);
		        	break;
        		}
        	}
        };
    	
    	
    	
    	dojo.xhrGet({
			url: rootPrefix+"/vospace-2.0/1/regions/info",
			handleAs: "json",
	        load: loginFunc, 
	        error: function(error) { // regions are not supported
	        	dojo.xhrGet({
	    			url: "regions.json",
	    			handleAs: "json",
	    	        load: loginFunc, 
	    	        error: function(error) { // regions are not supported
	    	        	dojo.xhrGet({
	    	    			url: "regions.json",
	    	    			handleAs: "json",
	    	    	        load: loginFunc, 
	    	    	        error: function(error) { // regions are not supported
	    	    	        	alert("Error: can't load the regions info");
	    	    	        }
	    	    	    });
	    	        }
	    	    });
	        }
	    });
    });
});

function loginToVO(vospace, component) {
    require(["dojo/dom", "my/FilePanel", "my/SyncManager"], function(dom, FilePanel, SyncManager){
    	
		if(!vospace) {
			console.error("Unknown vospace "+id);
			return;
		}
		
		if(!vospace.credentials) {
			login(vospace, component);
		} else {
			if(component != null) {
				var store = createStore(vospace);
				component.setStore(store);
			} else { // init
	        	panel1 = new FilePanel({
	    			login: this.loginToVO,
	    			store: createStore(vospace),
	    			vospaces: vospaces,
	    			createNewNodeXml: createNewNodeXml,
	    			}).placeAt(dom.byId("filePanel1Container"));
	    	
	    		panel2 = new FilePanel({
	    			login: this.loginToVO,
	    			store: createStore(vospace),
	    			vospaces: vospaces,
	    			createNewNodeXml: createNewNodeXml,
	    			}).placeAt(dom.byId("filePanel2Container"));
			}
		}
    });	
}

function createStore(vospace) {
	return new my.VosyncReadStore({
		vospace: vospace,
		doClientPaging: true,
		doClientSorting: true,
		pullFromVoJob: pullFromVoJob,
		pullToVoJob: pullToVoJob,
		moveJob: moveJob
	});
}

function dialogAlert(txtTitle, txtContent) {
    require(["dijit/Dialog"], function(Dialog){
	    var dialog = new dijit.Dialog({title: txtTitle, content: txtContent});
	    dojo.body().appendChild(dialog.domNode);
	    dialog.startup();
	    dialog.show();
    });
}

function pullFromVoJob(vospace, id, handler/*function*/, args/*handler args*/) {
    require(["my/OAuth"], function(OAuth){
		var reqData = createPullFromVoJob(id);
	    dojo.xhrPost(OAuth.sign("POST", {
			url: vospace.url+"/transfers",
			postData: reqData,
			handleAs: "xml",
	        sync: false,
	        handle: function(data, ioargs){
	        	if(null != data) {
		        	var endpoint = selectSingleNode(data.documentElement, "//vos:protocolEndpoint/text()", {vos: "http://www.ivoa.net/xml/VOSpace/v2.0"}).nodeValue;
		        	console.debug("Got endpoint for pullFrom job: "+endpoint);
		        	if(null != handler) {
		        		if(null == args)
		        			args = [];
		        		args.push(endpoint);
		        		handler.apply(this, args);
		        	}
	        	} else {
		            console.error("Error creating new pullFrom task");
	        	}
	        }
	    }, vospace.credentials));
    });
}

function pullToVoJob(vospace, id, endpoint) {
	console.debug("Pulling "+endpoint+" to "+id);
    require(["my/OAuth"], function(OAuth){
		var reqData = createPullToVoJob(id, endpoint);
	    dojo.xhrPost(OAuth.sign("POST", {
			url: vospace.url+"/transfers",
			postData: reqData,
			handleAs: "xml",
	        sync: false,
	        handle: function(data, ioargs){
	        	console.debug("Created pullToJob");
	        }
	    }, vospace.credentials));
    });
}

function moveJob(vospace, from, to) {
	console.debug("Moving from"+from+" to "+to);
    require(["my/OAuth"], function(OAuth){
		var reqData = createMoveJob(from, to);
	    dojo.xhrPost(OAuth.sign("POST", {
			url: vospace.url+"/transfers",
			postData: reqData,
			handleAs: "xml",
	        sync: false,
	        handle: function(data, ioargs){
	        	console.debug("Created move Job");
	        }
	    }, vospace.credentials));
    });
}


function createNewVoTask(transferDirection/*push, pull*/, id) {
	var taskData;
	switch(	transferDirection ){
	case "push":
		taskData = createPushToVoJob(id);
		break;
	case "pull":
		taskData = createPullFromVoJob(dijit.byId("pathInput").get("value"));
		break;
	default:
		alert("Error: not recognized the job type.");
		break;
	}
	
	dojo.xhrPost(dojox.io.OAuth.sign("POST", {
		url: obsCredentials[vo.id].url+"/transfers",
		postData: taskData,
		handleAs: "xml",
        sync: false,
		load: function(data){
        	var taskData2;
    		switch(	transferDirection ){
    		case "push":
	        	var dataUrl = selectSingleNode(data.documentElement, "//vos:protocol[@uri = 'ivo://ivoa.net/vospace/core#httpput']/vos:protocolEndpoint/text()", {vos: "http://www.ivoa.net/xml/VOSpace/v2.0"}).nodeValue;
	        	taskData2 = createPushFromVoJob(clickedNode.i.id, dataUrl);
        		break;
    		case "pull":
	        	var dataUrl = selectSingleNode(data.documentElement, "//vos:protocol[@uri = 'ivo://ivoa.net/vospace/core#httpget']/vos:protocolEndpoint/text()", {vos: "http://www.ivoa.net/xml/VOSpace/v2.0"}).nodeValue;
        		taskData2 = createPullToVoJob(clickedNode.i.id, dataUrl, transferType);
        		break;
    		}
        	
        	dojo.xhrPost(dojox.io.OAuth.sign("POST", {
        		url: transfersUrl,
        		postData: taskData2,
        		handleAs: "text",
                sync: false,
        		load: function(data){
        				reloadTasks();
                },
                error: function(error, data) {
                    alert(error+"\n"+data.xhr.responseText);
                }
            },getOAuthInfo()));
        	dijit.byId("extVoDlg").destroyDescendants(false);
        	dijit.byId("extVoDlg").destroyRecursive(false);
        },
        error: function(error, data) {
            alert(error+"\n"+data.xhr.responseText);
        }
    },obsCredentials[vo.id]));
}