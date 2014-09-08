/*!
 * Ext JS Library 4.0
 * Copyright(c) 2006-2011 Sencha Inc.
 * licensing@sencha.com
 * http://www.sencha.com/license
 */

Ext.define('MyDesktop.DnDWindow', {
    extend: 'Ext.ux.desktop.Module',

    requires: [
		'Ext.tree.*',
		'Ext.data.*',
		'Ext.tip.*'
    ],

    id:'dnd-win',


    constructor: function (config) {
        this.portal = config.portal;
        delete config.portal;
		this.callParent(arguments);
    },

    init : function(){
        this.launcher = {
            text: 'VO Space',
            iconCls:'icon-folder'
        };
    },

    createWindow : function(){
        var desktop = this.app.getDesktop();
        var win = desktop.getWindow('dnd-win');
        if(!win){
            var tree = this.createDnDTree();
            
            win = desktop.createWindow({
                id: 'dnd-win',
                title:'VO Space',
                //width:870,
                //y: 10,
                //height:480,
                iconCls: 'icon-folder',
                animCollapse:false,
                constrainHeader:true,
                layout: 'fit',
                items: [
                    tree
                ]
            });
        }
        return win;
    },
	
	createDnDTree: function() {
		var store = Ext.create('Ext.data.TreeStore', {
			//proxy: {
			//	type: 'ajax',
			//	url: 'get-nodes.php'
			//},
			//root: {
			//	text: 'Ext JS',
			//	id: 'src',
			//	expanded: true
			//},
			//folderSort: true,
			//sorters: [{
			//	property: 'text',
			//	direction: 'ASC'
			//}]

    root: {
        expanded: true,
		text: 'Tom',
        children: [
            { text: "Images", children: [
                { text: "M51.fits", leaf: true },
                { text: "M104.fits", leaf: true}
            ]  },
            { text: "Catalog", expanded: true, children: [
                { text: "2MASS-M51.vot", leaf: true },
                { text: "2MASS-M51.csv", leaf: true}
            ] },
            { text: "Publications", leaf: true },
            { text: "Notes", children: [
                { text: "VAO Meeting.doc", leaf: true },
                { text: "Poster.pdf", leaf: true}
            ]  }
        ]
    }


		});
	
		var tree = Ext.create('Ext.tree.Panel', {
			store: store,
			viewConfig: {
				plugins: {
					ptype: 'treeviewdragdrop'
				}
			},
			//renderTo: 'tree-div',
			height: 300,
			width: 250,
			title: 'Files',
			useArrows: true,
			dockedItems: [{
				xtype: 'toolbar',
				items: [{
					text: 'Expand All',
					handler: function(){
						tree.getEl().mask('Expanding tree...');
						var toolbar = this.up('toolbar');
						toolbar.disable();
						
						tree.expandAll(function() {
							tree.getEl().unmask();
							toolbar.enable();
						});
					}
				}, {
					text: 'Collapse All',
					handler: function(){
						var toolbar = this.up('toolbar');
						toolbar.disable();
						
						tree.collapseAll(function() {
							toolbar.enable();
						});
					}
				}]
			}]
		});
	
		return tree;
	}
    
});

