function oauthStep1(){
	var url = rootUrl+"request_token";

	dijit.byId("request_token").set("value","");
	dijit.byId("access_token").set("value","");
	dijit.byId("token_secret").set("value","");
	
    dojo.xhrGet(dojox.io.OAuth.sign("GET", {
    	url: url,
        handleAs: "text",
        sync: true,
        load: function(data) {
        	var request_tokens = data.split("&");
        	dijit.byId("request_token").set("value", request_tokens[0].slice("oauth_token=".length));
        	dijit.byId("token_secret").set("value", request_tokens[1].slice("oauth_token_secret=".length));
        	
        	var authDlg = new dijit.Dialog({
        		id: "authDlg",
                title: "Authorization dialog",
                style: "width: 800px; height: 500px; overflow:auto"
            });
        	
        	authDlg.set("content","<iframe src='"+oauthUrl+"oauth_callback=&oauth_token="+
        			dijit.byId("request_token").get("value")+
        			"' width='100%' height='80%' marginwidth='0' marginheight='0' frameborder='no' style='border-width:2px; border-color:#333; background:#FFF; border-style:solid;'></iframe><br/><br/>"+
        			"<center>Please authorize and click <button dojoType='dijit.form.Button' onClick='dijit.byId(\"authDlg\").destroy(); oauthStep2();'>Continue</button></center>");
        	
        	authDlg.show();
        },
        error: function(error, data) {
            alert(error+"\n"+data);
        }
    },getOAuthInfo()));
}

function oauthStep2(){
	var url = rootUrl+"access_token";

	dijit.byId("access_token").set("value","");

    dojo.xhrPost(dojox.io.OAuth.sign("POST", {
    	url: url,
        handleAs: "text",
        sync: false,
        load: function(data) {
        	var request_tokens = data.split("&");
        	dijit.byId("access_token").set("value", request_tokens[0].slice("oauth_token=".length));
        	dijit.byId("token_secret").set("value", request_tokens[1].slice("oauth_token_secret=".length));
        	
        	makeTree();
        	makeTasks();
        },
        error: function(error, data) {
            alert(error+"\n"+data);
        }
    },getOAuthInfo()));
}

function getOAuthInfo() {
	var oauthInfo = {
		       consumer: {
		               key: dijit.byId("consumer_key").get("value"),
		               secret: dijit.byId("consumer_secret").get("value")
		       },
		       sig_method: dijit.byId("signature_method").get("value")
	};
	
	if(dijit.byId("access_token").get("value")) {
		oauthInfo.token = {
            key: dijit.byId("access_token").get("value"),
            secret: dijit.byId("token_secret").get("value")
		};
	} else if(dijit.byId("request_token").get("value")) {
		oauthInfo.token = {
            key: dijit.byId("request_token").get("value"),
            secret: dijit.byId("token_secret").get("value")
		};
	}
	return oauthInfo;
}
