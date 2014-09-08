define([
        "dojo/_base/declare",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/_base/connect",
        "dojo/dom",
        "dojo/store/Memory",
        "dojo/data/ObjectStore",
        "dijit/Menu",
        "dijit/MenuItem",
        "my/OAuth",
        "dojo/text!./JobsManager/templates/JobsManager.html"
        ],
    function(declare, WidgetBase, TemplatedMixin, WidgetsInTemplateMixin, connect, dom, Memory, ObjectStore, Menu, MenuItem, OAuth, template){
        return declare([WidgetBase, TemplatedMixin, WidgetsInTemplateMixin], {

        templateString: template,
        transfers_url: null,
        vospace: null,
        
        layout: [{
		    rows: [
		        {name: "direction", field: "direction", width: "6.5%", formatter: formatJobDirection, style: "text-align: center"},
		        {name: "state", field: "state", width: "6.5%", formatter: formatJobState, style: "text-align: center"},
		        {name: "id", field: "id", width: "25%"},
		        {name: "starttime", field: "starttime", width: "13%"},
		        {name: "endtime", field: "endtime", width: "13%"},
		        {name: "path", field: "path"},
		    ]
		}],
        
        postCreate: function(){
            this.inherited(arguments);
            
            var panel = this;
            dojo.xhrGet(OAuth.sign("GET", {
					url: this.transfers_url,
					handleAs: "json",
					load: function(data) {
		    			var store = new Memory({
							data: data
						});
		    			panel._supportingWidgets.push(store);

		    		    panel.jobsgrid = new dojox.grid.DataGrid({
		    		        id: 'jobsgrid',
		    		        store: ObjectStore({objectStore: store}),
		    		        structure: panel.layout,
		    		        rowSelector: '20px',
		    		    },panel.jobsgrid);
		    		    
		    			panel._supportingWidgets.push(panel.jobsgrid);
		    		    panel.jobsgrid.startup();
		    		    
		    		    var menu = new Menu({ 
		    		    	targetNodeIds: ["jobsgrid"]
		    		    });
		    		    
		    			panel._supportingWidgets.push(menu);

		    			menu.addChild(new dijit.MenuItem({ 
		    		    	label: "View job error", 
		    		    	id: "err",
		    		    	disabled:false, 
		    		    	onClick:function(e) {
		    		    		alert("View error");
		    		    	} 
		    		    })); 
		    		    menu.startup();
		    		    
					},
					error: function(error) {
						alert("Error loading the jobs: "+error);
					}
				},this.vospace.credentials));
        },
        
        //TODO NOT FINISHED! (task get error)
        getJobError: function(){
			var jobQueryPath = gridNode.parentNode.children[2].innerHTML+"/error";
			dojo.xhrGet(OAuth.sign("GET", {
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
		    },this.vospace.credentials));
        }
        
        
    });
        
        
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

});