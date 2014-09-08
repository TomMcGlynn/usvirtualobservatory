define([
        "dojo/_base/declare",
        "dijit/_WidgetBase",
        "dijit/_TemplatedMixin",
        "dijit/_WidgetsInTemplateMixin",
        "dojo/data/ItemFileReadStore",
        "dojo/data/ItemFileWriteStore",
        "dojo/_base/connect",
        "dojox/grid/EnhancedGrid",
        "dojox/grid/enhanced/plugins/DnD",
        "dojox/grid/enhanced/plugins/Selector",
        "dojox/grid/enhanced/plugins/Menu",
        "dojox/grid/enhanced/plugins/exporter/CSVWriter",
        "dijit/Menu",
        "dojo/text!./SyncManager/templates/SyncManager.html",
        "dijit/form/Button",
        ],
    function(declare, WidgetBase, TemplatedMixin, WidgetsInTemplateMixin, ItemFileReadStore, ItemFileWriteStore, connect, EnhancedGrid, DnDPlugin, SelectorPlugin, MenuPlugin, CSVPlugin, Menu, template, Button){
        return declare([WidgetBase, TemplatedMixin, WidgetsInTemplateMixin], {
        	
        templateString: template,
        
        grid1: null,
        grid2: null,
		
        layout: [{
		    rows: [
		        {field: "id", width: "200px"},
		    ]
		}],

        items: null,
        regions_url: null,
		
        postMixInProperties: function() {
        },
        
        postCreate: function(){
            this.inherited(arguments);
            var panel = this;
            var deferred = dojo.xhrGet({
				url: this.regions_url,
				handleAs: "json"
				});
			deferred.addCallback(function(vospArray) {

				var data = {identifier: "id", items: vospArray};
				
    			var store1 = new ItemFileReadStore({
					data: data
				});

   	            //store1.fetchItemByIdentity({
  	            //    identity:"dimm.pha.jhu.edu",
  	            //    onItem:function(item){
  	            //        console.debug(item);
  	            //    }
   	            //});

    			
    		    panel.grid1 = new dojox.grid.EnhancedGrid({
    		        id: 'grid1',
    		        store: store1,
    		        structure: panel.layout,
    		        rowSelector: '20px',
    		        canSort: function(){return false;},
    		        plugins: {
    		        	dnd: {
    		            	'dndConfig': {
    		            		out: {
    				                col: false,
    				                row: true,
    				                cell: false
    			            	},
    		            	in: false
    			            }
    	    			},
  			            selector: {
  			            	row: 'single',
  			            	cell: 'disabled'
  			            }
    		        }
    		    },panel.gridWidget1);

    		    panel.grid1.startup();
			
				var menuObject = {rowMenu: new Menu()};
				
				menuObject.rowMenu.addChild(new dijit.MenuItem({ 
			    	label: "Delete", 
			    	onClick:function(e) {
			    		var selectedItems =panel.grid2.selection.getSelected("row", true);
			    		
			    		dojo.forEach(selectedItems, function(selectedItem){
			                if(selectedItem !== null){
			                    panel.grid2.store.deleteItem(selectedItem);
			                }
			            });
			    	} 
			    })); 

				
	         	var store2 = new ItemFileReadStore({
	 				data: {identifier: "id", items: []}
	 			});
				
			    panel.grid2 = new dojox.grid.EnhancedGrid({
			        id: 'grid2',
			        store: store2,
			        structure: panel.layout,
			        rowSelector: '20px',
			        //canSort: function(){return false;},
			        plugins: {
			        	dnd: {
			            	'dndConfig': {
			            		out: false,
			            	in: {
					                col: false,
					                row: true,
					                cell: false
				            	}
				            }
		    			},
				            selector: {
				            	row: 'single',
				            	cell: 'disabled'
				            },
				            menus: menuObject,
				            exporter: true
			        }
			    },panel.gridWidget2);
		
			    panel.grid2.startup();
			    
			    panel.setSyncNode(panel.items);
			});


        },
        
        setSyncNode: function(items) {
        	if(null != items) {
	        	var store = new ItemFileWriteStore({
					data: {identifier: "id", items: items}
				});
	
	    		var oldStore = this.grid2.store;
	    		
	    		this.grid2.setStore(store);
	    		oldStore.close();
        	}		    
        },

    });
});