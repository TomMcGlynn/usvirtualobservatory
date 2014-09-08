define([
        "dojo/_base/declare",
        "dojo/_base/connect",
        "dijit/_WidgetBase",
        "my/DataGrid",
        "dojox/grid/enhanced/plugins/DnD", 
        "dojox/grid/enhanced/plugins/Selector",
        "dojox/grid/enhanced/plugins/Menu",
        "dijit/Menu",
        "dojox/image/Lightbox",
        "my/OAuth",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/text!./FilePanel/templates/FilePanel.html",
        "dijit/layout/ContentPane",
        "dijit/form/Form",
        "dijit/form/Button",
        "dijit/form/Select",
        "dijit/form/ValidationTextBox",
        "dijit/form/TextBox",
        "dijit/form/Textarea",
        "dijit/InlineEditBox",
        "dijit/Toolbar",
        "dijit/ProgressBar",
        "dijit/Dialog",
        "dijit/registry",
        "my/SyncManager",
        "my/JobsManager"
       ],
    function(declare, connect, WidgetBase, DataGrid, DnDPlugin, SelectorPlugin, MenuPlugin, Menu, LightBox, OAuth, TemplatedMixin, WidgetsInTemplateMixin, template, ContentPane, Form, Button, Select, ValidationTextBox, TextBox, Textarea, InlineEditBox, Toolbar, ProgressBar, Dialog, registry, SyncManager, JobsManager){
        return declare([WidgetBase, TemplatedMixin, WidgetsInTemplateMixin], {
        	
            templateString: template,
            store : null,
            gridWidget: null,
            vospacesSelect: "",
            
            postMixInProperties: function() {
            	this.vospacesSelect = "";
            	if(this.vospaces != null) {
            		for(var i in vospaces){
            			var vospace = vospaces[i];
            			if(vospace.id == this.store.vospace.id)
            				this.vospacesSelect += "<option value=\""+vospace.id+"\" selected=\"true\">"+vospace.display+"</option>";
            			else
            				this.vospacesSelect += "<option value=\""+vospace.id+"\">"+vospace.display+"</option>";
            		}
            	}
            },
            
            postCreate: function(){
                var domNode = this.domNode;
                this.inherited(arguments);
                
                var panel = this;
                
                this.connect(this.pathSelect, "onChange", "_updateStore");
                this.connect(this.loginSelect, "onChange", "_login");
         	    this.connect(window, "onresize", "_resizeGrid");
         	    this.connect(this.transfersDialog, "onHide", "_destroyJobs");

         	    if(null != this.store) {
    				var layout = [
  				  				{ name: ' ', field: 'is_dir' , formatter: this._formatFileIcon, width: "10%"},
  				  				{ name: 'Name', field: 'path' , formatter: this._getName, width: "40%"},
  				  				{ name: 'Size', field: 'size' , width: "10%"},
  				  				{ name: 'Modified', field: 'modified' , width: "40%"}
  				  		];
  			
  				
  				var menuObject = {rowMenu: new Menu()};

  				var panel = this; // to keep context
  				menuObject.rowMenu.addChild(new dijit.MenuItem({ 
  			    	label: "Metadata...", 
  			    	onClick:function(e) {
  			    		var selectedItems = panel.gridWidget.selection.getSelected("row", true);
  			    		if(selectedItems.length > 1){
  			    			alert("Please choose one item only");
  			    		} else {
  	  			    		panel._showMetadata(selectedItems[0].i.path);
  			    		}
  			    	} 
  			    })); 
  				menuObject.rowMenu.addChild(new dijit.MenuItem({ 
  			    	label: "Preview...", 
  			    	onClick:function(e) {
  			    		var selectedItems = panel.gridWidget.selection.getSelected("row", true);
  			    		if(selectedItems.length > 1){
  			    			alert("Please choose one node only");
  			    		} else {
  				    	    dojo.xhrGet(my.OAuth.sign("GET", {
  				    	        url: panel.store.vospace.url+"/1/shares/sandbox"+selectedItems[0].i.path,
  				    	        handleAs: "json",
  				    	        sync: false,
  				    	        load: function(data) {
  				    	        	var lb = new dojox.image.Lightbox({ title:"Preview", group:"group", href:data.url });
  				    	        	lb.startup();
  				    	        	setTimeout(function(){
  				    	        		lb.show();
  				    	        	}, 2000);
  				    	        },
  				    	        error: function(error, data) {
  				    	            alert(error+"\n"+data.xhr.responseText);
  				    	        }
  				    	    },panel.store.vospace.credentials));
  			    		}
  			    	} 
  			    })); 
  				menuObject.rowMenu.addChild(new dijit.MenuItem({ 
  			    	label: "Delete", 
  			    	onClick:function(e) {
  			    		var selectedItems =panel.gridWidget.selection.getSelected("row", true); 
  				        for(var i = 0; i < selectedItems.length; i++) {
  				        	panel._deleteItem(selectedItems[i].i.path);
  				        }
  	    	        	var infoWindow = new dijit.Dialog({
  	    	        	    title: "Deleted",
  	    	        	    style: "background-color:white;",
  	    	        	    id : "IndoWindow",
  	    	        	    onCancel: function() {
  	    		        	    dijit.popup.close(this);
  	    		        	    this.destroyRecursive(false);
  	    	    	        	panel._refresh();
  	    	        	    }
  	    	        	  });
  	    	        	if(selectedItems.length > 0){
  	  	    	        	infoWindow.set("content","<p>The node(s) were deleted.</p>\n");
  	    	        	} else {
  	  	    	        	infoWindow.set("content","<p>Nothing was selected.</p>\n");
  	    	        	}
  	    	        	infoWindow.show();
  			    	} 
  			    })); 
  				menuObject.rowMenu.addChild(new dijit.MenuItem({ 
  			    	label: "Pull from URL...",
  			    	tooltip: "Pull data from URL to the selected item",
  			    	onClick:function(e) {
  			    		var selectedItems = panel.gridWidget.selection.getSelected("row", true);
  			    		if(selectedItems.length > 1){
  			    			alert("Please choose one item only");
  			    		} else {
	  			          	panel.transferNode.value = selectedItems[0].i.path;
	  			        	panel.urlInput.reset();
	  						panel.transferUrlDialog.show();
  			    		}
  			    	} 
  			    })); 
  				menuObject.rowMenu.addChild(new dijit.MenuItem({ 
  			    	label: "Sync...", 
  			    	onClick:function(e) {
  			    		var selectedItems =panel.gridWidget.selection.getSelected("row", true);
  			    		if(selectedItems.length > 1){
  			    			alert("Please choose one node only");
  			    		} else {
  	  			    		panel._showSyncDialog(selectedItems[0].i.path);
  			    		}
  			    	} 
  			    })); 
  				menuObject.rowMenu.addChild(new dijit.MenuItem({ 
  			    	label: "Share URL...", 
  			    	onClick:function(e) {
  			    		var selectedItems =panel.gridWidget.selection.getSelected("row", true);
  			    		if(selectedItems.length > 1){
  			    			alert("Please choose one node only");
  			    		} else {
				    	    dojo.xhrGet(my.OAuth.sign("GET", {
				    	        url: panel.store.vospace.url+"/1/shares/sandbox"+selectedItems[0].i.path,
				    	        handleAs: "json",
				    	        sync: false,
				    	        load: function(data) {
				    		    	var infoWindow = new dijit.Dialog({
				    		    	    title: "File URL",
				    		    	    style: "background-color:white;z-index:5;position:relative;",
				    		    	    id : "IndoWindow",
				    		    	    onCancel: function() {
				    		        	    dijit.popup.close(this);
				    		        	    this.destroyRecursive(false);
				    		    	    }
				    		    	  });
				    		    	var infoContent = "<p>Data URL: <a href="+data.url+">"+data.url+"</p>\n";
				    		    	infoWindow.set("content",infoContent);
				    		    	infoWindow.show();
				    	        },
				    	        error: function(error, data) {
				    	            alert(error+"\n"+data.xhr.responseText);
				    	        }
				    	    },panel.store.vospace.credentials));
  			    		}
  			    	} 
  			    })); 
  				
  				menuObject.rowMenu.startup();

  				this.gridWidget = new DataGrid({
  			        id: this.grid.id,
  			        store: this.store,
  			        structure: layout,
  			        rowSelector: '10px',
  			        plugins:{
  			            dnd: {
  			            	'dndConfig': {
  			            		out: {
	  				                col: false,
	  				                row: true,
	  				                cell: false
	  			            	},
				            	in: {
	  				                col: false,
	  				                row: true,
	  				                cell: false
	  			            	},
	  			            	within: {
	  			            		col: false,
	  				                row: false,
	  				                cell: false
	  			            	}
  			            	}
  			            },
  			            selector: {
  			            	row: 'multiple',
  			            	cell: 'disabled'
  			            },
  			            menus: menuObject
  			        },
  			        query: {list: 'true'},
  			        pathWidget: this.pathSelect,
  			        onRowDblClick : function(e) {
  				        var item = this.selection.getSelected("row", true)[0];
  				        if(item.i.is_dir) {
  				        	this.setCurrentPath(item.i.path);
  				        } else {
  				    	    dojo.xhrGet(my.OAuth.sign("GET", {
  				    	        url: panel.store.vospace.url+"/1/shares/sandbox"+item.i.path,
  				    	        handleAs: "json",
  				    	        sync: false,
  				    	        load: function(data) {
  				    	        	location.href = data.url;
  				    	        },
  				    	        error: function(error, data) {
  				    	            alert(error+"\n"+data.xhr.responseText);
  				    	        }
  				    	    },panel.store.vospace.credentials));
  				        }
  				        	
  			        },
  			        
  			        /**
  			         * TODO
  			         * figure out how to change the menu on click for different items chosen
  			         */
  			        onRowContextMenu: function(e) {
  			        	console.debug(e);
  			        }
  			      }, this.grid);
         	    connect.connect(this.gridWidget.plugin('dnd'), "onDragIn", this, "_dragIn");
  			
  			    /*Call startup() to render the grid*/
  			    this.gridWidget.startup();

				dojo.xhrGet(OAuth.sign("GET", {
			        url: this.store.vospace.url+"/1/account/info",
			        handleAs: "json",
			        sync: false,
			        load: function(accountInfo) {
			        	panel.userLimitBar.update({
			                maximum: accountInfo.quota_info.quota,
			                progress: accountInfo.quota_info.normal
			            });
			        	
			        	var tooltipText = dojo.number.format(accountInfo.quota_info.normal)+" bytes";
			        	panel.userLimitTooltip.set("label", tooltipText);
			        	dijit.Tooltip.defaultPosition=['below','after'];
			        	panel.userLimitTooltip.set("connectId",panel.userLimitBar.id);
			        },
			        error: function(error, data) {
			        	console.error(error);
			        }
			    },this.store.vospace.credentials));
  			    
                
            }
                
        },
        
        _refresh: function() {
        	this.gridWidget._refresh(true);
    		this.gridWidget.plugin('selector').clear();
        },
        
        _mkdirDialog: function() {
        	this.newNodeName.reset();
        	this.mkdirDialog.show();
        },

        _mkdir: function() {
        	var panel = this;
        	
        	if(this.createNewNodeXml != null) {
	        	var nodeid = this.store.getNodeVoId(this.gridWidget._currentPath+"/"+this.newNodeName.value);
	        	var nodeTemplate = formatXml(this.createNewNodeXml("ContainerNode", nodeid, this.store.vospace.id));
	        	
			    dojo.xhrPut(my.OAuth.sign("PUT", {
					url: this.store.vospace.url+"/nodes"+this.gridWidget._currentPath+"/"+this.newNodeName.value,
					putData: nodeTemplate,
					handleAs: "text",
					load: function(data){
						panel._refresh();
					},
			        error: function(error, data) {
			            alert(error+"\n"+data.xhr.responseText);
			        }
			    }, this.store.vospace.credentials));
        	}
        },
        
        _mkfileDialog: function() {
        	this.newNodeName.reset();
        	this.mkfileDialog.show();
        },

        _mkfile: function() {
        	var panel = this;
        	
        	if(this.createNewNodeXml != null) {
	        	var nodeid = this.store.getNodeVoId(this.gridWidget._currentPath+"/"+this.newNodeName.value);
	        	var nodeTemplate = formatXml(this.createNewNodeXml("DataNode", nodeid, this.store.vospace.id));
	        	
			    dojo.xhrPut(my.OAuth.sign("PUT", {
					url: this.store.vospace.url+"/nodes"+this.gridWidget._currentPath+"/"+this.newNodeName.value,
					putData: nodeTemplate,
					handleAs: "text",
					load: function(data){
						panel._refresh();
					},
			        error: function(error, data) {
			            alert(error+"\n"+data.xhr.responseText);
			        }
			    }, this.store.vospace.credentials));
        	}
        },
        
        _dragIn: function(sourcePlugin, isCopy) {
        	var selectedArray = sourcePlugin.selector.getSelected("row", true);
        	
        	for(var i=0; i<selectedArray.length; i++) {
	        	var nodePath = selectedArray[i].id;
	        	var nodeId = sourcePlugin.grid.store.getNodeVoId(nodePath);
	
	        	var nodePathArray = nodePath.split('/');
	        	var nodeName = nodePathArray[nodePathArray.length-1];
	        	
	        	var curPath = this.gridWidget._currentPath;
	        	var curPathArray = curPath.split('/');
	        	curPathArray.push(nodeName);
	        	curPath = curPathArray.join("/");
	        	var thisNodeId = this.store.getNodeVoId(curPath);
	        	
	        	var store = this.store;
	        	var args = [store.vospace, thisNodeId];
	
	        	if(sourcePlugin.grid.store.vospace != this.store.vospace) { // different VOSpaces
	        		sourcePlugin.grid.store.pullFromVoJob(sourcePlugin.grid.store.vospace, nodeId, store.pullToVoJob, args);
	        	} else {
	        		sourcePlugin.grid.store.moveJob(store.vospace, nodeId, thisNodeId);
	        	}
        	}

        	var panel = this;
        	
        	setTimeout(function() {
            	panel._refresh();
            	sourcePlugin.grid._refresh(true);
        	},1000);
        },
        
        _deleteItem: function(path) {
        	console.debug("Deleting item: "+path);
    	    dojo.xhrDelete(OAuth.sign("DELETE", {
    	        url: this.store.vospace.url+"/nodes"+path,
    	        handleAs: "text",
    	        handle: function(error, ioargs){
    	        },
    	        error: function(error, data) {
    	            alert(error+"\n"+data.xhr.responseText);
    	        }
    	    }, this.store.vospace.credentials));
        },
        
        _resizeGrid: function() {
     	   this.gridWidget.resize();
     	   this.gridWidget.update();
     	},

        setStore: function(store) {
        	this.store = store;
        	this.gridWidget.setStore(store);
        },
        
        _updateStore: function(path) {
        	if(path.length > 0){this.gridWidget.setCurrentPath(path);}
        },
        
        _formatFileIcon: function(isDir){
        	switch(isDir){
        		case true:
        			return "<img src='images/folder.jpg' title='Folder' alt='Folder' height='16'/>";
        		case false:
        			return "<img src='images/file.svg' title='File' alt='File' height='16'/>";
        	}
        },
        
        _getName: function(path) {
        	var pathTokens = path.split('/');
        	return pathTokens[pathTokens.length-1];
        },
        
        _login: function(id) {
        	this.login(this._getVospace(id), this);
        },
        
        _getVospace: function(id) {
        	var vospace;
    		for(var i in vospaces){
    			if(vospaces[i].id == id) {
    				vospace = vospaces[i]; 
    			}
    		}
    		return vospace;
        },
        
        _showSyncDialog: function(path) {
			this.syncDialog.show();
			
        	this.syncContainer.value = path; 
            	
			var panel = this;
			
    	    dojo.xhrGet(OAuth.sign("GET", {
    	        url: this.store.vospace.url+"/1/regions/"+path,
    	        handleAs: "json",
    	        load: function(data){
    				if(null == panel.syncManager.setSyncNode) {
    					panel.syncManager = new SyncManager({
    						items: data,
    						regions_url: panel.store.vospace.url+"/1/regions/info"
    					},panel.syncManager);
    				} else {
    					var items = data;
    					panel.syncManager.setSyncNode(items);
    				}
    	        },
    	        error: function(error, data) {
    	            alert(error+"\n"+data.xhr.responseText);
    	        }
    	    }, this.store.vospace.credentials));
			
        },
        
        _showMetadata: function(path) {
			var panel = this;
			
    	    dojo.xhrGet(OAuth.sign("GET", {
    	        url: this.store.vospace.url+"/nodes/"+path,
    	        handleAs: "text",
    	        load: function(data){
    	    		var editNodeDialog = new dijit.Dialog({
    	    			title: path,
    	    			style: "background-color:white, width: 500px",
    	    			id : "editNodeDialog",
    	    			onCancel: function() {
    	    				dijit.popup.close(this);
    	    				this.destroyRecursive(false);
    	    			}
    	    		});
    	        	var editMetaDiv = dojo.create("div",{id: "metaDiv"});
    	        	
    	    		var metaEditBox = new InlineEditBox({
    	    			editor: Textarea,
    	    			autoSave:false, 
    	    			value: data,
    	    			id: "nodeXmlContent",
    	    			style: "width: 600px",
    	    			width: "600px",
    	    			onChange: function(value) {
    	    				
    	    			    dojo.xhrPost(my.OAuth.sign("POST", {
    	    					url: panel.store.vospace.url+"/nodes/"+path,
    	    					postData: value,
    	    					handleAs: "text",
    	    			        sync: false,
    	    					load: function(data){
    	    			    	    dijit.popup.close(editNodeDialog);
    	    			    	    editNodeDialog.destroyRecursive(false);
    	    			        },
    	    			        error: function(error, data) {
    	    			            alert(error+"\n"+data.xhr.responseText);
    	    			        }
    	    			    }, panel.store.vospace.credentials));
    	    				
    	    			}
    	    		}, editMetaDiv);
    	    		
    	    		editNodeDialog.attr("content", metaEditBox.domNode);
    	    		editNodeDialog.show();
    	        	
    	        },
    	        error: function(error, data) {
    	            alert(error+"\n"+data.xhr.responseText);
    	        }
    	    }, this.store.vospace.credentials));
			
        },
        
        _pullToVo: function() {
        	this.store.pullToVoJob(this.store.vospace, 
        			this.store.getNodeVoId(this.transferNode.value),
        			this.urlInput.value);
        },
        
        _sync: function() {
        	var panel = this;
        	this.syncManager.grid2.exportGrid("csv", function(str){
        		var syncOrder = str.split("\r\n");
        		syncOrder = syncOrder.slice(1,syncOrder.length);
        		var syncOrderResultStr = syncOrder.join("\n");
        		
    		    dojo.xhrPut(my.OAuth.sign("PUT", {
    				url: panel.store.vospace.url+"/1/regions_put"+panel.syncContainer.value,
    				putData: syncOrderResultStr,
    				handleAs: "text",
    				handle: function(data, ioargs){
    		        },
    		        error: function(error, data) {
    		            alert(error+"\n"+data.xhr.responseText);
    		        }
    		    }, panel.store.vospace.credentials));
        		
        		
       		});
        	
        },

        _destroyJobs: function() { // destroys jobs grid
        	this.transfersManager.destroyRecursive();
        },
        
        _showTransfDialog: function() {

			var transfersManager = new JobsManager({
				vospace: this.store.vospace,
				transfers_url: this.store.vospace.url+"/1/transfers/info"
			});
			
			var dialog = new Dialog({
	            content: transfersManager,
	            onHide: function() {
	            	transfersManager.destroyRecursive();
	            	this.destroyRecursive();
	            }
	        });

			dialog.show();
			
        },
        
        login: function(id, component) {}
    });
});