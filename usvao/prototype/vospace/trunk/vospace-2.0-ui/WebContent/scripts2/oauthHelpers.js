function login(vospace, component) {
	console.debug(vospace);
    require(["dojo/cookie", "dojo/_base/json", "my/OAuth", "dijit/Dialog", "dojo/_base/connect", "dijit/layout/ContentPane"], function(cookie, dojo, OAuth, Dialog, connect, ContentPane){
    	var config = { consumer: {key: "sclient", secret: "ssecret"}};
        function success(data) {
            var request_tokens = data.split("&");
            var reqToken = request_tokens[0].slice("oauth_token=".length);
            var tokenSecret = request_tokens[1].slice("oauth_token_secret=".length);

            /*var cookie_value = {
            		"requestToken": reqToken, 
            		"tokenSecret": tokenSecret, 
            		"consumerKey": vospace.credentials.consumer.key, 
            		"consumerSecret": vospace.credentials.consumer.secret, 
            		"callbackUrl":""};
            cookie("vospace_oauth_extended", dojo.toJson(cookie_value), {} );*/

        	if(dijit.byId('formDialog') != undefined){
        		dijit.byId('formDialog').destroyRecursive();
        	}

    		var div = dojo.doc.createElement("div");
    		div.innerHTML += "Please authenticate at <a href='"+vospace.url+"/authorize?provider=vao&action=initiate&oauth_token="+reqToken+"' target='_blanc'>VAO</a> and click ";
    		
    		var button = new dijit.form.Button({
    			label: 'Done',
    			onClick: function () {
    	    		vospace.credentials = {
	    				sig_method: 'HMAC-SHA1',
	    				consumer: {
	    					key: 'sclient',
	    					secret: 'ssecret'
	    				},
	    				token: {
			            	key: reqToken,
			            	secret: tokenSecret
	    		        }
	    			};
    				dijit.byId('formDialog').hide();
    	        	login2(vospace, component);
    			}
    		});
    		div.appendChild(button.domNode);

    		var loginDialog = new dijit.Dialog({
    			id: 'formDialog',
    			title: "Authentication",
    			style: "width: 300px",
    			content: div
    		});
        	dijit.byId('formDialog').show();
        }
        
        function failure(data) { console.debug('Something bad happened: ' + data); }
		var xhrArgs = {
		    url: vospace.url+'/request_token',
		    handleAs: "text",
		    preventCache: false,
		    load: success,
		    error: failure
		};
		var args = OAuth.sign("GET", xhrArgs, config);
		dojo.xhrGet(args);
    });

}

function login2(vospace, component) {
    require(["dojo/cookie", "dojo/_base/json", "my/OAuth"], function(cookie, dojo, OAuth){
    	var url = vospace.url+"/access_token";

	    dojo.xhrPost(OAuth.sign("POST", {
	    	url: url,
	        handleAs: "text",
	        sync: false,
	        load: function(data) {
	        	var request_tokens = data.split("&");
	        	var token = request_tokens[0].slice("oauth_token=".length), tokenSecret = request_tokens[1].slice("oauth_token_secret=".length);
	        	
	        	/*var cookie_value = { 
	        			"accessToken": token, 
	        			"tokenSecret": tokenSecret, 
	        			"consumerKey": vospace.credentials.consumer.key, 
	        			"consumerSecret": vospace.credentials.consumer.secret
	        		};
	            dojo.cookie("vospace_oauth_extended", dojo.toJson(cookie_value), {} );*/

	            vospace.credentials.token = {
                	key: token,
                	secret: tokenSecret
                };
	            loginToVO(vospace, component); // with updated credentials
	        },
	        error: function(error, data) {
	        	console.error(error);
				alert("Error: Unable to authenticate. Did you authorize?");
				vospace.credentials = null;
	        }
	    },vospace.credentials));
    });

}