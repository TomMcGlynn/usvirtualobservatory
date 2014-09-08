function makeTransfer() {
   	var voId = dojo.fieldToObject(dojo.byId("voForm").voSelect);

   	obsStore.fetch({
        query: {
            id: voId
        },
        onComplete: function(items, request) {
           	if(obsCredentials[voId]){
           		createNewVoTask(items[0]);
           	} else {
            	oauthStep1Outer(items[0]);
           	}
        }
    });
};

function oauthStep1Outer(vo){
	obsCredentials[vo.id] = {
	    consumer: {
            key: vo.key,
            secret: vo.secret
	    },
   		sig_method: "HMAC-SHA1",
   		url: vo.url[0]
	}

    dojo.xhrGet(dojox.io.OAuth.sign("GET", {
    	url: vo.url+"/request_token",
        handleAs: "text",
        sync: false,
        load: function(data) {
        	var request_tokens = data.split("&");
        	var request_token = request_tokens[0].slice("oauth_token=".length);
        	var token_secret = request_tokens[1].slice("oauth_token_secret=".length);
        	
        	obsCredentials[vo.id].token = {
                key: request_token,
                secret: token_secret
    		};
        	
        	window.open(vo.url+"/authorize?oauth_callback=&oauth_token="+request_token);
			var authButton2 = new dijit.form.Button({
	            label: "Continue",
	            onClick: function() {
	            	oauthStep2Outer(vo);
	            }
	        },
	        "authButton2");
		    
        },
        error: function(error, data) {
            alert(error+"\n"+data);
        }
    },obsCredentials[vo.id]));
}

function oauthStep2Outer(vo){
    dojo.xhrPost(dojox.io.OAuth.sign("POST", {
    	url: vo.url+"/access_token",
        handleAs: "text",
        sync: false,
        load: function(data) {
        	var request_tokens = data.split("&");
        	var request_token = request_tokens[0].slice("oauth_token=".length);
        	var token_secret = request_tokens[1].slice("oauth_token_secret=".length);
        	
        	obsCredentials[vo.id].token = {
                key: request_token,
                secret: token_secret
    		};
        	updateObsCredentialsDiv();
        	createNewVoTask(vo);
        	
        },
        error: function(error, data) {
            alert(error+"\n"+data);
        }
    },obsCredentials[vo.id]));
}

function createNewVoTask(vo) {
	var taskData;
	switch(	transferDirection ){
	case "push":
		taskData = createPushToVoJob(dijit.byId("pathInput").get("value"));
		break;
	case "pull":
		taskData = createPullFromVoJob(dijit.byId("pathInput").get("value"));
		break;
	default:
		alert("Error: not recognized the job type.");
		break;
	}
	
	dojo.xhrPost(dojox.io.OAuth.sign("POST", {
		url: obsCredentials[vo.id].url+"/rest/transfers",
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

function createNewUrlTask() {
	var taskData;
	switch(	transferDirection ){
	case "push":
		taskData = createPushFromVoJob(clickedNode.i.id, dijit.byId("pathInput").get("value"));
		break;
	case "pull":
		taskData = createPullToVoJob(clickedNode.i.id, dijit.byId("pathInput").get("value"), transferType);
		break;
	default:
		alert("Error: not recognized the job type.");
		break;
	}

	dojo.xhrPost(dojox.io.OAuth.sign("POST", {
		url: transfersUrl,
		postData: taskData,
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
}

function updateObsCredentialsDiv() {
	dojo.byId("obsCredentialsDiv").innerHTML = "";
	for(var k in obsCredentials){
		dojo.byId("obsCredentialsDiv").innerHTML += k+": ";
		dojo.byId("obsCredentialsDiv").innerHTML += obsCredentials[k].consumer.key;
		dojo.byId("obsCredentialsDiv").innerHTML += " "+obsCredentials[k].url+"<br/>";
	};
}
